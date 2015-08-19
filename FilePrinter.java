
import java.awt.BorderLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Xiao
 */
public class FilePrinter {
    String LOG_DIRECTORY = "C:\\Automated_Transfer_App\\";
    String ERROR_LOG_NAME = "errorlog";
    String MOVE_LOG_NAME = "movelog";
    String ERROR_LOG_DIRECTORY = LOG_DIRECTORY + "errorlog_";
    String MOVE_LOG_DIRECTORY = LOG_DIRECTORY + "movelog_";
    String ERROR_ZEROES = "000000";
    String MOVE_ZEROES = "000000";
    PrintWriter moveLogWriter;
    PrintWriter errorLogWriter;
    int moveLogIterator;
    int errorLogIterator;
    File moveLog, errorLog;
    int SIZE_LIMIT = 104857600;
    
    public FilePrinter() {
	moveLogIterator = 1;
        errorLogIterator = 1;
        
        File theDir = new File(LOG_DIRECTORY);

        // if the directory does not exist, create it
        if (!theDir.exists())
        {
            System.out.println("creating directory: " + LOG_DIRECTORY);
            boolean result = theDir.mkdir();  
            if(result){    
                System.out.println("DIR created");  
            }

        }
        
        errorLog = new File(ERROR_LOG_DIRECTORY + ERROR_ZEROES + "1.txt");
        
        if (!errorLog.exists())
        {
            try {
                boolean result = errorLog.createNewFile();  
            } catch (java.io.IOException x) {
                //printError(x.toString());
            }

        } else {
            errorSizeChecker();
        }
        
        try {
            errorLogWriter = new PrintWriter(new FileWriter(errorLog, true)); 
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
        
        moveLog = new File(MOVE_LOG_DIRECTORY + MOVE_ZEROES + "1.txt");
        
        if (!moveLog.exists())
        {
            try {
                boolean result = moveLog.createNewFile();  
            } catch (java.io.IOException x) {
                //printError(x.toString());
            }

        } else {
            moveSizeChecker();
        }
        
        try {
            moveLogWriter = new PrintWriter(new FileWriter(moveLog, true)); 
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
    }
    
    public void printError(String error) {
        errorLogWriter.println(error + "\n");
        errorLogWriter.flush();
        long size = 0;
        try {
            size = Files.size(errorLog.toPath());
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
        if (size > SIZE_LIMIT) {
            errorSizeChecker();
        }
    }
    
    public void printMove(String move) {
        moveLogWriter.println(move + "\n");
        moveLogWriter.flush();
        long size = 0;
        try {
            size = Files.size(moveLog.toPath());
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
        if (size > SIZE_LIMIT) {
            moveSizeChecker();
        }
    }
    
    //Makes sure each log file has the correct numbering
    private void errorSizeChecker() {
        long size = 0;
        try {
            size = Files.size(errorLog.toPath());
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
        while (size > SIZE_LIMIT) {
            errorLogIterator++;
            if (errorLogIterator > 9) {
                ERROR_ZEROES = "00000";
            }
            if (errorLogIterator > 99) {
                ERROR_ZEROES = "0000";
            }
            if (errorLogIterator > 999) {
                ERROR_ZEROES = "000";
            }
            if (errorLogIterator > 9999) {
                ERROR_ZEROES = "00";
            }
            if (errorLogIterator > 99999) {
                ERROR_ZEROES = "0";
            }
            if (errorLogIterator > 999999) {
                ERROR_ZEROES = "";
            }

            errorLog = new File(ERROR_LOG_DIRECTORY + ERROR_ZEROES + errorLogIterator + ".txt");
            if (!errorLog.exists()) {
                size = 0;
                try {
                    boolean result = errorLog.createNewFile();  
                } catch (java.io.IOException x) {
                    //printError(x.toString());
                }
            } else {
                try {
                    size = Files.size(errorLog.toPath());
                } catch (java.io.IOException x) {
                    //printError(x.getLocalizedMessage());
                }
            }

        }
    }
    
    private void moveSizeChecker() {
        long size = 0;
        try {
            size = Files.size(moveLog.toPath());
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
        while (size > SIZE_LIMIT) {
            moveLogIterator++;
            if (moveLogIterator > 9) {
                MOVE_ZEROES = "00000";
            }
            if (moveLogIterator > 99) {
                MOVE_ZEROES = "0000";
            }
            if (moveLogIterator > 999) {
                MOVE_ZEROES = "000";
            }
            if (moveLogIterator > 9999) {
                MOVE_ZEROES = "00";
            }
            if (moveLogIterator > 99999) {
                MOVE_ZEROES = "0";
            }
            if (moveLogIterator > 999999) {
                MOVE_ZEROES = "";
            }

            moveLog = new File(MOVE_LOG_DIRECTORY + MOVE_ZEROES + moveLogIterator + ".txt");
            if (!moveLog.exists()) {
                size = 0;
                try {
                    boolean result = moveLog.createNewFile();  
                } catch (java.io.IOException x) {
                    //printError(x.toString());
                }
            } else {
                try {
                    size = Files.size(moveLog.toPath());
                } catch (java.io.IOException x) {
                    //printError(x.getLocalizedMessage());
                }
            }

        }
    }
    
    public void changeMoveLogName(String name) {
        if (MOVE_LOG_NAME.equals(name)) {
            return;
        }
        MOVE_LOG_NAME = name;
        
        moveLogIterator = 1;
        
        File theDir = new File(LOG_DIRECTORY);

        // if the directory does not exist, create it
        if (!theDir.exists())
        {
            System.out.println("creating directory: " + LOG_DIRECTORY);
            boolean result = theDir.mkdir();  
            if(result){    
                System.out.println("DIR created");  
            }

        }
        
        MOVE_LOG_DIRECTORY = LOG_DIRECTORY + name + "_";
        moveLog = new File(MOVE_LOG_DIRECTORY + MOVE_ZEROES + "1.txt");
        
        if (!moveLog.exists())
        {
            try {
                boolean result = moveLog.createNewFile();  
            } catch (java.io.IOException x) {
                //printError(x.toString());
            }

        } else {
            moveSizeChecker();
        }
        
        try {
            moveLogWriter = new PrintWriter(new FileWriter(moveLog, true)); 
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
    }
    
    public void changeErrorLogName(String name) {
        if (ERROR_LOG_NAME.equals(name)) {
            return;
        }
        ERROR_LOG_NAME = name;
        
        errorLogIterator = 1;
        
        File theDir = new File(LOG_DIRECTORY);

        // if the directory does not exist, create it
        if (!theDir.exists())
        {
            System.out.println("creating directory: " + LOG_DIRECTORY);
            boolean result = theDir.mkdir();  
            if(result){    
                System.out.println("DIR created");  
            }

        }
        
        ERROR_LOG_DIRECTORY = LOG_DIRECTORY + name + "_";
        errorLog = new File(ERROR_LOG_DIRECTORY + ERROR_ZEROES + "1.txt");
        
        if (!errorLog.exists())
        {
            try {
                boolean result = errorLog.createNewFile();  
            } catch (java.io.IOException x) {
                //printError(x.toString());
            }

        } else {
            errorSizeChecker();
        }
        
        try {
            errorLogWriter = new PrintWriter(new FileWriter(errorLog, true)); 
        } catch (java.io.IOException x) {
            //printError(x.getLocalizedMessage());
        }
        
        
    }
}
