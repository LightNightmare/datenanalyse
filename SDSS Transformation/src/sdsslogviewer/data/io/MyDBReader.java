package sdsslogviewer.data.io;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.io.sql.ConnectionFactory;
import prefuse.data.io.sql.DatabaseDataSource;

/**
 * Setp. 9 2010: Class created for import data from remote databases.<p/>
 *
 * NOTE: Replace the defaultdatahandler with SDSSdefaultdatahandler because there
 *       is a bug in the original codes.<p/>
 *
 * Oct. 20 2010: Change the constructor to ask for username and password.
 *               allow to change DB driver and DB URL.<p/>
 * @author JZhang<p/>
 */
public class MyDBReader {

    /** A constant for IBM DB2 database driver*/
    private final static String DB2DRIVER  = "com.ibm.db2.jcc.DB2Driver";
    private final static String DB2URL = "jdbc:db2://cluster.ischool.drexel.edu:50000/jz85";

    /** Database driver for db connection. Default is IBM DB2. */
    protected String dbdriver;

    /** Database url. Default is cluster.ischool.drexel.edu */
    protected String dburl;

    /** Database username. Default is my username at cluster.ischool.drexel.edu */
    protected String dbuser;

    /** Database user password. Default is my password at the cluster server */
    protected String dbpw;

    /** Data source for return */
    private DatabaseDataSource dbds;

    /**
     * construct db connection with my default DB account in cluster DB2 database.<p/>
     * @param user - username<p/>
     * @param password - password<p/>
     */
    public MyDBReader(String user, String password) throws SQLException, ClassNotFoundException{
        this(DB2DRIVER, DB2URL, user, password);
    }

    /**
     * Construct a db connector with driver, url, username, password.<p/>
     * @param driver - JDBC driver<p/>
     * @param url - database url<p/>
     * @param user - user name <p/>
     * @param password - passworld<p/>
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    public MyDBReader(String driver, String url, String user, String password)
            throws SQLException, ClassNotFoundException{
        dbdriver = driver;
        dburl = url;
        dbuser = user;
        dbpw = password;

        getConnection();
    }

    /**
     * set database JDBC driver.<p/>
     * @para dbdriver customized DBDrivers<p/>
     */
    public void setDBDriver(String dbdriver){
        this.dbdriver = dbdriver;
    }

    /**
     * set database url.<p/>
     * @param dburl customized DBURL<p/>
     */
    public void setURL (String dburl){
        this.dburl = dburl;
    }

    /**
     * get currenttly used database JDBC driver.<p/>
     * @return used DB Driver name
     */
    public String getDBDriver(){
        return this.dbdriver;
    }

    /**
     * Get currently used database url.<p/>
     * @return used DB URL
     */
    public String getDBURL(){
        return this.dburl;
    }

    /**
     * Get the connection with given driver, url, user, and password. Default
     * data handler is SDSSDefaultDataHandler.<p/>
     * @return DatabaseDataSource for query.
     */
    @SuppressWarnings("static-access")
	private void getConnection() throws SQLException, ClassNotFoundException {

        SDSSDefaultSQLDataHandler sdh = new SDSSDefaultSQLDataHandler();
        ConnectionFactory dbcf = new ConnectionFactory();
        dbds = null;

        dbds = dbcf.getDatabaseConnection(dbdriver, dburl, dbuser, dbpw, sdh);

    }

    /**
     * A method to directly issue a query to get data from MyDB at cluster.<p/>
     * @para a valid DB2 SQL query. This method does not make SQL syntactic check<p/>
     * @return a prefuse table.<p/>
     * 
     *  NOTE: (1) no ";" at end of a sql<p/>
     *        (2) case sensitive<p/>
     */
    public Table getTable (String query) {

        Table t = null;

        try {
            t = dbds.getData(query);
        } catch (DataIOException sqle){
            JOptionPane.showMessageDialog(null, "Wrong SQL", "Wrong SQL. Tables or fields not exist", JOptionPane.ERROR_MESSAGE);
        } 

        return t;
    }

}
