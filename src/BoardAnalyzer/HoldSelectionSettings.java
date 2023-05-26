package BoardAnalyzer;

import java.awt.GridLayout;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import BoardAnalyzer.Hold.Types;

public class HoldSelectionSettings extends JPanel {
	ArrayList<JCheckBox> m_hold_type_checkboxes;
	private JButton m_save_hold_button ;
	private JButton m_delete_hold_button;
	private JLabel m_direction_label;
	private double m_direction_rad;
	private JLabel m_size_label;
	private double m_size;
	private Vector2 m_new_pos;
	public HoldSelectionSettings(JButton save_hold_button, JButton delete_hold_button) {
		super(new GridLayout(9, 1));
		m_hold_type_checkboxes = new ArrayList<JCheckBox>();
		for (Hold.Types hold : Hold.Types.values()) {
			JCheckBox cb = new JCheckBox(hold.toString());
			m_hold_type_checkboxes.add(cb);
			add(cb);
		}
		
		m_direction_label = new JLabel("Direction: --");
		add(m_direction_label);
		
		m_size_label = new JLabel("Size: --");
		add(m_size_label);
		
		m_save_hold_button = save_hold_button;
		add(m_save_hold_button);
		
		m_delete_hold_button = delete_hold_button;
		add(m_delete_hold_button);
		
		disableAll();
	}
	
	public boolean isCrimp() {
		return m_hold_type_checkboxes.get(Types.CRIMP.ordinal()).isSelected();
	}
	
	public boolean isJug() {
		return m_hold_type_checkboxes.get(Types.JUG.ordinal()).isSelected();
	}
	
	public boolean isSloper() {
		return m_hold_type_checkboxes.get(Types.SLOPER.ordinal()).isSelected();
	}
	
	public boolean isPocket() {
		return m_hold_type_checkboxes.get(Types.POCKET.ordinal()).isSelected();
	}
	
	public boolean isPinch() {
		return m_hold_type_checkboxes.get(Types.PINCH.ordinal()).isSelected();
	}
	
	public boolean isFoot() {
		return m_hold_type_checkboxes.get(Types.FOOT.ordinal()).isSelected();
	}
	
	public double getDirection() {
		return m_direction_rad;
	}
	
	public double getHoldSize() {
		return m_size;
	}
	
	public Vector2 getHoldPosition() {
		return m_new_pos;
	}
	
	public void setCrimp(boolean b) {
		m_hold_type_checkboxes.get(Types.CRIMP.ordinal()).setSelected(b);
	}
	
	public void setJug(boolean b) {
		m_hold_type_checkboxes.get(Types.JUG.ordinal()).setSelected(b);
	}
	
	public void setSloper(boolean b) {
		m_hold_type_checkboxes.get(Types.SLOPER.ordinal()).setSelected(b);
	}
	
	public void setPocket(boolean b) {
		m_hold_type_checkboxes.get(Types.POCKET.ordinal()).setSelected(b);
	}
	
	public void setPinch(boolean b) {
		m_hold_type_checkboxes.get(Types.PINCH.ordinal()).setSelected(b);
	}
	
	public void setFoot(boolean b) {
		m_hold_type_checkboxes.get(Types.FOOT.ordinal()).setSelected(b);
	}
	
	public void setDirection(double rad) {
		m_direction_rad = rad;
		double adjusted_degs = Math.toDegrees(rad - (3 * Math.PI)/2 + (4 * Math.PI)) % 360;
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_direction_label.setText("Direction: " + formatted_num.format(adjusted_degs) + "°");
	}
	
	public void setHoldSize(double size, int old_position_x, int old_position_y, double old_circle_size) {
		m_size = size;
		
		int circle_centre_x = (int)(old_position_x + old_circle_size/2.0);
		int circle_centre_y = (int)(old_position_y + old_circle_size/2.0);
		
		m_new_pos = new Vector2(circle_centre_x - m_size/2.0, circle_centre_y - m_size/2.0);
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_size_label.setText("Size: " + formatted_num.format(m_size));
	}
	
	public void disableAll() {
		for (Iterator<JCheckBox> it = m_hold_type_checkboxes.iterator(); it.hasNext();) {
			JCheckBox cb = it.next();
			cb.setEnabled(false);
		}
		m_save_hold_button.setEnabled(false);
		m_delete_hold_button.setEnabled(false);
		m_direction_label.setText("Direction: --°");
	}
	
	public void enableAll() {
		for (Iterator<JCheckBox> it = m_hold_type_checkboxes.iterator(); it.hasNext();) {
			JCheckBox cb = it.next();
			cb.setEnabled(true);
		}
		m_delete_hold_button.setEnabled(true);
		m_save_hold_button.setEnabled(true);
	}
}
