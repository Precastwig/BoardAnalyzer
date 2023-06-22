package boardanalyzer.board_logic.analysis;

import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.HoldGenerationSettings;
import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;
import org.kynosarges.tektosyne.geometry.*;

import java.lang.reflect.Array;
import java.security.InvalidAlgorithmParameterException;
import java.util.*;

public class HoldSuggestionGenerator extends Analyzer {

    private final int[] m_hold_type_pref_ratio;
    private final int[] m_hold_direction_pref_ratio;

    private final int m_hold_size_min;
    private final int m_hold_size_max;
    public HoldSuggestionGenerator(
            Board board,
            Vector2 flat_board_ratio,
            int[] hold_type_pref_ratio,
            int[] hold_direction_pref_ratio,
            int hold_size_min,
            int hold_size_max) {
        super(board,  flat_board_ratio);
        m_hold_size_min = hold_size_min;
        m_hold_size_max = hold_size_max;
        m_hold_type_pref_ratio = hold_type_pref_ratio;
		m_hold_direction_pref_ratio = hold_direction_pref_ratio;
    }

    private Vector2 suggestHoldLocationImpl(FlatBoard b) {
        ArrayList<Vector2> new_locs = getAllPotentialNewHoldLocations(b);
        if (new_locs.size() < 1) {
            System.out.println("getAllPotentialNewHoldLocations has not generated any locations!");
        }
        Random r = new Random();
        int index = r.nextInt(new_locs.size());
        return new_locs.get(index);
    }

    private Vector2 getRandomValidPosition(FlatBoard b) {
        Vector2 pos = new Vector2(0,0);
        do {
            int EDGE_DISTANCE = (int) (Math.min(b.getBoardWidth(), b.getBoardHeight())*0.05);
            Random r = new Random();
            pos.x = r.nextInt((int) b.getBoardWidth() - 2 * EDGE_DISTANCE) + EDGE_DISTANCE;
            pos.y = r.nextInt((int) b.getBoardHeight()- 2 * EDGE_DISTANCE) + EDGE_DISTANCE;
        } while (checkLocationValid(pos, m_hold_size_min, b));
        return pos;
    }

