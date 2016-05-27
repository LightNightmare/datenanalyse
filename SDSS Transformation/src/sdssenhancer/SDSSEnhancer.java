/* Written by FlorianB for SQLQueryLogTransformer
 * This is the starting point. The interface is constructed, db connections managed and the single modules are called
 */
package sdssenhancer;

import geolocation.Geolocator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Select;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.regionName;
import com.maxmind.geoip.timeZone;

import accessarea.AccessArea;
import accessarea.AccessAreaExtraction;

import prefuse.data.Tuple;
import prefuse.data.io.DataIOException;

public class SDSSEnhancer{ //Starts the Interface, manages calls from external classes such as the CSV Transformer

		static SDSSHandler sdssHandler = new SDSSHandler();
		
		public static void main(String[] args) { //Start the User Interface
			SwingUtilities.invokeLater(new Runnable() {
	    		public void run() {
	    	UI mainUI = new UI();
			mainUI.addListener(sdssHandler);
			mainUI.setVisible(true);
	    	}});
		}
		
		
}
		
class SDSSHandler implements ValueSubmittedListener { //Is called by the ActionHandler through the interface and handles the five different modules and DB connections

	public static Connection conn;
	private static PreparedStatement pstmt;
	//private static int errorCount=0;
	private static int counter=0;
	//private static long startTime,endTime;
	static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");

    
    
    public SDSSHandler() {}

    
	@Override
	public void onSubmitted(int option, String inputFile, String outputFile) { //Called by the ActionHandle with the necessary parameters to call the modules. 'option' defines the module
		System.out.println("submitted");
		switch (option) {
		case 1://Option one: user wants to convert from CSV
			try {
				transformLog(inputFile, outputFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		
		default:
			break;
		}
	}

	
	public void transformLog(String inputFile, String outputFile) throws Exception {
		AccessAreaExtraction extraction = new AccessAreaExtraction();
		
		 long index = new Long(0);
	        Scanner scanner = new Scanner(new FileInputStream(inputFile));
	        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFile)));
	        Pattern regex = Pattern.compile("\"[^\"]+\"");
	        scanner.nextLine();
	        int current = 0;
	        AccessArea accessArea;
	        while (scanner.hasNext()) {
	        	try
	   		 {
	        		if(++current%1000==0) {
						//pst.executeBatch(); //does not speed it up much
						System.out.println(current + " rows processed");
					}
	        		try {
	        			String s = scanner.nextLine().replace("\"\"", " ").replace("FROM", " FROM ").replace("WHERE", " WHERE ");
						accessArea = extraction.extractAccessArea(s);
					} catch (Throwable t) {
						Expression expression = new NullValue();
						accessArea = new AccessArea(new ArrayList<FromItem>(), expression);
					}
	        		
	        		//check if access area is completely empty. Don't save this then
					String from = "";
					String where = "";
					
					try {
						from = accessArea.getFrom().toString();
						from = from.substring(1, from.length()-1);
					} catch (NullPointerException e) {
						from = "";
					}
					try {
						where = accessArea.getWhere().toString();
						if(where.equals("NULL") | where.equals("TRUE")) {
							where = "";
						}
					} catch (NullPointerException e) {
						where = "";
					}		
					if(!from.equals("") && !where.equals("")) {
						writer.write(from + ";");
		            	writer.write(where + ";");
		            	index++;
		                long seq = index;
		                writer.write(Long.toString(seq));
		                writer.newLine();
					}
	   		 		}
						catch (Exception e) {
						}	
	            
	            
	        }
	        scanner.close();
	        writer.close();

	}
	
	
	
}

//Interface for the calls the ActionHandler uses
interface ValueSubmittedListener {
    public void onSubmitted(int option, String inputFile, String outputFile);
}

@SuppressWarnings("serial") //The User Interface
class UI extends JFrame implements ActionListener{
	
	private List<ValueSubmittedListener> listeners = new ArrayList<ValueSubmittedListener>();


	private JTextField outputFileTF = new JTextField(20);
	private JTextField inputFileTF = new JTextField(40);
	final JTextPane jTextPane = new JTextPane();
	private final static File local = new File(".");
	private JFileChooser fileChooser = new JFileChooser();
	private JPanel panel = new JPanel();
	String file = null;

    public void addListener(ValueSubmittedListener listener) {
        listeners.add(listener);
    }
    //Action Listener notification
    private void notifyListeners(int option, String inputFile, String outputFile) {
        for (ValueSubmittedListener listener : listeners) {
			//System.out.println("Could not establish Connection");
            listener.onSubmitted(option, inputFile, outputFile);
        }
    }

	
	public UI() {
	        
	    initUI();
	}

	private void initUI() {

		//build the UI
		JPanel buttons = new JPanel(); //just for the buttons, one line at the bottom
		getContentPane().add(panel);
		getContentPane().add(buttons,BorderLayout.PAGE_END);
	   
	   panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

	   JButton transformLog = new JButton("Transform log");


	   JLabel inputFileLabel = new JLabel("Input file: ");
	   JLabel outputFileLabel = new JLabel("Output file: ");

	   JLabel StatusLabel = new JLabel("Status: ");




	   transformLog.addActionListener(this);
	   panel.add(inputFileLabel);
	   panel.add(inputFileTF);
	   panel.add(outputFileLabel);
	   panel.add(outputFileTF);
	   panel.add(transformLog);
	   
	   panel.add(StatusLabel);
	   panel.add( new JScrollPane( jTextPane ) );
	   MessageConsole mc = new MessageConsole(jTextPane);
	   mc.redirectOut();
	   mc.redirectErr(Color.RED, null);
	   mc.setMessageLines(250);
	   jTextPane.setPreferredSize(new Dimension(880, 130));
	   jTextPane.setMinimumSize(new Dimension(10, 10));

	    setTitle("SDSS Transformation");
	    setSize(880, 300);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	//The actual ActionHandler. Depending on which button was pressed, checks if necessary parameters are not inserted and either prompts the user or starts the further process
	public void actionPerformed(final ActionEvent event) {
		Thread newThread = new Thread() {
		      public void run() {
		String inputFile = inputFileTF.getText();
		String outputFile = outputFileTF.getText();
		
		float userSessionTime = -1;
		
		
		
		//Which button was pressed? Check parameters, call function
		switch (event.getActionCommand()) {
		case "Transform log":
			if(inputFile.isEmpty() || outputFile.isEmpty()){
				JOptionPane.showMessageDialog(panel, "Please make sure to enter:\ninput file\nUoutput file\nfor SDSS Data",
	                    "Challenge", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			notifyListeners(1,inputFile,outputFile);
			break;
		
		default:
			break;
		}
	}
		};
		newThread.start();
	}
}
