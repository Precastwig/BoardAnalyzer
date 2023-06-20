package boardanalyzer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class InstructionPanel extends JPanel  {
	public JLabel m_instruction_label;
	private JProgressBar m_progress_bar;
	public InstructionPanel() {
		setLayout(new BorderLayout());
		JPanel inner_layout = new JPanel();
		inner_layout.setLayout(new BoxLayout(inner_layout, BoxLayout.LINE_AXIS));
		m_instruction_label = new JLabel("Click new board to begin...");
		m_progress_bar = new JProgressBar(0, 100);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
        add(Box.createRigidArea(new Dimension(5,5)), BorderLayout.NORTH);
        add(Box.createRigidArea(new Dimension(5,5)), BorderLayout.SOUTH);
        inner_layout.add(m_instruction_label);
        inner_layout.add(Box.createHorizontalGlue());
        inner_layout.add(m_progress_bar);
        add(inner_layout, BorderLayout.CENTER);
		m_progress_bar.setVisible(false);
		m_progress_bar.setPreferredSize(new Dimension(500, 20));
	}
	
	public void updateProgressBarRange(int min, int max) {
		m_progress_bar.setMaximum(max);
		m_progress_bar.setMinimum(min);
	}
	
	public void showProgressBar() {
		m_progress_bar.setVisible(true);
		m_progress_bar.setValue(0);
		repaint();
	}
	
	public void updateProgressBar(int p) {
		m_progress_bar.setValue(p);
		repaint();
	}
	
	public void hideProgressBar() {
		m_progress_bar.setVisible(false);
		repaint();
	}
}
