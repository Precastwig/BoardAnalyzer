package boardanalyzer;

import boardanalyzer.board_logic.BoardSave;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.board_logic.analysis.HeatmapGenerator;
import boardanalyzer.board_logic.analysis.HoldSuggestionGenerator;
import boardanalyzer.ui.*;
import boardanalyzer.ui.basic_elements.BoardFileFilter;
import boardanalyzer.ui.basic_elements.ImageFileFilter;
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

public class BoardPanel extends JPanel implements ActionListener, ChangeListener, KeyListener {
	private enum AppState {
		LOAD_IMAGE,
		CORNER_SET,
		LOWEST_HAND_HOLD_SET,
		WAITING,
		HOLD_SELECTED,
		BOARD_STATS_UP,
		THINKING
	}
	static public String BOARD_EXTENSION = ".board";
	private String DEFAULT_BOARD_NAME = "default";
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Font DEFAULT_FONT = new Font("Times New Roman",
            Font.BOLD,
            50);
	private AppState m_state;
	private final JFileChooser m_file_chooser;
	
	private BoardSave m_board_save;
	private File m_current_loaded_board_file;
	private int m_mouse_x;
	private int m_mouse_y;
	private boolean m_dragging_direction;
	private boolean m_dragging_width;
	private boolean m_dragging_location;
	private Vector2 m_dragging_original_pos;
	
	private Hold m_selected_hold;
	private final HoldSelectionSettings m_hold_selection_settings;
	private final HoldGenerationSettings m_hold_generation_settings;
	private final BoardSettings m_board_settings;
	private final HeatmapSettings m_heatmap_settings;
	private final BoardStatistics m_board_statistics;
		
