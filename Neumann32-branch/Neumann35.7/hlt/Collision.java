package hlt;

public class Collision {
    /**
     * Test whether a given line segment intersects a circular area.
     *
     * @param start  The start of the segment.
     * @param end    The end of the segment.
     * @param circle The circle to test against.
     * @param fudge  An additional safety zone to leave when looking for collisions. Probably set it to ship radius.
     * @return true if the segment intersects, false otherwise
     */
    public static boolean segmentCircleIntersect(final Position start, final Position end, final Entity circle, final double fudge) {
        // Parameterize the segment as start + t * (end - start),
        // and substitute into the equation of a circle
        // Solve for t
        final double circleRadius = circle.getRadius();
        final double startX = start.getXPos();
        final double startY = start.getYPos();
        final double endX = end.getXPos();
        final double endY = end.getYPos();
        final double centerX = circle.getXPos();
        final double centerY = circle.getYPos();
        final double dx = endX - startX;
        final double dy = endY - startY;

        final double a = square(dx) + square(dy);

        final double b = -2 * (square(startX) - (startX * endX)
                            - (startX * centerX) + (endX * centerX)
                            + square(startY) - (startY * endY)
                            - (startY * centerY) + (endY * centerY));

        if (a == 0.0) {
            // Start and end are the same point
            return start.getDistanceTo(circle) <= circleRadius + fudge;
        }

        // Time along segment when closest to the circle (vertex of the quadratic)
        final double t = Math.min(-b / (2 * a), 1.0);
        if (t < 0) {
            return false;
        }

        final double closestX = startX + dx * t;
        final double closestY = startY + dy * t;
        final double closestDistance = new Position(closestX, closestY).getDistanceTo(circle);

        return closestDistance <= circleRadius + fudge;
    }

    public static double square(final double num) {
        return num * num;
    }
    
    public static double collision_time(double r, final Position loc1, final Position loc2, double thrust1, double thrust2){
        
        final double dx = loc1.getXPos() - loc2.getXPos();
        final double dy = loc1.getYPos() - loc2.getYPos();
        
        final double vx1 = thrust1 * Math.cos(r);
        final double vy1 = thrust1 * Math.sin(r);
        
        final double vx2 = thrust2 * Math.cos(r);
        final double vy2 = thrust2 * Math.sin(r);
        
        final double dvx = vx1 - vx2;
        final double dvy= vy1 - vy2;
        
        final double a = Math.pow(dvx, 2) + Math.pow(dvy,2);
        final double b = 2 * (dx * dvx + dy * dvy);
        final double c = Math.pow(dx, 2) + Math.pow(dy, 2) - Math.pow(r, 2);
        
        final double disc = Math.pow(b,2) - 4 * a * c;
        
        if(a == 0){
            if(b==0){
                if(c <= 0){
                    return(0);
                }
                return(-1);
            }
            final double t = -c/b;
            if(t >= 0){
                return(t);
            }
            return(-1);
        } else if(disc == 0){
            final double t = -b/(2*a);
            return(t);
        } else if( disc > 0){
            final double t1 = -b + Math.sqrt(disc);
            final double t2 = -b - Math.sqrt(disc);
            
            if(t1 >= 0 && t2 >=0){
                return Math.min(t1, t2) / (2 * a);
            } else if(t1 <= 0.0 && t2 <= 0.0){
                return Math.max(t1, t2) / (2 * a);
            } else{
                return 0;
            }
        } else{
            return(-1);
        }
  
    }
    
}
