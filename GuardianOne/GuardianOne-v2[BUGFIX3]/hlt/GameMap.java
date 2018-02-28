package hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Collection;

public class GameMap {
    private final int width, height;
    private static int playerId;
    private final List<Player> players;
    private final List<Player> playersUnmodifiable;
    private final Map<Integer, Planet> planets;
    private final List<Ship> allShips;
    private final List<Ship> allShipsUnmodifiable;

    // used only during parsing to reduce memory allocations
    private final List<Ship> currentShips = new ArrayList<>();

    public GameMap(final int width, final int height, final int playerId) {
        this.width = width;
        this.height = height;
        this.playerId = playerId;
        players = new ArrayList<>(Constants.MAX_PLAYERS);
        playersUnmodifiable = Collections.unmodifiableList(players);
        planets = new TreeMap<>();
        allShips = new ArrayList<>();
        allShipsUnmodifiable = Collections.unmodifiableList(allShips);
    }
    
    public boolean isAllPlanetsTaken(){
        boolean isAllPlanetsTaken = true;
        for(Planet planet: planets.values()){
            if(!planet.isOwned()){
                isAllPlanetsTaken = false;
                return isAllPlanetsTaken;
            }
        }
        return isAllPlanetsTaken;
    }
    
    public Position nearestHideout(Ship ship){
        ArrayList<Position> hideouts= new ArrayList<Position>();
        hideouts.add(new Position(5,5));
        hideouts.add(new Position(width - 5,height - 5));
        hideouts.add(new Position(width - 5,5));
        hideouts.add(new Position(5,height - 5));
        
        Position nearestHideout = new Position(5,5);
        for(Position hideout: hideouts){
            if(ship.getDistanceTo(hideout) < ship.getDistanceTo(nearestHideout)){
                nearestHideout = hideout;
            }
        }
        return nearestHideout;
    }
    
