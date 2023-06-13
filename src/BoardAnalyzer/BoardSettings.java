package BoardAnalyzer;

import java.awt.BorderLayout;
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
	
	public BoardSettings(
			JButton open_button, 
			JButton save_settings_button, 
			JButton set_board_corners_button, 
			JButton clear_holds_button) {
		setLayout(new BorderLayout());
		JPanel inner_panel = new JPanel();
		inner_panel.setLayout(new BoxLayout(inner_panel, BoxLayout.PAGE_AXIS));
		inner_panel.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 1200));
        
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
        
        inner_panel.add(open_button);
        inner_panel.add(set_board_corners_button);
        inner_panel.add(clear_holds_button);
        inner_panel.add(Box.createVerticalGlue());
        inner_panel.add(width_input);
        inner_panel.add(height_input);
        inner_panel.add(save_settings_button);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.NORTH);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.SOUTH);
        add(inner_panel, BorderLayout.CENTER);
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
