package boardanalyzer.ui.basic_elements;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class BarWithButtons extends JPanel{
	public JLabel m_label;
	public JProgressBar m_bar;
	public JButton m_plus_button;
	public JButton m_minus_button;
	public int m_value;
	static public int BUTTON_SIZE = 20;
	
	public BarWithButtons(String label) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		m_value = 10;
		m_label = new JLabel(label);
		m_bar = new JProgressBar(0, 100);
		m_bar.setStringPainted(true);
		m_bar.setPreferredSize(new Dimension(50, BUTTON_SIZE));
		m_plus_button = new JButton("+");
		m_plus_button.setMargin(new Insets(0,0,0,0));
//		m_plus_button.setFont(new Font(m_plus_button.getFont().getFontName(), Font.PLAIN, 8));
		m_plus_button.setSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
		m_plus_button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
		m_plus_button.setActionCommand("Plus");
		m_minus_button = new JButton("-");
		m_minus_button.setSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
		m_minus_button.setMargin(new Insets(0,0,0,0));
		m_minus_button.setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
		m_minus_button.setActionCommand("Minus");
		add(m_label);
		add(Box.createHorizontalGlue());
		add(m_minus_button);
		add(m_bar);
		add(m_plus_button);
	}
	
	public void increaseValue() {
		m_value++;
	}
	
	public void decreaseValue() {
		if (m_value > 0) {
			m_value--;
		}
	}
}
