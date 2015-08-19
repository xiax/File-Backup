/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.util.Iterator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import javax.swing.JOptionPane;
import java.util.Timer;
import java.util.TimerTask;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.AbstractButton;
import java.nio.file.Files;
import java.net.URL;

/*
 * FileChooserDemo.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 */
 
public class fileBackupProgram extends JPanel
                      implements ListSelectionListener {
    
    String greenButtonIcon = "/images/Green.jpg";
    String redButtonIcon = "/images/Red.jpg";
    
    private JList list, destList;
    private DefaultListModel listModel, destListModel;
    private JFormattedTextField destField, waitField, checkField;
    private ArrayList<Timer> timers;
 
    private static final String hireString = "Edit";
    private static final String fireString = "Delete";
    private static final String saveString = "Save";
    private static final String openString = "Setup New Monitor";
    private static final String destString = "Edit Destination Directory";
    private JButton fireButton;
    private JTextField employeeName;
    private JLabel monitored, destination, waitInt, check;
    ListSelectionModel listSelectionModel;
    JFrame frame;
    CustomDialog errorDialog, moveDialog;
    JRadioButton copyButton, moveButton;
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem editError, editMove, exit;
	
	static private final String newline = "\n";
    JButton openButton, saveButton, destButton, startButton, stopButton;
    JTextArea log;
    JFileChooser fc;
    static directoryStorage directoryList;
    Timer timer;
    int hour = 360000;
    static FilePrinter printer;
    boolean copy;
    static String LOG_DIRECTORY = "C:\\Automated_Transfer_App\\";
    static String ERROR_LOG_NAME = "errorlog";
    static String MOVE_LOG_NAME = "movelog";
    
    public fileBackupProgram(JFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        
        errorDialog = new CustomDialog(frame, "Please enter a new name for the error log", this);
        errorDialog.pack();
        
        moveDialog = new CustomDialog(frame, "Please enter a new name for the move log", this);
        moveDialog.pack();
        
        printer = new FilePrinter();
        timers = new ArrayList<>();
	log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
        Object obj;
        copy = true;
        listModel = new DefaultListModel();
        //destListModel = new DefaultListModel();
        directoryList = new directoryStorage();
        
        //Create a file chooser
        fc = new JFileChooser();

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");
        menuBar.add(menu);
        
        editError = new JMenuItem("Save Error Log As...");
        editError.getAccessibleContext().setAccessibleDescription(
                "Change the name of the error log file");
        editError.addActionListener(new ErrorListener());
        menu.add(editError);
        
        editMove = new JMenuItem("Save Move Log As...");
        editMove.getAccessibleContext().setAccessibleDescription(
                "Change the name of the move log file");
        editMove.addActionListener(new MoveListener());
        menu.add(editMove);
        
        exit = new JMenuItem("Exit");
        exit.getAccessibleContext().setAccessibleDescription(
                "Exit the Program");
        exit.addActionListener(new CloseListener());
        menu.add(exit);
        frame.setJMenuBar(menuBar);
        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        openButton = new JButton(openString);
        openButton.setActionCommand(openString);
        openButton.addActionListener(new OpenListener());
        
        destButton = new JButton(destString);
        destButton.setActionCommand(destString);
        destButton.addActionListener(new DestListener());
        
        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton(saveString);
        saveButton.setActionCommand(saveString);
        saveButton.addActionListener(new SaveListener());
        
        URL imageURL = getClass().getResource(greenButtonIcon);
        ImageIcon greenSquare = new ImageIcon(imageURL);
        startButton = new JButton("Start", greenSquare);
        startButton.setSize(60, 20);
        startButton.setHorizontalTextPosition(AbstractButton.LEADING);
        startButton.setActionCommand("Start");
        startButton.addActionListener(new StartListener());
        
        imageURL = getClass().getResource(redButtonIcon);
        ImageIcon redSquare = new ImageIcon(imageURL);
        stopButton = new JButton("Stop", redSquare);
        stopButton.setSize(60, 20);
        stopButton.setHorizontalTextPosition(AbstractButton.LEADING);
        stopButton.setActionCommand("Stop");
        stopButton.addActionListener(new StopListener());
        
        
        
        copyButton = new JRadioButton("Copy");
        copyButton.setActionCommand("Copy");
        copyButton.setSelected(true);
        copyButton.addActionListener(new RadioListener());
 
        moveButton = new JRadioButton("Move");
        moveButton.setActionCommand("Move");
        moveButton.addActionListener(new RadioListener());
        
        ButtonGroup group = new ButtonGroup();
        group.add(copyButton);
        group.add(moveButton);
        
        
        //For layout purposes, put the buttons in a separate panel
        
        JPanel optionPanel = new JPanel();
        
        GroupLayout layout = new GroupLayout(optionPanel);
        optionPanel.setLayout(layout);
 
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                    .addComponent(copyButton)
                    .addComponent(moveButton)
            );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(copyButton)
                    .addComponent(moveButton))
        );
        
        
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        
        layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
 
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(openButton)
                .addComponent(optionPanel))
                    .addComponent(destButton)
                    .addComponent(startButton)
                .addComponent(stopButton)
                //.addComponent(saveButton)
            );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton)
                    .addComponent(destButton)
                    .addComponent(startButton)
                    .addComponent(stopButton)
                    //.addComponent(saveButton)
                )
                .addComponent(optionPanel)
        );
        
        
        
        buttonPanel.add(optionPanel);
        /*
        buttonPanel.add(openButton);
        buttonPanel.add(destButton);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(listLabel);
        buttonPanel.add(copyButton);
        buttonPanel.add(moveButton);
        */
        destButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);

        //Add the buttons and the log to this panel.
        
        //add(logScrollPane, BorderLayout.CENTER);
		
 
        JLabel listLabel = new JLabel("Monitored Directory:");
        listLabel.setLabelFor(list);
 
        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(8);
        JScrollPane listScrollPane = new JScrollPane(list);
        JPanel listPane = new JPanel();
        listPane.setLayout(new BorderLayout());
        
        listPane.add(listLabel, BorderLayout.PAGE_START);
        listPane.add(listScrollPane ,BorderLayout.CENTER);
        
        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(
                new SharedListSelectionHandler());
        //monitored, destination, waitInt, check
        
        
        destination = new JLabel("Destination Directory: ");
        
        waitField = new JFormattedTextField();
        //waitField.setValue(240);
        waitField.setEditable(false);
        waitField.addPropertyChangeListener(new FormattedTextListener());
        
        waitInt = new JLabel("Wait Interval (in minutes)");
        //waitInt.setLabelFor(waitField);
        
        checkField = new JFormattedTextField();
        checkField.setSize(1, 10);
        //checkField.setValue(60);
        checkField.setEditable(false);
        checkField.addPropertyChangeListener(new FormattedTextListener());
 
        check = new JLabel("Check Interval (in minutes)");
        //check.setLabelFor(checkField);
        
        
 
        fireButton = new JButton(fireString);
        fireButton.setActionCommand(fireString);
        fireButton.addActionListener(new FireListener());
 
        JPanel fieldPane = new JPanel();
        //fieldPane.add(destField);
        layout = new GroupLayout(fieldPane);
        fieldPane.setLayout(layout);
 
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(waitInt)
                    .addComponent(check))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(waitField, 60, 60, 60)
                    .addComponent(checkField, 60, 60, 60))
                
            );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(waitInt)
                    .addComponent(waitField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(check)
                    .addComponent(checkField))
        );
        
       
        JPanel labelPane = new JPanel();
        
        labelPane.setLayout(new BorderLayout());
        
        labelPane.add(destination, BorderLayout.PAGE_START);
        labelPane.add(fieldPane, BorderLayout.CENTER);
        
        layout = new GroupLayout(labelPane);
        labelPane.setLayout(layout);
 
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(destination)
                    .addComponent(fieldPane))
                
            );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                    .addComponent(destination)
                    .addComponent(fieldPane)
        );
        //labelPane.add(destination);
        //labelPane.add(fieldPane);
        
        
        try {
            // Read from disk using FileInputStream
            FileInputStream f_in = new 
                    FileInputStream(LOG_DIRECTORY + "\\save.data");
            
            // Read object using ObjectInputStream
            ObjectInputStream obj_in = 
                    new ObjectInputStream (f_in);

            // Read an object
            directoryList = (directoryStorage)obj_in.readObject();
            ERROR_LOG_NAME = (String)obj_in.readObject();
            MOVE_LOG_NAME = (String)obj_in.readObject();
            
            if (ERROR_LOG_NAME instanceof String) {
                printer.changeErrorLogName(ERROR_LOG_NAME);
            }
            
            if (MOVE_LOG_NAME instanceof String) {
                printer.changeMoveLogName(MOVE_LOG_NAME);
            }
            
            if (directoryList instanceof directoryStorage)
            {
                System.out.println("found object");
                //directoryList = (directoryStorage) obj;

                Iterator<Directory> directories = directoryList.getDirectories();
                Directory d;
                while (directories.hasNext()) {
                        d = directories.next();
                        
                        try {
                            listModel.addElement(d.getDirectory().toRealPath());
                        } catch (IOException x) {
                            printer.printError(x.toString());
                        }

                        int index = list.getSelectedIndex();
                        if (index == -1) {
                            list.setSelectedIndex(0);
                        }

                        
                        index = list.getSelectedIndex();
                        Directory dir = directoryList.getDirectory(index);
                        
                        destButton.setEnabled(true);
                        checkField.setValue(dir.getInterval());
                        waitField.setValue(dir.getWaitInterval());
                        checkField.setEditable(true);
                        waitField.setEditable(true);
                        
                        //directoryList.addNewDirectory(d);
                        //try {
                        //listModel.addElement(d.getDirectory().toString());
                        //} catch (IOException x) {
                           // printer.printError(x.toString());
                        //}

                        //timer = new Timer();
                        //timer.schedule(new CopyTask(d.directory, d.destination, d.getWaitInterval(), printer, d.copy), 0, d.getInterval());
                }

            } else {
                System.out.println("did not find object");


            }
            obj_in.close();
        } catch (ClassNotFoundException x) {
            printer.printError(x.getLocalizedMessage());
            System.err.format("Unable to read");
        } catch (IOException y) {
            printer.printError(y.getLocalizedMessage());

        }
        
 
        //Layout the text fields in a panel.
        
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        
        buttonPane.add(fireButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 
        add(buttonPanel, BorderLayout.PAGE_START);
        add(listPane, BorderLayout.LINE_START);
        //add(destListScrollPane, BorderLayout.CENTER);
        
        add(fieldPane, BorderLayout.LINE_END);
        add(labelPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
        
    }
    
    class ErrorListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            errorDialog.setLocationRelativeTo(frame);
            errorDialog.setVisible(true);
 
            String s = errorDialog.getValidatedText();
            if (!"".equals(s)) {
                System.out.println(s);
                ERROR_LOG_NAME = s;
                printer.changeErrorLogName(s);
            }
            
        }
    }
    
    class MoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            moveDialog.setLocationRelativeTo(frame);
            moveDialog.setVisible(true);
 
            String s = moveDialog.getValidatedText();
            if (!"".equals(s)) {
                System.out.println(s);
                MOVE_LOG_NAME = s;
                printer.changeMoveLogName(s);
            }
            
        }
    }
    
    
	
    class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();

            int index = list.getSelectedIndex();
            Directory dir = directoryList.getDirectory(index);
            
            waitField.setValue(dir.getWaitInterval());
            checkField.setValue(dir.getInterval());
            if (dir.copy) {
                copyButton.setSelected(true);
            } else {
                moveButton.setSelected(true);
            }
            if (dir.destination != null) {
                
                destination.setText("Destination Directory: " + dir.destination.toString());
                startButton.setEnabled(true);
                if (dir.backupNumber == 0) {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                } else {
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                }
            } else {
                destination.setText("Destination Directory: ");
                startButton.setEnabled(false);
            }
        }
    }
    
    class FormattedTextListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            Object source = e.getSource();
            int index = list.getSelectedIndex();
            if (index == -1) {
                return;
            }
            if (source.equals(waitField)) {
                try {
                    waitField.commitEdit();
                } catch(java.text.ParseException x) {
                    printer.printError(x.toString());
                }
                if (waitField.isEditable()) {
                    int wait = ((Number)waitField.getValue()).intValue();
                    
                    directoryList.getDirectory(index).setWaitInterval(wait);
                }
            } else if (source.equals(checkField)) {
                try {
                    checkField.commitEdit();
                } catch(java.text.ParseException x) {
                    printer.printError(x.toString());
                }
                if (checkField.isEditable()) {
                    int check = ((Number)checkField.getValue()).intValue();
                    directoryList.getDirectory(index).setInterval(check);
                }
            }
        }
    }
    class RadioListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            if ("Move".equals(e.getActionCommand())) {
                copy = false;
                directoryList.getDirectory(index).copy = false;
            } 
            if ("Copy".equals(e.getActionCommand())) {
                copy = true;
                directoryList.getDirectory(index).copy = true;
            }
        }
    }
    
    class CloseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                FileOutputStream f_out = new 
                FileOutputStream(LOG_DIRECTORY + "\\save.data");

                // Write object with ObjectOutputStream
                ObjectOutputStream obj_out = new
                ObjectOutputStream (f_out);

                // Write object out to disk
                obj_out.writeObject ( directoryList );
                obj_out.writeObject(ERROR_LOG_NAME);
                obj_out.writeObject(MOVE_LOG_NAME);
                obj_out.flush();
                obj_out.close();
                printer.printError(LOG_DIRECTORY);
            } catch (IOException x) {
                printer.printError(x.toString());
            }
            System.exit(0);
        }
    }
    
    class OpenListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int returnVal = fc.showOpenDialog(fileBackupProgram.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                Path directory = file.toPath();
                directoryList.addNewDirectory(directory, 3600000, 14400000, copy);
                
                try {
                    listModel.addElement(directory.toRealPath());
                } catch (IOException x) {
                    printer.printError(x.toString());
                }
                
                int index = list.getSelectedIndex();
                if (index == -1) {
                    list.setSelectedIndex(0);
                }
                
                index = list.getSelectedIndex();
                Directory dir = directoryList.getDirectory(index);
                
                if (dir.copy) {
                    copyButton.setSelected(true);
                } else {
                    moveButton.setSelected(true);
                }
                
                destButton.setEnabled(true);
                checkField.setValue(dir.getInterval());
                waitField.setValue(dir.getWaitInterval());
                checkField.setEditable(true);
                waitField.setEditable(true);
                
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
    
    class DestListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            if (index == -1) {
                return;
            }
            int returnVal = fc.showOpenDialog(fileBackupProgram.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                Path directory = file.toPath();

                try {
                    destination.setText("Destination Directory: " + directory.toRealPath());
                    //destField.setValue(directory.toRealPath());
                } catch (IOException x) {
                    printer.printError(x.toString());
                }
                
                directoryList.getDirectory(index).setDestination(directory);
                startButton.setEnabled(true);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
    
    class StartListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            try {
                waitField.commitEdit();
                checkField.commitEdit();
            } catch(java.text.ParseException x) {
                printer.printError(x.toString());
                return;
            }
            Directory task = directoryList.getDirectory(index);
            startButton.setEnabled(false);
            task.setWaitInterval(((Number)waitField.getValue()).intValue());
            task.setInterval(((Number)checkField.getValue()).intValue());
            Path destination = task.getDestination();
            Path directory = task.getDirectory();
            task.backupNumber = 1;
            
            timer = new Timer();
            timer.schedule(new CopyTask(directory, destination, task.waitInterval, printer, copy), 0, task.interval);
            timers.add(timer);
            
            stopButton.setEnabled(true);
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
    
    class StopListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            stopButton.setEnabled(false);
            timers.get(index).cancel();
            timers.remove(index);
            startButton.setEnabled(true);
            
            Directory task = directoryList.getDirectory(index);
            task.backupNumber = 0;
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
    
    class SaveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Write to disk with FileOutputStream
            try {
                FileOutputStream f_out = new 
                FileOutputStream(LOG_DIRECTORY + "\\save.data");

                // Write object with ObjectOutputStream
                ObjectOutputStream obj_out = new
                ObjectOutputStream (f_out);

                // Write object out to disk
                obj_out.writeObject ( directoryList );
                log.setCaretPosition(log.getDocument().getLength());
            } catch (IOException x) {
                printer.printError(x.toString());
            }
            
        }
    }
    
    class FireListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //This method can be called only if
            //there's a valid selection
            //so go ahead and remove whatever's selected.
            int index = list.getSelectedIndex();
            listModel.remove(index);
 
            int size = listModel.getSize();
 
            directoryList.deleteDirectory(index);
            if (size == 0) { //Nobody's left, disable firing.
                fireButton.setEnabled(false);
                destButton.setEnabled(false);
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
 
            } else { //Select an index.
                if (index == listModel.getSize()) {
                    //removed item in last position
                    index--;
                }
 
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    }
 
    
 
    //This method is required by ListSelectionListener.
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
 
            if (list.getSelectedIndex() == -1) {
            //No selection, disable fire button.
                fireButton.setEnabled(false);
 
            } else {
            //Selection, enable the fire button.
                fireButton.setEnabled(true);
            }
        }
    }
    
    static class WindowListen extends WindowAdapter implements WindowListener {
        @Override
        public void windowClosing(WindowEvent e) {
            try {
                FileOutputStream f_out = new 
                FileOutputStream(LOG_DIRECTORY + "\\save.data");

                // Write object with ObjectOutputStream
                ObjectOutputStream obj_out = new
                ObjectOutputStream (f_out);

                // Write object out to disk
                obj_out.writeObject ( directoryList );
                obj_out.writeObject(ERROR_LOG_NAME);
                obj_out.writeObject(MOVE_LOG_NAME);
                obj_out.flush();
                obj_out.close();
                     
                printer.printError(LOG_DIRECTORY);
            } catch (IOException x) {
                printer.printError(x.toString());
            }
        }
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Automated File Mover");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we){
                try {
                    FileOutputStream f_out = new 
                    FileOutputStream(LOG_DIRECTORY + "save.data");

                    // Write object with ObjectOutputStream
                    ObjectOutputStream obj_out = new
                    ObjectOutputStream (f_out);

                    // Write object out to disk
                    obj_out.writeObject ( directoryList );
                    obj_out.writeObject(ERROR_LOG_NAME);
                    obj_out.writeObject(MOVE_LOG_NAME);
                    obj_out.flush();
                    obj_out.close();
                    printer.printError(LOG_DIRECTORY);
                } catch (IOException x) {
                    printer.printError(x.toString());
                }
            }
        });
        
        //Create and set up the content pane.
        JComponent newContentPane = new fileBackupProgram(frame);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}


