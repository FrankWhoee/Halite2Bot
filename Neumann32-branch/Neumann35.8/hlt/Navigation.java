package hlt;
import java.util.*;

public class Navigation {

    public static ThrustMove navigateShipToDock(
            final GameMap gameMap,
            final Ship ship,
            final Entity dockTarget,
            final int maxThrust)
    {
        final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
        final boolean avoidObstacles = true;
        final double angularStepRad = Math.PI/180.0;
        final Position targetPos = ship.getClosestPoint(dockTarget);

        return navigateShipTowardsTarget(gameMap, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad);
    }

    public static ThrustMove navigateShipTowardsTarget(
            final GameMap gameMap,
            final Ship ship,
            final Position targetPos,
            final int maxThrust,
            final boolean avoidObstacles,
            final int maxCorrections,
            final double angularStepRad)
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

            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad);
        }

        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }

        final int angleDeg = Util.angleRadToDegClipped(angleRad);

        return new ThrustMove(ship, angleDeg, thrust);
    }
    
    public static ThrustMove navigateShipTowardsTargetWithCollisionTime(
            final GameMap gameMap,
            final Ship ship,
            final Position targetPos,
            final Map<Entity, Double> record)
            
    {
        final double distance = ship.getDistanceTo(targetPos);
        final double angleRad = ship.orientTowardsInRad(targetPos);
        int thrustActual = 0;
        
        for(Entity entities : record.keySet()){
            for(int thrust = 0; thrust < 7; thrust += 1){
                if(Collision.collision_time(angleRad, new Position(ship.getXPos(), ship.getYPos()), new Position(entities.getXPos(), entities.getYPos()), thrust, record.get(entities)) == -1){
                    thrustActual = thrust;
                }
            }
        }
        
        record.put(ship, (double)thrustActual);
        
        if (distance < thrustActual) {
            // Do not round up, since overshooting might cause collision.
            thrustActual = (int) distance;
        }
        

        final int angleDeg = Util.angleRadToDegClipped(angleRad);

        return new ThrustMove(ship, angleDeg, thrustActual);
    }
}
