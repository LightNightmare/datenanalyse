package sdsslogviewer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import sdsslogviewer.Viz.SampleVizer;

/**
 * A new GUI window that allow users to input an SQL string and view the visual 
 * representation of the SQL string.<p/>
 *
 * @author James<p/>
 */
@SuppressWarnings("serial")
public class SQLExampleWindow extends JFrame {

    //- components in this window
    private JLabel z_inputlabel1 = new JLabel("  Please input your SQL in the below field.\n"),
                   z_inputlabel2 = new JLabel("  Click \"Show me\" button to see the visual transformation, and \n"
                   + "\"Cancel\" to clean all");
    private JTextField z_input;
    private JButton z_showButton, z_cancelButton;
    private SampleVizer z_vizer;
    private JPanel upper_panel = new JPanel();
    private JPanel lower_panel = new JPanel();

    //- control variable of this window
    private int WIDTH = 600, HEIGHT = 300;
    
    /**
     * Construct a new window of SQL example, including an input window, two buttons,
     * and a visualization that shows three lines of texts.<p/>
     */
    public SQLExampleWindow(){
        //-1. Create new components and layout
        init();
        //-2. pack and appear
        showWindow();
    }

    /**
     * initialize the window
     */
    private void init(){
        //-Initialize new components
        z_input = new JTextField(100);
        z_showButton  = new JButton("Show me");
        z_cancelButton = new JButton("Cacnel");
        z_vizer = new SampleVizer("");

        //- layout components
        upper_panel.setLayout(new GridLayout(3,1));
        upper_panel.add(z_inputlabel1);
        upper_panel.add(z_inputlabel2);

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(2));
        box.add(z_input);
        box.add(Box.createHorizontalGlue());
        box.add(z_showButton);
        box.add(Box.createHorizontalStrut(2));
        box.add(z_cancelButton);

        upper_panel.add(box);

        lower_panel.setLayout(new BorderLayout());
        lower_panel.add(z_vizer, BorderLayout.CENTER);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(upper_panel, BorderLayout.NORTH);
        this.getContentPane().add(lower_panel, BorderLayout.CENTER);

        //- Add actions to control components
        z_showButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                showAction();
            }
        });

        z_cancelButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                cancelAction();
            }
        });

    }

    /**
     * pack up and show
     */
    private void showWindow(){

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);

    }

    //----- Actions -------
    /**
     * Retrieve text from input field and visualize it
     */
    private void showAction(){
        String sql = z_input.getText();
        if (!sql.equals("")){

            lower_panel.remove(0);

            z_vizer.cleanUp();
            z_vizer = new SampleVizer(sql);

            lower_panel.add(z_vizer, BorderLayout.CENTER);
            lower_panel.updateUI();

        }else //show error message
            JOptionPane.showMessageDialog(this, "There is nothing to show",
                    "Empty input", JOptionPane.ERROR_MESSAGE);
    }

    private void cancelAction(){

        z_input.setText("");

        lower_panel.remove(0);

        z_vizer.cleanUp();
        z_vizer = new SampleVizer("");

        lower_panel.add(z_vizer, BorderLayout.CENTER);
        lower_panel.updateUI();
    }

}
