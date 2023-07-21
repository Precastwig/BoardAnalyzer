package boardanalyzer.board_logic.analysis;

import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.side_panel_tabs.HoldGenerationSettings;
import boardanalyzer.utils.PerspectiveTransform;
import boardanalyzer.utils.Vector2;
import org.kynosarges.tektosyne.geometry.*;

import java.security.InvalidAlgorithmParameterException;
import java.util.*;

public class HoldSuggestionGenerator extends Analyzer {

    private final double[] m_hold_type_pref_percentages;
    private final double[] m_hold_direction_pref_percentages;

    private final int m_hold_size_min;
    private final int m_hold_size_max;

    public HoldSuggestionGenerator(
            Board board,
            Vector2 flat_board_ratio,
            double[] hold_type_pref_perc,
            double[] hold_direction_pref_perc,
            int hold_size_min,
            int hold_size_max) {
        super(board, flat_board_ratio);
        m_hold_size_min = hold_size_min;
        m_hold_size_max = hold_size_max;
        m_hold_type_pref_percentages = hold_type_pref_perc;
        m_hold_direction_pref_percentages = hold_direction_pref_perc;
    }

    private List<Vector2> cullClosestHalf(FlatBoard b, List<Vector2> positions) {
        // Cull the 50% of points that are closest to their nearest hold
        if (positions.size() < 5) {
            return positions;
        }
        double total_distance = 0;
        for (Vector2 p : positions) {
            total_distance += b.getDistanceToNearestHold(p);
        }
        double average = total_distance / positions.size();
        List<Vector2> culled_list = new ArrayList<>();
        for (Vector2 p : positions) {
            if (b.getDistanceToNearestHold(p) > average) {
                culled_list.add(p);
            }
        }
        return culled_list;
    }

    private Vector2 suggestHoldLocationImpl(FlatBoard b) {
        List<Vector2> new_locs = getAllPotentialNewHoldLocations(b);
        new_locs = cullClosestHalf(b, new_locs);

        // Choose a random one
        Random r = new Random();
        int index = r.nextInt(new_locs.size());
        return new_locs.get(index);
    }

    private Vector2 suggestHoldLocationForType(FlatBoard b, Hold.Type type) {
        List<Vector2> new_locs = getAllPotentialNewHoldLocations(b);

        if (type == Hold.Type.FOOT) {
            new_locs.removeIf(
                    v -> (v.y < b.getBoardHeight() / 2)
            );
        }

        new_locs = cullClosestHalf(b, new_locs);

        Vector2 return_position = new_locs.get(0); // default
        double least_holds_of_type_in_proximity_percentage = 1.0;
        double furthest_hold_of_type = 0;
        for (Vector2 loc : new_locs) {
            List<Hold> nearby_holds = b.getHoldsInRange(loc, getProximityDistance(b));
            double closest_distance = Double.POSITIVE_INFINITY;
            int holds_in_proximity_of_type = 0;
            for (Hold h : nearby_holds) {
                if (h.typesContain(type)) {
                    double h_distance = loc.distanceTo(h.position());
                    if (h_distance < closest_distance) {
                        closest_distance = h_distance;
                    }
                    holds_in_proximity_of_type += 1;
                }
            }

            double holds_in_proximity_of_type_percentage = (holds_in_proximity_of_type) / (double) nearby_holds.size();
            if (holds_in_proximity_of_type_percentage < least_holds_of_type_in_proximity_percentage) {
                least_holds_of_type_in_proximity_percentage = holds_in_proximity_of_type_percentage;

                return_position = loc;
            } else if (holds_in_proximity_of_type_percentage == least_holds_of_type_in_proximity_percentage) {
                if (closest_distance > furthest_hold_of_type) {
                    furthest_hold_of_type = closest_distance;
                }

            }
        }
        return return_position;
    }

    private Vector2 getRandomValidPosition(FlatBoard b) {
        Vector2 pos = new Vector2(0, 0);
        do {
            int EDGE_DISTANCE = (int) (Math.min(b.getBoardWidth(), b.getBoardHeight()) * 0.05);
            Random r = new Random();
            pos.x = r.nextInt((int) b.getBoardWidth() - 2 * EDGE_DISTANCE) + EDGE_DISTANCE;
            pos.y = r.nextInt((int) b.getBoardHeight() - 2 * EDGE_DISTANCE) + EDGE_DISTANCE;
        } while (checkLocationValid(pos, m_hold_size_min, b));
        return pos;
    }

