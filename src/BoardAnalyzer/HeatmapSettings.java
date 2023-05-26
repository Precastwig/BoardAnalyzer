package BoardAnalyzer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;

import BoardAnalyzer.Hold.Types;

public class HeatmapSettings extends JPanel {
	JSlider m_brightness_slider;
	ArrayList<JCheckBox> m_hold_type_selection;
	
	public HeatmapSettings() {
		m_brightness_slider = new JSlider(JSlider.HORIZONTAL, 10, 200, 100);

		m_brightness_slider.setMajorTickSpacing(50);
		m_brightness_slider.setMinorTickSpacing(25);
		m_brightness_slider.setPaintTicks(true);
		m_brightness_slider.setPaintLabels(true);
        
        add(m_brightness_slider);
        
        m_hold_type_selection = new ArrayList<JCheckBox>();
		for (Hold.Types hold : Hold.Types.values()) {
			JCheckBox cb = new JCheckBox(hold.toString());
			m_hold_type_selection.add(cb);
			add(cb);
		}
	}
	
	public int getBrightness() {
		return m_brightness_slider.getValue();
	}
	
	public boolean atLeastOneHoldTypeSelected() {
		for (Hold.Types hold : Hold.Types.values()) {
			if (m_hold_type_selection.get(hold.ordinal()).isSelected()) {
				return true;
			}
		}
		return false;
	}
	
	public HashSet<Hold.Types> getSelectedHoldTypes() {
		HashSet<Hold.Types> types = new HashSet<Hold.Types>();
		for (Hold.Types hold : Hold.Types.values()) {
			if (m_hold_type_selection.get(hold.ordinal()).isSelected()) {
				types.add(hold);
			}
		}
		return types;
	}
	
	public boolean isCrimpSelected() {
		return m_hold_type_selection.get(Types.CRIMP.ordinal()).isSelected();
	}
	
	public boolean isJugSelected() {
		return m_hold_type_selection.get(Types.JUG.ordinal()).isSelected();
	}
	
	public boolean isSloperSelected() {
		return m_hold_type_selection.get(Types.SLOPER.ordinal()).isSelected();
	}
	
	public boolean isPocketSelected() {
		return m_hold_type_selection.get(Types.POCKET.ordinal()).isSelected();
	}
	
	public boolean isPinchSelected() {
		return m_hold_type_selection.get(Types.PINCH.ordinal()).isSelected();
	}
	
	public boolean isFootSelected() {
		return m_hold_type_selection.get(Types.FOOT.ordinal()).isSelected();
	}
}
