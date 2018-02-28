
package hlt;

import java.util.*;


public class Record {
    private static Map<Ship,Velocity> record = new HashMap<Ship,Velocity>();
    
    public static Map<Ship,Velocity> getRecord(){
        return record;
    }
    
    public static void storeVectors(Ship ship, Velocity velocity){
        record.put(ship, velocity);
        Log.log("Vectors [velx" + velocity.x + "] [vely" + velocity.y + "], ship [" + ship.getId() + "]" );
    }
    
    public static void clear(){
        record.clear();
        Log.log("Record cleared.");
    }
    
    public static void print(){
        
        Log.log("Record contents: " + record);
    }
    
}
