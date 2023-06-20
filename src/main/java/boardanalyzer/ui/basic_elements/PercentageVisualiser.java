package boardanalyzer.ui.basic_elements;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class PercentageVisualiser<Type> extends JPanel {
    private ArrayList<JProgressBar> m_bars;
    private Type[] m_all_types;
    public PercentageVisualiser(Type[] values) {
        setLayout(new GridLayout(0,2));
        m_bars = new ArrayList<JProgressBar>();
        m_all_types = values;
        for (Type type : m_all_types) {
            JLabel label = new JLabel(type.toString() + "'s");
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);
            m_bars.add(bar);
            add(label);
            add(bar);
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
