import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Ship112-v2");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);
        
        ArrayList<Planet> plannedPlanets = new ArrayList<>();
        final ArrayList<Move> moveList = new ArrayList<>();
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                for (final Planet planet : gameMap.getAllPlanets().values()) {
                    if (planet.isOwned()) {
                        continue;
                    }else{ 

                    
                    
                   
                        
                        if (ship.canDock(planet) && plannedPlanets.contains(planet)) {
                            moveList.add(new DockMove(ship, planet));
                            
                            break;
                        } 
                    
                        final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                        
                        
                        if (newThrustMove != null) {
                            
                            moveList.add(newThrustMove);
                            
                            plannedPlanets.add(planet);
                        }
                    
                    break;
                }
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}