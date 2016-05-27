/*
 * A class extends from ColorAction to assign a list of colors to a visulitem
 *
 */

package prefusePlus.Action;

import prefuse.action.EncoderAction;
import prefuse.visual.VisualItem;
import prefusePlus.util.ColorLibPlus;
import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * <p>Assign a set of color values to a VisualItem's column. And each cell contains
 * the set of color values.</p>
 *
 * <p>By default a CompositedColorAction looks for the given data field and retrieves
 * an int array in a cell that contains a set of types. Then assigns a set of color values 
 * based on these types according to a given palette, or a default grayscale palette
 * by computing the scale of types * </p>
 * The default column of this stored color list is SDSSLOGVIEWCONSTANTS.COLORS.<p />
 * NOTE: if use specified color palette, make sure the number of colors in the palette
 * is larger than the number of types. Otherwise a black color will be assigned to
 * unmatched number of types.
 * 
 * @author James
 */

public class CompositedColorAction extends EncoderAction {

    /* The data column that specifies types*/
    private String m_dataField;
    /* The data column that stores assigned color arrays*/
    private String m_colorListField = SDSSLOGVIEWCONSTANTS.COLORS;
    /* The color palette*/
    private int[]  m_palette;

//    private String m_splitter = "\t";

    /**
     * Construct a CompositedColorAction by the specified data group (a visual table)
     * in the param. <p />
     * The default column for this constructor is SDSSLOGVIEWCONSTANTS.COLORS.<p />
     * The default color palette is gray-scale palette.<p />
     * @param group - the visual table<p />
     * 
     * June 20, 2011, these constructor is confusing. So removed
     */
//    public CompositedColorAction(String group){
//        super(group);
//    }

    /**
     * Construct a CompositedColorAction by the specified data group (a visual table)
     * and the data filed that specifies the column of the type list. <p />
     * @param group - the visual table<p />
     * @param dataField - the column that specifies data types. Each row of this 
     * column should contain an int array.<p />
     */
    public CompositedColorAction(String group, String dataField){
        super(group);
        m_dataField=dataField;
    }
    
    /**
     * Construct a CompositedColorAction by the specified data group (a visual table),
     * the data filed that specifies the column of the type list, and the color
     * palette. <p />
     * @param group - the visual table<p />
     * @param dataField - the column that specifies data types. Each row of this 
     * column should contain an int array.<p />     
     * @param palette - user-specified color palette<p />
     */    
    public CompositedColorAction(String group, String dataField, int[] palette){
        super(group);
        m_dataField=dataField;
        m_palette=palette;
    }

    /**
     * Construct a CompositedColorAction by the specified data group (a visual table),
     * the data filed of the visual item, and the color palette. It also specifies
     * the column that will store the assigned color<p />
     * @param group - the visual table<p />
     * @param dataField - the column that specifies data types. Each row of this 
     * column should contain an int array.<p />
     * @param palette - user-specified color palette<p />
     * @param colorlistfield - the column that stores assigned color arrays
     */    
    public CompositedColorAction(String group, String dataField, String colorlistfield, int[] palette){
        super(group);
        m_dataField=dataField;
        m_palette=palette;
        m_colorListField=colorlistfield;
    }


    @Override
    /**
     * Override supclass's process method, assign the colors.<p />
     * For example, a cell has an int array <p />
     * [0,2,3,5],<p />
     * Then, the color palette is <p />
     * [BLACK, WHITE, RED, GREEN, YELLOW, GRAY, CYAN]<P />
     * Now the colorList column is< p />
     * [BLACK, RED, GREEN, GRAY]<P />
     * 
     * NOTE: if use specified color palette, make sure the number of colors in the palette
     * is larger than the number of types. Otherwise a black color will be assigned to
     * unmatched number of types.
     */
    public void process(VisualItem vi, double d) {
        int z_palette_length = m_palette.length;
        
        int[] l=(int[]) vi.get(m_dataField);

        int[] colors = new int[l.length];
        for (int i=0;i<l.length;i++){
            if (i < z_palette_length){
                colors[i] = m_palette[l[i]];
            } else {
                colors[i] = ColorLibPlus.BLACK;
            }
        }
        vi.set(m_colorListField, colors);
    }
}
