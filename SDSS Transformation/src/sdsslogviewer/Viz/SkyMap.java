package sdsslogviewer.Viz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.query.ListQueryBinding;
import prefuse.data.query.NumberRangeModel;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.UpdateListener;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.sort.ItemSorter;

import prefusePlus.Renderer.VarientShapeRenderer;
import prefusePlus.controls.SQLToolTipControl;
import prefusePlus.util.ColorLibPlus;
import prefusePlus.util.ui.JValueSliderPlus;
import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

import sdsslogviewer.data.SDSSLogTable;


/**
 * This is a Display to show the SkyMap View in the SDSS Log Viewer.<p/>
 * 
 * Dec. 6, 2010: revise setDisplay method to adapt to the x/y=2 ratio.<p/>
 *
 * Nov. 22nd, 2010: Finish inner and outer dynamic query functions.<p/>
 *
 * March 16th, 2011: set Panning available.<p/>
 * @author James Zhang at Drexel iSchool<p/>
 */
@SuppressWarnings("serial")
public class SkyMap extends Display {

    /**Local variables*/
    private double z_UNIT = 1.0;
    private int z_alpha = 20;

    private String TABLENAME = "skymap";
    private String m_shape = SDSSLOGVIEWCONSTANTS.AREATYPES;
    private String m_RA = SDSSLOGVIEWCONSTANTS.AREA_RA;
    private String m_DEC = SDSSLOGVIEWCONSTANTS.AREA_DEC;

    private String m_condition = SDSSLOGVIEWCONSTANTS.AREATYPES + " =1 or " +
                                 SDSSLOGVIEWCONSTANTS.AREATYPES + " =2";

    private Predicate z_filter = (Predicate) ExpressionParser.parse(m_condition);
    
    private AndPredicate inner_predicate;
    private AndPredicate outer_predicate = null;

    private Rectangle2D m_dataBound = new Rectangle2D.Double();

    private SizeAction size;

    private JPopupMenu popup;
    private JMenuItem z_print = new JMenuItem("Export image");
    private JFastLabel z_info = new JFastLabel();
    private JValueSliderPlus z_transparency;
    private int TOTAL,
                V_TOTAL;

    //--A local table for drawing skymap coordinate --
    private final Schema nullschema= new Schema(); {
        nullschema.addColumn(m_RA, double.class);
        nullschema.addColumn(m_DEC, double.class);
    }
    private Table nulltable = nullschema.instantiate();
    //-------------------------------------------------

