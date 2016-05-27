package sdsslogviewer.Viz;


import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.ToolTipManager;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.SquarifiedTreeMapLayout;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.ColorMap;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTree;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

import prefusePlus.data.TableTree;

import sdsslogviewer.data.SDSSLogTable;

/**
 * This class creates a TreeMap Statistic View with a SDSS Log Tabel.<p/>
 * April 19th, 2011: Fixed the bug in statistics view for cross condition combination.<p/>
 * 
 * @author jzhang
 */
@SuppressWarnings("serial")
public class TreeMapStat extends Display{

    /* A set of string variables for identifications. */
    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";

    /* z_label string to show which column will be visualized in the new built tree. */
    private String z_label = TableTree.CONTENT;

    /* A tree as the back data structure */
    private Tree z_tree;

    // create data description of z_label, setting colors, fonts ahead of time
    private static final Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();
    static {
        LABEL_SCHEMA.setDefault(VisualItem.INTERACTIVE, true);
        LABEL_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(200));
        LABEL_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma",10));
    }

    /* Predicate to set kids visible or not */
    private AndPredicate inner_predicate = new AndPredicate(),
                         outer_predicate = null;

    /* The back table use to retrieve detailed contents of kid nodes*/
    private Table backTable;

    /**
     * Construct an empty TreeMapStat, showing only a rectangular region, resizable
     * with its container.<p/>
     */
    public TreeMapStat(){
        super(new Visualization());
        setSize(400,400);
        this.setBackground(ColorLib.getColor(50, 50, 50));
    }

    /**
     * Construct a TreeMapStat from a back table. The field to show a TreeMap is 
     * the default SDSSLogTable.IPADDRESS.<p/>
     * 
     * @param t - table for building up a TableTree.<p/>
     */
    public TreeMapStat(Table t){
        this(t, SDSSLogTable.IPADDRESS);
    }

    /**
     * Construct a TreeMapStat from a back table, which is a SDSS SQL table with
     * all parsed tokens.<p/>
     * 
     * @param t - back table for the TreeMap
     * @param label - column for build the TableTree
     */
    @SuppressWarnings("unused")
	public TreeMapStat(Table t, String label){
        super(new Visualization());

        //--. Create a TableTree with secified
        TableTree tabletree = new TableTree(t, label);
        z_tree = tabletree.getTableTree();

        //--. Refer to the data table to back table, so toltip control can access it.
        //NOTE, this will be risky in terms of memory manager.
        backTable = t;

        // add the tree to the visualization
        VisualTree vt = m_vis.addTree(tree, z_tree);
        m_vis.setVisible(treeEdges, null, false);

        // ensure that only leaf nodes are interactive
        Predicate noLeaf = (Predicate)ExpressionParser.parse("childcount()> 0");
//        Predicate noLeaf = (Predicate)ExpressionParser.parse("treedepth()!=1");
        m_vis.setInteractive(treeNodes, noLeaf, false);

        // add labels to the visualization
        // first create a filter to show labels only at top-level nodes
        Predicate labelP = (Predicate)ExpressionParser.parse("treedepth()=1 AND "
                + "childcount() > 100");
        // now create the labels as decorators of the nodes
        m_vis.addDecorators(z_label, treeNodes, labelP, LABEL_SCHEMA);

        // set up the renderers - one for nodes and one for labels
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.add(new InGroupPredicate(treeNodes), new NodeRenderer());
        rf.add(new InGroupPredicate(z_label), new LabelRenderer(z_label));
        m_vis.setRendererFactory(rf);

        // border colors
        final ColorAction borderColor = new BorderColorAction(treeNodes);
        final ColorAction fillColor = new FillColorAction(treeNodes);

        // color settings
        ActionList colors = new ActionList();
        colors.add(fillColor);
        colors.add(borderColor);
        m_vis.putAction("colors", colors);

        // animate paint change
        ActionList animatePaint = new ActionList();
        animatePaint.add(new ColorAnimator(treeNodes));
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);

        // create the single filtering and layout action list
        ActionList layout = new ActionList();
        layout.add(new SquarifiedTreeMapLayout(tree));
        layout.add(new LabelLayout(z_label));
        layout.add(colors);
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // initialize our display
        this.setSize(600,400);
        this.setBackground(ColorLib.getColor(50, 50, 50));
        // perform layout
        setDisplay();

        //set layout auto fit current component size
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized (ComponentEvent e){
                setDisplay();
            }
        });

        setItemSorter(new TreeDepthItemSorter());
        addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                if (m_vis.isInGroup(item, tree)){
                    item.setStrokeColor(borderColor.getColor(item));
                    item.getVisualization().repaint();
                    Display d = (Display) e.getSource();
//                    String tipContents = getTableTreeTipContents(item);
//                    d.setToolTipText(tipContents);
                    d.setToolTipText(item.getString(z_label));
                }
            }
            public void itemExited(VisualItem item, MouseEvent e) {
                if (m_vis.isInGroup(item, tree)){
                    item.setStrokeColor(item.getEndStrokeColor());
                    item.getVisualization().repaint();
                    Display d = (Display) e.getSource();
                    d.setToolTipText(null);
                }
            }
        });
