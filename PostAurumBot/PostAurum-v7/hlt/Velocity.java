
package hlt;

public class Velocity {
    public double x;
    public double y;
    
    public Velocity(final double x, final double y){
        this.x = x;
        this.y = y;
    }
    
    public static Velocity calculateVelocity(double angleRad, int thrust){
        double velX = Math.cos(angleRad) * thrust;
        double velY = Math.sin(angleRad) * thrust;
        
        Velocity returnValue = new Velocity(velX, velY);
        return returnValue;
    }
    
}
