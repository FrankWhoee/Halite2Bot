
package hlt;

import java.util.*;


public class Record {
    private static Map<Ship,Velocity> record = new HashMap<Ship,Velocity>();
    
    public static Map<Ship,Velocity> getRecord(){
        return record;
    }
    
    public static void storeVectors(Ship ship, Velocity velocity){
        if(ship.getDockingStatus() == Ship.DockingStatus.Undocked){
            record.put(ship, velocity);
            Log.log("RECORD.storeVectors: STORING VECTORS [velx: " + velocity.x + "] [vely: " + velocity.y + "], SHIP [" + ship.getId() + "]" );
            
        }
        
    }
    
    public static void removeVectors(Ship ship){
        record.remove(ship);
        Log.log("RECORD.removeVectors: " + "ship" + ship.getId() + " removed.");
        
    }
    
    public static void clear(){
        record.clear();
        Log.log("RECORD.clear: Record cleared.");
    }
    
    public static void print(){
        
        Log.log("RECORD.print: Record contents: " + record);
    }
    
}
