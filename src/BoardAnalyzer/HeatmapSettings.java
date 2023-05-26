package BoardAnalyzer;

import javax.swing.JPanel;
import javax.swing.JSlider;

public class HeatmapSettings extends JPanel {
	JSlider m_heatmap_brightness_slider;
	public HeatmapSettings() {
        m_heatmap_brightness_slider = new JSlider(JSlider.HORIZONTAL, 10, 500, 100);

        m_heatmap_brightness_slider.setMajorTickSpacing(50);
        m_heatmap_brightness_slider.setMinorTickSpacing(25);
        m_heatmap_brightness_slider.setPaintTicks(true);
        m_heatmap_brightness_slider.setPaintLabels(true);
        
        add(m_heatmap_brightness_slider);
	}
	
	public int getBrightness() {
		return m_heatmap_brightness_slider.getValue();
	}
}
