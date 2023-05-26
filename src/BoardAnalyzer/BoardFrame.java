package BoardAnalyzer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.*;
import org.apache.commons.io.*;


public class BoardFrame extends JPanel implements ActionListener, ChangeListener {
	private enum AppState {
		LOAD_IMAGE,
		CORNER_SET,
		WAITING,
		HOLD_SELECTED,
		THINKING
	}
	private String DEFAULT_BOARD_IMAGE_SAVE_NAME = new String("board.jpg");
	private String DEFAULT_BOARD_SAVE_NAME = new String("saved.board");
	private static final long serialVersionUID = 1L;
	private static final Font DEFAULT_FONT = new Font("Times New Roman",
            Font.BOLD,
            50);
	private AppState m_state;
	private JFileChooser m_file_chooser;
	
	private Image m_image;
	
	private Board m_board;
	private int m_mouse_x;
	private int m_mouse_y;
	private boolean m_dragging;
	
	private Hold m_selected_hold;
	private HoldSelectionSettings m_hold_selection_settings;
	private BoardSettings m_board_settings;
	private HeatmapSettings m_heatmap_settings;
	
	private JLabel m_instruction_label;
	
	public BoardFrame(
			JFrame fram, 
			HoldSelectionSettings hss, 
			JLabel il, 
			BoardSettings bs,
			HeatmapSettings hs) {
		m_hold_selection_settings = hss;
		m_instruction_label = il;
		m_board_settings = bs;
		m_heatmap_settings = hs;
		m_state = AppState.LOAD_IMAGE;
		m_dragging = false;

		m_file_chooser = new JFileChooser();
		m_file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		FileInputStream fi;
		try {
			fi = new FileInputStream(new File(DEFAULT_BOARD_SAVE_NAME));
			ObjectInputStream oi = new ObjectInputStream(fi);
			m_board = (Board)oi.readObject();
			m_board_settings.setBoardDimensions(m_board.getBoardWidth(), m_board.getBoardHeight());
		} catch (FileNotFoundException e) {
			// Ignore no file found, we don't care
			m_board = new Board();
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Saved board object corrupted");
			e.printStackTrace();
		}
		
		openFile(DEFAULT_BOARD_IMAGE_SAVE_NAME);
		CanvasMouseListener mouselisten = new CanvasMouseListener();
        this.addMouseListener(mouselisten);
        this.addMouseMotionListener(mouselisten);
        this.requestFocus();
	}
	
	public Color getColorFromHold(Hold h) {
		int red = 126;
		int blue = 126;
		int green = 126;
		int alpha = 170;
		if (m_state == AppState.HOLD_SELECTED) {
			if (h != m_selected_hold) {
				alpha = 126;
			} else {
				alpha = 255;
			}
		} else if (m_state == AppState.WAITING) {
			if (h.contains(m_mouse_x, m_mouse_y)) { 
				alpha = 255;
			}
		}
		if (h.m_is_crimp) {
			red = 0;
		}
		if (h.m_is_foot) {
			red = 255;
		}
		if (h.m_is_jug) {
			blue = 0;
		}
		if (h.m_is_pinch) {
			blue = 255;
		}
		if (h.m_is_pocket) {
			green = 0;
		}
		if (h.m_is_sloper) {
			green = 255;
		}
		
		return new Color(red, blue, green, alpha);
	}
	
