package BoardAnalyzer;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Vector2 implements Serializable {
	private static final long serialVersionUID = 1L;
	public double x;
	public double y;
	
	public Vector2() {
		x = 0.0;
		y = 0.0;
	}
	
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2(Point2D.Double p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	// Compare two vectors
    public boolean equals(Vector2 other) {
        return (this.x == other.x && this.y == other.y);
    }
    
    public void normalize() {
        // sets length to 1
        //
        double length = Math.sqrt(x*x + y*y);

        if (length != 0.0) {
            double s = 1.0f / (double)length;
            x = x*s;
            y = y*s;
        }
    }
    
    public double length() {
        return Math.sqrt(x*x + y*y);
    }
    
    public double angle() {
    	return Math.atan(y / x);
    }
    
    public double gradient() {
    	return y / x;
    }
    
    public boolean isLeft(Vector2 line_point_A, Vector2 line_point_B){
    	return ((line_point_B.x - line_point_A.x)*(this.y - line_point_A.y) - (line_point_B.y - line_point_A.y)*(this.x - line_point_A.x)) > 0;
    }
    
    public void print() {
    	System.out.println("(" + x + ", " + y + ")");
    }
    
	
	public Point2D.Double toPoint2D() {
		return new Point2D.Double(x,y);
	}
    
    // Static methods
    static public Vector2 normalize(Vector2 v) {
    	double length = Math.sqrt(v.x*v.x + v.y*v.y);
    	
    	double x = 0;
    	double y = 0;
    	if (length != 0.0) {
    		double s = 1.0f / (double)length;
    		
    		x = v.x*s;
    		y = v.y*s;
    	}
    	return new Vector2(x,y);
    }
    
    static public double dotProduct(Vector2 v1, Vector2 v2) {
    	return v1.x * v2.x + v1.y * v2.y;
    }
    
    static public Vector2 perpendicularClockwise(Vector2 v) {
    	return new Vector2(v.y, -v.x);
    }
    
    static public Vector2 perpendicularAntiClockwise(Vector2 v) {
    	return new Vector2(-v.y, v.x);
    }
    
    static public Vector2 intersect(Vector2 p1, Vector2 v1, Vector2 p2, Vector2 v2) {
    	double c1 = p1.y - (v1.gradient() * p1.x); // c = y - mx
    	double c2 = p2.y - (v2.gradient() * p2.x);
    	
    	double x = 0;
    	double y = 0;
    	if (v1.x == 0.0) {
    		x = p1.x;
    		y = v2.gradient() * x + c2;
    	} else if (v2.x == 0.0) {
    		x = p2.x;
    		y = v1.gradient() * x + c1;
    	} else {
    		x = (c2 - c1) / (v1.gradient() - v2.gradient());
    		y = v1.gradient() * x + c1;
    	}
    	
        
//        System.out.println("c1 " + c1);
//        System.out.println("c2 " + c2);
//        System.out.println("x " + x);
//        System.out.println("y " + y);

        return new Vector2(x, y);
    }
    
    public static void main(String[] args) {
    	Vector2 res = Vector2.intersect(
    			new Vector2(0,0), 
    			new Vector2(5,2), 
    			new Vector2(10,0), 
    			new Vector2(3,-1));
    	res.print();
    	
    	Vector2 left = new Vector2(-1,1);
    	System.out.println(left.isLeft(new Vector2(0, 0 ), new Vector2(-1,0)));
    }
}
