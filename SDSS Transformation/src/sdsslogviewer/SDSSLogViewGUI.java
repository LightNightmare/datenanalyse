package sdsslogviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.data.Schema;
import prefuse.data.io.DataIOException;
import prefuse.data.Table;
import prefuse.data.column.ColumnMetadata;
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
import sdsslogviewer.Viz.SkyMap;
import sdsslogviewer.Viz.TreeMapStat;
import sdsslogviewer.data.SDSSLogFileManager;
import sdsslogviewer.data.SDSSLogTable;
import sdsslogviewer.data.io.MyDBReader;
import sdsslogviewer.data.io.openLocalFile;
import sdsslogviewer.ui.SQLExampleWindow;
import sdsslogviewer.ui.aboutInfoWindow;


/**
 * Initial GUI of the SDSS Log Viewer. This is the start class.<p/>
 * This GUI contains menus, icons, three views, and one popup menu.<p/>
 * @author James<p/>
 *
 * June 7th, 2010: Start this project for real SDSS Log Viewer tool.<p/>
 *                  Test with prefuse on multiple displays.<p/>
 *          Each component is a Prefuse Display class<p/>
 *
 * Setp. 10th, 2010: Design two step viz.<p/>
 *                   Step 1: Show timeline with dynamic query field.<p/>
 *                   Step 2: Show SQL-content and skymap with a limited dynamic
 *                           functions.<p/>
 * Dec. 2nd, 2010: Start to integrate three views and add control and interactions<p/>
 *
 * Dec. 20th, 2010: First fully functional version<p/>
 *
 * Dec. 22nd, 2010: Fix dynamic query bugs. Functional now.<p/>
 *
 * Dec. 26th 2010: Functionality is fixed. Start to process data and database<p/>
 *
 * Dec. 30th 2010: Start to design UI process.<p/>
 *
 * Jan. 9th, 2011: Use simplified version of SDSSLogFileManager and single DynQueryThread
 *                 work-flow in this version for evaluation and testing.<p/>
 *
 * Jan. 10th, 2011:Confront a severe out of memory problem. Struggled with for five
 *                 days.<p/>
 *
 * Jan. 14th, 2011:Solve the out of memory problem PATIALLY(90%) and this version
 *                 can work now!!!!!!!!!!!!!!<p/>
 *
 * Jan. 15th, 2011:Add new contents to tool tips and solve tool tips exception.<p/>
 *
 * Jan. 17th, 2011: Add new JRangeValueSliderPlus in pop up dynamic query panel.
 *                  Now users can set a range to run time and no of rows.<p/>
 * 
 * Jan. 21st, 2011: Alpha 0.1 fix a few bugs, and add a new menu item to read in
 *                  the very small data set.<p/>
 *
 * Feb. 7th, 2011: Alpha 0.2 add display print out function.<p/>
 *
 * Feb. 16th, 2011: Create Web start version, replace absolute path with URLs.<p/>
 *
 * Feb. 27th, 2011: Create a version of visiting JHU. Add a new database connector
 *                  to local JDBC for timeline view and change Frequcency table's
 *                  string from "FREQ" to "FREQUENCY"<p/>
 *
 * March 1st, 2011: A beta version. Add new features, including:<p/>
 *
 * Done 3/6/11      1) improve SQL parser, and ready for database import.<p/>
 *                  2) try a new GUI layout, a Tabbedpane<p/>
 *                  3) create a new view for showing stat of tables, ......<p/>
 *                  4) create a database connector and a database manager<p/>
 *                  5) create a new single SQL example visualizer, to show one line of user inputs<p/>
 *                  6) try a more user-friendly select menu in dynamic query menu<p/>
 *
 * March 6th, 2011: Beta version 0.1 fixed bugs in SQL Parser, and improve the parser
 *                  with exp4j library.<p/>
 * 
 * March 6th, 2011: Try tabbed panel as GUI layout.<p/>
 *
 * March 7th, 2011: Beta version 0.2 revises GUI layout to a tabbed pane.<p/>
 * 
 * March 8th, 2011: Add a new TreeMap view for one ordinal variable statistic<p/>
 *
 * March 8th, 2011: Beta version 0.4 adds a new TreeMap statistic view to show usage
 *                  of columns.<p/>
 *
 * March 14th, 2011: Add a drop down menu to treemap. Need to add in the GUI part
 *                   which is a bad coding practice, but currently is the only way.
 *                   Done by March 15th.<p/>
 *
 * March 15th, 2011: Beta version 0.6 finished treemap view.<p/>
 *
 * March 16th, 2011: //TODO: Add a new window for showing demo SQL content sample.<p/>
 *
 * March 25th, 2011: Modified the SQL parser to accommodate the ObjeRectFunction.<p/>
 *
 * April 13th, 2011: Create sample visualizer window for user to see what their SQL
 *                   query will turn to.<p/>
 *
 * April 19th, 2011: Fixed the bug in statistics view for cross condition combination.<p/>
 */