    /**
     * Create an empty skymap with coordinate only.<p/>
     * Used for initial GUI display.<p/>
     */
    @SuppressWarnings("unused")
	public SkyMap(){

        super (new Visualization());

        addRow(0.0, 0.0);

        VisualTable vt = m_vis.addTable(TABLENAME, nulltable);

        m_vis.setRendererFactory(new RendererFactory(){

            ShapeRenderer lr = new ShapeRenderer(1);
            Renderer arX = new AxisRenderer(Constants.CENTER, Constants.CENTER);
            Renderer arY = new AxisRenderer(Constants.CENTER, Constants.CENTER);

            public Renderer getRenderer(VisualItem item) {
                return item.isInGroup("xlab") ? arX :
                       item.isInGroup("ylab") ? arY :lr ;
            }
        });

        ColorAction color = new ColorAction(TABLENAME, VisualItem.FILLCOLOR, ColorLibPlus.BLACK);
        ShapeAction shape = new ShapeAction(TABLENAME);

        AxisLayout x_axis = new AxisLayout(TABLENAME, m_RA, Constants.X_AXIS);
        NumberRangeModel x_nrm = new NumberRangeModel(0, 360, 0, 360);
        x_axis.setRangeModel(x_nrm);
        AxisLayout y_axis = new AxisLayout(TABLENAME, m_DEC, Constants.Y_AXIS);
        NumberRangeModel y_nrm = new NumberRangeModel(-90, 90, -90, 90);
        y_axis.setRangeModel(y_nrm);

        x_axis.setLayoutBounds(m_dataBound);
        y_axis.setLayoutBounds(m_dataBound);

        AxisLabelLayout X = new AxisLabelLayout("xlab", x_axis, m_dataBound);
        X.setAscending(false);
        X.setSpacing(40);
        AxisLabelLayout Y = new AxisLabelLayout("ylab", y_axis, m_dataBound);
        Y.setSpacing(20);

        ActionList paint = new ActionList();
        paint.add(color);
        paint.add(shape);
        paint.add(X);
        paint.add(Y);
        paint.add(x_axis);
        paint.add(y_axis);
        paint.add(new RepaintAction());
        m_vis.putAction("paint", paint);

        this.setVisualization(m_vis);
        this.setBackground(ColorLib.getColor(0,0,0));

        m_vis.run("paint");

        this.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized (ComponentEvent e){
                setEmptyDisplay();
            }
        });

    }

    /**
     * Create a SkyMap Display with a specified SDSS Log Table.<p/>
     * @param t - the SDSS Log Table
     */
    public SkyMap(Table t) {
        super(new Visualization());

        //--1. get table stat
        TOTAL = t.getRowCount();

        //--2. Initialize vis
        VisualTable vt = m_vis.addTable(TABLENAME, t, z_filter);
        V_TOTAL = vt.getRowCount();

        //--3. SetRenderers
        m_vis.setRendererFactory(new RendererFactory() {

            VarientShapeRenderer lr = new VarientShapeRenderer();

            Renderer arY = new AxisRenderer(Constants.CENTER, Constants.CENTER);
            Renderer arX = new AxisRenderer(Constants.CENTER, Constants.CENTER);

            public Renderer getRenderer(VisualItem item) {

                return item.isInGroup("xlab") ? arX :
                       item.isInGroup("ylab") ? arY :lr ;
            }
        });

        //--4. Assign size, color, and shape to queries
        int[] colors = {ColorLib.rgba(0, 255, 0, z_alpha), ColorLib.rgba(255, 255, 0, z_alpha)};
        DataColorAction color = new DataColorAction(TABLENAME, m_shape, Constants.NOMINAL,
                VisualItem.FILLCOLOR, colors);
        int[] shapes = {Constants.SHAPE_ELLIPSE, Constants.SHAPE_RECTANGLE};
        DataShapeAction shape = new DataShapeAction(TABLENAME, m_shape, shapes);

        //-- . Assign X and Y axis and lable
        AxisLayout x_axis = new AxisLayout(TABLENAME, m_RA, Constants.X_AXIS);
        NumberRangeModel x_nrm = new NumberRangeModel(0, 360, 0, 360);
        x_axis.setRangeModel(x_nrm);

        AxisLayout y_axis = new AxisLayout(TABLENAME, m_DEC, Constants.Y_AXIS);
        NumberRangeModel y_nrm = new NumberRangeModel(-90, 90, -90, 90);
        y_axis.setRangeModel(y_nrm);

        x_axis.setLayoutBounds(m_dataBound);
        y_axis.setLayoutBounds(m_dataBound);

        AxisLabelLayout X = new AxisLabelLayout("xlab", x_axis, m_dataBound);
        X.setSpacing(40);
        AxisLabelLayout Y = new AxisLabelLayout("ylab", y_axis, m_dataBound);
        Y.setSpacing(20);

        //--. assign dynamic controls
        ListQueryBinding shapeQ =  new ListQueryBinding(vt, SDSSLOGVIEWCONSTANTS.AREATYPES);
        inner_predicate = new AndPredicate(shapeQ.getPredicate());

        //-- . add to actionlist
        m_vis.putAction("color", color);

        ActionList paint = new ActionList();
        paint.add(new VisibilityFilter(TABLENAME, inner_predicate));
//        paint.add(color);
        paint.add(shape);
        paint.add(X);
        paint.add(Y);
        paint.add(x_axis);
        paint.add(y_axis);
        paint.add(new RepaintAction());
        m_vis.putAction("paint", paint);

        ActionList update = new ActionList();
        update.add(shape);
        update.add(X);
        update.add(Y);
        update.add(x_axis);
        update.add(y_axis);
        update.add(new RepaintAction());
        m_vis.putAction("update", update);

        //when inner condition changed, update the contents with both inner and
        //outer conditions.
        UpdateListener uplstr = new UpdateListener(){
            public void update(Object src) {
                updateDynamicQuery(outer_predicate);
            }
        };
        inner_predicate.addExpressionListener(uplstr);

        //-- . Set display features
        this.setBackground(ColorLib.getColor(0, 0, 0));
        this.setVisualization(m_vis);

//        m_vis.run("paint");
        setDisplay();

        //-- . add Controls to this display
        String[] tipcolumns = {SDSSLogTable.TIME,
                               SDSSLogTable.IPADDRESS,
                               SDSSLogTable.STATEMENT,
                               SDSSLogTable.ACCESSPORTAL,
                               SDSSLogTable.DATABASE,
                               SDSSLogTable.REQUESTOR,
                               SDSSLogTable.SERVER};
        SQLToolTipControl ttc = new SQLToolTipControl(tipcolumns);
        this.addControlListener(ttc);

        this.addControlListener(new ZoomControl());
        this.addControlListener(new WheelZoomControl());
        this.addControlListener(new PanControl());

        this.setItemSorter(new ItemSorter(){

            @Override
            public int score(VisualItem vi){
                int score = super.score(vi);
                if (vi.isInGroup(TABLENAME) ){
                    score += 100000;
                    return score;
                }
                return score;
            }
        });

        Control hover = new ControlAdapter(){
            @Override
            public void itemEntered(VisualItem vi, MouseEvent e){
                if (vi.isInGroup(TABLENAME)){
                    vi.setStrokeColor(ColorLibPlus.RED);
                    vi.getVisualization().repaint();
                }
            }

            @Override
            public void itemExited(VisualItem vi, MouseEvent e){
                if (vi.isInGroup(TABLENAME)){
                    vi.setStrokeColor(vi.getEndStrokeColor());
                    vi.getVisualization().repaint();
                }
            }
        };
        this.addControlListener(hover);
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized (ComponentEvent e){
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

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        //-- . add other components
        z_info.setBackground(Color.BLACK);
        z_info.setForeground(Color.WHITE);
        z_info.setPreferredSize(new Dimension(100, 25));
        z_info.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 14));
        z_info.setHorizontalAlignment(SwingConstants.RIGHT);
        z_info.setText("Total " + TOTAL + " queries; " + V_TOTAL + " search areas, showing here");

        z_transparency = new JValueSliderPlus("Transparency", 0, 255, 1, z_alpha, JValueSliderPlus.HORIZONTAL);
        z_transparency.setBackground(Color.WHITE);
        z_transparency.setForeground(Color.BLACK);
        z_transparency.setPreferredSize(new Dimension(30, 15));
        z_transparency.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                m_vis.removeAction("color");
                z_alpha = z_transparency.getValue().intValue();
                int[] colors = {ColorLib.rgba(0, 255, 0, z_alpha), ColorLib.rgba(255, 255, 0, z_alpha)};
                DataColorAction color = new DataColorAction(TABLENAME, m_shape, Constants.NOMINAL,
                VisualItem.FILLCOLOR, colors);
                m_vis.putAction("color", color);
                m_vis.run("color");
                m_vis.repaint();
            }
        });
        Box radiobox = new Box(BoxLayout.X_AXIS);
        radiobox.add(Box.createHorizontalStrut(0));
        radiobox.add(shapeQ.createRadioGroup());
        radiobox.add(Box.createHorizontalStrut(1));
        radiobox.add(z_transparency);
        radiobox.add(Box.createHorizontalStrut(5));
