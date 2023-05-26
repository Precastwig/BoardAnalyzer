package BoardAnalyzer;

import java.awt.Dimension;
import java.awt.TextField;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BoardSettings extends JPanel {
	private TextField m_width_textfield;
	private TextField m_height_textfield;
	
	public BoardSettings(JButton open_button, JButton save_settings_button, JButton set_board_corners_button) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 1200));
        
//        open_button.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
//        open_button.setMinimumSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
        
        m_width_textfield = new TextField();
        m_height_textfield = new TextField();
        		
        open_button.setAlignmentX(0.5f);
        save_settings_button.setAlignmentX(0.5f);
        set_board_corners_button.setAlignmentX(0.5f);
        
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
        
		add(open_button);
		add(set_board_corners_button);
		add(Box.createVerticalGlue());
        add(width_input);
        add(height_input);
        add(save_settings_button);
        
	}
	
	public double getBoardWidth() throws NumberFormatException {
		return Double.parseDouble(m_width_textfield.getText());
	}
	
	public double getBoardHeight() throws NumberFormatException {
		return Double.parseDouble(m_height_textfield.getText());
	}
	
	public void setBoardDimensions(double width, double height) {
		DecimalFormat formatted_num = new DecimalFormat("#.##");
		m_width_textfield.setText(formatted_num.format(width));
		m_height_textfield.setText(formatted_num.format(height));
	}
}
