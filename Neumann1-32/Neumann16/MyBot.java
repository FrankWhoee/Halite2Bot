import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Neumann-v14");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        ArrayList<Planet> plannedPlanets = new ArrayList<Planet>();
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    if (planet.isOwned()) {
                        continue;
                    }

                    if (ship.canDock(planet) && plannedPlanets.contains(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }

                    if (plannedPlanets.contains(planet)) {
                        continue;
                    }else{
                    
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

