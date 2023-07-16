package boardanalyzer.ui.side_panel_tabs.elements;

import javax.swing.*;
import java.awt.*;

public class MinMaxHoldSizePanel extends JPanel {
    private final TextField m_min_textfield;
    private final TextField m_max_textfield;
    public MinMaxHoldSizePanel() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
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
