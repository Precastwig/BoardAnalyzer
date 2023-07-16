package boardanalyzer.ui.basic_elements;

import javax.swing.*;
import java.awt.*;

public class BorderedPanel extends JPanel {
    private final JPanel m_inner_panel;
    public BorderedPanel() {
        setLayout(new BorderLayout());
        m_inner_panel = new JPanel();

        add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.WEST);
        add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.EAST);
        add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.NORTH);
        add(Box.createRigidArea(new Dimension(20,20)), BorderLayout.SOUTH);
        add(m_inner_panel, BorderLayout.CENTER);
    }

    public void setToBoxLayout() {
        m_inner_panel.setLayout(new BoxLayout(m_inner_panel, BoxLayout.PAGE_AXIS));
    }

    public void setInnerLayout(java.awt.LayoutManager mgr) {
        m_inner_panel.setLayout(mgr);
    }

    @Override
    public java.awt.Component add(java.awt.Component comp) {
        return m_inner_panel.add(comp);
    }
}
