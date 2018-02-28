import hlt.*;

import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("DryAgedSteakBot-v7");

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
                
                if(ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) > 108){
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
            ArrayList<Planet> takenPlanets = new ArrayList<Planet>();
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
                
                for (final Ship ship : gameMap.getSortedTeamShipsById()) {
                    botNum++;
                    if(gameMap.getAllShips().size()/4 > gameMap.getMyPlayer().getShips().size() && numOfPlayer == 4){

                        ThrustMove runMove = Navigation.navigateShipTowardsTarget(gameMap, ship, gameMap.nearestHideout(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        if(runMove != null){
                            moveList.add(runMove);
                        }
                        Log.log("Ship" + ship.getId() + " escaping");
                        continue;
                    }

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

                    if(ship.getDistanceTo(enemyShips.get(0)) < 15 || gameMap.isAllPlanetsTaken()){
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
                            
                            int shipsGoingToPlanet = 0;
                            for(Planet takenPlanet : takenPlanets){
                                if(takenPlanet == planet){
                                    shipsGoingToPlanet++;
                                }
                            }
                            
                            if(shipsGoingToPlanet < (planet.getDockingSpots() - planet.getDockedShips().size())){
                                ThrustMove goToDock = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                                //If you can't dock, go to the planet
                                if(goToDock != null){
                                    moveList.add(goToDock);
                                    takenPlanets.add(planet);
                                    Log.log("Ship" + ship.getId() + " moving to" + planet.getId());
                                    break;
                                } 
                            }else{
                                Position closestPointToEnemyShip = ship.getClosestPoint(enemyShips.get(0));
                                ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, closestPointToEnemyShip, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);

                                if(attackMove != null){
                                    moveList.add(attackMove);
                                }
                                Log.log("Ship" + ship.getId() + " attacking ship" + enemyShips.get(0).getId());
                                continue;
                            }
                            
                            
                        }




                    }
                    double endTime = System.currentTimeMillis();
                    if(endTime - startTime > 1900){
                        Log.log("Time out detected, skipping rest of bots. Bots ran: " + botNum +"/" +gameMap.getMyPlayer().getShips().size());
                        break;
                    }
                    
                }
            }
            Networking.sendMoves(moveList);
        }
    }
}
