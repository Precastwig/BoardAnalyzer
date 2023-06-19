package BoardAnalyzer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

import javax.swing.JProgressBar;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

public class Analyzer {
	class AngleProportions {
		public enum BucketLabel {
			UP("Up"),
			RIGHT_SKEW("Slanted right"),
			LEFT_SKEW("Slanted left"),
			RIGHT_SIDEPULL("Right sidepull"),
			LEFT_SIDEPULL("Left sidepull"),
			UNDERCUT("Undercut");
			private String name;
			private BucketLabel(String s) {
				this.name = s;
			}
			
			@Override
			public String toString() {
				return name;
			}
		}
		private int[] m_angle_buckets;
		private int[] m_ideal_proportion;
		private static double MARGIN = Math.PI / 32.0;
		private static double UP_ANGLE = -Math.PI/2 - MARGIN;
		private static double LEFT_ANGLE = Math.PI;
		private static double RIGHT_ANGLE = 0;
		private static double HALF_CIRCLE = Math.PI;

		AngleProportions(ArrayList<Hold> holds, int[] ideal_proportion) {
			m_angle_buckets = new int[] {
					0,
					0,
					0,
					0,
					0,
					0
			};
			
			for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
				Hold h = it.next();
				double dir = h.direction();
				m_angle_buckets[classifyAngle(dir).ordinal()] += 1; 
			}
			
			m_ideal_proportion = ideal_proportion;
		}
		
		public static BucketLabel classifyAngle(double angle) {
			if (UP_ANGLE - MARGIN < angle && angle < UP_ANGLE + MARGIN) {
				return BucketLabel.UP;
			} else if ((-LEFT_ANGLE < angle && angle < -LEFT_ANGLE + MARGIN) || 
					(LEFT_ANGLE - MARGIN < angle && angle < LEFT_ANGLE)) {
				return BucketLabel.LEFT_SIDEPULL;
			} else if (RIGHT_ANGLE - MARGIN < angle && angle < RIGHT_ANGLE + MARGIN) {
				return BucketLabel.RIGHT_SIDEPULL;
			} else if (angle > 0) {
				return BucketLabel.UNDERCUT;
			} else if (angle < -Math.PI/2) {
				return BucketLabel.LEFT_SKEW;
			} else if (angle > -Math.PI/2) {
				return BucketLabel.RIGHT_SKEW;
			}
			
			// Default up
			return BucketLabel.UP;
		}
		
		private BucketLabel getLeastFilledBucket() {
			BucketLabel least_filled_bucket = BucketLabel.UP;
			double smallest_proportion = Double.POSITIVE_INFINITY;
			for (BucketLabel direction_label : BucketLabel.values()) {
				double proportion = (double)m_angle_buckets[direction_label.ordinal()] / (double)m_ideal_proportion[direction_label.ordinal()]; 
				if (proportion < smallest_proportion) {
					least_filled_bucket = direction_label;
					smallest_proportion = proportion;
				}
			}
			System.out.println(least_filled_bucket.toString());
			return least_filled_bucket;
		}
		
