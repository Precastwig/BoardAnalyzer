package BoardAnalyzer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;

public class Analyzer {
	private Board m_flat_board;
	public Analyzer(Board board) {
		flattenBoard(board);
	}
	
	private double heatmapFunction(double distance, int brightness_factor) {
		// Equation y = 
		double distance_cap = (brightness_factor * Math.PI) / 2;
		distance = Math.min(distance, distance_cap);
		return  Math.max(Math.cos(distance / brightness_factor), 0.0);
	}
	
	private Color getPixelProximityColour(int i, int j, int brightness_factor, HashSet<Hold.Types> hold_types_to_show) {
		int blueness = 244;
		int redness = 44;
		int greenness = 44;
		int blue_decrease_amount = 200;
		int red_increase_amount = 200;
		ArrayList<Hold> holds = m_flat_board.getHolds();
		double smallest_proximity = Double.POSITIVE_INFINITY;
		for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			if (h.isOneOf(hold_types_to_show)) {
				// Pixel to hold vector
				Vector2 pixel_to_hold_v = new Vector2(i - h.position().x, j - h.position().y);
				double pixel_to_hold = pixel_to_hold_v.length() - h.size();
//			System.out.println(ph.length());
//			System.out.println(h.m_size);
				if (pixel_to_hold < 0.0) {
					return new Color(
							redness + red_increase_amount, 
							greenness,
							blueness - blue_decrease_amount);
				}
				if (pixel_to_hold < smallest_proximity) {
					smallest_proximity = pixel_to_hold;
				}
				
			}
			
			//System.out.println("Should be reddish");
			
		}
		double relative_brightness = heatmapFunction(smallest_proximity, brightness_factor);
		return new Color(
				Math.min((int)(redness + (relative_brightness * red_increase_amount)), 244), 
				greenness, 
				Math.max((int)(blueness - (relative_brightness * blue_decrease_amount)), 44));
	}
	
	public BufferedImage getHeatmap(int brightness_factor, HashSet<Hold.Types> hold_types_to_show) {
		Vector2 image_size = m_flat_board.getBoardSize();
		BufferedImage image = new BufferedImage(
				(int)image_size.x, 
				(int)image_size.y, 
				BufferedImage.TYPE_INT_RGB
			);
//		Graphics2D g2 = image.createGraphics();
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//	    		RenderingHints.VALUE_ANTIALIAS_ON);
//	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
//	    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//		// Draw all the points in the flat board
//		ArrayList<Hold> holds = m_flat_board.getHolds();
//		if (!holds.isEmpty()) {		
//			for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
//				g2.draw();
//			}
//		}
		for (int i = 0; i < image_size.x; i++) {
			for (int j = 0; j < image_size.y; j++) {
				// Calculate pixel
				Color c = getPixelProximityColour(i, j, brightness_factor, hold_types_to_show);
				image.setRGB(i, j, c.getRGB());
			}
		}
		
		return image;
		// Output to file
//		try {
//		    ImageIO.write(image, "png", output_file);
//		} catch (IOException e) {
//		    System.out.println("Failed to write output file");
//		}
	}
	
	private Vector2 getFlatBoardSize(Board old_board) {
		double ratio = old_board.getBoardWidth() / old_board.getBoardHeight();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screen_width = screenSize.getWidth();
		double screen_height = screenSize.getHeight();
		
		int new_width = (int)screen_width;
		int new_height = (int)(new_width / ratio);
		if (new_height > screen_height) {
			new_height = (int)screen_height;
			new_width = (int)(new_height * ratio);
		}
		
		return new Vector2(new_width, new_height);
	}
	
