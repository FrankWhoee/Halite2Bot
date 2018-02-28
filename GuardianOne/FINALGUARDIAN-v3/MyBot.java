import hlt.*;
import java.util.ArrayList;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("GuardianONE-v2");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log("MYBOT.main: " + initialMapIntelligence);
        double startTime = 0;
        int turnNum = 0;
        final ArrayList<Move> moveList = new ArrayList<>();
        int numOfPlayer = gameMap.getAllPlayers().size();
        for (;;) {
        	ArrayList<Planet> takenPlanets = new ArrayList<Planet>();
            ArrayList<Ship> safeShips = new ArrayList<Ship>();
            ArrayList<Ship> takenEnemyShips = new ArrayList<Ship>();
            Log.log("MYBOT.main: " + "safeShips cleared");
            turnNum++;
            int botNum = 0;
            moveList.clear();
            Log.log("MYBOT.main: " + "moveList cleared");
            networking.updateMap(gameMap);
            Record.clear();
            startTime = System.currentTimeMillis();
                ArrayList<Ship> allTeamShips = gameMap.getTeamShipsSortedByShipId();
                for (final Ship ship : allTeamShips) {
                	botNum++;
                	double endTime = System.currentTimeMillis();
                	if((((endTime - startTime)/botNum) + (endTime - startTime)) > 1950) {
                		Log.log("MYBOT.main: TIMEOUT DETECTED. SKIPPING REST OF BOTS. " + botNum +"/" + allTeamShips.size() + " ran.");
                		break;
                	}
                    //Import lists so you don't have to sort several times.
                    
                    ArrayList<Ship> dangerousEnemyShips = gameMap.getSortedDangerousEnemyShips(ship);
                    //If you are safe, add yourself to the list of safe ships.
                    if(dangerousEnemyShips.size() > 0 && ship.getDistanceTo(dangerousEnemyShips.get(0)) < 13 && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                        safeShips.add(ship);
                    }
                    
                    
                    if(dangerousEnemyShips.size() > 0 && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                    	//Count number of nearby enemy ships
                    	int numOfSurroundingEnemyShips = 0;
                    	for(Ship enemyShip: dangerousEnemyShips){
                    		if(ship.getDistanceTo(enemyShip) < Constants.ENEMY_SCAN_RANGE){
                    			numOfSurroundingEnemyShips++;
                    		}
                    	}
                    	
                    	//Count number of nearby allied ships.
                    	int numOfSurroundingTeamShips = 0;
                    	for(Ship teamShip: gameMap.getMyPlayer().getShips().values()){
                    		if(ship.getDistanceTo(teamShip) < Constants.ALLY_SCAN_RANGE && teamShip.getDockingStatus() == Ship.DockingStatus.Undocked){
                    			numOfSurroundingTeamShips++;
                    		}
                    	}
                    	
                    	//If you are outnumbered...
                    	if(numOfSurroundingEnemyShips >= numOfSurroundingTeamShips && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                    		//Find the nearest safe ship
                    		Ship nearestSafeShip = null; 
                    		if(safeShips.size() > 0){
                    			nearestSafeShip = safeShips.get(0);
                                for(Ship safeShip: safeShips){
                                	if(ship.getDistanceTo(safeShip) < ship.getDistanceTo(nearestSafeShip) && safeShip != ship){
                                        nearestSafeShip = safeShip;
                                    }
                                }
                            }
                    		//Thrust towards the nearest safe ship.
                            if(safeShips.size() > 0 && ship.getDistanceTo(nearestSafeShip) < Constants.MAXIMUM_SAFE_SHIP_TRAVEL_DISTANCE && nearestSafeShip != ship){
                            	Position retreatPoint = ship.getClusterPoint(nearestSafeShip);
                                ThrustMove retreatMove = Navigation.navigateShipTowardsTarget(gameMap, ship, retreatPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                                if(retreatMove != null){
                                    moveList.add(retreatMove);
                                    continue;
                                }
                            }else{
                            	ArrayList<Ship> teamShips = gameMap.getUndockedTeamShips(ship);
                            	//If there are no safe ships nearby... 
                            	if(teamShips.size() > 0 && ship.getDistanceTo(teamShips.get(0)) < Constants.MAX_TRAVEL_DISTANCE && ship.getDistanceTo(teamShips.get(0)) > Constants.MINIMUM_DISTANCE_TO_SHIP) {
                            		//Move to the nearest ship if it's not too far away...
                            		
                            		//Search for any dangerous enemies between this ship and the nearest allied ship.
                            		ArrayList<Entity> objectsBetween = gameMap.objectsBetween(ship, teamShips.get(0));
                            		boolean isEnemyInBetween = false;
                            		for(Entity entity : objectsBetween) {
                            			if(entity.getOwner() != gameMap.getMyPlayerId() && entity instanceof Ship) {
                            				Ship thisEntity = (Ship)entity;
                            				if(thisEntity.getDockingStatus() == Ship.DockingStatus.Undocked) {
                            					isEnemyInBetween = true;
                                				break;
                            				}
                            				
                            			}
                            		}
                            		
                            		if(!isEnemyInBetween) {
                            			//...and there are no enemies in between, move to the allied ship.
                            			Position retreatPoint = ship.getClusterPoint(teamShips.get(0));
                            			ThrustMove retreatMove = Navigation.navigateShipTowardsTarget(gameMap, ship, retreatPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                                        if(retreatMove != null){
                                            moveList.add(retreatMove);
                                            continue;
                                        }
                            		}
                            	}else {
                            		//...otherwise, retreat from the nearest enemy by thrusting in the opposite direction.
                            		ThrustMove retreatMove = Navigation.retreatMove(ship, gameMap, dangerousEnemyShips.get(0), Constants.MAX_SPEED, Math.PI/180);
                                    if(retreatMove != null){
                                        moveList.add(retreatMove);
                                        continue;
                                    }
                            	}
                                
                            }
                        }
                    }
                    
                    //Run away if the game is lost. Getting 2nd place is better than 3rd or fourth. Only do this in a four player game.
                    if(gameMap.getMyPlayer().getShips().size() < (gameMap.getAllShips().size()/4) - Constants.COWARD_OFFSET && numOfPlayer == 4){
                        if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                            moveList.add(new UndockMove(ship));
                            continue;
                        }else{
                            ThrustMove runMove = Navigation.navigateShipTowardsTarget(gameMap, ship, gameMap.nearestHideout(ship), Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                            if(runMove != null){
                                moveList.add(runMove);
                                continue;
                            }
                             
                        }
                        
                    }
                    
                    //Import all enemy ships.
                    ArrayList<Ship> enemyShips = gameMap.getSortedEnemyShips(ship);

                    //Attack nearest enemy.
                    boolean willAttack = false;
                    if(ship.getDistanceTo(enemyShips.get(0)) < Constants.AGGRO_RANGE || gameMap.isAllPlanetsTaken() && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                    	for(Ship enemyShip: enemyShips) {
                    		int assignedShips = 0;
                        	for(Ship enemy : takenEnemyShips) {
                        		if(enemyShip == enemy) {
                        			assignedShips++;
                        		}
                        	}
                        	if(assignedShips <= 2 && ship.getDistanceTo(enemyShip) < 35) {
                        		Position attackPoint = ship.getClosestPoint(enemyShip);
                                ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                                if(attackMove != null){
                                    moveList.add(attackMove);
                                    Log.log("MYBOT.main: " + "ship " + ship.getId() + " attacking ship " + enemyShip.getId());
                                    willAttack = true;
                                    break;              
                                }
                        	}
                    	}
                    }
                    
                    if(gameMap.isAllPlanetsTaken() && !willAttack) {
                    	Position attackPoint = ship.getClosestPoint(enemyShips.get(0));
                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                        if(attackMove != null){
                            moveList.add(attackMove);
                            Log.log("MYBOT.main: " + "ship " + ship.getId() + " attacking ship " + enemyShips.get(0).getId());
                            willAttack = true;
                            break;              
                        }
                    }
                    
                    if(!willAttack && !gameMap.isAllPlanetsTaken()) {
                    	for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                            //If you can dock, and there are no enemy ships nearby...
                            if(ship.canDock(planet) && !planet.isFull()){
                                //Dock with this planet
                            	takenPlanets.add(planet);
                                moveList.add(new DockMove(ship,planet));
                                Log.log("MYBOT.main: " + "Ship" + ship.getId() + " docking with planet " + planet.getId());
                                break;
                            }else if(!planet.isOwned() || (planet.isOwned() && planet.getOwner() == gameMap.getMyPlayerId()) && !planet.isFull() && ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                            	int assignedShips = 0;
                            	for(Planet takenPlanet : takenPlanets) {
                            		if(takenPlanet == planet) {
                            			assignedShips++;
                            		}
                            	}
                            	
                            	if(turnNum > 5) {
                            		if(assignedShips < (planet.getDockingSpots() - planet.getDockedShips().size() + Constants.NUM_OF_GUARD_SHIPS)) {
                                		//If you can't dock, go to the planet
                                        ThrustMove goToDock = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

                                        if(goToDock != null){
                                            moveList.add(goToDock);
                                            takenPlanets.add(planet);
                                            Log.log("Ship" + ship.getId() + " moving to planet" + planet.getId());
                                            break;
                                        }
                                	}else{
                                		Position attackPoint = ship.getClosestPoint(enemyShips.get(0));
                                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                                        if(attackMove != null){
                                        	moveList.add(attackMove);
                                            Log.log("ship " + ship.getId() + " attacking ship" + enemyShips.get(0).getId());
                                            break;              
                                        }
                                	}
                            	}else {
                            		if(assignedShips < (planet.getDockingSpots() - planet.getDockedShips().size())) {
                                		//If you can't dock, go to the planet
                                        ThrustMove goToDock = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);

                                        if(goToDock != null){
                                            moveList.add(goToDock);
                                            takenPlanets.add(planet);
                                            Log.log("Ship" + ship.getId() + " moving to planet" + planet.getId());
                                            break;
                                        }
                                	}else{
                                		Position attackPoint = ship.getClosestPoint(enemyShips.get(0));
                                        ThrustMove attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180);
                                        if(attackMove != null){
                                        	moveList.add(attackMove);
                                            Log.log("ship " + ship.getId() + " attacking ship" + enemyShips.get(0).getId());
                                            break;              
                                        }
                                	}
                            	}
                            	
                            }
                        }
                    }
                    
                    
                    //To prevent static ships from colliding with other ships.
                    if(!Record.getRecord().containsKey(ship)) {
                    	Record.storeVectors(ship, Velocity.calculateVelocity(0, 0));
                    	Log.log("No moves issued.");
                    }
                    
                    
                }
            
            Networking.sendMoves(moveList);
        }
    }
}
