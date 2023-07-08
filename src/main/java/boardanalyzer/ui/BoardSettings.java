package boardanalyzer.ui;

import boardanalyzer.MainWindow;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.basic_elements.PercentageChooser;
import boardanalyzer.utils.Vector2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Objects;

import javax.swing.*;

public class BoardSettings extends JPanel implements ActionListener {

	static class MinMaxSizePanel extends JPanel {
		private final TextField m_min_textfield;
		private final TextField m_max_textfield;
		public MinMaxSizePanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			JPanel min_input = new JPanel();
			min_input.add(new JLabel("Min"));
			m_min_textfield = new TextField();
			m_min_textfield.setText("0");
			min_input.add(m_min_textfield);
			add(min_input);

			JPanel max_input = new JPanel();
			max_input.add(new JLabel("Max"));
			m_max_textfield = new TextField();
			m_max_textfield.setText("0");
			max_input.add(m_max_textfield);
			add(max_input);
		}
		public int getHoldSizeMin() throws NumberFormatException {
			int ret = Integer.parseInt(m_min_textfield.getText());
			if (ret < 0) {
				throw new NumberFormatException();
			} else if (ret == 0) {
				return 10;
			}
			return ret;
		}

		public int getHoldSizeMax() throws NumberFormatException {
			int ret = Integer.parseInt(m_max_textfield.getText());
			if (ret < 0) {
				throw new NumberFormatException();
			} else if (ret == 0) {
				ret = 100;
			}
			int min = getHoldSizeMin();
			if (ret < min) {
				System.out.println("Max is smaller than min!");
				return min + 100;
			}
			return ret;
		}
		public void setHoldSizeMin(int min) {
			m_min_textfield.setText(String.valueOf(min));
		}

		public void setHoldSizeMax(int max) {
			m_max_textfield.setText(String.valueOf(max));
		}

	}
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
	private final MinMaxSizePanel m_hold_size;

	public BoardSettings(
			int preferred_width) {
		setLayout(new BorderLayout());
		JPanel inner_panel = new JPanel();
		inner_panel.setLayout(new BoxLayout(inner_panel, BoxLayout.PAGE_AXIS));

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
        m_hold_size = new MinMaxSizePanel();

		m_adjust_holds_with_corners_checkbox.setVisible(false);

        JTabbedPane tabbed_panel = new JTabbedPane();
        tabbed_panel.add("Type", m_hold_type_bars);
        tabbed_panel.add("Direction", m_hold_direction_bars);
        tabbed_panel.add("Size", m_hold_size);

        JLabel hold_pref_label = new JLabel("Hold preferences");
        hold_pref_label.setAlignmentX(0.5f);

		inner_panel.add(m_new_board_button);
		inner_panel.add(m_open_board_button);
		inner_panel.add(Box.createRigidArea(new Dimension(0, 20)));
		inner_panel.add(m_update_board_image_button);
		inner_panel.add(m_adjust_holds_with_corners_checkbox);
        inner_panel.add(m_move_board_corners_button);
		inner_panel.add(m_set_lowest_hold_button);
        inner_panel.add(m_clear_holds_button);
//        inner_panel.add(Box.createRigidArea(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 40)));
        inner_panel.add(m_board_size_input);
        inner_panel.add(hold_pref_label);
        inner_panel.add(tabbed_panel);
        inner_panel.add(Box.createVerticalGlue());
        inner_panel.add(m_save_settings_button);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.NORTH);
		add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.SOUTH);
        add(inner_panel, BorderLayout.CENTER);
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

	public int getHoldSizeMin() throws NumberFormatException {
		return m_hold_size.getHoldSizeMin();
	}

	public void setHoldSizeMin(int min) {
		m_hold_size.setHoldSizeMin(min);
	}

	public void setHoldSizeMax(int max) {
		m_hold_size.setHoldSizeMax(max);
	}

	public int getHoldSizeMax() throws NumberFormatException {
		return m_hold_size.getHoldSizeMax();
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
