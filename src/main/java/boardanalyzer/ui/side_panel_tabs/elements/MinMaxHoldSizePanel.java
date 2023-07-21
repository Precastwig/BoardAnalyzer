package boardanalyzer.ui.side_panel_tabs.elements;

import javax.swing.*;
import java.awt.*;

public class MinMaxHoldSizePanel extends JPanel {
    private final TextField m_min_textfield;
    private final TextField m_max_textfield;

    public MinMaxHoldSizePanel() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(new JLabel("Min"));
        add(Box.createHorizontalStrut(5));
        m_min_textfield = new TextField();
        m_min_textfield.setText("0");
        m_min_textfield.setMaximumSize(new Dimension(300, 20));
        add(m_min_textfield);
        add(Box.createHorizontalGlue());

        add(new JLabel("Max"));
        add(Box.createHorizontalStrut(5));
        m_max_textfield = new TextField();
        m_max_textfield.setText("0");
        m_max_textfield.setMaximumSize(new Dimension(300, 20));
        add(m_max_textfield);

        // I don't know why this is necessary, it just is. Otherwise, it takes up a BUNCH of vertical space.
        setMaximumSize(new Dimension(300, 20));
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

    public void setHoldSizeMin(int min) {
        m_min_textfield.setText(String.valueOf(min));
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

    public void setHoldSizeMax(int max) {
        m_max_textfield.setText(String.valueOf(max));
    }

}