	public BoardPanel(
			HoldSelectionSettings hss,
			BoardSettings bs,
			HeatmapSettings hs,
			HoldGenerationSettings hgs,
			BoardStatistics bstats) {
		m_hold_selection_settings = hss;
		m_hold_generation_settings = hgs;
		m_board_settings = bs;
		m_heatmap_settings = hs;
		m_board_statistics = bstats;
		m_state = AppState.LOAD_IMAGE;
		m_dragging_direction = false;

		m_file_chooser = new JFileChooser();
		m_file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		try {
			File f = new File(DEFAULT_BOARD_NAME + BOARD_EXTENSION);
			if (!openSavedBoard(f)) {
				// If a board can't be opened, then create a new one
				m_board_save = new BoardSave();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		m_board_statistics.updateLabels(m_board_save.m_board);
		
		CanvasMouseListener mouselisten = new CanvasMouseListener();
        this.addMouseListener(mouselisten);
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
		if (m_state == AppState.HOLD_SELECTED) {
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
		if (m_state != AppState.LOAD_IMAGE && m_board_save.m_board_image != null) {
			g2.drawImage(m_board_save.m_board_image, 0, 0, (int)this.getSize().getWidth(), (int)this.getSize().getHeight(),this);			
		
			// Draw holds
			ArrayList<Hold> holds = m_board_save.m_board.getHolds();
			if (!holds.isEmpty()) {
				for (Hold h : holds) {
					Color c = getColorFromHold(h);
					Hold hold_to_render = h;
					int lineWidth = 5;
					if (m_state == AppState.HOLD_SELECTED && h == m_selected_hold) {
						hold_to_render = m_hold_selection_settings.getNewHold();
						lineWidth += 2;
					}
					Vector2 circle_size = hold_to_render.size();
					double direction = hold_to_render.direction();
					int circle_pos_x = (int) hold_to_render.position().x;
					int circle_pos_y = (int) hold_to_render.position().y;

					Vector2 circle_centre = hold_to_render.getCentrePoint();
					Vector2 point_on_circle_in_dir =
							getPointOnCircleFromRad(
									direction,
									circle_centre,
									circle_size
							);

					if (m_state == AppState.HOLD_SELECTED && h == m_selected_hold) {
						// Draw background highlight
						Shape highlight = new Ellipse2D.Double(circle_pos_x, circle_pos_y, circle_size.x, circle_size.y);
						g2.setStroke(new BasicStroke(lineWidth + 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
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
					Shape circle = new Ellipse2D.Double(circle_pos_x, circle_pos_y, circle_size.x, circle_size.y);
					g2.draw(circle);
				}
			}
			
			// Draw board corners and lines between them
			ArrayList<Vector2> corners = m_board_save.m_board.getCorners();
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			
			for (int i = 0; i < corners.size(); i++) {
				g2.drawRect(
						(int)corners.get(i).x, 
						(int)corners.get(i).y, 
						5, 5);
				if (i == 0) {
					if (corners.size() == 4) {
						// We have all 4 corners, so connect 0 to 3
						g2.drawLine(
								(int)corners.get(0).x, 
								(int)corners.get(0).y, 
								(int)corners.get(3).x, 
								(int)corners.get(3).y
								);
					}
				} else {					
					g2.drawLine(
							(int)corners.get(i-1).x, 
							(int)corners.get(i-1).y, 
							(int)corners.get(i).x, 
							(int)corners.get(i).y
							);
				}
			}
		}

		if (m_state == AppState.LOWEST_HAND_HOLD_SET) {
			// Show previous lowest
			int lowest_y = (int)m_board_save.m_board.getLowestAllowedHandHoldHeight();
			g2.setColor(new Color(64,201,92));
			g2.drawLine(0, lowest_y, (int)m_board_save.m_board.getBoardWidth(), lowest_y);
			// Show where mouse is
			g2.setColor(new Color(15,255,63));
			g2.drawLine(0, m_mouse_y,(int)m_board_save.m_board.getBoardWidth(), m_mouse_y );
		}
		
		if (m_state == AppState.BOARD_STATS_UP) {
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
		} else if (Objects.equals(e.getActionCommand(), "SetCorners")) {
			m_board_save.m_board.clearCorners();
			setCornerSetState();
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
		m_board_statistics.updateLabels(m_board_save.m_board); // Inefficient, but covers our bases
		repaint();
	}

	private void setLowestHandHoldHeight() {
		m_state = AppState.LOWEST_HAND_HOLD_SET;
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
			m_hold_selection_settings.setToHoldType(new_type);
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
			m_hold_selection_settings.setDirection(new_dir);
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
		m_file_chooser.setFileFilter(new BoardFileFilter());
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
		m_current_loaded_board_file = new File(input + BOARD_EXTENSION);
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
		openFileOpenerDialogAndOpenFile();
		saveBoard(m_current_loaded_board_file);
	}
	
	private void clearAllHolds() {
		m_selected_hold = null;
		m_hold_selection_settings.disableAll();
		m_board_save.m_board.clearAllHolds();
		setWaitingState();
	}
	
	private void showHoldStats() {
		if (m_state != AppState.BOARD_STATS_UP) {
			add(m_board_statistics.m_hold_type_chart);
			m_state = AppState.BOARD_STATS_UP;			
		} else {
			remove(m_board_statistics.m_hold_type_chart);
			m_state = AppState.WAITING;
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
		Optional<Hold> new_hold = generator.generateHold(m_hold_generation_settings);
		if (new_hold.isPresent()) {
			m_board_save.m_board.addHold(new_hold.get());
			selectHold(new_hold.get());
			m_board_statistics.updateLabels(m_board_save.m_board);
		} else { 
			MainWindow.m_instruction_panel.m_instruction_label.setText("Hold generation failed.");
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
			MainWindow.setInstructionText("Board saved to file " + file.getAbsolutePath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void generateHeatmap() {
		HeatmapGenerator generator = new HeatmapGenerator(
				m_board_save.m_board,
				m_board_save.m_board_dimensions);
		File output_file = new File("heatmap.png");
		HashSet<Hold.Type> holdtypes = m_heatmap_settings.getSelectedHoldTypes();
		if (holdtypes.isEmpty()) {
			holdtypes.add(Hold.Type.CRIMP);
			holdtypes.add(Hold.Type.FOOT);
			holdtypes.add(Hold.Type.JUG);
			holdtypes.add(Hold.Type.PINCH);
			holdtypes.add(Hold.Type.POCKET);
			holdtypes.add(Hold.Type.SLOPER);
		}
		MainWindow.setInstructionText("Generating heatmap..");
		BufferedImage image = generator.generateHeatmap(
				m_heatmap_settings.getBrightness(),
				holdtypes, 
				m_heatmap_settings.holdTypesShouldExactlyMatch(),
				m_heatmap_settings.holdDirectionMatters()
				);
		try {
			ImageIO.write(image, "png", output_file);
			MainWindow.m_instruction_panel.m_instruction_label.setText("<html>Generated heatmap file</html>");
		} catch (IOException e1) {
			System.out.println("Failed to write output file");
		}
	}
	
	private void saveSettings() {
		try {
			m_board_save.m_board_dimensions = m_board_settings.getBoardDimensions();
		} catch (java.lang.NumberFormatException e) {
			MainWindow.setInstructionText("Board dimensions in non-number format");
		}
		try {
			m_board_save.m_hold_minimum_size = m_board_settings.getHoldSizeMin();
			m_board_save.m_hold_maximum_size = m_board_settings.getHoldSizeMax();
		} catch (java.lang.NumberFormatException e) {
			MainWindow.setInstructionText("Hold size min/max in non-number format");
		}
		m_board_save.m_hold_type_ratio = m_board_settings.getHoldTypeRatio();
		m_board_save.m_hold_direction_ratio = m_board_settings.getHoldDirectionRatio();
	}
	
	private void openFileOpenerDialogAndOpenFile() {
		m_file_chooser.setFileFilter(new ImageFileFilter());
		int returnVal = m_file_chooser.showDialog(this, "Open Image");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File chosen_file = m_file_chooser.getSelectedFile();
			try {
				openImageFile(chosen_file);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
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
		if (m_board_save.m_board.areAllCornersSet()) {
			setWaitingState();
		} else {
			setCornerSetState();
		}
		repaint();
	}
	
	private boolean openSavedBoard(File f) throws IOException, ClassNotFoundException {
		FileInputStream fi;
		try {
			fi = new FileInputStream(f);
			ObjectInputStream oi = new ObjectInputStream(fi);
			m_board_save = (BoardSave)oi.readObject();
			oi.close();
			m_board_settings.setBoardDimensions(m_board_save.m_board_dimensions);
			m_board_settings.setHoldSizeMin(m_board_save.m_hold_minimum_size);
			m_board_settings.setHoldSizeMax(m_board_save.m_hold_maximum_size);
			m_board_settings.setHoldTypeRatio(m_board_save.m_hold_type_ratio);
			m_board_settings.setHoldDirectionRatio(m_board_save.m_hold_direction_ratio);
			MainWindow.m_frame.setSize(
					(int)m_board_save.m_board.getBoardWidth(), 
					(int)m_board_save.m_board.getBoardHeight());
			if (m_board_save.m_board.areAllCornersSet()) {
				setWaitingState();
			} else {
				setCornerSetState();
			}
			m_current_loaded_board_file = f;
			m_board_statistics.updateLabels(m_board_save.m_board);
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
		m_board_statistics.updateLabels(m_board_save.m_board);
	}
	
	private void deselectHold() {
		m_selected_hold = null;
		m_state = AppState.WAITING;
		m_hold_selection_settings.disableAll();
	}
	
	private void saveSelectedHold() {
		if (m_selected_hold == null) {
			// nothing to do
			return;
		}

		if (m_board_save.m_board.isOutsideBorders(m_hold_selection_settings.getHoldPosition())) {
			selectHold(m_selected_hold);
			MainWindow.setInstructionText("Error! Cannot place hold outside of border");
			return;
		}

		if (m_hold_selection_settings.isCrimp()) {
			m_selected_hold.addType(Hold.Type.CRIMP);
		}
		if (m_hold_selection_settings.isJug()) {
			m_selected_hold.addType(Hold.Type.JUG);
		}
		if (m_hold_selection_settings.isSloper()) {
			m_selected_hold.addType(Hold.Type.SLOPER);
		}
		if (m_hold_selection_settings.isPocket()) {
			m_selected_hold.addType(Hold.Type.POCKET);
		}
		if (m_hold_selection_settings.isPinch()) {
			m_selected_hold.addType(Hold.Type.PINCH);
		}
		if (m_hold_selection_settings.isFoot()) {
			m_selected_hold.addType(Hold.Type.FOOT);
		}
		m_selected_hold.setDirection(m_hold_selection_settings.getDirection());
		m_selected_hold.setSize(m_hold_selection_settings.getHoldSize());
		m_selected_hold.setPosition(m_hold_selection_settings.getHoldPosition());
		deselectHold();
		m_board_statistics.updateLabels(m_board_save.m_board);
	}
	
	private void selectHold(Hold h) {
		m_selected_hold = h;
		m_hold_selection_settings.setCrimp(m_selected_hold.isCrimp());
		m_hold_selection_settings.setJug(m_selected_hold.isJug());
		m_hold_selection_settings.setSloper(m_selected_hold.isSloper());
		m_hold_selection_settings.setPocket(m_selected_hold.isPocket());
		m_hold_selection_settings.setPinch(m_selected_hold.isPinch());
		m_hold_selection_settings.setFoot(m_selected_hold.isFoot());
		m_hold_selection_settings.setDirection(m_selected_hold.direction());
		m_hold_selection_settings.setHoldSize(
				m_selected_hold.size(), 
				m_selected_hold);
		m_hold_selection_settings.setPosition(m_selected_hold.position());
		m_state = AppState.HOLD_SELECTED;
		m_hold_selection_settings.enableAll();
	}
		
	private void tryClick(int x, int y) {
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
						MainWindow.setInstructionText("Selection is outside of board limits");
					}
				}
			}
			case CORNER_SET -> {
				m_board_save.m_board.addCorner(new Vector2(x, y));
				if (m_board_save.m_board.areAllCornersSet()) {
					setWaitingState();
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
		m_state = AppState.LOWEST_HAND_HOLD_SET;
		MainWindow.setInstructionText("Set the lowest position on the board that is acceptable for a hand hold");
	}

	private void setCornerSetState() {
		m_state = AppState.CORNER_SET;
		MainWindow.setInstructionText("The corners of the board need to be located, click on the corners in clockwise order.");
	}
	
	private void setWaitingState() {
		m_state = AppState.WAITING;
		MainWindow.setInstructionText("Click anywhere to add a hold, or click on an existing hold to edit.");
	}
	
	private class CanvasMouseListener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        	//System.out.println("Clicked x:" + e.getX() + " y: " + e.getY());

            tryClick(e.getX(),e.getY());
        }
        @Override
        public void mousePressed(MouseEvent e) {
        	int x = e.getX();
        	int y = e.getY();
        	
        	if (m_state == AppState.HOLD_SELECTED) {
        		if (m_hold_selection_settings.getNewHold().contains(x, y)) {
        			m_dragging_direction = true;
        		} else {
					m_mouse_x = e.getX();
					m_mouse_y = e.getY();
					m_dragging_location = true;
					m_dragging_original_pos = m_hold_selection_settings.getHoldPosition();
        		}
        	}
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        	if (m_state == AppState.HOLD_SELECTED) {
        		m_dragging_direction = false;
        		m_dragging_width = false;
				m_dragging_location = false;
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
        	if (m_dragging_direction) {
    			m_mouse_x = e.getX();
    			m_mouse_y = e.getY();
    			
    			Vector2 circle_centre = m_selected_hold.getCentrePoint();
				
				int mouse_vector_x = (int) (m_mouse_x - circle_centre.x);
				int mouse_vector_y = (int) (m_mouse_y - circle_centre.y);
				
				double vector_size = Math.hypot(mouse_vector_x, mouse_vector_y);
				
				double mouse_unit_vector_x = mouse_vector_x / vector_size;
				double mouse_unit_vector_y = mouse_vector_y / vector_size;
				
				m_hold_selection_settings.setDirection(Math.atan2(mouse_unit_vector_y, mouse_unit_vector_x));
    			
				Vector2 old_size = m_hold_selection_settings.getHoldSize();
				double old_ratio = old_size.x / old_size.y;
				
				m_hold_selection_settings.setHoldSize(
						new Vector2(old_ratio * vector_size, vector_size), 
						m_selected_hold);
				repaint();
    		} else if (m_dragging_width) {
//    			Vector2 circle_centre = m_selected_hold.getCentrePoint();
//    			m_selected_hold
//    			m_selected_hold.direction();
				//// TODO make ellipse creation work, a lot of underlying functions in analysis will require changing too
    			repaint();
    		} else if (m_dragging_location) {
				int x = e.getX();
				int y = e.getY();
				Vector2 mouse_movement = new Vector2(x - m_mouse_x, y - m_mouse_y);
				//System.out.println("(" + mouse_movement.x + ", " + mouse_movement.y + ")");
				Vector2 new_pos = new Vector2(m_dragging_original_pos.x + mouse_movement.x, m_dragging_original_pos.y + mouse_movement.y);
				m_hold_selection_settings.setPosition(new_pos);
				repaint();
			}
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        	//System.out.println("Clicked x:" + e.getX() + " y: " + e.getY());
			if (m_state == AppState.LOWEST_HAND_HOLD_SET) {
				m_mouse_x = e.getX();
				m_mouse_y = e.getY();
				repaint();
			}
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
		if (m_state == AppState.HOLD_SELECTED) {			
			if (e.getKeyCode() == KeyEvent.VK_DELETE &&  e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				deleteSelectedHold();
				repaint();
			}
		}
		/// Ctrl + S
		if (e.getKeyCode() == KeyEvent.VK_S && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
			if (m_state == AppState.HOLD_SELECTED) {
				saveSelectedHold();
				repaint();
			} else {
				saveBoard(m_current_loaded_board_file);
			}
		}
	}
}
