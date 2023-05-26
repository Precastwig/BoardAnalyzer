package BoardAnalyzer;

import java.awt.GridLayout;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class HoldSelectionSettings extends JPanel {
	private JCheckBox m_jug_checkbox;
	private JCheckBox m_crimp_checkbox;
	private JCheckBox m_sloper_checkbox;
	private JCheckBox m_pocket_checkbox;
	private JCheckBox m_pinch_checkbox;
	private JCheckBox m_foot_checkbox;
	private JButton m_save_hold_button ;
	private JButton m_delete_hold_button;
	private JLabel m_direction_label;
	private double m_direction_rad;
	private JLabel m_size_label;
	private double m_size;
	private Point2D.Double m_new_pos;
	public HoldSelectionSettings(JButton save_hold_button, JButton delete_hold_button) {
		super(new GridLayout(9, 1));
		m_jug_checkbox = new JCheckBox("Jug");
		m_crimp_checkbox = new JCheckBox("Crimp");
		m_sloper_checkbox = new JCheckBox("Sloper");
		m_pocket_checkbox = new JCheckBox("Pocket");
		m_pinch_checkbox = new JCheckBox("Pinch");
		m_foot_checkbox = new JCheckBox("Foot");
		add(m_jug_checkbox);
		add(m_crimp_checkbox);
		add(m_sloper_checkbox);
		add(m_pocket_checkbox);
		add(m_pinch_checkbox);
		add(m_foot_checkbox);
		
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
		return m_crimp_checkbox.isSelected();
	}
	
	public boolean isJug() {
		return m_jug_checkbox.isSelected();
	}
	
	public boolean isSloper() {
		return m_sloper_checkbox.isSelected();
	}
	
	public boolean isPocket() {
		return m_pocket_checkbox.isSelected();
	}
	
	public boolean isPinch() {
		return m_pinch_checkbox.isSelected();
	}
	
	public boolean isFoot() {
		return m_foot_checkbox.isSelected();
	}
	
	public double getDirection() {
		return m_direction_rad;
	}
	
	public double getHoldSize() {
		return m_size;
	}
	
	public Point2D.Double getHoldPosition() {
		return m_new_pos;
	}
	
	public void setCrimp(boolean b) {
		m_crimp_checkbox.setSelected(b);
	}
	
	public void setJug(boolean b) {
		m_jug_checkbox.setSelected(b);
	}
	
	public void setSloper(boolean b) {
		m_sloper_checkbox.setSelected(b);
	}
	
	public void setPocket(boolean b) {
		m_pocket_checkbox.setSelected(b);
	}
	
	public void setPinch(boolean b) {
		m_pinch_checkbox.setSelected(b);
	}
	
	public void setFoot(boolean b) {
		m_foot_checkbox.setSelected(b);
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
		
		m_new_pos = new Point2D.Double(circle_centre_x - m_size/2.0, circle_centre_y - m_size/2.0);
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_size_label.setText("Size: " + formatted_num.format(m_size));
	}
	
	public void disableAll() {
		m_jug_checkbox.setEnabled(false);
		m_crimp_checkbox.setEnabled(false);
		m_sloper_checkbox.setEnabled(false);
		m_pocket_checkbox.setEnabled(false);
		m_pinch_checkbox.setEnabled(false);
		m_foot_checkbox.setEnabled(false);
		m_save_hold_button.setEnabled(false);
		m_delete_hold_button.setEnabled(false);
		m_direction_label.setText("Direction: --°");
	}
	
	public void enableAll() {
		m_jug_checkbox.setEnabled(true);
		m_crimp_checkbox.setEnabled(true);
		m_sloper_checkbox.setEnabled(true);
		m_pocket_checkbox.setEnabled(true);
		m_pinch_checkbox.setEnabled(true);
		m_foot_checkbox.setEnabled(true);
		m_delete_hold_button.setEnabled(true);
		m_save_hold_button.setEnabled(true);
	}
}
