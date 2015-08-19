
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimerTask;
import java.nio.file.FileVisitOption;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.io.IOException;
import java.util.*;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CopyTask extends TimerTask {
    Path directory, destination;
    int waitInterval;
    FilePrinter printer;
    boolean copy;
    Document config_xml;
    File source_email_dir, destination_email_dir;
    
    //directory is the folder directory being monitored, destination is the destination to copy files to.
    //waitInterval is how long to wait after a file is created before copying it over.
    //printer is the instantiated FilePrinter instance used to write error and move logs.
    //copy is a flag for whether to just copy files over or move them so the files are no longer in the source
    //directory.
    public CopyTask (Path directory, Path destination, int waitInterval, FilePrinter printer, boolean copy) {
            this.directory = directory;
            this.destination = destination;
            this.waitInterval = waitInterval;
            this.printer = printer;
            this.copy = copy;
    }

    @Override
    public void run() {
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
	FileChecker cf = new FileChecker(directory, destination, copy, true, waitInterval);
        System.out.println("Got Here");
        
        try {
            
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            config_xml = docBuilder.parse(new File("config.xml"));
            config_xml.getDocumentElement().normalize();
            
            source_email_dir = new File(config_xml.getElementsByTagName("source_email_dir").item(0).getTextContent());
            destination_email_dir = new File(config_xml.getElementsByTagName("destination_email_dir").item(0).getTextContent());
            
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        
        Path email_source = source_email_dir.toPath();
        Path email_destination = destination_email_dir.toPath();
        
        FileChecker cf_email = new FileChecker(email_source, email_destination, copy, true, waitInterval);
        try {
            Files.walkFileTree(email_source, opts, Integer.MAX_VALUE, cf_email);
            Files.walkFileTree(directory, opts, Integer.MAX_VALUE, cf);
        } catch (IOException x) {
            printer.printError(x.toString());
            System.err.format("Unable to copy: %s: %s %n", directory.toAbsolutePath(), destination.toAbsolutePath());
        }
    }

    
    void copyFile(Path source, Path target, boolean prompt, boolean preserve) {
        CopyOption[] options = 
            new CopyOption[] { REPLACE_EXISTING };
        try {
            Files.copy(source, target, options);
        } catch (IOException x) {
            printer.printError(x.toString());
            System.err.format("Unable to copy: %s: %s%n", source, x);
        }
        
    }
    
    void moveFile(Path source, Path target, boolean prompt, boolean preserve) {
        CopyOption[] options = 
            new CopyOption[] { REPLACE_EXISTING };
        try {
            Files.move(source, target, options);
        } catch (IOException x) {
            printer.printError(x.toString());
            System.err.format("Unable to move: %s: %s%n", source, x);
        }
        
    }
    
    public Calendar getCurrentTime() {
        Calendar currentTime;
        
        currentTime = Calendar.getInstance();
        
        return currentTime;
    }
    
    //Goes over each file in the source directory and checks how long it has been created for
    //and copies all files over to the target directory if the time the files have been created
    //for is over the wait interval for all files in a folder.
    class FileChecker implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        private final boolean copy;
        private final boolean preserve;
        private boolean copyFlag;
        private boolean oldcopyFlag;
        private long FOURHOURS;
        
        FileChecker(Path source, Path target, boolean copy, boolean preserve, long waitInterval) {
            this.source = source;
            this.target = target;
            this.copy = copy;
            this.preserve = preserve;
            this.copyFlag = false;
            this.FOURHOURS = waitInterval;
        }
 
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
            CopyOption[] options = (preserve) ?
                new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];
 
            Path newdir = target.resolve(source.relativize(dir));
                
            try {
                long fileTime = Files.getLastModifiedTime(dir).toMillis();
                long currentTime = Calendar.getInstance().getTimeInMillis();
                
                if (currentTime - fileTime < FOURHOURS) {
                    this.copyFlag = false;
                } else {
                    System.out.println("Visiting " + dir.toString());
                    
                    this.oldcopyFlag = this.copyFlag;
                    this.copyFlag = true;
                    if (!newdir.toFile().exists()) {
                        Files.copy(dir, newdir, options);
                    }
                    printer.printMove("Copied " + dir.toString() + " to " + newdir.toString() + "\n");
                }
            } catch (FileAlreadyExistsException x) {
                printer.printError(x.toString());
            } catch (IOException x) {
                printer.printError(x.toString());
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            try {
                long fileTime = Files.getLastModifiedTime(file).toMillis();
                long currentTime = Calendar.getInstance().getTimeInMillis();
                
                if (currentTime - fileTime < FOURHOURS) {
                    System.out.println("FileTime: " + fileTime + ", currentTime: " + currentTime + " FOURHOURS: " + FOURHOURS);
                    this.copyFlag = false;
                }
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (IOException x) {
                printer.printError(x.toString());
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // check if directory is good for copying
            if (this.copyFlag) {
                System.out.println("True");
            } else {
                System.out.println("False");
            }
            if (this.copyFlag && dir != source) {
                System.out.println("Copied " + dir.toString());
                Path newdir = target.resolve(source.relativize(dir));
                EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                FileCopier fc = new FileCopier(dir, newdir, false, true);
                try {
                    Files.walkFileTree(dir, opts, Integer.MAX_VALUE, fc);
                } catch (IOException x) {
                    printer.printError(x.toString());
                    System.err.format("Unable to copy: %s: %s %n", source.toAbsolutePath(), target.toAbsolutePath());
                }
            } else {
                System.out.println("Didn't copy " + dir.toString());
            }
            if (this.copyFlag == true) {
                this.copyFlag = this.oldcopyFlag;
            }
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                printer.printError("cycle detected: " + file);
            } else {
                printer.printError("Unable to copy:" + file + ":" + exc + "%n");
            }
            return CONTINUE;
        }
    }
    
    //Does the actual copying of the files in a folder that passes the check
    class FileCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        private final boolean prompt;
        private final boolean preserve;
 
        FileCopier(Path source, Path target, boolean prompt, boolean preserve) {
            this.source = source;
            this.target = target;
            this.prompt = prompt;
            this.preserve = preserve;
        }
 
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
            CopyOption[] options = (preserve) ?
                new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];
 
            Path newdir = target.resolve(source.relativize(dir));
            try {
                if (!newdir.toFile().exists()) {
                    Files.copy(dir, newdir, options);
                }
                printer.printMove("Copied " + dir.toString() + " to " + newdir.toString() + "\n");
            } catch (FileAlreadyExistsException x) {
                printer.printError(x.toString());
            } catch (IOException x) {
                printer.printError(x.toString());
                System.err.format("Unable to create: %s: %s%n", newdir, x);
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (copy) {
                copyFile(file, target.resolve(source.relativize(file)),
                        prompt, preserve);
                printer.printMove("Copied " + file.toString());
            } else {
                moveFile(file, target.resolve(source.relativize(file)),
                        prompt, preserve);
                printer.printMove("Moved " + file.toString());
            }
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // fix up modification time of directory when done
            if (exc == null && preserve) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                    if (!copy) {
                        Files.delete(dir);
                    }
                } catch (IOException x) {
                    printer.printError(x.toString());
                    System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
                }
            }
            return CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                printer.printError("cycle detected: " + file);
            } else {
                printer.printError("Unable to copy:" + file + ":" + exc + "%n");
            }
            return CONTINUE;
        }
    }
}
