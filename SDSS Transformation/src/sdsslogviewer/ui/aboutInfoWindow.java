package sdsslogviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * A GUI window to show About information.<p/>
 * @author JZhang
 */
@SuppressWarnings("serial")
public class aboutInfoWindow extends JFrame {

    JFrame frame;

    /**
     * Construct an about information window<p/>
     */
    public aboutInfoWindow(){

        super("About SDSS Log Viewer");
        this.setPreferredSize(new Dimension(300, 250));
        this.setResizable(false);

        int screenWidth=Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight=Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setLocation(screenWidth/2-150,screenHeight/2-125);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());

        JPanel messagePanel=new JPanel(new BorderLayout());

        JTextArea aboutarea=new JTextArea();
        aboutarea.setFont(new Font(Font.SANS_SERIF,Font.BOLD,16));
        aboutarea.setLineWrap(true);
        aboutarea.setWrapStyleWord(true);
        aboutarea.setEditable(false);
        aboutarea.setForeground(Color.BLACK);
        aboutarea.setBackground(Color.WHITE);
        aboutarea.setText("       SDSS Log Viewer version 1.2"+"\n\n");
        aboutarea.append("Created in 2010 at Drexel for my dissertation."+"\n\n"+
                          "Author: Jian Zhang @Drexel IST"+"\n\n"+
                          "Contact: jz85@drexel.edu");

        JButton closeButton=new JButton("Close");

        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        messagePanel.add(aboutarea, BorderLayout.CENTER);
        messagePanel.add(closeButton, BorderLayout.SOUTH);

        this.add(messagePanel,BorderLayout.CENTER);

        this.pack();
        this.setVisible(true);
    }

}
