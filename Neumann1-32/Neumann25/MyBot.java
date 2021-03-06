import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Neumann-v23");

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
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                
                
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    if (ship.canDock(planet) && (planet.isFull() == false) && ((ship.getId() % 5) == 1)) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }else{
                        if (planet.getOwner() == ship.getOwner()) {
                            continue;
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

