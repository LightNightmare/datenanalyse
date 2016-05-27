package sdsslogviewer.Viz;

//--import java classes
import javax.swing.*;
import java.awt.*;

//--import prefuse classes
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Iterator;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ControlAdapter;
import prefuse.data.*;
import prefuse.data.column.ColumnMetadata;
import prefuse.data.expression.AndPredicate;
import prefuse.data.query.ObjectRangeModel;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.VisiblePredicate;

import prefusePlus.Action.CompositedColorAction;
import prefusePlus.Renderer.CompositedShapeRenderer;
import prefusePlus.controls.SQLLegendToolTipControl;
import prefusePlus.controls.SQLToolTipControl;
import prefusePlus.util.ColorLibPlus;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;
import sdsslogviewer.data.SDSSLogTable;

/**
 * The Display of SDSS SQL Content View. Use a Table to construct a color-coded
 * bar line visual representation. And allow interactive, mainly the tow kinds
 * of tool tips.<p/>
 * 
 * June 8th, 2010: Create this class for display SQL visualization.<p/>
 *          TODO:: What is the data type for a SQL query??<p/>
 *          Prefuse give tables as one of the data structures. Hard to process the SQLs.<p/>
 *
 * August 5th, 2010: First successfully implements the color-coded rectangles with
 *                   the width of rectangles are set proportionally to their SQL lengths.<p/>
 *
 * Nov. 2nd, 2019:  Set the layout and zoom function properly. Could wait for real
 *                  SQL comes in.<p/>
 * 
 * @author James
 */
@SuppressWarnings("serial")
public class SQLview extends Display {

    private final String TABLENAME = "sdsslog";
//    public static final String COLORS = "_colors";
    private final String Y_AXIS = SDSSLOGVIEWCONSTANTS.TIMEID;
    private final String X_AXIS = SDSSLOGVIEWCONSTANTS.CONSTANT;
    /**
     * Local variables for SQL view
     */
    private Rectangle2D dataB = new Rectangle2D.Double();
    private Rectangle2D ylabB = new Rectangle2D.Double();
    private final int SQLview_WIDTH = 896;
    private final int SQLview_HEIGHT = 700;
    private final int SIZE = 1;
    /** 
     * A background color, which do NOT interfere with the twelve basic colors.
     * Therefore it could be used by other classes.<p/>
     */
    public static final Color BACKGROUNDCOLOR = ColorLib.getColor(75, 75, 75);

    @SuppressWarnings("unused")
	private VisibilityFilter z_filter;
    private AndPredicate inner_predicate = new AndPredicate(),
                         outer_predicate = null;

    private JFastLabel z_info;
    @SuppressWarnings("unused")
	private JSearchPanel z_search;
    private JPopupMenu popup;
    private JMenuItem z_print = new JMenuItem("Export image");

    private int TOTAL,
                SHOWING;

    /**
     * a null table for coordinate only. This constructor is for initial, empty
     * GUI.<p/>
     */ 
    public SQLview() {

        super(new Visualization());
        this.setBackground(BACKGROUNDCOLOR);
        this.setSize(SQLview_WIDTH, SQLview_HEIGHT);

    }

