/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package prefusePlus.Action;

import java.awt.geom.Rectangle2D;
import prefuse.action.EncoderAction;
import prefuse.visual.VisualItem;
import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * <p>Assign a set of rectangles to a VisualItem by a specified length field.</p>
 * The length filed contains an int array that specifies the length of each rectangle.<p />
 * <p>By default a CompositedShapeAction looks for the given data field and retrieve
 * an int array, and assign a set of lengths based on this array.
 * </p>
 * 
 * NOTE: the assigned data filed should be an int array.<p />
 * The default filed is SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH.
 * 
 * @author James
 */
public class CompositedShapeAction extends EncoderAction {

    /* The column specified lenght field*/
    private String m_CompositField = SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH;
//    private String m_splitter = "\t";
    /* The height of all rectangles. Default is 1 pixel*/
    private int m_height = 1;
    /* The shape of each token. Default is a rectangle*/
    private Rectangle2D m_shape = new Rectangle2D.Double();

    /**
     * Construct a CompositedShapeAction. Default is to find the SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH
     * column for rectangle length array<p />
     * @param group - name of a visual table<p />
     * 
     */
    public CompositedShapeAction(String group){
        this(group, SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH);
    }

    /**
     * Construct a CompositedShapeAction with specified column for rectangle 
     * length array<p />
     * @param group - name of a visual table<p />
     * @param field - the column with specified array of lengths in an int array<p />
     */
    public CompositedShapeAction(String group, String field){
        super(group);
        m_CompositField = field;
    }

/*    public CompositedShapeAction(String group, String field, String splitter){
        super(group);
        m_CompositField = field;
        m_splitter=splitter;
    }
*/

    @Override
    /**
     * Override superclass' process method for return of a shape.<p />
     * This method return a rectangle that include all rectangles in it, so that
     * the bound of this visual item would include all rectangles.
     */
    public void process(VisualItem vi, double d) {

        int[] token_length = (int[]) vi.get(m_CompositField);
        int size = 0;
        Double x = vi.getX();
        Double y = vi.getY();

        for (int i=0;i<token_length.length;i++){
            size += token_length[i];
        }

        m_shape.setRect(x, y, size, m_height);
    }

}
