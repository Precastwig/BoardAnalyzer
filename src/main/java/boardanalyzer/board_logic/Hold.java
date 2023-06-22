package boardanalyzer.board_logic;

import boardanalyzer.utils.Vector2;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Hold implements Serializable{
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 1L;
	
	// Holds can be any combination of the below categories
	public enum Type {
		JUG("Jug"),
		CRIMP("Crimp"),
		SLOPER("Sloper"),
		POCKET("Pocket"),
		PINCH("Pinch"),
		FOOT("Foot");
		private String name;
        private Type(String s) {
            this.name = s;
        }
        
        static public Type[] getHandTypes() {
        	return new Type[] {
        		JUG,
        		CRIMP,
        		SLOPER,
        		POCKET,
        		PINCH
        	};
        }
       
        @Override
        public String toString(){
            return name;
        } 
	}

	public enum Direction {
		UP("Up"),
		RIGHT_SKEW("Slanted right"),
		LEFT_SKEW("Slanted left"),
		RIGHT_SIDEPULL("Right sidepull"),
		LEFT_SIDEPULL("Left sidepull"),
		UNDERCUT("Undercut");
		private final String name;
		private Direction(String s) {
			this.name = s;
		}
		private static final double MARGIN = Math.PI / 32.0;
		private static final double UP_ANGLE = -Math.PI/2;
		private static final double LEFT_ANGLE = Math.PI;
		private static final double RIGHT_ANGLE = 0;
		static public Direction classifyAngle(double angle) {
			if (UP_ANGLE - MARGIN < angle && angle < UP_ANGLE + MARGIN) {
				return Hold.Direction.UP;
			} else if ((-LEFT_ANGLE < angle && angle < -LEFT_ANGLE + MARGIN) ||
					(LEFT_ANGLE - MARGIN < angle && angle < LEFT_ANGLE)) {
				return Hold.Direction.LEFT_SIDEPULL;
			} else if (RIGHT_ANGLE - MARGIN < angle && angle < RIGHT_ANGLE + MARGIN) {
				return Hold.Direction.RIGHT_SIDEPULL;
			} else if (angle > 0) {
				return Hold.Direction.UNDERCUT;
			} else if (angle < -Math.PI/2) {
				return Hold.Direction.LEFT_SKEW;
			} else if (angle > -Math.PI/2) {
				return Hold.Direction.RIGHT_SKEW;
			}

			// Default up
			return Hold.Direction.UP;
		}
		static public double getAngle(Direction d) {
			switch(d) {
				case UP -> {
					return UP_ANGLE;
				}
				case LEFT_SIDEPULL -> {
					return LEFT_ANGLE;
				}
				case RIGHT_SIDEPULL -> {
					return RIGHT_ANGLE;
				}
				case LEFT_SKEW -> {
					return -(3 * Math.PI) / 4;
				}
				case RIGHT_SKEW -> {
					return -Math.PI / 4;
				}
				case UNDERCUT -> {
					return Math.PI / 2;
				}
			}
			return 0.0;
		}
		static public double getRandomAngle(Direction d) {
			double random_amount = Math.random();
			switch (d) {
				case UP -> {
					return (UP_ANGLE - MARGIN) + (random_amount * 2 * MARGIN);
				}
				case LEFT_SIDEPULL -> {
					if (random_amount < 0.5) {
						return (-LEFT_ANGLE) + (random_amount * MARGIN);
					} else {
						random_amount = 1 - random_amount;
						return (LEFT_ANGLE) - (random_amount * MARGIN);
					}
				}
				case RIGHT_SIDEPULL -> {
					return (RIGHT_ANGLE - MARGIN) + (random_amount * 2 * MARGIN);
				}
				case UNDERCUT -> {
					return (RIGHT_ANGLE + MARGIN) + (random_amount * (LEFT_ANGLE - (2 * MARGIN)));
				}
				case LEFT_SKEW -> {
					return (-LEFT_ANGLE + MARGIN) + (random_amount * ((Math.PI / 2) - (2 * MARGIN)));
				}
				case RIGHT_SKEW -> {
					return (UP_ANGLE + MARGIN) + (random_amount * ((Math.PI / 2) - (2 * MARGIN)));
				}
			}
			System.out.println("Error!");
			// Default up
			return 0.0;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	private Vector2 m_pos;
	private Vector2 m_size;
	private double m_direction_rad;
	
	private HashSet<Type> m_types;
	
	public Hold() {
		m_pos = new Vector2(0,0);
		m_size = new Vector2(0,0);
		m_direction_rad = Direction.UP_ANGLE;
		m_types = new HashSet<Type>();
	}
	
	public Hold(Vector2 pos, Vector2 size, double direction_rad) {
		m_pos = new Vector2(pos);
		m_size = new Vector2(size);
		m_direction_rad = direction_rad;
		m_types = new HashSet<Type>();
	}
	
	public Vector2 position() {
		return m_pos;
	}
	
	public void setPosition(Vector2 p) {
		m_pos = p;
	}
	
	public Vector2 size() {
		return m_size;
	}
	
	public void setSize(Vector2 s) {
		Vector2 prev_centre_point = getCentrePoint();
		m_size = new Vector2(s);
		setCentrePoint(prev_centre_point);
	}
	
	public double direction() {
		return m_direction_rad;
	}
	
	public void setDirection(double dir) {
		m_direction_rad = dir;
	}
	
	public boolean isOneOf(HashSet<Type> hold_types) {
		Set<Type> intersection = new HashSet<Type>(hold_types);
		intersection.retainAll(m_types);
		return !intersection.isEmpty();
	}
	
	public boolean isTypes(HashSet<Type> hold_types) {
		return m_types.equals(hold_types);
	}
	
	public boolean typesContain(Type t) {
		return m_types.contains(t);
	}
	
	public HashSet<Type> getTypes() {
		return m_types;
	}
	
	public void addTypes(HashSet<Type> hold_types) {
		m_types.addAll(hold_types);
	}
	
	public void addType(Type t) {
		m_types.add(t);
	}
	
	public boolean contains(int x, int y) {
		Vector2 centre = getCentrePoint();
		return (Math.pow(x - centre.x,2) / Math.pow(m_size.x/2.0, 2)) + 
			   (Math.pow(y - centre.y,2) / Math.pow(m_size.y/2.0, 2)) <= 1;
	}
	
	public void setCentrePoint(Vector2 centre_point) {
		m_pos = new Vector2(centre_point.x - (m_size.x / 2.0), centre_point.y - (m_size.y / 2.0));
	}
	
	public Vector2 getCentrePoint() {
		return new Vector2(m_pos.x + m_size.x/2.0, m_pos.y + m_size.y/2.0);
	}
	
	public void print() {
		System.out.print("Hold: ");
		m_pos.print();
		System.out.println("Size: " + m_size);
		System.out.println("Direction: " + m_direction_rad);
		System.out.println(m_types);
	}
	
	public boolean isJug() {
		return m_types.contains(Type.JUG);
	}
	
	public boolean isCrimp() {
		return m_types.contains(Type.CRIMP);
	}
	
	public boolean isSloper() {
		return m_types.contains(Type.SLOPER);
	}
	
	public boolean isPocket() {
		return m_types.contains(Type.POCKET);
	}
	
	public boolean isPinch() {
		return m_types.contains(Type.PINCH);
	}
	
	public boolean isFoot() {
		return m_types.contains(Type.FOOT);
	}
}