/*
        SearchQueryBinding searchQ = new SearchQueryBinding(vt.getNodeTable(), z_label);
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchQ.getSearchSet());
        searchQ.getPredicate().addExpressionListener(new UpdateListener() {
            public void update(Object src) {
                m_vis.cancel("animatePaint");
                m_vis.run("colors");
                m_vis.run("animatePaint");
            }
        });
*/
        //--Keep the tool tip showing till move out
        int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();

        dismissDelay = Integer.MAX_VALUE;
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
    }

    /* A set of private method for adjust image.*/
    /**
     * get the tooltip contents from specified back table.
     * @param vi
     * @return
     */
    @SuppressWarnings("unused")
	private String getTableTreeTipContents(VisualItem vi){
        if (backTable != null){
            int parentID = vi.getInt(TableTree.PARENT);
            String[] tipcolumns = {SDSSLogTable.IPADDRESS,
                                   SDSSLogTable.STATEMENT,
                                   SDSSLogTable.ACCESSPORTAL,
                                   SDSSLogTable.DATABASE,
                                   SDSSLogTable.REQUESTOR,
                                   SDSSLogTable.SERVER};
            String tipContents = "<HTML>";
            for (int i=0;i<tipcolumns.length;i++){
                tipContents += "<font color=\"#FFA500\"><b>" + tipcolumns[i] + "</b></font>:";
                tipContents += buildTipText(backTable.getString(parentID, tipcolumns[i]));
            }
            tipContents += "</HTML>";
            return tipContents;
        } else
            return "<HTML>Content data corrupted.</HTML>";
    }

    /**
     * Format tooltip strings
     * @param tokens
     * @return
     */
    private String buildTipText(String tokens){
        StringBuilder sbder = new StringBuilder();

        String[] s = tokens.replaceAll("<", "&lt;").replaceAll(">", "&gt;").split("\\s|\\[br\\]");
        int length = 0;

        for (int i=0;i<s.length;i++){

            length += s[i].length();
            if (length < 50){
                sbder.append(s[i]);
                sbder.append(' ');
            } else {
                sbder.append("<br />");
                sbder.append(s[i]);
                sbder.append(' ');
                length = 0;
            } //END if
        } //end for
        sbder.append("<br />");

        return sbder.toString();
    }


    /**
     * Update the contents in the TreeMap. Set kids with chosen features visible
     * only, but also keep the parent node visible.<p/>
     * @param p - predicate passed from outsiders.<p/>
     */
    public void updateDynamicQuery(AndPredicate p){
        //--. remove existing filter;
        m_vis.removeAction("filter");
        //--. get new filter predicate
        AndPredicate temp_pred = new AndPredicate();
        if (p != null){
            outer_predicate = new AndPredicate();
            outer_predicate.add(p);
            inner_predicate = outer_predicate;      //give the dynamic query predicate to this one.
            temp_pred.add(outer_predicate);
        } else{
            temp_pred.add(inner_predicate);
        }
        //--. filter rows and update
        ActionList filter = new ActionList();
        filter.add(new VisibilityFilterForTableTree(treeNodes, temp_pred, backTable));
        m_vis.putAction("filter", filter);
        m_vis.run("filter");
        setDisplay();
    }

    /**
     * method to read in the current display dimension and set the bounds of according
     * to this new dimension. Then run layout action again to fit the tree map into
     * new dimension.<p />
     * This method solve the problem of inconsistancy between container panel dimension
     * and the display dimension.<p />
     */
    private void setDisplay(){
        Insets i=this.getInsets();
        int w=this.getWidth();
        int h=this.getHeight();
        int space=0;
        int topspace = 15;

        this.setBounds(i.left+space, i.top+topspace,
                w-(i.left+i.right+space), h-(i.top+i.bottom+space));

        m_vis.run("layout");
//        m_vis.repaint();
    }

    /**
     * This method clean up this view and release memory.<p/>
     */
    public void cleanup(){
        //--. clean up this display
        this.setVisualization(null);
        this.reset();
        backTable = null;
    }

    /**
     * Return the inner predicate used by this view for outsiders.<p/>
     * @return a predicate.<p/>
     */
    public AndPredicate getInnerPredicate(){
        return inner_predicate;
    }

    // ------------------------------------------------------------------------

    /**
     * Set the stroke color for drawing treemap node outlines. A graded
     * grayscale ramp is used, with higer nodes in the tree drawn in
     * lighter shades of gray.<p/>
     */
    public class BorderColorAction extends ColorAction {

        public BorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }

        public int getColor(VisualItem item) {
            NodeItem nitem = (NodeItem)item;
            if ( nitem.isHover() )
                return ColorLib.rgb(255,255,0);

            int depth = nitem.getDepth();
            if ( depth < 2 ) {
                return ColorLib.gray(200);
            } else if ( depth < 4 ) {
                return ColorLib.gray(75);
            } else {
                return ColorLib.gray(50);
            }
        }
    }

    /**
     * Set fill colors for treemap nodes. Search items are colored
     * in pink, while normal nodes are shaded according to their
     * depth in the tree.<p/>
     */
    public class FillColorAction extends ColorAction {
        private ColorMap cmap = new ColorMap(
            ColorLib.getInterpolatedPalette(10,
                ColorLib.rgb(85,85,85), ColorLib.rgb(0,0,0)), 0, 9);

        public FillColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }

        public int getColor(VisualItem item) {
            if ( item instanceof NodeItem ) {
                NodeItem nitem = (NodeItem)item;
                if ( nitem.getChildCount() > 0 ) {
                    return 0; // no fill for parent nodes
                } else {
                    if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
                        return ColorLib.rgb(191,99,130);
                    else
                        return cmap.getColor(nitem.getDepth());
                }
            } else {
                return cmap.getColor(0);
            }
        }

    } // end of inner class TreeMapColorAction

    /**
     * Set z_label positions. Labels are assumed to be DecoratorItem instances,
     * decorating their respective nodes. The layout simply gets the bounds
     * of the decorated node and assigns the z_label coordinates to the center
     * of those bounds.<p/>
     */
    public class LabelLayout extends Layout {
        public LabelLayout(String group) {
            super(group);
        }
        @SuppressWarnings("rawtypes")
		public void run(double frac) {
            Iterator iter = m_vis.items(m_group);
            while ( iter.hasNext() ) {
                DecoratorItem item = (DecoratorItem)iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getCenterX());
                setY(item, null, bounds.getCenterY());
            }
        }
    } // end of inner class LabelLayout

    /**
     * A renderer for treemap nodes. Draws simple rectangles, but defers
     * the bounds management to the layout.<p/>
     */
    public static class NodeRenderer extends AbstractShapeRenderer {
        private Rectangle2D m_bounds = new Rectangle2D.Double();

        public NodeRenderer() {
            m_manageBounds = false;
        }

        protected Shape getRawShape(VisualItem item) {
            m_bounds.setRect(item.getBounds());
            return m_bounds;
        }
    } // end of inner class NodeRenderer

    /**
     * Class to set the visibility of items in the TableTree.<p/>
     */
    public class VisibilityFilterForTableTree extends GroupAction {

        @SuppressWarnings("unused")
		private Predicate m_filter = (Predicate)ExpressionParser.parse("childcount()=0");
        private Predicate z_predicate;
        private Table z_backTable;

        public VisibilityFilterForTableTree(String group, Predicate p, Table backTable){
            super(group);
            z_backTable = backTable;
            z_predicate = p;
            m_filter = new OrPredicate(p, VisiblePredicate.TRUE);
        }

        @SuppressWarnings("rawtypes")
		@Override
        public void run(double d) {
            Iterator it = m_vis.items(m_group, "childcount()=0");
            while (it.hasNext()){
                VisualItem vi = (VisualItem) it.next();
                int parentID = vi.getInt(TableTree.PARENT);
                Tuple parentTuple = z_backTable.getTuple(parentID);
                boolean visible = z_predicate.getBoolean(parentTuple);
                PrefuseLib.updateVisible(vi, visible);
            }
        }
    }


}
