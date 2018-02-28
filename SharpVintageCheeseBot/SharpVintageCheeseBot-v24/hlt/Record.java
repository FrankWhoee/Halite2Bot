
package hlt;

import java.util.*;


public class Record {
    private static Map<Ship,Position> record = new HashMap<Ship,Position>();
    
    public static Map<Ship,Position> getRecord(){
        return record;
    }
    
    public static void storeVectors(Ship ship, Position position){
        record.put(ship, position);
        Log.log("Vectors [velx" + position.getXPos() + "] [vely" + position.getYPos() + "], ship [" + ship.getId() + "]" );
    }
    
    public static void clear(){
        record.clear();
        Log.log("Record cleared.");
    }
    
    public static void print(){
        
        Log.log("Record contents: " + record);
    }
    
}
