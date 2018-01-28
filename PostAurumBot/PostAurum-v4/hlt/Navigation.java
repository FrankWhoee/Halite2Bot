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
        double distance = ship.getDistanceTo(targetPos);
        double angleRad = (Util.angleRadToDegClipped(ship.orientTowardsInRad(targetPos)) * Math.PI) / 180.0;
        
        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }
        
        double distanceNegative = ship.getDistanceTo(targetPos);
        double distancePositive = ship.getDistanceTo(targetPos);
        
        for(double divergence = 0; divergence < Math.PI * 2; divergence += angularStepRad){
            Position newTarget = getNewTarget(ship,angleRad, divergence, distance);
            distanceNegative = ship.getDistanceTo(newTarget);
            if(gameMap.objectsBetween(ship, newTarget).isEmpty()){
                if(!checkForCollisions(ship, angleRad, divergence, thrust)){
                    angleRad += divergence;
                    break;
                }
            }
            newTarget = getNewTarget(ship,angleRad, (divergence * -1), distance);
            distancePositive = ship.getDistanceTo(newTarget);
            if(gameMap.objectsBetween(ship, newTarget).isEmpty()){
                if(!checkForCollisions(ship, angleRad, (divergence * -1), thrust)){
                    angleRad -= divergence;
                    break;
                }
            }
        }
        
        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        Velocity decidedVel = Velocity.calculateVelocity(angleRad, thrust);
        Record.storeVectors(ship, decidedVel);
        return new ThrustMove(ship, angleDeg, thrust);
    }
    
    public static boolean checkForCollisions(Ship ship, double angleRad, double divergence, int thrust){
        boolean willCrash = false;
        for(final Ship teamShip: Record.getRecord().keySet()){
            final double r = Constants.SHIP_RADIUS * 2;
            final Velocity shipVelocity = Velocity.calculateVelocity(angleRad + divergence, thrust);
            final double t = Collision.collision_time(r,ship, teamShip,shipVelocity , Record.getRecord().get(teamShip));
            if(t >= 0 && t <= 1 && teamShip != ship){
                willCrash = true;
            }
        }
        return willCrash;
    }
    
    public static Position getNewTarget(Ship ship, double angleRad, double divergence, double distance){
            double newTargetDx = Math.cos(angleRad + divergence) * distance;
            double newTargetDy = Math.sin(angleRad + divergence) * distance;
            Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);  
            return newTarget;
    }
    
    public static ThrustMove retreatMove(Ship ship, GameMap gameMap, Position targetPos, int maxThrust, double angularStepRad){
        Log.log("Ship " + ship.getId() + " thrusting away");
        final double radian = Math.PI * 2;
        double angleRad = ((ship.orientTowardsInRad(targetPos)%radian + radian/2)+radian)%radian;
        
        final double distance = ship.getDistanceTo(getNewTarget(ship, angleRad, 0, maxThrust));
        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }
        Position newTarget = getNewTarget(ship, angleRad, 0, thrust);
        double distanceNegative = ship.getDistanceTo(newTarget);
        double distancePositive = ship.getDistanceTo(newTarget);

        for(double divergence = 0; divergence < Math.PI * 2; divergence += angularStepRad){
            Log.log("angleRad:" + angleRad + " divergence:" + divergence);
            distanceNegative = ship.getDistanceTo(newTarget);
            newTarget = getNewTarget(ship,angleRad, divergence, distanceNegative);
            if(newTarget.getXPos() > 5 && newTarget.getXPos() < gameMap.getWidth() - 5 && newTarget.getYPos() > 5 && newTarget.getYPos() < gameMap.getHeight() - 5){
                Log.log("First test passed, will not smash into wall. newTarget = (" + newTarget.getXPos() +"," + newTarget.getYPos() + ")");
                Log.log("Current location ("  + ship.getXPos() + "," + ship.getYPos() + ")");
                if(gameMap.objectsBetween(ship, newTarget).isEmpty()){
                    Log.log("Second test passed, will not smash into still objects");
                    if(!checkForCollisions(ship, angleRad, divergence, maxThrust)){
                        Log.log("Third test passed, will not smash into team ships.");
                        angleRad += divergence;
                        Log.log("All tests passed, course set.");
                        break;
                    }
                }
            }
            distancePositive = ship.getDistanceTo(newTarget);
            newTarget = getNewTarget(ship,angleRad, (divergence * -1), distancePositive);
            if(newTarget.getXPos() > 5 && newTarget.getXPos() < gameMap.getWidth() - 5 && newTarget.getYPos() > 5 && newTarget.getYPos() < gameMap.getHeight() - 5){
                Log.log("First test passed, will not smash into wall. newTarget = (" + newTarget.getXPos() +"," + newTarget.getYPos() + ")");
                if(gameMap.objectsBetween(ship, newTarget).isEmpty()){
                    Log.log("Second test passed, will not smash into still objects");
                    if(!checkForCollisions(ship, angleRad, (divergence * -1), maxThrust)){
                        Log.log("Third test passed, will not smash into team ships.");
                        angleRad -= divergence;
                        Log.log("All tests passed, course set.");
                        break;
                    }
                }
            }
        }
        
        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        Velocity decidedVel = Velocity.calculateVelocity(angleRad, maxThrust);
        Record.storeVectors(ship, decidedVel);
        return new ThrustMove(ship, angleDeg, maxThrust);
        
    }
}

