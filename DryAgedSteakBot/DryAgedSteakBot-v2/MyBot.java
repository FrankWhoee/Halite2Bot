import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("DryAgedSteakBot-v1");

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
            Log.log("moveList cleared");
            networking.updateMap(gameMap);
            Record.clear();
            if(gameMap.isAllPlanetsTaken()){
                Log.log("All planets taken.");
            }
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                //Import all docked/docking/undocking enemy ships...
                ArrayList<Ship> vulnerableEnemyShips = gameMap.getSortedVulnerableEnemyShips(ship);
                //If all there is a nearby undocked ship...
                
                if(vulnerableEnemyShips.size() > 0){
                    if(ship.getDockingStatus() == Ship.DockingStatus.Undocked && ship.getDistanceTo(vulnerableEnemyShips.get(0)) < 50){
                        //attack that nearby ship.
                        Position closestPointToEnemyShip = ship.getClosestPoint(vulnerableEnemyShips.get(0));
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, closestPointToEnemyShip, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);

                        if(attackMove != null){
                            moveList.add(attackMove);
                        }
                        Log.log("Ship" + ship.getId() + " attacking vulnerable enemy ship" + vulnerableEnemyShips.get(0).getId());
                        continue;
                    }                    
                }

                ArrayList<Ship> enemyShips = gameMap.getSortedEnemyShips(ship);
                
                if(ship.getDistanceTo(enemyShips.get(0)) < 35 || gameMap.isAllPlanetsTaken()){
                    //If you are docked...
                    if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                        //and you are in danger...
                        if(gameMap.getUndockedTeamShips(ship).size() > 0){
                            if(ship.getDistanceTo(gameMap.getUndockedTeamShips(ship).get(0)) > (((ship.getHealth() - Constants.WEAPON_DAMAGE * 3)/Constants.WEAPON_DAMAGE) * 7)){
                                //undock to protect yourself.
                                Log.log("Ship" + ship.getId() + " undocking to attack ship" + enemyShips.get(0).getId());
                                moveList.add(new UndockMove(ship)); 
                                continue;
                            }else{
                                //if you are not in danger, continue mining.
                               continue; 
                            }
                        }else{
                            Log.log("Ship" + ship.getId() + " undocking to attack ship" + enemyShips.get(0).getId());
                            moveList.add(new UndockMove(ship));
                            continue;
                        }
                        
                        
                    }else{
                        //If you are not docked, attack enemy ships
                        Position closestPointToEnemyShip = ship.getClosestPoint(enemyShips.get(0));
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, closestPointToEnemyShip, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        
                        if(attackMove != null){
                            moveList.add(attackMove);
                        }
                        Log.log("Ship" + ship.getId() + " attacking ship" + enemyShips.get(0).getId());
                        continue;
                    }
                }
                for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                    //If you can dock, and there are no enemy ships nearby...
                    if(ship.canDock(planet) && !planet.isFull()){
                        //Dock with this planet
                        moveList.add(new DockMove(ship,planet));
                        Log.log("Ship" + ship.getId() + " docking with" + planet.getId());
                        break;
                    }else if(!planet.isOwned() || (planet.isOwned() && planet.getOwner() == gameMap.getMyPlayerId()) && !planet.isFull()){
                        //If you can't dock, go to the planet
                        ThrustMove goToDock = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                        
                        if(goToDock != null){
                            moveList.add(goToDock);
                            Log.log("Ship" + ship.getId() + " moving to" + planet.getId());
                            break;
                        }
                    }
                        
                    
                    
                    
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}
