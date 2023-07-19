package boardanalyzer;

import boardanalyzer.ui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MainWindow {

    static JFrame m_frame;
    static int PREFERRED_GENERATE_TAB_WIDTH = 200;
    static int PREFERRED_BOTTOM_BAR_HEIGHT = 50;
    static Properties applicationProps;
    
    public static void saveAppProperties() {
    	FileOutputStream out;
		try {
			out = new FileOutputStream("appProperties");
			applicationProps.store(out, "---No Comment---");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
    	try { 
    	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
    	// create and load default properties
    	Properties defaultProps = new Properties();
    	FileInputStream in;
		try {
			in = new FileInputStream("defaultProperties");
			defaultProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// create application properties with default
		applicationProps = new Properties(defaultProps);
		
		try {			
			in = new FileInputStream("appProperties");
			applicationProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        m_frame = new JFrame("Board Analyzer");
        m_frame.setResizable(false);
        m_frame.setSize(1800,1200);
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_frame.setLayout(new BorderLayout());      
        try {
			m_frame.setIconImage(ImageIO.read(ClassLoader.getSystemResource("images/icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // Create the three main panels
		InstructionPanel instruction_panel = new InstructionPanel();
		SidePanel side_panel = new SidePanel(PREFERRED_GENERATE_TAB_WIDTH);
		BoardPanel board = new BoardPanel(side_panel, instruction_panel);

		// Make the board listen to the side panel
		side_panel.addActionListener(board);
		side_panel.addChangeListener(board);

        // Add everything to frame
        m_frame.add(board,BorderLayout.CENTER);
        m_frame.add(side_panel, BorderLayout.EAST);
        m_frame.add(instruction_panel,BorderLayout.PAGE_END);

        // Make see
        m_frame.setVisible(true);
    }
}