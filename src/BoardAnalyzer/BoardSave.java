package BoardAnalyzer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import BoardAnalyzer.MathUtils.Vector2;

public class BoardSave implements Serializable {
	private static final long serialVersionUID = 2L;
	public Board m_board;
	public Vector2 m_board_dimensions;
	transient public Image m_board_image;
	public int[] m_hold_type_ratio;
	public int[] m_hold_direction_ratio;
	
	// This class is a wrapper for all the objects we want to save to file
	// Everything contained within this class should be
	// a) public
	// b) Serialisable or added to the write/read object functions
	public BoardSave() {
		m_board = new Board();
		m_board_dimensions = new Vector2(0,0);
		m_board_image = null;
		m_hold_type_ratio = new int[] {
				0,0,0,0,0,0
		};
		m_hold_direction_ratio = new int[] {
				0,0,0,0,0,0
		};
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (m_board_image != null) {
        	ImageIO.write((BufferedImage)m_board_image, "png", out); // png is lossless        	
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (in.available() > 0) {
        	m_board_image = ImageIO.read(in);        	
        } else {
        	System.out.println("Image not availabele to read");
        }
    }
}
