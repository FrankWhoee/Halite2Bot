package hlt;

public class Constants {

    ////////////////////////////////////////////////////////////////////////
    // Implementation-independent language-agnostic constants

    /** Games will not have more players than this */
    public static final int MAX_PLAYERS = 4;

    /** Max number of units of distance a ship can travel in a turn */
    public static final int MAX_SPEED = 7;

    /** Radius of a ship */
    public static final double SHIP_RADIUS = 0.5;

    /** Starting health of ship, also its max */
    public static final int MAX_SHIP_HEALTH = 255;

    /** Starting health of ship, also its max */
    public static final int BASE_SHIP_HEALTH = 255;

    /** Weapon cooldown period */
    public static final int WEAPON_COOLDOWN = 1;

    /** Weapon damage radius */
    public static final double WEAPON_RADIUS = 5.0;

    /** Weapon damage */
    public static final int WEAPON_DAMAGE = 64;

    /** Radius in which explosions affect other entities */
    public static final double EXPLOSION_RADIUS = 10.0;

    /** Distance from the edge of the planet at which ships can try to dock */
    public static final double DOCK_RADIUS = 4.0;

    /** Number of turns it takes to dock a ship */
    public static final int DOCK_TURNS = 5;

    /** Number of production units per turn contributed by each docked ship */
    public static final int BASE_PRODUCTIVITY = 6;

    /** Distance from the planets edge at which new ships are created */
    public static final double SPAWN_RADIUS = 2.0;

    ////////////////////////////////////////////////////////////////////////
    // Implementation-specific constants

    public static final double FORECAST_FUDGE_FACTOR = SHIP_RADIUS + 0.1;
    public static final int MAX_NAVIGATION_CORRECTIONS = 90;

    /**
     * Used in Position.getClosestPoint()
     * Minimum distance specified from the object's outer radius.
     */
    public static final int MIN_DISTANCE_FOR_CLOSEST_POINT = 2;
    
    
    //NON STARTER-KIT CONSTANTS START HERE
    
    //Distance needed to rush successfully
    public static final int MAXIMUM_RUSH_DISTANCE = 96;
    
    //Distance needed to send distracting ship out successfully
    public static final int MAXIMUM_DISTRACT_DISTANCE = 50;
    
    //Radius to look for enemy ships
    public static final int ENEMY_SCAN_RANGE = 20;
    
    //Radius to look for ally ships
    public static final int ALLY_SCAN_RANGE = 7;
    
    //Maximum distance for a safe ship to be to travel to it.
    public static final double MAXIMUM_SAFE_SHIP_TRAVEL_DISTANCE = 28;
    
    //Max thrust plus ship diameter
    public static final double MAX_TRAVEL_DISTANCE = 8;
    
    //Minimum distance to another ship
    public static final double MINIMUM_DISTANCE_TO_SHIP = 1;
    
    //Amount of ships less than the average is when our ships start fleeing.
    public static final double COWARD_OFFSET = 10;
    
    //Minimum distance an enemy ship can be until this ship attacks it.
    public static final double AGGRO_RANGE = 35;
    
    //Amount of "escort" ships to send with docking ships to defend them
    public static final int NUM_OF_GUARD_SHIPS = 2;
    
    //Maximum distance the ship can approach the border of the game map.
    public static final double MAX_DISTANCE_TO_BORDER = 1.5; 
    
    //Controls how far collision time scans.
    public static final double COLLISION_TIME_SCAN_RANGE = 28;
}