	static public Vector2 getPointOnCircleFromRad(double rad, Vector2 circle_centre, double circle_size) {
		double unit_vector_x = Math.cos(rad);
		double unit_vector_y = Math.sin(rad);
		return new Vector2(circle_centre.x + (unit_vector_x * circle_size / 2.0), 
				circle_centre.y + (unit_vector_y * circle_size / 2.0));
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
		if (m_state != AppState.LOAD_IMAGE) {
			g2.drawImage(m_image, 0, 0, (int)this.getSize().getWidth(), (int)this.getSize().getHeight(),this);			
		
			// Draw holds
			ArrayList<Hold> holds = m_board.getHolds();
			if (!holds.isEmpty()) {		
				for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
					Hold h = it.next();
					
					Color c = getColorFromHold(h);
					g2.setColor(c);
					double circle_size = h.m_size;
					double direction = h.m_direction_rad;
					int circle_pos_x = (int)h.m_pos.x;
					int circle_pos_y = (int)h.m_pos.y;
					int lineWidth = 5;

					if (m_state == AppState.HOLD_SELECTED && h == m_selected_hold) {
						circle_size = m_hold_selection_settings.getHoldSize();
						direction = m_hold_selection_settings.getDirection();
						circle_pos_x = (int)m_hold_selection_settings.getHoldPosition().getX();
						circle_pos_y = (int)m_hold_selection_settings.getHoldPosition().getY();
						// Make line width thicker for circle
						lineWidth = 7;
					}
					
					Vector2 circle_centre = h.getCentrePoint();
					Vector2 point_on_circle_towards_mouse = 
							getPointOnCircleFromRad(
									direction,
									circle_centre,
									circle_size
									);
					
					g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
	
					g2.drawLine(
							(int)point_on_circle_towards_mouse.x, 
							(int)point_on_circle_towards_mouse.y, 
							(int)circle_centre.x, 
							(int)circle_centre.y
							);					
					Shape circle = new Ellipse2D.Double(circle_pos_x, circle_pos_y, circle_size, circle_size);											
					g2.draw(circle);
				}
			}
			
			// Draw board corners and lines between them
			ArrayList<Vector2> corners = m_board.getCorners();
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
		if (e.getActionCommand() == "OpenFile") {
			System.out.println("Open file");
			openFileOpenerDialogAndOpenFile();
		} else if (e.getActionCommand() == "Save") {
			saveSettings();
			FileOutputStream f;
			try {
				f = new FileOutputStream(new File(DEFAULT_BOARD_SAVE_NAME));
				ObjectOutputStream o = new ObjectOutputStream(f);
				
				// Write objects to file
				o.writeObject(m_board);
				setSavedText();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			repaint();
		} else if (e.getActionCommand() == "SaveHold") {
			saveSelectedHold();
			repaint();
		} else if (e.getActionCommand() == "DeleteHold") {
			deleteSelectedHold();
			repaint();
		} else if (e.getActionCommand() == "Generate") {
			Analyzer a = new Analyzer(m_board);
			File output_file = new File("heatmap.png");
			BufferedImage image = a.getHeatmap(m_heatmap_settings.getBrightness());
			try {
				ImageIO.write(image, "png", output_file);
			} catch (IOException e1) {
				System.out.println("Failed to write output file");
			}
		} else if (e.getActionCommand() == "SetCorners") {
			m_board.clearCorners();
			setCornerSetState();
			repaint();
		}
	}
	
	private void saveSettings() {
		try {
			m_board.setBoardDimensions(m_board_settings.getBoardWidth(), m_board_settings.getBoardHeight());
		} catch (java.lang.NumberFormatException e) {
			System.out.println("Board settings not saved, error in width or height");
		}
	}
	
	private void openFileOpenerDialogAndOpenFile() {
		int returnVal = m_file_chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File chosen_file = m_file_chooser.getSelectedFile();
            //This is where a real application would open the file.
            System.out.println("Opening: " + chosen_file.getName() + ".");
            
            String file_name = "board." + FilenameUtils.getExtension(chosen_file.getName());
            File new_loc = new File(file_name);
            try {
				FileUtils.copyFile(chosen_file, new_loc);
				openFile(file_name);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
	}
	
	private void openFile(String path) {
		File file = new File(path);
        try {
			m_image = ImageIO.read(file);
			int new_width = m_image.getWidth(this) + MainWindow.PREFERRED_GENERATE_TAB_WIDTH;
			int new_height = m_image.getHeight(this) + MainWindow.PREFERRED_BOTTOM_BAR_HEIGHT;
			
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
			if (m_board.areAllCornersSet()) {
				setWaitingState();
			} else {
				setCornerSetState();
			}
			repaint();
		} catch (IOException e1) {
			System.out.println("Image not available.");
		}
	}
	
	private void deleteSelectedHold() {
		m_board.removeHold(m_selected_hold);
		deselectHold();
	}
	
	private void deselectHold() {
		m_selected_hold = null;
		m_state = AppState.WAITING;
		m_hold_selection_settings.disableAll();
	}
	
	private void saveSelectedHold() {
		m_selected_hold.m_is_crimp = m_hold_selection_settings.isCrimp();
		m_selected_hold.m_is_jug = m_hold_selection_settings.isJug();
		m_selected_hold.m_is_sloper = m_hold_selection_settings.isSloper();
		m_selected_hold.m_is_pocket = m_hold_selection_settings.isPocket();
		m_selected_hold.m_is_pinch = m_hold_selection_settings.isPinch();
		m_selected_hold.m_is_foot = m_hold_selection_settings.isFoot();
		m_selected_hold.m_direction_rad = m_hold_selection_settings.getDirection();
		m_selected_hold.m_size = m_hold_selection_settings.getHoldSize();
		m_selected_hold.m_pos.x = m_hold_selection_settings.getHoldPosition().getX();
		m_selected_hold.m_pos.y = m_hold_selection_settings.getHoldPosition().getY();
		deselectHold();
	}
	
	private void selectHold(Hold h) {
		m_selected_hold = h;
		m_hold_selection_settings.setCrimp(m_selected_hold.m_is_crimp);
		m_hold_selection_settings.setJug(m_selected_hold.m_is_jug);
		m_hold_selection_settings.setSloper(m_selected_hold.m_is_sloper);
		m_hold_selection_settings.setPocket(m_selected_hold.m_is_pocket);
		m_hold_selection_settings.setPinch(m_selected_hold.m_is_pinch);
		m_hold_selection_settings.setFoot(m_selected_hold.m_is_foot);
		m_hold_selection_settings.setDirection(m_selected_hold.m_direction_rad);
		m_hold_selection_settings.setHoldSize(
				m_selected_hold.m_size, 
				(int)m_selected_hold.m_pos.x, 
				(int)m_selected_hold.m_pos.y,
				m_selected_hold.m_size);
		m_state = AppState.HOLD_SELECTED;
		m_hold_selection_settings.enableAll();
	}
		
	private void tryClick(int x, int y) {
       	System.out.println("Clicked x:" + x + " y: " + y);
       	if (m_state == AppState.WAITING) {
        	if (m_board.existsHold(x, y)) {
        		try {
					selectHold(m_board.getHold(x, y));
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					System.out.println("Can't find hold! Logic mismatch in existshold and gethold");
					e.printStackTrace();
				}
        	} else {        			
	        	// No hold where we clicked
        		selectHold(m_board.createHold(x, y));
        	}
        } else if (m_state == AppState.CORNER_SET) {
        	m_board.addCorner(new Vector2(x, y));
        	if (m_board.areAllCornersSet()) {
        		setWaitingState();
        	}
        }
        // If something changes, we should do this: 
        repaint();
    }
	
	private void setCornerSetState() {
		m_state = AppState.CORNER_SET;
		m_instruction_label.setText("<html>The corners of the board need to be located, click on the corners in clockwise order.</html>");
	}
	
	private void setWaitingState() {
		m_state = AppState.WAITING;
		m_instruction_label.setText("<html>Click anywhere to add a hold, or click on an existing hold to edit.</html>");
	}
	
	private void setSavedText() {
		m_instruction_label.setText("<html>Successfully saved board layout.</html>");
	}
	
	private class CanvasMouseListener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        	System.out.println("Clicked x:" + e.getX() + " y: " + e.getY());

            tryClick(e.getX(),e.getY());
        }
        @Override
        public void mousePressed(MouseEvent e) {
        	int x = e.getX();
        	int y = e.getY();
        	
        	if (m_state == AppState.HOLD_SELECTED && m_selected_hold.contains(x, y)) {
        		m_dragging = true;
        	}
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        	if (m_state == AppState.HOLD_SELECTED) {
        		m_dragging = false;
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
        	if (m_state == AppState.HOLD_SELECTED && m_dragging) {
    			m_mouse_x = e.getX();
    			m_mouse_y = e.getY();
    			
    			int circle_centre_x = (int)(m_selected_hold.m_pos.x + m_selected_hold.m_size/2.0);
				int circle_centre_y = (int)(m_selected_hold.m_pos.y + m_selected_hold.m_size/2.0);
				
				int mouse_vector_x = (m_mouse_x - circle_centre_x);
				int mouse_vector_y = (m_mouse_y - circle_centre_y);
				
				double vector_size = Math.hypot(mouse_vector_x, mouse_vector_y);
				
				double mouse_unit_vector_x = mouse_vector_x / vector_size;
				double mouse_unit_vector_y = mouse_vector_y / vector_size;
				
				m_hold_selection_settings.setDirection(Math.atan2(mouse_unit_vector_y, mouse_unit_vector_x));
    			
				m_hold_selection_settings.setHoldSize(
						vector_size, 
						(int)m_selected_hold.m_pos.x, 
						(int)m_selected_hold.m_pos.y, 
						m_selected_hold.m_size);
				repaint();
    		}
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        	//System.out.println("Clicked x:" + e.getX() + " y: " + e.getY());
        }
    }
}