    /**
     * Construct a SQL Content View with a specified Table, which is a SDSS Log
     * Table actually. <p/>
     * @param t - the SDSS Log Table
     */
    @SuppressWarnings("unused")
	public SQLview(Table t) {

        super(new Visualization());

        ColumnMetadata cmdTime = t.getMetadata(SDSSLOGVIEWCONSTANTS.TIMEID);
        cmdTime.setComparator(new timeComparator());

        t.addColumn(SDSSLOGVIEWCONSTANTS.COLORS, int[].class);

        VisualTable vt = m_vis.addTable(TABLENAME, t);

        /**1. set renderer */
        final Renderer yaxisRender = new AxisRenderer(Constants.FAR_LEFT, Constants.CENTER);
        final Renderer xaxisRender = new AxisRenderer(Constants.CENTER, Constants.FAR_TOP);

        // Original Renderer that can display the contents of SQL queries
//       final LabelRenderer text= new LabelRenderer(CONTENT);
//            text.setHorizontalAlignment(Constants.LEFT);
//       final ShapeRenderer text=new ShapeRenderer(1);

        // Use my new Renderer to render the SQL contents
        final CompositedShapeRenderer text = new CompositedShapeRenderer(SIZE);
//       text.setSize(5);

        //creates a new default renderfactory to replace to default one, and assign
        //visual items to their proper renderers.
        DefaultRendererFactory drf = new DefaultRendererFactory() {

            @Override
            public Renderer getRenderer(VisualItem vi) {
                return vi.isInGroup("xaxis") ? xaxisRender : vi.isInGroup("yaxis") ? yaxisRender : text;
            }
        };
        m_vis.setRendererFactory(drf);

        // the color palette used for color-coding future SQL types
        int[] palette = ColorLibPlus.getBasicColorPallete(11);

        /**
         *  My new Composited color action, assigning the "_COLORS" colum that contains a
         *  list of color, with the pre-defined palette. So the CompositedShapeRender can
         *  find the correct color to render.
         */
        CompositedColorAction colors = new CompositedColorAction(TABLENAME, SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, palette);
//        DataColorAction textcolor=new DataColorAction(tname, ORDER,
//                Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
//        textcolor.setDefaultColor(ColorLib.rgb(255,150,150));
//        SizeAction textsize=new SizeAction(tname, 1.0);

//        CompositedShapeAction bounds = new CompositedShapeAction(TABLENAME, TOKENS);

        AxisLayout X = new AxisLayout(TABLENAME, X_AXIS, Constants.X_AXIS, VisiblePredicate.TRUE);
        X.setLayoutBounds(dataB);
        AxisLayout Y = new AxisLayout(TABLENAME, Y_AXIS, Constants.Y_AXIS, VisiblePredicate.TRUE);
        Y.setLayoutBounds(dataB);

        ObjectRangeModel timelabels = new ObjectRangeModel(new String[]{"TIME", " "});
        AxisLabelLayout xline = new AxisLabelLayout("xaxis", Constants.X_AXIS, timelabels, ylabB);

        AxisLabelLayout yline = new AxisLabelLayout("yaxis", Y, ylabB);
        ColumnMetadata cmd = t.getMetadata(Y_AXIS);
        Object[] times = cmd.getOrdinalArray();
        ObjectRangeModel orm = new ObjectRangeModel(times);
        yline.setRangeModel(orm);

        /**
         * May 14th, 2011: Add this one line to fix the time alignment bug. Now
         *                 the lines are assigned to their position correctly.
         */
        Y.setRangeModel(orm);

        ActionList paint = new ActionList();
        paint.add(xline);
        paint.add(yline);
        paint.add(X);
        paint.add(Y);
//        paint.add(textcolor);
        paint.add(colors);
//        paint.add(textsize);
        paint.add(new RepaintAction());

        ActionList update = new ActionList();
        update.add(colors);
        update.add(xline);
        update.add(yline);
        update.add(X);
        update.add(Y);
        update.add(new RepaintAction());

        //put actions to work and start run renderers.
        m_vis.putAction("paint", paint);
        m_vis.putAction("update", update);

        //set statistics info
        TOTAL = t.getRowCount();
//        SearchQueryBinding searchQ = new SearchQueryBinding(vt, SDSSLogTable.STATEMENT, new KeywordSearchTupleSet());
//        z_search = searchQ.createSearchPanel();
//        z_search.setLabelText("Search>>");
//        z_search.setShowCancel(true);
//        z_search.setShowBorder(true);
//        z_search.setShowResultCount(true);

        z_info = new JFastLabel();
        z_info.setBackground(BACKGROUNDCOLOR);
        z_info.setForeground(Color.WHITE);
        z_info.setPreferredSize(new Dimension(SQLview_WIDTH, 30));
        z_info.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
        z_info.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        z_info.setHorizontalAlignment(SwingConstants.LEFT);
        z_info.setText("Total: " + TOTAL + "queries; Showing " + TOTAL + " queries");

        Box infobox = new Box(BoxLayout.X_AXIS);
        infobox.add(Box.createHorizontalStrut(5));
//        infobox.add(z_search);
        infobox.add(z_info);
        infobox.add(Box.createHorizontalGlue());
        infobox.add(Box.createHorizontalStrut(5));

        //set up the features of Display field
        this.setBackground(BACKGROUNDCOLOR);
        this.setSize(SQLview_WIDTH, (t.getRowCount() * (SIZE) + 120));

        //--run rendering
        setDisplay();

        this.setLayout(new BorderLayout());
        this.add(infobox, BorderLayout.NORTH);

        //put interactions with Display at work.
        String[] tipcolumns = {SDSSLogTable.TIME,
                               SDSSLogTable.IPADDRESS,
                               SDSSLogTable.STATEMENT,
                               SDSSLogTable.ACCESSPORTAL,
                               SDSSLogTable.DATABASE,
                               SDSSLogTable.REQUESTOR,
                               SDSSLogTable.SERVER};
        SQLToolTipControl ttc = new SQLToolTipControl(tipcolumns);
        this.addControlListener(ttc);
        this.addControlListener(new SQLLegendToolTipControl());
//        this.addControlListener(new WheelZoomControl());
//        this.addControlListener(new ZoomToFitControl());
//        this.addControlListener(new PanControl());

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setDisplay();
            }
        });

        //Feb. 7th, 2011. Add print out function
        popup = new JPopupMenu();
        popup.add(z_print);

        z_print.addActionListener(new ExportDisplayAction(this));

        this.addControlListener(new ControlAdapter(){
            public void mouseClicked(MouseEvent e){
                if (UILib.isButtonPressed(e, RIGHT_MOUSE_BUTTON)){
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        //--Keep the tool tip showing till move out
        int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();

        dismissDelay = Integer.MAX_VALUE;
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
    }

    /**
     * Accept additional Predicate from outsiders to dynamically control the
     * visibility of items in this SQL Content View.<p/>
     */
    @SuppressWarnings("rawtypes")
	public void updateDynamicQuery(AndPredicate p){
        //--. remove existing filter;
        m_vis.removeAction("filter");
        //--. get new filter predicate
        AndPredicate temp_pred = new AndPredicate();
        if (p != null){
            outer_predicate = new AndPredicate();
            outer_predicate.add(p);
//            temp_pred.add(inner_predicate);
            temp_pred.add(outer_predicate);
        } else{
            temp_pred.add(inner_predicate);
        }
        //--. filter rows and update
        ActionList filter = new ActionList();
        filter.add(new VisibilityFilter(TABLENAME, temp_pred));
        m_vis.putAction("filter", filter);
        m_vis.run("filter");
        m_vis.run("update");
        //--. check number of visible item and show
        VisualItem item = null;
        Iterator it = m_vis.items(TABLENAME);
        SHOWING = 0;
        while (it.hasNext()){
            item = (VisualItem)it.next();
            if (temp_pred.getBoolean(item)){
                ++SHOWING;
            }
        }
        z_info.setText("Total: " + TOTAL + "queries; Showing " + SHOWING + " queries");
    }

    @Override
    /**
     * Method override createToolTip to change tool tip's colors.<p/>
     */
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBackground(BACKGROUNDCOLOR);
        tip.setForeground(Color.YELLOW);

        return tip;
    }

    private void setDisplay() {
        Insets i = this.getInsets();
        int w = this.getWidth();
        int h = this.getHeight();
        int leftspace = 130;
        int topspace = 50;
        int bottomspace = 70;

        ylabB.setRect(i.left + leftspace, i.top + topspace, i.left + 20, h - bottomspace);
        dataB.setRect(i.left + leftspace, i.top + topspace, w - i.left - i.right, h - i.top - i.bottom - bottomspace);

        m_vis.run("paint");
    }

    /**
     * cleanup the component of this display and release all listener. This operation
     * will release the majority of memory for future use.<p/>
     */
    public void cleanup(){
        //--. clean up this display
        this.setVisualization(null);
        this.reset();
    }


    /**
     * an inner class to sort the timeid column
     * String:  11/30/2004 11:58:00 PM:4
     * Order:   year->month->day->a/pm->hour->min->second->seq
     *          2004  30     11   PM    11    58   00      4
     *
     * May 14th, 2011: Change the way that timeID field constructed. So now the
     *                 timeID field is:
     * String:  2005:1:2:11:58:01:1
     * Order:   year->month->day->hour->min->sec->seqid
     *
     */
    @SuppressWarnings("rawtypes")
	private static class timeComparator implements Comparator{

        public int compare(Object o1, Object o2) {

            String s1 = (String) o1;
            String s2 = (String) o2;

            String[] t1 = s1.split("[/:\\s]");
            String[] t2 = s2.split("[/:\\s]");  //May 14th, 2011 still ok

            try {
                if ((t1.length<6)||(t2.length<6)){
                    return 0;
                } else {
                    if (Integer.parseInt(t1[0])> Integer.parseInt(t2[0])){
                        return 1;
                    } else {
                        if (Integer.parseInt(t1[0])< Integer.parseInt(t2[0])) {
                            return -1;
                        } else {
                            //year1 = year2
                            if (Integer.parseInt(t1[1])> Integer.parseInt(t2[1])){
                                return 1;
                            } else {
                                if (Integer.parseInt(t1[1])< Integer.parseInt(t2[1])){
                                    return -1;
                                } else {
                                    //month1 = month2
                                   if (Integer.parseInt(t1[2])> Integer.parseInt(t2[2])){
                                        return 1;
                                    } else {
                                        if (Integer.parseInt(t1[2])< Integer.parseInt(t2[2])){
                                            return -1;
                                        } else {
                                         //day1 = day2;
                                            if (Integer.parseInt(t1[3])>Integer.parseInt(t1[3])){
                                                return 1;
                                            } else {
                                                if (Integer.parseInt(t1[3])<Integer.parseInt(t1[3])){
                                                    return -1;
                                                } else {
                                                    if (Integer.parseInt(t1[4])>Integer.parseInt(t2[4])){
                                                        return 1;
                                                    } else {
                                                        if (Integer.parseInt(t1[4])<Integer.parseInt(t2[4])){
                                                            return -1;
                                                        } else {
                                                            if (Integer.parseInt(t1[5])>Integer.parseInt(t2[5])){
                                                                return 1;
                                                            } else {
                                                                if (Integer.parseInt(t1[5])<Integer.parseInt(t2[5])){
                                                                    return -1;
                                                                } else {
                                                                    if (Integer.parseInt(t1[6])>Integer.parseInt(t2[6])){
                                                                        return 1;
                                                                    } else {
                                                                        if (Integer.parseInt(t1[6])<Integer.parseInt(t2[6])){
                                                                            return -1;
                                                                        } else return 0;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }   //end if comparebreak;
            } catch (NumberFormatException wnfe){
                return 0;
            }

        }   //end compare method
    }   //end of inner class

}
