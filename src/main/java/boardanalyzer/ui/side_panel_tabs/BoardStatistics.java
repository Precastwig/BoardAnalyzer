package boardanalyzer.ui.side_panel_tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.*;

import boardanalyzer.ui.basic_elements.BarChart;
import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.board_logic.Hold.Type;
import boardanalyzer.ui.basic_elements.BorderedPanel;
import boardanalyzer.ui.basic_elements.PercentageVisualiser;

public class BoardStatistics extends BorderedPanel {
	private final JButton m_show_hold_stats_button;
	private final JLabel m_num_holds;
	private final JCheckBox m_ignore_feet_checkbox;
	private final PercentageVisualiser<Hold.Type> m_hold_type_percentages;
	private final PercentageVisualiser<Hold.Direction> m_hold_direction_percentages;

	public BarChart m_hold_type_chart;
	
	public BoardStatistics() {
		setToBoxLayout();
		m_show_hold_stats_button = new JButton("Show detailed hold statistics");
		m_num_holds = new JLabel();
		m_hold_type_chart = new BarChart();
		m_hold_type_percentages = new PercentageVisualiser<Hold.Type>(Hold.Type.values());
		m_hold_direction_percentages = new PercentageVisualiser<Hold.Direction>(Hold.Direction.values());
		m_ignore_feet_checkbox = new JCheckBox("Ignore feet");

		m_show_hold_stats_button.setActionCommand("ShowHoldStats");
		m_ignore_feet_checkbox.setActionCommand("StatisticsIgnoreFeet");

		m_ignore_feet_checkbox.setAlignmentX(0.5f);
		m_hold_direction_percentages.setAlignmentX(0.5f);
		m_hold_type_percentages.setAlignmentX(0.5f);
		m_num_holds.setAlignmentX(0.5f);

		add(m_ignore_feet_checkbox);
		add(m_num_holds);
		add(m_hold_type_percentages);
		add(Box.createVerticalGlue());
		add(m_hold_direction_percentages);
		add(m_show_hold_stats_button, BorderLayout.PAGE_END);
	}

	public void addActionListener(ActionListener listener) {
		m_show_hold_stats_button.addActionListener(listener);
		m_ignore_feet_checkbox.addActionListener(listener);
	}

	private boolean ignoreFeet() {
		return m_ignore_feet_checkbox.isSelected();
	}

	public void updateLabels(Board b) {
		m_num_holds.setText("Number of holds: " + b.getHolds().size());
		int[] num_holds_type_count = {0,0,0,0,0,0};
		int[] num_hold_dir_count = {0,0,0,0,0,0};
		for (Hold h : b.getHolds()) {
			HashSet<Type> types = h.getTypes();
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
