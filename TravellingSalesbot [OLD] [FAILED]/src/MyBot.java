import hlt.*;


import java.util.*;
import java.util.Map.Entry;


public class MyBot {
    
    
       

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Ship112-v3");

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

            List<Ship> teamShips = new ArrayList<Ship>();
            teamShips = gameMap.getAllShips();
            
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                
                
                if (ship.getDockingStatus() == Ship.DockingStatus.Docked) {
                    continue;
                }
                
                
                
               Map<Double,Entity> entitiesByDistance = new HashMap<Double,Entity>();
               entitiesByDistance = gameMap.nearbyEntitiesByDistance(ship);
               
               
               //Sorts entitiesByDistance
               List<Entry<Double, Entity>> list = new LinkedList<Entry<Double, Entity>>(entitiesByDistance.entrySet());
               Collections.sort(list,new Comparator<Entry<Double,Entity>>() {
                    @Override
                    public int compare(Entry<Double, Entity> o1, Entry<Double, Entity> o2) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                });
               //Adds all closest empty planets. 
               ArrayList<Entity> closestEmptyPlanets = new ArrayList<Entity>();
               for(double distance: entitiesByDistance.keySet()){
                   if(entitiesByDistance.get(distance) instanceof hlt.Planet && ((Planet)entitiesByDistance.get(distance)).isOwned() == false){
                       closestEmptyPlanets.add(entitiesByDistance.get(distance));
                   }
               }
               //Adds all enemy ships into list
               ArrayList<Entity> closestEnemyShips = new ArrayList<Entity>();
               for(double distance: entitiesByDistance.keySet()){
                   if(entitiesByDistance.get(distance) instanceof hlt.Ship && teamShips.contains(entitiesByDistance.get(distance)) == false){
                        closestEnemyShips.add(entitiesByDistance.get(distance));
                   }
               }
              
               
               if (closestEmptyPlanets.size() > 0){
                    Entity targetPlanet = closestEmptyPlanets.get(0);
       
                   if (ship.canDock((Planet)targetPlanet)){
                       final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, targetPlanet, Constants.MAX_SPEED);
                       
                        
                       
                            
                        if(newThrustMove != null){
                            moveList.add(newThrustMove);
                        }
                        break;    
                            
                     
                        
                        
                   } else{
                       
                       final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, 
                               ship, 
                               ship.getClosestPoint(targetPlanet), 
                               Constants.MAX_SPEED, 
                               false, 
                               Constants.MAX_NAVIGATION_CORRECTIONS, 
                               Math.PI/180);
                        
                        
                        if (newThrustMove != null) {
                            
                            moveList.add(newThrustMove);
                            
                            
                        }
                        Networking.sendMoves(moveList);
                        
                       
                       
                       
                   }

               } /*else if(closestEnemyShips.size() > 0){
                   Ship targetShip = (Ship)closestEnemyShips.get(0);
                   final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, 
                               ship, 
                               ship.getClosestPoint(targetShip), 
                               Constants.MAX_SPEED, 
                               false, 
                               5, 
                               0);
                        
                        
                        if (newThrustMove != null) {
                            
                            moveList.add(newThrustMove);
                            
                            
                        }
                        Networking.sendMoves(moveList);
                        
                       
               }
               */ 
               Networking.sendMoves(moveList);
                
                
               
            }
        }
    }
}