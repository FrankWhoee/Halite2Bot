import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Tamagocchi");

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
                ArrayList<Ship> enemyShips = gameMap.getSortedEnemyShips(ship);
                
                if(ship.getDistanceTo(enemyShips.get(0)) < 35 || gameMap.isAllPlanetsTaken()){
                    //If you are docked...
                    if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                        //and you are in danger...
                        if(ship.getDistanceTo(gameMap.getUndockedTeamShips(ship).get(0)) > (((ship.getHealth() - Constants.WEAPON_DAMAGE * 3)/Constants.WEAPON_DAMAGE) * 7)){
                            //undock to protect yourself.
                            moveList.add(new UndockMove(ship));  
                        }else{
                            //if you are not in danger, continue mining.
                           continue; 
                        }
                    }else{
                        //If you are not docked, attack enemy ships
                        Position closestPointToEnemyShip = ship.getClosestPoint(enemyShips.get(0));
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, closestPointToEnemyShip, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        
                        if(attackMove != null){
                            moveList.add(attackMove);
                        }
                        continue;
                    }
                }
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    //If you can dock, and there are no enemy ships nearby...
                    if(ship.canDock(planet) && !planet.isFull()){
                        //Dock with this planet
                        moveList.add(new DockMove(ship,planet));
                    }else if(!planet.isOwned() || (planet.isOwned() && planet.getOwner() == gameMap.getMyPlayerId())){
                        //If you can't dock, go to the planet
                        ThrustMove goToDock = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                        
                        if(goToDock != null){
                            moveList.add(goToDock);
                        }
                    }
                        
                    
                    
                    
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}
