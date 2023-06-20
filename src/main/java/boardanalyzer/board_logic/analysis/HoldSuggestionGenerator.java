package boardanalyzer.board_logic.analysis;

import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.HoldGenerationSettings;
import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;
import org.kynosarges.tektosyne.geometry.*;

import java.security.InvalidAlgorithmParameterException;
import java.util.*;

public class HoldSuggestionGenerator extends Analyzer {

    private final int[] m_hold_type_pref_ratio;
    private final int[] m_hold_direction_pref_ratio;
    public HoldSuggestionGenerator(
            Board board,
            Vector2 flat_board_ratio,
            int[] hold_type_pref_ratio,
            int[] hold_direction_pref_ratio) {
        super(board,  flat_board_ratio);
        m_hold_type_pref_ratio = hold_type_pref_ratio;
		m_hold_direction_pref_ratio = hold_direction_pref_ratio;
    }

    private Vector2 suggestHoldLocationImpl(FlatBoard b) {
        ArrayList<Vector2> new_locs = getAllPotentialNewHoldLocations(b, 10, Double.POSITIVE_INFINITY, Optional.empty());
        if (new_locs.size() < 1) {
            System.out.println("getAllPotentialNewHoldLocations has not generated any locations!");
        }
        Random r = new Random();
        int index = r.nextInt(new_locs.size());
        return new_locs.get(index);
    }

    private ArrayList<Vector2> getAllPotentialNewHoldLocations(
            FlatBoard b,
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

    private boolean checkLocationValid(
            Vector2 loc,
            double min_distance,
            double max_distance,
            FlatBoard b) {
        if (!b.isInsideBorders(loc) || b.existsHold((int)loc.x, (int)loc.y)) {
            return false;
        }
        Hold nearest_hold = b.getNearestHold(loc);
        double dist = nearest_hold.getCentrePoint().distanceTo(loc);
        return min_distance < dist && dist < max_distance;
    }

    public Hold.Type suggestHoldType(Hold h) {
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
        flat_board.removeHoldAt(h.position());
        return getLeastFilledHoldType(flat_board, h.getCentrePoint(), Hold.Type.getHandTypes());
    }

    public double suggestHoldDirection(Hold h) {
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
        flat_board.removeHoldAt(h.position());
        return suggestHoldDirectionImpl(flat_board, h.getCentrePoint(), h.size());
    }

    private double suggestHoldDirectionImpl(FlatBoard b, Vector2 position, Vector2 size) {
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

    private double suggestNewHoldSize(FlatBoard b, Vector2 position) throws InvalidAlgorithmParameterException {
        Hold nearest_hold = b.getNearestHold(position);
        double MAX_HOLD_SIZE = 100;
        // TODO This does not account for ellipses!
        double min_size = 30;
        for (Hold h : b.getHolds()) {
            Vector2 hold_size = h.size();
            double hold_size_max = Math.max(hold_size.x, hold_size.y);
            double hold_sizemin = Math.min(hold_size.x, hold_size.y);
            if (hold_sizemin < min_size) {
                min_size = hold_sizemin;
            }
            if (hold_size_max > MAX_HOLD_SIZE) {
                MAX_HOLD_SIZE = hold_size_max;
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

    private Hold.Type getLeastFilledHoldType(FlatBoard b, Vector2 position, Hold.Type[] types) {
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

    private HoldGenerationReturnStatus generateLeastCommonTypeHoldImpl(FlatBoard b, Hold new_hold) {
        try {
            Vector2 new_loc = suggestHoldLocationImpl(b);
            new_hold.setCentrePoint(new_loc);
            Hold.Type type = getLeastFilledHoldType(b, new_loc, Hold.Type.getHandTypes());
            new_hold.addType(type);
            double new_size;
            new_size = suggestNewHoldSize(b, new_loc);
            Vector2 new_size_vector = new Vector2(new_size, new_size);
            new_hold.setSize(new_size_vector);
            double new_dir = suggestHoldDirectionImpl(b, new_loc, new_size_vector);
            new_hold.setDirection(new_dir);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println(e);
            return HoldGenerationReturnStatus.FAILURE;
        }

        return HoldGenerationReturnStatus.SUCCESS;
    }

    public Optional<Hold> generateHold(HoldGenerationSettings settings) {
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
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

        PerspectiveTransform to_old = Board.getTransform(flat_board, m_board);
        new_h.setPosition(flat_board.fromFlat(new_h.position()));

        return Optional.of(new_h);
    }
}