    private ArrayList<Vector2> getValidVoronoiNewLocations(FlatBoard b) {
        ArrayList<Vector2> return_list = new ArrayList<Vector2>();

        // Use Voronoi to generate potential positions
        PointD[] hold_positions = getPointArrayFromHolds(b.getHolds());
        RectD clipping_rect = new RectD(0.0, 0.0, b.getBoardWidth(), b.getBoardHeight());
        VoronoiResults v = Voronoi.findAll(hold_positions, clipping_rect);
        PointD[] points = v.voronoiVertices;

        // Trim this set of points using distances
        for (PointD point : points) {
            Vector2 p = new Vector2(point);
            if (checkLocationValid(p, m_hold_size_min, b)) {
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
            if (checkLocationValid(p, m_hold_size_min, b)) {
                return_list.add(p);
            }
        }

        return return_list;
    }

    private ArrayList<Vector2> getAllPotentialNewHoldLocations(
            FlatBoard b
    ) {
        ArrayList<Vector2> return_list = new ArrayList<Vector2>();
        ArrayList<Hold> holds = b.getHolds();

        if (holds.size() < 3) {
            // Pick randomly
            return_list.add(getRandomValidPosition(b));
            return return_list;
        }

        ArrayList<Vector2> potential_locations = getValidVoronoiNewLocations(b);

        // Cull the 50% of points that are closest to their nearest hold
        double total_distance = 0;
        for (Vector2 p : potential_locations) {
            total_distance += b.getDistanceToNearestHold(p);
        }
        double average = total_distance / potential_locations.size();
        for (Vector2 p: potential_locations) {
            if (b.getDistanceToNearestHold(p) > average) {
                return_list.add(p);
            }
        }

        return return_list;
    }

    private boolean checkLocationValid(
            Vector2 loc,
            double min_distance,
            FlatBoard b) {
        if (b.isOutsideBorders(loc) || b.existsHold((int)loc.x, (int)loc.y) ||
            loc.x < min_distance || loc.y < min_distance ||
                b.getBoardWidth() - loc.x < min_distance ||
                b.getBoardHeight() - loc.y < min_distance) {
            return false;
        }
        double dist = b.getDistanceToNearestHold(loc);
        return min_distance < dist;
    }

    public Hold.Type suggestHoldType(Hold h) {
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
        flat_board.removeHoldAt(h.position());
        return suggestNewHoldType(flat_board, flat_board.toFlat(h.getCentrePoint()));
    }

    public double suggestHoldDirection(Hold h) {
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
        flat_board.removeHoldAt(h.position());
        return suggestHoldDirectionImpl(flat_board, flat_board.toFlat(h.getCentrePoint()), h.size());
    }

    private ArrayList<Hold.Direction> getRelevantHoldDirectionsFromPosition(FlatBoard b, Vector2 position) {
        // If we are far left we don't want to generate right sidepulls or underclings
        // and vice versa
        double margin_size = 0.1;
        ArrayList<Hold.Direction> relevant_directions = new ArrayList<Hold.Direction>(Arrays.asList(Hold.Direction.values()));
        if (position.x < b.getBoardWidth() * margin_size) {
            relevant_directions.remove(Hold.Direction.RIGHT_SIDEPULL);
            relevant_directions.remove(Hold.Direction.UNDERCUT);
        }
        if (b.getBoardWidth() * (1-margin_size) < position.x) {
            relevant_directions.remove(Hold.Direction.LEFT_SIDEPULL);
            relevant_directions.remove(Hold.Direction.UNDERCUT);
        }
        if (position.y < b.getBoardHeight() * 0.3) {
            // No underclings near the very top
            relevant_directions.remove(Hold.Direction.UNDERCUT);
        }

        return relevant_directions;
    }

    private double suggestHoldDirectionImpl(FlatBoard b, Vector2 position, Vector2 size) {
        // First check for proximity to top of board, this is the most obvious direction restriction
        if (position.y < b.getBoardHeight() * 0.05) {
            return Hold.Direction.getAngle(Hold.Direction.UP);
        }

        double hold_vicinity_distance = getProximityDistance(b);
        ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
        if (holds_in_proximity.isEmpty()) {
            // Generate a direction based upon the preference distribution
            ArrayList<Hold.Direction> choices = new ArrayList<Hold.Direction>();
            // Build up an array list that contains hold directions for each number in the preference ratio
            // e.g. m_hold_direction_pref_ratio = [4, 0, 0, 0, 0, 1]
            // choices = [UP, UP, UP, UP, UNDERCUT]
            for (Hold.Direction dir : getRelevantHoldDirectionsFromPosition(b, position)) {
                Hold.Direction[] temp = new Hold.Direction[m_hold_direction_pref_ratio[dir.ordinal()]];
                Arrays.fill(temp, dir);
                Collections.addAll(choices, temp);
            }
            // Then pick one randomly
            Random r = new Random();
            Hold.Direction new_dir = choices.get(r.nextInt(choices.size()));
            return Hold.Direction.getRandomAngle(new_dir);
        }

        Hold.Direction dir = getLeastFilledHoldDirection(b, position, getRelevantHoldDirectionsFromPosition(b, position));
        return Hold.Direction.getRandomAngle(dir);
    }

    private Hold.Type suggestNewHoldType(FlatBoard b, Vector2 position) {
        double lowest_hold_y = b.getLowestAllowedHandHoldHeight();

        if (lowest_hold_y < position.y) {
            // Bottom of the board
            return Hold.Type.FOOT;
        }

        return getLeastFilledHoldType(b, position, Hold.Type.getHandTypes());
    }

    private double suggestNewHoldSize(FlatBoard b, Vector2 position) throws InvalidAlgorithmParameterException {
        double max_size = m_hold_size_max;
        if (b.getHolds().size() != 0) {
            Hold nearest_hold = b.getNearestHold(position);
            // TODO This does not account for ellipses!
            max_size = nearest_hold.getCentrePoint().distanceTo(position) - nearest_hold.size().x;

            if (max_size < m_hold_size_min) {
                throw new InvalidAlgorithmParameterException();
            }
            max_size = Math.min(m_hold_size_max, max_size);
        }

        // Generate a random size between the smallest and largest
        return m_hold_size_min + (Math.random() * (max_size - m_hold_size_min));
    }

    private Hold.Type getLeastFilledHoldType(FlatBoard b,  Vector2 position, Hold.Type[] types) {
        Hold.Type least_filled_type = types[0];
        double hold_vicinity_distance = getProximityDistance(b);
        double smallest_proportion = Double.POSITIVE_INFINITY;
        ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
        for (Hold.Type type : types) {
            double proportion = (double)Board.countType(holds_in_proximity, type) /
                            (double)m_hold_type_pref_ratio[type.ordinal()];
            if (proportion < smallest_proportion) {
                least_filled_type = type;
                smallest_proportion = proportion;
            }
        }
        return least_filled_type;
    }

    private Hold.Direction getLeastFilledHoldDirection(FlatBoard b, Vector2 position, ArrayList<Hold.Direction> directions) {
        Hold.Direction least_filled_direction = Hold.Direction.UP;
        double smallest_proportion = Double.POSITIVE_INFINITY;
        double hold_vicinity_distance = getProximityDistance(b);
        ArrayList<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);
        for (Hold.Direction direction_label : directions) {
            double proportion =
                    (double)Board.countDirection(holds_in_proximity, direction_label) /
                    (double)m_hold_direction_pref_ratio[direction_label.ordinal()];
            if (proportion < smallest_proportion) {
                least_filled_direction = direction_label;
                smallest_proportion = proportion;
            }
        }
        return least_filled_direction;
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
