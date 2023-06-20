package boardanalyzer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import boardanalyzer.ui.basic_elements.BarChart;
import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.board_logic.Hold.Type;
import boardanalyzer.ui.basic_elements.PercentageVisualiser;

public class BoardStatistics extends JPanel {
	
	private JLabel m_num_holds;
	PercentageVisualiser<Hold.Type> m_hold_type_percentages;
	PercentageVisualiser<Hold.Direction> m_hold_direction_percentages;

	public BarChart m_hold_type_chart;
	
	public BoardStatistics(JButton show_hold_stats) {
		setLayout(new BorderLayout());
		JPanel inner_layout = new JPanel();
		inner_layout.setLayout(new BoxLayout(inner_layout, BoxLayout.PAGE_AXIS));
		m_num_holds = new JLabel();
		m_hold_type_chart = new BarChart();
		m_hold_type_percentages = new PercentageVisualiser<Hold.Type>(Hold.Type.values());
		m_hold_direction_percentages = new PercentageVisualiser<Hold.Direction>(Hold.Direction.values());
		inner_layout.add(m_num_holds);
		inner_layout.add(m_hold_type_percentages);
		inner_layout.add(Box.createVerticalGlue());
		inner_layout.add(m_hold_direction_percentages);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.NORTH);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.SOUTH);
		add(inner_layout, BorderLayout.CENTER);
		add(show_hold_stats, BorderLayout.PAGE_END);
	}
	
	public void updateLabels(Board b) {
		m_num_holds.setText("Number of holds: " + b.getHolds().size());
		int[] num_holds_type_count = {0,0,0,0,0,0};
		int[] num_hold_dir_count = {0,0,0,0,0,0};
		for (Hold h : b.getHolds()) {
			HashSet<Type> types = h.getTypes();
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
