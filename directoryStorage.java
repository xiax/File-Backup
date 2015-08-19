

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class directoryStorage implements java.io.Serializable {
	ArrayList<Directory> directories;
	
	public directoryStorage () {
		directories = new ArrayList<Directory>();
	}
        
        public void addNewDirectory (Directory dir) {
            directories.add(dir);
        }
        
        public int addNewDirectory (Path directory, int interval, int waitInterval, boolean copy) {
		directories.add(new Directory(directory, interval, waitInterval, copy));
                return 0;
	}
	
	public int addNewDirectory (Path directory, Path destination, int interval, int waitInterval, boolean copy) {
		directories.add(new Directory(directory, destination, interval, waitInterval, copy));
                return 0;
	}
	
	public Iterator getDirectories() {
		return directories.iterator();
	}
        
        public Directory getDirectory (int index) {
            return directories.get(index);
        }
        
        public Directory deleteDirectory (int index) {
            return directories.remove(index);
        }
        

}


