
package hlt;

import java.util.*;


public class Record {
    public static Map<Ship,Position> record = new HashMap<Ship,Position>();
    
    public static Map<Ship,Position> getRecord(){
        return record;
    }
    
    public static void storeVectors(Ship ship, Position position){
        record.put(ship, position);
    }
    
    public static void clear(){
        record.clear();
        Log.log("Record cleared.");
        Log.log("Record contents [" + record + "]");
    }
    
}
