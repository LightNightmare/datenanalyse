/* Written by FlorianB for SQLQueryLogTransformer
 * Only for reference
 */
package transformation;

import java.io.FileNotFoundException;
//import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.File;

import javax.xml.stream.XMLStreamException;
import sdsslogviewer.data.SDSSLogFileManager;
import sdsslogviewer.data.SDSSLogTable;
import prefuse.data.Table;

public class test {
	static SDSSLogFileManager manager;
	static Table datatable = null;     //default is null
	static Connection conn;
	static PreparedStatement pstmt;
	static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
	/**
	 * @param args
	 * @throws XMLStreamException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws SQLException, FileNotFoundException, XMLStreamException {
		//Establish connection
		try {
			Class.forName ("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
		int month=5;
		int limitMonth=1;
		int limitYear=2012;
		String monthString;
		conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","sdss", "");
		for(int year=2012; year<=limitYear; year++){
			while (month != 0){
				if(month < 10){
					monthString="0"+String.valueOf(month);
				}
				else{
					monthString=String.valueOf(month);
				}
				System.out.println("Parsing File: data/BESTDR9_"+String.valueOf(year)+"-"+monthString+"_errorAll_rowsAll"+".csv");
				//transformation.CSVparser.main("data/BESTDR9_"+String.valueOf(year)+"-"+monthString+"_errorAll_rowsAll"+".csv");
				manager = new SDSSLogFileManager(new File("data/BESTDR9_"+String.valueOf(year)+"-"+monthString+"_errorAll_rowsAll"+".csv"));
                manager.initManager();
				SDSSLogTable sdsstable = new SDSSLogTable(manager.getVizTable());
                datatable =  sdsstable.getSDSSLogTable();
				month++;
				if(month>12)month=1;
				if(month>limitMonth && year==limitYear)month=0;
			}
		}
		} catch(Exception e){e.printStackTrace();}
		finally{
		conn.close();
		}

	}
	public static void saveTuple(String[] tuple) throws SQLException {
		try{
		pstmt = conn.prepareStatement("insert into \""+"ORIGINAL\" (\"YY\", \"MM\",\"DD\",\"HH\",\"MI\",\"SS\",\"SEQ\",\"THETIME\",\"LOGID\",\"CLIENTIP\",\"REQUESTOR\",\"SERVER\",\"DBNAME\",\"ACC\",\"ELAPSED\",\"BUSY\",\"NRROWS\",\"STATEMENT\",\"ERROR\",\"ERRORMESSAGE\") values (?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, Integer.parseInt(tuple[0]));
		pstmt.setInt(2, Integer.parseInt(tuple[1]));
		pstmt.setInt(3, Integer.parseInt(tuple[2]));
		pstmt.setInt(4, Integer.parseInt(tuple[3]));
		pstmt.setInt(5, Integer.parseInt(tuple[4]));
		pstmt.setInt(6, Integer.parseInt(tuple[5]));
		pstmt.setInt(7, Integer.parseInt(tuple[6]));
		//THETIME:
		Timestamp date1 = StringToSQLDate(tuple[7]);
		pstmt.setTimestamp(8, date1);  
		pstmt.setInt(9, Integer.parseInt(tuple[8]));    
		pstmt.setString(10, tuple[9]);
		pstmt.setString(11, tuple[10]);
		pstmt.setString(12, tuple[11]);
		pstmt.setString(13, tuple[12]);
		pstmt.setString(14, tuple[13]);
		pstmt.setDouble(15, Double.parseDouble(tuple[14]));
		pstmt.setDouble(16, Double.parseDouble(tuple[15]));    
		pstmt.setInt(17, Integer.parseInt(tuple[16]));  
		pstmt.setString(18, tuple[17]);  
		pstmt.setInt(19, Integer.parseInt(tuple[18])); 
		pstmt.setString(20, tuple[19]);    
		
        pstmt.executeUpdate();
		}catch(Exception e){e.printStackTrace();}
		finally{
			pstmt.close();
		}
		

	}
	private static Timestamp StringToSQLDate(String s) {
        Timestamp sqlDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
            sqlDate = new java.sql.Timestamp(sdf.parse(s).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqlDate;
    }
}