@SuppressWarnings("unused")
public class SDSSLogViewGUI {

    //-- Constants for dimensions
    private final int GUI_WIDTH     = 1200,
                      GUI_HEIGHT    = 800,
                      UPDOWN_DIV    = 350,
                      LEFTRIGHT_DIV = 600,
                      POPUP_HEIGHT  = 240;

    private final String helpURL = "http://nevac.ischool.drexel.edu/~james/" +
                                   "SDSSLogViewer/SDSSLogViewer.html";

    private final String z_version = "SDSS SQL Log Viewer Version 1.1.0";

//    URL upIconURL = getClass().getResource("UpArrow-small.png");
	private final ImageIcon 
                upIcon = new ImageIcon(getClass().getResource("UpArrow-small.png")),
                downIcon = new ImageIcon(getClass().getResource("DownButton-small.png")),
                leftIcon = new ImageIcon(getClass().getResource("LeftArrow-small.png")),
                rightIcon = new ImageIcon(getClass().getResource("RightArrow-small.png")),
                openIcon =  new ImageIcon(getClass().getResource("OpenIcon.png")),
                databaseIcon = new ImageIcon(getClass().getResource("DatabaseIcon.png")),
                sampleDataIcon = new ImageIcon(getClass().getResource("SampleData-small.png")),
                closeIcon = new ImageIcon(getClass().getResource("CloseIcon.png"));
    //TODO: add a new SampleViz icon.

    //-- Control variables
    private AndPredicate filter = new AndPredicate();

    private MenuChangedListener update = null;

    //-- name Strings
    private final String ID = TimelineView.ID;
    private final String TIME = TimelineView.TIME;

    //-- Components of the GUI -------------------------------------------------
    private JFrame GUI;
    /**
     * March 6th, 2011: Try a tabbed pane to contain the three (planed four) views
     */
    private JTabbedPane container;

    private JSplitPane UpDown_splitPane;
    private JSplitPane LeftRight_splitPane;

    private JPanel tab1Pane;
    private JPanel tab2Pane;
    private JPanel SQLpane;
    private JScrollPane SQLSpane;
    private JPanel SkyMapPane;
    private JPanel TreeMapPane;

    private SkyMap sm;
    private SQLview sqlv;
    private TreeMapStat treemap;                     // 3/8 add new view

    private JRadioButton yearRB;
    private JRadioButton monthRB;
    private JRadioButton dayRB;

	@SuppressWarnings("rawtypes")
	private JComboBox TreeMapMenu;                        // 3/14 add new control
    private String[] cateColumns = {SDSSLogTable.IPADDRESS,
                                    SDSSLogTable.DATABASE,
                                    SDSSLogTable.ACCESSPORTAL,
                                    SDSSLogTable.REQUESTOR,
                                    SDSSLogTable.SERVER};

    private JButton dynamicQuery;
    private JPopupMenu popupmenu;
    private FilterPopupMenu popup;

    //Menu parameter block------------------------------------------------------
    private JMenu fileMenu=new JMenu("File");   //define the file menu and its contents
        JMenuItem openFileItem=new JMenuItem("Open data file");
        JMenuItem openDBItem = new JMenuItem("Connect database");
        JMenuItem closeItem=new JMenuItem("Close current data");
        JMenuItem exitItem=new JMenuItem("Exit");

    private JMenu dataMenu=new JMenu("Data");   //define the data menu and its contents
        JMenuItem sampleDataItem = new JMenuItem("A Sample Data");
        JMenuItem preBlockItem=new JMenuItem("Previous N records");
        JMenuItem nextBlockItem=new JMenuItem("Next N records");

   private JMenu sampleMenu = new JMenu("SampleViz"); //define the sample menu
        JMenuItem sampleVizItem = new JMenuItem("See SQL viz demo");

    private JMenu helpMenu=new JMenu("Help");
        JMenuItem aboutItem=new JMenuItem("About SDSSLogViewer");
        JMenuItem helpItem=new JMenuItem("Help");

    private JMenuBar systemMenubar;

