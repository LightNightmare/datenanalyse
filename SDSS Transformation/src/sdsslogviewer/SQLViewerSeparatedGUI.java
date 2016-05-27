package sdsslogviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.SQLException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.data.io.DataIOException;
import prefuse.data.Table;
import prefuse.data.expression.AndPredicate;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.BrowserLauncher;
import prefuse.util.ui.UILib;

import sdsslogviewer.Event.MenuChangedListener;
import sdsslogviewer.Viz.TimelineView;
import sdsslogviewer.Controls.FilterPopupMenu;
import sdsslogviewer.Event.DataTableListener;
import sdsslogviewer.Viz.SQLview;
import sdsslogviewer.data.SDSSLogFileManager;
import sdsslogviewer.data.SDSSLogTable;
import sdsslogviewer.data.io.openLocalFile;
import sdsslogviewer.ui.aboutInfoWindow;

/**
 * Initial GUI of SQL content Viewer.<p/>
 * Jan. 17th, 2011: Class created to construct a separated GUI for SQL Viewer alone.<p/>
 *                  Has the same menu and tool bars as SDSS Log Viewer GUI. And
 *                  has the same data manager and dynamic query.
 *                  But has the SQL Viewer alone.<p/>
 * @author James
 */
public class SQLViewerSeparatedGUI {

    //-- Constants for dimension
    private final int GUI_WIDTH     = 1000,
                      GUI_HEIGHT    = 600,
                      POPUP_HEIGHT  = 240;

    private final String helpURL = "http://nevac.ischool.drexel.edu/~james/" +
                                   "SDSSLogViewer/SDSSLogViewer.html";

    @SuppressWarnings("unused")
	private final ImageIcon
                upIcon = new ImageIcon("./resource/UpArrow-small.png", ""),
                downIcon = new ImageIcon("./resource/DownButton-small.png", ""),
                leftIcon = new ImageIcon("./resource/LeftArrow-small.png", ""),
                rightIcon = new ImageIcon("./resource/RightArrow-small.png", ""),
                openIcon =  new ImageIcon("./resource/OpenIcon.png", ""),
                databaseIcon = new ImageIcon("./resource/DatabaseIcon.png", ""),
                sampleDataIcon = new ImageIcon("./resource/SampleData-small.png", ""),
                closeIcon = new ImageIcon("./resource/CloseIcon.png", "");

    //-- Control variables
    private AndPredicate filter = new AndPredicate();

    MenuChangedListener update = null;

    //-- name Strings
    @SuppressWarnings("unused")
	private final String ID = TimelineView.ID;
    @SuppressWarnings("unused")
	private final String TIME = TimelineView.TIME;

    //-- Components of the GUI -------------------------------------------------
    private JFrame GUI;
    private JPanel SQLpane;
    private JScrollPane SQLSpane;

    private SQLview sqlv;

    JButton dynamicControl;
    JPopupMenu popupmenu;
    FilterPopupMenu popup;

    //Menu parameter block------------------------------------------------------
    private JMenu fileMenu=new JMenu("File");   //define the file menu and its contents
        JMenuItem openFileItem=new JMenuItem("Open data file");
        JMenuItem openDBItem = new JMenuItem("Connect database");
        JMenuItem closeItem=new JMenuItem("Close current data");
        JMenuItem exitItem=new JMenuItem("Exit");

    JMenu dataMenu=new JMenu("Data");   //define the edit menu and its contents
        JMenuItem sampleDataItem = new JMenuItem("A Sample Data");
        JMenuItem preBlockItem=new JMenuItem("Previous N records");
        JMenuItem nextBlockItem=new JMenuItem("Next N records");

    JMenu helpMenu=new JMenu("Help");
        JMenuItem aboutItem=new JMenuItem("About SDSSLogViewer");
        JMenuItem helpItem=new JMenuItem("Help");

    JMenuBar systemMenubar;

    //-- System tool bar components
    JToolBar systemToolBar;
    JButton preBlockButton,
            nextBlockButton,
            openFileButton,
            openDatabaseButton,
            sampleDataButton,
            closeButton;
    JLabel systemStatus;
    JProgressBar progressBar;

    //- Data variables ---------------------------------------------------------
    private Table datatable = null;     //default is null

    private SDSSLogFileManager manager;

    /**
     * Construct a default view of SDSS Log Viewer.
     */
    public SQLViewerSeparatedGUI() throws SQLException, ClassNotFoundException, DataIOException{

        initUI();

    }   //end of constructor

