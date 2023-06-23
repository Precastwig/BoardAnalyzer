package boardanalyzer.ui;

import boardanalyzer.MainWindow;
import boardanalyzer.board_logic.Hold;
import boardanalyzer.ui.basic_elements.PercentageChooser;
import boardanalyzer.utils.Vector2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class BoardSettings extends JPanel {
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
	private final JButton m_set_board_corners_button;
	private final JButton m_save_settings_button;
	private final TextField m_width_textfield;
	private final TextField m_height_textfield;
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
		m_set_lowest_hold_button = new JButton("Set lowest allowed hand-hold height");
		m_set_board_corners_button = new JButton("Set board corners");
		m_save_settings_button = new JButton("Save");

		// Set action commands
		m_new_board_button.setActionCommand("NewBoard");
		m_open_board_button.setActionCommand("OpenBoard");
		m_clear_holds_button.setActionCommand("ClearAllHolds");
		m_set_lowest_hold_button.setActionCommand("SetLowestHandHoldHeight");
		m_set_board_corners_button.setActionCommand("SetCorners");
		m_save_settings_button.setActionCommand("Save");

		// Set sizes
		m_clear_holds_button.setPreferredSize(new Dimension(preferred_width, 20));
		m_new_board_button.setPreferredSize(new Dimension(preferred_width, 20));
		m_set_lowest_hold_button.setPreferredSize(new Dimension(preferred_width, 20));
		m_open_board_button.setPreferredSize(new Dimension(preferred_width, 20));
		m_set_board_corners_button.setPreferredSize(new Dimension(preferred_width, 20));
		m_save_settings_button.setPreferredSize(new Dimension(preferred_width, 20));

		// Set alignment
		m_new_board_button.setAlignmentX(0.5f);
		m_open_board_button.setAlignmentX(0.5f);
		m_save_settings_button.setAlignmentX(0.5f);
		m_set_board_corners_button.setAlignmentX(0.5f);
		m_clear_holds_button.setAlignmentX(0.5f);
		m_set_lowest_hold_button.setAlignmentX(0.5f);

		m_width_textfield = new TextField();
		m_height_textfield = new TextField();

		JPanel width_input = new JPanel();
        JLabel width_label = new JLabel("Width");
        m_width_textfield.setText("0");
//        m_width_textfield.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH / 2, 20));
        width_input.add(width_label);
        width_input.add(m_width_textfield);
//        width_input.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
        width_input.setAlignmentX(0.5f);

        JPanel height_input = new JPanel();
        JLabel height_label = new JLabel("Height");
        m_height_textfield.setText("0");
//        m_height_textfield.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH / 2, 20));
        height_input.add(height_label);
        height_input.add(m_height_textfield);
//        height_input.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH / 2, 20));
        height_input.setAlignmentX(0.5f);
        m_hold_type_bars = new PercentageChooser<Hold.Type>(Hold.Type.getHandTypes());
        m_hold_direction_bars = new PercentageChooser<Hold.Direction>(Hold.Direction.values());
        m_hold_size = new MinMaxSizePanel();

        JTabbedPane tabbed_panel = new JTabbedPane();
        tabbed_panel.add("Type", m_hold_type_bars);
        tabbed_panel.add("Direction", m_hold_direction_bars);
        tabbed_panel.add("Size", m_hold_size);

        JLabel hold_pref_label = new JLabel("Hold preferences");
        hold_pref_label.setAlignmentX(0.5f);

		inner_panel.add(m_new_board_button);
		inner_panel.add(m_open_board_button);
		inner_panel.add(Box.createRigidArea(new Dimension(0, 20)));
        inner_panel.add(m_set_board_corners_button);
		inner_panel.add(m_set_lowest_hold_button);
        inner_panel.add(m_clear_holds_button);
//        inner_panel.add(Box.createRigidArea(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 40)));
        inner_panel.add(width_input);
        inner_panel.add(height_input);
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
		m_set_board_corners_button.addActionListener(listener);
		m_clear_holds_button.addActionListener(listener);
		m_set_lowest_hold_button.addActionListener(listener);
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
