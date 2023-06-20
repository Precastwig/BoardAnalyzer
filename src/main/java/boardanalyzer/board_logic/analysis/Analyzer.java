package boardanalyzer.board_logic.analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;

import boardanalyzer.BoardFrame;
import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.HoldGenerationSettings;
import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiEdge;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

public final class Analyzer {
	public static class AngleProportions {
		private int[] m_angle_buckets;
		private int[] m_ideal_proportion;

		AngleProportions(ArrayList<Hold> holds, int[] ideal_proportion) {
			m_angle_buckets = new int[]{
					0,
					0,
					0,
					0,
					0,
					0
			};

			for (Hold h : holds) {
				double dir = h.direction();
				m_angle_buckets[Hold.Direction.classifyAngle(dir).ordinal()] += 1;
			}

			m_ideal_proportion = ideal_proportion;
		}
		
		private Hold.Direction getLeastFilledBucket() {
			Hold.Direction least_filled_bucket = Hold.Direction.UP;
			double smallest_proportion = Double.POSITIVE_INFINITY;
			for (Hold.Direction direction_label : Hold.Direction.values()) {
				double proportion = (double)m_angle_buckets[direction_label.ordinal()] / (double)m_ideal_proportion[direction_label.ordinal()]; 
				if (proportion < smallest_proportion) {
					least_filled_bucket = direction_label;
					smallest_proportion = proportion;
				}
			}
			return least_filled_bucket;
		}
		
		public double getNewAngle() {
			Hold.Direction least_filled_bucket = getLeastFilledBucket();
			return Hold.Direction.getRandomAngle(least_filled_bucket);
		}
	}
	
	private final Board m_board;
	private final Vector2 m_flat_board_size;
	private final int[] m_hold_type_pref_ratio;
	private final int[] m_hold_direction_pref_ratio;
	
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
		for (Hold h : holds) {
			al.add(new PointD(h.getCentrePoint().x, h.getCentrePoint().y));
		}
		return al.toArray(new PointD[0]);
	}
	
	private boolean checkLocationValid(
			Vector2 loc,
			double min_distance,
			double max_distance,
			Board b) {
		if (!b.isInsideBorders(loc) || b.existsHold((int)loc.x, (int)loc.y)) {
			return false;
		}
		Hold nearest_hold = b.getNearestHold(loc);
		double dist = nearest_hold.getCentrePoint().distanceTo(loc);
		if (min_distance < dist && dist < max_distance) {
			return true;
		}
		return false;
	}
	
	private ArrayList<Vector2> getAllPotentialNewHoldLocations(
			Board b,
			double min_distance,
			double max_distance,
			Optional<Hold.Type> hold_type
			) {
		ArrayList<Vector2> return_list = new ArrayList<Vector2>();
		ArrayList<Hold> holds = b.getHolds();
		
		if (holds.size() < 3) {
			// Pick randomly
			int ran_x;
			int ran_y;
			do {
				int EDGE_DISTANCE = (int) (Math.min(b.getBoardWidth(), b.getBoardHeight())*0.05);
				Random r = new Random();
				ran_x = r.nextInt((int) b.getBoardWidth() - 2 * EDGE_DISTANCE) + EDGE_DISTANCE;
				ran_y = r.nextInt((int) b.getBoardHeight()- 2 * EDGE_DISTANCE) + EDGE_DISTANCE;
			} while (b.existsHold(ran_x, ran_y));
			return_list.add(new Vector2(ran_x, ran_y));
			return return_list;
		}
		
		// Use Voronoi to generate potential positions
		PointD[] hold_positions = getPointArrayFromHolds(holds);
		RectD clipping_rect = new RectD(0.0, 0.0, b.getBoardWidth(), b.getBoardHeight());
		VoronoiResults v = Voronoi.findAll(hold_positions, clipping_rect);
		PointD[] points = v.voronoiVertices;
		
		// Trim this set of points using distances
		for (int i = 0; i < points.length; i++) {
			Vector2 p = new Vector2(points[i]);
			if (checkLocationValid(p, min_distance, max_distance, b)) {
				return_list.add(p);
			}
		}

		VoronoiEdge[] edges = v.voronoiEdges;
		// Find the longest edge

		for (VoronoiEdge edge : edges) {
			Vector2 p1 = new Vector2(points[edge.vertex1]);
			Vector2 p2 = new Vector2(points[edge.vertex2]);

			Vector2 line = new Vector2(p1.x - p2.x, p1.y - p2.y);
			Vector2 p = new Vector2(p2.x + 0.5 * line.x, p2.y + 0.5 * line.y);
			if (checkLocationValid(p, min_distance, max_distance, b)) {
				return_list.add(p);
			}
		}
		
		return return_list;
	}
	
	// Return the location in the same space as b
	private Vector2 getNewHoldLocation(Board b) {
		ArrayList<Vector2> new_locs = getAllPotentialNewHoldLocations(b, 10, Double.POSITIVE_INFINITY, Optional.empty());
		if (new_locs.size() < 1) {
			System.out.println("getAllPotentialNewHoldLocations has not generated any locations!");
		}
		Random r = new Random();
		int index = r.nextInt(new_locs.size());
		return new_locs.get(index);
	}
	
