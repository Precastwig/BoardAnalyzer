package boardanalyzer.ui.side_panel_tabs;

import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.board_logic.Hold.Direction;
import boardanalyzer.board_logic.Hold.Type;
import boardanalyzer.ui.basic_elements.BarChart;
import boardanalyzer.ui.basic_elements.BorderedPanel;
import boardanalyzer.ui.basic_elements.PercentageVisualiser;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

public class BoardStatistics extends BorderedPanel {
    private final JButton m_show_hold_stats_button;
    private final JLabel m_num_holds;
    private final JCheckBox m_ignore_feet_checkbox;
    private final JCheckBox m_single_colour_checkbox;
    private final JCheckBox m_hide_holds_checkbox;
    private final PercentageVisualiser<Hold.Type> m_hold_type_percentages;
    private final PercentageVisualiser<Hold.Direction> m_hold_direction_percentages;
    private final JSlider m_brightness_slider;

    public BarChart m_hold_type_chart;

    public BoardStatistics() {
        setToBoxLayout();
        m_show_hold_stats_button = new JButton("Show detailed hold statistics");
        m_num_holds = new JLabel();
        JLabel highlight_title_label = new JLabel("Hold highlight settings");
        JLabel highlight_size_label = new JLabel("Size:");
        m_brightness_slider = new JSlider(JSlider.HORIZONTAL, 10, 410, 100);
        m_brightness_slider.setMajorTickSpacing(50);
        m_brightness_slider.setMinorTickSpacing(25);
        m_brightness_slider.setPaintTicks(true);
        m_single_colour_checkbox = new JCheckBox("Single colour mode");
        m_hide_holds_checkbox = new JCheckBox("Hide holds during");
        m_hide_holds_checkbox.setSelected(true);
        m_hold_type_chart = new BarChart();
        m_hold_type_percentages = new PercentageVisualiser<>(Hold.Type.values());
        m_hold_direction_percentages = new PercentageVisualiser<>(Hold.Direction.values());
        m_ignore_feet_checkbox = new JCheckBox("Ignore feet");
        m_ignore_feet_checkbox.setSelected(true);

        m_show_hold_stats_button.setActionCommand("ShowHoldStats");
        m_ignore_feet_checkbox.setActionCommand("StatisticsIgnoreFeet");

        m_ignore_feet_checkbox.setAlignmentX(0.5f);
        m_hold_direction_percentages.setAlignmentX(0.5f);
        m_hold_type_percentages.setAlignmentX(0.5f);
        m_num_holds.setAlignmentX(0.5f);
        m_brightness_slider.setAlignmentX(0.5f);
        m_single_colour_checkbox.setAlignmentX(0.5f);
        m_hide_holds_checkbox.setAlignmentX(0.5f);
        highlight_size_label.setAlignmentX(0.5f);
        highlight_title_label.setAlignmentX(0.5f);

        add(m_num_holds);
        add(Box.createVerticalStrut(20));
        add(highlight_title_label);
        add(highlight_size_label);
        add(m_brightness_slider);
        add(m_single_colour_checkbox);
        add(m_hide_holds_checkbox);
        add(Box.createVerticalStrut(20));
        add(m_hold_type_percentages);
        add(Box.createVerticalStrut(50));
        add(m_hold_direction_percentages);
        add(Box.createVerticalStrut(50));
        add(m_ignore_feet_checkbox);
    }

    public void addActionListener(ActionListener listener) {
        m_show_hold_stats_button.addActionListener(listener);
        m_ignore_feet_checkbox.addActionListener(listener);
        m_hold_direction_percentages.addActionListener(listener);
        m_hold_type_percentages.addActionListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        m_brightness_slider.addChangeListener(listener);
        m_single_colour_checkbox.addChangeListener(listener);
        m_hide_holds_checkbox.addChangeListener(listener);
    }

    public boolean highlightSingleColour() {
        return m_single_colour_checkbox.isSelected();
    }

    public boolean hideHoldsWhileHighlighting() {
        return m_hide_holds_checkbox.isSelected();
    }

    public Map<Hold.Type, Boolean> getHoldTypeHighlightingMap() {
        return m_hold_type_percentages.getHighlightMap();
    }

    public Map<Direction, Boolean> getHoldDirectionHighlightingMap() {
        return m_hold_direction_percentages.getHighlightMap();
    }

    public int getBrightness() {
        return m_brightness_slider.getValue();
    }

    private boolean ignoreFeet() {
        return m_ignore_feet_checkbox.isSelected();
    }

    public void updateLabels(Board b) {
        m_num_holds.setText("Number of holds: " + b.getHolds().size());
        int[] num_holds_type_count = {0, 0, 0, 0, 0, 0};
        int[] num_hold_dir_count = {0, 0, 0, 0, 0, 0};
        for (Hold h : b.getHolds()) {
            Set<Type> types = h.getTypes();
            if (ignoreFeet() && types.contains(Type.FOOT)) {
                continue;
            }
            for (Type t : types) {
                num_holds_type_count[t.ordinal()] += 1;
            }
            Hold.Direction angle = Hold.Direction.classifyAngle(h.direction());
            num_hold_dir_count[angle.ordinal()] += 1;
        }
        m_hold_type_percentages.updateBarPercentages(num_holds_type_count);
        m_hold_direction_percentages.updateBarPercentages(num_hold_dir_count);
    }
}
