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
        
        
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                boolean shipIsHarvester = true;
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                if(ship.getId() % 20 == 0){
                    shipIsHarvester = false;
                }
                
                
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    if (ship.canDock(planet) && (planet.isFull() == false) && shipIsHarvester) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }else{
                        if (planet.getOwner() == ship.getOwner()) {
                            continue;
                        }
                    }
                    
                   if(ship.getDistanceTo(planet) < (planet.getRadius() + 50) && ship.getOwner() != planet.getOwner() && (ship.getDockingStatus() != ship.getDockingStatus().Undocked)){
                       shipIsHarvester = false;
                   }
                   
                   if(ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) < 20){
                       shipIsHarvester = false;
                   }

                    

                    if(shipIsHarvester == false && gameMap.allPlanetsTaken()){
                        ThrustMove newMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getSortedEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);;
                        if(ship.getId() % 5 == 0){
                            newMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getTeamShipsByHealth(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        }else{
                            newMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getSortedEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        }
                        
                        if (newMove != null) {
                            moveList.add(newMove);
                            break;
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