    /** start local methods --------------------------------------------------*/

    private void initUI() throws SQLException, ClassNotFoundException{
        //-- . Initial an empty GUI
        GUI = new JFrame("SDSS SQL Content Viewer Alpha 0.2");

        //-- . set menu and tool bar icon, and set actions for menu items and icons
        systemMenubar = new JMenuBar();
        systemMenubar.add(fileMenu);
        systemMenubar.add(dataMenu);
        systemMenubar.add(helpMenu);

        fileMenu.add(openFileItem);
        fileMenu.add(openDBItem);
            openDBItem.setEnabled(false);
        fileMenu.add(closeItem);
        fileMenu.add(exitItem);
            closeItem.setEnabled(false);

        dataMenu.add(sampleDataItem);
        dataMenu.add(preBlockItem);
        dataMenu.add(nextBlockItem);
            preBlockItem.setEnabled(false);
            nextBlockItem.setEnabled(false);

        helpMenu.add(aboutItem);
        helpMenu.add(helpItem);

        openFileItem.setToolTipText("Open SDSS SQL log data from a local CSV file");
        openFileItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
					openLocalFileAction();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        openDBItem.setToolTipText("Connect to remote database to read SDSS SQL log data. " +
                "This version does not support yet");
        openDBItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                openDBAction();
            }
        });

        closeItem.setToolTipText("Close current visualizations");
        closeItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                closeAction();
            }
        });

        exitItem.setToolTipText("Exit the tool");
        exitItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                exitAction();
            }
        });

        sampleDataItem.setToolTipText("Directly load a sample data file");
        sampleDataItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
					openSampleDataAction();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        preBlockItem.setToolTipText("Read the previous block of data and visualize it");
        preBlockItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                getPreBlockAction();
            }
        });
        
        nextBlockItem.setToolTipText("Read the next block of data and visualize it");
        nextBlockItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
					getNextBlockAction();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        
        aboutItem.setToolTipText("Show about infomation");
        aboutItem.addActionListener(new ActionListener (){
            public void actionPerformed(ActionEvent e) {
                aboutAction();
            }
        });

        helpItem.setToolTipText("Show the help web page");
        helpItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                helpAction();
            }
        });

        GUI.setJMenuBar(systemMenubar);
        GUI.getContentPane().setLayout(new BorderLayout());

        systemToolBar = new JToolBar();

        preBlockButton = new JButton("", leftIcon);
        preBlockButton.setEnabled(false);
        preBlockButton.setToolTipText("Read the previous block of data and visualize it");
        
        nextBlockButton = new JButton("", rightIcon);
        nextBlockButton.setEnabled(false);
        nextBlockButton.setToolTipText("Read the next block of data and visualize it");

        openFileButton = new JButton("", openIcon);
        openFileButton.setToolTipText("Open SDSS SQL log data from a local CSV file");

        openDatabaseButton = new JButton("", databaseIcon);
        openDatabaseButton.setEnabled(false);
        openDatabaseButton.setToolTipText("Connect to remote database to read SDSS SQL log data. " +
                "This version does not support yet");

        sampleDataButton = new JButton("", sampleDataIcon);
        sampleDataButton.setToolTipText("Directly load a sample data file");

        closeButton = new JButton("", closeIcon);
        closeButton.setEnabled(false);
        closeButton.setToolTipText("Close current visualizations");

        preBlockButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                getPreBlockAction();
            }
        });

        nextBlockButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
					getNextBlockAction();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        openFileButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
					openLocalFileAction();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        openDatabaseButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                //TODO: openDBAction();
            }
        });

        sampleDataButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
					openSampleDataAction();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        closeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                closeAction();
            }
        });

        progressBar = new JProgressBar();
        progressBar.setMaximumSize(new Dimension(100, 20));
        progressBar.setVisible(true);

        systemStatus = new JLabel("Use Open Data File to view SDSS SQL log");
        systemStatus.setForeground(Color.GRAY);
        systemStatus.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));

        systemToolBar.setFloatable(false);
        systemToolBar.setRollover(true);
        systemToolBar.add(openFileButton);
        systemToolBar.add(openDatabaseButton);
        systemToolBar.add(sampleDataButton);
        systemToolBar.add(closeButton);
        systemToolBar.addSeparator(new Dimension(100,1));
        systemToolBar.add(preBlockButton);
        systemToolBar.add(nextBlockButton);
        systemToolBar.addSeparator();
        systemToolBar.add(progressBar);
        systemToolBar.addSeparator(new Dimension(10, 0));
        systemToolBar.add(systemStatus);

        GUI.getContentPane().add(systemToolBar, BorderLayout.PAGE_START);

        //--. intial an empty sql content view
        sqlv = new SQLview();
        SQLpane = new JPanel();
        SQLpane.setLayout(new BorderLayout());
        SQLpane.add(sqlv, BorderLayout.CENTER);
        SQLSpane = new JScrollPane(SQLpane);

        //--. intial dynamic query button and
        dynamicControl = new JButton("", upIcon);
        dynamicControl.setToolTipText("Show dynamic query menu");
        dynamicControl.setEnabled(false);
        dynamicControl.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                popupmenu.show(dynamicControl, 0, -POPUP_HEIGHT);
            }
        });
        popupmenu = new JPopupMenu();
        popup = new FilterPopupMenu();
        popupmenu.add(popup);
        update = new MenuChangedListener(){
            @Override
            public void menuChanged() {
                filter = (AndPredicate) popup.getPredicate();
                ((SQLview) SQLpane.getComponent(0)).updateDynamicQuery(filter);
            }
        };

        //-- . Layout the GUI frame and set visible
        GUI.getContentPane().add(SQLSpane, BorderLayout.CENTER);
        GUI.getContentPane().add(dynamicControl, BorderLayout.SOUTH);

        GUI.setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
        GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GUI.pack();
        GUI.setVisible(true);
    }

    /**
     * get sql log data from a local CSV file
     * @throws InterruptedException 
     */
    private void openLocalFileAction() throws InterruptedException{

        //--. Get the data file.
        try {
            //--. ask user to pick a CSV file and check if the CSV is sdss log data
            openLocalFile openfile = new openLocalFile(GUI);
            renderViz(openfile.getFile());

        } catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "Open failed!"+"\n"+
                        "File not opened. Please try later", "Open failed!", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /** 
     * get sql log data from remove database: default is my DB2 database
     */
    private void openDBAction(){

    }

    /**
     * close current skymap view and sql content view, replacing with empty window
     * And, clean all table data, collect garbage.
     */
    private void closeAction(){
        datatable.removeAllTableListeners();
        datatable.clear();
        datatable = null;
        manager = null;
        repaint();

        //unregister all listeners.
    }

    /** 
     * close this SDSS Log Viewer
     */
    private void exitAction(){
        System.exit(0);
    }

    private void openSampleDataAction() throws InterruptedException{
        File f = new File("./verysmall.csv");
        if (f.exists()){
            renderViz(f);
        } else
            JOptionPane.showMessageDialog(null, "Open failed!"+"\n"+
                        "File does not exists. Please make sure the verysmall.csv"
                        + "in the project's folder.", "Open failed!", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * retrieve table in the next block
     * @throws InterruptedException 
     */
    private void getNextBlockAction() throws InterruptedException{
        nextBlockButton.setEnabled(false);
        dynamicControl.setEnabled(false);
        progressBar.setIndeterminate(true);
        manager.getNextTable();
    }

    /**
     * retrieve table in the next block
     */
    private void getPreBlockAction(){
        preBlockButton.setEnabled(false);
        dynamicControl.setEnabled(false);
        progressBar.setIndeterminate(true);
        manager.getPreTable();
    }

    /**open a new window for information*/
    @SuppressWarnings("unused")
	private void aboutAction(){
        aboutInfoWindow aw = new aboutInfoWindow();
    }

    /**open a new browser window and link to project page*/
    private void helpAction(){
        //-- lauch system browser and link to my project page
        BrowserLauncher.showDocument(helpURL);
    }

    /**
     * Processing the data file, and make the data ready for rendering
     * @param localfile
     * @throws InterruptedException 
     */
    private void renderViz(File localfile) throws InterruptedException{

        //--. start progress bar
        progressBar.setIndeterminate(true);
        ChangeStatus("Open data file...");

        //--. if already has a table, close it firt.
        if (datatable != null){
            closeAction();
        }
        //--.
        try {
            if (localfile != null){
                manager = new SDSSLogFileManager(localfile);

                //--. add status change listener
                manager.addStatusChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        ChangeStatus(manager.getStatus());
                    }
                });

                //--. add data table ready listener
                manager.addDataTableListener(new DataTableListener(){
                    public void TableIsReady() {

                        //--1. cleanup the previous data table
                        if ( datatable != null){
                            ChangeStatus("Closing current data and viz...");
                            datatable.removeAllTableListeners();
                            datatable.clear();
                            datatable = null;
System.gc();
Runtime.getRuntime().gc();
System.out.println("data table is cleaned up. " + PrefuseLib.getMemoryUsageInMB());
                        }
                        //--2. read in new data table and convert to sdsstable
                        ChangeStatus("Parsing data...");
                        SDSSLogTable sdsstable = new SDSSLogTable(manager.getVizTable());
                        datatable =  sdsstable.getSDSSLogTable();
                        //--3. repaint the contents
                        ChangeStatus("Rendering visualizations...");
                        repaint();

                        //--4. After repaint, set next or pre buttons and menu enabled.
                        if (manager.hasNextTable()){
                            nextBlockButton.setEnabled(true);
                            nextBlockItem.setEnabled(true);
                        }
                        if (manager.hasPreTable()){
                            preBlockButton.setEnabled(true);
                            preBlockItem.setEnabled(true);
                        }

                        //--5. Stop progress bar
                        progressBar.setIndeterminate(false);
                        ChangeStatus("Visualization is ready to view and interact");
                    }
                });

                //--. Initlize the first reading
                manager.initManager();

            } else return;  //if not, just do nothing and return
        } catch (IOException ioe){
            JOptionPane.showMessageDialog(null, "Open failed!"+"\n"+
                        "File not opened. Please try later", "Open failed!", JOptionPane.INFORMATION_MESSAGE);
        } catch (DataIOException dioe){
            JOptionPane.showMessageDialog(null, "Open failed!"+"\n"+
                        "File not opened. Please try later", "Open failed!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void repaint() {

        if (datatable != null){

            //--1. clean up the previous vizs
            sqlv.cleanup();
            sqlv =null;
System.gc();
Runtime.getRuntime().gc();
System.out.println("Pre vizs are cleaned up. " + PrefuseLib.getMemoryUsageInMB());

            //--2. create new vizs
            sqlv = new SQLview(datatable);

            SQLpane.remove(0);

            SQLpane.add(sqlv);
            SQLpane.updateUI();
            
            //--3. create dynamic query panel
            ChangeStatus("Creating dynamic query bar...");
            createDynamicQuery();

            setControlComponent(true);

        } else {
            //--1. clean up the previous vizs
            sqlv.cleanup();
            sqlv =null;
            popup.cleanup();
            popup = null;
System.gc();
Runtime.getRuntime().gc();
System.out.println("Pre vizs are cleaned up. " + PrefuseLib.getMemoryUsageInMB());

            sqlv = new SQLview();
            SQLpane.remove(0);
            SQLpane.add(sqlv);
            SQLpane.updateUI();

            preBlockItem.setEnabled(false);
            preBlockButton.setEnabled(false);
            nextBlockItem.setEnabled(false);
            nextBlockButton.setEnabled(false);
            
            setControlComponent(false);
        }

        //TODO: After rendering the process, trigger a new DynQueryThread to read in next block
    }

    private void setControlComponent(boolean enable){
        closeItem.setEnabled(enable);
        closeButton.setEnabled(enable);
        dynamicControl.setEnabled(enable);
    }

    private void ChangeStatus(String status){
        systemStatus.setText(status);
    }

    private void createDynamicQuery(){

        if (popup != null){
            popup.cleanup();
            popup.removeAll();
            popup = null;
        }

        popup = new FilterPopupMenu(datatable);
        popup.setPreferredSize(new Dimension(GUI.getWidth()-10, POPUP_HEIGHT));
        popup.addMenuChangeListener(update);
        popupmenu.remove(0);
        popupmenu.add(popup);
System.gc();
Runtime.getRuntime().gc();
System.out.println(PrefuseLib.getMemoryUsageInMB());
System.out.println("This round is finished.");
    }

    /**
     * main method to start the tool
     * @param args the command line arguments
     */
    @SuppressWarnings("unused")
	public static void main(String[] args) throws SQLException, ClassNotFoundException, DataIOException {
        //set the look and feel to neat one.
        UILib.setPlatformLookAndFeel();

        SQLViewerSeparatedGUI sdss = new SQLViewerSeparatedGUI();
    }

}

