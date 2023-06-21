package boardanalyzer.board_logic;

import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class Board implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Hold> m_holds;
	// Hold positions are stored in image space
	// This is probably not a good idea
	private ArrayList<Vector2> m_board_corners;
	private Vector2 m_board_size;
	protected double m_hand_hold_lowest_y_val;
	
	public Board() {
		m_holds = new ArrayList<Hold>();
		m_board_corners = new ArrayList<Vector2>();
		m_board_size = new Vector2();
		m_hand_hold_lowest_y_val = 0;
	}

	public double getLowestAllowedHandHoldHeightNoDefault() {
		return m_hand_hold_lowest_y_val;
	}

	public double getLowestAllowedHandHoldHeight() {
		if (m_hand_hold_lowest_y_val != 0) {
			return m_hand_hold_lowest_y_val;
		}
		double lowest_hold_y = 0;
		/// Find the "lowest hand hold" if we haven't set a value
		for (Hold h : m_holds) {
			if (!h.isFoot()) {
				if (h.position().y > lowest_hold_y) {
					lowest_hold_y = h.position().y;
				}
			}
		}
		// Anything lower than that (or default value of 20% of the board height) is a foot
		return Math.max(lowest_hold_y, m_board_size.y * 0.8);
	}

	public void setLowestAllowedHandHoldHeight(double y) {
		m_hand_hold_lowest_y_val = y;
	}
	
	public void clearCorners() {
		m_board_corners.clear();
	}
	
	public boolean areAllCornersSet() {
		return m_board_corners != null && m_board_corners.size() == 4; 
	}
	
	public void addCorner(Vector2 corner) {
		 m_board_corners.add(corner);
	}
	
	public ArrayList<Vector2> getCorners() {
		return m_board_corners;
	}
	
	public void addHold(Hold h) {
		m_holds.add(h);
	}
	
	public ArrayList<Hold> getHolds() {
		return m_holds;
	}
	
	public void removeHold(Hold h) {
		m_holds.remove(h);
	}
	
	public void removeHoldAt(Vector2 pos) {
		for (Hold hold : m_holds) {
			if (hold.position().x == pos.x && hold.position().y == pos.y) {
				m_holds.remove(hold);
				System.out.println("Removed hold");
				return;
			}
		}
	}
	
	public void setBoardDimensions(double w, double h) {
		// if our board contains points, they need to be modified by the new proportions
		PerspectiveTransform pt = PerspectiveTransform.getQuadToQuad(
				// from
				0, 0,
				m_board_size.x, 0,
				m_board_size.x, m_board_size.y, 
				0, m_board_size.y, 
				// to
				0, 0,
				w, 0, 
				w, h,
				0, h);
		for (Hold hold : m_holds) {
			Point2D.Double new_pos = new Point2D.Double(); 
			pt.transform(hold.position().toPoint2D(), new_pos);
			hold.setPosition(new Vector2(new_pos));
		}
		ArrayList<Vector2> new_board_corners = new ArrayList<Vector2>();
		for (Vector2 corner : m_board_corners) {
			Point2D.Double new_corner = new Point2D.Double();
			pt.transform(corner.toPoint2D(), new_corner);
			new_board_corners.add(new Vector2(new_corner));
		}
		m_board_corners = new_board_corners;
		m_board_size.x = w;
		m_board_size.y = h;
	}
	
	public double getBoardWidth() {
		return m_board_size.x;
	}
	
	public double getBoardHeight() {
		return m_board_size.y;
	}
	
	public Vector2 getBoardSize() {
		return m_board_size;
	}
	
	public void clearAllHolds() {
		m_holds.clear();
	}
	
	public boolean existsHold(int x, int y) {
		for (Hold mHold : m_holds) {
			if (mHold.contains(x, y)) {
				return true;
			}
		}
		return false;
	}
	
	public Hold getHold(int x, int y) throws IllegalAccessException {
		for (Hold h : m_holds) {
			if (h.contains(x, y)) {
				return h;
			}
		}
		throw new IllegalAccessException();
	}
	
	public Hold getNearestHold(Vector2 p) {
		double distance = Double.POSITIVE_INFINITY;
		Hold ret_h = m_holds.get(0);
		for (Hold h : m_holds) {
			double new_dis = h.getCentrePoint().distanceTo(p);
			if (new_dis < distance) {
				ret_h = h;
				distance = new_dis;
			}
		}
		return ret_h;
	}
	
	public boolean isOutsideBorders(Vector2 p) {
		if (!areAllCornersSet()) {
			return true;
		}
		
		int some_big_number = 1000000;
        Vector2 extreme = new Vector2(some_big_number, p.y);

        int count = 0, i = 0;
        do {
            int next = (i + 1) % 4;
            if (Vector2.intersectFiniteLines(m_board_corners.get(i), m_board_corners.get(next), p, extreme)) {
                if (Vector2.orientation(m_board_corners.get(i), p, m_board_corners.get(next)) == 0)
                    return !Vector2.onSegment(m_board_corners.get(i), p, m_board_corners.get(next));
                count++;
            }
            i = next;
        } while (i != 0);

        return (count & 1) != 1;
	}
	
	public Optional<Hold> createHold(Vector2 pos) {
		if (isOutsideBorders(pos)) {
			return Optional.empty();
		}
		Hold new_hold = new Hold();
		new_hold.setSize(new Vector2(50, 50));
		new_hold.setPosition(new Vector2(pos.x - 25, pos.y - 25));
//		System.out.println(x + "   " + y);
		m_holds.add(new_hold);
		return Optional.of(new_hold);
	}
	
	public int countType(Hold.Type t) {
		return countType(m_holds, t);
	}
	
	static public int countType(ArrayList<Hold> holds, Hold.Type t) {
		HashSet<Hold.Type> types = new HashSet<Hold.Type>();
		types.add(t);
		int count = 0;
		for (Hold h : holds) {
			if (h.isOneOf(types)) {
				count++;
			}
		}
		return count;
	}

	static public int countDirection(ArrayList<Hold> holds, Hold.Direction dir) {
		int count = 0;
		for (Hold h : holds) {
			if (Hold.Direction.classifyAngle(h.direction()) == dir) {
				count++;
			}
		}
		return count;
	}

	static public PerspectiveTransform getTransform(Board from, Board to) {
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
	
	public Hold.Type getLeastCommonType(HashSet<Hold.Type> ignored_types) throws InvalidAlgorithmParameterException {
		int type_count = Integer.MAX_VALUE;
		if (Hold.Type.values().length == ignored_types.size()) {
			throw new InvalidAlgorithmParameterException("All types are ignored");
		}
		Hold.Type min_type = Hold.Type.CRIMP;
		for (Hold.Type type : Hold.Type.values()) {
			if (!ignored_types.contains(type)) {				
				int new_count = countType(type);
				if (new_count < type_count) {
					type_count = new_count;
					min_type = type;
				}
			}
		}
		return min_type;
	}
}
