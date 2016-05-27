
package sdsslogviewer.Viz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import java.util.Comparator;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.Display;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.ToolTipControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.column.ColumnMetadata;
import prefuse.data.expression.AndPredicate;
import prefuse.data.query.RangeQueryBinding;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.UpdateListener;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.ui.JRangeSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefusePlus.Renderer.LineShapeRenderer;
import prefusePlus.util.ColorLibPlus;


/**
 * This class create the Timeline View in the SDSS Log Viewer. It needs a time-frequncy
 * table and draw a time line in an x-y cordinate.<p/>
 * 
 *  August 1st, 2010:   Class created. Originally to test method for drawing line
 *                      by modifying prefuse's class.<p/>
 *
 *  Sept. 21th, 2010:   Move to log view package to become the time line view.
 *                      Borrow some codes and ideas from prefuse.demo.GraphView<p/>
 *
 *  Oct. 20, 2010:      Change data.io by reading in table from remote DB and process
 *                      timeline from the FREQ_DIST_TABLE. Then do the control later
 *                      after sampling.<p/>
 * @author James
 */
@SuppressWarnings("serial")
public class TimelineView extends Display {

    /** the three field required in a FREQ_DIST_TABLE table */
    public final static String ID = "ID",
                                FREQUENCY = "FREQUENCY",
                                TIME ="time";

    //-- the data group name
    private final String TIMELINE="timeline";

    //-- the line group name
    private final String SOURCE="sourceitem";
    private final String TARGET="targetitem";
    private final String EDGE="edge";

    Display timelineDisplay = new Display();
    private Visualization lineViz;
    private JPopupMenu popup;
    private JMenuItem z_print = new JMenuItem("Export image");

    private Rectangle2D datum=new Rectangle2D.Float();
    private Rectangle2D xLabel=new Rectangle2D.Float();
    private Rectangle2D yLabel=new Rectangle2D.Float();


    //-- the table schema for store line information
    protected final Schema edgeschema=new Schema();
    {
        edgeschema.addColumn(ID, int.class);
        edgeschema.addColumn(SOURCE, int.class);
        edgeschema.addColumn(TARGET, int.class);
    }
    //-- table for lines
    private Table edge=edgeschema.instantiate();

    /**
     * Construct a new Timeline View with specified Table, which is a time-frequency
     * table actually.<p/>
     * @param t - the time-frequency table<p/>
     */
    public TimelineView(Table t) throws SQLException, ClassNotFoundException{

        addEdges(t);

        //-1. import data into a data table ------------------------------------
        lineViz=new Visualization();
        VisualTable vt=lineViz.addTable(TIMELINE, t);
        lineViz.addTable(EDGE, edge);

        //-2. set Renderer to render items -------------------------------------
        lineViz.setRendererFactory(new RendererFactory() {

            LineShapeRenderer sr = new LineShapeRenderer(TIMELINE);
            ShapeRenderer lr = new ShapeRenderer(3);
            Renderer arY = new AxisRenderer(Constants.LEFT, Constants.TOP);
            Renderer arX = new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM);

            public Renderer getRenderer(VisualItem item) {

                return item.isInGroup(EDGE) ? sr :
                       item.isInGroup("xlab") ? arX :
                       item.isInGroup("ylab") ? arY : lr;
            }
        });

        //-3. set Actions to encode visulitem. ---------------------------------
        //-set range model for x and y axis.
        RangeQueryBinding freqQ = new RangeQueryBinding(vt, FREQUENCY);
        RangeQueryBinding timeQ = new RangeQueryBinding(vt, TIME, false);
//        ListQueryBinding yearQ = new ListQueryBinding(vt, "YY");
        AndPredicate filter = new AndPredicate(freqQ.getPredicate());
//        filter.add(yearQ.getPredicate());
        filter.add(timeQ.getPredicate());

        //-3.1 set X and Y axis labels-
//        AxisLayout xaxis1=new AxisLayout(TIMELINE, TIME, Constants.X_AXIS, VisiblePredicate.TRUE);
        AxisLayout xaxis1=new AxisLayout(TIMELINE, TIME, Constants.X_AXIS);
        Object[] o = t.getMetadata(TIME).getOrdinalArray();
        timeQ.getObjectModel().setValueRange(o);
        xaxis1.setRangeModel(timeQ.getModel());

        AxisLayout yaxis1=new AxisLayout(TIMELINE, FREQUENCY, Constants.Y_AXIS);
        yaxis1.setRangeModel(freqQ.getModel());
        ColumnMetadata cmdFreq = t.getMetadata(FREQUENCY);
        int Max = t.getInt(cmdFreq.getMaximumRow(), FREQUENCY);
        freqQ.getNumberModel().setValueRange(0, Max, 0, Max+1);

        xaxis1.setLayoutBounds(datum);
        yaxis1.setLayoutBounds(datum);

        AxisLabelLayout xlabels=new AxisLabelLayout("xlab", xaxis1, xLabel);
        AxisLabelLayout ylabels=new AxisLabelLayout("ylab", yaxis1, yLabel);

        lineViz.putAction("xlabels", xaxis1);
        lineViz.putAction("ylables", yaxis1);

