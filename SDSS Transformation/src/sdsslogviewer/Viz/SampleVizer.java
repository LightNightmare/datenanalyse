package sdsslogviewer.Viz;

import java.awt.geom.Rectangle2D;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

import prefusePlus.Action.CompositedColorAction;
import prefusePlus.Renderer.CompositedColorLabelRenderer;
import prefusePlus.Renderer.CompositedShapeRenderer;
import prefusePlus.util.ColorLibPlus;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;
import sdsslogviewer.SQL.SQLParser;

/**
 * A demo visualization, displaying a user inputed string and show the corresponding
 * SQL content visualization.
 * <p />
 * March 3rd, 2011: Class created to visualize a user input sample query and show
 *                  its corresponding SQL visualization.<p/>
 * Showing three lines:<p />
 * 1st line: The original user input string. <p />
 * 2nd line: The parsed tokens with their codes. <p />
 * 3rd line: The color-coded bars in one line. <p />
 *
 * April 5th, 2011: 1st acceptable version.<p />
 * //TODO: Need to revise AxisLayout so as to arrange text should by should. And
 *         this work will need layout and render to work together.<p />
 *
 * April 10th, 2011: Change viz idea to render one line as one item.<p />
 *                   Paint a non-colored line with the LabelRenderer<p />
 *                   Revise LabelRenderer to paint a color text line.<p />
 * 
 * @author jzhang<p />
 */
@SuppressWarnings("serial")
public class SampleVizer extends Display {

    private final String ID = "id";
    private final String TOKEN = "token";
    private final String TYPE = "token_type";
    private final String CONSTANT = "constant";
    private final String TEXTLINE1 = "textline-nocolored";
    private final String TEXTLINE2 = "textline-colored";
    private final String COLORLINE = "colorbarline";
    private final int SIZE = 11;

    //--inner data table for visualizing a text string with colors
    private final Schema textLineSchema = new Schema();

    {
        textLineSchema.addColumn(ID, int.class);
        textLineSchema.addColumn(TOKEN, String.class);
        textLineSchema.addColumn(TYPE, int.class);
        textLineSchema.addColumn(CONSTANT, int.class, 0);
    }
    Table textLineTable = textLineSchema.instantiate();

    //--inner data tabel for visualizing the color bar line.
    private final Schema colorbarLineSchema = new Schema();
    {
        colorbarLineSchema.addColumn(ID, int.class);
        colorbarLineSchema.addColumn(SDSSLOGVIEWCONSTANTS.TOKENS, String[].class);
        colorbarLineSchema.addColumn(SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, int[].class);
        colorbarLineSchema.addColumn(SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH, int[].class);
        colorbarLineSchema.addColumn(SDSSLOGVIEWCONSTANTS.COLORS, int[].class);
        colorbarLineSchema.addColumn(CONSTANT, int.class, 0);
    }
    Table colorbarLineTable = colorbarLineSchema.instantiate();
    
    //Control variables for this visualization
    private Rectangle2D line1Bound = new Rectangle2D.Double();
    private Rectangle2D line2Bound = new Rectangle2D.Double();
    private Rectangle2D line3Bound = new Rectangle2D.Double();

//------------------------------------------------------------------------------

    /**
     * Construct a sample vizer with input string.<p />
     * If input is empty or null, return an empty display.<p />
     */
    public SampleVizer(String sampleSQL) {
        super(new Visualization());
        this.setBackground(SQLview.BACKGROUNDCOLOR);

        //--Set controls to this display
        this.setBackground(ColorLibPlus.getColor(85, 85, 85));
        this.addControlListener(new WheelZoomControl());
        this.addControlListener(new PanControl());

        if (sampleSQL != null && !sampleSQL.equals("")) {
            buildTables(sampleSQL);
            visualizeTables();
        } //end if. For null and empty, do nothing, just give the empty display.

    }

    /**
     * parse the sql string and tokenize it. Then build the two data tables for viz<p />
     * @param sql
     */
    private void buildTables(String sql) {
        SQLParser parser = new SQLParser();

        parser.tokenize(sql);
        String[] tokens = parser.getTokens();
        int[] types = parser.getTypes();
        int[] lengths = parser.getTokenLength();

        //-- build no color text data table
        textLineTableAddRow(sql, 0);

        //-- build color text data table.
        int i = colorbarLineTable.addRow();
        colorbarLineTable.set(i, ID, i);
        colorbarLineTable.set(i, SDSSLOGVIEWCONSTANTS.TOKENS, tokens);
        colorbarLineTable.set(i, SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, types);
        colorbarLineTable.set(i, SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH, lengths);
    }

