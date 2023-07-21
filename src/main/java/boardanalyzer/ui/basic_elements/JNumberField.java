package boardanalyzer.ui.basic_elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class JNumberField extends JPanel {
    private final JTextField m_text;

    public JNumberField() {
        m_text = new JTextField(6);
        m_text.setText("0.0");
        m_text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String value = m_text.getText();
                if ((e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
                        (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') ||
                        (e.getKeyChar() == KeyEvent.VK_PERIOD && !value.contains(".") && value.length() > 0)) {
                    m_text.setEditable(true);
                    super.keyPressed(e);
                } else {
                    m_text.setEditable(false);
                }
            }
        });
        add(m_text);
    }

    @Override
    public void setPreferredSize(Dimension d) {
        super.setPreferredSize(d);
        m_text.setPreferredSize(d);
    }

    public double getNumber() {
        try {
            return Double.parseDouble(m_text.getText());
        } catch (NumberFormatException e) {
            System.out.println(e);
            return 0.0;
        }
    }

    public void setNumber(double number) {
        DecimalFormat formatted_num = new DecimalFormat("#.##");
        m_text.setText(formatted_num.format(number));
    }
}