        //- set Y-axis slider for number
        JRangeSlider yslider = freqQ.createVerticalRangeSlider();
        yslider.setThumbColor(null);
        yslider.setMinExtent(100);
        yslider.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                timelineDisplay.setHighQuality(false);
            }
            public void mouseReleased(MouseEvent e) {
                timelineDisplay.setHighQuality(true);
                timelineDisplay.repaint();
            }
        });

        JRangeSlider xslider = timeQ.createHorizontalRangeSlider();
        xslider.setThumbColor(null);
        xslider.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
                timelineDisplay.setHighQuality(false);
            }
            public void mouseReleased(MouseEvent e) {
                timelineDisplay.setHighQuality(true);
                timelineDisplay.repaint();
            }
        });

        //-3.2 set action to determine the color and shape of visualitems-
        int[] col=new int[] {ColorLib.rgb(0,0,0)};
        DataColorAction color=new DataColorAction(TIMELINE, FREQUENCY, Constants.ORDINAL,
                VisualItem.FILLCOLOR, col);
        ColorAction c2=new ColorAction(EDGE, VisualItem.STROKECOLOR, ColorLib.rgb(50, 50, 255));
        int[] Shape=new int[]{Constants.SHAPE_ELLIPSE};
        DataShapeAction shape=new DataShapeAction(TIMELINE, ID, Shape);

        //-3.3 add actionlists
        ActionList draw=new ActionList();
        draw.add(xlabels);
        draw.add(ylabels);
        draw.add(xaxis1);
        draw.add(yaxis1);
        draw.add(color);
        draw.add(c2);
        draw.add(shape);
        draw.add(new RepaintAction());
        lineViz.putAction("Draw", draw);

        ActionList update = new ActionList();
        update.add(new VisibilityFilter(TIMELINE, filter));
        update.add(xaxis1);
        update.add(yaxis1);
        update.add(xlabels);
        update.add(ylabels);
        update.add(new RepaintAction());
        lineViz.putAction("update", update);

        UpdateListener uplstr = new UpdateListener(){
            public void update(Object src) {
                lineViz.run("Draw");
            }
        };

        filter.addExpressionListener(uplstr);

        //-4. add the viz to this display --------------------------------------
        this.setVisualization(lineViz);

        String[] ttcs = {TIME, FREQUENCY};
        Control ttc = new ToolTipControl(ttcs);
//        Control drag=new WheelZoomControl();
//        Control autocenter=new ZoomToFitControl();
        Control hoverc = new ControlAdapter(){

            public void itemEntered(VisualItem vi, MouseEvent e){
                if (vi.isInGroup(TIMELINE)){
                    vi.setSize(vi.getStartSize()*2);
                    vi.setFillColor(ColorLibPlus.ORANGE);
                    vi.getVisualization().repaint();
                }
            }

            public void itemExited(VisualItem vi, MouseEvent e){
                if (vi.isInGroup(TIMELINE)){
                    vi.setSize(vi.getEndSize());
                    vi.setFillColor(vi.getEndFillColor());
                    vi.getVisualization().repaint();
                    //displayLayout();
                }
            }
        };
        this.addControlListener(ttc);
//        this.addControlListener(drag);
//        this.addControlListener(autocenter);
        this.addControlListener(hoverc);
        this.addComponentListener(uplstr);

        //TODO test propert Display interactions.
        this.setHighQuality(true);
        this.setSize(600,350);
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e){
                displayLayout();
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

        displayLayout();

        this.setLayout(new BorderLayout());
        this.add(yslider, BorderLayout.EAST);
        this.add(xslider, BorderLayout.SOUTH);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

    }


    @Override
    /**
     * Override Display's createToolTip method to change tool tip color.<p/>
     */
    public JToolTip createToolTip(){
       JToolTip tip = super.createToolTip();
       tip.setBackground(Color.YELLOW);

       return tip;
    }

    /**
     * This method layout items in both X and Y axis, and items in the diagram.<p/>
     * Also set the display auto fit to its container's dimensions.<p/>
     *
     */
    protected void displayLayout() {

        Insets i = this.getInsets();
        int w = this.getWidth();
        int h = this.getHeight();
        int iw = i.left+i.right;
        int ih = i.top+i.bottom;
        int atop = 10;
        int aleft = 10;
        int abot = 30;
        int aright =20;

        datum.setRect(i.left+atop, i.top+aleft, w-iw-aleft-aright, h-ih-atop-abot);
        xLabel.setRect(i.left+atop, h-2*atop-abot, w-iw-aleft-aright, 2*atop);
        yLabel.setRect(i.left+atop, i.top+aleft, w-iw-aleft-aright, h-ih-atop-abot);

        lineViz.run("Draw");
    }

    /**
     * This method add a row into the EDGE data table<p/>
     */
    protected void addEdgeRows(int id, int source, int target){
        int r= edge.addRow();
        edge.set(r, ID, id);
        edge.set(r, SOURCE, source);
        edge.set(r, TARGET, target);
    }

    /**
     * Method to add edge of the input table to the edgetable<p/>
     * @param t, Table need edges<p/>
     * NOTE: will change the edgeTable according to the input Table.<p/>
     */
    private void addEdges(Table t){

        int n=t.getRowCount();
        int start=0;

        for (int i=0;i<n;i++){
            t.setInt(i, ID, i+1);
            if (i>start){
                addEdgeRows(i, start, i);
                start++;
            }
        }

    }

    /**
     * inner class to implement a comparator, comparing the time string,
     * o1 and o2 are the time String<p/>
     * 1. tX[2] = year;<p/>
     * 2. tX[1] = month;<p/>
     * 3. tX[0] = day;<p/>
     */
    @SuppressWarnings({ "unused", "rawtypes" })
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

