/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sdsslogviewer.Event;

import java.util.EventListener;

/**
 * A simple listener for detecting any parts of dynamic query menu have been 
 * changed by users.<p/>
 * @author JZhang
 */
public interface MenuChangedListener extends EventListener{

    /**
     * Set the action when menu changes occur.<p/>
     */
    public void menuChanged();

}
