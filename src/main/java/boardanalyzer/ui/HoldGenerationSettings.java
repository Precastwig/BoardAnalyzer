package boardanalyzer;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class HoldGenerationSettings extends JPanel implements ItemListener {
	JCheckBox m_least_common_hold_checkbox;
	ArrayList<JCheckBox> m_hold_type_checkboxes;
	
	public HoldGenerationSettings() {
		setLayout(new BorderLayout());
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
		for (Iterator<JCheckBox> it = m_hold_type_checkboxes.iterator(); it.hasNext();) {
			JCheckBox cb = it.next();
			cb.setEnabled(false);
		}
	}
	
	public void enableHoldTypesCheckboxes() {
		for (Iterator<JCheckBox> it = m_hold_type_checkboxes.iterator(); it.hasNext();) {
			JCheckBox cb = it.next();
			cb.setEnabled(true);
		}
	}
}
