package BoardAnalyzer;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Board implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Hold> m_holds;
	private ArrayList<Vector2> m_board_corners;
	private Vector2 m_board_size;
	
	public Board() {
		m_holds = new ArrayList<Hold>();
		m_board_corners = new ArrayList<Vector2>();
		m_board_size = new Vector2();
	}
	
	public void clearCorners() {
		m_board_corners.clear();
	}
	
	public boolean areAllCornersSet() {
		return m_board_corners != null && m_board_corners.size() == 4; 
	}
	
	public void addCorner(Vector2 corner) {
		 m_board_corners.add(corner);
	}
	
	public ArrayList<Vector2> getCorners() {
		return m_board_corners;
	}
	
	public void addHold(Hold h) {
		m_holds.add(h);
	}
	
	public ArrayList<Hold> getHolds() {
		return m_holds;
	}
	
	public void removeHold(Hold h) {
		m_holds.remove(h);
	}
	
	public void setBoardDimensions(double w, double h) {
		m_board_size.x = w;
		m_board_size.y = h;
	}
	
	public double getBoardWidth() {
		return m_board_size.x;
	}
	
	public double getBoardHeight() {
		return m_board_size.y;
	}
	
	public Vector2 getBoardSize() {
		return m_board_size;
	}
	
	public boolean existsHold(int x, int y) {
		for (Iterator<Hold> it = m_holds.iterator(); it.hasNext();) {
			if (it.next().contains(x, y)) {
				return true;
			}
		}
		return false;
	}
	
	public Hold getHold(int x, int y) throws IllegalAccessException {
		for (Iterator<Hold> it = m_holds.iterator(); it.hasNext();) {
			Hold h = it.next();
			if (h.contains(x, y)) {
				return h;
			}
		}
		throw new IllegalAccessException();
	}
	
	public Hold createHold(int x, int y) {
		Hold new_hold = new Hold();
		new_hold.setSize(50);
		new_hold.setPosition(new Vector2(x - 25, y - 25));
//		System.out.println(x + "   " + y);
		m_holds.add(new_hold);
		return new_hold;
	}
}
