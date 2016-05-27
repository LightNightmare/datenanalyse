/*
 * Dec. 22nd, 2010: Fix bugs.
 * 
 */

package sdsslogviewer.Controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import prefuse.data.Table;
import prefuse.data.column.ColumnMetadata;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.query.ListQueryBinding;
import prefuse.util.UpdateListener;
import prefuse.util.ui.JToggleGroup;
import prefusePlus.util.ui.JRangeSliderPlus;

import sdsslogviewer.Event.MenuChangedListener;
import sdsslogviewer.data.SDSSLogTable;

/**
 * The content of the dynamic menu, implemented as a pop up menu. <p/>
 * This menu take a SDSS Log Table as input, and extract features from<p/>
 * SDSSLogTable.IPADDRESS<p/>
 * SDSSLogTable.REQUESTOR<p/>
 * SDSSLogTable.SERVER<p/>
 * SDSSLogTable.DATABASE<p/>
 * SDSSLogTable.ACCESSPORTAL<p/>
 * SDSSLogTable.ERROR<p/>
 * SDSSLogTable.ELAPSED<p/>
 * SDSSLogTable.ROWS<p/>
 * to create three kind of control menu components.<p/>
 * @author James
 */
@SuppressWarnings({ "serial" })
public class FilterPopupMenu extends JPanel {

    //-- . local data structure
    private EventListenerList z_listeners;

    //-- . major return lowValue
    private AndPredicate filter;
    private Predicate eclapesedP = null,
                      rowsP = null;

    //--. components in this class
    private JRangeSliderPlus eclapesedL,
                             rowsL;

//    JPanel uppane, downpane;
//    JButton okB, cancelB;

    /**
     * Construct an empty popup menu.<p/>
     */
    public FilterPopupMenu(){
        //DO nothing. Just return a component
    }

    /**
     * Construct a new FilterPopupMenu with input table to get dynamic query feature.<p/>
     * @param t - table to get dynamic query feature<p/>
     */
    public FilterPopupMenu(Table t){
        this.setForeground(Color.GRAY);
        this.setBackground(Color.WHITE);
        z_listeners = new EventListenerList();
        initUI(t);
    }

    /**
     * Get the predicate,created by combination of all control menus.<p/>
     * @return 
     */
    public Predicate getPredicate(){
        return filter;
    }

