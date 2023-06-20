package boardanalyzer.board_logic.analysis;

import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.utils.Vector2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

public class HeatmapGenerator extends Analyzer {
    public HeatmapGenerator(Board board, Vector2 flat_board_ratio) {
        super(board, flat_board_ratio);
    }

    private double heatmapFunction(double distance, int brightness_factor) {
        // Equation y =
        double distance_cap = (brightness_factor * Math.PI) / 2;
        distance = Math.min(distance, distance_cap);
        return  Math.max(Math.cos(distance / brightness_factor), 0.0);
    }

    private Color getPixelProximityColour(
            FlatBoard b,
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

    public BufferedImage generateHeatmap(
            int brightness_factor,
            HashSet<Hold.Type> hold_type_to_show,
            boolean hold_types_exact_match,
            boolean hold_direction_matters) {
        //MainWindow.m_instruction_panel.showProgressBar();
        FlatBoard flat_board = new FlatBoard(m_board, m_flat_board_size);
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
}