    private List<Vector2> getValidVoronoiNewLocations(FlatBoard b) {
        List<Vector2> return_list = new ArrayList<>();

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

    private List<Vector2> getAllPotentialNewHoldLocations(
            FlatBoard b
    ) {
        List<Vector2> return_list = new ArrayList<>();
        List<Hold> holds = b.getHolds();

        if (holds.size() < 3) {
            // Pick randomly
            return_list.add(getRandomValidPosition(b));
            return return_list;
        }

        List<Vector2> potential_locations = getValidVoronoiNewLocations(b);
        if (potential_locations.size() < 1) {
            System.out.println("getAllPotentialNewHoldLocations has not generated any locations!");
        }
        return potential_locations;
    }

    private boolean checkLocationValid(
            Vector2 loc,
            double min_distance,
            FlatBoard b) {
        if (b.isOutsideBorders(loc) || b.existsHold((int) loc.x, (int) loc.y) ||
                loc.x < min_distance || loc.y < min_distance ||
                b.getBoardWidth() - loc.x < min_distance ||
                b.getBoardHeight() - loc.y < min_distance ||
                loc.y > b.getLowestAllowedHandHoldHeight()) {
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
        return suggestHoldDirectionImpl(flat_board, flat_board.toFlat(h.getCentrePoint()), h.getTypes());
    }

    private List<Hold.Direction> getRelevantHoldDirectionsFromPosition(FlatBoard b, Vector2 position) {
        // If we are far left we don't want to generate right sidepulls or underclings
        // and vice versa
        double margin_size = 0.2;
        List<Hold.Direction> relevant_directions = new ArrayList<>(Arrays.asList(Hold.Direction.values()));
        if (position.x < b.getBoardWidth() * margin_size) {
            relevant_directions.remove(Hold.Direction.RIGHT_SIDEPULL);
            relevant_directions.remove(Hold.Direction.UNDERCUT);
        }
        if (b.getBoardWidth() * (1 - margin_size) < position.x) {
            relevant_directions.remove(Hold.Direction.LEFT_SIDEPULL);
            relevant_directions.remove(Hold.Direction.UNDERCUT);
        }
        if (position.y < b.getBoardHeight() * 0.3) {
            // No underclings near the very top
            relevant_directions.remove(Hold.Direction.UNDERCUT);
        }

        return relevant_directions;
    }

    private List<Hold.Direction> getReleventHoldDirectionsFromType(Set<Hold.Type> types) {
        List<Hold.Direction> valid_directions = new ArrayList<>(Arrays.asList(Hold.Direction.values()));

        if (types.contains(Hold.Type.FOOT)) {
            // I don't think it makes any sense to put a "toehook" foothold on a board
            valid_directions.remove(Hold.Direction.UNDERCUT);
            valid_directions.remove(Hold.Direction.LEFT_SIDEPULL);
            valid_directions.remove(Hold.Direction.RIGHT_SIDEPULL);
        }

        if (types.contains(Hold.Type.PINCH)) {
            valid_directions.remove(Hold.Direction.UNDERCUT);
        }
        return valid_directions;
    }

    private double suggestHoldDirectionImpl(FlatBoard b, Vector2 position, Set<Hold.Type> types) {
        // First check for proximity to top of board, this is the most obvious direction restriction
        if (position.y < b.getBoardHeight() * 0.05) {
            return Hold.Direction.getAngle(Hold.Direction.UP);
        }

        List<Hold.Direction> valid_directions = getReleventHoldDirectionsFromType(types);
        valid_directions.retainAll(getRelevantHoldDirectionsFromPosition(b, position));
        if (valid_directions.isEmpty()) {
            System.out.println("Error: No valid direction for hold type and position");
            return Hold.Direction.getAngle(Hold.Direction.UP);
        }

        Hold.Direction dir = getHoldDirectionBasedOnProximityAndPreference(b, position, valid_directions);
        return Hold.Direction.getRandomAngle(dir);
    }

    private Hold.Type suggestNewHoldType(FlatBoard b, Vector2 position) {
        double lowest_hold_y = b.getLowestAllowedHandHoldHeight();

        if (lowest_hold_y < position.y) {
            // Bottom of the board
            return Hold.Type.FOOT;
        }

        return getHoldTypeBasedOnProximityAndPreference(b, position, Hold.Type.getHandTypes());
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

    private Hold.Type getHoldTypeBasedOnProximityAndPreference(FlatBoard b, Vector2 position, Hold.Type[] types) {
        Hold.Type least_filled_type = types[0];
        double hold_vicinity_distance = getProximityDistance(b);
        double largest_percentage = Double.NEGATIVE_INFINITY;
        List<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);

        if (holds_in_proximity.isEmpty()) {
            // Do global holds instead
            holds_in_proximity = b.getHolds();
        }

        for (Hold.Type type : types) {
            double proportion_percentage = (double) (Board.countType(holds_in_proximity, type)) / holds_in_proximity.size();
            double percentage_diff = m_hold_type_pref_percentages[type.ordinal()] - proportion_percentage;
            if (largest_percentage < percentage_diff) {
                least_filled_type = type;
                largest_percentage = percentage_diff;
            }
        }
        return least_filled_type;
    }

    private Hold.Direction getHoldDirectionBasedOnProximityAndPreference(
            FlatBoard b,
            Vector2 position,
            List<Hold.Direction> directions) {
        Hold.Direction least_filled_type = directions.get(0);
        double hold_vicinity_distance = getProximityDistance(b);
        double largest_percentage = Double.NEGATIVE_INFINITY;
        List<Hold> holds_in_proximity = getHoldsInProximity(b, position, hold_vicinity_distance);

        if (holds_in_proximity.isEmpty()) {
            // Do global holds instead
            holds_in_proximity = b.getHolds();
        }

        for (Hold.Direction dir : directions) {
            double proportion_percentage = (double) (Board.countDirection(
                    holds_in_proximity,
                    dir)) / holds_in_proximity.size();
            double percentage_diff = m_hold_direction_pref_percentages[dir.ordinal()] - proportion_percentage;
            if (largest_percentage < percentage_diff) {
                least_filled_type = dir;
                largest_percentage = percentage_diff;
            }
        }
        return least_filled_type;
    }

    private HoldGenerationReturnStatus generateHoldOfType(FlatBoard b, Hold new_hold, Hold.Type type) {
        try {
            new_hold.addType(type);
            Vector2 new_loc = suggestHoldLocationForType(b, type);
            new_hold.setCentrePoint(new_loc);
            double new_size = suggestNewHoldSize(b, new_loc);
            Vector2 new_size_vector = new Vector2(new_size, new_size);
            new_hold.setSize(new_size_vector);
            double new_dir = suggestHoldDirectionImpl(b, new_loc, new_hold.getTypes());
            new_hold.setDirection(new_dir);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return HoldGenerationReturnStatus.FAILURE;
        }
        return HoldGenerationReturnStatus.SUCCESS;
    }

    private HoldGenerationReturnStatus generateLeastCommonTypeHandHoldImpl(FlatBoard b, Hold new_hold) {
        try {
            Vector2 new_loc = suggestHoldLocationImpl(b);
            new_hold.setCentrePoint(new_loc);
            Hold.Type type = getHoldTypeBasedOnProximityAndPreference(b, new_loc, Hold.Type.getHandTypes());
            new_hold.addType(type);
            double new_size = suggestNewHoldSize(b, new_loc);
            Vector2 new_size_vector = new Vector2(new_size, new_size);
            new_hold.setSize(new_size_vector);
            double new_dir = suggestHoldDirectionImpl(b, new_loc, new_hold.getTypes());
            new_hold.setDirection(new_dir);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return HoldGenerationReturnStatus.FAILURE;
        }

        return HoldGenerationReturnStatus.SUCCESS;
    }

    public Optional<Hold> generateHold(HoldGenerationSettings settings) {
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
        Hold new_h = new Hold();

        HoldGenerationReturnStatus status = settings.generateLeastCommonHoldType()
                ? generateLeastCommonTypeHandHoldImpl(flat_board, new_h)
                : generateHoldOfType(flat_board, new_h, settings.getHoldTypeToGenerate());

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
