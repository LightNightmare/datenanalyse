
package prefusePlus.Renderer;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import prefuse.Visualization;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * New renderer to draw a line that connect two nodes.<P />
 * @author James
 */
public class LineShapeRenderer extends AbstractShapeRenderer {

    private double m_baseSize=10;
    //-- String name of the group where source and target visualitem belongs to.
    private String SOURCE="sourcetable";
    private GeneralPath m_line=new GeneralPath();

    public LineShapeRenderer(String source){
        SOURCE=source;
    }

    public LineShapeRenderer(double size){
        m_baseSize=size;
    }

    public void setSource(String source){
        SOURCE=source;
    }

    public String getSource(){
        return SOURCE;
    }

    public void setSize(double size){
        m_baseSize=size;
    }

    public double getSize(){
        return m_baseSize;
    }

    @Override
    public Shape getRawShape (VisualItem vi) {

        if (vi.isVisible()){
        Visualization v=vi.getVisualization();
        int s=vi.getInt("sourceitem");
        int t=vi.getInt("targetitem");

        VisualTable ts=(VisualTable) v.getGroup(SOURCE);
//        VisualItem v1=ts.getItem(s-1);
//        VisualItem v2=ts.getItem(t-1);
        VisualItem v1=ts.getItem(s);
        VisualItem v2=ts.getItem(t);

        double x1=v1.getX();
        double y1=v1.getY();
        double x2=v2.getX();
        double y2=v2.getY();
        m_line.reset();
        m_line.moveTo(x1, y1);
        m_line.lineTo(x2, y2);

        return m_line;
        }
        return null;
    }

}
