package BoardAnalyzer;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MainWindow {

    static JFrame m_frame;
    static BoardFrame m_board;
    static int PREFERRED_GENERATE_TAB_WIDTH = 200;
    static int PREFERRED_BOTTOM_BAR_HEIGHT = 50;
    static Properties applicationProps;
    
    public static void saveAppProperties() {
    	FileOutputStream out;
		try {
			out = new FileOutputStream("appProperties");
			applicationProps.store(out, "---No Comment---");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MainWindow.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        m_frame = new JFrame("Board Analyzer");
        m_frame.setResizable(false);
        m_frame.setSize(1800,1200);
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        m_frame.setLayout(new BorderLayout());      
        
        //Create the game panel and link to the hold selection settings by passing into constructor
        JButton save_hold_button = new JButton("Save hold");
        JButton delete_hold_button = new JButton("Delete hold");
        JLabel instruction_label = new JLabel("Load image to begin...");
        JButton open_button = new JButton("Open board image");
        open_button.setPreferredSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
        open_button.setMinimumSize(new Dimension(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20));
        open_button.setSize(MainWindow.PREFERRED_GENERATE_TAB_WIDTH, 20);
        JButton set_board_corners_button = new JButton("Set board corners");
        JButton save_settings_button = new JButton("Save");
        // Settings Panels
        HeatmapSettings heatmap_settings_panel = new HeatmapSettings();
        BoardSettings board_settings_panel = new BoardSettings(open_button, save_settings_button, set_board_corners_button);
        HoldSelectionSettings hold_selection_settings = new HoldSelectionSettings(save_hold_button, delete_hold_button);
        HoldGenerationSettings hold_generation_settings = new HoldGenerationSettings();
        m_board = new BoardFrame(
        		m_frame, 
        		hold_selection_settings, 
        		instruction_label, 
        		board_settings_panel,
        		heatmap_settings_panel);
        
        save_hold_button.setActionCommand("SaveHold");
        save_hold_button.addActionListener(m_board);
        delete_hold_button.setActionCommand("DeleteHold");
        delete_hold_button.addActionListener(m_board);
        
        // Tabs
        JTabbedPane tabbed_panel = new JTabbedPane();
        
        // Board settings tab
		open_button.setActionCommand("OpenFile");
		open_button.addActionListener(m_board);
		set_board_corners_button.setActionCommand("SetCorners");
		set_board_corners_button.addActionListener(m_board);
		save_settings_button.setActionCommand("Save");
        save_settings_button.addActionListener(m_board);
		
        tabbed_panel.addTab("Board settings", board_settings_panel);

        // Heatmap tab
		JButton generate_heatmap_button = new JButton("GenerateHeatmap");
		generate_heatmap_button.setActionCommand("GenerateHeatmap");
		generate_heatmap_button.addActionListener(m_board);
		generate_heatmap_button.setPreferredSize(new Dimension(PREFERRED_GENERATE_TAB_WIDTH, 20));
		
		heatmap_settings_panel.add(generate_heatmap_button);
		heatmap_settings_panel.setPreferredSize(new Dimension(PREFERRED_GENERATE_TAB_WIDTH, 20));
		
		tabbed_panel.addTab("Heatmap", heatmap_settings_panel);
		
		// Hold generation tab
		JButton generate_hold_button = new JButton("GenerateHold");
		generate_hold_button.setActionCommand("GenerateHold");
		generate_hold_button.addActionListener(m_board);
		generate_hold_button.setPreferredSize(new Dimension(PREFERRED_GENERATE_TAB_WIDTH, 20));
		tabbed_panel.addTab("Hold Suggestion", hold_generation_settings);

		// Sidebar section
		JPanel eastbit = new JPanel(new GridLayout(4, 1, 0, 10));
		eastbit.add(tabbed_panel);
		eastbit.add(hold_selection_settings);
		eastbit.setPreferredSize(new Dimension(PREFERRED_GENERATE_TAB_WIDTH, 1200));
		
		/////// Bottom part
        JPanel bottombit = new JPanel(new BorderLayout());
        bottombit.setBackground(Color.WHITE);
        bottombit.add(instruction_label, BorderLayout.CENTER);
        
        /// Add everything to frame
        m_frame.add(m_board,BorderLayout.CENTER);
        m_frame.add(eastbit, BorderLayout.EAST);
        m_frame.add(bottombit,BorderLayout.PAGE_END);

        //Make see
        m_frame.setVisible(true);
    }
}