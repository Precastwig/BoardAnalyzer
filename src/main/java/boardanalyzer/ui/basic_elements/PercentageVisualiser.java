package boardanalyzer.ui.basic_elements;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class PercentageVisualiser<Type> extends JPanel {
    private ArrayList<JProgressBar> m_bars;
    private Type[] m_all_types;
    public PercentageVisualiser(Type[] values) {
        setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        m_bars = new ArrayList<>();
        m_all_types = values;
        constraint.gridy = 0;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        for (Type type : m_all_types) {
            constraint.gridwidth = 1;
            constraint.gridx = 0;
            constraint.weightx = 0.3;
            constraint.anchor = GridBagConstraints.PAGE_START;
            JLabel label = new JLabel(type.toString() + "'s");
            add(label, constraint);

            constraint.gridwidth = 2;
            constraint.gridx = 1;
            constraint.weightx = 0.7;
            constraint.anchor = GridBagConstraints.PAGE_END;
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);
            m_bars.add(bar);
            add(bar, constraint);

            //b.add(Box.createHorizontalGlue());
//            add(b);
            constraint.gridy++;
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
