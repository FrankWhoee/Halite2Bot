import hlt.*;

import java.util.*;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Neumann-v29");

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
                
                //Attack if there are no empty planets left to capture.
                
                //Don't do anything if you're docked
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                //Iterate every planet
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    
                    //If you can dock, it is in your best interest to dock.
                    if (ship.canDock(planet) && (planet.isFull() == false)  && (gameMap.allPlanetsTaken() == false)){
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }else if (planet.getOwner() == gameMap.getMyPlayerId()){
                        continue;
                        
                    }else if(planet.isOwned() && gameMap.allPlanetsTaken() == false){
                        continue;   
                    } else if(planet.getOwner() != gameMap.getMyPlayerId()){
                        
                        if((ship.getDistanceTo(largestPlanet) < 90) && (largestPlanet.isOwned() == false)){
                            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                            if (newThrustMove != null) {
                                moveList.add(newThrustMove);
                            	break;
                            }
                        }
                        
                        if(gameMap.allPlanetsTaken() == true){
                            ThrustMove newMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getSortedEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);;
                            if(ship.getId() % 5 == 0 && ship.getId() != 0){
                                newMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getTeamShipsByHealth(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                            }else{
                                newMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getSortedEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                            }
                            if (newMove != null) {
                                moveList.add(newMove);
                                break;
                            }   
                        }
                    }
                    
                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        break;
                    }
                    
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}