    /**
     * Dec 22. '10  resume List query binding with all option.
     */
    private void initUI(Table z_t){

        ListQueryBinding ipQ = new ListQueryBinding(z_t, SDSSLogTable.IPADDRESS);
        ListQueryBinding requestorQ = new ListQueryBinding(z_t, SDSSLogTable.REQUESTOR);
        ListQueryBinding serverQ = new ListQueryBinding(z_t, SDSSLogTable.SERVER);
        ListQueryBinding databaseQ = new ListQueryBinding(z_t, SDSSLogTable.DATABASE);
        ListQueryBinding accessQ = new ListQueryBinding(z_t, SDSSLogTable.ACCESSPORTAL);
        ListQueryBinding errorQ = new ListQueryBinding(z_t, SDSSLogTable.ERROR);

        filter = new AndPredicate(ipQ.getPredicate());
        filter.add(requestorQ.getPredicate());
        filter.add(serverQ.getPredicate());
        filter.add(databaseQ.getPredicate());
        filter.add(accessQ.getPredicate());
        filter.add(errorQ.getPredicate());

        filter.addExpressionListener(new UpdateListener(){
            @Override
            public void update(Object arg0) {
                fireMenuChanged();
            }
        });

        ColumnMetadata cmd = z_t.getMetadata(SDSSLogTable.ELAPSED);
        float min_e = z_t.getFloat(cmd.getMinimumRow(), SDSSLogTable.ELAPSED);
        float max_e = z_t.getFloat(cmd.getMaximumRow(), SDSSLogTable.ELAPSED);
        eclapesedL = new JRangeSliderPlus(min_e, max_e, min_e, max_e);
        eclapesedL.setPreferredSize(new Dimension(300, 20));
        eclapesedL.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                if (eclapesedP!=null){
                    filter.remove(eclapesedP);
                }
                float lowValue = eclapesedL.getLowValue().floatValue();
                float highValue = eclapesedL.getHighValue().floatValue();
                String query = SDSSLogTable.ELAPSED + ">=" + lowValue + " AND " +
                               SDSSLogTable.ELAPSED + "<=" + highValue;
                eclapesedP = (Predicate) ExpressionParser.parse(query);
                filter.add(eclapesedP);
                fireMenuChanged();
            }
        });

        cmd = z_t.getMetadata(SDSSLogTable.ROWS);
        long min_r = z_t.getInt(cmd.getMinimumRow(), SDSSLogTable.ROWS);
        long max_r = z_t.getInt(cmd.getMaximumRow(), SDSSLogTable.ROWS);
        rowsL = new JRangeSliderPlus(min_r, max_r, min_r, max_r);
        rowsL.setPreferredSize(new Dimension(300, 20));
        rowsL.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                if (rowsP!=null){
                    filter.remove(rowsP);
                }
                long lowValue = rowsL.getLowValue().longValue();
                long highValue = rowsL.getHighValue().longValue();
                String query = SDSSLogTable.ROWS + ">=" + lowValue + " AND " +
                               SDSSLogTable.ROWS + "<=" + highValue;
                rowsP = (Predicate) ExpressionParser.parse(query);
                filter.add(rowsP);
                fireMenuChanged();
            }
        });

        JToggleGroup ipG = ipQ.createCheckboxGroup();
        ipG.setAxisType(BoxLayout.Y_AXIS);
        JScrollPane ipsp = new JScrollPane(ipG);
        JToggleGroup reqG = requestorQ.createCheckboxGroup();
        reqG.setAxisType(BoxLayout.Y_AXIS);
        JScrollPane reqsp = new JScrollPane(reqG);
        JToggleGroup servG = serverQ.createCheckboxGroup();
        servG.setAxisType(BoxLayout.Y_AXIS);
        JScrollPane servsp = new JScrollPane(servG);
        JToggleGroup dbG = databaseQ.createCheckboxGroup();
        dbG.setAxisType(BoxLayout.Y_AXIS);
        JScrollPane dbsp = new JScrollPane(dbG);
        JToggleGroup accG = accessQ.createCheckboxGroup();
        accG.setAxisType(BoxLayout.Y_AXIS);
        JScrollPane accsp = new JScrollPane(accG);

        Box upleft = new Box(BoxLayout.Y_AXIS);
        Box upright = new Box(BoxLayout.X_AXIS);
        Box rtbox = new Box(BoxLayout.X_AXIS);
        Box rowbox = new Box(BoxLayout.X_AXIS);
        Box ipbox = new Box(BoxLayout.Y_AXIS);
        Box reqbox = new Box(BoxLayout.Y_AXIS);
        Box servbox = new Box(BoxLayout.Y_AXIS);
        Box dbbox = new Box(BoxLayout.Y_AXIS);
        Box accessbox = new Box(BoxLayout.Y_AXIS);
        Box errbox = new Box(BoxLayout.X_AXIS);

        rtbox.setBorder(BorderFactory.createTitledBorder("Query run time"));
        rtbox.add(eclapesedL);
        rowbox.setBorder(BorderFactory.createTitledBorder("No. of rows retrieved"));
        rowbox.add(rowsL);
        ipbox.setBorder(BorderFactory.createTitledBorder("IP Address"));
        ipbox.add(ipsp);
        reqbox.setBorder(BorderFactory.createTitledBorder("Requestor"));
        reqbox.add(reqsp);
        servbox.setBorder(BorderFactory.createTitledBorder("Server"));
        servbox.add(servsp);
        dbbox.setBorder(BorderFactory.createTitledBorder("Database"));
        dbbox.add(dbsp);
        accessbox.setBorder(BorderFactory.createTitledBorder("Access Portal"));
        accessbox.add(accsp);
        errbox.setBorder(BorderFactory.createTitledBorder("Error"));
        errbox.add(errorQ.createRadioGroup());

        upleft.setBorder(BorderFactory.createTitledBorder("Range Query"));
        upleft.add(rtbox);
        upleft.add(rowbox);

        upright.setBorder(BorderFactory.createTitledBorder("Select Query"));
        upright.add(ipbox);
        upright.add(reqbox);
        upright.add(servbox);
        upright.add(dbbox);
        upright.add(accessbox);

        JPanel uppane = new JPanel();
        JPanel downpane = new JPanel();

        uppane.setLayout(new BorderLayout());
        uppane.add(upleft, BorderLayout.WEST);
        uppane.add(upright, BorderLayout.CENTER);

        downpane.setLayout(new BoxLayout(downpane, BoxLayout.X_AXIS));
        downpane.add(errbox);
        downpane.add(Box.createHorizontalGlue());

        this.setLayout(new BorderLayout());
        this.add(uppane, BorderLayout.CENTER);
        this.add(downpane, BorderLayout.SOUTH);

        //TODO: Add action listeners to each control. And return predicate
    }

    /**
     * Add Menu Change Listener.<p/>
     * @param mcl - MenuChangeListener. see sdsslogviewer.Event.MenuChangedListener;
     */
    public void addMenuChangeListener(MenuChangedListener mcl){
        z_listeners.add(MenuChangedListener.class, mcl);
    }

    /**
     * Remove Menu Change Listener<p/>
     * @param mcl - MenuChangeListener. see sdsslogviewer.Event.MenuChangedListener;
     */
    public void removeMenuChangeListener(MenuChangedListener mcl){
        z_listeners.remove(MenuChangedListener.class, mcl);
    }

    protected void fireMenuChanged(){
        Object[] listeners = z_listeners.getListenerList();

        int numlistener = listeners.length;
        for (int i=0;i<numlistener;i+=2){
            if (listeners[i] == MenuChangedListener.class){
                ((MenuChangedListener)listeners[i+1]).menuChanged();
            }
        }
    }

    /**
     * release all components and listeners to release memory.<p/>
     */
    public void cleanup(){
//        if (z_listeners.getListenerCount()>0){
//            z_listeners = null;
//        }
        this.removeAll();
    }

}
