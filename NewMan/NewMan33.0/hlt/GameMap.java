package hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Collection;

public class GameMap {
    private final int width, height;
    private final int playerId;
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
    
    public boolean allPlanetsTaken() {
        for(Planet planet: planets.values()){
            if(planet.isOwned() == false){
                return false;
            }
        }
        return true;
    }
    
    public ArrayList<Ship> nearestVulnerableEnemyShips(Ship thisShip) {
        ArrayList<Ship> sortedShips = new ArrayList<>();
        ArrayList<Ship> myShips = new ArrayList<>();
        Ship removed;
        sortedShips = new ArrayList<>();
        for(Ship ship: getMyPlayer().getShips().values()){
            myShips.add(ship);
        }
        
        for(Ship ship: getAllShips()){
            if(!myShips.contains(ship)){
                sortedShips.add(ship);
            }
        }
        int index;
        for(int k = 0; k < sortedShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedShips.size(); i++){
                if(sortedShips.get(index).getDistanceTo(thisShip) > sortedShips.get(i).getDistanceTo(thisShip)){
                    index = i;
                }
            }
            removed = sortedShips.get(k);
            sortedShips.set(k, sortedShips.get(index));
            sortedShips.set(index, removed);
        }
        
        for(int i = 0; i < sortedShips.size(); i++){
            if(sortedShips.get(i).getDockingStatus() == Ship.DockingStatus.Undocked && thisShip.getDistanceTo(sortedShips.get(i)) > 70){
                sortedShips.remove(i);
            }
        }
      return sortedShips;  
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
                }
            }
            removed = sortedPlanets.get(k);
            sortedPlanets.set(k, sortedPlanets.get(index));
            sortedPlanets.set(index, removed);
        }
      return sortedPlanets;  
    }
    
public ArrayList<Ship> getSortedEnemyShips(Ship thisShip){
    ArrayList<Ship> sortedShips = new ArrayList<>();
    ArrayList<Ship> myShips = new ArrayList<>();
    Ship removed;
        sortedShips = new ArrayList<>();
        for(Ship ship: getMyPlayer().getShips().values()){
            myShips.add(ship);
        }
        
        for(Ship ship: getAllShips()){
            if(!myShips.contains(ship)){
                sortedShips.add(ship);
            }
        }
        int index;
        for(int k = 0; k < sortedShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedShips.size(); i++){
                if(sortedShips.get(index).getDistanceTo(thisShip) > sortedShips.get(i).getDistanceTo(thisShip)){
                    index = i;
                }
            }
            removed = sortedShips.get(k);
            sortedShips.set(k, sortedShips.get(index));
            sortedShips.set(index, removed);
        }
      return sortedShips;  
    }

    public ArrayList<Ship> getTeamShipsByHealth(Ship thisShip){
    ArrayList<Ship> sortedShips = new ArrayList<>();
    ArrayList<Ship> myShips = new ArrayList<>();
    Ship removed;
        sortedShips = new ArrayList<>();
        for(Ship ship: getMyPlayer().getShips().values()){
            myShips.add(ship);
        }
        
       sortedShips = myShips;
        int index;
        for(int k = 0; k < sortedShips.size() - 1; k++){
            index = k;
            for(int i = k + 1; i < sortedShips.size(); i++){
                if(sortedShips.get(index).getHealth() > sortedShips.get(i).getHealth()){
                    index = i;
                }
            }
            removed = sortedShips.get(k);
            sortedShips.set(k, sortedShips.get(index));
            sortedShips.set(index, removed);
        }
      return sortedShips;  
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