package boardanalyzer.board_logic;

import boardanalyzer.board_logic.Hold.Type;
import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;

public class Board implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Vector2 m_board_size;
    protected List<Hold> m_holds;
    protected double m_hand_hold_lowest_y_val;
    // Hold positions are stored in image space
    // This is probably not a good idea
    private List<Vector2> m_board_corners;

    public Board() {
        m_holds = new ArrayList<>();
        m_board_corners = new ArrayList<>();
        m_board_size = new Vector2();
        m_hand_hold_lowest_y_val = 0;
    }

    /**
     *
     */
    public static List<Hold> transformHolds(List<Hold> holds, PerspectiveTransform t) {
        List<Hold> transformed_holds = new ArrayList<>();
        for (Hold hold : holds) {
            Vector2 new_pos = hold.position().transformBy(t);
            Hold flat_hold =
                    new Hold(
                            new_pos,
                            new Vector2(hold.size()),
                            hold.direction());
            flat_hold.addTypes(hold.getTypes());
            transformed_holds.add(flat_hold);
        }
        return transformed_holds;
    }

    public static int countType(List<Hold> holds, Hold.Type t) {
        HashSet<Hold.Type> types = new HashSet<>();
        types.add(t);
        int count = 0;
        for (Hold h : holds) {
            if (h.isOneOf(types)) {
                count++;
            }
        }
        return count;
    }

    public static int countDirection(List<Hold> holds, Hold.Direction dir) {
        int count = 0;
        for (Hold h : holds) {
            if (Hold.Direction.classifyAngle(h.direction()) == dir) {
                count++;
            }
        }
        return count;
    }

    public static PerspectiveTransform getTransform(Board from, Board to) {
        List<Vector2> old_corners = from.getCorners();
        Vector2 A_old = old_corners.get(0);
        Vector2 B_old = old_corners.get(1);
        Vector2 C_old = old_corners.get(2);
        Vector2 D_old = old_corners.get(3);

        List<Vector2> to_corners = to.getCorners();
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

    public void transformAllHoldsBy(PerspectiveTransform transform) {
        m_holds = transformHolds(m_holds, transform);
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

    public void moveCorner(int index, Vector2 new_pos) {
        m_board_corners.get(index).x = new_pos.x;
        m_board_corners.get(index).y = new_pos.y;
    }

    public boolean areAllCornersSet() {
        return m_board_corners != null && m_board_corners.size() == 4;
    }

    public void addCorner(Vector2 corner) {
        m_board_corners.add(corner);
    }

    public List<Vector2> getCorners() {
        return m_board_corners;
    }

    public List<Vector2> getClonedCorners() {
        List<Vector2> copy = new ArrayList<>();
        for (Vector2 corner : m_board_corners) {
            copy.add(new Vector2(corner));
        }
        return copy;
    }

    public void addHold(Hold h) {
        m_holds.add(h);
    }

    public List<Hold> getHolds() {
        return m_holds;
    }

    public List<Hold> getHoldsInRange(Vector2 position, double range) {
        List<Hold> holds_in_range = new ArrayList<>();
        for (Hold h : m_holds) {
            if (position.distanceTo(h.position()) < range) {
                holds_in_range.add(h);
            }
        }
        return holds_in_range;
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
        List<Vector2> new_board_corners = new ArrayList<>();
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

    public boolean existsHold(double x, double y) {
        for (Hold mHold : m_holds) {
            if (mHold.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public int getNearestCornerIndex(Vector2 pos) {
        double shortest_distance = Double.POSITIVE_INFINITY;
        int index = 0;
        for (int i = 0; i < m_board_corners.size(); i++) {
            double dist = m_board_corners.get(i).distanceTo(pos);
            if (dist < shortest_distance) {
                shortest_distance = dist;
                index = i;
            }
        }
        return index;
    }

    public Hold getHold(double x, double y) throws IllegalAccessException {
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

    public double getDistanceToNearestHold(Vector2 p) {
        Hold nearest_hold = getNearestHold(p);
        /// TODO this needs to account for ellipses                vvvvvvvvvvvvv
        return nearest_hold.getCentrePoint().distanceTo(p) - nearest_hold.size().x;
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

    public Hold.Type getLeastCommonType(Set<Type> ignored_types) throws InvalidAlgorithmParameterException {
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