    /**
     * Use the two tables to visualize<p />
     */
    @SuppressWarnings("unused")
	private void visualizeTables() {
        VisualTable TLNOCOLOR = m_vis.addTable(TEXTLINE1, textLineTable);
        VisualTable TLCOLOR = m_vis.addTable(TEXTLINE2, colorbarLineTable);
        VisualTable BARLINE = m_vis.addTable(COLORLINE, colorbarLineTable);

        final LabelRenderer textNoColorRenderer = new LabelRenderer(TOKEN);
        final CompositedColorLabelRenderer textColorRenderer = 
                new CompositedColorLabelRenderer(SDSSLOGVIEWCONSTANTS.TOKENS, "");
        final CompositedShapeRenderer text = new CompositedShapeRenderer(SIZE);
        textNoColorRenderer.setHorizontalAlignment(Constants.LEFT);

        DefaultRendererFactory drf = new DefaultRendererFactory(){

            @Override
            public Renderer getRenderer(VisualItem item) {

                return item.isInGroup(TEXTLINE1) ? textNoColorRenderer :
                       item.isInGroup(TEXTLINE2) ? textColorRenderer : text;
            }
        };
        m_vis.setRendererFactory(drf);

        //- the color palette used for color-coding SQL types
        int[] palette = ColorLibPlus.getBasicColorPallete(12);

        //- Assign colors to the three visual tables.
        ColorAction textnocolor = new ColorAction(TEXTLINE1, VisualItem.TEXTCOLOR,
                ColorLibPlus.WHITE);
        CompositedColorAction textcolor = new CompositedColorAction(TEXTLINE2,
                SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, palette);
        CompositedColorAction barcolors = new CompositedColorAction(COLORLINE,
                SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, palette);

        //- Assign size to the three tables, temporarily not assign
        SizeAction textsize1=new SizeAction(TEXTLINE1, 1.6);
        SizeAction textsize2=new SizeAction(TEXTLINE2, 2.29);

        //- Assign layout positions
        setAnchorPoints();

        AxisLayout x1 = new AxisLayout(TEXTLINE1, ID, Constants.X_AXIS);
        AxisLayout y1 = new AxisLayout(TEXTLINE1, CONSTANT, Constants.Y_AXIS);
        x1.setLayoutBounds(line1Bound);
        y1.setLayoutBounds(line1Bound);

        AxisLayout x2 = new AxisLayout(TEXTLINE2, ID, Constants.X_AXIS);
        AxisLayout y2 = new AxisLayout(TEXTLINE2, CONSTANT, Constants.Y_AXIS);
        x2.setLayoutBounds(line2Bound);
        y2.setLayoutBounds(line2Bound);

        AxisLayout x3 = new AxisLayout(COLORLINE, ID, Constants.X_AXIS);
        AxisLayout y3 = new AxisLayout(COLORLINE, CONSTANT, Constants.Y_AXIS);
        x3.setLayoutBounds(line3Bound);
        y3.setLayoutBounds(line3Bound);


        ActionList line1 = new ActionList();
        line1.add(textnocolor);
        line1.add(textsize1);
        line1.add(x1);
        line1.add(y1);
        m_vis.putAction("line1", line1);

        ActionList line2 = new ActionList();
        line2.add(textcolor);
        line2.add(textsize2);
        line2.add(x2);
        line2.add(y2);
        m_vis.putAction("line2", line2);

        ActionList line3 = new ActionList();
        line3.add(barcolors);
        line3.add(x3);
        line3.add(y3);
        m_vis.putAction("line3", line3);

        ActionList repaint = new ActionList();
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);

        m_vis.run("line1");
        m_vis.run("line2");
        m_vis.run("line3");
        m_vis.run("repaint");

    } //end of visualize

    /**
     * clean this visualization and free memory.<p />
     */
    public void cleanUp(){
        m_vis.reset();
        this.setVisualization(null);
    }

    /**
     * Create new viz with an SQL string.<p />
     * @param sql
     */
    public void updateVisualization(String sql) {
        cleanUp();
        buildTables(sql);
        visualizeTables();
    }

    /**
     * Add a new row of data to text line table.
     * @param token
     * @param type
     */
    private void textLineTableAddRow(String token, int type) {
        int i = textLineTable.addRow();
        textLineTable.setInt(i, ID, i);
        textLineTable.setString(i, TOKEN, token);
        textLineTable.setInt(i, TYPE, type);
    }

    @SuppressWarnings("unused")
	private void setAnchorPoints(){
        int top = 10;
        int left = 50;
        int right = 10;
        int space = 20;
        int height = 20;
        int w = this.getWidth();
        int h = this.getHeight();

        line1Bound.setRect(left, top, w-right, height*2 + space);
        line2Bound.setRect(left, top + height + space, w - right, height*2 + space);
        line3Bound.setRect(left, top + height + space*2, w-right, height*2 + space);

    }

}

    /**
     * A test class to assign color to different rows in one table.<p/>
     * NOTE: Not used in the final version.<p/>
     */
/*    public class FixColorNominalDataColorAction extends ColorAction{

        private String z_dataField;
        private int[] z_palette;

        public FixColorNominalDataColorAction(String group, String dataField, 
               String colorField, int[] palette){
            super(group, colorField);
            z_dataField = dataField;
            z_palette = palette;
        }

        @Override
        public int getColor(VisualItem vi){
            return z_palette[vi.getInt(z_dataField)];
        }
    }
*/


