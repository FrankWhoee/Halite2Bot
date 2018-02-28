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
        Map<Entity,Double> record = new HashMap<Entity,Double>();
        final ArrayList<Move> moveList = new ArrayList<>();
        Planet largestPlanet = gameMap.getPlanet(0);
        for(int i = 0; i < gameMap.getAllPlanets().size(); i++){
            if(gameMap.getPlanet(i).getRadius() > largestPlanet.getRadius()){
                largestPlanet = gameMap.getPlanet(i);
            }
        }
        int turnNum = 0;
        
        for (;;) {
            record.clear();
            moveList.clear();
            networking.updateMap(gameMap);
            turnNum++;
            
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                
                //Coward function
                if(turnNum > 40 && (gameMap.getAllShips().size()/gameMap.getAllPlayers().size()) - 10 > gameMap.getMyPlayer().getShips().size()){
                        if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                            moveList.add(new UndockMove(ship));
                            continue;
                        }else{
                            
                            ThrustMove runMove;
                            runMove = Navigation.navigateShipTowardsTargetWithCollisionTime(gameMap, ship, largestPlanet, record);
                            //queue up attack move and stop considering planets
                            moveList.add(runMove);
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
                    
                    if(ship.canDock(planet) && !planet.isFull() && !(ship.getDistanceTo(sortedEnemyShips.get(0)) < 30) && planet.getHealth() > 500){
                        moveList.add(new DockMove(ship,planet));
                        break;
                    }else if(gameMap.allPlanetsTaken() || ship.getDistanceTo(sortedEnemyShips.get(0)) < 25){
                        if(nearestVulnerableEnemyShips.isEmpty() == false){
                            if(ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) < 40 && ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) > 4.5){
                                ThrustMove attackMove;

                                //If there are ships that are docking or undocking, attack them first.
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,nearestVulnerableEnemyShips.get(0), 5, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                break;
                            }
                            if(ship.getDistanceTo(sortedEnemyShips.get(0)) < 35 && ship.getDistanceTo(sortedEnemyShips.get(0)) > 4.5){
                                ThrustMove attackMove;
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,sortedEnemyShips.get(0), 5, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                break;
                            }
                            if(gameMap.allPlanetsTaken() && ship.getDistanceTo(sortedEnemyShips.get(0)) > 4.5){
                                ThrustMove attackMove;
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,sortedEnemyShips.get(0), 7, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                break;
                            }
                        }
                        
                    }else if(planet.isOwned() == false){
                        final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, 5);
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