    public ArrayList<Planet> getSortedPlanets(Ship ship){
        ArrayList<Planet> sortedPlanets = new ArrayList<>();
        Planet removed;
        sortedPlanets = new ArrayList<>();
        for(Planet planet: getAllPlanets().values()){
            sortedPlanets.add(planet);
        }
        int index;
        for(int k = 0; k < sortedPlanets.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedPlanets.size(); i++){
                if(sortedPlanets.get(index).getDistanceTo(ship) > sortedPlanets.get(i).getDistanceTo(ship)){
                    index = i;
                }else if(sortedPlanets.get(index).getDistanceTo(ship) == sortedPlanets.get(i).getDistanceTo(ship)){
                    if(sortedPlanets.get(index).getRadius() > sortedPlanets.get(i).getRadius()){
                    index = i;
                    }else if(sortedPlanets.get(index).getRadius() == sortedPlanets.get(i).getRadius()){
                        if(sortedPlanets.get(index).getHealth() > sortedPlanets.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedPlanets.get(k);
            sortedPlanets.set(k, sortedPlanets.get(index));
            sortedPlanets.set(index, removed);
        }
      return sortedPlanets;  
    }
    
    public ArrayList<Ship> getSortedEnemyShips(Ship ship){
        ArrayList<Ship> sortedEnemyShips = new ArrayList<>();
        Ship removed;
        sortedEnemyShips = new ArrayList<>();
        for(Ship allShips: getAllShips()){
            if(allShips.getOwner() != playerId){
                sortedEnemyShips.add(allShips);
            }     
        }
        int index;
        for(int k = 0; k < sortedEnemyShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedEnemyShips.size(); i++){
                if(sortedEnemyShips.get(index).getDistanceTo(ship) > sortedEnemyShips.get(i).getDistanceTo(ship)){
                    index = i;
                }else if(sortedEnemyShips.get(index).getDistanceTo(ship) == sortedEnemyShips.get(i).getDistanceTo(ship)){
                    if(sortedEnemyShips.get(index).getRadius() > sortedEnemyShips.get(i).getRadius()){
                    index = i;
                    }else if(sortedEnemyShips.get(index).getRadius() == sortedEnemyShips.get(i).getRadius()){
                        if(sortedEnemyShips.get(index).getHealth() > sortedEnemyShips.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedEnemyShips.get(k);
            sortedEnemyShips.set(k, sortedEnemyShips.get(index));
            sortedEnemyShips.set(index, removed);
        }
      return sortedEnemyShips;  
    }
    
    public ArrayList<Ship> getUndockedTeamShips(Ship ship){
        ArrayList<Ship> sortedTeamShips = new ArrayList<>();
        Ship removed;
        sortedTeamShips = new ArrayList<>();
        for(Ship allShips: getMyPlayer().getShips().values()){
            if(allShips.getDockingStatus() == Ship.DockingStatus.Undocked && ship.getId() != allShips.getId()){
                sortedTeamShips.add(allShips);
            }
        }
        int index;
        for(int k = 0; k < sortedTeamShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedTeamShips.size(); i++){
                if(sortedTeamShips.get(index).getDistanceTo(ship) > sortedTeamShips.get(i).getDistanceTo(ship)){
                    index = i;
                }else if(sortedTeamShips.get(index).getDistanceTo(ship) == sortedTeamShips.get(i).getDistanceTo(ship)){
                    if(sortedTeamShips.get(index).getRadius() > sortedTeamShips.get(i).getRadius()){
                    index = i;
                    }else if(sortedTeamShips.get(index).getRadius() == sortedTeamShips.get(i).getRadius()){
                        if(sortedTeamShips.get(index).getHealth() > sortedTeamShips.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedTeamShips.get(k);
            sortedTeamShips.set(k, sortedTeamShips.get(index));
            sortedTeamShips.set(index, removed);
        }
      return sortedTeamShips;  
    }
    
    public ArrayList<Ship> getTeamShipsSortedByShipId(){
        ArrayList<Ship> sortedTeamShips = new ArrayList<>();
        Ship removed;
        sortedTeamShips = new ArrayList<>();
        for(Ship allShips: getMyPlayer().getShips().values()){
            if(allShips.getDockingStatus() == Ship.DockingStatus.Undocked){
                sortedTeamShips.add(allShips);
            }
        }
        int index;
        for(int k = 0; k < sortedTeamShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedTeamShips.size(); i++){
                if(sortedTeamShips.get(index).getId() > sortedTeamShips.get(i).getId()){
                    index = i;
                }else if(sortedTeamShips.get(index).getId() == sortedTeamShips.get(i).getId()){
                    if(sortedTeamShips.get(index).getRadius() > sortedTeamShips.get(i).getRadius()){
                    index = i;
                    }else if(sortedTeamShips.get(index).getRadius() == sortedTeamShips.get(i).getRadius()){
                        if(sortedTeamShips.get(index).getHealth() > sortedTeamShips.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedTeamShips.get(k);
            sortedTeamShips.set(k, sortedTeamShips.get(index));
            sortedTeamShips.set(index, removed);
        }
      return sortedTeamShips;  
    }
    
    public ArrayList<Ship> getSortedVulnerableEnemyShips(Ship ship){
        ArrayList<Ship> sortedEnemyShips = new ArrayList<>();
        Ship removed;
        sortedEnemyShips = new ArrayList<>();
        for(Ship allShips: getAllShips()){
            if(allShips.getOwner() != playerId && allShips.getDockingStatus() != Ship.DockingStatus.Undocked){
                sortedEnemyShips.add(allShips);
            }     
        }
        int index;
        for(int k = 0; k < sortedEnemyShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedEnemyShips.size(); i++){
                if(sortedEnemyShips.get(index).getDistanceTo(ship) > sortedEnemyShips.get(i).getDistanceTo(ship)){
                    index = i;
                }else if(sortedEnemyShips.get(index).getDistanceTo(ship) == sortedEnemyShips.get(i).getDistanceTo(ship)){
                    if(sortedEnemyShips.get(index).getRadius() > sortedEnemyShips.get(i).getRadius()){
                    index = i;
                    }else if(sortedEnemyShips.get(index).getRadius() == sortedEnemyShips.get(i).getRadius()){
                        if(sortedEnemyShips.get(index).getHealth() > sortedEnemyShips.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedEnemyShips.get(k);
            sortedEnemyShips.set(k, sortedEnemyShips.get(index));
            sortedEnemyShips.set(index, removed);
        }
      return sortedEnemyShips;  
    }

    public ArrayList<Ship> getSortedDangerousEnemyShips(Ship ship){
        ArrayList<Ship> sortedEnemyShips = new ArrayList<>();
        Ship removed;
        sortedEnemyShips = new ArrayList<>();
        for(Ship allShips: getAllShips()){
            if(allShips.getOwner() != playerId && allShips.getDockingStatus() == Ship.DockingStatus.Undocked){
                sortedEnemyShips.add(allShips);
            }     
        }
        int index;
        for(int k = 0; k < sortedEnemyShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedEnemyShips.size(); i++){
                if(sortedEnemyShips.get(index).getDistanceTo(ship) > sortedEnemyShips.get(i).getDistanceTo(ship)){
                    index = i;
                }else if(sortedEnemyShips.get(index).getDistanceTo(ship) == sortedEnemyShips.get(i).getDistanceTo(ship)){
                    if(sortedEnemyShips.get(index).getRadius() > sortedEnemyShips.get(i).getRadius()){
                    index = i;
                    }else if(sortedEnemyShips.get(index).getRadius() == sortedEnemyShips.get(i).getRadius()){
                        if(sortedEnemyShips.get(index).getHealth() > sortedEnemyShips.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedEnemyShips.get(k);
            sortedEnemyShips.set(k, sortedEnemyShips.get(index));
            sortedEnemyShips.set(index, removed);
        }
      return sortedEnemyShips;  
    }
    
    public ArrayList<Ship> getSortedEnemyShipsByHealth(Ship ship){
        ArrayList<Ship> sortedEnemyShips = new ArrayList<>();
        Ship removed;
        sortedEnemyShips = new ArrayList<>();
        for(Ship allShips: getAllShips()){
            if(allShips.getOwner() != playerId && allShips.getDockingStatus() == Ship.DockingStatus.Undocked){
                sortedEnemyShips.add(allShips);
            }     
        }
        int index;
        for(int k = 0; k < sortedEnemyShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedEnemyShips.size(); i++){
                if(sortedEnemyShips.get(index).getHealth() > sortedEnemyShips.get(i).getHealth()){
                    index = i;
                }else if(sortedEnemyShips.get(index).getHealth() == sortedEnemyShips.get(i).getHealth()){
                    if(sortedEnemyShips.get(index).getRadius() > sortedEnemyShips.get(i).getRadius()){
                    index = i;
                    }else if(sortedEnemyShips.get(index).getRadius() == sortedEnemyShips.get(i).getRadius()){
                        if(sortedEnemyShips.get(index).getHealth() > sortedEnemyShips.get(i).getHealth()){
                            index = i;
                        }
                    }
                }
            }
            removed = sortedEnemyShips.get(k);
            sortedEnemyShips.set(k, sortedEnemyShips.get(index));
            sortedEnemyShips.set(index, removed);
        }
      return sortedEnemyShips;  
    }
    


    
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getMyPlayerId() {
        return playerId;
    }

    public List<Player> getAllPlayers() {
        return playersUnmodifiable;
    }

    public Player getMyPlayer() {
        return getAllPlayers().get(getMyPlayerId());
    }

    public Ship getShip(final int playerId, final int entityId) throws IndexOutOfBoundsException {
        return players.get(playerId).getShip(entityId);
    }

    public Planet getPlanet(final int entityId) {
        return planets.get(entityId);
    }

    public Map<Integer, Planet> getAllPlanets() {
        return planets;
    }

    public List<Ship> getAllShips() {
        return allShipsUnmodifiable;
    }
    
    public ArrayList<Entity> objectsBetween(Position start, Position target) {
        final ArrayList<Entity> entitiesFound = new ArrayList<>();

        addEntitiesBetween(entitiesFound, start, target, planets.values());
        addEntitiesBetween(entitiesFound, start, target, allShips);

        return entitiesFound;
    }

    private static void addEntitiesBetween(final List<Entity> entitiesFound,
                                           final Position start, final Position target,
                                           final Collection<? extends Entity> entitiesToCheck) {

        for (final Entity entity : entitiesToCheck) {
        	
        	
        	if(entity instanceof Ship) {
            	Ship ship = (Ship)entity;
            	if(ship.getDockingStatus() == Ship.DockingStatus.Undocked) {
            		continue;
            	}
            }
        	
        	
        	if (entity.equals(start) || entity.equals(target)) {
                continue;
            }
            
            
            if (Collision.segmentCircleIntersect(start, target, entity, Constants.FORECAST_FUDGE_FACTOR)) {
                entitiesFound.add(entity);
            }
        }
    }

    public Map<Double, Entity> nearbyEntitiesByDistance(final Entity entity) {
        final Map<Double, Entity> entityByDistance = new TreeMap<>();

        for (final Planet planet : planets.values()) {
            if (planet.equals(entity)) {
                continue;
            }
            entityByDistance.put(entity.getDistanceTo(planet), planet);
        }

        for (final Ship ship : allShips) {
            if (ship.equals(entity)) {
                continue;
            }
            entityByDistance.put(entity.getDistanceTo(ship), ship);
        }

        return entityByDistance;
    }

    public GameMap updateMap(final Metadata mapMetadata) {
        final int numberOfPlayers = MetadataParser.parsePlayerNum(mapMetadata);

        players.clear();
        planets.clear();
        allShips.clear();

        // update players info
        for (int i = 0; i < numberOfPlayers; ++i) {
            currentShips.clear();
            final Map<Integer, Ship> currentPlayerShips = new TreeMap<>();
            final int playerId = MetadataParser.parsePlayerId(mapMetadata);

            final Player currentPlayer = new Player(playerId, currentPlayerShips);
            MetadataParser.populateShipList(currentShips, playerId, mapMetadata);
            allShips.addAll(currentShips);

            for (final Ship ship : currentShips) {
                currentPlayerShips.put(ship.getId(), ship);
            }
            players.add(currentPlayer);
        }

        final int numberOfPlanets = Integer.parseInt(mapMetadata.pop());

        for (int i = 0; i < numberOfPlanets; ++i) {
            final List<Integer> dockedShips = new ArrayList<>();
            final Planet planet = MetadataParser.newPlanetFromMetadata(dockedShips, mapMetadata);
            planets.put(planet.getId(), planet);
        }

        if (!mapMetadata.isEmpty()) {
            throw new IllegalStateException("Failed to parse data from Halite game engine. Please contact maintainers.");
        }

        return this;
    }
}