		public double getNewAngle() {
			BucketLabel least_filled_bucket = getLeastFilledBucket();
			double random_amount = Math.random();
			switch(least_filled_bucket) {
			case UP:
				return (UP_ANGLE - MARGIN) + (random_amount * 2 * MARGIN);
			case LEFT_SIDEPULL:
				if (random_amount < 0.5) {
					return (-LEFT_ANGLE) + (random_amount * MARGIN);
				} else {
					random_amount = 1 - random_amount;
					return (LEFT_ANGLE) - (random_amount * MARGIN);
				}
			case RIGHT_SIDEPULL:
				return (RIGHT_ANGLE - MARGIN) + (random_amount * 2 * MARGIN);
			case UNDERCUT:
				return (RIGHT_ANGLE + MARGIN) + (random_amount * (LEFT_ANGLE - (2 * MARGIN)));
			case LEFT_SKEW:
				return (-LEFT_ANGLE + MARGIN) + (random_amount * ((HALF_CIRCLE / 2) - (2 * MARGIN)));
			case RIGHT_SKEW:
				return (UP_ANGLE + MARGIN) + (random_amount * ((HALF_CIRCLE / 2) - (2 * MARGIN)));
			}
			System.out.println("Error!");
			// Default up
			return 0.0;
		}
		
	}
	
	private Board m_board;
	private Vector2 m_flat_board_size;
	private int[] m_hold_type_pref_ratio;
	private int[] m_hold_direction_pref_ratio;
	
	public enum HoldGenerationReturnStatus {
		FAILURE,
		SUCCESS
	}
	
	public Analyzer(Board board, Vector2 flat_board_size, int[] hold_type_pref_ratio, int[] hold_direction_pref_ratio) {
		m_board = board;
		m_flat_board_size = flat_board_size;
		m_hold_type_pref_ratio = hold_type_pref_ratio;
		m_hold_direction_pref_ratio = hold_direction_pref_ratio;
	}
	
	private double heatmapFunction(double distance, int brightness_factor) {
		// Equation y = 
		double distance_cap = (brightness_factor * Math.PI) / 2;
		distance = Math.min(distance, distance_cap);
		return  Math.max(Math.cos(distance / brightness_factor), 0.0);
	}
	
	private PointD[] getPointArrayFromHolds(ArrayList<Hold> holds) {
		ArrayList<PointD> al = new ArrayList<PointD>();
		for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			al.add(new PointD(h.getCentrePoint().x, h.getCentrePoint().y));
		}
		return al.toArray(new PointD[al.size()]);
	}
	
	// Return the location in the same space as b
	private Vector2 getNewHoldLocation(Board b) {
		double MARGIN = 0.001;
		ArrayList<Hold> holds = b.getHolds();
		PointD[] hold_positions = getPointArrayFromHolds(holds);
		RectD clipping_rect = new RectD(0.0, 0.0, b.getBoardWidth(), b.getBoardHeight());
		VoronoiResults v = Voronoi.findAll(hold_positions, clipping_rect);
		PointD[] points = v.voronoiVertices;
		Vector2 furthest_point = new Vector2(holds.get(0).getCentrePoint());
		double furthest_distance = 0.0;
		for (int i = 0; i < points.length; i++) {
			if (0.0 + MARGIN < points[i].x && points[i].x < b.getBoardWidth() - MARGIN &&
				0.0 + MARGIN < points[i].y && points[i].y < b.getBoardHeight() - MARGIN) {
				Vector2 p = new Vector2(points[i]);
				double dist = b.getNearestHold(p).getCentrePoint().distanceTo(p);
				if (dist > furthest_distance) {
					furthest_distance = dist;
					furthest_point = p;
				}
			}
		}
		// No valid points
		VoronoiEdge[] edges = v.voronoiEdges;
		// Find the longest edge

		for (int i = 0; i < edges.length; i++) {
			Vector2 p1 = new Vector2(points[edges[i].vertex1]);
			Vector2 p2 = new Vector2(points[edges[i].vertex2]);
			
			Vector2 line = new Vector2(p1.x - p2.x, p1.y - p2.y);
			
		}
		return furthest_point;
	}
	