//// I normally hate pushing commented out code, 
//// but the analysis done here is solid and I probably want it for something...
//// (also no one can stop me, mwah hah hah hah)
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
		for (Hold h : b.getHolds()) {
			if (h.getCentrePoint().distanceTo(position) < range) {
				holds_in_proximity.add(h);
			}
		}
		return holds_in_proximity;
	}
	
	public Hold.Type suggestHoldTypes(Hold h) {
		Board flat_board = flattenBoard();
		flat_board.removeHoldAt(h.position());
		return getLeastFilledHoldType(flat_board, h.getCentrePoint(), Hold.Type.getHandTypes());
	}
	
	public double suggestHoldDirection(Hold h) {
		Board flat_board = flattenBoard();
		flat_board.removeHoldAt(h.position());
		return getNewHoldDirection(flat_board, h.getCentrePoint(), h.size());
	}
	
	private double getProximityDistance(Board b) {
		return Math.min(b.getBoardHeight(), b.getBoardWidth()) / 2;
	}
	
	private double getNewHoldDirection(Board b, Vector2 position, Vector2 size) {
		double default_dir = Hold.Direction.getAngle(Hold.Direction.UP);
		// First check for proximity to top of board, this is the most obvious direction restriction
		if (position.y < b.getBoardHeight() * 0.05) {
			return default_dir;
		}

		double hold_vicinity_distance = getProximityDistance(b);
		ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
		if (holds_in_proximity.isEmpty()) {
			// Generate a direction based upon the preference distribution
			ArrayList<Hold.Direction> choices = new ArrayList<Hold.Direction>();
			for (Hold.Direction dir : Hold.Direction.values()) {
				Hold.Direction[] temp = new Hold.Direction[m_hold_direction_pref_ratio[dir.ordinal()]];
				Arrays.fill(temp, dir);
				Collections.addAll(choices, temp);
			}
			Random r = new Random();
			Hold.Direction new_dir = choices.get(r.nextInt(choices.size()));
			return Hold.Direction.getRandomAngle(new_dir);
		}
		AngleProportions proximate_stats = new AngleProportions(holds_in_proximity, m_hold_direction_pref_ratio);
		return proximate_stats.getNewAngle();
	}
	
	private double getNewHoldSize(Board b, Vector2 position) throws InvalidAlgorithmParameterException {
		Hold nearest_hold = b.getNearestHold(position);
		double MAX_HOLD_SIZE = 100;
		// TODO This does not account for ellipses!
		double min_size = 30;
		for (Hold h : b.getHolds()) {
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
	
	private Hold.Type getLeastFilledHoldType(Board b, Vector2 position, Hold.Type[] types) {
		Hold.Type least_filled_type = types[0];
		double hold_vicinity_distance = getProximityDistance(b);
		double smallest_proportion = Double.POSITIVE_INFINITY;
		ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
		for (Hold.Type type : types) {
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
			Hold.Type type = getLeastFilledHoldType(b, new_loc, Hold.Type.getHandTypes());
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
			HashSet<Hold.Type> generate_types = settings.getHoldTypeToGenerate();
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
			HashSet<Hold.Type> hold_types_to_show,
			boolean hold_types_exact_match) {
		int blueness = 244;
		int redness = 44;
		int greenness = 44;
		int blue_decrease_amount = 200;
		int red_increase_amount = 200;
		ArrayList<Hold> holds = b.getHolds();
		double smallest_proximity = Double.POSITIVE_INFINITY;
		for (Hold h : holds) {
			//h.print();
			if (
					(!hold_types_exact_match && h.isOneOf(hold_types_to_show))
							|| (hold_types_exact_match && h.isTypes(hold_types_to_show))) {
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
				// The above comments are a start... The rest is in the yellow notebook (FELIX...)
				double pixel_to_hold = pixel_to_hold_centre_v.length() - h.size().y;
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
		}
		double relative_brightness = heatmapFunction(smallest_proximity, brightness_factor);
		return new Color(
				Math.min((int)(redness + (relative_brightness * red_increase_amount)), 244), 
				greenness, 
				Math.max((int)(blueness - (relative_brightness * blue_decrease_amount)), 44));
	}
	
	public BufferedImage getHeatmap(
			int brightness_factor, 
			HashSet<Hold.Type> hold_type_to_show,
			boolean hold_types_exact_match,
			boolean hold_direction_matters) {
		//MainWindow.m_instruction_panel.showProgressBar();
		Board flat_board = flattenBoard();
		Vector2 image_size = flat_board.getBoardSize();
		//MainWindow.m_instruction_panel.updateProgressBarRange(0, (int)image_size.x);
		BufferedImage image = new BufferedImage(
				(int)image_size.x, 
				(int)image_size.y, 
				BufferedImage.TYPE_INT_RGB
			);
		for (int i = 0; i < image_size.x; i++) {
			//MainWindow.m_instruction_panel.updateProgressBar(i);;
			for (int j = 0; j < image_size.y; j++) {
				// Calculate pixel
				Color c = getPixelProximityColour(
						flat_board,
						i, j, 
						brightness_factor,
						hold_type_to_show,
						hold_types_exact_match);
				image.setRGB(i, j, c.getRGB());
			}
		}
		//MainWindow.m_instruction_panel.hideProgressBar();
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
		System.out.println("Regular board size: " + m_board.getBoardWidth() + ", " + m_board.getBoardHeight());
		System.out.println("Flat board size: " + flat_board.getBoardWidth() + ", " + flat_board.getBoardHeight());
		return flat_board;
	}
}
