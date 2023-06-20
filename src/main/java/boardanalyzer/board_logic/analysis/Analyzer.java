package boardanalyzer.board_logic.analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;

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

abstract public class Analyzer {
	public static class AngleProportions {
		private final int[] m_angle_buckets;
		private final int[] m_ideal_proportion;

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
	
	protected Board m_board;
	protected Vector2 m_flat_board_size;
	
	public enum HoldGenerationReturnStatus {
		FAILURE,
		SUCCESS
	}
	
	public Analyzer(Board board, Vector2 flat_board_ratio) {
		m_board = board;
		m_flat_board_size = getFlatBoardSize(flat_board_ratio);
	}
	
	protected PointD[] getPointArrayFromHolds(ArrayList<Hold> holds) {
		ArrayList<PointD> al = new ArrayList<PointD>();
		for (Hold h : holds) {
			al.add(new PointD(h.getCentrePoint().x, h.getCentrePoint().y));
		}
		return al.toArray(new PointD[0]);
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
	
	protected ArrayList<Hold> getHoldsInProximity(FlatBoard b, Vector2 position, double range) {
		ArrayList<Hold> holds_in_proximity = new ArrayList<Hold>();
		for (Hold h : b.getHolds()) {
			if (h.getCentrePoint().distanceTo(position) < range) {
				holds_in_proximity.add(h);
			}
		}
		return holds_in_proximity;
	}
	
	protected double getProximityDistance(FlatBoard b) {
		return Math.min(b.getBoardHeight(), b.getBoardWidth()) / 2;
	}
	
	private Vector2 getFlatBoardSize(Vector2 flat_board_ratio) {
		double ratio = flat_board_ratio.x / flat_board_ratio.y;
		
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
}
