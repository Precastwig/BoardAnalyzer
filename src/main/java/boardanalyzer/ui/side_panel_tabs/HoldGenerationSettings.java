package boardanalyzer.ui.side_panel_tabs;

import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.basic_elements.BorderedPanel;
import boardanalyzer.ui.side_panel_tabs.elements.MinMaxHoldSizePanel;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

public class HoldGenerationSettings extends BorderedPanel implements ItemListener {
	JCheckBox m_least_common_hold_checkbox;
	JComboBox<Hold.Type> m_hold_type_selection;
	JButton m_generate_hold_button;
	MinMaxHoldSizePanel m_hold_size_pref;
	
	public HoldGenerationSettings(int width) {
		setToBoxLayout();

		m_generate_hold_button = new JButton("Generate Hold");
		m_least_common_hold_checkbox = new JCheckBox("Least Common Hold Type");
		m_hold_type_selection = new JComboBox<Hold.Type>(Hold.Type.values());
		m_hold_type_selection.setMaximumSize(new Dimension(300,50));
		JLabel hold_size_label = new JLabel("Hold Size:");
		m_hold_size_pref = new MinMaxHoldSizePanel();

		m_generate_hold_button.setAlignmentX(0.5f);
		m_least_common_hold_checkbox.setAlignmentX(0.5f);
		m_hold_type_selection.setAlignmentX(0.5f);
		m_hold_size_pref.setAlignmentX(0.5f);
		hold_size_label.setAlignmentX(0.5f);

		m_generate_hold_button.setActionCommand("GenerateHold");

		m_least_common_hold_checkbox.addItemListener(this);

		add(m_least_common_hold_checkbox);
		add(m_hold_type_selection);
		add(Box.createVerticalStrut(25));
		add(hold_size_label);
		add(m_hold_size_pref);
		add(Box.createVerticalStrut(25));
		add(m_generate_hold_button);
		add(Box.createVerticalGlue());
	}

	public void addActionListener(ActionListener listener) {
		m_generate_hold_button.addActionListener(listener);
	}
	
	public boolean generateLeastCommonHoldType() {
		return m_least_common_hold_checkbox.isSelected();
	}
	
	public Hold.Type getHoldTypeToGenerate() {
		return (Hold.Type) m_hold_type_selection.getSelectedItem();
	}

	public int getHoldSizeMin() throws NumberFormatException {
		return m_hold_size_pref.getHoldSizeMin();
	}

	public void setHoldSizeMin(int min) {
		m_hold_size_pref.setHoldSizeMin(min);
	}

	public void setHoldSizeMax(int max) {
		m_hold_size_pref.setHoldSizeMax(max);
	}

	public int getHoldSizeMax() throws NumberFormatException {
		return m_hold_size_pref.getHoldSizeMax();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		m_hold_type_selection.setEnabled(!m_least_common_hold_checkbox.isSelected());
	}
}
