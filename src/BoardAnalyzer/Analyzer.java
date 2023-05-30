package BoardAnalyzer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Analyzer {
	private Board m_board;
	public Analyzer(Board board) {
		m_board = board;
	}
	
	private double heatmapFunction(double distance, int brightness_factor) {
		// Equation y = 
		double distance_cap = (brightness_factor * Math.PI) / 2;
		distance = Math.min(distance, distance_cap);
		return  Math.max(Math.cos(distance / brightness_factor), 0.0);
	}
	
	private Color getPixelProximityColour(
			int i, int j, 
			int brightness_factor, 
			HashSet<Hold.Types> hold_types_to_show,
			boolean hold_types_exact_match) {
		int blueness = 244;
		int redness = 44;
		int greenness = 44;
		int blue_decrease_amount = 200;
		int red_increase_amount = 200;
		ArrayList<Hold> holds = m_board.getHolds();
		double smallest_proximity = Double.POSITIVE_INFINITY;
		for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			//h.print();
			if (
				(!hold_types_exact_match && h.isOneOf(hold_types_to_show))
			||  (hold_types_exact_match && h.isTypes(hold_types_to_show))) {
				//System.out.println(hold_types_to_show);
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
	
	public BufferedImage getHeatmap(
			int brightness_factor, 
			HashSet<Hold.Types> hold_types_to_show, 
			boolean hold_types_exact_match,
			boolean hold_direction_matters) {
		Vector2 image_size = m_board.getBoardSize();
		BufferedImage image = new BufferedImage(
				(int)image_size.x, 
				(int)image_size.y, 
				BufferedImage.TYPE_INT_RGB
			);
		for (int i = 0; i < image_size.x; i++) {
			for (int j = 0; j < image_size.y; j++) {
				// Calculate pixel
				Color c = getPixelProximityColour(i, j, 
						brightness_factor, 
						hold_types_to_show, 
						hold_types_exact_match);
				image.setRGB(i, j, c.getRGB());
			}
		}
		
		return image;
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
		
	private Vector2 getNewFlatPosition(
			Vector2 point, 
			PerspectiveTransform pt
			) {
		 Point2D.Double[] out_arr = {new Point2D.Double(0,0)};
		 Point2D.Double[] in_arr = {point.toPoint2D()};
		 pt.transform(in_arr, 0, out_arr, 0, 1);
		 // This could be done all at once, but we still have to loop through the output
		 // and create each hold, so not actually much simpler
		 return new Vector2(out_arr[0]);
	}
	
	public void flattenBoard() {
		Board flat_board = new Board();
		Vector2 flat_board_size = getFlatBoardSize(m_board);
		flat_board.setBoardDimensions(flat_board_size.x, flat_board_size.y);
		
		Vector2 A = new Vector2(0,0);
		Vector2 B = new Vector2(flat_board_size.x, 0);
		Vector2 C = new Vector2(flat_board_size.x, flat_board_size.y);
		Vector2 D = new Vector2(0, flat_board_size.y);
		
		flat_board.addCorner(A);
		flat_board.addCorner(B);
		flat_board.addCorner(C);
		flat_board.addCorner(D);
		
		ArrayList<Vector2> old_corners = m_board.getCorners();
		Vector2 A_old = old_corners.get(0);
		Vector2 B_old = old_corners.get(1);
		Vector2 C_old = old_corners.get(2);
		Vector2 D_old = old_corners.get(3);
		
		PerspectiveTransform pt = PerspectiveTransform.getQuadToQuad(
				 A_old.x, A_old.y,
				 B_old.x, B_old.y,
				 C_old.x, C_old.y,
				 D_old.x, D_old.y,
				 A.x, A.y,
				 B.x, B.y,
				 C.x, C.y,
				 D.x, D.y);
		
		ArrayList<Hold> old_holds = m_board.getHolds();
		for (Iterator<Hold> it = old_holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			Vector2 new_pos = getNewFlatPosition(
					new Vector2(h.position().x, h.position().y), pt);
			Vector2 old_size_pos = BoardFrame.getPointOnCircleFromRad(h.direction(), h.getCentrePoint(), h.size());
			Vector2 new_size_pos = getNewFlatPosition(old_size_pos, pt);
			
			Vector2 new_direction_size_vector = new Vector2(
					new_size_pos.x - new_pos.x, new_size_pos.y - new_pos.y
					);
			
			Hold flat_hold = 
					new Hold((int)new_pos.x, 
							(int)new_pos.y, 
							new_direction_size_vector.length(), 
							new_direction_size_vector.angle());
			flat_hold.addTypes(h.getTypes());
			flat_board.addHold(flat_hold);
		}
		
		m_board = flat_board;
	}
}
