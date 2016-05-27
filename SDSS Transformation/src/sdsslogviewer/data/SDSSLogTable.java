package sdsslogviewer.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import prefuse.data.Table;
import prefuse.data.column.ColumnMetadata;
import prefusePlus.data.column.ColumnMetadataPlus;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;
import sdsslogviewer.SQL.SDSSSQL;
import sdsslogviewer.SQL.SQLParser;

/**
 * The class decorates prefuse Table with SDSS derived columns.<p/>
 * 
 * Nov 18th, 2010:  Is a prefuse.data.table that stores needed information from
 *                  a SDSS log table
 *
 * Dec. 10th, 2010: add full function to process raw CSV data, including output
 *                  to text fills that can be imported to a database table.
 *
 * May 14th, 2011: Reconstruct the timeID field by integrating yy, mm, dd, hh, mi,
 *                 ss, instead of the original theTime filed, which has inconsistent
 *                 format.
 * @author JZhang
 */
public class SDSSLogTable {

    /** the column names of SDSS sql log */
    public final static String YEAR      = "yy";
    public final static String MONTH     = "mm";
    public final static String DAY       = "dd";
    public final static String HOUR     = "hh";
    public final static String MINITUE   = "mi";
    public final static String SECOND    = "ss";
    public final static String SEQUENCE  = "seq";
    public final static String TIME      = "theTime";
    public final static String LOGID     = "logID";
    public final static String IPADDRESS = "clientIP";
    public final static String REQUESTOR = "requestor";
    public final static String SERVER    = "server";
    public final static String DATABASE  = "dbname";
    public final static String ACCESSPORTAL = "access";
    public final static String ELAPSED   = "elapsed";
    public final static String BUSYTIME  = "busy";
    public final static String ROWS      = "rows";
    public final static String STATEMENT = "statement";
    public final static String ERROR     = "error";
    public final static String ERRORMESSAGE = "errorMessage";
    public final static String VISIBLE   = "isvisible";

    private Table m_table = null;

    private ColumnMetadata      m_cmd;
    private ColumnMetadataPlus  m_cmdp;

    private SQLParser           m_tokenizer;
    private SDSSSQL             m_sdsssql;

