package BoardAnalyzer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextField;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class BoardSettings extends JPanel {
//	class HoldTypePreferenceSelectors extends JPanel {
//		private ArrayList<BarWithButtons> m_bars;
//		
//		public HoldTypePreferenceSelectors() {
//			m_bars = new ArrayList<BarWithButtons>();
//			for (Hold.Types type : Hold.Types.values()) {
//				BarWithButtons bwb = new BarWithButtons(type.toString());
//				m_bars.add(bwb);
//				add(bwb);
//			}
//		}
//	}
	
	
	private TextField m_width_textfield;
	private TextField m_height_textfield;
	private PercentageChooser<Hold.Types> m_hold_type_bars;
	private PercentageChooser<Analyzer.AngleProportions.BucketLabel> m_hold_direction_bars;
	
	public BoardSettings(
			JButton new_board_button, 
			JButton save_settings_button, 
			JButton set_board_corners_button, 
			JButton clear_holds_button,
			JButton open_board_button) {
		setLayout(new BorderLayout());
		JPanel inner_panel = new JPanel();
		inner_panel.setLayout(new BoxLayout(inner_panel, BoxLayout.PAGE_AXIS));
		inner_panel.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 1200));
        
//        open_button.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
//        open_button.setMinimumSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
        
        m_width_textfield = new TextField();
        m_height_textfield = new TextField();
        		
        new_board_button.setAlignmentX(0.5f);
        open_board_button.setAlignmentX(0.5f);
        save_settings_button.setAlignmentX(0.5f);
        set_board_corners_button.setAlignmentX(0.5f);
        clear_holds_button.setAlignmentX(0.5f);
        
		JPanel width_input = new JPanel();
        JLabel width_label = new JLabel("Width");
        m_width_textfield.setText("0");
        m_width_textfield.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH / 2, 20));
        width_input.add(width_label);
        width_input.add(m_width_textfield);
        width_input.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
        width_input.setAlignmentX(0.5f);
        
        JPanel height_input = new JPanel();
        JLabel height_label = new JLabel("Height");
        m_height_textfield.setText("0");
        m_height_textfield.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH / 2, 20));
        height_input.add(height_label);
        height_input.add(m_height_textfield);
        height_input.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH / 2, 20));
        height_input.setAlignmentX(0.5f);
        m_hold_type_bars = new PercentageChooser<Hold.Types>(Hold.Types.getHandTypes());
        m_hold_direction_bars = new PercentageChooser<Analyzer.AngleProportions.BucketLabel>(Analyzer.AngleProportions.BucketLabel.values());
        JTabbedPane tabbed_panel = new JTabbedPane();
        tabbed_panel.add("Type", m_hold_type_bars);
        tabbed_panel.add("Direction", m_hold_direction_bars);
        
        JLabel hold_pref_label = new JLabel("Hold preferences");
        hold_pref_label.setAlignmentX(0.5f);
        
        inner_panel.add(new_board_button);
        inner_panel.add(open_board_button);
        inner_panel.add(Box.createRigidArea(new Dimension(0, 20)));
        inner_panel.add(set_board_corners_button);
        inner_panel.add(clear_holds_button);
//        inner_panel.add(Box.createRigidArea(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 40)));
        inner_panel.add(width_input);
        inner_panel.add(height_input);
        inner_panel.add(hold_pref_label);
        inner_panel.add(tabbed_panel);
        inner_panel.add(Box.createVerticalGlue());
        inner_panel.add(save_settings_button);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.NORTH);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.SOUTH);
        add(inner_panel, BorderLayout.CENTER);
	}
	
	public int[] getHoldTypeRatio() {
		return m_hold_type_bars.getRatio();
	}
	
	public int[] getHoldDirectionRatio() {
		return new int[] {
			1,1,1,1,1,1
		};
	}
	
	public Vector2 getBoardDimensions() throws NumberFormatException {
		return new Vector2(getBoardWidth(), getBoardHeight());
	}
	
	public double getBoardWidth() throws NumberFormatException {
		return Double.parseDouble(m_width_textfield.getText());
	}
	
	public double getBoardHeight() throws NumberFormatException {
		return Double.parseDouble(m_height_textfield.getText());
	}
	
	public void setBoardDimensions(Vector2 d) {
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_width_textfield.setText(formatted_num.format(d.x));
		m_height_textfield.setText(formatted_num.format(d.y));
	}
}
