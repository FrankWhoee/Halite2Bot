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
        double angleRad = (Util.angleRadToDegClipped(ship.orientTowardsInRad(targetPos)) * Math.PI) / 180.0;
        
        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }
        
        boolean willCrash = false;
        for(final Ship teamShip: Record.getRecord().keySet()){
            final double r = Constants.SHIP_RADIUS * 2;
            final Velocity shipVelocity = Velocity.calculateVelocity(angleRad, thrust);
            final double t = Collision.collision_time(r,ship, teamShip,shipVelocity , Record.getRecord().get(teamShip));
            Log.log("t= " + t + " between ships " + ship.getId() + " and " + teamShip.getId());
            if(t >= 0 && t <= 1 && teamShip != ship){
                willCrash = true;
                Log.log("Collision detected between " + ship.getId() + " and " + teamShip.getId());
                Log.log("Would be collision t=" + t);
            }
        }
        
        if ((avoidObstacles && !gameMap.objectsBetween(ship, targetPos).isEmpty()) || willCrash) {
            final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
            final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
            final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad);
        }

        

        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        Velocity decidedVel = Velocity.calculateVelocity(angleRad, thrust);
        Record.storeVectors(ship, decidedVel);
        return new ThrustMove(ship, angleDeg, thrust);
    }
}
