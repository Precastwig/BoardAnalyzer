package boardanalyzer.ui;

import boardanalyzer.ui.side_panel_tabs.BoardSettings;
import boardanalyzer.ui.side_panel_tabs.BoardStatistics;
import boardanalyzer.ui.side_panel_tabs.HoldGenerationSettings;
import boardanalyzer.ui.side_panel_tabs.HoldSelectionSettings;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class SidePanel extends JPanel {
    public BoardStatistics m_board_stats;
    public HoldSelectionSettings m_hold_selection_settings;
    public HoldGenerationSettings m_hold_generation_settings;
    public BoardSettings m_board_settings;

    public SidePanel(int width) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(width, 10000));

        m_board_stats = new BoardStatistics();
        m_hold_selection_settings = new HoldSelectionSettings();
        m_hold_generation_settings = new HoldGenerationSettings(width);
        m_board_settings = new BoardSettings(width);

        JTabbedPane tab_panel = new JTabbedPane();
        tab_panel.addTab("Board settings", m_board_settings);
        tab_panel.addTab("Hold Suggestion", m_hold_generation_settings);
        tab_panel.addTab("Board Statistics", m_board_stats);

        tab_panel.setPreferredSize(new Dimension(width, 1000));

        add(tab_panel);
        add(Box.createVerticalGlue());
        add(m_hold_selection_settings);
    }

    public void addActionListener(ActionListener listener) {
        m_hold_selection_settings.addActionListener(listener);
        m_board_settings.addActionListener(listener);
        m_board_stats.addActionListener(listener);
        m_hold_generation_settings.addActionListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        m_board_stats.addChangeListener(listener);
    }
}
