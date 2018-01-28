

import hlt.*;

import java.util.*;

public class MyBot {
    
    

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("AN59");

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
                
                if(ship.getDistanceTo(gameMap.getSortedEnemyShips(ship).get(0)) > 91){
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
        
        for (;;) {
            
            Record.clear();
            
            moveList.clear();
            Log.log("moveList cleared.");
            networking.updateMap(gameMap);
            Log.log("gameMap updated");
            turnNum++;
            if(cheese == false){

                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    
                    ArrayList<Ship> sortedEnemyShips = new ArrayList<Ship>();
                    sortedEnemyShips = gameMap.getSortedEnemyShips(ship);
                    
                    
                    //Coward function
                    if(turnNum > 20 && (gameMap.getAllShips().size()/gameMap.getAllPlayers().size()) - 5 > gameMap.getMyPlayer().getShips().size()){
                            
                                    
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
                    for (final Planet planet : gameMap.getSortedPlanets(ship)) {
                        

                        ArrayList<Ship> nearestVulnerableEnemyShips = new ArrayList<Ship>();
                        nearestVulnerableEnemyShips = gameMap.nearestVulnerableEnemyShips(ship);

                        if(ship.canDock(planet) && !planet.isFull() && planet.getHealth() > 500 ){
                            Log.log("Ship [" + ship.getId() +"] docking with planet [" + planet.getId() + "]");
                            moveList.add(new DockMove(ship,planet));
                            break;
                        }else if(gameMap.allPlanetsTaken() || ship.getDistanceTo(sortedEnemyShips.get(0)) < 25){
                            
                                if(nearestVulnerableEnemyShips.isEmpty() == false){
                                    if(ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) < 40 && ship.getDistanceTo(nearestVulnerableEnemyShips.get(0)) > 2){
                                        ThrustMove attackMove;
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
                                
                                if(sortedEnemyShips.size() > 0){
                                    if(ship.getDistanceTo(sortedEnemyShips.get(0)) < 35){
                                        ThrustMove attackMove;
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
                                        ThrustMove attackMove;
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
                            

                        }else if(planet.isOwned() == false){
                            
                            if(ship.getId() % 2 == 0){
                                    ThrustMove attackMove;
                                    Position attackPoint = ship.getClosestPoint(sortedEnemyShips.get(0));
                                    attackMove = Navigation.navigateShipTowardsTarget(gameMap, ship, attackPoint,Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());

                                    //queue up attack move and stop considering planets
                                    if (attackMove != null) {
                                        moveList.add(attackMove);
                                    }
                                    Log.log("Ship [" + ship.getId() +"] attacking other ships because shipId mod 3 is 0. Attacking ship [" + sortedEnemyShips.get(0).getId() + "]");
                                    break;
                                
                                
                            }else{
                                final ThrustMove thrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, 5, Record.getRecord());
                                //queue up thrust move (to dock) and stop considering planets
                                if (thrustMove != null) {
                                    moveList.add(thrustMove);
                                }
                                Log.log("Planet [" + planet.getId() + "] is too far away to dock. Moving towards it with ship[" + ship.getId() + "]");
                                break;
                            }

                        }

                    }

                }
                Networking.sendMoves(moveList);
            }else{
                
                /*
                if(turnNum == 7){
                    int shipsUndocked = 0;
                    for(Ship ship: gameMap.getSortedEnemyShips(gameMap.getMyPlayer().getShips().get(0))){
                        if(ship.getDockingStatus() == Ship.DockingStatus.Undocked){
                            shipsUndocked++;
                        }
                    }
                    if(shipsUndocked > 2){
                        isRushIncoming = true;
                    }
                }
                */
                
                
                for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                    /*
                    if(isRushIncoming){
                        ThrustMove defendMove = Navigation.navigateShipTowardsTarget(gameMap, ship, new Position(5,5),Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180, true, Record.getRecord());
                                //queue up attack move and stop considering planets
                                if (defendMove != null) {
                                    moveList.add(defendMove);
                                }
                                Log.log("Ship [" + ship.getId() +"] detecting rush incoming. Retreating to (5,5)");
                                break; 
                        
                    }else*/
                    
                       ArrayList<Ship> sortedEnemyShips = new ArrayList<Ship>();
                    sortedEnemyShips = gameMap.getSortedEnemyShips(ship);
                    
                    ArrayList<Planet> sortedPlanets = new ArrayList<Planet>();
                    sortedPlanets = gameMap.getSortedPlanets(ship);
                        ThrustMove attackMove;
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

