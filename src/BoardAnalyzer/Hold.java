package BoardAnalyzer;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Hold implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Vector2 m_pos;
	public double m_size;
	public double m_direction_rad;
	
	// Holds can be any combination of the below categories
	public boolean m_is_jug;
	public boolean m_is_crimp;
	public boolean m_is_sloper;
	public boolean m_is_pocket;
	public boolean m_is_pinch;
	public boolean m_is_foot;
	
	public Hold() {
		m_pos = new Vector2(0,0);
		m_size = 0;
		m_direction_rad = 0;
		m_is_jug = false;
		m_is_crimp = false;
		m_is_sloper = false;
		m_is_pocket = false;
		m_is_pinch = false;
		m_is_foot = false;
	}
	
	public Hold(double xpos, double ypos, double size, double direction_rad) {
		m_pos = new Vector2(xpos, ypos);
		m_size = size;
		m_direction_rad = direction_rad;
		m_is_jug = false;
		m_is_crimp = false;
		m_is_sloper = false;
		m_is_pocket = false;
		m_is_pinch = false;
		m_is_foot = false;
	}
	
	public boolean contains(int x, int y) {
		Vector2 centre = getCentrePoint();
		return Math.pow(x - centre.x,2) + Math.pow(y - centre.y,2) < Math.pow(m_size / 2.0, 2);
	}
	
	public Vector2 getCentrePoint() {
		return new Vector2(m_pos.x + m_size/2.0, m_pos.y + m_size/2.0);
	}
	
	public void print() {
		System.out.print("Hold: ");
		m_pos.print();
		System.out.println("Size: " + m_size);
		System.out.println("Direction: " + m_direction_rad);
	}
}