    /**
     * Construct a SDSSLogTable with a prefuse Table.<p/>
     * @param t - 
     */
    public SDSSLogTable (Table t){

        //--1. tokenize SQL statements
        int sqlcol = (t.getSchema()).getColumnIndex(STATEMENT);             //check if has statement column
        if (sqlcol != -1){
            //-remove two column, not analyze in this tool
            t.removeColumn(VISIBLE);                                        //for data processing, not need to delete

            //--2. add new columns for deriving new values
            t.addColumn(SDSSLOGVIEWCONSTANTS.TIMEID, String.class);         //timeid, add id within one second
            t.addColumn(SDSSLOGVIEWCONSTANTS.TOKENS, String[].class);       //tokens, add parsed sql tokens
            t.addColumn(SDSSLOGVIEWCONSTANTS.SQL_TYPES, int.class);          //type, add parsed sql type
            t.addColumn(SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, int[].class);
            t.addColumn(SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH, int[].class);
            t.addColumn(SDSSLOGVIEWCONSTANTS.CONSTANT, int.class, 1);       //constant, add for SQLview x-axis
            //colors will be created by SQLView class on fly, not here
//            t.addColumn(SDSSLOGVIEWCONSTANTS.COLORS, ArrayList.class);      //colors, colors of parsed sql tokens
            t.addColumn(SDSSLOGVIEWCONSTANTS.AREATYPES, int.class);         //areatype, areas in a sql query
            t.addColumn(SDSSLOGVIEWCONSTANTS.AREA_RA, double.class);        //ra, ra of area center point
            t.addColumn(SDSSLOGVIEWCONSTANTS.AREA_DEC, double.class);       //dec, dec of area center point
            t.addColumn(SDSSLOGVIEWCONSTANTS.AREA_WIDTH, double.class);     //width, radius or width of area
            t.addColumn(SDSSLOGVIEWCONSTANTS.AREA_HEIGHT, double.class);    //height, height of area

            //--3. process raw data and derive values for new column

            //--3.1 initialize tokenizer and local control variables
            m_tokenizer = new SQLParser();
            m_tokenizer.setLineBreaker(SDSSLOGVIEWCONSTANTS.LINEBREAKER);
            m_sdsssql = new SDSSSQL();

            String sql = "", time = "", pretime = "";
            int id = 0;
            int size = t.getRowCount();
            double[] area = {SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE, SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                             SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE, SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                             SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE};

            //--3.2 iterator all rows and derive values;
            for (int i=0;i<size;i++){
                sql = (String) t.get(i, SDSSLogTable.STATEMENT);
                //tokenize and set in
                m_tokenizer.tokenize(sql);
                t.set(i, SDSSLOGVIEWCONSTANTS.TOKENS, m_tokenizer.getTokens());
                t.set(i, SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, m_tokenizer.getTypes());
                t.set(i, SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH, m_tokenizer.getTokenLength());
                t.set(i, SDSSLOGVIEWCONSTANTS.SQL_TYPES, m_sdsssql.getSQLtype(sql));

                area = m_tokenizer.parseArea(sql);
                t.set(i, SDSSLOGVIEWCONSTANTS.AREATYPES, area[0]);
                t.set(i, SDSSLOGVIEWCONSTANTS.AREA_RA, area[1]);
                t.set(i, SDSSLOGVIEWCONSTANTS.AREA_DEC, area[2]);
                t.set(i, SDSSLOGVIEWCONSTANTS.AREA_WIDTH, area[3]);
                t.set(i, SDSSLOGVIEWCONSTANTS.AREA_HEIGHT, area[4]);

                //set fine granular time tag
                time = (String.valueOf(t.get(i, YEAR))) + ":" +
                        (String.valueOf(t.get(i, MONTH))) + ":" +
                        (String.valueOf(t.get(i, DAY))) + " " +
                        (String.valueOf(t.get(i, HOUR))) + ":" +
                        (String.valueOf(t.get(i, MINITUE))) + ":" +
                        (String.valueOf(t.get(i, SECOND)));
                if (time.equalsIgnoreCase(pretime)){
                    t.set(i, SDSSLOGVIEWCONSTANTS.TIMEID, time + ":" + String.valueOf(id++));
                } else {
                    id = 0;
                    t.set(i, SDSSLOGVIEWCONSTANTS.TIMEID, time + ":" + String.valueOf(id++));
                    pretime = time;
                }
                //clean the string for the next found.
                sql = "";
            }  //end for iteration of all rows
            //--3.3 May remove some columns for certain applications
//            t.removeColumn(STATEMENT);
//            t.removeColumn(TIME);
           // t.removeColumn(ERRORMESSAGE); //edit by florianB

    System.out.println("Log data table is ready");
    DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy-HH_mm_ss");
    Date d=new Date();
    String newTable=dateFormat.format(d);
    System.out.println(newTable);

            //--4. Pass the processed table to my table
            m_table = t;

            //--5. Get other statistics for control preference
            //NOTE: not implement yet.
            getSDSSLogTableStat();

        } else throw new UnsupportedOperationException("Not has statement column yet.");

    }

    /**
     * Get the decorated SDSS log table.<p/>
     * @return decorated Table<p/>
     */
    public Table getSDSSLogTable(){
        return m_table;
    }

    /**
     * Get the column metadata with specified column name.<p/>
     * @param column - name of the column for metadata<p/>
     * @return a ColumnMetadata<p/>
     */
    public ColumnMetadata getColumnMetadata (String column){
        m_cmd = m_table.getMetadata(column);
        return m_cmd;
    }

    /**
     * Get a column metadata plus with specified column name.<p/>
     * @param column - name of the column for metadata plus
     * @return a ColumnMetadataPlus
     */
    public ColumnMetadataPlus getColumnMetadataPlus (String column){
        m_cmdp = new ColumnMetadataPlus(m_table, column);
        return m_cmdp;
    }