/*
private class FileChooserDemo extends JPanel
                             implements ActionListener {
    static private final String newline = "\n";
    JButton openButton, saveButton;
    JTextArea log;
    JFileChooser fc;
	DirectoryData directoryList;
	Timer timer;

    public FileChooserDemo() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
		        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

		// Read from disk using FileInputStream
		FileInputStream f_in = new 
			FileInputStream("save.data");

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = 
			new ObjectInputStream (f_in);

		// Read an object
		Object obj = obj_in.readObject();

		if (obj instanceof DirectoryData)
		{
			// Cast object to a Vector
			DirectoryData directoryList = (DirectoryData) obj;

			// Do something with vector....
		} else {
			directoryList = new DirectoryData();
		}
		
        //Create a file chooser
        fc = new JFileChooser();

        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Open a File...",
                                 createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);

        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton("Save a File...",
                                 createImageIcon("images/Save16.gif"));
        saveButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(FileChooserDemo.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
				Path directory = file.toPath();
				
				returnVal = fc.showOpenDialog(FileChooserDemo.this);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					file = fc.getSelectedFile();
					Path destination = file.toPath();
					
					int string = 
					JOptionPane.showInputDialog ( "Please enter interval in hours between backups:" ); 

					//convert numbers from type String to type int 
					int interval = Integer.parseInt ( time);
					
					directoryList.addNewDirectory(directory, destination, interval);
					
					timer = new Timer();
					timer.schedule(new CopyTask(directory, destination), 0, hours*interval);
				}
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

        //Handle save button action.
        } else if (e.getSource() == saveButton) {
            // Write to disk with FileOutputStream
			FileOutputStream f_out = new 
			FileOutputStream("save.data");

			// Write object with ObjectOutputStream
			ObjectOutputStream obj_out = new
			ObjectOutputStream (f_out);

			// Write object out to disk
			obj_out.writeObject ( directoryList );
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. 
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FileChooserDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new FileChooserDemo());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}
*/