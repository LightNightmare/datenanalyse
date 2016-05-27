
package prefusePlus.Renderer;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * <p>Renderer for drawing a rectangle area for a visual item. This renderer
 * works with prefusePlus.AreaLayout together
 * </p>
 * 
 * width = 2ndVisualItem.x - 1stVisualItem.x <p />
 * hight = value in "height"<p />
 *
 * @author James<p />
 * 
 * NOTE: The SDSS Log Viewer does not use this class.<p />
 * 
 */
public class AreaShapeRenderer extends AbstractShapeRenderer {

    private Rectangle2D m_area=new Rectangle2D.Double();

    //-- data to process
    private String m_group;

    AreaShapeRenderer(String group){
        m_group=group;
    }


    @Override
    protected Shape getRawShape(VisualItem vi) {
        VisualItem next_item=null;
        VisualTable vt=(VisualTable) vi.getVisualization().getGroup(m_group);
        int row=vi.getRow();

        if (row<vt.getRowCount()-1){
            next_item=vt.getItem(row+1);
        double x1=vi.getX();
        double y1=vi.getY();
        double x2=next_item.getX();
        double width=x2-x1;
        double hight=vi.getDouble("hight");

        m_area.setFrame(x1, y1, width, hight);
        }

        return m_area;
    }

}
