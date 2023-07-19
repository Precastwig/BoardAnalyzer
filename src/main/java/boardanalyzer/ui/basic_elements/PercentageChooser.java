package boardanalyzer.ui.basic_elements;

import boardanalyzer.ui.basic_elements.BarWithButtons;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JPanel;

public class PercentageChooser<Type> extends JPanel implements ActionListener {
	private final ArrayList<BarWithButtons> m_bars;
	private final Type[] m_all_types;
	public PercentageChooser(Type[] values) {
		setLayout(new GridBagLayout());
		GridBagConstraints constraint = new GridBagConstraints();
		m_bars = new ArrayList<>();
		m_all_types = values;
		constraint.gridy = 0;
		constraint.fill = GridBagConstraints.HORIZONTAL;
		for (Type type : m_all_types) {
			BarWithButtons bwb = new BarWithButtons(type.toString());
			bwb.m_plus_button.addActionListener(this);
			bwb.m_minus_button.addActionListener(this);
			m_bars.add(bwb);

			constraint.gridwidth = 1;
			constraint.gridx = 2;
			constraint.weightx = 0.5;
			constraint.anchor = GridBagConstraints.CENTER;
			add(bwb.m_label, constraint);

			constraint.gridwidth = 1;
			constraint.gridy++;
			constraint.gridx = 0;
			constraint.weightx = 0.1;
			constraint.anchor = GridBagConstraints.PAGE_START;
			add(bwb.m_minus_button, constraint);

			constraint.weightx = 1.0;
			constraint.gridx = 1;
			constraint.gridwidth = 3;
			constraint.anchor = GridBagConstraints.CENTER;
			add(bwb.m_bar, constraint);

			constraint.gridx = 4;
			constraint.gridwidth = 1;
			constraint.weightx = 0.1;
			constraint.anchor = GridBagConstraints.PAGE_END;
			add(bwb.m_plus_button, constraint);

			constraint.gridy++;
		}
		//setPreferredSize(new Dimension(0, m_all_types.length * (BarWithButtons.BUTTON_SIZE + 5)));
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

	public void setRatio(int[] ratio) {
		for (int i = 0; i < m_all_types.length; i++) {
			m_bars.get(i).m_value = ratio[i];
		}
		updateBarPercentages();
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
		if (Objects.equals(e.getActionCommand(), "Plus")) {
			for (int i = 0; i < m_all_types.length; i++) {
				BarWithButtons bar = m_bars.get(i);
				if (bar.m_plus_button == e.getSource()) {
					bar.increaseValue();
				}
			}
		} else if (Objects.equals(e.getActionCommand(), "Minus")) {
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
