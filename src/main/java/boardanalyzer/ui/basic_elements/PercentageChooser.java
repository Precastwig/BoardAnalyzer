package boardanalyzer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

public class PercentageChooser<Type> extends JPanel implements ActionListener {
	private ArrayList<BarWithButtons> m_bars;
	private Type[] m_all_types;
	public PercentageChooser(Type[] values) {
		m_bars = new ArrayList<BarWithButtons>();
		m_all_types = values;
		for (Type type : m_all_types) {
			BarWithButtons bwb = new BarWithButtons(type.toString());
			bwb.setAlignmentX(0f);
			bwb.m_plus_button.addActionListener(this);
			bwb.m_minus_button.addActionListener(this);
			m_bars.add(bwb);
			add(bwb);
		}
		setPreferredSize(new Dimension(0, m_all_types.length * (BarWithButtons.BUTTON_SIZE + 5)));
		updateBarPercentages();
	}
	
	public int[] getRatio() {
		int[] ratio = new int[m_all_types.length];
		for (int i = 0; i < m_all_types.length; i++) { 
			BarWithButtons bar = m_bars.get(i);
			ratio[i] = bar.m_value;
		}
		return ratio;
	}
	
	private void updateBarPercentages() {
		int total = 0;
		for (int i = 0; i < m_all_types.length; i++) {
			BarWithButtons bar = m_bars.get(i);
			total = total + bar.m_value;
		}
		for (int i = 0; i < m_all_types.length; i++) {
			BarWithButtons bar = m_bars.get(i);
			bar.m_bar.setValue((int)(((double)bar.m_value / (double)total) * 100.0));
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Plus") {
			for (int i = 0; i < m_all_types.length; i++) {
				BarWithButtons bar = m_bars.get(i);
				if (bar.m_plus_button == e.getSource()) {
					bar.increaseValue();
				}
			}
		} else if (e.getActionCommand() == "Minus") {
			for (int i = 0; i < m_all_types.length; i++) {
				BarWithButtons bar = m_bars.get(i);
				if (bar.m_minus_button == e.getSource()) {
					bar.decreaseValue();
				}
			}
		}
		updateBarPercentages();
	}
}
