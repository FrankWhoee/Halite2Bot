import hlt.*;
import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("IgnisUnus-v3");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);
        Ship earlyAggressor = gameMap.getAllShips().get(0);
        double startTime = 0;
        int turnNum = 0;
        
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
        boolean cheesed = false;
        
        
        for (;;) {
            ArrayList<Ship> safeShips = new ArrayList<Ship>();
            Log.log("safeShips cleared");
            turnNum++;
            int botNum = 0;
            moveList.clear();
            Log.log("moveList cleared");
            networking.updateMap(gameMap);
            Record.clear();
            startTime = System.currentTimeMillis();
            if(turnNum == 1 ){
                
                for(Ship ship: gameMap.getMyPlayer().getShips().values()){
                
                    if(ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) < 91){
                        ArrayList<Ship> allTeamShips = new ArrayList<Ship>();
                        for(Ship myShips : gameMap.getAllShips()){
                            if(myShips.getOwner() == gameMap.getMyPlayerId()){
                                allTeamShips.add(myShips);
                            }
                        }
                        earlyAggressor = allTeamShips.get(0);
                        Log.log("earlyAggressor chosen, ship " + earlyAggressor.getId());
                    }
                }
                
                
            }
            
            Log.log("" + earlyAggressor.getId());
            
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
                //Run away if outnumbered
                
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    ArrayList<Ship> vulnerableEnemies = gameMap.getSortedVulnerableEnemyShips(ship);
                    ArrayList<Ship> dangerousEnemyShips = gameMap.getSortedDangerousEnemyShips(ship);
                    ArrayList<Ship> teamShips = gameMap.getUndockedTeamShips(ship);
                    
                    if(dangerousEnemyShips.size() > 0 && ship.getDistanceTo(dangerousEnemyShips.get(0)) < 13 && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                        safeShips.add(ship);
                    }
                    
                    if(dangerousEnemyShips.size() > 0 && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                       int numOfSurroundingEnemyShips = 0;
                       for(Ship enemyShip: dangerousEnemyShips){
                           if(ship.getDistanceTo(enemyShip) < 20){
                               numOfSurroundingEnemyShips++;
                           }
                       }
                       int numOfSurroundingTeamShips = 0;
                       for(Ship teamShip: gameMap.getMyPlayer().getShips().values()){
                           if(ship.getDistanceTo(teamShip) < 14 && teamShip.getDockingStatus() == Ship.DockingStatus.Undocked){
                               numOfSurroundingTeamShips++;
                           }
                       }
                       if(numOfSurroundingEnemyShips >= numOfSurroundingTeamShips){
                            if(safeShips.size() > 0 && ship.getDistanceTo(safeShips.get(0)) < 28){
                                Position retreatPoint = ship.getClosestPoint(safeShips.get(0));
                                
                                ThrustMove retreatMove = Navigation.navigateShipTowardsTarget(gameMap, ship, retreatPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                                moveList.add(retreatMove);
                                continue; 
                            }else{
                                ThrustMove retreatMove = Navigation.retreatMove(ship, gameMap, dangerousEnemyShips.get(0), Constants.MAX_SPEED, Math.PI/180);
                                moveList.add(retreatMove);
                                continue;                                
                            }
                        }                       
                    }
                    
                    //Coward
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
                    ArrayList<Ship> enemyShips = gameMap.getSortedEnemyShips(ship);
                    //Anti-rush system
                    /*
                    
                    if(ship.getDockingStatus() == Ship.DockingStatus.Docked && numOfPlayer == 2){
                        double dockedShips = 0;
                        double undockedShips = 0;
                        for(Ship enemyShip : enemyShips){
                            if(enemyShip.getDockingStatus() == Ship.DockingStatus.Docked){
                                dockedShips ++;
                            }else{
                                undockedShips++;
                            }
                        }
                        if((dockedShips/(undockedShips + dockedShips)) < 0.33 && gameMap.getMyPlayer().getShips().size() < enemyShips.size()){
                            cheesed = true;
                            Log.log("Cheese incoming.");
                        }else{
                            cheesed = false;
                        }
                        
                    }
                    */
                    
                    if(ship.getId() == earlyAggressor.getId()){
                        Position attackPoint;
                        
                        if(vulnerableEnemies.size() > 0){
                            attackPoint = ship.getClosestPoint(vulnerableEnemies.get(0));
                        }else{
                           attackPoint = ship.getClosestPoint(gameMap.getSortedEnemyShips(ship).get(0)); 
                        }
                        
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        moveList.add(attackMove);
                        Log.log("ship " + ship.getId() + " attacking ship " + enemyShips.get(0));
                        continue;
                    }
                    
                    
                    //Attack
                    if(ship.getDistanceTo(enemyShips.get(0)) < 35 || gameMap.isAllPlanetsTaken()){
                        Position attackPoint = ship.getClosestPoint(enemyShips.get(0));
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        moveList.add(attackMove);
                        Log.log("ship " + ship.getId() + " attacking ship " + enemyShips.get(0));
                        continue;
                    }
                    
                    if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                        continue;
                    }
                    
                    //Move to dock
                    for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                        //If you can dock, and there are no enemy ships nearby...
                        if(ship.canDock(planet) && !planet.isFull() && !cheesed){
                            //Dock with this planet
                            moveList.add(new DockMove(ship,planet));
                            Log.log("Ship" + ship.getId() + " docking with" + planet.getId());
                            break;
                        }else if(!planet.isOwned() || (planet.isOwned() && planet.getOwner() == gameMap.getMyPlayerId()) && !planet.isFull() && !cheesed && ship != earlyAggressor){
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
