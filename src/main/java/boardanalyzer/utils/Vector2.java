package boardanalyzer.utils;

import org.kynosarges.tektosyne.geometry.PointD;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.io.Serializable;

public class Vector2 implements Serializable {
    @Serial
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

    public Vector2(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vector2(PointD p) {
        this.x = p.x;
        this.y = p.y;
    }

    // Static methods
    public static Vector2 normalize(Vector2 v) {
        double length = Math.sqrt(v.x * v.x + v.y * v.y);

        double x = 0;
        double y = 0;
        if (length != 0.0) {
            double s = 1.0f / length;

            x = v.x * s;
            y = v.y * s;
        }
        return new Vector2(x, y);
    }

    public static double dotProduct(Vector2 v1, Vector2 v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public static Vector2 perpendicularClockwise(Vector2 v) {
        return new Vector2(v.y, -v.x);
    }

    public static Vector2 perpendicularAntiClockwise(Vector2 v) {
        return new Vector2(-v.y, v.x);
    }

    /// Where p1 is a point and v1 is it's directional vector
    /// and p2 is a point and v2 is it's directional vector
    public static Vector2 intersectInfiniteLines(Vector2 p1, Vector2 v1, Vector2 p2, Vector2 v2) {
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

    // Where p1 -> q1 makes up a line
    // and p2 -> q2 makes up a line
    public static boolean intersectFiniteLines(Vector2 p1, Vector2 q1, Vector2 p2, Vector2 q2) {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        if (o1 != o2 && o3 != o4)
            return true;

        if (o1 == 0 && onSegment(p1, p2, q1))
            return true;

        if (o2 == 0 && onSegment(p1, q2, q1))
            return true;

        if (o3 == 0 && onSegment(p2, p1, q2))
            return true;

        return o4 == 0 && onSegment(p2, q1, q2);
    }

    public static boolean onSegment(Vector2 p, Vector2 q, Vector2 r) {
        return q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x)
                && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y);
    }

    public static int orientation(Vector2 p, Vector2 q, Vector2 r) {
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0)
            return 0;

        return (val > 0) ? 1 : 2;
    }

    public static void main(String[] args) {
        Vector2 res = Vector2.intersectInfiniteLines(
                new Vector2(0, 0),
                new Vector2(5, 2),
                new Vector2(10, 0),
                new Vector2(3, -1));
        res.print();

        Vector2 left = new Vector2(-1, 1);
        System.out.println(left.isLeft(new Vector2(0, 0), new Vector2(-1, 0)));
    }

    // Compare two vectors
    public boolean equals(Vector2 other) {
        return (this.x == other.x && this.y == other.y);
    }

    public void normalize() {
        // sets length to 1
        //
        double length = Math.sqrt(x * x + y * y);

        if (length != 0.0) {
            double s = 1.0f / length;
            x = x * s;
            y = y * s;
        }
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double distanceTo(Vector2 v) {
        Vector2 dis_vec = new Vector2(x - v.x, y - v.y);
        return dis_vec.length();
    }

    public double angle() {
        return Math.atan(y / x);
    }

    public double gradient() {
        return y / x;
    }

    public boolean isLeft(Vector2 line_point_A, Vector2 line_point_B) {
        return ((line_point_B.x - line_point_A.x) * (this.y - line_point_A.y) - (line_point_B.y - line_point_A.y) * (this.x - line_point_A.x)) > 0;
    }

    public void print() {
        System.out.println("(" + x + ", " + y + ")");
    }

    public Point2D.Double toPoint2D() {
        return new Point2D.Double(x, y);
    }

    public Vector2 transformBy(PerspectiveTransform t) {
        Point2D.Double in = toPoint2D();
        Point2D.Double out = new Point2D.Double();
        t.transform(in, out);
        return new Vector2(out);
    }
}