//	private Vector2 getClosestPointOnLine(
//			Vector2 point,
//			Vector2 line_point_A,
//			Vector2 line_point_B) {
//		Vector2 line_direction = new Vector2(line_point_A.x - line_point_B.x, line_point_A.y - line_point_B.y);
//		line_direction.normalize();//this needs to be a unit vector
//		Vector2 v = new Vector2(point.x - line_point_B.x, point.y - line_point_B.y);
//	    double d = Vector2.dotProduct(v, line_direction);
//	    return new Vector2(line_point_B.x + (line_direction.x * d), line_point_B.y + (line_direction.y * d));
//	}
		
	private Vector2 getNewFlatPosition(
			Vector2 point, 
			Vector2 A_old, 
			Vector2 B_old, 
			Vector2 C_old, 
			Vector2 D_old,
			Vector2 A,
			Vector2 B,
			Vector2 C,
			Vector2 D
			) {
		// Debug
				File output_file = new File("heatmap_debug.png");
				Vector2 image_size = m_flat_board.getBoardSize();
				BufferedImage debug_image = new BufferedImage(
						(int)image_size.x, 
						(int)image_size.y, 
						BufferedImage.TYPE_INT_RGB
					);
				Graphics2D g2 = debug_image.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			    		RenderingHints.VALUE_ANTIALIAS_ON);
			    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
			    		RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			    g2.setPaint(Color.white);
			    g2.fillRect(0,0, debug_image.getWidth(), debug_image.getHeight());
				g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			    g2.setColor(Color.black);
			    // Debug over
			    
		Vector2 CA = new Vector2(A.x - C.x, A.y - C.y);
		Vector2 CA_norm = Vector2.normalize(CA);
		Vector2 DB = new Vector2(B.x - D.x, B.y - D.y);
		Vector2 DB_norm = Vector2.normalize(DB);
		Vector2 CA_old = new Vector2(A_old.x - C_old.x, A_old.y - C_old.y);
		Vector2 DB_old = new Vector2(B_old.x - D_old.x, B_old.y - D_old.y);
		
		
		Vector2 v1 = new Vector2(point.x - C_old.x, point.y - C_old.y);
	    double perc_along_CA = Vector2.dotProduct(v1, Vector2.normalize(CA_old)) / CA.length();
	    
	    Vector2 v2 = new Vector2(point.x - D_old.x, point.y - D_old.y);
	    double perc_along_DB = Vector2.dotProduct(v2, Vector2.normalize(DB_old)) / DB.length();
		
	    // Point along CA using the old percentage
	    Vector2 CAK = new Vector2(
	    		C.x + (CA.x * perc_along_CA),
	    		C.y+ (CA.y * perc_along_CA));
	    Vector2 DBK = new Vector2(
	    		D.x + (DB.x * perc_along_DB),
	    		D.y+ (DB.y * perc_along_DB));
	    
	    boolean is_left_CA = point.isLeft(C_old, A_old);
	    boolean is_left_DB = point.isLeft(D_old, B_old);
	    /////// FIND PERPENDICULAR OF CA AND DB THEN FIND INTERSECTION OF THOSE TWO LINES FROM CAK AND DBK
	    Vector2 perp_CA_norm;
	    Vector2 perp_DB_norm;
	    if (is_left_CA) {
	    	perp_CA_norm = Vector2.perpendicularAntiClockwise(CA_norm);
	    } else {
	    	perp_CA_norm = Vector2.perpendicularClockwise(CA_norm);
	    }
	    
	    if (is_left_DB) {
	    	perp_DB_norm = Vector2.perpendicularAntiClockwise(DB_norm);
	    } else {
	    	perp_DB_norm = Vector2.perpendicularClockwise(DB_norm);
	    }
	    
//	    CAK.print();
//	    perp_CA_norm.print();
//	    
//	    DBK.print();
//	    perp_DB_norm.print();
	    g2.drawLine((int)CAK.x, (int)CAK.y, (int)(CAK.x + perp_CA_norm.x), (int)(CAK.y + perp_CA_norm.y));
	    g2.drawLine((int)DBK.x, (int)DBK.y, (int)(DBK.x + perp_DB_norm.x), (int)(DBK.y + perp_DB_norm.y));
		try {
			ImageIO.write(debug_image, "png", output_file);
		} catch (IOException e1) {
			System.out.println("Failed to write output file");
		}
	    
		return Vector2.intersect(CAK, perp_CA_norm, DBK, perp_DB_norm);
	}
	
	private void flattenBoard(Board board) {
		
		Vector2 flat_board_size = getFlatBoardSize(board);
		m_flat_board = new Board();
		m_flat_board.setBoardDimensions(flat_board_size.x, flat_board_size.y);
		Vector2 A = new Vector2(0,0);
		Vector2 B = new Vector2(flat_board_size.x, 0);
		Vector2 C = new Vector2(flat_board_size.x, flat_board_size.y);
		Vector2 D = new Vector2(0, flat_board_size.y);
	
//		Vector2 CA = new Vector2(A.x - C.x, A.y - C.y);
//		Vector2 DB = new Vector2(B.x - D.x, B.y - D.y);
		
		m_flat_board.addCorner(A);
		m_flat_board.addCorner(B);
		m_flat_board.addCorner(C);
		m_flat_board.addCorner(D);
		
		ArrayList<Vector2> old_corners = board.getCorners();
		Vector2 A_old = old_corners.get(0);
		Vector2 B_old = old_corners.get(1);
		Vector2 C_old = old_corners.get(2);
		Vector2 D_old = old_corners.get(3);
		// Create two criss cross vectors between opposite corners
//		Vector2 CA_old = new Vector2(A_old.x - C_old.x, A_old.y - C_old.y);
//		Vector2 DB_old = new Vector2(B_old.x - D_old.x, B_old.y - D_old.y);
		
		ArrayList<Hold> old_holds = board.getHolds();
		for (Iterator<Hold> it = old_holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			//h.print();
			Vector2 new_pos = getNewFlatPosition(
					new Vector2(h.position().x, h.position().y),
					A_old, B_old, C_old, D_old,
					A, B, C, D);
			Vector2 old_size_pos = BoardFrame.getPointOnCircleFromRad(h.direction(), h.getCentrePoint(), h.size());
			Vector2 new_size_pos = getNewFlatPosition(
					old_size_pos,
					A_old, B_old, C_old, D_old,
					A, B, C, D);
			
			Vector2 new_direction_size_vector = new Vector2(
					new_size_pos.x - new_pos.x, new_size_pos.y - new_pos.y
					);
			
			Hold flat_hold = 
					new Hold((int)new_pos.x, 
							(int)new_pos.y, 
							new_direction_size_vector.length(), 
							new_direction_size_vector.angle());
			flat_hold.print();
			m_flat_board.addHold(flat_hold);
		}
	}
}
