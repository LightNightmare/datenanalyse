/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdsslogviewer.Event;

import java.util.EventListener;

/**
 * A simple Listener for control "data table is ready" event.<p/>
 * @author James
 */
public interface DataTableListener extends EventListener{

    /**
     * Set the action when an table is ready event occurs.<p/>
     * @throws InterruptedException 
     */
    public void TableIsReady() throws InterruptedException;
    
}
