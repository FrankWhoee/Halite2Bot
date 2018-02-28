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
        int thrust;
        if (distance < maxThrust) {
             // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }else {
             thrust = maxThrust;
        }
        boolean isCollision = false;
        double t = -1;
        for(Ship ships: record.keySet()){
            final double r = Constants.SHIP_RADIUS * 2;
            t = Collision.collision_time(r, ship, ships,Position.calculateShipVelocity(angleRad, thrust), record.get(ships));
            Log.log("t [" + t + "]");
            if(t >= 0 && t<= 1 && ship != ships){
                Log.log("Collision detected between ships [" + ship.getId() + "] and [" + ships.getId() + "]");
                isCollision = true;
            }
        }
        if (avoidObstacles && !gameMap.objectsBetween(ship, targetPos).isEmpty() || isCollision) {
            final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
            final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
            final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

            return navigateShipTowardsTarget(gameMap, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad, false, record);
        }
        if(thrust > 7){
            thrust = 7;
        }
        if(thrust < 0){
            thrust = 0;
        }

        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        Record.storeVectors(ship, Position.calculateShipVelocity(angleRad, thrust));
        return new ThrustMove(ship, angleDeg, thrust);
    }
    public static Position getPlanetSpiralPoint(Ship ship, Planet planet, double AngleStepRad){
        double newAngle = planet.orientTowardsInRad(ship) - AngleStepRad;
        
        double xPos = (Math.cos(newAngle) * (planet.getRadius())*2 + 5) + planet.getXPos();
        double yPos = (Math.sin(newAngle) * (planet.getRadius())*2 + 5) + planet.getYPos();
        Log.log("Ship [" + ship.getId() + "] spiraling planet [" + planet.getId() + "], thrusting to position [" + xPos +"," + yPos + "]");
        Log.log("Ship angle relative to planet[" + planet.orientTowardsInDeg(ship) + " °]");
        Log.log("New angle to move towards[" + newAngle + "rad]");
        
        
        return new Position(xPos,yPos);
        
        
    }
    
    public static boolean isPlanetInBetween(Position target, GameMap gameMap, Ship ship){
        Planet planetInBetween;
        boolean isPlanetInBetween = false;
        for(Entity entity : gameMap.objectsBetween(ship, target)){
            if(entity instanceof Planet){
                planetInBetween = (Planet)entity;
                isPlanetInBetween = true;
                Log.log("Planet [" + planetInBetween.getId() + "]" + " is in between ship [" + ship.getId() + "] and target");
                break;
            }
        }
        return isPlanetInBetween;
    }
    
    public static Planet planetInBetween(Position target, GameMap gameMap, Ship ship){
        for(Entity entity : gameMap.objectsBetween(ship, target)){
            if(entity instanceof Planet){
                return (Planet)entity; 
            }
        }
       return null;
    }
    
    public static ThrustMove planetSpiralMove(Ship ship, Position target, GameMap gameMap){
        if(Navigation.isPlanetInBetween(target, gameMap, ship)){
            Position attackPoint = Navigation.getPlanetSpiralPoint(ship, Navigation.planetInBetween(target, gameMap, ship), 1);
            ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());
            Log.log("Ship [" + ship.getId() +"] detecting vulnerable enemy behind planet. Spiraling around planet [" + Navigation.planetInBetween(target, gameMap, ship).getId() + "]");
            return attackMove;
        }
        return null;
    }
    
}

    
   