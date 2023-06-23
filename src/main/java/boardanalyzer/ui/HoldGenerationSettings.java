package boardanalyzer.ui;

import boardanalyzer.board_logic.Hold;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.*;

public class HoldGenerationSettings extends JPanel implements ItemListener {
	JCheckBox m_least_common_hold_checkbox;
	ArrayList<JCheckBox> m_hold_type_checkboxes;
	JButton m_generate_hold_button;
	
	public HoldGenerationSettings(int width) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		m_generate_hold_button = new JButton("Generate Hold");
		JPanel inner_layout = new JPanel();
		inner_layout.setLayout(new BoxLayout(inner_layout, BoxLayout.PAGE_AXIS));
		m_least_common_hold_checkbox = new JCheckBox("Least Common Hold Type");
		add(m_least_common_hold_checkbox);
		m_hold_type_checkboxes = new ArrayList<JCheckBox>();
		for (Hold.Type hold : Hold.Type.values()) {
			JCheckBox cb = new JCheckBox(hold.toString());
			m_hold_type_checkboxes.add(cb);
			add(cb);
		}
		m_least_common_hold_checkbox.addItemListener(this);

		m_generate_hold_button.setActionCommand("GenerateHold");
		m_generate_hold_button.setPreferredSize(new Dimension(width, 20));
		add(m_generate_hold_button);
	}

	public void addActionListener(ActionListener listener) {
		m_generate_hold_button.addActionListener(listener);
	}
	
	public boolean generateLeastCommonHoldType() {
		return m_least_common_hold_checkbox.isEnabled();
	}
	
	public HashSet<Hold.Type> getHoldTypeToGenerate() {
		HashSet<Hold.Type> returnTypes = new HashSet<Hold.Type>();
		for (Hold.Type hold : Hold.Type.values()) {
			if (m_hold_type_checkboxes.get(hold.ordinal()).isEnabled()) {
				returnTypes.add(hold);
			}
		}
		return returnTypes;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (m_least_common_hold_checkbox.isEnabled()) {
			enableHoldTypesCheckboxes();
		} else {
			disableHoldTypesCheckboxes();
		}
	}
	
	public void disableHoldTypesCheckboxes() {
		for (JCheckBox cb : m_hold_type_checkboxes) {
			cb.setEnabled(false);
		}
	}
	
	public void enableHoldTypesCheckboxes() {
		for (JCheckBox cb : m_hold_type_checkboxes) {
			cb.setEnabled(true);
		}
	}
}
