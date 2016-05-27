
package prefusePlus.Renderer;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import prefuse.render.ShapeRenderer;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;
import prefusePlus.util.GraphicsLibPlus;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * This class inharit prefuse.renderer.AbstractShapeRender to create a shape composited
 * by a series of rectangles with different colors and widths, which are determined
 * by a map containing a set of widths and colors.<p />
 *
 * Dec. 22nd, 2010: Solve setBound problem. Now hover control can work for the whole
 *                  line of a query.<p />
 * @author James
 */
public class CompositedShapeRenderer extends ShapeRenderer{

    /* unit of the base size of a rectangle*/
    private double m_baseSize=10;   
//    private double m_arcWidth=5;
//    private double m_arcHeight=5;
    /**
     * The color column that store assigned color to each rectangle in
     * the CompositedColorAction.<p />
     */
    private String m_colors = SDSSLOGVIEWCONSTANTS.COLORS;
    /**
     * Nov.22, change to Rectangle 2D, and increase performance A LOT!<p />
     */
    private Rectangle2D m_shape=new Rectangle2D.Double();
    /* a column to store the length of tokens, which determine the length of rectangles*/
    private String m_type = SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH;
    /* A rectangle to set the overall bounds of these rectangles*/
    @SuppressWarnings("unused")
	private Rectangle2D m_bound = new Rectangle2D.Double();

    /**
     * Creates a new CompositedShapeRenderer with default base size of 10 pixels.<p />
     */
    public CompositedShapeRenderer(){
        //Do nothing as default
    }

    /**
     * Creates a new CompositedShapeRender with a given base size.<p />
     * @param size - the base size.<p />
     */
    public CompositedShapeRenderer(int size){
        m_baseSize=size;
    }

    /**
     * Creates a new CompositedShapeRenderer with a given column and each cell is
     * a color list.<p />
     * @param colorlistfield - column of colors<p />
     */
    public CompositedShapeRenderer(String colorlistfield){
        m_colors=colorlistfield;
    }

    /**
     * Sets the base size, in pixels, for shapes drawn by this renderer. The
     * base size is the width and height value used when a VisualItem's size
     * value is 1. The base size is scaled by the item's size value to arrive
     * at the final scale used for rendering.<p />
     * @param size - the base size in pixels<p />
     */
    public void setSize(int size){
        m_baseSize=size;
    }

    /**
     * Returns the base size, in pixels, for shapes drawn by this renderer.<p />
     * @return the base size in pixels.<p />
     */
    public double getSize(){
        return m_baseSize;
    }

    /**
     * revises getRawShape method defined in AbstractShapeRender.<p />
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)<p />
     * @return an array of shapes for render method to use.<p />
     */
    protected Shape[] getRawShapes(VisualItem vi) {

        // a temp shape array to store all rectganles
        Shape[] tempShapes=null;

        // create shapes
        double x=vi.getX();
        double y=vi.getY();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;

        int[] tokens=(int[]) vi.get(m_type);
        tempShapes=new Shape[tokens.length];
        int size;
        for (int i=0;i<tokens.length;i++){
            size = tokens[i];
            m_shape=new Rectangle2D.Double();
            m_shape.setRect(x, y, size*m_baseSize,
                    m_baseSize);
            tempShapes[i]=m_shape;

            x=x+(Integer)size*m_baseSize;
        }

        return tempShapes;
    }

    /**
     * Get the color list from a visual item.<p />
     * @param vi - visual item<p />
     * @return an int array with colors<p />
     */
    protected int[] getRawColors(VisualItem vi){

        int[] colors=(int[]) vi.get(m_colors);
        
        return colors;
    }   //end of getRawColors method

    /**
     * Get the overall shape of all rectangles for setting bounds.<p />
     * @param vi - visual item<p />
     * @return a shape
     */
    protected Shape getOverallShape(VisualItem vi){

        Rectangle2D tempShape = new Rectangle2D.Double();

        // create shapes
        double x=vi.getX();
        double y=vi.getY();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;

        int[] tokens=(int[]) vi.get(m_type);
        int size = tokens.length;
        double overAllSize = 0;
        for (int i=0;i<size;i++){
            overAllSize += (Integer)tokens[i]*m_baseSize;
        }
        tempShape.setRect(x, y, overAllSize, m_baseSize);
        return tempShape;
    }

    @Override
    /**
     * Override superclass method to get the shape of this visual item.<p />
     * return a transformed shape<p />
     */
    public Shape getShape(VisualItem vi){
        AffineTransform at = getTransform(vi);
        Shape rawShape = getOverallShape(vi);
        return (at==null || rawShape==null ? rawShape
                 : at.createTransformedShape(rawShape));
    }


    @Override
    /**
     * Override superclass' method to set the bounds of a visual item for 
     * interactions.<p />
     */
    public void setBounds(VisualItem vi){
        if ( !m_manageBounds ) return;
        Shape shape = getShape(vi);
        if ( shape == null ) {
            vi.setBounds(vi.getX(), vi.getY(), 0, 0);
        } else {
            GraphicsLib.setBounds(vi, shape, getStroke(vi));
        }

    }

    @Override
    /**
     * Override the render method to use GraphicsLibPlus method to paint this visual
     * item.<p />
     */
    public void render(Graphics2D g, VisualItem vi){

        if (vi.isVisible()){
            Shape[] shapes=getRawShapes(vi);
            int[] colors=getRawColors(vi);
            if (shapes!=null){
                GraphicsLibPlus.compositedPaint(g, vi, shapes, colors, vi.getStroke(), getRenderType(vi));
            }
        }
    } // end of render

}
