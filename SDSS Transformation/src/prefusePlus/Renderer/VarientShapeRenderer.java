package prefusePlus.Renderer;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import prefuse.Constants;
import prefuse.render.ShapeRenderer;
import prefuse.visual.VisualItem;
import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * This render return different shapes based on a type filed. Similar as Heer did
 * in the Congress demo.<p />
 * This class is specified for the SDSS Log Viewer and draw two shapes only. So 
 * it is not a common and generic renderer for other purpose.<p />
 * But method used in this class is generic enough to help 
 * prefuse.render.ShapeRenderer. So it could work in the same way as DataColorAction
 * or DataShapeAction did.<p />
 * @author James
 */
@SuppressWarnings("unused")
public class VarientShapeRenderer extends ShapeRenderer{

    /**Variables for info from visualitem*/
	private String z_RA = SDSSLOGVIEWCONSTANTS.AREA_RA;
    private String z_DEC = SDSSLOGVIEWCONSTANTS.AREA_DEC;
    private String z_size1 = SDSSLOGVIEWCONSTANTS.AREA_WIDTH;
    private String z_size2 = SDSSLOGVIEWCONSTANTS.AREA_HEIGHT;
    private Double z_BaseSize;
//    private String z_splitter;

    /**local variables*/
    private int type;       //shape type
    private double x = 0.0,
                   y = 0.0,
                   radius = 0.0,
                   width = 0.0,
                   height = 0.0;
    private String[] w_h = null;

    private Rectangle2D z_rect = new Rectangle2D.Double();
    private Ellipse2D z_circle = new Ellipse2D.Double();


    /* Construct a VarientShapeRenderer*/
    public VarientShapeRenderer(){
//        z_BaseSize = size;
    }

    @Override
    /**
     * Generate two shapes based on the type filed in a visual item. The size of
     * a shape is determined by the base size.<p />
     */
    public Shape getRawShape(VisualItem vi){

        //--. get shape type
        type = vi.getShape();

        //--. get center x and y
        x = vi.getX();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        y = vi.getY();
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;

        //--. get width and height
        z_BaseSize = vi.getSize();
        width = vi.getDouble(z_size1)*z_BaseSize;
        height = vi.getDouble(z_size2)*z_BaseSize;

        if (type == Constants.SHAPE_ELLIPSE){
            radius = width;
            x = x-radius;
            y = y-radius;
            z_circle.setFrame(x, y, 2*radius, 2*radius);
            return z_circle;
        } else {
            if (type == Constants.SHAPE_RECTANGLE){
                x = x - width/2;
                y = y - height/2;
                z_rect.setFrame(x, y, width, height);
//System.out.println("drawing a rect...");
                return z_rect;
            } else return null;
        } //end of if

    } //end of getrawshape method

}
