package boardanalyzer.ui;

import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import boardanalyzer.board_logic.Hold.Type;

public class HeatmapSettings extends JPanel {
	JSlider m_brightness_slider;
	ArrayList<JCheckBox> m_hold_type_selection;
	JCheckBox m_hold_type_exact_match;
	JCheckBox m_hold_direction_checkbox;
	
	public HeatmapSettings() {
		JLabel explanationLabel = new JLabel("<html>This will create a heatmap from the various settings below, if no hold types are selected, all will be used.</html>");
		add(explanationLabel);
		
		m_brightness_slider = new JSlider(JSlider.HORIZONTAL, 10, 200, 100);

		m_brightness_slider.setMajorTickSpacing(50);
		m_brightness_slider.setMinorTickSpacing(25);
		m_brightness_slider.setPaintTicks(true);
		m_brightness_slider.setPaintLabels(true);
        
        add(m_brightness_slider);
        
        m_hold_type_selection = new ArrayList<JCheckBox>();
		for (Type hold : Type.values()) {
			JCheckBox cb = new JCheckBox(hold.toString());
			m_hold_type_selection.add(cb);
			add(cb);
		}
		
		m_hold_type_exact_match = new JCheckBox("Hold types exactly match");
		add(m_hold_type_exact_match);
		
		m_hold_direction_checkbox = new JCheckBox("Hold direction matters");
		add(m_hold_direction_checkbox);
	}
	
	public int getBrightness() {
		return m_brightness_slider.getValue();
	}
	
	public boolean atLeastOneHoldTypeSelected() {
		for (Type hold : Type.values()) {
			if (m_hold_type_selection.get(hold.ordinal()).isSelected()) {
				return true;
			}
		}
		return false;
	}
	
	public HashSet<Type> getSelectedHoldTypes() {
		HashSet<Type> types = new HashSet<Type>();
		for (Type hold : Type.values()) {
			if (m_hold_type_selection.get(hold.ordinal()).isSelected()) {
				types.add(hold);
			}
		}
		return types;
	}
	
	public boolean holdTypesShouldExactlyMatch() {
		return m_hold_type_exact_match.isSelected();
	}
	
	public boolean holdDirectionMatters() {
		return m_hold_direction_checkbox.isSelected();
	}
	
	public boolean isCrimpSelected() {
		return m_hold_type_selection.get(Type.CRIMP.ordinal()).isSelected();
	}
	
	public boolean isJugSelected() {
		return m_hold_type_selection.get(Type.JUG.ordinal()).isSelected();
	}
	
	public boolean isSloperSelected() {
		return m_hold_type_selection.get(Type.SLOPER.ordinal()).isSelected();
	}
	
	public boolean isPocketSelected() {
		return m_hold_type_selection.get(Type.POCKET.ordinal()).isSelected();
	}
	
	public boolean isPinchSelected() {
		return m_hold_type_selection.get(Type.PINCH.ordinal()).isSelected();
	}
	
	public boolean isFootSelected() {
		return m_hold_type_selection.get(Type.FOOT.ordinal()).isSelected();
	}
}
