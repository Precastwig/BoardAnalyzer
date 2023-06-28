package boardanalyzer;

import boardanalyzer.board_logic.BoardSave;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.board_logic.analysis.HeatmapGenerator;
import boardanalyzer.board_logic.analysis.HoldSuggestionGenerator;
import boardanalyzer.ui.*;
import boardanalyzer.utils.Vector2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BoardPanel extends JPanel implements ActionListener, ChangeListener, KeyListener {
	private enum BoardPanelState {
		LOAD_IMAGE,
		CORNER_MOVE,
		LOWEST_HAND_HOLD_SET,
		WAITING,
		HOLD_SELECTED,
		BOARD_STATS_UP
	}
	static public String BOARD_EXTENSION = "board";
	private String DEFAULT_BOARD_NAME = "default";
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Font DEFAULT_FONT = new Font("Times New Roman",
            Font.BOLD,
            50);
	private BoardPanelState m_state;
	private final JFileChooser m_file_chooser;
	
	private BoardSave m_board_save;
	private File m_current_loaded_board_file;
	private Vector2 m_mouse_position;
	enum DragState {
		NO_DRAG,
		DIRECTION_DRAG,
		WIDTH_DRAG,
		LOCATION_DRAG,
		PANNING_DRAG,
		CORNER_DRAG
	}
	private DragState m_drag_state;
	private int m_corner_index_dragging;
	private Vector2 m_hold_original_pos;
	private double m_board_zoom_factor;
	private Vector2 m_previous_render_offset; // For use while dragging is happening
	private Vector2 m_max_render_offset;
	private Vector2 m_render_offset;

	private Hold m_selected_hold;
	private final SidePanel m_side_panel;
	private final InstructionPanel m_instruction_panel;
		
	public BoardPanel(SidePanel sp, InstructionPanel ip) {
		m_side_panel = sp;
		m_instruction_panel= ip;
		m_state = BoardPanelState.LOAD_IMAGE;
		m_drag_state = DragState.NO_DRAG;
		m_corner_index_dragging = -1;
		m_board_zoom_factor = 1.0;
		m_max_render_offset = new Vector2(0,0);
		m_render_offset = new Vector2();

		m_file_chooser = new JFileChooser();
		m_file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		try {
			File f = new File(DEFAULT_BOARD_NAME + "." + BOARD_EXTENSION);
			if (!openSavedBoard(f)) {
				// If a board can't be opened, then create a new one
				m_board_save = new BoardSave();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		m_side_panel.m_board_stats.updateLabels(m_board_save.m_board);
		
		CanvasMouseListener mouselisten = new CanvasMouseListener();
        this.addMouseListener(mouselisten);
		this.addMouseWheelListener(mouselisten);
        this.addMouseMotionListener(mouselisten);
        this.requestFocus();
        this.setLayout(new GridLayout(3, 1, 50, 200));
        this.setBackground(Color.white);
	}

	public static Color blend(ArrayList<Color> colour_list) {
		if (colour_list == null || colour_list.size() <= 0) {
			return null;
		}
		float ratio = 1f / ((float) colour_list.size());

		int a = 0;
		int r = 0;
		int g = 0;
		int b = 0;

		for (Color color : colour_list) {
			int rgb = color.getRGB();
			int a1 = (rgb >> 24 & 0xff);
			int r1 = ((rgb & 0xff0000) >> 16);
			int g1 = ((rgb & 0xff00) >> 8);
			int b1 = (rgb & 0xff);
			a += ((int) a1 * ratio);
			r += ((int) r1 * ratio);
			g += ((int) g1 * ratio);
			b += ((int) b1 * ratio);
		}

		return new Color(a << 24 | r << 16 | g << 8 | b);
	}
	
	public Color getColorFromHold(Hold h) {
		int alpha = 255;
		if (m_state == BoardPanelState.HOLD_SELECTED) {
			if (h != m_selected_hold) {
				alpha = 126;
			}
		}
		if (h.isFoot()) {
			return new Color(0,0,0, alpha);
		}

		ArrayList<Color> colours = new ArrayList<Color>();


		if (h.isJug()) {
			colours.add(new Color(49, 141, 49));
		}
		if (h.isCrimp()) {
			colours.add(new Color(176, 62, 62));
		}
		if (h.isPinch()) {
			colours.add(new Color(68,51,121));
		}
		if (h.isPocket()) {
			colours.add(new Color(141,49,101));
		}
		if (h.isSloper()) {
			colours.add(new Color(176,156,62));
		}
		Color col = blend(colours);
		if (col == null) {
			return Color.GRAY;
		}
		return new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
	}
	
	static public Vector2 getPointOnCircleFromRad(double rad, Vector2 circle_centre, Vector2 circle_size) {
		double unit_vector_x = Math.cos(rad);
		double unit_vector_y = Math.sin(rad);
		return new Vector2(circle_centre.x + (unit_vector_x * circle_size.x / 2.0), 
				circle_centre.y + (unit_vector_y * circle_size.y / 2.0));
	}

	public Vector2 transformBoardToRender(Vector2 pos) {
		Vector2 new_pos = new Vector2(pos.x * m_board_zoom_factor, pos.y * m_board_zoom_factor);
		return applyRenderOffset(new_pos);
	}

	public Vector2 applyRenderOffset(Vector2 pos) {
		return new Vector2(pos.x + m_render_offset.x, pos.y + m_render_offset.y);
	}

	public Vector2 applyRenderOffsetInverse(Vector2 pos) {
		return new Vector2(pos.x - m_render_offset.x, pos.y - m_render_offset.y);
	}

	public Vector2 transformRenderToBoard(Vector2 pos) {
		Vector2 new_pos = applyRenderOffsetInverse(pos);
		new_pos = new Vector2(new_pos.x / m_board_zoom_factor, new_pos.y / m_board_zoom_factor);
		return new_pos;
	}
	
	@Override
    public void update(Graphics g) {
	    super.paintComponents(g);
//	    System.out.println("Drawing");
	    Graphics2D g2 = (Graphics2D) g; 
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	    		RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
	    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if (m_state != BoardPanelState.LOAD_IMAGE && m_board_save.m_board_image != null) {
			int resized_board_width = (int)(this.getSize().getWidth() * m_board_zoom_factor);
			int resized_board_height = (int)(this.getSize().getHeight() * m_board_zoom_factor);

			g2.drawImage(
					m_board_save.m_board_image,
					(int)m_render_offset.x, (int)m_render_offset.y,
					(int)resized_board_width, (int)resized_board_height,this);

			// Draw holds
			ArrayList<Hold> holds = m_board_save.m_board.getHolds();
			if (!holds.isEmpty()) {
				for (Hold h : holds) {
					Color c = getColorFromHold(h);
					Hold hold_to_render = h;
					if (m_state == BoardPanelState.HOLD_SELECTED && h == m_selected_hold) {
						hold_to_render = m_side_panel.m_hold_selection_settings.getNewHold();
					}
					Vector2 circle_size = new Vector2(
							hold_to_render.size().x * m_board_zoom_factor,
							hold_to_render.size().y * m_board_zoom_factor);
					double direction = hold_to_render.direction();
					Vector2 circle_pos = transformBoardToRender(hold_to_render.position());

					Vector2 circle_centre = transformBoardToRender(hold_to_render.getCentrePoint());
					Vector2 point_on_circle_in_dir =
							getPointOnCircleFromRad(
									direction,
									circle_centre,
									circle_size
							);
					int lineWidth = Math.max((int)(circle_size.x / 10.0), 3);

					if (m_state == BoardPanelState.HOLD_SELECTED && h == m_selected_hold) {
						// Draw background highlight
						Shape highlight = new Ellipse2D.Double(circle_pos.x, circle_pos.y, circle_size.x, circle_size.y);
						g2.setStroke(new BasicStroke(lineWidth + 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
						g2.setColor(Color.WHITE);
						g2.draw(highlight);
						g2.drawLine(
								(int) point_on_circle_in_dir.x,
								(int) point_on_circle_in_dir.y,
								(int) circle_centre.x,
								(int) circle_centre.y
						);
					}

					g2.setColor(c);
					g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));

					g2.drawLine(
							(int) point_on_circle_in_dir.x,
							(int) point_on_circle_in_dir.y,
							(int) circle_centre.x,
							(int) circle_centre.y
					);
					Shape circle = new Ellipse2D.Double(circle_pos.x, circle_pos.y, circle_size.x, circle_size.y);
					g2.draw(circle);
				}
			}
			
			// Draw board corners and lines between them
			ArrayList<Vector2> corners = m_board_save.m_board.getCorners();
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			ArrayList<Vector2> transformed_corners = new ArrayList<>();
			for (Vector2 corner : corners) {
				transformed_corners.add(transformBoardToRender(corner));
			}
			for (int i = 0; i < transformed_corners.size(); i++) {
				if (i == m_corner_index_dragging) {
					g2.setColor(Color.WHITE);
				}
				int corner_size = 15;
				g2.fillRect(
						(int)(transformed_corners.get(i).x - (corner_size / 2.0)),
						(int)(transformed_corners.get(i).y - (corner_size / 2.0)),
						corner_size, corner_size);
				if (i == m_corner_index_dragging) {
					g2.setColor(Color.BLACK);
				}
				if (i == 0) {
					if (transformed_corners.size() == 4) {
						// We have all 4 corners, so connect 0 to 3
						g2.drawLine(
								(int)transformed_corners.get(0).x,
								(int)transformed_corners.get(0).y,
								(int)transformed_corners.get(3).x,
								(int)transformed_corners.get(3).y
								);
					}
				} else {					
					g2.drawLine(
							(int)transformed_corners.get(i-1).x,
							(int)transformed_corners.get(i-1).y,
							(int)transformed_corners.get(i).x,
							(int)transformed_corners.get(i).y
							);
				}
			}
		}

		if (m_state == BoardPanelState.LOWEST_HAND_HOLD_SET) {
			// Show previous lowest
			int lowest_y = (int)m_board_save.m_board.getLowestAllowedHandHoldHeight();
			g2.setColor(new Color(64,201,92));
			g2.drawLine(0, lowest_y, (int)m_board_save.m_board.getBoardWidth(), lowest_y);
			// Show where mouse is
			g2.setColor(new Color(15,255,63));
			g2.drawLine(0, (int)m_mouse_position.y,(int)m_board_save.m_board.getBoardWidth(), (int)m_mouse_position.y );
		}
		
		if (m_state == BoardPanelState.BOARD_STATS_UP) {
		    super.paintComponents(g);
		}
	}
	
	@Override
    public void paint(Graphics g) {
        update(g);
    }

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (Objects.equals(e.getActionCommand(), "NewBoard")) {
			newBoard();
		} else if (Objects.equals(e.getActionCommand(), "OpenBoard")) {
			openBoard();
		} else if (Objects.equals(e.getActionCommand(), "Save")) {
			saveBoard(m_current_loaded_board_file);
		} else if (Objects.equals(e.getActionCommand(), "SaveHold")) {
			saveSelectedHold();
		} else if (Objects.equals(e.getActionCommand(), "DeleteHold")) {
			deleteSelectedHold();
		} else if (Objects.equals(e.getActionCommand(), "GenerateHeatmap")) {
			generateHeatmap();
		} else if (Objects.equals(e.getActionCommand(), "MoveCorners")) {
			flipCornerMoveState();
		} else if (Objects.equals(e.getActionCommand(), "GenerateHold")) {
			generateHold();
			//// Something
		} else if (Objects.equals(e.getActionCommand(), "ShowHoldStats")) {
			showHoldStats();
		} else if (Objects.equals(e.getActionCommand(), "ClearAllHolds")) {
			clearAllHolds();
		} else if (Objects.equals(e.getActionCommand(), "SuggestHoldType")) {
			suggestHoldType();
		} else if (Objects.equals(e.getActionCommand(), "SuggestHoldDirection")) {
			suggestHoldDirection();
		} else if (Objects.equals(e.getActionCommand(), "SetLowestHandHoldHeight")) {
			setLowestHandHoldHeight();
		}
		m_side_panel.m_board_stats.updateLabels(m_board_save.m_board); // Inefficient, but covers our bases
		repaint();
	}

	private void setLowestHandHoldHeight() {
		setLowestHandHoldState();
	}
	
	private void suggestHoldType() {
		if (m_selected_hold != null) {
			HoldSuggestionGenerator generator = new HoldSuggestionGenerator(
					m_board_save.m_board, 
					m_board_save.m_board_dimensions,
					m_board_save.m_hold_type_ratio, 
					m_board_save.m_hold_direction_ratio,
					m_board_save.m_hold_minimum_size,
					m_board_save.m_hold_maximum_size);
			Hold.Type new_type = generator.suggestHoldType(m_selected_hold);
			m_side_panel.m_hold_selection_settings.setToHoldType(new_type);
		}
	}
	
	private void suggestHoldDirection() {
		if (m_selected_hold != null) {
			HoldSuggestionGenerator generator = new HoldSuggestionGenerator(
					m_board_save.m_board,
					m_board_save.m_board_dimensions,
					m_board_save.m_hold_type_ratio, 
					m_board_save.m_hold_direction_ratio,
					m_board_save.m_hold_minimum_size,
					m_board_save.m_hold_maximum_size);
			double new_dir = generator.suggestHoldDirection(m_selected_hold);
			m_side_panel.m_hold_selection_settings.setDirection(new_dir);
		}
	}
	
	private int showOpenBoardConfirmDialog() {
		return JOptionPane.showConfirmDialog(
				this, 
				"You already have a board open, any unsaved changes will be lost, are you sure?",
				"Are you sure?", 
				JOptionPane.YES_NO_OPTION, 
				JOptionPane.QUESTION_MESSAGE);
	}
	
	private void openBoard() {
		if (m_board_save.m_board_image != null) {
			int choice = showOpenBoardConfirmDialog();
			if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.CLOSED_OPTION) {
				return;
			}
		}
		m_file_chooser.setFileFilter(new FileNameExtensionFilter("Board saves", BoardPanel.BOARD_EXTENSION));
		int returnVal = m_file_chooser.showDialog(this, "Open Board Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File chosen_file = m_file_chooser.getSelectedFile();
			System.out.println(chosen_file.getAbsolutePath());
			try {
				openSavedBoard(chosen_file);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void newBoard() {
		if (m_board_save.m_board_image != null) {
			int choice = showOpenBoardConfirmDialog();
			if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.CLOSED_OPTION) {
				return;
			}
		}
		String input = JOptionPane.showInputDialog(null, "Enter board name (leave blank for default)");
		if (input.length() == 0){
			input = DEFAULT_BOARD_NAME;
		}
		m_current_loaded_board_file = new File(input + "." + BOARD_EXTENSION);
		if (m_current_loaded_board_file.exists()) {
			int choice = JOptionPane.showConfirmDialog(
					this, 
					"A board with this name already exists, would you like to overwrite it?",
					"This board already exists", 
					JOptionPane.YES_NO_OPTION, 
					JOptionPane.QUESTION_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return;
			}
		}
		BoardSizeInputPanel size_input = new BoardSizeInputPanel();
		int choice = JOptionPane.showConfirmDialog(null,
				size_input,
				"JOptionPane Example : ",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		m_side_panel.m_board_settings.setBoardDimensions(size_input.getBoardSize());
		if (openFileOpenerDialogAndOpenFile()) {
			saveBoard(m_current_loaded_board_file);
		}
	}
	
	private void clearAllHolds() {
		m_selected_hold = null;
		m_side_panel.m_hold_selection_settings.disableAll();
		m_board_save.m_board.clearAllHolds();
		setWaitingState();
	}
	
	private void showHoldStats() {
		if (m_state != BoardPanelState.BOARD_STATS_UP) {
			add(m_side_panel.m_board_stats.m_hold_type_chart);
			m_state = BoardPanelState.BOARD_STATS_UP;
		} else {
			remove(m_side_panel.m_board_stats.m_hold_type_chart);
			m_state = BoardPanelState.WAITING;
		}
	}
	
	private void generateHold() {
		if (m_selected_hold != null) {
			saveSelectedHold();
		}
		HoldSuggestionGenerator generator = new HoldSuggestionGenerator(
				m_board_save.m_board,
				m_board_save.m_board_dimensions,
				m_board_save.m_hold_type_ratio, 
				m_board_save.m_hold_direction_ratio,
				m_board_save.m_hold_minimum_size,
				m_board_save.m_hold_maximum_size);
		Optional<Hold> new_hold = generator.generateHold(m_side_panel.m_hold_generation_settings);
		if (new_hold.isPresent()) {
			m_board_save.m_board.addHold(new_hold.get());
			selectHold(new_hold.get());
			m_side_panel.m_board_stats.updateLabels(m_board_save.m_board);
		} else { 
			m_instruction_panel.setText("Hold generation failed.");
		}
	}
	
	private void saveBoard(File file) {
		saveSettings();
		FileOutputStream f;
		try {
			f = new FileOutputStream(file);
			ObjectOutputStream o = new ObjectOutputStream(f);
			
			// Write objects to file
			o.writeObject(m_board_save);
			o.flush();
			o.close();
			m_instruction_panel.setText("Board saved to file " + file.getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void generateHeatmap() {
		HeatmapGenerator generator = new HeatmapGenerator(
				m_board_save.m_board,
				m_board_save.m_board_dimensions);
		File output_file = new File("heatmap.png");
		HashSet<Hold.Type> holdtypes = m_side_panel.m_heatmap_settings.getSelectedHoldTypes();
		if (holdtypes.isEmpty()) {
			holdtypes.add(Hold.Type.CRIMP);
			holdtypes.add(Hold.Type.FOOT);
			holdtypes.add(Hold.Type.JUG);
			holdtypes.add(Hold.Type.PINCH);
			holdtypes.add(Hold.Type.POCKET);
			holdtypes.add(Hold.Type.SLOPER);
		}
		m_instruction_panel.setText("Generating heatmap..");
		BufferedImage image = generator.generateHeatmap(
				m_side_panel.m_heatmap_settings.getBrightness(),
				holdtypes,
				m_side_panel.m_heatmap_settings.holdTypesShouldExactlyMatch(),
				m_side_panel.m_heatmap_settings.holdDirectionMatters()
				);
		try {
			ImageIO.write(image, "png", output_file);
			m_instruction_panel.setText("<html>Generated heatmap file</html>");
		} catch (IOException e1) {
			System.out.println("Failed to write output file");
		}
	}
	
	private void saveSettings() {
		try {
			m_board_save.m_board_dimensions = m_side_panel.m_board_settings.getBoardDimensions();
		} catch (java.lang.NumberFormatException e) {
			m_instruction_panel.setText("Board dimensions in non-number format");
		}
		try {
			m_board_save.m_hold_minimum_size = m_side_panel.m_board_settings.getHoldSizeMin();
			m_board_save.m_hold_maximum_size = m_side_panel.m_board_settings.getHoldSizeMax();
		} catch (java.lang.NumberFormatException e) {
			m_instruction_panel.setText("Hold size min/max in non-number format");
		}
		m_board_save.m_hold_type_ratio = m_side_panel.m_board_settings.getHoldTypeRatio();
		m_board_save.m_hold_direction_ratio = m_side_panel.m_board_settings.getHoldDirectionRatio();
	}
	
	private boolean openFileOpenerDialogAndOpenFile() {
		m_file_chooser.setFileFilter(new FileNameExtensionFilter("JPG and PNG images", "jpeg", "jpg", "png"));
		int returnVal = m_file_chooser.showDialog(this, "Open Image");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File chosen_file = m_file_chooser.getSelectedFile();
			try {
				openImageFile(chosen_file);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }
		return false;
	}
	
	private void openImageFile(File file) throws IOException {
		if (m_board_save.m_board_image != null) {
			// We already have an image, which means we should ask if we should 
			// also clear the save
			int result = JOptionPane.showConfirmDialog(null, "Board data already exists, Would you like to clear it?");
			if (result == JOptionPane.YES_OPTION) {
				// Clear board data
				m_board_save = new BoardSave();
			} else if (result == JOptionPane.NO_OPTION) {
				///// TODO remap the old hold positions and corners to the new image size
			} else {
				// Do nothing
				return;
			}
		}
		FileInputStream fis = new FileInputStream(file);
		m_board_save.m_board_image = ImageIO.read(fis);
		if (m_board_save.m_board_image == null) {
			throw new IOException("ImageIO can only load GIF PNG JPEG BMP and WBMP");
		}
		int new_width = m_board_save.m_board_image.getWidth(this);
		int new_height = m_board_save.m_board_image.getHeight(this);
		
		double new_ratio = (double)new_width / (double)new_height;
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screen_width = screenSize.getWidth();
		double screen_height = screenSize.getHeight();
		
		if (new_width > screen_width) {
			new_width = (int)screen_width - 50;
			new_height = (int)(new_width / new_ratio);
		}
		if (new_height > screen_height) {
			new_height = (int)screen_height - 50;
			new_width = (int)(new_height * new_ratio);
		}
		MainWindow.m_frame.setSize(
				new_width, 
				new_height);
		m_board_save.m_board.setBoardDimensions(new_width, new_height);
		setDefaultBoardCorners(new Vector2(new_width, new_height));
		setWaitingState();
		repaint();
	}

	private void setDefaultBoardCorners(Vector2 board_size) {
		Vector2 margin = new Vector2(board_size.x * 0.2, board_size.y * 0.2);
		m_board_save.m_board.addCorner(margin);
		m_board_save.m_board.addCorner(new Vector2(board_size.x - margin.x, margin.y));
		m_board_save.m_board.addCorner(new Vector2(board_size.x - margin.x, board_size.y - margin.y));
		m_board_save.m_board.addCorner(new Vector2(margin.x, board_size.y - margin.y));
	}
	
	private boolean openSavedBoard(File f) throws IOException, ClassNotFoundException {
		FileInputStream fi;
		try {
			fi = new FileInputStream(f);
			ObjectInputStream oi = new ObjectInputStream(fi);
			m_board_save = (BoardSave)oi.readObject();
			oi.close();
			m_side_panel.m_board_settings.setBoardDimensions(m_board_save.m_board_dimensions);
			m_side_panel.m_board_settings.setHoldSizeMin(m_board_save.m_hold_minimum_size);
			m_side_panel.m_board_settings.setHoldSizeMax(m_board_save.m_hold_maximum_size);
			m_side_panel.m_board_settings.setHoldTypeRatio(m_board_save.m_hold_type_ratio);
			m_side_panel.m_board_settings.setHoldDirectionRatio(m_board_save.m_hold_direction_ratio);
			MainWindow.m_frame.setSize(
					(int)m_board_save.m_board.getBoardWidth(), 
					(int)m_board_save.m_board.getBoardHeight());
			setWaitingState();
			m_current_loaded_board_file = f;
			m_side_panel.m_board_stats.updateLabels(m_board_save.m_board);
			repaint();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			System.out.println(f.getAbsolutePath());
			return false;
		}
		return true;
	}
	
	private void deleteSelectedHold() {
		m_board_save.m_board.removeHold(m_selected_hold);
		deselectHold();
		m_side_panel.m_board_stats.updateLabels(m_board_save.m_board);
	}
	
	private void deselectHold() {
		m_selected_hold = null;
		m_state = BoardPanelState.WAITING;
		m_side_panel.m_hold_selection_settings.disableAll();
	}
	
	private void saveSelectedHold() {
		if (m_selected_hold == null) {
			// nothing to do
			return;
		}

		if (m_board_save.m_board.isOutsideBorders(m_side_panel.m_hold_selection_settings.getNewHold().getCentrePoint())) {
			selectHold(m_selected_hold);
			m_instruction_panel.setText("Error! Cannot place hold outside of border");
			return;
		}

		if (m_side_panel.m_hold_selection_settings.isCrimp()) {
			m_selected_hold.addType(Hold.Type.CRIMP);
		}
		if (m_side_panel.m_hold_selection_settings.isJug()) {
			m_selected_hold.addType(Hold.Type.JUG);
		}
		if (m_side_panel.m_hold_selection_settings.isSloper()) {
			m_selected_hold.addType(Hold.Type.SLOPER);
		}
		if (m_side_panel.m_hold_selection_settings.isPocket()) {
			m_selected_hold.addType(Hold.Type.POCKET);
		}
		if (m_side_panel.m_hold_selection_settings.isPinch()) {
			m_selected_hold.addType(Hold.Type.PINCH);
		}
		if (m_side_panel.m_hold_selection_settings.isFoot()) {
			m_selected_hold.addType(Hold.Type.FOOT);
		}
		m_selected_hold.setDirection(m_side_panel.m_hold_selection_settings.getDirection());
		m_selected_hold.setSize(m_side_panel.m_hold_selection_settings.getHoldSize());
		m_selected_hold.setPosition(m_side_panel.m_hold_selection_settings.getHoldPosition());
		deselectHold();
		m_side_panel.m_board_stats.updateLabels(m_board_save.m_board);
	}
	
	private void selectHold(Hold h) {
		m_selected_hold = h;
		m_side_panel.m_hold_selection_settings.selectHold(m_selected_hold);
		m_state = BoardPanelState.HOLD_SELECTED;
	}
		
	private void tryClick(double x, double y) {
		switch (m_state) {
			case WAITING -> {
				if (m_board_save.m_board.existsHold(x, y)) {
					try {
						saveSelectedHold();
						selectHold(m_board_save.m_board.getHold(x, y));
					} catch (IllegalAccessException e) {
						System.out.println("Can't find hold! Logic mismatch in existshold and gethold");
						e.printStackTrace();
					}
				} else {
					// No hold where we clicked
					Optional<Hold> hold_maybe = m_board_save.m_board.createHold(new Vector2(x, y));
					if (hold_maybe.isPresent()) {
						selectHold(hold_maybe.get());
					} else {
						m_instruction_panel.setText("Selection is outside of board limits");
					}
				}
			}
			case LOWEST_HAND_HOLD_SET -> {
				m_board_save.m_board.setLowestAllowedHandHoldHeight(y);
				setWaitingState();
			}
		}
        // If something changes, we should do this: 
        repaint();
    }

	private void setLowestHandHoldState() {
		m_state = BoardPanelState.LOWEST_HAND_HOLD_SET;
		m_instruction_panel.setText("Set the lowest position on the board that is acceptable for a hand hold");
	}

	private void flipCornerMoveState() {
		if (m_state == BoardPanelState.WAITING) {
			m_state = BoardPanelState.CORNER_MOVE;
			m_instruction_panel.setText("Move the corners to match the edges of your board");
		} else if (m_state == BoardPanelState.CORNER_MOVE) {
			setWaitingState();
		}
	}
	
	private void setWaitingState() {
		m_state = BoardPanelState.WAITING;
		m_instruction_panel.setText("Click anywhere to add a hold, or click on an existing hold to edit.");
	}

	private void setMaxRenderOffset() {
		double resized_board_width = this.getSize().getWidth() * m_board_zoom_factor;
		double resized_board_height = this.getSize().getHeight() * m_board_zoom_factor;
		double max_x_offset = this.getSize().getWidth() - resized_board_width;
		double max_y_offset = this.getSize().getHeight() - resized_board_height;
		m_max_render_offset = new Vector2(max_x_offset, max_y_offset);
		capRenderOffsetByMax();
	}

	private void capRenderOffsetByMax() {
		m_render_offset.x = Math.max(m_render_offset.x, m_max_render_offset.x);
		m_render_offset.x = Math.min(m_render_offset.x, 0);
		m_render_offset.y = Math.max(m_render_offset.y, m_max_render_offset.y);
		m_render_offset.y = Math.min(m_render_offset.y, 0);
	}
	
	private class CanvasMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener  {
        @Override
        public void mouseClicked(MouseEvent e) {
			// Transform the click into the potentially zoomed/shifted image space
			if (e.getButton() == 1) {
				Vector2 new_click_pos = transformRenderToBoard(new Vector2(e.getX(), e.getY()));
				tryClick(new_click_pos.x, new_click_pos.y);

			}
		}
        @Override
        public void mousePressed(MouseEvent e) {
			Vector2 click_pos = transformRenderToBoard(new Vector2(e.getX(), e.getY()));
			if (e.getButton() == 1) {
				// Primary click
				if (m_state == BoardPanelState.HOLD_SELECTED) {
					if (m_side_panel.m_hold_selection_settings.getNewHold().contains(click_pos.x, click_pos.y)) {
						m_drag_state = DragState.DIRECTION_DRAG;
					} else {
						m_mouse_position = click_pos;
						m_drag_state = DragState.LOCATION_DRAG;
						m_hold_original_pos = m_side_panel.m_hold_selection_settings.getHoldPosition();
					}
				} else if (m_state == BoardPanelState.CORNER_MOVE) {
					m_mouse_position = click_pos;
					m_corner_index_dragging = m_board_save.m_board.getNearestCornerIndex(click_pos);
					// Repurposing m_hold_original_pos like a heathen
					m_hold_original_pos = m_board_save.m_board.getCorners().get(m_corner_index_dragging);
					m_drag_state = DragState.CORNER_DRAG;
				}
			} else if (e.getButton() == 2 || e.getButton() == 3 ) {
				// Middle click or right click
				m_mouse_position = click_pos;
				m_previous_render_offset = new Vector2(m_render_offset);
				m_drag_state = DragState.PANNING_DRAG;
			}
		}
        @Override
        public void mouseReleased(MouseEvent e) {
        	if (m_state == BoardPanelState.HOLD_SELECTED) {
				m_drag_state = DragState.NO_DRAG;
				m_corner_index_dragging = -1;
        	}
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
			switch(m_drag_state) {
				case DIRECTION_DRAG -> {
					m_mouse_position = transformRenderToBoard(new Vector2(e.getX(), e.getY()));
					Vector2 circle_centre = m_selected_hold.getCentrePoint();

					int mouse_vector_x = (int) (m_mouse_position.x - circle_centre.x);
					int mouse_vector_y = (int) (m_mouse_position.y - circle_centre.y);

					// I have no idea why this needs to be multiplied by 2, probably the same reason the size
					// needs to be divided by two when rendering, in other words, I've made garbage.
					double vector_size = Math.hypot(mouse_vector_x, mouse_vector_y) * 2;

					double mouse_unit_vector_x = mouse_vector_x / vector_size;
					double mouse_unit_vector_y = mouse_vector_y / vector_size;

					m_side_panel.m_hold_selection_settings.setDirection(Math.atan2(mouse_unit_vector_y, mouse_unit_vector_x));

					Vector2 old_size = m_side_panel.m_hold_selection_settings.getHoldSize();
					double old_ratio = old_size.x / old_size.y;

					m_side_panel.m_hold_selection_settings.setHoldSize(
							new Vector2(old_ratio * vector_size, vector_size));
					repaint();
				}
				case WIDTH_DRAG -> {
					//// TODO make ellipse creation work, a lot of underlying functions in analysis will require changing too
					repaint();
				}
				case LOCATION_DRAG -> {
					Vector2 mouse_pos = transformRenderToBoard(new Vector2(e.getX(), e.getY()));
					Vector2 mouse_movement = new Vector2(mouse_pos.x - m_mouse_position.x, mouse_pos.y - m_mouse_position.y);
					Vector2 new_pos = new Vector2(
							m_hold_original_pos.x + mouse_movement.x,
							m_hold_original_pos.y + mouse_movement.y);
					m_side_panel.m_hold_selection_settings.setPosition(new_pos);
					repaint();
				}
				case PANNING_DRAG -> {
					if (m_board_zoom_factor <= 1.0) {
						// Do nothing
						return;
					}
					Vector2 mouse_pos = new Vector2(e.getX() - m_previous_render_offset.x, e.getY() - m_previous_render_offset.y);
					mouse_pos.x = mouse_pos.x / m_board_zoom_factor;
					mouse_pos.y = mouse_pos.y / m_board_zoom_factor;
					m_render_offset = new Vector2(
							m_previous_render_offset.x + mouse_pos.x - m_mouse_position.x,
							m_previous_render_offset.y + mouse_pos.y - m_mouse_position.y);
					capRenderOffsetByMax();
					repaint();
				}
				case CORNER_DRAG -> {
					if (0 < m_corner_index_dragging && m_corner_index_dragging < 4) {
						Vector2 mouse_pos = transformRenderToBoard(new Vector2(e.getX(), e.getY()));
						Vector2 mouse_movement = new Vector2(mouse_pos.x - m_mouse_position.x, mouse_pos.y - m_mouse_position.y);
						// Repurposing m_hold_original_pos like a heathen, here it just represents the original position
						// of the corner
						Vector2 new_pos = new Vector2(
								m_hold_original_pos.x + mouse_movement.x,
								m_hold_original_pos.y + mouse_movement.y);
						m_board_save.m_board.moveCorner(m_corner_index_dragging, new_pos);
						repaint();
					}
				}
			}
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        	//System.out.println("Clicked x:" + e.getX() + " y: " + e.getY());
			if (m_state == BoardPanelState.LOWEST_HAND_HOLD_SET) {
				m_mouse_position = transformRenderToBoard(new Vector2(e.getX(), e.getY()));
				repaint();
			}
        }

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int notches = e.getWheelRotation();
			double amount = Math.abs((double) notches / 10.0);
			double zoom_before = m_board_zoom_factor;
			if (notches < 0) {
				// In
				m_board_zoom_factor += amount;
				m_board_zoom_factor = Math.min(m_board_zoom_factor, 3);
			} else {
				// out
				m_board_zoom_factor -= amount;
				m_board_zoom_factor = Math.max(m_board_zoom_factor, 1);
			}
			// Move the board by where the mouse is
			// This is to create the "zooming in on the mouse" behaviour
			Vector2 mouse_position_movement = new Vector2(
					(e.getX() * m_board_zoom_factor) - (e.getX() * zoom_before),
					(e.getY() * m_board_zoom_factor) - (e.getY() * zoom_before));
			m_render_offset = new Vector2(m_render_offset.x-mouse_position_movement.x, m_render_offset.y-mouse_position_movement.y);
			setMaxRenderOffset();
			repaint();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (m_state == BoardPanelState.HOLD_SELECTED) {
			if (e.getKeyCode() == KeyEvent.VK_DELETE &&  e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				deleteSelectedHold();
				repaint();
			}
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				saveSelectedHold();
			}
		}
		/// Ctrl + S
		int onmask = KeyEvent.CTRL_DOWN_MASK;
		if (e.getKeyCode() == KeyEvent.VK_S && (e.getModifiersEx() & (onmask)) == onmask) {
			if (m_state == BoardPanelState.HOLD_SELECTED) {
				saveSelectedHold();
				repaint();
			} else {
				saveBoard(m_current_loaded_board_file);
			}
		}
	}
}