    private void getSDSSLogTableStat(){
        //TODO: get statistic of each columns.
        //For string:   object--frequency
        //For number:   Distribution, max, min, median, SD, 20% counts
    }

    /**
     * A method to convert SDSS Log Table to a Table that can be readed by
     * DB2 data importer.<p/>
     * @return 
     */
    public Table convertToDBOutput(){

        //--1. create a new table and
        Table derivedTable = new Table();
        derivedTable.addColumn(SDSSLogTable.SEQUENCE, int.class);
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.TOKENS, String.class);
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, String.class);
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH, String.class);
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.SQL_TYPES, int.class);
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.CONSTANT, int.class, 1);       //constant, add for SQLview x-axis
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.AREATYPES, int.class);         //areatype, areas in a sql query
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.AREA_RA, double.class);        //ra, ra of area center point
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.AREA_DEC, double.class);       //dec, dec of area center point
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.AREA_WIDTH, double.class);     //width, radius or width of area
        derivedTable.addColumn(SDSSLOGVIEWCONSTANTS.AREA_HEIGHT, double.class);    //height, height of area

        //--2. Convert m_table's array contents into a string for output to local file
        int size = m_table.getRowCount();
        String[] tokens = null;
        int[] tokentypes = null;
        int[] tokenlength = null;
        int r;

        for (int i=0;i<size;i++){
            tokens = (String[]) m_table.get(i, SDSSLOGVIEWCONSTANTS.TOKENS);
            tokentypes = (int[]) m_table.get(i, SDSSLOGVIEWCONSTANTS.TOKEN_TYPES);
            tokenlength = (int[]) m_table.get(i, SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH);

            r = derivedTable.addRow();
            derivedTable.setInt(r, SDSSLogTable.SEQUENCE, m_table.getInt(i, SDSSLogTable.SEQUENCE));
            derivedTable.setString(r, SDSSLOGVIEWCONSTANTS.TOKENS, stringArrayToString(tokens));
            derivedTable.setString(r, SDSSLOGVIEWCONSTANTS.TOKEN_TYPES, intArrayToString(tokentypes));
            derivedTable.setString(r, SDSSLOGVIEWCONSTANTS.TOKEN_LENGTH, intArrayToString(tokenlength));
            derivedTable.setInt(r, SDSSLOGVIEWCONSTANTS.SQL_TYPES, m_table.getInt(i, SDSSLOGVIEWCONSTANTS.SQL_TYPES));
            derivedTable.setInt(r, SDSSLOGVIEWCONSTANTS.AREATYPES, m_table.getInt(i, SDSSLOGVIEWCONSTANTS.AREATYPES));
            derivedTable.setDouble(r, SDSSLOGVIEWCONSTANTS.AREA_RA, m_table.getDouble(i, SDSSLOGVIEWCONSTANTS.AREA_RA));
            derivedTable.setDouble(r, SDSSLOGVIEWCONSTANTS.AREA_DEC, m_table.getDouble(i, SDSSLOGVIEWCONSTANTS.AREA_DEC));
            derivedTable.setDouble(r, SDSSLOGVIEWCONSTANTS.AREA_WIDTH, m_table.getDouble(i, SDSSLOGVIEWCONSTANTS.AREA_WIDTH));
            derivedTable.setDouble(r, SDSSLOGVIEWCONSTANTS.AREA_HEIGHT, m_table.getDouble(i, SDSSLOGVIEWCONSTANTS.AREA_HEIGHT));

        }  //end for scan of convert

        return derivedTable;
    }

    private String stringArrayToString(String[] sa){
        String s = "";
        for (int i=0;i<sa.length;i++){
            s += sa[i] + SDSSLOGVIEWCONSTANTS.TOKENBREAKER;
        }
        return s;
    }

    private String intArrayToString(int[] ia){
        String s = "";
        for (int i=0;i<ia.length;i++){
            s += ia[i] + SDSSLOGVIEWCONSTANTS.TOKENBREAKER;
        }
        return s;
    }

}
