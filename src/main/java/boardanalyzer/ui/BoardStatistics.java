package boardanalyzer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import boardanalyzer.ui.basic_elements.BarChart;
import boardanalyzer.board_logic.Board;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.board_logic.Hold.Type;

public class BoardStatistics extends JPanel {
	
	private JLabel m_num_holds;
	private ArrayList<JLabel> m_num_holds_types;
	private ArrayList<JProgressBar> m_num_holds_types_pbs;
	
	public BarChart m_hold_type_chart;
	
	public BoardStatistics(JButton show_hold_stats) {
		setLayout(new BorderLayout());
		JPanel inner_layout = new JPanel();
		inner_layout.setLayout(new BoxLayout(inner_layout, BoxLayout.PAGE_AXIS));
		m_num_holds = new JLabel();
		m_hold_type_chart = new BarChart();
		m_num_holds_types = new ArrayList<JLabel>(); 
		m_num_holds_types_pbs = new ArrayList<JProgressBar>();
		inner_layout.add(m_num_holds);
		for (Type hold : Type.values()) {
			JLabel l = new JLabel(hold.toString() + "'s : ");
			JProgressBar pb = new JProgressBar(0, 100);
			pb.setStringPainted(true);
			m_num_holds_types_pbs.add(pb);
			m_num_holds_types.add(l);
			inner_layout.add(l);
			inner_layout.add(pb);
		}
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.NORTH);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.SOUTH);
		add(inner_layout, BorderLayout.CENTER);
		add(show_hold_stats, BorderLayout.PAGE_END);
	}
	
	public void updateLabels(Board b) {
		m_num_holds.setText("Number of holds: " + b.getHolds().size());
		int num_holds_count[] = {0,0,0,0,0,0};
		for (Hold h : b.getHolds()) {
			HashSet<Type> types = h.getTypes();
			for (Type t : types) {
				num_holds_count[t.ordinal()] += 1;
			}
		}
		for (Type hold : Type.values()) {
			//System.out.println(num_holds_count[hold.ordinal()]);
			double percentage = (((double)num_holds_count[hold.ordinal()]) / (double)b.getHolds().size()) * 100.0;
			//System.out.println(percentage);
			//DecimalFormat formatted_num = new DecimalFormat("#.##");
			m_num_holds_types.get(hold.ordinal()).setText(hold.toString() + "'s : ");;
			m_num_holds_types_pbs.get(hold.ordinal()).setValue((int)percentage);
		}
	}
}