package hlt;
import java.util.*;

public class Navigation {

    public static ThrustMove navigateShipToDock(
            final GameMap gameMap,
            final Ship ship,
            final Entity dockTarget,
            final int maxThrust,
            final Map<Ship, Position> record)
    {
        final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
        final boolean avoidObstacles = true;
        final double angularStepRad = Math.PI/180.0;
        final Position targetPos = ship.getClosestPoint(dockTarget);

        return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad, true, record);
    }

    public static ThrustMove navigateShipTowardsTarget(
            final GameMap gameMap,
            final Ship ship,
            final Position targetPos,
            final int maxThrust,
            final boolean avoidObstacles,
            final int maxCorrections,
            final double angularStepRad,
            final boolean isDocking,
            final Map<Ship,Position> record)
    {
        if (maxCorrections <= 0) {
            return null;
        }
        
        
        final double distance = ship.getDistanceTo(targetPos);
        final double angleRad = ship.orientTowardsInRad(targetPos);

        if (avoidObstacles && !gameMap.objectsBetween(ship, targetPos).isEmpty()) {
            final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
            final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
            final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad, false, record);
        }
        
        int thrust;
        if(isDocking){
            if (distance < maxThrust) {
                // Do not round up, since overshooting might cause collision.
                thrust = (int) distance;
            }else {
                thrust = maxThrust;
            }
        }else{
            if (distance - 2 < maxThrust) {
                // Do not round up, since overshooting might cause collision.
                thrust = (int) distance - 2;
            }else {
                thrust = maxThrust;
            }
        }
        if(thrust > 7){
            thrust = 7;
        }
        if(thrust < 0){
            thrust = 0;
        }
        
        double t = 0;
        for(Ship ships: record.keySet()){
            final double r = Constants.SHIP_RADIUS * 5;
            t = Collision.collision_time(r, ship, ships,Position.calculateShipVelocity(angleRad, maxThrust), record.get(ships));
            Log.log("Collision Detection: t = " + t +" ship [" + ship.getId() + "]" + "[" + ships.getId() + "]");
            if(t >= 0 && t<= 1){
                Log.log("Collision detected between ships [" + ship.getId() + "] and [" + ships.getId() + "]");
                Record.storeVectors(ship, new Position(0,0));
                return new ThrustMove(ship,Util.angleRadToDegClipped(angleRad),0);
                
                
            }
        }

        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        Record.storeVectors(ship, Position.calculateShipVelocity(angleRad, thrust));
        return new ThrustMove(ship, angleDeg, thrust);
    }
    public static Position getPlanetSpiralPoint(Ship ship, Planet planet, double AngleStepRad){
        double newAngle = planet.orientTowardsInRad(ship) + AngleStepRad;
        
        double xPos = (Math.cos(newAngle) * (planet.getRadius()) + 10) + planet.getXPos();
        double yPos = (Math.sin(newAngle) * (planet.getRadius()) + 10) + planet.getYPos();
        
        return new Position(xPos,yPos);
        
        
    }
    
}

    
   