/* Written by FlorianB for SQLQueryLogTransformer
 * This file uses code from Jian Zhangs SDSS Log Viewer to transform SDSS SkyServer CSV files into tabular format that can be stored in database.
 * Can be used with any database since it uses its own format.
 * Basically, we split large files and parse each on its own.
 * To handle many errors in the CSV files we check if the current line has enough tuples (21). If not we add the next line.
 */
package transformation;

//import java.io.FileInputStream;
import java.io.File;

import sdsslogviewer.data.SDSSLogFileManager;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.data.io.DataIOException;
import prefuse.data.Table;
import prefuse.util.PrefuseLib;

import sdsslogviewer.Event.DataTableListener;
import sdsslogviewer.data.SDSSLogTable;
import sdsslogviewer.data.io.SDSSLogFileReader;

public class CSVparser {

	/**
	 * @param string
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 */
    public static String[] tuple = new String[21];//21 columns of interest in SQL Log
    public static int tupleSize=0;
    static Table z_vizTable = null;
    static Table datatable = null;
    static SDSSLogFileReader z_reader;
    static SDSSLogFileManager manager;
//	static CSV csv = CSV
//		    .separator(',')  // delimiter of fields
//		    .quote('"')      // quote character
//			.skipLines(1)
//		    .create();       // new instance is immutable
	public static void main(String csvFile) throws SQLException, DataIOException, IOException, InterruptedException {

        
     return;
			
			
			
//        csv.read(csvFile, new CSVReadProc() {
//        	int checkSize;
//            public void procRow(int rowIndex, String... values) {
//                System.out.println(rowIndex + ": " + Arrays.asList(values));
//                checkSize = checkTuple(values);
//                System.out.println(checkSize);
//                if(checkSize==21){
//                	try {
//                		transformation.Transform.saveTuple(tuple);
//                		tupleSize=0;
//                	} catch (SQLException e) {
//                		// TODO Auto-generated catch block
//                		e.printStackTrace();
//                	}
//                }
//            }
//        });
//         return;
		


        
	}
	
//	protected String CSVparser(String line){
//
//        CSVParser parser = new CSVParser(',', '"');
//        String[] col = null;
//        try {
//            col = parser.parseLine(line);
//        } catch (IOException ex) {
//            System.err.println("CSV parser error(1001): " + line);
//        }
//
//        String output = "";
//        if (col !=  null){
//            for (int i=0;i<col.length;i++){
//                output += col[i] + "\t";
//            }
//            output.trim();
//        }
//        return output;
//    }
	
	public static int checkTuple(String... values){ //Check if 21 attributes reached. If Not add next line from CSV
		if(values.length<21){
        	if(tupleSize==0){
        		System.arraycopy(values, 0, tuple, 0, values.length);
        		tupleSize = values.length;
        		return tupleSize;
        	}
        	else if(tupleSize<21){
        		tuple[17]+=values[0];
        		if(values.length>1){
        			if(values[values.length-1]=="1"){
        				for(int i = 1; i<values.length; i++){
        					tuple[i+tupleSize-1]=values[i];
        					//if(i%500 == 0) System.out.println("Saved "+i+" of "+values.length+" tuples.");
        				}
        				tupleSize=tupleSize+values.length-1;
        			}
        			else{
        				for(int i = 0; i<values.length; i++){
        					tuple[17]+=values[i];
        				}
        			}
        		}
        		return tupleSize;
        	}
        }
		if(values.length>21){
        	if(tupleSize==0){
        		System.arraycopy(values, 0, tuple, 0, 17);
        		for(int i = 18; i<values.length; i++){
    				tuple[17]+=values[i];
    			}
        		tupleSize = 18;
        		return tupleSize;
        	}
        	else if(tupleSize<21){
        		return tupleSize;
        	}
        }
		tuple = values;
		return values.length;
	}
}
