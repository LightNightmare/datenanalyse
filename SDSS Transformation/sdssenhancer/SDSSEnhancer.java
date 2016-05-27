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
import java.io.File;
import java.io.IOException;
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
import net.sf.jsqlparser.statement.select.FromItem;

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
		
		public static void saveTuple(Tuple tuple) { //Save tuples from CSVTransformer to database
			try {
				SDSSHandler.saveTuple(tuple);
			} catch (SQLException e) {
				System.err.println("Could not save tuple to Database");
				//e.printStackTrace();
			}
		}

		public static void closeConnection() { //Grant external classes the ability to close the DB connection and the Prepared Statement
			SDSSHandler.closeConnection();
		}	
		
		public static void closePstmt() {
			SDSSHandler.closePstmt();
	    }
}
		
class SDSSHandler implements ValueSubmittedListener { //Is called by the ActionHandler through the interface and handles the five different modules and DB connections

	public static Connection conn;
	private static PreparedStatement pstmt;
	//private static int errorCount=0;
	private static int counter=0;
	//private static long startTime,endTime;
	static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");

	//serverAddress = "localhost:1521:xe:1521";
	//username = "sdss";
	//password = "d23";
    
    
    public SDSSHandler() {}

    public static void establishConnection(String serverAddress, String username, String password) {
		//Establish connection
		try {
			Class.forName ("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			//e.printStackTrace();
		}
		try {

			//startTime = new Date().getTime();
			conn = DriverManager.getConnection("jdbc:oracle:thin:@"+serverAddress, username, password);
			//conn.close();
			conn.setAutoCommit(false);
			System.out.println("Connection established");
		
		} catch(Exception e) {
			System.err.println("Could not establish Connection");
			//e.printStackTrace();
		}
    }

    public static void closeConnection() {
		
		try {
			//endTime = new Date().getTime();
			//System.out.println(endTime-startTime + " milliseconds");
			conn.close();
			System.out.println("Connection closed");
		
		} catch(Exception e) {
			System.err.println("Could not close Connection");
		}
    }

    public static void closePstmt() {
		
		try {
			pstmt.close();
		} catch(Exception e) {
			System.err.println("Could not close prepared statement");
		}
    }

	@Override
	public void onSubmitted(int option, String serverAddress, String username, String password, String original, String geoAreaFile) { //Called by the ActionHandle with the necessary parameters to call the modules. 'option' defines the module
		System.out.println("submitted");
		establishConnection(serverAddress, username, password); //Also starts the connection
		switch (option) {
		case 1://Option one: user wants to convert from CSV
			convertSDSS(original, geoAreaFile);
			break;
		case 2://etc.
			startTransform(original, geoAreaFile);
			break;
		case 3:
			getGeolocation(original, geoAreaFile);
			break;
		default:
			break;
		}
	}

	@Override //Called from ActionHandler for generalization, with the necessary parameters, also starts the Connection
	public void onSubmitted(int option, String serverAddress, String username, String password, String original, String statement, String generalized) {
		System.out.println("submitted");

		establishConnection(serverAddress, username, password);
		generalize(original, statement, generalized);
	}

	@Override //Called from ActionHandler for User Session calculation
	public void onSubmitted(int option, String serverAddress, String username, String password, String original, String userSessionTable, float userSessionTime) {
		System.out.println("submitted");

		establishConnection(serverAddress, username, password);
		getUserSession(original, userSessionTable, userSessionTime);
	}

	//Convert from CSV file and store in database. Calls external class in transformation package
	public void convertSDSS(String originalTable, String file) {
		//System.out.println("submitted");
		try {
			Statement st = conn.createStatement();
			pstmt = conn.prepareStatement("insert into "+originalTable+" (\"YY\", \"MM\",\"DD\",\"HH\",\"MI\",\"SS\",\"SEQ\",\"THETIME\",\"LOGID\",\"CLIENTIP\",\"REQUESTOR\",\"SERVER\",\"DBNAME\",\"ACC\",\"ELAPSED\",\"BUSY\",\"NRROWS\",\"STATEMENT\",\"ERROR\",\"ERRORMESSAGE\") values (?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			try {
				st.executeQuery("SELECT * FROM "+originalTable);
			} catch (SQLSyntaxErrorException e) {
				st.executeQuery("CREATE TABLE "+originalTable+" (YY NUMBER(4, 0) NOT NULL, MM NUMBER(2, 0) NOT NULL, DD NUMBER(2, 0) NOT NULL, HH NUMBER(2, 0) NOT NULL, MI NUMBER(2, 0) NOT NULL, SS NUMBER(2, 0) NOT NULL, SEQ NUMBER(10, 0) NOT NULL, THETIME TIMESTAMP(0) NOT NULL, LOGID NUMBER, CLIENTIP VARCHAR2(20 BYTE), REQUESTOR VARCHAR2(40 BYTE), SERVER VARCHAR2(20 BYTE), DBNAME VARCHAR2(20 BYTE), ACC VARCHAR2(20 BYTE), ELAPSED NUMBER, BUSY NUMBER, NRROWS NUMBER, STATEMENT VARCHAR2(4000 BYTE), ERROR NUMBER(5, 0) NOT NULL, ERRORMESSAGE VARCHAR2(100 BYTE), CONSTRAINT "+originalTable+"_PK PRIMARY KEY (THETIME, SEQ) ENABLE)");
			}
			transformation.CSVparser.main(file);
		} catch (SQLException e) {
			System.err.println("SQL Exception");
			//e.printStackTrace();
		} catch (DataIOException e) {
			System.err.println("Problem with CSV File");
			//e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception");
			//e.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Interrupted Exception");
			//e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("No connection");
			//e.printStackTrace();
		}
	}

	//Starts access area extraction.If necessary, creates the access arrea table on the server and gets the tuples
	public void startTransform(String originalTable, String accessAreaTable) {
		AccessAreaExtraction extraction = new AccessAreaExtraction();
		// make sure autocommit is off
		//System.out.println("Interrupted Exception");
		try {
			Statement st = conn.createStatement();
			Statement st2 = conn.createStatement();
			
			// Turn use of the cursor on.
			st.setFetchSize(50);
			ResultSet rs = null;
			
			try { //check if table exists, if not create it
				rs = st.executeQuery("SELECT * FROM "+accessAreaTable);
			} catch (SQLSyntaxErrorException e) {
				rs = st.executeQuery("CREATE TABLE "+accessAreaTable+" (Access_Area_FROM VARCHAR2(4000), Access_Area_WHERE VARCHAR2(4000), THETIME TIMESTAMP(0) NOT NULL ,SEQ NUMBER(20, 0) NOT NULL, CONSTRAINT "+accessAreaTable+"_PK PRIMARY KEY (THETIME, SEQ) ENABLE)");
				rs = st.executeQuery("ALTER TABLE "+accessAreaTable+" ADD CONSTRAINT "+accessAreaTable+"_"+originalTable+"_FK1 FOREIGN KEY (THETIME, SEQ) REFERENCES "+originalTable+" (THETIME, SEQ) ENABLE");
			}
			
			System.out.println("Getting nr. of rows from DB");
			//Only get the nr of rows that need procession
			rs = st.executeQuery("SELECT count(*) as records FROM "+originalTable+" WHERE LOWER(statement) NOT LIKE '%create table%' AND LOWER(statement) NOT LIKE 'declare %' AND NOT EXISTS (SELECT * FROM "+accessAreaTable+" WHERE "+accessAreaTable+".seq = "+originalTable+".seq and "+accessAreaTable+".thetime = "+originalTable+".thetime)");
			//rs = st.executeQuery("SELECT count(*) as records FROM "+originalTable+" WHERE LOWER(statement) NOT LIKE '%create table%' AND LOWER(statement) NOT LIKE 'declare %' AND NOT EXISTS (SELECT * FROM "+accessAreaTable+" WHERE "+accessAreaTable+".seq = "+originalTable+".seq and "+accessAreaTable+".thetime = "+originalTable+".thetime) AND NOT EXISTS (SELECT * FROM unable WHERE unable.seq = "+originalTable+".seq)");
			rs.next();
			int records = rs.getInt("records");
			System.out.println("# rows: "+records);
			
			//Now get the actual tuples
			System.out.println("Getting rows from DB");
			rs = st.executeQuery("SELECT statement, seq, thetime, error FROM "+originalTable+" WHERE LOWER(statement) NOT LIKE '%create table%' AND LOWER(statement) NOT LIKE 'declare %' AND NOT EXISTS (SELECT * FROM "+accessAreaTable+" WHERE "+accessAreaTable+".seq = "+originalTable+".seq and "+accessAreaTable+".thetime = "+originalTable+".thetime)");
			//rs = st.executeQuery("SELECT statement, seq, thetime, error FROM "+originalTable+" WHERE LOWER(statement) NOT LIKE '%create table%' AND LOWER(statement) NOT LIKE 'declare %' AND NOT EXISTS (SELECT * FROM "+accessAreaTable+" WHERE "+accessAreaTable+".seq = "+originalTable+".seq and "+accessAreaTable+".thetime = "+originalTable+".thetime) AND seq = '1977064124'");
			//rs = st.executeQuery("SELECT statement, seq, thetime, error FROM "+originalTable+" WHERE LOWER(statement) NOT LIKE '%create table%' AND LOWER(statement) NOT LIKE 'declare %' AND NOT EXISTS (SELECT * FROM "+accessAreaTable+" WHERE "+accessAreaTable+".seq = "+originalTable+".seq and "+accessAreaTable+".thetime = "+originalTable+".thetime) AND NOT EXISTS (SELECT * FROM unable WHERE unable.seq = "+originalTable+".seq)");
			AccessArea accessArea;
			PreparedStatement pst = conn.prepareStatement("INSERT INTO "+accessAreaTable+" (Access_Area_FROM, Access_Area_WHERE, SEQ, THETIME) values (?,?,?,?)");
			//rs.next();
			int current = 0;
			//startTime = new Date().getTime();
			
			//For each tuple, extract access area and store in db via prepared statement. Commit after each 10000 rows.
			while (rs.next()) {
				//long lStartTimeTotal = new Date().getTime();
				//if(rs.getInt("error") == 0) { //Use this if you want to exclude erroneous queries

					if(++current%10000==0) {
						//pst.executeBatch(); //does not speed it up much
						conn.commit();
						System.out.println(current + " out of "+records+" rows processed");
					}
					try {
						accessArea = extraction.extractAccessArea(rs.getString("statement"));
					} catch (Throwable t) {
						Expression expression = new NullValue();
						accessArea = new AccessArea(new ArrayList<FromItem>(), expression);
						//System.out.println("Could not parse sql statement:");
						//System.out.println(rs.getString("statement"));
						//t.printStackTrace();
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
				if(!from.equals("") || !where.equals("")) {
					pst.setString(1, from);
					pst.setString(2, where);
					pst.setString(3, rs.getString("seq"));
					pst.setTimestamp(4, rs.getTimestamp("thetime"));
					try{
						//long lStartTime = new Date().getTime();
						
						pst.executeUpdate();
						//pst.addBatch(); //no real speed up
						
						//long lEndTime = new Date().getTime();
					 
						//long difference = lEndTime - lStartTime;
					 
						//System.out.println("Elapsed milliseconds: \t" + difference);
						
						//long lEndTimeTotal = new Date().getTime();
					 
						//long differenceTotal = lEndTimeTotal - lStartTimeTotal;
					 
						//System.out.println("Total milliseconds: \t" + differenceTotal);
					}
					catch (Exception e) {
						//System.out.println("Could not store Access Area in DB");
						//e.printStackTrace();
					}
				}				
			}
			conn.commit();
			System.out.println(current + " out of "+records+" rows processed");
			rs.close();
			pst.close();
			st.close();
			st2.close();
		} catch (Throwable t) {
			System.err.println("Exception, could not execute query on database");
			//t.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	//Calls the geolocator, get location details for each IP in DB
	public void getGeolocation(String originalTable, String geolocationTable) {
		Geolocator geolocator = new Geolocator();
		
		try {
			Statement st = conn.createStatement();
			
			// Turn use of the cursor on.
			st.setFetchSize(50);
			ResultSet rs = null;
			
			//If relation not exists, create it
			try {
				rs = st.executeQuery("SELECT * FROM "+geolocationTable);
			} catch (SQLSyntaxErrorException e) {
				rs = st.executeQuery("CREATE TABLE "+geolocationTable+" (IP VARCHAR2(20), CountryCode VARCHAR2(10), CountryName VARCHAR(100), Region VARCHAR2(100), RegionName VARCHAR2(100), City VARCHAR2(100), POSTALCODE VARCHAR2(20), Latitude NUMBER(20,10), Longitude NUMBER(20,10), MetroCode NUMBER(20), AreaCode NUMBER(20), Timezone VARCHAR(100), CONSTRAINT "+geolocationTable+"_PK PRIMARY KEY (IP) ENABLE)");
			}
			
			//Get number of distinct IPs to process
			rs = st.executeQuery("SELECT count(distinct clientip) as records FROM "+originalTable+" WHERE NOT EXISTS (SELECT * FROM "+geolocationTable+" WHERE "+geolocationTable+".ip = "+originalTable+".clientip)");
			rs.next();
			int records = rs.getInt("records");
			System.out.println("Distinct IPs: "+records);
			
			//Get distinct IPs
			rs = st.executeQuery("SELECT distinct clientip FROM "+originalTable+" WHERE NOT EXISTS (SELECT * FROM "+geolocationTable+" WHERE "+geolocationTable+".ip = "+originalTable+".clientip)");
			PreparedStatement pst = null;
			pst = conn.prepareStatement("INSERT INTO "+geolocationTable+" (IP, CountryCode, CountryName, Region, RegionName, City, Postalcode, Latitude, Longitude, Metrocode, AreaCode, Timezone) values (?,?,?,?,?,?,?,?,?,?,?,?)");

			int current = 0;
			//For each IP get details, then store in DB via prepared statement
			while (rs.next())
			{
					if(++current%500==0) System.out.println(current + " out of "+records+" IPs processed");
					try {
						Location location = geolocator.locateIP(rs.getString("clientip"));
						if(location != null) {
							pst.setString(1, rs.getString("clientip"));
							//System.out.println(location.countryCode);
							pst.setString(2, location.countryCode);
							pst.setString(3, location.countryName);
							pst.setString(4, location.region);
							pst.setString(5, regionName.regionNameByCode(location.countryCode, location.region));
							pst.setString(6, location.city);
							pst.setString(7, location.postalCode);
							pst.setFloat(8, location.latitude);
							pst.setFloat(9, location.longitude);
							pst.setInt(10, location.metro_code);
							pst.setInt(11, location.area_code);
							pst.setString(12, timeZone.timeZoneByCountryAndRegion(location.countryCode, location.region));
							pst.executeUpdate();
						} else System.err.println("IP not known in geolocation database: "+rs.getString("clientip"));
					} catch (Exception e) {
						System.err.println("IP not known in geolocation database: "+rs.getString("clientip"));
					} finally{
						
					}
				
			}
			System.out.println(current + " out of "+records+" IPs processed");
			rs.close();
			
			//Foreign Key relation could be added but we do not add IPs to tables we do not know
			//st.executeQuery("ALTER TABLE "+originalTable+" ADD CONSTRAINT "+originalTable+"_"+geolocationTable+"_FK1 FOREIGN KEY (clientip) REFERENCES "+geolocationTable+" (IP) ENABLE");
			
	
			// Close the statement.
			st.close();
			pst.close();
		} catch (NullPointerException | SQLException e) {
			System.err.println("Exception, could not execute query on database");
			//e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	
	//User Session Calculation. Fill procedure with parameters from Interface, then push to Server and execute
	public void getUserSession(String originalTable, String userSessionTable, float userSessionTime) {
				
		try {
			Statement st = conn.createStatement();
					//If table not exists, create
			try {
				st.executeQuery("SELECT * FROM "+userSessionTable);
			} catch (SQLSyntaxErrorException e) {
				st.executeQuery("CREATE TABLE "+userSessionTable+" (UserSession NUMBER(20,0), THETIME TIMESTAMP(0) NOT NULL ,SEQ NUMBER(20, 0) NOT NULL, CONSTRAINT "+userSessionTable+"_PK PRIMARY KEY (THETIME, SEQ) ENABLE)");
				st.executeQuery("ALTER TABLE "+userSessionTable+" ADD CONSTRAINT "+userSessionTable+"_"+originalTable+"_FK1 FOREIGN KEY (THETIME, SEQ) REFERENCES "+originalTable+" (THETIME, SEQ) ENABLE");
			}
			
			try {
				st.executeQuery("create or replace procedure user_sessions as\n"+
								  "  ip varchar(200) := 'init'; \n"+
								  "  session_id number := 0; \n"+
								  "  currentTime timestamp; \n"+
								  "  lastTime timestamp; \n"+
								
								  "BEGIN \n"+
								  //for each tuple, from the list ordered by IP and time...
								  "FOR tuples IN (SELECT clientip,thetime,seq,yy,mm,dd,hh,mi,ss FROM "+originalTable+" where NOT EXISTS (SELECT * FROM "+userSessionTable+" WHERE "+userSessionTable+".seq = "+originalTable+".seq and "+userSessionTable+".thetime = "+originalTable+".thetime) and clientip not like '%undefined%' order by clientip,yy,mm,dd,hh,mi,ss asc) \n"+
								  "LOOP \n"+
								  	//Get current time from yy,hh,mm,etc. since theTime is not unambiguous
								    "currentTime := to_timestamp(tuples.yy||'-'||tuples.mm||'-'||tuples.dd||' '||tuples.hh||':'||tuples.mi||':'||tuples.ss,'YYYY-MM-DD HH24:MI:SS'); \n"+
								    "if ( tuples.clientip != ip ) then \n"+ //If IP not the same as before then it's a new session
								      "ip := tuples.clientip;\n"+
								      "session_id := session_id + 1; \n"+
								      //else means IP is the same. So if less than 'think-time' minutes have elapse its still the same session, else new session
								    "elsif (currentTime - lastTime > NUMTODSINTERVAL("+userSessionTime+", 'MINUTE')) then \n"+
								            "session_id := session_id + 1; \n"+
								    "end if; \n"+
								      
								    "lastTime := currentTime; \n"+
								      
								    "insert into "+userSessionTable+" (usersession,seq,thetime) \n"+
								    "values (session_id,tuples.seq,tuples.thetime); \n"+
								  "END LOOP; \n"+
								  "commit; \n"+
								"END;");	
				System.out.println("Procedure stored in DB");
			} catch (Exception e) {
				System.err.println("Could not store procedure");
			}
			try {
				System.out.println("Start calculating User Sessions");
				CallableStatement callableStatement = conn.prepareCall("begin USER_SESSIONS(); end;");
				callableStatement.execute();
				callableStatement.close();
				System.out.println("Calculation done");
			} catch (Exception e) {
				System.err.println("Could not call procedure");
				//e.printStackTrace();
			}
			
			
			// Close the statement.
			st.close();
		} catch (NullPointerException | SQLException e) {
			System.err.println("Exception, could not execute query on database");
			//e.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	//Generalization. Store the necessary procedures and functions on the Server, execute them
	public void generalize(String originalTable, String statementTable, String generalizedTable) {
		try {
			Statement st = conn.createStatement();
					//If table not exists, create
			try {
				st.executeQuery("SELECT * FROM "+generalizedTable);
			} catch (SQLSyntaxErrorException e) {
				st.executeQuery("CREATE TABLE "+generalizedTable+" (SELECT_PART VARCHAR2(4000), FROM_PART VARCHAR2(4000), WHERE_PART VARCHAR2(4000), GROUPBY_PART VARCHAR2(4000), HAVING_PART VARCHAR2(4000), ORDERBY_PART VARCHAR2(4000), THETIME TIMESTAMP(0) NOT NULL ,SEQ NUMBER(20, 0) NOT NULL, CONSTRAINT "+generalizedTable+"_PK PRIMARY KEY (THETIME, SEQ) ENABLE)");
				st.executeQuery("ALTER TABLE "+generalizedTable+" ADD CONSTRAINT "+generalizedTable+"_"+originalTable+"_FK1 FOREIGN KEY (THETIME, SEQ) REFERENCES "+originalTable+" (THETIME, SEQ) ENABLE");
			}

			//If table not exists, create
			try {
				st.executeQuery("SELECT * FROM "+statementTable);
				//st.executeQuery("truncate table "+statementTable);
			} catch (SQLSyntaxErrorException e) {
				st.executeQuery("CREATE TABLE "+statementTable+" (SELECT_PART VARCHAR2(4000), FROM_PART VARCHAR2(4000), WHERE_PART VARCHAR2(4000), GROUPBY_PART VARCHAR2(4000), HAVING_PART VARCHAR2(4000), ORDERBY_PART VARCHAR2(4000), THETIME TIMESTAMP(0) NOT NULL ,SEQ NUMBER(20, 0) NOT NULL, CONSTRAINT "+statementTable+"_PK PRIMARY KEY (THETIME, SEQ) ENABLE)");
				st.executeQuery("ALTER TABLE "+statementTable+" ADD CONSTRAINT "+statementTable+"_"+originalTable+"_FK1 FOREIGN KEY (THETIME, SEQ) REFERENCES "+originalTable+" (THETIME, SEQ) ENABLE");
			}
			
			try {
				//This splits the SQL statement into ints outer clauses (SELECT, FROM, WHERE etc.)
				st.executeQuery("create or replace procedure split_statement as\n"+

								  "v_statement varchar2(4000) :='';\n"+
								  "v_select_part varchar2(4000) :='';\n"+
								  "v_from_part varchar2(4000) :='';\n"+
								  "v_where_part varchar2(4000) :='';\n"+
								  "v_groupby_part varchar2(4000) :='';\n"+
								  "v_having_part varchar2(4000) :='';\n"+
								  "v_orderby_part varchar2(4000) :='';\n"+
								  "position_select pls_integer :=-1;\n"+
								  "position_from pls_integer :=-1;\n"+
								  "position_where pls_integer :=-1;\n"+
								  "position_groupby pls_integer :=-1;\n"+
								  "position_having pls_integer :=-1;\n"+
								  "position_orderby pls_integer :=-1;\n"+
								  "length_select pls_integer :=-1;\n"+
								  "length_from pls_integer :=-1;\n"+
								  "length_where pls_integer :=-1;\n"+
								  "length_groupby pls_integer :=-1;\n"+
								  "length_having pls_integer :=-1;\n"+
								  "length_orderby pls_integer :=-1;\n"+
								  "type int_array is varray(5) of pls_integer;\n"+
								  "array_positions int_array := int_array(-1,-1,-1,-1,-1);\n"+
								  "position_nextStatement pls_integer;\n"+
								  
								  //Get position of the next outer clause (FROM,WHERE etc.) Clauses always have a certain order.
								  "function getNextStatementPos (startStatement in pls_integer, nextStatement in int_array, minPosition in out pls_integer)\n"+
								    "return pls_integer is\n"+
								    "j pls_integer :=1;\n"+
								    "begin\n"+
								      "if startStatement < 0 then return -1; end if;\n"+
								      "while j <=5 loop\n"+
								        "if nextStatement(j) != 0 and nextStatement(j) < minPosition and nextStatement(j) > startStatement then minPosition := nextStatement(j); end if;\n"+
								        "j := j+1;\n"+
								      "end loop;\n"+
								      "return minPosition;\n"+
								  "end;\n"+
								    
								  //Get positions of each clause. watch for ()
								  "function getPosition (statement in varchar2, sqlElement in varchar2)\n"+
								   "return pls_integer is\n"+
								    "j pls_integer :=1;\n"+
								    "position_element pls_integer := -1;\n"+
								    "partleng pls_integer :=0;\n"+
								    "begin\n"+
								    "while position_element = -1 loop\n"+ //search for the outermost clause of the current type
								      "exit when INSTR(statement,sqlElement,1,j) = 0;\n"+
								      "partleng := length(REGEXP_REPLACE(substr(statement,1,INSTR(statement,sqlElement,1,j)),'[^(]'))-length(REGEXP_REPLACE(substr(statement,1,INSTR(statement,sqlElement,1,j)),'[^)]'));\n"+
								      "if partleng <=0 or partleng is null then\n"+
								        "position_element := INSTR(statement,sqlElement,1,j);\n"+
								      "else j := j + 1;\n"+
								      "end if;\n"+
								    "end loop;\n"+
								    "return position_element;\n"+
								  "end;\n"+
								    
								"BEGIN\n"+ //Call the functions above for each tuple in referenced relation
								  "FOR tuples IN (SELECT SEQ,THETIME,STATEMENT FROM "+originalTable+" WHERE error=0 and lower(statement) like '%select%')\n"+
								  "LOOP          \n"+
								    "v_statement := REPLACE(LOWER(tuples.statement),'[br]',' ');\n"+
								    "position_select := INSTR(v_statement,'select');\n"+ //outer select is always the first select until the first from
								
								    "position_from := getPosition(v_statement, 'from');\n"+ //get position of outer from clause
								    "position_where := getPosition(v_statement, 'where');\n"+ //etc.
								    "position_groupby := getPosition(v_statement, 'group by');\n"+
								    "position_having := getPosition(v_statement, 'having');\n"+
								    "position_orderby := getPosition(v_statement, 'order by');\n"+
								    
								    "array_positions(1):=position_from;\n"+
								    "array_positions(2):=position_where;\n"+
								    "array_positions(3):=position_groupby;\n"+
								    "array_positions(4):=position_having;\n"+
								    "array_positions(5):=position_orderby;\n"+
								    
								    "position_nextStatement := length(v_statement)+1;\n"+ //now cut the statement into pieces
								    "length_select := getNextStatementPos(position_select,array_positions,position_nextStatement)-position_select-6;\n"+
								    
								    "position_nextStatement := length(v_statement)+1;\n"+
								    "length_from := getNextStatementPos(position_from,array_positions,position_nextStatement)-position_from-4;\n"+
								    
								    "position_nextStatement := length(v_statement)+1;\n"+
								    "length_where := getNextStatementPos(position_where,array_positions,position_nextStatement)-position_where-5;\n"+
								    
								    "position_nextStatement := length(v_statement)+1;\n"+
								    "length_groupby := getNextStatementPos(position_groupby,array_positions,position_nextStatement)-position_groupby-8;\n"+
								    
								    "position_nextStatement := length(v_statement)+1;\n"+
								    "length_having := getNextStatementPos(position_having,array_positions,position_nextStatement)-position_having-6;\n"+
								    
								    "position_nextStatement := length(v_statement)+1;\n"+
								    "length_orderby := getNextStatementPos(position_orderby,array_positions,position_nextStatement)-position_orderby-8;\n"+
								        
								    "v_select_part := LTRIM(SUBSTR(v_statement,position_select+6,length_select));\n"+ //delete any additional space characters
								    "v_from_part := LTRIM(SUBSTR(v_statement,position_from+4,length_from));\n"+
								    "v_where_part := LTRIM(SUBSTR(v_statement,position_where+5,length_where));\n"+
								    "v_groupby_part := LTRIM(SUBSTR(v_statement,position_groupby+8,length_groupby));\n"+
								    "v_having_part := LTRIM(SUBSTR(v_statement,position_having+6,length_having));\n"+
								    "v_orderby_part := LTRIM(SUBSTR(v_statement,position_orderby+8,length_orderby));\n"+
								    
								    "insert into "+statementTable+" (SEQ, THETIME, select_part, from_part, where_part, groupby_part, having_part, orderby_part)\n"+
								    "values (tuples.SEQ, tuples.THETIME, v_select_part, v_from_part, v_where_part, v_groupby_part, v_having_part, v_orderby_part);   \n"+ 
								  "END LOOP;\n"+
								"END;");	
			} catch (Exception e) {
				System.err.println("Could not store function");
			}
			
			
			try {
				//This function works does some magic to strings ;) actually it just helps sorting the relations alphabetically, they are in one string
				st.executeQuery("CREATE OR REPLACE PACKAGE STRING_FNC\n"+
				"IS \n"+
				"\n"+
				"TYPE t_array IS TABLE OF VARCHAR2(4000) INDEX BY BINARY_INTEGER; \n"+
				"\n"+
				"FUNCTION SPLIT (p_in_string VARCHAR2, p_delim VARCHAR2) RETURN t_array; \n"+
				"FUNCTION SORT_COLLECTION_PLSQL (p_collection IN t_array) RETURN t_array; \n"+
				"END;");
				st.executeQuery("CREATE OR REPLACE PACKAGE BODY STRING_FNC \n"+
				"IS \n"+
				"\n"+
				"   FUNCTION SPLIT (p_in_string VARCHAR2, p_delim VARCHAR2) RETURN t_array  \n"+ //Split the relations in the FROM clause
				"   IS \n"+
				"   \n"+
				"      i       number :=0; \n"+
				"      pos     number :=0; \n"+
				"      lv_str  varchar2(4000) := p_in_string; \n"+
				"      \n"+
				"   strings t_array; \n"+
				"   \n"+
				"   BEGIN \n"+
				"   \n"+
				"      -- determine first chuck of string   \n"+
				"      pos := instr(lv_str,p_delim,1,1); \n"+
				"      \n"+
				"      -- while there are chunks left, loop  \n"+
				"      WHILE ( pos != 0) LOOP \n"+
				"      \n"+
				"         -- increment counter  \n"+
				"         i := i + 1; \n"+
				"         \n"+
				"         -- create array element for chuck of string  \n"+
				"         strings(i) := substr(lv_str,1,pos); \n"+
				"         \n"+
				"         -- remove chunk from string  \n"+
				"         lv_str := substr(lv_str,pos+1,length(lv_str)); \n"+
				"         \n"+
				"         -- determine next chunk  \n"+
				"         pos := instr(lv_str,p_delim,1,1); \n"+
				"         \n"+
				"         -- no last chunk, add to array  \n"+
				"         IF pos = 0 THEN \n"+
				"        		 \n"+
				"            strings(i+1) := lv_str; \n"+
				"            \n"+
				"         END IF; \n"+
				"         \n"+
				"      END LOOP; \n"+
				"      \n"+
				"      -- return array  \n"+
				"      RETURN strings; \n"+
				"      \n"+
				"   END SPLIT; \n"+
				"   FUNCTION sort_collection_plsql (p_collection IN t_array)\n"+ //Sort the relations in the from clause
				"                  RETURN t_array IS\n"+
				"                  \n"+
				"       TYPE sorter_aat IS TABLE OF PLS_INTEGER\n"+
				"          INDEX BY VARCHAR2(4000);\n"+
				"          \n"+
				"       v_collection t_array;\n"+
				"       v_sorter     sorter_aat;\n"+
				"      v_sorter_idx VARCHAR2(4000);\n"+
				"      \n"+
				"   BEGIN\n"+
				"   \n"+
				"      /* Sort the collection using the sorter array... */\n"+
				"      FOR i IN 1 .. p_collection.COUNT LOOP\n"+
				"         v_sorter_idx := p_collection(i);\n"+
				"         v_sorter(v_sorter_idx) := CASE\n"+
				"                                      WHEN v_sorter.EXISTS(v_sorter_idx)\n"+
				"                                      THEN v_sorter(v_sorter_idx) + 1\n"+
				"                                      ELSE 1\n"+
				"                                   END;\n"+
				"      END LOOP;\n"+
				"      \n"+
				"      /* Assign sorted elements back to collection... */\n"+
				"      v_sorter_idx := v_sorter.FIRST;\n"+
				"      WHILE v_sorter_idx IS NOT NULL LOOP\n"+
				"      \n"+
				"         /* Handle multiple copies of same value... */\n"+
				"         FOR i IN 1 .. v_sorter(v_sorter_idx) LOOP\n"+
				"            v_collection(i) := v_sorter(v_sorter_idx);\n"+
				"            v_collection(v_collection.LAST) := v_sorter_idx;\n"+
				"         END LOOP;\n"+
				"         \n"+
				"         v_sorter_idx := v_sorter.NEXT(v_sorter_idx);\n"+
				"         \n"+
				"      END LOOP;\n"+
				"      \n"+
				"      RETURN v_collection;\n"+
				"      \n"+
				"   END sort_collection_plsql;\n"+
				"END;");
			} catch (Exception e) {
				System.err.println("Could not store string function");
				//e.printStackTrace();
			}
			
			
			try {
				//Actual regular expressions
				st.executeQuery("create or replace procedure generalize as\n"+ 
								  "\n"+								
								  "v_select_part varchar2(4000) :='';\n"+
								  "v_from_part varchar2(4000) :='';\n"+
								  "v_where_part varchar2(4000) :='';\n"+
								  "v_groupby_part varchar2(4000) :='';\n"+
								  "v_having_part varchar2(4000) :='';\n"+
								  "v_orderby_part varchar2(4000) :='';\n"+
								  "v_counter number(4) := 0;\n"+
								  "\n"+								  
								  "function myRegEx (part in out varchar2, type in varchar2)\n"+
								  "  return varchar2 is\n"+
								  "  value_error EXCEPTION;\n"+
								  "  PRAGMA EXCEPTION_INIT(value_error , -6502);\n"+
								  "  \n"+								    
								  "  begin\n"+
								  "  \n"+								      
								  "    part := REGEXP_REPLACE(part,'(( )?select )',' SELECT '); --make SQL keywords UPPERCASE (for subqueries)\n"+
								  "    part := REGEXP_REPLACE(part,'( from )',' FROM '); --make SQL keywords UPPERCASE (for subqueries)\n"+
								  "    part := REGEXP_REPLACE(part,'( where )',' WHERE '); --make SQL keywords UPPERCASE (for subqueries)\n"+
								  "    part := REGEXP_REPLACE(part,'( group by )',' GROUP BY '); --make SQL keywords UPPERCASE (for subqueries)\n"+
								  "    part := REGEXP_REPLACE(part,'( having )',' HAVING '); --make SQL keywords UPPERCASE (for subqueries)\n"+
								  "    part := REGEXP_REPLACE(part,'( order by )',' ORDER BY'); --make SQL keywords UPPERCASE (for subqueries)\n"+
								  "    part := REGEXP_REPLACE(part,'( on )',' ON '); --Replace on with ON\n"+
								  "    part := REGEXP_REPLACE(part,'(([[''\"]?)([a-z0-9]*)([a-z]+)([a-z0-9]*)([]''\"]?)\\.+)'); --Remove Classes, Names etc.\n"+
								  "    part := REGEXP_REPLACE(part,'( as [[''\"]?\\w+[]''\"]?)'); --Remove naming 'as ...'\n"+
								  "    part := REGEXP_REPLACE(part,'((top \\d+)|(top ))'); --Remove 'top' select clause\n"+
								  "    part := REGEXP_REPLACE(part,'(0x[[:xdigit:]]+)','HEXNUM'); --Replace Hexadecimal Numbers\n"+
								  "    part := REGEXP_REPLACE(part,'((\\d([,.x:]\\d)*)+)','NUM'); --Replace Numbers\n"+
								  "    part := REGEXP_REPLACE(part,'(#(\\w)+( (\\w)+)?)','temp'); --Replace Temporary Tables incl. naming\n"+
								  "    part := REGEXP_REPLACE(part,'([''\"]+\\w*[''\"]+)','STRING'); --Replace Strings\n"+
								  "    part := REGEXP_REPLACE(part,'( ((left|right|inner|outer|full)( )?)*join )',' JOIN '); --Replace Joins\n"+
								  "    part := REGEXP_REPLACE(part,'(( outer apply )|( cross apply ))',' APPLY '); --Replace Apply\n"+
								  "    part := REGEXP_REPLACE(part,'( distinct )'); --Remove distinct\n"+
								  "    part := REGEXP_REPLACE(part,'( ){2,}',' '); --Remove multiple Spaces\n"+
								  "    part := REGEXP_REPLACE(part,'(( and )|( or )|( xor )|( nor )|( nand )|( xand )|( xnor )|( xnand )|( is not )|( is )|( not in)|( not )|( exists )|( all )|( any )|( in )|(&)|(\\|)|(#)|(~))',' LOGIC '); --Replace and or nor nand is is not not in in\n"+
								  "    part := REGEXP_REPLACE(part,'((=<)|(<=)|(>=)|(=>)|(!=)|(<>)|(%)|(\\+)|(-)|(\\*)|(\\^)|(\\|/)|(\\|\\|/)|(!!)|(!)|(@)|(<<)|(>>)|(=)|(/)|(<)|(>))',' MATH '); --Replace =<>!= + - * / %\n"+
								  "    part := REGEXP_REPLACE(part,'( ){2,}',' '); --Remove multiple Spaces\n"+
								  "    if type = 'from' then\n"+
								  "      part := REGEXP_REPLACE(part,' MATH ',' '); --Remove MATH because of naming\n"+
								  "      part := REGEXP_REPLACE(part,'(\\)) ([[''\"]?[^[:upper:], ]+[]''\"]?)((,)|( JOIN )|( APPLY )|( ON )|( )?$)','\\1\\3'); --Remove naming '(subquery)_ ...'\n"+
								  "      part := REGEXP_REPLACE(part,'([[''\"]?[^[:upper:], ]+[]''\"]?) ([[''\"]?[^[:upper:], ]+[]''\"]?)((,)|( JOIN )|( APPLY )|( ON )|$)','\\1\\3'); --Remove naming '_ ...'\n"+
								  "      part := REGEXP_REPLACE(part,'(\\(( )?SELECT .+ FROM )(.+)([[:upper:]]+[. ]*)?(\\))','\\3'); --Remove subqueries in from part, keep only 'sub'-from parts here\n"+
								  "      part := REGEXP_REPLACE(part,'(\\w+)(\\(.*\\))','\\1'); --Remove function parameters\n"+
								  "      part := REGEXP_REPLACE(part,'\\(( )?SELECT.*\\)'); --Remove subqueries without from\n"+
								  "      part := REGEXP_REPLACE(part,'([^[:upper:], ]+).*?( )?((,)|( JOIN )|( )?$)','\\1 '); --Only keep table names\n"+
								  "      part := REGEXP_REPLACE(part,'temp'); --Remove temporary tables\n"+
								  "    end if;\n"+
								  "    part := REGEXP_REPLACE(part,'([[:punct:][^_]])',' '); --Remove all punctuation\n"+
								  "    part := REGEXP_REPLACE(part,'( ){2,}',' '); --Remove multiple Spaces, again\n"+
								  "    \n"+								      
								  "    return LTRIM(RTRIM(part));\n"+
								  "    EXCEPTION\n"+
								  "      WHEN value_error THEN\n"+
								  "        part := 'A value_error occured here.';\n"+
								  "        return LTRIM(RTRIM(part));\n"+
								  "      WHEN OTHERS THEN\n"+
								  "        part := 'A random error occured here.';\n"+
								  "        return LTRIM(RTRIM(part));\n"+
								  "        \n"+								
								  "end;\n"+
								  "\n"+								  
								  //This sorts the relations in the FROM clause, uses above functions
								  "function myRegExSort (part in out varchar2)\n"+
								  "  return varchar2 is\n"+
								  "  		\n"+								    
								  "  array_part string_fnc.t_array;\n"+
								  "\n"+								    
								  "  begin\n"+
								  "  \n"+								      
								  "    if part is not null then\n"+
								  "      part := part || ' ';\n"+
								  "      part := REGEXP_REPLACE(part,'( ){2,}',' '); --Remove multiple Spaces\n"+
								  "      array_part := string_fnc.split(part,' ');\n"+
								  "      array_part := string_fnc.SORT_COLLECTION_PLSQL(array_part);\n"+
								  "      part := LTRIM(RTRIM(array_part(1)));\n"+
								  "  \n"+								            
								  "      for i in 2..array_part.count loop\n"+
								  "        part := part || ' ' || LTRIM(RTRIM(array_part(i)));\n"+
								  "      end loop;\n"+
								  "    end if;\n"+
								  "    \n"+								      
								  "    return part;\n"+
								  "end;\n"+
								  "\n"+								
								  "BEGIN\n"+ //apply regular expressions to all SQL queries in referenced relation.
								  " FOR tuples IN (SELECT * FROM "+statementTable+" order by thetime asc)\n"+
									"  LOOP\n"+
									"    v_select_part := myRegEx(tuples.select_part,'select');\n"+
									"    v_from_part := myRegEx(tuples.from_part,'from');\n"+
									"    v_from_part := myRegExSort(v_from_part);\n"+
									"    v_where_part := myRegEx(tuples.where_part,'where');\n"+
									"    v_groupby_part := myRegEx(tuples.groupby_part,'group');\n"+
									"    v_having_part := myRegEx(tuples.having_part,'having');\n"+
									"    v_orderby_part := myRegEx(tuples.orderby_part,'order');\n"+
									"    if v_select_part is null then\n"+
									"      v_select_part :='MATH';\n"+
									"    end if;\n"+
									"    insert into "+generalizedTable+" (SEQ, THETIME, select_part, from_part, where_part, groupby_part, having_part, orderby_part)\n"+
									"    values (tuples.SEQ, tuples.THETIME, v_select_part, v_from_part, v_where_part, v_groupby_part, v_having_part, v_orderby_part);\n"+    
									"     v_counter := v_counter + 1;\n"+
									"     if MOD(v_counter, 1000) = 0 then\n"+
									"        v_counter := 0;\n"+
									"        commit;\n"+
									"     end if;\n"+
									"  END LOOP;\n"+
									"END;");	
			} catch (Exception e) {
				System.err.println("Could not store procedure");
			}
			try {
				System.out.println("Functions and procedures successfully stored.\nGoing to split the SQL statements.");
				CallableStatement callableStatement = conn.prepareCall("begin SPLIT_STATEMENT(); end;");
				callableStatement.execute();
				callableStatement.close();
			} catch (Exception e) {
				System.err.println("Could not call procedure");
				//e.printStackTrace();
			}
			try {
				System.out.println("Statements split.\nGoing to generalize.");
				CallableStatement callableStatement = conn.prepareCall("begin GENERALIZE(); end;");
				callableStatement.execute();
				callableStatement.close();
				System.out.println("Successfully generalized.");
			} catch (Exception e) {
				System.err.println("Could not call procedure");
				//e.printStackTrace();
			}
			
			
			// Close the statement.
			st.close();
		} catch (NullPointerException | SQLException e) {
			System.err.println("Exception, could not execute query on database");
		} finally {
			closeConnection();
		}
	}
	
	//This stores the tuples delivered by the CSVConverter in the database via the provided connection in the current class (SDSSHanldeR)
	public static void saveTuple(Tuple tuple) throws SQLException {
		++counter;
		if(((counter)%100000) == 0) System.out.println("Saved "+(counter)+" tuples.");
		try{

//			System.out.println(tuple.getString("yy"));
//			System.out.println(tuple.getString("mm"));
//			System.out.println(tuple.getString("dd"));
//			System.out.println(tuple.getString("hh"));
//			System.out.println(tuple.getString("mi"));
//			System.out.println(tuple.getString("ss"));
//			System.out.println(tuple.getString("seq"));
//			System.out.println(tuple.getString("theTime"));
//			System.out.println(tuple.getString("logID"));
//			System.out.println(tuple.getString("clientIP"));
//			System.out.println(tuple.getString("requestor"));
//			System.out.println(tuple.getString("server"));
//			System.out.println(tuple.getString("dbname"));
//			System.out.println(tuple.getString("access"));
//			System.out.println(tuple.getString("elapsed"));
//			System.out.println(tuple.getString("busy"));
//			System.out.println(tuple.getString("rows"));
//			System.out.println(tuple.getString("statement"));
//			System.out.println(tuple.getString("error"));
//			System.out.println(tuple.getString("errorMessage"));
		
		pstmt.setInt(1, Integer.parseInt(tuple.getString("yy")));
		pstmt.setInt(2, Integer.parseInt(tuple.getString("mm")));
		pstmt.setInt(3, Integer.parseInt(tuple.getString("dd")));
		pstmt.setInt(4, Integer.parseInt(tuple.getString("hh")));
		pstmt.setInt(5, Integer.parseInt(tuple.getString("mi")));
		pstmt.setInt(6, Integer.parseInt(tuple.getString("ss")));
		pstmt.setInt(7, Integer.parseInt(tuple.getString("seq")));
		//THETIME:
		Timestamp date1 = StringToSQLDate(tuple.getString("theTime"));
		pstmt.setTimestamp(8, date1);  
		pstmt.setInt(9, Integer.parseInt(tuple.getString("logID")));    
		pstmt.setString(10, tuple.getString("clientIP"));
		pstmt.setString(11, tuple.getString("requestor"));
		pstmt.setString(12, tuple.getString("server"));
		pstmt.setString(13, tuple.getString("dbname"));
		pstmt.setString(14, tuple.getString("access"));
		pstmt.setDouble(15, Double.parseDouble(tuple.getString("elapsed")));
		pstmt.setDouble(16, Double.parseDouble(tuple.getString("busy")));    
		pstmt.setInt(17, Integer.parseInt(tuple.getString("rows")));  
		pstmt.setString(18, tuple.getString("statement"));  
		pstmt.setInt(19, Integer.parseInt(tuple.getString("error"))); 
		pstmt.setString(20, tuple.getString("errorMessage"));  
		

		//System.out.println("1");
        pstmt.executeUpdate();
		//System.out.println("2");
		}catch(Exception e){
			//e.printStackTrace();
			//System.out.println("3");
			//System.out.println(tuple.getString("seq"));
			//System.out.println(++errorCount);
			//if(errorCount>1)conn.close();
		}
		finally{
			//pstmt.close();
			//System.out.println("4");
		}

	}
	private static Timestamp StringToSQLDate(String s) {
        Timestamp sqlDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
            sqlDate = new java.sql.Timestamp(sdf.parse(s).getTime());
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return sqlDate;
    }
}

//Interface for the calls the ActionHandler uses
interface ValueSubmittedListener {
    public void onSubmitted(int option, String serverAddress, String username, String password, String original, String statement, String generalized);
    public void onSubmitted(int option, String serverAddress, String username, String password, String original, String geoAreaFile);
    public void onSubmitted(int option, String serverAddress, String username, String password, String original, String userSessionTable, float userSessionTime);
}

@SuppressWarnings("serial") //The User Interface
class UI extends JFrame implements ActionListener{
	
	private List<ValueSubmittedListener> listeners = new ArrayList<ValueSubmittedListener>();

	private JTextField originalTableTF = new JTextField(20);
	private JTextField accessareaTableTF = new JTextField(20);
	private JTextField generalizedTableTF = new JTextField(20);
	private JTextField statementTableTF = new JTextField(20);
	private JTextField geolocationTableTF = new JTextField(20);
	private JTextField userSessionTableTF = new JTextField(20);
	private JTextField userSessionTimeTF = new JTextField(20);
	private JTextField usernameTF = new JTextField(20);
	private JTextField serverAddressTF = new JTextField(40);
	final JTextPane jTextPane = new JTextPane();
	private final static File local = new File(".");
	private JFileChooser fileChooser = new JFileChooser();
	private JPasswordField passwordTF = new JPasswordField(20);
	private JPanel panel = new JPanel();
	String file = null;

    public void addListener(ValueSubmittedListener listener) {
        listeners.add(listener);
    }
    //Action Listener notification
    private void notifyListeners(int option, String serverAddress, String username, String password, String original, String statement, String generalized) {
        for (ValueSubmittedListener listener : listeners) {
			//System.out.println("Could not establish Connection");
            listener.onSubmitted(option, serverAddress, username, password, original, statement, generalized);
        }
    }

    private void notifyListeners(int option, String serverAddress, String username, String password, String original, String geoAreaFile) {
        for (ValueSubmittedListener listener : listeners) {
            listener.onSubmitted(option, serverAddress, username, password, original, geoAreaFile);
        }
    }

    private void notifyListeners(int option, String serverAddress, String username, String password, String original, String userSessionTable, float userSessionTime) {
        for (ValueSubmittedListener listener : listeners) {
            listener.onSubmitted(option, serverAddress, username, password, original, userSessionTable, userSessionTime);
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

	   JButton convertSDSS = new JButton("Convert from CSV");
	   JButton userSession = new JButton("Calculate User Sessions");
	   JButton fileButton = new JButton("Select CSV");
	   //convertSDSS.setBounds(0, 0, 0, 0);
	   JButton startTransformation = new JButton("Transform into AccessArea");
	   //startTransformation.setBounds(50, 60, 80, 30);
	   JButton geolocation = new JButton("Get Geolocations");
	   //geolocation.setBounds(50, 60, 80, 30);
	   JButton generalize = new JButton("Generalize");
	   //generalize.setBounds(50, 60, 80, 30);

	   JLabel serverAddressLabel = new JLabel("Server address: ");
	   JLabel usernameLabel = new JLabel("Username: ");
	   JLabel passwordLabel = new JLabel("Password: ");
	   

	   JLabel originalLabel = new JLabel("Tablename for SDSS data: ");
	   JLabel accessareaLabel = new JLabel("Tablename for Access Areas: ");
	   JLabel generalizedLabel = new JLabel("Tablename for generalized SQL statements: ");
	   JLabel statementLabel = new JLabel("Tablename for split SQL statements: ");
	   JLabel geolocationLabel = new JLabel("Tablename for geolocation: ");
	   JLabel userSessionLabel = new JLabel("Tablename for user sessions: ");
	   JLabel userSessionTimeLabel = new JLabel("'Think-Time' for user sessions (in minutes): ");
	   JLabel StatusLabel = new JLabel("Status: ");

	   //JLabel fileLabel = new JLabel("CSV File: ");
	   //File chooser
	   FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files","csv");
	   fileChooser.setFileFilter(filter);
	   try {
		   fileChooser.setCurrentDirectory(local);
	   } catch (Exception e) {
		   //e.printStackTrace();
	   }

	   convertSDSS.addActionListener(this);
	   startTransformation.addActionListener(this);
	   geolocation.addActionListener(this);
	   generalize.addActionListener(this);
	   fileButton.addActionListener(this);
	   userSession.addActionListener(this);
	   panel.add(serverAddressLabel);
	   panel.add(serverAddressTF);
	   panel.add(usernameLabel);
	   panel.add(usernameTF);
	   panel.add(passwordLabel);
	   panel.add(passwordTF);
	   panel.add(originalLabel);
	   panel.add(originalTableTF);
	   panel.add(accessareaLabel);
	   panel.add(accessareaTableTF);
	   panel.add(generalizedLabel);
	   panel.add(generalizedTableTF);
	   panel.add(statementLabel);
	   panel.add(statementTableTF);
	   panel.add(geolocationLabel);
	   panel.add(geolocationTableTF);
	   panel.add(userSessionLabel);
	   panel.add(userSessionTableTF);
	   panel.add(userSessionTimeLabel);
	   panel.add(userSessionTimeTF);
	   //panel.add(fileChooser);
	   buttons.add(fileButton);
	   buttons.add(convertSDSS);
	   buttons.add(startTransformation);
	   buttons.add(geolocation);
	   buttons.add(generalize);
	   buttons.add(userSession);
	   //Tooltips
	   convertSDSS.setToolTipText("Click to convert CSV Files containing SDSS Data into tabluar format");
	   startTransformation.setToolTipText("Click to start transforming the SDSS SQL statements into Access Areas");
	   geolocation.setToolTipText("Click to get Geolocation for the IPs in your SDSS Data");
	   generalize.setToolTipText("Click to generalize and group the SDSS SQL statements");
	   userSession.setToolTipText("Click to generate user sessions for SDSS Users");
	   fileButton.setToolTipText("Click to select SDSS CSV File you would like to transform into tabular format");
	   
	   panel.add(StatusLabel);
	   panel.add( new JScrollPane( jTextPane ) );
	   MessageConsole mc = new MessageConsole(jTextPane);
	   mc.redirectOut();
	   mc.redirectErr(Color.RED, null);
	   mc.setMessageLines(250);
	   jTextPane.setPreferredSize(new Dimension(880, 130));
	   jTextPane.setMinimumSize(new Dimension(10, 10));

	    setTitle("SDSS Transformation");
	    setSize(880, 600);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	//The actual ActionHandler. Depending on which button was pressed, checks if necessary parameters are not inserted and either prompts the user or starts the further process
	public void actionPerformed(final ActionEvent event) {
		Thread newThread = new Thread() {
		      public void run() {
		String serverAddress = serverAddressTF.getText();
		String username = usernameTF.getText();
		String password = new String(passwordTF.getPassword());
		String originalTable = originalTableTF.getText();
		String accessareaTable = accessareaTableTF.getText();
		String generalizedTable = generalizedTableTF.getText();
		String statementTable = statementTableTF.getText();
		String geolocationTable = geolocationTableTF.getText();
		String userSessionTable = userSessionTableTF.getText();
		String userSessionTimeString = userSessionTimeTF.getText();
		float userSessionTime = -1;
		try {
			userSessionTime = Float.parseFloat(userSessionTimeString);
		} catch (Exception e) {
			userSessionTime = -1;
		}
		
		//if(serverAddress.isEmpty()){
			//serverAddress = "marsara.ipd.uni-karlsruhe.de:1521:student";
		//}
		
		//originalTable = "original";
		//accessareaTable = "AccessArea";
		//generalizedTable = "GENERALIZED";
		//statementTable = "STATEMENT";
		//geolocationTable = "GEOLOCATION";
		//userSessionTable = "USERSESSION";
		
		//Which button was pressed? Check parameters, call function
		switch (event.getActionCommand()) {
		case "Convert from CSV":
			if(originalTable.isEmpty() || serverAddress.isEmpty() || username.isEmpty() || password.isEmpty()){
				JOptionPane.showMessageDialog(panel, "Please make sure to enter:\nServer Address\nUsername\nPassword\nTablename for SDSS Data",
	                    "Challenge", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			notifyListeners(1,serverAddress,username,password,originalTable,file);
			break;
		case "Transform into AccessArea":
			if(originalTable.isEmpty() || serverAddress.isEmpty() || username.isEmpty() || password.isEmpty() || accessareaTable.isEmpty()){
				JOptionPane.showMessageDialog(panel, "Please make sure to enter:\nServer Address\nUsername\nPassword\nTablename for SDSS Data\nTablename for Access Areas",
	                    "Challenge", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			notifyListeners(2,serverAddress,username,password,originalTable,accessareaTable);
			break;
		case "Get Geolocations":
			if(originalTable.isEmpty() || serverAddress.isEmpty() || username.isEmpty() || password.isEmpty() || geolocationTable.isEmpty()){
				JOptionPane.showMessageDialog(panel, "Please make sure to enter:\nServer Address\nUsername\nPassword\nTablename for SDSS Data\nTablename for geolocation data",
	                    "Challenge", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			notifyListeners(3,serverAddress,username,password,originalTable,geolocationTable);
			break;
		case "Generalize":
			if(originalTable.isEmpty() || serverAddress.isEmpty() || username.isEmpty() || password.isEmpty() || generalizedTable.isEmpty() || statementTable.isEmpty()){
				JOptionPane.showMessageDialog(panel, "Please make sure to enter:\nServer Address\nUsername\nPassword\nTablename for SDSS Data\nTablename for split statements\nTablename for generalized statements",
	                    "Challenge", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			notifyListeners(4,serverAddress,username,password,originalTable,statementTable,generalizedTable);
			break;
		case "Select CSV":
			int returnVal = fileChooser.showOpenDialog(panel);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile().getPath();
			}
			break;
		case "Calculate User Sessions":
			if(originalTable.isEmpty() || serverAddress.isEmpty() || username.isEmpty() || password.isEmpty() || userSessionTable.isEmpty() || userSessionTime < 0){
				JOptionPane.showMessageDialog(panel, "Please make sure to enter:\nServer Address\nUsername\nPassword\nTablename for SDSS Data\nTablename for user sessions\n'Think-Time' for user sessions >= 0",
	                    "Challenge", JOptionPane.INFORMATION_MESSAGE);
				break;
			}
			notifyListeners(5,serverAddress,username,password,originalTable,userSessionTable,userSessionTime);
			break;
		default:
			break;
		}
	}
		};
		newThread.start();
	}
}
