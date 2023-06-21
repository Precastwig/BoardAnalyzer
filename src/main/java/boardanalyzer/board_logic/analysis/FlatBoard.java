package boardanalyzer.board_logic.analysis;

import boardanalyzer.BoardPanel;
import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public final class FlatBoard extends Board {
    private final PerspectiveTransform m_to_flat;
    private final PerspectiveTransform m_from_flat;
    public FlatBoard(Board b, Vector2 flat_board_size) {
        setBoardDimensions(flat_board_size.x, flat_board_size.y);


        addCorner(new Vector2(0, 0));
        addCorner(new Vector2(flat_board_size.x, 0));
        addCorner(new Vector2(flat_board_size.x, flat_board_size.y));
        addCorner(new Vector2(0, flat_board_size.y));

        m_to_flat = Board.getTransform(b, this);
        m_from_flat = Board.getTransform(this, b);

        double lowest_height = b.getLowestAllowedHandHoldHeightNoDefault();
        if (lowest_height != 0.0) {
            // To (partially) allow for image skew, transform the line and take the average y value
            // TODO if we saved this line as two points rather than a horizontal line then this would be more accurate
            // I just cant be bothered
            Vector2 temp_1 = new Vector2(0, lowest_height);
            Vector2 temp_2 = new Vector2(b.getBoardWidth(), lowest_height);
            Vector2 new_temp_1 = toFlat(temp_1);
            Vector2 new_temp_2 = toFlat(temp_2);
            // Take the average of the two new points y values
            lowest_height = (new_temp_1.y + new_temp_2.y) / 2.0;
        }
        setLowestAllowedHandHoldHeight(lowest_height);

        ArrayList<Hold> old_holds = b.getHolds();
        for (Hold h : old_holds) {
            Vector2 new_pos = toFlat(
                    new Vector2(h.position().x, h.position().y));
            Vector2 old_size_pos = BoardPanel.getPointOnCircleFromRad(h.direction(), h.getCentrePoint(), h.size());
            Vector2 new_size_pos = toFlat(old_size_pos);

            Vector2 new_direction_size_vector = new Vector2(
                    new_size_pos.x - new_pos.x, new_size_pos.y - new_pos.y
            );

            double old_size_ratio = h.size().x / h.size().y;

            Hold flat_hold =
                    new Hold(new_pos,
                            new Vector2(old_size_ratio * new_direction_size_vector.length(), new_direction_size_vector.length()),
                            h.direction());
            flat_hold.addTypes(h.getTypes());
            addHold(flat_hold);
        }
    }

    private Vector2 toFlat(Vector2 point) {
        Point2D.Double in = point.toPoint2D();
        Point2D.Double out = new Point2D.Double();
        m_to_flat.transform(in, out);
        return new Vector2(out);
    }

    public Vector2 fromFlat(Vector2 point) {
        Point2D.Double in = point.toPoint2D();
        Point2D.Double out = new Point2D.Double();
        m_from_flat.transform(in, out);
        return new Vector2(out);
    }
}