    //-- System tool bar components
    private JToolBar systemToolBar;
    private JButton preBlockButton,
            nextBlockButton,
            openFileButton,
            openDatabaseButton,
            sampleDataButton,
            closeButton;
    private JLabel systemStatus;
    private JProgressBar progressBar;

    //- Data variables ---------------------------------------------------------
    private Table datatable = null;     //default is null
    private Table table_freq;

    private MyDBReader timeReader;
    private SDSSLogFileManager manager;

    /**
     * Construct a default view of SDSS Log Viewer.
     */
    public SDSSLogViewGUI() throws SQLException, ClassNotFoundException, DataIOException{

        /**
         * Feb. 27th, 2011, adding this function to check network connection.
         * If no connection, go for a local JDBC data source
        */
        try {
        timeReader = new MyDBReader("jz85", "zhjwy9343");
        } catch (Exception e1){
            try {
            timeReader = new MyDBReader("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:freq_dist_table",
                    "", "");
            } catch (Exception e2){
                System.err.println("Fail to build connection to database. System exit!");
                System.exit(1);
            }
        }

        initUI();

    }   //end of constructor

    /** start local methods --------------------------------------------------*/
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void initUI() throws SQLException, ClassNotFoundException{
        //-- . Initial an empty GUI
        GUI = new JFrame(z_version);

        container = new JTabbedPane();

        UpDown_splitPane = new JSplitPane();
        LeftRight_splitPane = new JSplitPane();

        //-- . set menu and tool bar icon, and set actions for menu items and icons
        systemMenubar = new JMenuBar();
        systemMenubar.add(fileMenu);
        systemMenubar.add(dataMenu);
        systemMenubar.add(sampleMenu);
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

        sampleMenu.add(sampleVizItem);

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

        sampleVizItem.setToolTipText("Try your SQLs to see what they will be represented");
        sampleVizItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                showSampleAction();
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
        systemToolBar.addSeparator(new Dimension(200,1));
        systemToolBar.add(preBlockButton);
        systemToolBar.add(nextBlockButton);
        systemToolBar.addSeparator();
        systemToolBar.add(progressBar);
        systemToolBar.addSeparator(new Dimension(10, 0));
        systemToolBar.add(systemStatus);

        GUI.getContentPane().add(systemToolBar, BorderLayout.PAGE_START);

        //-- . intial timeline view; set default as Day view
        yearRB = new JRadioButton("Year");
        monthRB =  new JRadioButton("Month");
        dayRB = new JRadioButton("Day");
        dayRB.setSelected(true);
        Box radiobox = new Box(BoxLayout.X_AXIS);
        radiobox.add(Box.createHorizontalStrut(10));
        radiobox.add(dayRB);
        radiobox.add(Box.createHorizontalStrut(10));
        radiobox.add(monthRB);
        radiobox.add(Box.createHorizontalStrut(10));
        radiobox.add(yearRB);
        ButtonGroup bg = new ButtonGroup();
        bg.add(yearRB);
        bg.add(monthRB);
        bg.add(dayRB);

        tab1Pane = new JPanel();
        tab1Pane.setPreferredSize(new Dimension(1000, 600));
        tab1Pane.setLayout(new BorderLayout());
        tab1Pane.setBorder(BorderFactory.createLoweredBevelBorder());
        tab1Pane.add(radiobox, BorderLayout.NORTH);

        yearRB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    table_freq = timeReader.getTable("select sum(FREQ) as FREQUENCY, YY from FREQ_DIST_TABLE"
                                                    + " group by YY"
                                                    + " order by YY");
                    table_freq.addColumn(ID, int.class);
                    table_freq.addColumn(TIME, "CONCAT(YY)");
                    ColumnMetadata cmdTime = table_freq.getMetadata(TIME);
                    cmdTime.setComparator(new timeComparator());

                    tab1Pane.remove(1);
                    TimelineView tlv = new TimelineView(table_freq);
                    tab1Pane.add(tlv, BorderLayout.CENTER);
                    tab1Pane.updateUI();

                } catch (SQLException ex) {
                    Logger.getLogger(TimelineView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TimelineView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        monthRB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    table_freq = timeReader.getTable("select sum(FREQ) as FREQUENCY, YY, MM from FREQ_DIST_TABLE"
                                                    + " group by YY, MM"
                                                    + " order by YY, MM");
                    table_freq.addColumn(ID, int.class);
                    table_freq.addColumn(TIME, "CONCAT(MM, '-', YY)");
                    ColumnMetadata cmdTime = table_freq.getMetadata(TIME);
                    cmdTime.setComparator(new timeComparator());

                    tab1Pane.remove(1);
                    TimelineView tlv = new TimelineView(table_freq);
                    tab1Pane.add(tlv, BorderLayout.CENTER);
                    tab1Pane.updateUI();

                } catch (SQLException ex) {
                    Logger.getLogger(TimelineView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TimelineView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        dayRB.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    table_freq = timeReader.getTable("select FREQ as FREQUENCY, YY, MM, DD from FREQ_DIST_TABLE");
                    table_freq.addColumn(ID, int.class);
                    table_freq.addColumn(TIME, "CONCAT(MM, '-', DD, '-', YY)");
                    ColumnMetadata cmdTime = table_freq.getMetadata(TIME);
                    cmdTime.setComparator(new timeComparator());

                    tab1Pane.remove(1);
                    TimelineView tlv = new TimelineView(table_freq);
                    tab1Pane.add(tlv, BorderLayout.CENTER);
                    tab1Pane.updateUI();

                } catch (SQLException ex) {
                    Logger.getLogger(TimelineView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TimelineView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        table_freq = timeReader.getTable("select FREQ as FREQUENCY, YY, MM, DD from FREQ_DIST_TABLE");
        table_freq.addColumn(ID, int.class);
        table_freq.addColumn(TIME, "CONCAT(MM, '-', DD, '-', YY)");
        ColumnMetadata cmdTime = table_freq.getMetadata(TIME);
        cmdTime.setComparator(new timeComparator());

        TimelineView tlv = new TimelineView(table_freq);
        tab1Pane.add(tlv, BorderLayout.CENTER);

        //--. initialize an empty skymap view
        sm = new SkyMap();
        SkyMapPane = new JPanel();
        SkyMapPane.setLayout(new BorderLayout());
        SkyMapPane.add(sm, BorderLayout.CENTER);

        //--. initialize TreeMap view
        TreeMapPane = new JPanel();

        TreeMapMenu = new JComboBox(cateColumns);
        TreeMapMenu.setEnabled(false);
        TreeMapMenu.setMaximumSize(new Dimension(100, 15));
        Box treemapBox = new Box(BoxLayout.X_AXIS);
        treemapBox.add(Box.createHorizontalGlue());
        treemapBox.add(TreeMapMenu);

        treemap = new TreeMapStat();
        TreeMapPane.setLayout(new BorderLayout());
        TreeMapPane.add(treemapBox, BorderLayout.NORTH);
        //NOTE XXX: add treemap after drop down menu, to make sure we remove item
        //          1 from treemap pane to update treemap.
        //March 15th: Not need to remove the dropdown menu, just change its model.
        TreeMapPane.add(treemap, BorderLayout.CENTER);

        //--. Add SkyMap and TreeMap to updown split pane
        UpDown_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        UpDown_splitPane.setDividerLocation(UPDOWN_DIV);
        UpDown_splitPane.setTopComponent(SkyMapPane);
        UpDown_splitPane.setBottomComponent(TreeMapPane);

        //--. initialize an empty sql content view
        sqlv = new SQLview();
        SQLpane = new JPanel();
        SQLpane.setLayout(new BorderLayout());
        SQLpane.add(sqlv, BorderLayout.CENTER);
        SQLSpane = new JScrollPane(SQLpane);

        LeftRight_splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        LeftRight_splitPane.setDividerLocation(LEFTRIGHT_DIV);
        LeftRight_splitPane.setLeftComponent(SQLSpane);
        LeftRight_splitPane.setRightComponent(UpDown_splitPane);

        //--. initialize dynamic query button and
        dynamicQuery = new JButton("", upIcon);
        dynamicQuery.setToolTipText("Show dynamic query menu");
        dynamicQuery.setEnabled(false);
        dynamicQuery.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                popupmenu.show(dynamicQuery, 0, -POPUP_HEIGHT);
            }
        });
        popupmenu = new JPopupMenu();
        popup = new FilterPopupMenu();
        popupmenu.add(popup);
        update = new MenuChangedListener(){
            @Override
            public void menuChanged() {
                filter = (AndPredicate) popup.getPredicate();
                ((SkyMap) SkyMapPane.getComponent(0)).updateDynamicQuery(filter);
                ((SQLview) SQLpane.getComponent(0)).updateDynamicQuery(filter);
                ((TreeMapStat) TreeMapPane.getComponent(1)).updateDynamicQuery(filter);
            }
        };

        tab2Pane = new JPanel();
        tab2Pane.setBorder(BorderFactory.createLoweredBevelBorder());
        tab2Pane.setLayout(new BorderLayout());
        tab2Pane.add(LeftRight_splitPane, BorderLayout.CENTER);
        tab2Pane.add(dynamicQuery, BorderLayout.SOUTH);

        //-- . initialize the tabbed pane and add two tabbed pane for the GUI.
        container.addTab("Timeline Panel", null, tab1Pane, "This view shows SDSS SQL traffic");
        container.addTab("SQL Content Panel", null, tab2Pane, "This view shows contents of SDSS SQL log");

        //-- . Layout the GUI frame and set visible
        GUI.getContentPane().add(container, BorderLayout.CENTER);
//        GUI.getContentPane().add(dynamicQuery, BorderLayout.SOUTH);

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

        URL sampleFileURL = this.getClass().getResource("verysamll.csv");

        File f = null;
        try {
            f = new File(sampleFileURL.toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(SDSSLogViewGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        dynamicQuery.setEnabled(false);
        progressBar.setIndeterminate(true);
        manager.getNextTable();
    }

    /**
     * retrieve table in the next block
     */
    private void getPreBlockAction(){
        preBlockButton.setEnabled(false);
        dynamicQuery.setEnabled(false);
        progressBar.setIndeterminate(true);
        manager.getPreTable();
    }

    /**
     * Show an new window to allow user input some SQL and show what they will be
     * presented in SQL content view.
     */
    private void showSampleAction(){
        SQLExampleWindow sampleWin = new SQLExampleWindow();
        //TODO: set Window focused and disable.
    }

    /**open a new window for information*/
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

                //-- . switch the tab pane: If in tab1, switch to tab2. Otherwise
                //     stay on tab2.
                if (container.getSelectedIndex()==0){
                    container.setSelectedIndex(1);
                }

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

    /**
     * Actual paint work for the three views.<p/>
     */
    private void repaint() {

        if (datatable != null){

            //--1. clean up the previous vizs
            sm.cleanup();
            sm = null;
            sqlv.cleanup();
            sqlv = null;
System.gc();
Runtime.getRuntime().gc();
System.out.println("Pre vizs are cleaned up. " + PrefuseLib.getMemoryUsageInMB());

            //--2. create new vizs
            sm = new SkyMap(datatable);
            sqlv = new SQLview(datatable);

            SkyMapPane.remove(0);
            SQLpane.remove(0);

            SkyMapPane.add(sm);
            SQLpane.add(sqlv);

            SkyMapPane.updateUI();
            SQLpane.updateUI();

            //--3. create tree map view and drop down menu contents
            TreeMapMenu.setEnabled(true);
            TreeMapMenu.addActionListener(new ActionListener() {
                @SuppressWarnings("rawtypes")
				@Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox box = (JComboBox) e.getSource();
                    String column = (String) box.getSelectedItem();
                    
                    createTreeMapContent(column, ((TreeMapStat) TreeMapPane.getComponent(1)).getInnerPredicate());
                }
            });

            createTreeMapContent((String)TreeMapMenu.getSelectedItem(), null);

            //--4. create dynamic query panel
            ChangeStatus("Creating dynamic query bar...");
            createDynamicQuery();

            setControlComponent(true);

System.gc();
Runtime.getRuntime().gc();
System.out.println(PrefuseLib.getMemoryUsageInMB());
System.out.println("This round is finished.");

        } else {
            //--1. clean up the previous vizs
            sm.cleanup();
            sm = null;
            sqlv.cleanup();
            sqlv =null;
            treemap.cleanup();
            treemap = null;
            popup.cleanup();
            popup = null;
System.gc();
Runtime.getRuntime().gc();
System.out.println("Pre vizs are cleaned up. " + PrefuseLib.getMemoryUsageInMB());

            sm = new SkyMap();
            SkyMapPane.remove(0);
            SkyMapPane.add(sm);
            SkyMapPane.updateUI();

            sqlv = new SQLview();
            SQLpane.remove(0);
            SQLpane.add(sqlv);
            SQLpane.updateUI();

            //-- treate the treemap view separately.
            TreeMapMenu.setEnabled(false);
            treemap = new TreeMapStat();
            TreeMapPane.remove(1);
            TreeMapPane.add(treemap, BorderLayout.CENTER);
            TreeMapPane.updateUI();

            preBlockItem.setEnabled(false);
            preBlockButton.setEnabled(false);
            nextBlockItem.setEnabled(false);
            nextBlockButton.setEnabled(false);
            
            setControlComponent(false);
        }
    } //end of repaint method

    /**
     * Create initial tree map view.<p/>
     * @param column<p/>
     * @param outter_Pred <p/>
     */
    private void createTreeMapContent(String column, AndPredicate outter_Pred){

        treemap.cleanup();
        treemap = null;

        treemap = new TreeMapStat(datatable, column);
        TreeMapPane.remove(1);
        TreeMapPane.add(treemap, BorderLayout.CENTER);
        TreeMapPane.updateUI();
        
        //If there is an predicate, update the content with predicate after creating
        //the new contents.
        if (outter_Pred != null){
            ((TreeMapStat) TreeMapPane.getComponent(1)).updateDynamicQuery(outter_Pred);
        }
    }

    /**
     * Get all string columns from the table's column names<p/>
     * @param t
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private String[] getStringColumns(Table t){
        ArrayList stringColumns = new ArrayList();
        Schema mds = t.getSchema();
        for (int i=0;i<mds.getColumnCount();i++){
            if (mds.getColumnType(i) == String.class){
                stringColumns.add(mds.getColumnName(i));
            }
        }
        String[] columns = (String[]) stringColumns.toArray(new String[stringColumns.size()]);
        return columns;
    }

    /**
     * enable or disable control components, including, left and right block arrow
     * and dynamic query button.<p/>
     * @param enable<p/>
     */
    private void setControlComponent(boolean enable){
        closeItem.setEnabled(enable);
        closeButton.setEnabled(enable);
        dynamicQuery.setEnabled(enable);
    }

    /**
     * change the displayed system status.<p/>
     * @param status<p/>
     */
    private void ChangeStatus(String status){
        systemStatus.setText(status);
    }

    /**
     * Create the dynamic query popup window based on current data table.<p/>
     */
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
    }

    /**
     * main method to start the tool<p/>
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, DataIOException {
        //set the look and feel to neat one.
        UILib.setPlatformLookAndFeel();

        SDSSLogViewGUI sdss = new SDSSLogViewGUI();
    }

    /**
     * inner class to implement a comparator, comparing the time string,
     * o1 and o2 are the time String
     * 1. tX[2] = year;
     * 2. tX[1] = month;
     * 3. tX[0] = day;
     */
    @SuppressWarnings("rawtypes")
	private static class timeComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;

            String[] t1 = s1.split("-");
            String[] t2 = s2.split("-");

            int size = t1.length;

            switch (size) {
                case 3: if (Integer.parseInt(t1[2])> Integer.parseInt(t2[2])){
                            return 1;
                        } else {
                            if (Integer.parseInt(t1[2])< Integer.parseInt(t2[2])) {
                                return -1;
                            } else {
                                //year1 = year2
                                if (Integer.parseInt(t1[0])> Integer.parseInt(t2[0])){
                                    return 1;
                                } else {
                                    if (Integer.parseInt(t1[0])< Integer.parseInt(t2[0])){
                                        return -1;
                                    } else {
                                        //month1 = month2
                                        if (Integer.parseInt(t1[1])> Integer.parseInt(t2[1])){
                                            return 1;
                                        } else {
                                            if (Integer.parseInt(t1[1])> Integer.parseInt(t2[1])){
                                                return -1;
                                            } else
                                                //day1 = day2;
                                                return 0;
                                        }
                                    }
                                }
                            }
                        }   //end if comparebreak;
                case 2: if (Integer.parseInt(t1[1])> Integer.parseInt(t2[1])){
                            return 1;
                        } else {
                            if (Integer.parseInt(t1[1])< Integer.parseInt(t2[1])) {
                                return -1;
                            } else {
                                //year1 = year2
                                if (Integer.parseInt(t1[0])> Integer.parseInt(t2[0])){
                                    return 1;
                                } else {
                                    if (Integer.parseInt(t1[0])< Integer.parseInt(t2[0])){
                                        return -1;
                                    } else
                                        return 0;
                                    }
                                }
                            }   //end if comparebreak;
                case 1: if (Integer.parseInt(t1[0])> Integer.parseInt(t2[0])){
                            return 1;
                        } else {
                            if (Integer.parseInt(t1[0])< Integer.parseInt(t2[0])) {
                                return -1;
                            } else
                                return 0;
                        } // end if comprabreak;
                default: return 0;
            }
        } //end of compare method
    } //end of inner class comparator

}

