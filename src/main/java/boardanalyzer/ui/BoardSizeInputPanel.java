package boardanalyzer.ui;

import boardanalyzer.ui.basic_elements.JNumberField;
import boardanalyzer.utils.Vector2;

import javax.swing.*;

public class BoardSizeInputPanel extends JPanel {
    private final JNumberField m_width_numberfield;
    private final JNumberField m_height_numberfield;
    public BoardSizeInputPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(Box.createVerticalGlue());
        m_width_numberfield = new JNumberField();
//        m_width_numberfield.setPreferredSize(new Dimension(width,20));
        JPanel width_input = new JPanel();
        width_input.setLayout(new BoxLayout(width_input, BoxLayout.LINE_AXIS));
        JLabel width_label = new JLabel("Width");
        width_label.setAlignmentY(0.5f);
        m_width_numberfield.setAlignmentY(0.5f);
        width_input.add(width_label);
        width_input.add(m_width_numberfield);
        width_input.setAlignmentX(0.5f);
        add(width_input);
        m_height_numberfield = new JNumberField();
//        m_height_numberfield.setPreferredSize(new Dimension(width, 20));
        JPanel height_input = new JPanel();
        JLabel height_label = new JLabel("Height");
        height_input.setLayout(new BoxLayout(height_input, BoxLayout.LINE_AXIS));
        height_input.add(height_label);
        height_input.add(m_height_numberfield);
        height_input.setAlignmentX(0.5f);
        add(height_input);
        add(Box.createVerticalGlue());
    }

    public double getBoardWidth() {
        return m_width_numberfield.getNumber();
    }

    public double getBoardHeight() {
        return m_height_numberfield.getNumber();
    }

    public Vector2 getBoardSize() {return new Vector2(m_width_numberfield.getNumber(), m_height_numberfield.getNumber());}

    public void setBoardWidth(double d) {
        m_width_numberfield.setNumber(d);
    }

    public void setBoardHeight(double d) {
        m_height_numberfield.setNumber(d);
    }
}
