package boardanalyzer.ui.basic_elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class PercentageVisualiser<Type extends Enum<Type>> extends JPanel implements ActionListener {
    private final List<JProgressBar> m_bars;
    private final List<JButton> m_highlight_buttons;
    private final List<Boolean> m_highlighting;
    private final Type[] m_all_types;
    private final Icon m_lightbulb_dim_icon;
    private final Icon m_lightbuilb_lit_icon;

    public PercentageVisualiser(Type[] values) {
        setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        m_bars = new ArrayList<>();
        m_highlighting = new ArrayList<>();
        m_highlight_buttons = new ArrayList<>();
        try {
            Image image_dim = ImageIO.read(ClassLoader.getSystemResource("images/lightbulb-dim.png"));
            Image resized_image_dim = image_dim.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
            m_lightbulb_dim_icon = new ImageIcon(resized_image_dim);

            Image image_lit = ImageIO.read(ClassLoader.getSystemResource("images/lightbulb-lit.png"));
            Image resized_image_lit = image_lit.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            m_lightbuilb_lit_icon = new ImageIcon(resized_image_lit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        m_all_types = values;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        for (Type type : m_all_types) {
            m_highlighting.add(false);
            constraint.gridwidth = 2;
            constraint.gridx = 2;
            constraint.weightx = 0.5;
            constraint.anchor = GridBagConstraints.CENTER;
            JLabel label = new JLabel(type.toString() + "'s");
            add(label, constraint);

            JButton highlight_button;
            if (m_lightbulb_dim_icon.getIconHeight() > 0) {
                highlight_button = new JButton(m_lightbulb_dim_icon);
            } else {
                // I have no idea if this works
                highlight_button = new JButton("\uD83D\uDCA1");
            }
            highlight_button.setMargin(new Insets(0, 0, 0, 0));
            highlight_button.setMaximumSize(new Dimension(100, 20));
            highlight_button.setActionCommand("Highlight");
            m_highlight_buttons.add(highlight_button);

            constraint.gridx = 4;
            constraint.gridwidth = 1;
            constraint.weightx = 0.1;
            constraint.gridheight = 2;
            constraint.weighty = 0.5;
            constraint.fill = GridBagConstraints.VERTICAL;
            constraint.anchor = GridBagConstraints.PAGE_END;
            add(highlight_button, constraint);

            constraint.gridy++;
            constraint.fill = GridBagConstraints.HORIZONTAL;
            constraint.gridheight = 1;
            constraint.gridwidth = 4;
            constraint.gridx = 0;
            constraint.weightx = 0.7;
            constraint.anchor = GridBagConstraints.PAGE_END;
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);
            m_bars.add(bar);
            add(bar, constraint);

            constraint.gridy++;
        }

        addActionListener(this);
    }

    public void addActionListener(ActionListener listener) {
        for (JButton button : m_highlight_buttons) {
            button.addActionListener(listener);
        }
    }

    public void updateBarPercentages(int[] vals) {
        int total = 0;
        for (int val : vals) {
            total = total + val;
        }
        for (int i = 0; i < m_all_types.length; i++) {
            m_bars.get(i).setValue((int) (((double) vals[i] / (double) total) * 100.0));
        }
    }

    public void removeHighlights() {
        m_highlighting.clear();
        for (Type t : m_all_types) {
            m_highlighting.add(false);
        }
    }

    public Map<Type, Boolean> getHighlightMap() {
        Map<Type, Boolean> map = new HashMap<>();
        for (Type t : m_all_types) {
            map.put(t, m_highlighting.get(t.ordinal()));
        }
        return map;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Objects.equals(e.getActionCommand(), "Highlight")) {
            for (int i = 0; i < m_all_types.length; i++) {
                JButton button = m_highlight_buttons.get(i);
                if (button == e.getSource()) {
                    m_highlighting.set(i, !m_highlighting.get(i));

                    if (m_highlighting.get(i)) {
                        // Should be "lit"
                        button.setIcon(m_lightbuilb_lit_icon);
                    } else {
                        button.setIcon(m_lightbulb_dim_icon);
                    }
                }
            }
        }
    }
}
