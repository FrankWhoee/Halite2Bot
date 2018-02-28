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
                
                if(ship.getId() % 23 == 0){
                    shipIsHarvester = false;
                }
                
                if(ship.getId() % 53 == 0 && ship.canDock(gameMap.getPlanet(gameMap.getAllPlanets().size() - 1))){
                    moveList.add(new DockMove(ship, gameMap.getPlanet(gameMap.getAllPlanets().size() - 1)));
                    continue;
                }
                
                if(ship.getId() % 53 == 0){
                    final ThrustMove explorerThrustMove = Navigation.navigateShipToDock(gameMap, ship, gameMap.getPlanet(gameMap.getAllPlanets().size() - 1), Constants.MAX_SPEED);
                    if (explorerThrustMove != null) {
                        moveList.add(explorerThrustMove);
                        continue;
                    }
                }
                
                if(ship.getId() % 47 == 0 && ship.canDock(gameMap.getPlanet(gameMap.getAllPlanets().size() - 2))){
                    moveList.add(new DockMove(ship, gameMap.getPlanet(gameMap.getAllPlanets().size() - 2)));
                    continue;
                }
                
                if(ship.getId() % 47 == 0){
                    final ThrustMove explorerThrustMove = Navigation.navigateShipToDock(gameMap, ship, gameMap.getPlanet(gameMap.getAllPlanets().size() - 1), Constants.MAX_SPEED);
                    if (explorerThrustMove != null) {
                        moveList.add(explorerThrustMove);
                        continue;
                    }
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
                    
                   if((planet.getDistanceTo(planet) < planet.getRadius() + 20 )&& (planet.getId() != gameMap.getMyPlayer().getId())){
                       shipIsHarvester = false;
                   }

                    

                    if(shipIsHarvester == false){
                        final ThrustMove newAttackMove = Navigation.navigateShipTowardsTarget(gameMap, ship,gameMap.getSortedEnemyShips(ship).get(0), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        if (newAttackMove != null) {
                            moveList.add(newAttackMove);
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