//	private AngleStats getAngleStats(ArrayList<Hold> holds) {
//		// https://www.ncss.com/wp-content/themes/ncss/pdf/Procedures/NCSS/Circular_Data_Analysis.pdf
//		double c = 0.0;
//		double s = 0.0;
//		int num_points = 0;
//		for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
//			Hold h = it.next();
//			c += Math.cos(h.direction());
//			s += Math.sin(h.direction());
//			num_points++;
//		}
//		double c_avg = c / num_points;
//		double s_avg = s / num_points;
//		double mean_angle = Math.atan(s_avg / c_avg); // T is the estimation for the mean angle
//		
//		if (c_avg > 0) {
//			if (s_avg < 0) {
//				mean_angle += 2 * Math.PI;
//			} 
//		} else {
//			mean_angle += Math.PI;
//		}
//		
//		double R = Math.sqrt(c * c + s * s);
//		double r_avg = R / num_points;
//		
//		double variance = 1 - (r_avg); // 0 no variance, 1 lots of variance
//		return new AngleStats(variance, mean_angle);
//	}
	
	private ArrayList<Hold> getHoldsInProximity(Board b, Vector2 position, double range) {
		ArrayList<Hold> holds_in_proximity = new ArrayList<Hold>();
		for (Iterator<Hold> it = b.getHolds().iterator(); it.hasNext();) {
			Hold h = it.next();
			if (h.getCentrePoint().distanceTo(position) < range) {
				holds_in_proximity.add(h);
			}
		}
		return holds_in_proximity;
	}
	
	public Hold.Types suggestHoldTypes(Hold h) {
//		Board flat_board = flattenBoard();
//		flat_board.removeHoldAt(h.position());
		return getLeastFilledHoldType(m_board, h.getCentrePoint(), Hold.Types.getHandTypes());
	}
	
	public double suggestHoldDirection(Hold h) {
//		Board flat_board = flattenBoard();
//		flat_board.removeHoldAt(h.position());
		return getNewHoldDirection(m_board, h.getCentrePoint(), h.size());
	}
	
	private double getProximityDistance(Board b) {
		return Math.min(b.getBoardHeight(), b.getBoardWidth()) / 2;

	}
	
	private double getNewHoldDirection(Board b, Vector2 position, Vector2 size) {
		double hold_vicinity_distance = getProximityDistance(b);
		ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
		if (holds_in_proximity.isEmpty()) {
			// Default direction for this hold type.... 
			// Whatever that is
			// Up? (for now)
			return -Math.PI / 2; 
		}
		AngleProportions proximate_stats = new AngleProportions(holds_in_proximity, m_hold_direction_pref_ratio);
		return proximate_stats.getNewAngle();
	}
	
	private double getNewHoldSize(Board b, Vector2 position) throws InvalidAlgorithmParameterException {
		Hold nearest_hold = b.getNearestHold(position);
		double MAX_HOLD_SIZE = 100;
		// TODO This does not account for ellipses!
		double min_size = 10;
		for (Iterator<Hold> it = b.getHolds().iterator(); it.hasNext();) {
			Hold h = it.next();
			Vector2 hsize = h.size();
			double hsizemax = Math.max(hsize.x, hsize.y);
			double hsizemin = Math.min(hsize.x, hsize.y);
			if (hsizemin < min_size) {
				min_size = hsizemin;
			}
			if (hsizemax > MAX_HOLD_SIZE) {
				MAX_HOLD_SIZE = hsizemax;
			}
		}
		double max_size = 
				Math.min(nearest_hold.getCentrePoint().distanceTo(position) - 
				Math.max(nearest_hold.size().x, nearest_hold.size().y), MAX_HOLD_SIZE);
		
	
		if (max_size < min_size) {
			// Problem
			throw new InvalidAlgorithmParameterException("getNewHoldSize failure");
		}
		
		// Generate a random size between the smallest and largest
		return min_size + (Math.random() * (max_size - min_size)); 
	}
	
	private Hold.Types getLeastFilledHoldType(Board b, Vector2 position, Hold.Types[] types) {
		Hold.Types least_filled_type = types[0];
		double hold_vicinity_distance = getProximityDistance(b);
		double smallest_proportion = Double.POSITIVE_INFINITY;
		ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
		for (Hold.Types type : types) {
			double proportion = (double)Board.countType(holds_in_proximity, type) / m_hold_type_pref_ratio[type.ordinal()];
			if (proportion < smallest_proportion) {
				least_filled_type = type;
				smallest_proportion = proportion;
			}
		}
		return least_filled_type;
	}
	
	private HoldGenerationReturnStatus generateLeastCommonTypeHoldImpl(Board b, Hold new_hold) {
		if (b.getHolds().size() <= 1) {
			// No holds/one hold, do nothing
			return HoldGenerationReturnStatus.FAILURE;
		}
		try {
			Vector2 new_loc = getNewHoldLocation(b);
			new_hold.setPosition(new_loc);
			Hold.Types type = getLeastFilledHoldType(b, new_loc, Hold.Types.getHandTypes());
			new_hold.addType(type);
			System.out.println("Generated hold at " + new_loc.x + "," + new_loc.y);
			double new_size;
			new_size = getNewHoldSize(b, new_loc);
			Vector2 new_size_vector = new Vector2(new_size, new_size);
			new_hold.setSize(new_size_vector);
			double new_dir = getNewHoldDirection(b, new_loc, new_size_vector);
			new_hold.setDirection(new_dir);
		} catch (InvalidAlgorithmParameterException e) {
			System.out.println(e);
			return HoldGenerationReturnStatus.FAILURE;
		}
		
		return HoldGenerationReturnStatus.SUCCESS;
	}
		
	public Optional<Hold> generateHold(HoldGenerationSettings settings) {
		Board flat_board = flattenBoard();
		Hold new_h = new Hold();
		
		HoldGenerationReturnStatus status = HoldGenerationReturnStatus.FAILURE;
		if (settings.generateLeastCommonHoldType()) {
			status = generateLeastCommonTypeHoldImpl(flat_board, new_h);
		} else {
			HashSet<Hold.Types> generate_types = settings.getHoldTypeToGenerate();
			// TODO 
			System.out.println("This needs to be added LOL");
		}
		
		// Make sure to do something on failure, 
		// TODO we should probably return the status from this function
		if (status == HoldGenerationReturnStatus.FAILURE) {
			return Optional.empty();
		}
		
		PerspectiveTransform to_old = getTransform(flat_board, m_board);
		new_h.setPosition(transformPoint(new_h.position(), to_old));
		
		return Optional.of(new_h);
	}
	
	private Color getPixelProximityColour(
			Board b,
			int i, int j, 
			int brightness_factor, 
			HashSet<Hold.Types> hold_types_to_show,
			boolean hold_types_exact_match) {
		int blueness = 244;
		int redness = 44;
		int greenness = 44;
		int blue_decrease_amount = 200;
		int red_increase_amount = 200;
		ArrayList<Hold> holds = b.getHolds();
		double smallest_proximity = Double.POSITIVE_INFINITY;
		for (Iterator<Hold> it = holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			//h.print();
			if (
				(!hold_types_exact_match && h.isOneOf(hold_types_to_show))
			||  (hold_types_exact_match && h.isTypes(hold_types_to_show))) {
				//System.out.println(hold_types_to_show);
				// Pixel to hold vector
				Vector2 hold_centre = h.getCentrePoint();
				Vector2 pixel_to_hold_centre_v = new Vector2(i - hold_centre.x, j - hold_centre.y);
				double pixel_to_hold_grad = pixel_to_hold_centre_v.gradient();
				// y = pixel_to_hold_grad * x + c
				// y - pixel_to_hold_grad * x = c
//				double c = hold_centre.y - (pixel_to_hold_grad * hold_centre.x);
				// x^2 / h.size().x^2 + y^2 / h.size().y^2 = 1  ellipse equxation
//				double hold_circ_x = ;
				/////// TODO This is very simplistic, 
				// ideally we should account for the elliptical nature of the hold
				// The above comments are a start... The rest is in the yellow notebook (FELIX)
				double pixel_to_hold = pixel_to_hold_centre_v.length() - h.size().y;
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
		MainWindow.m_instruction_panel.m_instruction_label.setText("Generating heatmap..");
		MainWindow.m_instruction_panel.showProgressBar();
		Board flat_board = flattenBoard();
		Vector2 image_size = flat_board.getBoardSize();
		MainWindow.m_instruction_panel.updateProgressBarRange(0, (int)image_size.x);
		BufferedImage image = new BufferedImage(
				(int)image_size.x, 
				(int)image_size.y, 
				BufferedImage.TYPE_INT_RGB
			);
		for (int i = 0; i < image_size.x; i++) {
			MainWindow.m_instruction_panel.updateProgressBar(i);;
			for (int j = 0; j < image_size.y; j++) {
				// Calculate pixel
				Color c = getPixelProximityColour(
						flat_board,
						i, j, 
						brightness_factor, 
						hold_types_to_show, 
						hold_types_exact_match);
				image.setRGB(i, j, c.getRGB());
			}
		}
		MainWindow.m_instruction_panel.hideProgressBar();
		return image;
	}
	
	private Vector2 getFlatBoardSize() {
		double ratio = m_flat_board_size.x / m_flat_board_size.y;
		
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
		
	private Vector2 transformPoint(
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
	
	private PerspectiveTransform getTransform(Board from, Board to) {
		ArrayList<Vector2> old_corners = from.getCorners();
		Vector2 A_old = old_corners.get(0);
		Vector2 B_old = old_corners.get(1);
		Vector2 C_old = old_corners.get(2);
		Vector2 D_old = old_corners.get(3);
		
		ArrayList<Vector2> to_corners = to.getCorners();
		Vector2 A_to = to_corners.get(0);
		Vector2 B_to = to_corners.get(1);
		Vector2 C_to = to_corners.get(2);
		Vector2 D_to = to_corners.get(3);
		
		return PerspectiveTransform.getQuadToQuad(
				 A_old.x, A_old.y,
				 B_old.x, B_old.y,
				 C_old.x, C_old.y,
				 D_old.x, D_old.y,
				 A_to.x, A_to.y,
				 B_to.x, B_to.y,
				 C_to.x, C_to.y,
				 D_to.x, D_to.y);
	}
	
	private Board flattenBoard() {
		Board flat_board = new Board();
		Vector2 flat_board_size = getFlatBoardSize();
		flat_board.setBoardDimensions(flat_board_size.x, flat_board_size.y);
		
		flat_board.addCorner(new Vector2(0,0));
		flat_board.addCorner(new Vector2(flat_board_size.x, 0));
		flat_board.addCorner(new Vector2(flat_board_size.x, flat_board_size.y));
		flat_board.addCorner(new Vector2(0, flat_board_size.y));
		
		PerspectiveTransform transformToFlat = getTransform(m_board, flat_board);
		
		ArrayList<Hold> old_holds = m_board.getHolds();
		for (Iterator<Hold> it = old_holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			Vector2 new_pos = transformPoint(
					new Vector2(h.position().x, h.position().y), transformToFlat);
			Vector2 old_size_pos = BoardFrame.getPointOnCircleFromRad(h.direction(), h.getCentrePoint(), h.size());
			Vector2 new_size_pos = transformPoint(old_size_pos, transformToFlat);
			
			Vector2 new_direction_size_vector = new Vector2(
					new_size_pos.x - new_pos.x, new_size_pos.y - new_pos.y
					);
			
			double old_size_ratio = h.size().x / h.size().y;
			
			Hold flat_hold = 
					new Hold(new_pos, 
							new Vector2(old_size_ratio * new_direction_size_vector.length(), new_direction_size_vector.length()), 
							h.direction());
			flat_hold.addTypes(h.getTypes());
			flat_board.addHold(flat_hold);
		}
		
		return flat_board;
	}
}
