import hlt.*;
import java.util.*;

public class MyBot {
    
    

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("SharpVintageCheeseBot-v24");
        Log.log("This is bot SharpVintageCheeseBot-v17 intializing.");
        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);
        
        final ArrayList<Move> moveList = new ArrayList<>();
        Planet largestPlanet = gameMap.getPlanet(0);
        for(int i = 0; i < gameMap.getAllPlanets().size(); i++){
            if(gameMap.getPlanet(i).getRadius() > largestPlanet.getRadius()){
                largestPlanet = gameMap.getPlanet(i);
            }
        }
        int numOfPlayer = gameMap.getAllPlayers().size();
        boolean cheese = true;
        if(numOfPlayer == 4){
            Log.log("4 player game, no rushing.");
            cheese = false;
        }else{
            for(Ship ship: gameMap.getMyPlayer().getShips().values()){
                
                if(ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) > 180){
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
        boolean isRushIncoming = false;
        int turnNum = 0;
        ArrayList<Position> plannedHideouts = new ArrayList<Position>();
        ArrayList<Planet> plannedPlanets = new ArrayList<Planet>();
        for (;;) {
            Log.log("Has reached top of loop.");
            Record.clear();
            moveList.clear();
            Log.log("moveList cleared.");
            networking.updateMap(gameMap);
            Log.log("gameMap updated");
            plannedPlanets.clear();
            Log.log("plannedPlanets cleared.");
            turnNum++;
            
            if(cheese == false){
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    ArrayList<Ship> sortedEnemyShips = gameMap.getSortedEnemyShips(ship);
                    /*
                    if(turnNum == 6 && numOfPlayer == 2){
                        int undockedShips = 0;
                        for(Ship ships : sortedEnemyShips){
                            if(ships.getDockingStatus() == Ship.DockingStatus.Undocked){
                                undockedShips++;
                            }
                        }
                        if(undockedShips >= 2){
                            isRushIncoming = true;
                            Log.log("Rush detected. May Spaghetti Monster help us all.");
                        }
                    }
                    */
                    //Coward function
                    if((turnNum > 10 && (gameMap.getAllShips().size()/gameMap.getAllPlayers().size()) - (turnNum/15) > gameMap.getMyPlayer().getShips().size() && (numOfPlayer == 4)) || isRushIncoming){  
                            if(ship.getDockingStatus() == Ship.DockingStatus.Docked){
                                moveList.add(new UndockMove(ship));
                                Log.log("Ship[" + ship.getId() +"] undocking.");
                                continue;
                            }else{
                            
                            ArrayList<Position> closestHideouts = gameMap.getClosestHideouts(ship);
                            for(Position hideout : closestHideouts){
                                
                                if(plannedHideouts.contains(hideout) == false){
                                    ThrustMove runMove;
                                    runMove = Navigation.navigateShipTowardsTarget(gameMap, ship, hideout, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, false, Record.getRecord());
                                    //queue up run move and get the heck out of dodge
                                    if(runMove != null){
                                        moveList.add(runMove);
                                    }
                                    plannedHideouts.remove(hideout);
                                    Log.log("Ship[" + ship.getId() +"] moving towards hideout at (" + hideout.getXPos() +"," + hideout.getYPos() + ")");
                                    break;
                                }
                                
                                
                            }
                             continue;   
                             
                            }   
                    }
                    if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
                        continue;
                    }
                    
                    
                    
                    //Iterate every planet
                    for (final Planet planet : gameMap.getSortedPlanets(ship)){
                        ArrayList<Ship> nearestVulnerableEnemyShips = gameMap.nearestVulnerableEnemyShips(ship);
                        ArrayList<Ship> dangerousEnemyShips = gameMap.getSortedNearbyDangerousEnemyShips(ship);
                        if(ship.getDistanceTo(sortedEnemyShips.get(0)) < 30|| gameMap.allPlanetsTaken()){
                                if(sortedEnemyShips.size() > 0){
                                    ThrustMove attackMove = Navigation.planetSpiralMove(ship, sortedEnemyShips.get(0), gameMap);
                                    if (attackMove != null && (sortedEnemyShips.get(0).getDockingStatus() != Ship.DockingStatus.Undocked)) {
                                        moveList.add(attackMove);
                                        break;
                                    }
                                    if(ship.getDistanceTo(sortedEnemyShips.get(0)) < 35){
                                        
                                        Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                                        attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());

                                        //queue up attack move and stop considering planets
                                        if (attackMove != null) {
                                            moveList.add(attackMove);
                                        }
                                        Log.log("Ship [" + ship.getId() +"] detecting enemy within aggro range. Attacking ship [" + sortedEnemyShips.get(0).getId() + "]");
                                        break;
                                    }
                                    if(gameMap.allPlanetsTaken() ){
                                        
                                        Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                                        attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());
                                        //queue up attack move and stop considering planets
                                        if (attackMove != null) {
                                            moveList.add(attackMove);
                                        }
                                        Log.log("All planets taken. Attacking ship [" + sortedEnemyShips.get(0).getId() +"]. Attacking with ship [" + ship.getId() + "]");
                                        break;
                                    }
                                }
                                if(nearestVulnerableEnemyShips.isEmpty() == false){
                                    ThrustMove attackMove = Navigation.planetSpiralMove(ship, nearestVulnerableEnemyShips.get(0), gameMap);
                                    if (attackMove != null) {
                                        moveList.add(attackMove);
                                        break;
                                    }
                                    if(ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) < 100){
                                        Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                                        //If there are ships that are docking or undocking, attack them first.
                                        attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());
                                        //queue up attack move and stop considering planets
                                        if (attackMove != null) {
                                            moveList.add(attackMove);
                                        }
                                        Log.log("Ship [" + ship.getId() +"] detecting vulnerable enemy. Attacking ship [" + nearestVulnerableEnemyShips.get(0).getId() + "]");
                                        break;
                                    }
                                }
                                
                        }else if(ship.canDock(planet) && !planet.isFull()){
                            Log.log("Ship [" + ship.getId() +"] docking with planet [" + planet.getId() + "]");
                            moveList.add(new DockMove(ship,planet));
                            break;
                        }else if(!planet.isFull() && (planet.getOwner() == gameMap.getMyPlayerId() || !planet.isOwned())){
                            int instancesofPlanet = 0;
                            for(Planet planets : plannedPlanets){
                                if(planets == planet){
                                    instancesofPlanet++;
                                }
                            }
                                    
                            if(instancesofPlanet < planet.getDockingSpots()){
                                final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED, Record.getRecord());
                                //queue up thrust move (to dock) and stop considering planets
                                if (thrustMove != null) {
                                    moveList.add(thrustMove);
                                    plannedPlanets.add(planet);
                                    Log.log("Planet [" + planet.getId() + "] is too far away to dock. Moving towards it with ship[" + ship.getId() + "]");
                                    break;
                                }
                                    
                                    
                            }else{
                                ThrustMove attackMove;
                                Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                                attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());

                                //queue up attack move and stop considering planets
                                if (attackMove != null) {
                                    moveList.add(attackMove);
                                }
                                Log.log("Ship [" + ship.getId() +"] attacking ship [" + sortedEnemyShips.get(0).getId() + "]");
                                break;
                            }
                        }
                    }
                }
                Networking.sendMoves(moveList);
                
            }else{
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    ArrayList<Ship> sortedEnemyShips = new ArrayList<Ship>();
                    ArrayList<Ship> nearestVulnerableEnemyShips = gameMap.nearestVulnerableEnemyShips(ship);
                    sortedEnemyShips = gameMap.getSortedEnemyShips(ship);
                    if(nearestVulnerableEnemyShips.isEmpty() == false){
                        ThrustMove attackMove = Navigation.planetSpiralMove(ship, nearestVulnerableEnemyShips.get(0), gameMap);
                        if (attackMove != null) {
                            moveList.add(attackMove);
                            break;
                        }
                        if(ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) < 100){
                            Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                            //If there are ships that are docking or undocking, attack them first.
                            attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());
                            //queue up attack move and stop considering planets
                            if (attackMove != null) {
                                moveList.add(attackMove);

                            }
                            Log.log("Ship [" + ship.getId() +"] detecting vulnerable enemy. Attacking ship [" + nearestVulnerableEnemyShips.get(0).getId() + "]");
                            break;
                        }
                    }                                                
                    
                    
                    ThrustMove attackMove = Navigation.planetSpiralMove(ship, sortedEnemyShips.get(0), gameMap);
                    if (attackMove != null && (sortedEnemyShips.get(0).getDockingStatus() != Ship.DockingStatus.Undocked)) {
                        moveList.add(attackMove);
                        continue;
                    }
                    
                    ArrayList<Planet> sortedPlanets = new ArrayList<Planet>();
                    sortedPlanets = gameMap.getSortedPlanets(ship);
                    
                    
                    
                    Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                    attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());
                    //queue up attack move and stop considering planets
                    if (attackMove != null) {
                        moveList.add(attackMove);
                    }
                    Log.log(" Attacking ship [" + sortedEnemyShips.get(0).getId() +"]. Attacking with ship [" + ship.getId() + "]");
                    continue;  
                }
                Networking.sendMoves(moveList);
                Log.log("Sending moveList to STDOUT");
            }
        }
    }
}