//        radiobox.add(Box.createHorizontalGlue());
        radiobox.add(z_info);
//        radiobox.add(Box.createHorizontalStrut(5));
        radiobox.setBackground(Color.WHITE);

        this.setLayout(new BorderLayout());
        this.add(radiobox, BorderLayout.NORTH);
    }

    /**
     * Accept additional predicate to dynamically control the visibility of items
     * in this view.<p/>
     * @param p - Predicate passed from outsiders.<p/>
     */
    @SuppressWarnings("rawtypes")
	public void updateDynamicQuery(AndPredicate p){
        //--. remove existing filter
        m_vis.removeAction("filter");
        //--. get predicates
        AndPredicate temp_pred = new AndPredicate();
        if (p != null){
            outer_predicate = new AndPredicate();
            outer_predicate.add(p);
            temp_pred.add(inner_predicate);
            temp_pred.add(outer_predicate);
        } else{
            temp_pred.add(inner_predicate);
        }
        //--. filter rows and update
        ActionList filter = new ActionList();
        filter.add(new VisibilityFilter(TABLENAME, temp_pred));
        m_vis.putAction("filter", filter);
        m_vis.run("filter");
        m_vis.run("color");
        m_vis.run("update");

        //--. check number of visible item and show
        VisualItem item = null;
        Iterator it = m_vis.items(TABLENAME);
        V_TOTAL = 0;
        while (it.hasNext()){
            item = (VisualItem)it.next();
            if (temp_pred.getBoolean(item)){
                ++V_TOTAL;
            }
        }
        z_info.setText("Total: " + TOTAL + " queries; " + V_TOTAL + " showing here");
    }

    @Override
    /**
     * Override createToolTip method to set different colors of tool tips.<p/>
     */
    public JToolTip createToolTip(){
       JToolTip tip = super.createToolTip();
       tip.setBackground(Color.YELLOW);

       return tip;
    }

    private void setEmptyDisplay(){
        Insets i=this.getInsets();
        int w=this.getWidth();
        int h=this.getHeight();
        int leftspace=10;
        int rightspace = 10;
        int topspace=30;
        int bottomspace = 30;

        if ((w/h) >= 2){ //wider, use height as baseline
            m_dataBound.setRect(i.left+leftspace+(w-2*h)/2, i.top+topspace,
                    2*h-i.left-i.right-rightspace, h-i.top-i.bottom-topspace-bottomspace);
        } else {    //else higher, use width as baseline
            m_dataBound.setRect(i.left+leftspace, i.top+topspace+(h/2-w/4),
                    w-i.left-i.right-leftspace-rightspace, w/2-i.top-i.bottom-topspace-bottomspace);
        }

        m_vis.run("paint");
    }

    /**
     * The most important method in this class. Set up display to fill the upper
     * level container.
     */
    private void setDisplay(){

        Insets i=this.getInsets();
        int w=this.getWidth();
        int h=this.getHeight();
        int leftspace=10;
        int rightspace = 10;
        int topspace=30;
        int bottomspace = 30;

        if ((w/h) >= 2){ //wider, use height as baseline
            m_dataBound.setRect(i.left+leftspace+(w-2*h)/2, i.top+topspace,
                    2*h-i.left-i.right-rightspace, h-i.top-i.bottom-topspace-bottomspace);
            z_UNIT = (h-i.top-i.bottom-topspace-bottomspace)/180.0;
        } else {    //else higher, use width as baseline
            m_dataBound.setRect(i.left+leftspace, i.top+topspace+(h/2-w/4),
                    w-i.left-i.right-leftspace-rightspace, w/2-i.top-i.bottom-topspace-bottomspace);
            z_UNIT = (w-i.left-i.right-leftspace-rightspace)/360.0;
        }

        m_vis.removeAction("size");

        size = new SizeAction(TABLENAME, z_UNIT);
        m_vis.putAction("size", size);
        m_vis.run("size");
        updateDynamicQuery(outer_predicate);

    }

    /**
     * private method to add a row into the nulltable table for initial display.
     * @param ra
     * @param dec
     */
    private void addRow(double ra, double dec){
        int r = nulltable.addRow();
        nulltable.setDouble(r, m_RA, ra);
        nulltable.setDouble(r, m_DEC, dec);
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

}
