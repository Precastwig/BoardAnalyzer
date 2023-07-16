package boardanalyzer.ui.side_panel_tabs;

import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.side_panel_tabs.elements.BoardSizeInputPanel;
import boardanalyzer.ui.basic_elements.BorderedPanel;
import boardanalyzer.ui.basic_elements.PercentageChooser;
import boardanalyzer.utils.Vector2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.*;

public class BoardSettings extends BorderedPanel implements ActionListener {
	private final JButton m_new_board_button;
	private final JButton m_open_board_button;
	private final JButton m_clear_holds_button;
	private final JButton m_set_lowest_hold_button;
	private final JCheckBox m_adjust_holds_with_corners_checkbox;
	private final JButton m_move_board_corners_button;
	private final JButton m_update_board_image_button;
	private boolean m_move_corners_text;
	private final JButton m_save_settings_button;
	private final BoardSizeInputPanel m_board_size_input;
	private final PercentageChooser<Hold.Type> m_hold_type_bars;
	private final PercentageChooser<Hold.Direction> m_hold_direction_bars;

	public BoardSettings(
			int preferred_width) {
		setToBoxLayout();

		// Make buttons
		m_new_board_button = new JButton("New board");
		m_open_board_button = new JButton("Open board");
		m_clear_holds_button = new JButton("Clear all holds");
		m_set_lowest_hold_button = new JButton("<html><centre>Set lowest allowed hand height</centre></html>");
		m_adjust_holds_with_corners_checkbox = new JCheckBox("Move holds with corners");
		m_move_board_corners_button = new JButton("Move board corners");
		m_update_board_image_button = new JButton("Update board image");
		m_move_corners_text = false;
		m_save_settings_button = new JButton("Save");

		// Set action commands
		m_new_board_button.setActionCommand("NewBoard");
		m_open_board_button.setActionCommand("OpenBoard");
		m_clear_holds_button.setActionCommand("ClearAllHolds");
		m_set_lowest_hold_button.setActionCommand("SetLowestHandHoldHeight");
		m_update_board_image_button.setActionCommand("UpdateBoardImage");
		m_move_board_corners_button.setActionCommand("MoveCorners");
		m_save_settings_button.setActionCommand("Save");

		// Set sizes
//		m_clear_holds_button.setPreferredSize(new Dimension(preferred_width, 20));
//		m_new_board_button.setPreferredSize(new Dimension(preferred_width, 20));
//		m_set_lowest_hold_button.setPreferredSize(new Dimension(preferred_width, 20));
//		m_open_board_button.setPreferredSize(new Dimension(preferred_width, 20));
//		m_set_board_corners_button.setPreferredSize(new Dimension(preferred_width, 20));
//		m_save_settings_button.setPreferredSize(new Dimension(preferred_width, 20));

		// Set alignment
		m_new_board_button.setAlignmentX(0.5f);
		m_open_board_button.setAlignmentX(0.5f);
		m_save_settings_button.setAlignmentX(0.5f);
		m_move_board_corners_button.setAlignmentX(0.5f);
		m_adjust_holds_with_corners_checkbox.setAlignmentX(0.5f);
		m_update_board_image_button.setAlignmentX(0.5f);
		m_move_board_corners_button.addActionListener(this);
		m_clear_holds_button.setAlignmentX(0.5f);
		m_set_lowest_hold_button.setAlignmentX(0.5f);

		m_board_size_input = new BoardSizeInputPanel();

        m_hold_type_bars = new PercentageChooser<Hold.Type>(Hold.Type.getHandTypes());
        m_hold_direction_bars = new PercentageChooser<Hold.Direction>(Hold.Direction.values());

		m_adjust_holds_with_corners_checkbox.setVisible(false);

        JTabbedPane tabbed_panel = new JTabbedPane();
        tabbed_panel.add("Type", m_hold_type_bars);
        tabbed_panel.add("Direction", m_hold_direction_bars);

        JLabel hold_pref_label = new JLabel("Hold preferences");
        hold_pref_label.setAlignmentX(0.5f);

		add(m_new_board_button);
		add(m_open_board_button);
		add(Box.createRigidArea(new Dimension(0, 20)));
		add(m_update_board_image_button);
		add(m_adjust_holds_with_corners_checkbox);
        add(m_move_board_corners_button);
		add(m_set_lowest_hold_button);
        add(m_clear_holds_button);
//        inner_panel.add(Box.createRigidArea(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 40)));
        add(m_board_size_input);
        add(hold_pref_label);
        add(tabbed_panel);
        add(Box.createVerticalGlue());
        add(m_save_settings_button);
	}

	public void addActionListener(ActionListener listener) {
		m_new_board_button.addActionListener(listener);
		m_open_board_button.addActionListener(listener);
		m_save_settings_button.addActionListener(listener);
		m_move_board_corners_button.addActionListener(listener);
		m_update_board_image_button.addActionListener(listener);
		m_clear_holds_button.addActionListener(listener);
		m_set_lowest_hold_button.addActionListener(listener);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (Objects.equals(e.getActionCommand(), "MoveCorners")) {
			setMovingCornersUI(!m_move_corners_text);
			repaint();
		}
	}

	public void setMovingCornersUI(boolean moving_corners) {
		if (moving_corners) {
			m_move_board_corners_button.setText("Save board corners");
			m_move_corners_text = true;
			m_board_size_input.setVisible(false);
			m_new_board_button.setVisible(false);
			m_open_board_button.setVisible(false);
			m_clear_holds_button.setVisible(false);
			m_set_lowest_hold_button.setVisible(false);
			m_update_board_image_button.setVisible(false);
			m_adjust_holds_with_corners_checkbox.setVisible(true);
		} else {
			m_move_board_corners_button.setText("Move board corners");
			m_move_corners_text = false;
			m_board_size_input.setVisible(true);
			m_new_board_button.setVisible(true);
			m_open_board_button.setVisible(true);
			m_clear_holds_button.setVisible(true);
			m_set_lowest_hold_button.setVisible(true);
			m_update_board_image_button.setVisible(true);
			m_adjust_holds_with_corners_checkbox.setVisible(false);
		}
	}
	
	public int[] getHoldTypeRatio() {
		return m_hold_type_bars.getRatio();
	}

	public void setHoldTypeRatio(int[] ratio) {
		m_hold_type_bars.setRatio(ratio);
	}

	public void setHoldDirectionRatio(int [] ratio) {
		m_hold_direction_bars.setRatio(ratio);
	}
	
	public int[] getHoldDirectionRatio() {
		return m_hold_direction_bars.getRatio();
	}
	
	public Vector2 getBoardDimensions() {
		return new Vector2(getBoardWidth(), getBoardHeight());
	}
	
	public double getBoardWidth() {
		return m_board_size_input.getBoardWidth();
	}
	
	public double getBoardHeight() {
		return m_board_size_input.getBoardHeight();
	}

	public boolean moveHoldsWithCorners() {
		return m_adjust_holds_with_corners_checkbox.isSelected();
	}
	public void setMoveHoldsWithCorners(boolean b) {
		m_adjust_holds_with_corners_checkbox.setSelected(b);
	}
	
	public void setBoardDimensions(Vector2 d) {
		m_board_size_input.setBoardWidth(d.x);
		m_board_size_input.setBoardHeight(d.y);
	}
}
