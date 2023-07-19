package boardanalyzer.ui.basic_elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PercentageVisualiser<Type> extends JPanel {
    private ArrayList<JProgressBar> m_bars;
    private ArrayList<JButton> m_hightlight_buttons;
    private Type[] m_all_types;
    public PercentageVisualiser(Type[] values) {
        setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        m_bars = new ArrayList<>();
        m_hightlight_buttons = new ArrayList<>();
        Icon icon;
        try {
            Image image = ImageIO.read(ClassLoader.getSystemResource("images/lightbulb.PNG"));
            Image scaled_image = image.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled_image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        m_all_types = values;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        for (Type type : m_all_types) {
            constraint.gridwidth = 2;
            constraint.gridx = 2;
            constraint.weightx = 0.5;
            constraint.anchor = GridBagConstraints.CENTER;
            JLabel label = new JLabel(type.toString() + "'s");
            add(label, constraint);

            JButton highlight_button;
            if (icon.getIconHeight() > 0) {
                highlight_button = new JButton(icon);
            } else {
                // I have no idea if this works
                highlight_button = new JButton("\uD83D\uDCA1");
            }
            highlight_button.setMargin(new Insets(0,0,0,0));
            highlight_button.setMaximumSize(new Dimension(100, 20));
            m_hightlight_buttons.add(highlight_button);

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
    }

    public void addActionListener(ActionListener listener) {
        for (JButton button : m_hightlight_buttons) {
            button.addActionListener(listener);
        }
    }

    public void updateBarPercentages(int[] vals) {
        int total = 0;
        for (int val : vals) {
            total = total + val;
        }
        for (int i = 0; i < m_all_types.length; i++) {
            m_bars.get(i).setValue((int)(((double)vals[i] / (double)total) * 100.0));
        }
    }
}
