

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Directory implements java.io.Serializable{
    
    Path directory;
    Path destination;
    int backupNumber;
    int interval;
    int waitInterval;
    boolean copy;

    public Directory (Path directory, int interval, int waitInterval, boolean copy) {
        this.directory = directory;
        this.interval = interval;
        this.waitInterval = waitInterval;
        this.destination = null;
        this.backupNumber = 0;
        this.copy = copy;
    }

    public Directory (Path directory, Path destination, int interval, int waitInterval, boolean copy) {
            this.directory = directory;
            this.destination = destination;
            this.interval = interval;
            this.backupNumber = 0;
            this.waitInterval = waitInterval;
            this.copy = copy;
    }

    public Path getDirectory () {
            return this.directory;
    }

    public int setDestination (Path destination) {
        this.destination = destination;
        return 0;
    }

    public Path getDestination () {
            return this.destination;
    }

    public int setInterval (int newInterval) {
            this.interval = newInterval * 60000;
            return 0;
    }

    public int setWaitInterval (int waitInterval) {
        this.waitInterval = waitInterval * 60000;
        return 0;
    }

    public int getInterval () {
            return this.interval / 60000;
    }

    public int getWaitInterval () {
        return this.waitInterval / 60000;
    }

    public int updateBackups () {
            this.backupNumber++;
            return 0;
    }

    private void writeObject(ObjectOutputStream os) { 
        try {
            os.writeInt(backupNumber);
            os.writeInt(interval);
            os.writeInt(waitInterval);
            os.writeBoolean(copy);
            os.writeObject(directory.toString());
            if (destination == null) {
                os.writeObject("");
            } else {
                os.writeObject(destination.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream ois) {
        try {
            backupNumber = ois.readInt();
            interval = ois.readInt();
            waitInterval = ois.readInt();
            copy = ois.readBoolean();
            
            directory = Paths.get((String)ois.readObject());
            System.out.println(directory.toString());
            
            String dest = (String)ois.readObject();
            if (dest.equals("")) {
                destination = null;
            } else {
                destination = Paths.get(dest);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}