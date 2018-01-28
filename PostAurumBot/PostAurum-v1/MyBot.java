import hlt.*;
import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("PostAurum-v1");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);
        
        double startTime = 0;
        
        
        final ArrayList<Move> moveList = new ArrayList<>();
        int numOfPlayer = gameMap.getAllPlayers().size();
        boolean cheese = true;
        if(numOfPlayer == 4){
            Log.log("4 player game, no rushing.");
            cheese = false;
        }else{
            for(Ship ship: gameMap.getMyPlayer().getShips().values()){
                
                if(ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) > 96){
                    cheese = false;
                    break;
                }
            }
                
            if(cheese){
                Log.log("2 Player game. Cannon rush activated.");
            }else{
                Log.log("2 player game, enemy ships too far away to succesively rush.");
            }
           
        }
        
        for (;;) {
            int botNum = 0;
            moveList.clear();
            Log.log("moveList cleared");
            networking.updateMap(gameMap);
            Record.clear();
            startTime = System.currentTimeMillis();
            if(cheese){
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    botNum++;
                    ArrayList<Ship> enemyShips = gameMap.getSortedEnemyShips(ship);
                    Position closestPointToEnemyShip = ship.getClosestPoint(enemyShips.get(0));
                    ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, closestPointToEnemyShip, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);

                    if(attackMove != null){
                        moveList.add(attackMove);
                    }
                    Log.log("Ship" + ship.getId() + " attacking ship" + enemyShips.get(0).getId());
                    continue;
                }
            }else{
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    ArrayList<Ship> enemyShips = gameMap.getSortedEnemyShips(ship);
                    
                    int numOfSurroundingEnemyShips = 0;
                    for(Ship enemyShip: enemyShips){
                        if(ship.getDistanceTo(enemyShip) < 35 && enemyShip.getDockingStatus() == Ship.DockingStatus.Undocked){
                            numOfSurroundingEnemyShips++;
                        }
                    }
                    int numOfSurroundingTeamShips = 0;
                    for(Ship teamShips: gameMap.getMyPlayer().getShips().values()){
                        if(ship.getDistanceTo(teamShips) < 35 && teamShips.getDockingStatus() == Ship.DockingStatus.Undocked){
                            numOfSurroundingTeamShips++;
                        }
                    }
                    if(numOfSurroundingEnemyShips >= numOfSurroundingTeamShips){
                        ThrustMove retreatMove = Navigation.retreatMove(ship, gameMap, enemyShips, Constants.MAX_SPEED, Math.PI/180);
                        moveList.add(retreatMove);
                        continue;
                    }
                    
                    if(gameMap.getMyPlayer().getShips().size() < (gameMap.getAllShips().size()/4) - 15){
                        if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                            moveList.add(new UndockMove(ship));
                            continue;
                        }else{
                            ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, gameMap.nearestHideout(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                            moveList.add(attackMove);
                            continue; 
                        }
                        
                    }
                    
                    if(ship.getDockingStatus() != Ship.DockingStatus.Undocked){
                        continue;
                    }
                    
                    if(ship.getDistanceTo(enemyShips.get(0)) < 35 || gameMap.isAllPlanetsTaken()){
                        Position attackPoint = ship.getClosestPoint(enemyShips.get(0));
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        moveList.add(attackMove);
                        continue;
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
            }
            Networking.sendMoves(moveList);
        }
    }
}
