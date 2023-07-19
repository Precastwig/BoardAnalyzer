package boardanalyzer.ui.side_panel_tabs;

import boardanalyzer.BoardPanel;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.basic_elements.BorderedPanel;
import boardanalyzer.utils.Vector2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.*;

public class HoldSelectionSettings extends BorderedPanel implements ActionListener {
	ArrayList<JCheckBox> m_hold_type_checkboxes;
	private final JButton m_save_hold_button ;
	private final JButton m_delete_hold_button;
	private final JButton m_suggest_type_button;
	private final JButton m_suggest_direction_button;
	private final JLabel m_direction_label;
	private final JLabel m_size_label;
	private Hold m_new_hold;
	public HoldSelectionSettings() {
		setToBoxLayout();

		m_hold_type_checkboxes = new ArrayList<>();
		m_new_hold = new Hold();
		m_direction_label = new JLabel("Direction: --");
		m_size_label = new JLabel("Size: --");
		m_save_hold_button = new JButton("Save hold");
		m_delete_hold_button = new JButton("Delete hold");
		m_suggest_type_button = new JButton("Suggest type");
		m_suggest_direction_button = new JButton("Suggest direction");

		m_suggest_direction_button.setActionCommand("SuggestHoldDirection");
		m_suggest_type_button.setActionCommand("SuggestHoldType");
		m_delete_hold_button.setActionCommand("DeleteHold");
		m_save_hold_button.setActionCommand("SaveHold");

		m_direction_label.setAlignmentX(0.5f);
		m_size_label.setAlignmentX(0.5f);
		m_save_hold_button.setAlignmentX(0.5f);
		m_delete_hold_button.setAlignmentX(0.5f);
		m_suggest_type_button.setAlignmentX(0.5f);
		m_suggest_direction_button.setAlignmentX(0.5f);

		add(Box.createVerticalGlue());
		for (Hold.Type hold : Hold.Type.values()) {
			Box b = Box.createHorizontalBox();
			JCheckBox cb = new JCheckBox(hold.toString());
			cb.addActionListener(this);
			m_hold_type_checkboxes.add(cb);
			b.add(cb);
			b.add(Box.createHorizontalGlue());
			add(b);
		}

		add(m_direction_label);
		add(m_size_label);
		add(Box.createVerticalStrut(20));
		add(m_save_hold_button);
		add(m_delete_hold_button);
		add(Box.createVerticalStrut(20));
		add(m_suggest_type_button);
		add(m_suggest_direction_button);

		disableAll();
	}

	public void addActionListener(ActionListener listener) {
		m_save_hold_button.addActionListener(listener);
		m_delete_hold_button.addActionListener(listener);
		m_suggest_type_button.addActionListener(listener);
		m_suggest_direction_button.addActionListener(listener);
		for (JCheckBox cb : m_hold_type_checkboxes) {
			cb.addActionListener(listener);
		}
	}

	public void selectHold(Hold h) {
		m_new_hold = new Hold(h);
		updateTypeCheckboxes();
		updateHoldSizeLabel();
		updateDirectionLabel();
		enableAll();
	}
	
	public boolean isCrimp() {
		return m_new_hold.isCrimp();
	}
	
	public boolean isJug() {
		return m_new_hold.isJug();
	}
	
	public boolean isSloper() {
		return m_new_hold.isSloper();
	}
	
	public boolean isPocket() {
		return m_new_hold.isPocket();
	}
	
	public boolean isPinch() {
		return m_new_hold.isPinch();
	}
	
	public boolean isFoot() {
		return m_new_hold.isFoot();
	}
	
	public double getDirection() {
		return m_new_hold.direction();
	}
	
	public Vector2 getHoldSize() {
		return m_new_hold.size();
	}
	
	public Vector2 getHoldPosition() {
		return m_new_hold.position();
	}

	public Vector2 getHoldCentrePoint() {
		return m_new_hold.getCentrePoint();
	}

	public Hold getNewHold() {return m_new_hold;};
	
	public void setToHoldType(Hold.Type type) {
		m_new_hold.clearTypes();
		m_new_hold.addType(type);
		updateTypeCheckboxes();
	}

	public void updateTypeCheckboxes() {
		HashSet<Hold.Type> new_hold_types = m_new_hold.getTypes();
		for (Hold.Type type : Hold.Type.values()) {
			m_hold_type_checkboxes.get(type.ordinal()).setSelected(new_hold_types.contains(type));
		}
	}

	public void updateDirectionLabel() {
		double adjusted_degs = Math.toDegrees(m_new_hold.direction() - (3 * Math.PI)/2 + (4 * Math.PI)) % 360;
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_direction_label.setText(
				"Direction: " +
						formatted_num.format(adjusted_degs) + "\u00B0" // degree symbol
						+ "  : " + m_new_hold.directionClassification());
	}
	
	public void setDirection(double rad) {
		m_new_hold.setDirection(rad);
		updateDirectionLabel();
	}

	public void updateHoldSizeLabel() {
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_size_label.setText("Size: (" +
				formatted_num.format(m_new_hold.size().x) + ", " +
				formatted_num.format(m_new_hold.size().y) + ")");
	}
	
	public void setHoldSize(
			Vector2 size) {
		m_new_hold.setSize(size);
		updateHoldSizeLabel();
	}

	public void setPosition(Vector2 pos) {
		m_new_hold.setPosition(pos);
	}
	
	public void disableAll() {
		for (JCheckBox cb : m_hold_type_checkboxes) {
			cb.setEnabled(false);
		}
		m_save_hold_button.setEnabled(false);
		m_delete_hold_button.setEnabled(false);
		m_suggest_type_button.setEnabled(false);
		m_suggest_direction_button.setEnabled(false);
		m_direction_label.setText("Direction: --\u00B0");
		m_size_label.setText("Size: --");
	}
	
	public void enableAll() {
		for (JCheckBox cb : m_hold_type_checkboxes) {
			cb.setEnabled(true);
		}
		m_delete_hold_button.setEnabled(true);
		m_save_hold_button.setEnabled(true);
		m_suggest_type_button.setEnabled(true);
		m_suggest_direction_button.setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		m_new_hold.clearTypes();
		for (Hold.Type type : Hold.Type.values()) {
			if (m_hold_type_checkboxes.get(type.ordinal()).isSelected()) {
				m_new_hold.addType(type);
			}
		}
	}
}
