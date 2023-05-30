package BoardAnalyzer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Hold implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Holds can be any combination of the below categories
	public enum Types {
		JUG("Jug"),
		CRIMP("Crimp"),
		SLOPER("Sloper"),
		POCKET("Pocket"),
		PINCH("Pinch"),
		FOOT("Foot");
		private String name;
        private Types(String s) {
            this.name = s;
        }
       
        @Override
        public String toString(){
            return name;
        } 
	}
	
	private Vector2 m_pos;
	private double m_size;
	private double m_direction_rad;
	
	private HashSet<Types> m_types; 
	
	public Hold() {
		m_pos = new Vector2(0,0);
		m_size = 0;
		m_direction_rad = 0;
		m_types = new HashSet<Types>();
	}
	
	public Hold(double xpos, double ypos, double size, double direction_rad) {
		m_pos = new Vector2(xpos, ypos);
		m_size = size;
		m_direction_rad = direction_rad;
		m_types = new HashSet<Types>();
	}
	
	public Vector2 position() {
		return m_pos;
	}
	
	public void setPosition(Vector2 p) {
		m_pos = p;
	}
	
	public double size() {
		return m_size;
	}
	
	public void setSize(double s) {
		m_size = s;
	}
	
	public double direction() {
		return m_direction_rad;
	}
	
	public void setDirection(double dir) {
		m_direction_rad = dir;
	}
	
	public boolean isOneOf(HashSet<Hold.Types> hold_types) {
		Set<Types> intersection = new HashSet<Types>(hold_types);
		intersection.retainAll(m_types);
		return !intersection.isEmpty();
	}
	
	public boolean isTypes(HashSet<Hold.Types> hold_types) {
		return m_types.equals(hold_types);
	}
	
	public HashSet<Hold.Types> getTypes() {
		return m_types;
	}
	
	public void addTypes(HashSet<Hold.Types> hold_types) {
		m_types.addAll(hold_types);
	}
	
	public void addType(Types t) {
		m_types.add(t);
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
		System.out.println(m_types);
	}
	
	public boolean isJug() {
		return m_types.contains(Types.JUG);
	}
	
	public boolean isCrimp() {
		return m_types.contains(Types.CRIMP);
	}
	
	public boolean isSloper() {
		return m_types.contains(Types.SLOPER);
	}
	
	public boolean isPocket() {
		return m_types.contains(Types.POCKET);
	}
	
	public boolean isPinch() {
		return m_types.contains(Types.PINCH);
	}
	
	public boolean isFoot() {
		return m_types.contains(Types.FOOT);
	}
}
