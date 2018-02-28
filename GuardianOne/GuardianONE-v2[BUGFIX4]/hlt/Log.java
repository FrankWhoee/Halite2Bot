package hlt;

import java.io.FileWriter;
import java.io.IOException;

public class Log {

    private final FileWriter file;
    private static Log instance;

    private Log(final FileWriter f) {
        file = f;
    }

    static void initialize(final FileWriter f) {
        instance = new Log(f);
    }

    public static void log(final String message) {
        if(Turn.getTurn() > 54 && Turn.getTurn() < 60) {
        	try {
                instance.file.write(message);
                instance.file.write('\n');
                instance.file.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}
