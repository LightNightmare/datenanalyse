
package prefusePlus.Renderer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;
import sdsslogviewer.SDSSLOGVIEWCONSTANTS;


/**
 * This class works with ComposiedColorAction. This class will render the assigned
 * color array in a visual item and paint the text label based on these color arrays
 * in a Visualization. <p />
 * This class is used in the demon function of the SDSS Log Viewer<p />
 * @author James
 */
public class CompositedColorLabelRenderer extends AbstractShapeRenderer {

    //base size of font. default is the size of font
    private double m_baseSize=1.0;
    //specify the column that stores color list, default is colors of sdss contants
    private String m_colors = SDSSLOGVIEWCONSTANTS.COLORS;
    //specify the column that stores token list
    private String m_tokens;
    //the overall shape of texts
    @SuppressWarnings("unused")
	private Rectangle2D m_shape=new Rectangle2D.Double();
    //the overall bound of texts
    @SuppressWarnings("unused")
	private Rectangle2D m_bound = new Rectangle2D.Double();
    //the space between tokens, default is one space.
    private String m_space = " ";

    //dimension of text field
    private Dimension m_textDm = new Dimension();
    private Font m_font;

    /**
     * Specify the text field that needs to be rendered.<p />
     * Default color column is SDSSLOGVIEWCONSTANTS.COLORS<p />
     * Default space character is a white space.<p />
     * @param textField - the text filed needs to be rendered<p />
     */
    public CompositedColorLabelRenderer(String textField){
        this(textField, " ");
    }

    /**
     * Specify the text filed that needs to be rendered and the characters that 
     * separate these texts.<p />
     * 
     * Default color column is SDSSLOGVIEWCONSTANTS.COLORS<p />
     * @param textField - the text filed needs to be rendered<p />
     * @param space - character used to separate texts.<p />
     */
    public CompositedColorLabelRenderer(String textField, String space){
        this(textField, space, SDSSLOGVIEWCONSTANTS.COLORS);
    }

    /**
     * Specify the text filed that needs to be rendered, the characters that separate
     * these texts, and the color column that will assign colors to text labels.<p />
     * @param textField - the text filed needs to be rendered<p />
     * @param space - character used to separate texts.<p />
     * @param colorField - column that stores color assigned to texts.<p />
     */
    public CompositedColorLabelRenderer(String textField, String space, String colorField){
        m_tokens = textField;
        m_space = space;
        m_colors = colorField;
    }

    /**
     * Set the color column.<p />
     * @param colorlist - column that stores color assigned to texts.<p />
     */
    public void setColorColumn(String colorlist){
        m_colors = colorlist;
    }

    /**
     * Get the column that store colors.<p />
     * @return the column of colors.
     */
    public String getColorColumn(){
        return m_colors;
    }

    /**
     * Get text tokens from a visual item.<p />
     * @return a string array.
     */
    protected String[] getTokens(VisualItem vi){

        if (vi.get(m_tokens) instanceof String[]){
            return (String[]) vi.get(m_tokens);
        } else
            return null;
    }

    /**
     * Get the color array that have been assigned to text in 
     * PrefusePlus.Action.CompositedColorAction.<p />
     * @param vi
     * @return 
     */
    protected int[] getColors(VisualItem vi){

        if (vi.get(m_colors) instanceof int[]){
            return (int[]) vi.get(m_colors);
        } else
            return null;
    }

    /**
     * Compute the dimensions of text string. And get the overall bound of this
     * visual item.<p />
     * NOTE: revised based on Heer's Prefuse LableRenderer<p />
     * @param vi
     */
    protected void computeTextDimensions(VisualItem vi){

        m_font = vi.getFont();
        m_baseSize = vi.getSize();

        if (m_baseSize != 1.0){
            m_font = FontLib.getFont(m_font.getName(), m_font.getStyle(),
                    m_baseSize * m_font.getSize());
        }

        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);

        //calculate the text dimensions
        m_textDm.width = 0;
        String[] tokens = getTokens(vi);
        int w = 0;
        for (int i=0;i<tokens.length;i++){
            w += fm.stringWidth(tokens[i] + m_space);
        }

        m_textDm.width = Math.max(w, m_textDm.width);
        m_textDm.height = fm.getHeight();
    }


    @Override
    /**
     * Override superclass' render method to allow Visualization to paint the text
     * in different color.<p />
     */
    public void render(Graphics2D g, VisualItem vi){

        if (vi.isVisible()){
            String[] tokens= getTokens(vi);
            int[] colors=getColors(vi);

            if (tokens!=null && colors !=null){

                g.setFont(m_font);
                FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
                float x = (float) vi.getX(),
                      y = (float) vi.getY() + fm.getDescent();
                int color;

                for (int i=0;i<tokens.length;i++){
                    color = colors[i];
                    if (tokens[i] !=  null && ColorLib.alpha(color) > 0){
                        g.setPaint(ColorLib.getColor(color));
                        g.drawString(tokens[i] + m_space, x, y);

                        x += fm.stringWidth(tokens[i] + m_space);
                    }
                }
            }//else do nothing
        }
    }

    @Override
    /**
     * Based on computed text dimension, return the overall shape of these text
     * labels in a visual item. Other classes will use this method to check the 
     * shape of this visual item.<p />
     */
    protected Shape getRawShape(VisualItem vi) {
        return getOverallShape(vi);
    }

    /**
     * Compute the overall shape of a visual item, enbracing all text labels.<p />
     * @param vi
     * @return 
     */
    private Shape getOverallShape(VisualItem vi){
        computeTextDimensions(vi);
        Rectangle2D overallShape = new Rectangle2D.Double();
        overallShape.setRect(vi.getX(), vi.getY(), m_textDm.getWidth(), m_textDm.getHeight());
        return overallShape;
    }

    @Override
    /**
     * Similar to getRawShape method. But this method includes transformation information
     * so could be used by other classes when the Display is zoomed.<p />
     */
    public Shape getShape(VisualItem vi){
        AffineTransform at = getTransform(vi);
        Shape rawShape = getOverallShape(vi);
        return (at==null || rawShape==null ? rawShape
                 : at.createTransformedShape(rawShape));
    }

    @Override
    /**
     * Set the bounds of a visual item so that all text labels will be included 
     * and ready to be interactive.<p />
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

}
