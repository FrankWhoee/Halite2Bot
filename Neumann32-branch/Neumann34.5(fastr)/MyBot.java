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
                
                ArrayList<Ship> sortedDangerousEnemyShips = new ArrayList<Ship>();
                sortedDangerousEnemyShips = gameMap.getSortedDangerousEnemyShips(ship);
                
                if(sortedDangerousEnemyShips.size() > 0){
                    if(ship.getDistanceTo(sortedDangerousEnemyShips.get(0)) < 42 && (ship.getDockingStatus() == Ship.DockingStatus.Undocked) && ship.getDistanceTo(sortedDangerousEnemyShips.get(0)) > 4.5){
                        ThrustMove attackMove;
                        attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,sortedDangerousEnemyShips.get(0), Constants.MAX_SPEED/2, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        //queue up attack move and stop considering planets
                        if (attackMove != null) {
                            moveList.add(attackMove);
                        }
                        continue;
                    }
                }
                
                //Don't do anything if you're docked
                if(!sortedDangerousEnemyShips.isEmpty() && (ship.getDockingStatus() == Ship.DockingStatus.Docked)){
                    if(ship.getDistanceTo(sortedDangerousEnemyShips.get(0)) < 42){
                      moveList.add(new UndockMove(ship));
                      continue;
                    }
                }
                
                
                if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
                    continue;
                }
                
                
                
                //Iterate every planet
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    ArrayList<Ship> sortedEnemyShips = new ArrayList<Ship>();
                    sortedEnemyShips = gameMap.getSortedEnemyShips(ship);
                    
                    ArrayList<Ship> nearestVulnerableEnemyShips = new ArrayList<Ship>();
                    nearestVulnerableEnemyShips = gameMap.nearestVulnerableEnemyShips(ship);
                    
                    if(ship.canDock(planet) && !planet.isFull() && !(ship.getDistanceTo(sortedEnemyShips.get(0)) < 30)){
                        moveList.add(new DockMove(ship,planet));
                        break;
                    }else if(gameMap.allPlanetsTaken() || ship.getDistanceTo(sortedEnemyShips.get(0)) < 25){
                        if(nearestVulnerableEnemyShips.size() > 0){
                            if(ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) < 40){
                                ThrustMove attackMove;

                                //If there are ships that are docking or undocking, attack them first.
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,nearestVulnerableEnemyShips.get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                break;
                            }
                            if(ship.getDistanceTo(sortedEnemyShips.get(0)) < 35){
                                ThrustMove attackMove;
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,sortedEnemyShips.get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                break;
                            }
                            if(gameMap.allPlanetsTaken()){
                                ThrustMove attackMove;
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,sortedEnemyShips.get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                break;
                            }
                        }
                        
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

