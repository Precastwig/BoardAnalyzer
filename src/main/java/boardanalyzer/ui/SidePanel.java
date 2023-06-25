package boardanalyzer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SidePanel extends JPanel {
    public BoardStatistics m_board_stats;
    public HeatmapSettings m_heatmap_settings;
    public HoldSelectionSettings m_hold_selection_settings;
    public HoldGenerationSettings m_hold_generation_settings;
    public BoardSettings m_board_settings;
    private JTabbedPane m_tab_panel;

    public SidePanel(int width) {
        setLayout(new GridLayout(2, 1, 10, 10));
        setPreferredSize(new Dimension(width, 1200));

        m_board_stats = new BoardStatistics();
        m_heatmap_settings = new HeatmapSettings(width);
        m_hold_selection_settings = new HoldSelectionSettings();
        m_hold_generation_settings = new HoldGenerationSettings(width);
        m_board_settings = new BoardSettings(width);

        m_heatmap_settings.setPreferredSize(new Dimension(width, 600));

        m_tab_panel = new JTabbedPane();
        m_tab_panel.addTab("Board settings", m_board_settings);
        m_tab_panel.addTab("Heatmap", m_heatmap_settings);
        m_tab_panel.addTab("Hold Suggestion", m_hold_generation_settings);
        m_tab_panel.addTab("Board Statistics", m_board_stats);

        m_tab_panel.setPreferredSize(new Dimension(width, 600));

        add(m_tab_panel);
        add(m_hold_selection_settings);
    }

    public void addActionListener(ActionListener listener) {
        m_hold_selection_settings.addActionListener(listener);
        m_board_settings.addActionListener(listener);
        m_board_stats.addActionListener(listener);
        m_heatmap_settings.addActionListener(listener);
        m_hold_generation_settings.addActionListener(listener);
    }
}
