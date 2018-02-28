package hlt;

import java.util.*;
public class Navigation {

    public static ThrustMove navigateShipToDock(
            final GameMap gameMap,
            final Ship ship,
            final Entity dockTarget,
            final int maxThrust)
    {
        
        if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
            return null;
        }
        
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
        
        if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
            return null;
        }
        
        double distance = ship.getDistanceTo(targetPos);
        double angleRad = (Util.angleRadToDegClipped(ship.orientTowardsInRad(targetPos)) * Math.PI) / 180.0;
        //double angleRad = ship.orientTowardsInRad(targetPos);
        
        int thrust;
        
        if(maxThrust > 7) {
        	thrust = 7;
        }else if(maxThrust < 0) {
        	thrust = 0;
        }
        
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }
        
        double distanceNegative = ship.getDistanceTo(targetPos);
        double distancePositive = ship.getDistanceTo(targetPos);
        final double radian = Math.PI * 2;
        for(double divergence = 0; divergence < radian; divergence += angularStepRad){
            Position newTarget = getNewTarget(ship,angleRad + divergence, distance);
            distanceNegative = ship.getDistanceTo(newTarget);
            if(isTargetInsideGameMap(newTarget, gameMap)) {
            	if(gameMap.objectsBetween(ship, newTarget).isEmpty()){
            		Log.log("NAVIGATION.navigateShipTowardsTarget: Ship " + ship.getId() + " passed segmentCircleIntersect test");
                    if(!checkForCollisions(ship, angleRad + divergence, thrust)){
                    	Log.log("NAVIGATION.navigateShipTowardsTarget: Ship " + ship.getId() + " passed collision time test");
                        angleRad += divergence;
                        
                        Log.log("NAVIGATION.navigateShipTowardsTarget: Ship " + ship.getId() + " passed all tests, course corrected and set.");
                        break;
                    }
                }
            }
            
            newTarget = getNewTarget(ship,angleRad - divergence, distance);
            distancePositive = ship.getDistanceTo(newTarget);
            if(isTargetInsideGameMap(newTarget, gameMap)) {
            	if(gameMap.objectsBetween(ship, newTarget).isEmpty()){
            		Log.log("NAVIGATION.navigateShipTowardsTarget: Ship " + ship.getId() + " passed segmentCircleIntersect test");
	                if(!checkForCollisions(ship, angleRad - divergence, thrust)){
	                	Log.log("NAVIGATION.navigateShipTowardsTarget: Ship " + ship.getId() + " passed collision time test");
	                    angleRad -= divergence;
	                    
	                    Log.log("NAVIGATION.navigateShipTowardsTarget: Ship " + ship.getId() + " passed all tests, course corrected and set.");
	                    break;
	                }
	            }
            }
            
            
            
            
        }
        
        if(checkForCollisions(ship, angleRad, thrust)) {
        	Log.log("NAVIGATION.navigateShipTowardsTarget: COLLISION VERIFY FAILED. SETTING THRUST TO 0.");
        	thrust = 0;
        }else if(!gameMap.objectsBetween(ship, getNewTarget(ship,angleRad, thrust)).isEmpty()){
        	Log.log("NAVIGATION.navigateShipTowardsTarget: SEGMENTCIRCLEINTERSECT VERIFY FAILED. SETTING THRUST TO 0.");
        	thrust = 0;
        	
        }else {
        	if (distance < maxThrust) {
                // Do not round up, since overshooting might cause collision.
                thrust = (int) distance;
            }
            else {
                thrust = maxThrust;
            }
        }

        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        Velocity decidedVel = Velocity.calculateVelocity(angleRad, thrust);
        Log.log("NAVIGATION.navigateShipTowardsTarget: angleDeg: " +  angleDeg + " thrust: " + thrust);
        Record.storeVectors(ship, decidedVel);
        return new ThrustMove(ship, angleDeg, thrust);
    }
    
    public static boolean checkForCollisions(Ship ship, double angleRad, int thrust){
        boolean willCrash = false;
        for(final Ship teamShip: Record.getRecord().keySet()){
        	if(ship.getDistanceTo(teamShip) > Constants.COLLISION_TIME_SCAN_RANGE) {
        		continue;
        	}
        		final double r = Constants.SHIP_RADIUS * 2;
                final Velocity shipVelocity = Velocity.calculateVelocity(angleRad, thrust);
                final double t = Collision.collision_time(r,ship, teamShip,shipVelocity , Record.getRecord().get(teamShip));
                Log.log("NAVIGATION.checkForCollisions: Checking for collision between ship " + ship.getId() +" and " + teamShip.getId() + " t = " + t);
                if(t >= 0 && t <= 1 && teamShip != ship){
                    willCrash = true;
                    Log.log("NAVIGATION.checkForCollisions: Collision detected. t = " + t + "ships [" + ship.getId() + "," + teamShip.getId() + "]");
                }
        	
        }
        return willCrash;
    }
    
    public static Position getNewTarget(Ship ship, double angleRad, double distance){
            double newTargetDx = Math.cos(angleRad) * distance;
            double newTargetDy = Math.sin(angleRad) * distance;
            Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);  
            return newTarget;
    }
    
    public static ThrustMove retreatMove(Ship ship, GameMap gameMap, Position targetPos, int maxThrust, double angularStepRad){
        
        if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
            return null;
        }
        
        Log.log("Ship " + ship.getId() + " thrusting away");
        final double radian = Math.PI * 2;
        double targetAngleRad = (Util.angleRadToDegClipped(ship.orientTowardsInRad(targetPos)) * Math.PI) / 180.0;
        double angleRad = targetAngleRad + radian/2;
        Position target = getNewTarget(ship, angleRad, maxThrust + 1);
        
        return navigateShipTowardsTarget(gameMap,ship,target,maxThrust,true,Constants.MAX_NAVIGATION_CORRECTIONS,angularStepRad);
    }
    
    public static boolean isTargetInsideGameMap(Position position, GameMap gameMap) {
    	if(position.getXPos() > Constants.MAX_DISTANCE_TO_BORDER && position.getXPos() < (gameMap.getWidth() - Constants.MAX_DISTANCE_TO_BORDER) && position.getYPos() > Constants.MAX_DISTANCE_TO_BORDER && position.getYPos() < (gameMap.getHeight() - Constants.MAX_DISTANCE_TO_BORDER)) {
    		return true;
    	}else {
    		return false;
    	}
    }
}

