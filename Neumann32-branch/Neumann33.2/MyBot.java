import hlt.*;

import java.util.*;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Neumann-v33.0");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        Planet largestPlanet = gameMap.getPlanet(0);
        for(int i = 0; i < gameMap.getAllPlanets().size(); i++){
            if(gameMap.getPlanet(i).getRadius() > largestPlanet.getRadius()){
                largestPlanet = gameMap.getPlanet(i);
            }
        }
        
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                //Don't do anything if you're docked
                if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
                    continue;
                }
                
                //Iterate every planet
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {

                    if(ship.canDock(planet) && !planet.isFull()){
                        moveList.add(new DockMove(ship,planet));
                        break;
                    }else if(ship.getDistanceTo(gameMap.nearestVulnerableEnemyShips(ship).get(0)) < 50 || gameMap.allPlanetsTaken() || ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) < 15){
                        ThrustMove attackMove = Navigation.navigateShipToDock(gameMap, ship, planet, 0);
                        
                        //If there are ships that are docking or undocking, attack them first.
                        if(gameMap.nearestVulnerableEnemyShips(ship).isEmpty()){
                            attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getSortedEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        }else{
                            attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.nearestVulnerableEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        }
        
                        //queue up attack move and stop considering planets
                        if (attackMove != null) {
                            moveList.add(attackMove);
                        }
                        break;
                        
                    }else if(planet.isOwned() == false){
                        final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                        //queue up thrust move (to dock) and stop considering planets
                        if (thrustMove != null) {
                            moveList.add(thrustMove);
                        }
                        break;
                        
                    }
                    
                }
                
            }
            Networking.sendMoves(moveList);
        }
    }
}

