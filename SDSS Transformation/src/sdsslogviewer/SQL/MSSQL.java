
package sdsslogviewer.SQL;

import java.util.HashSet;


/**
 * June 13th 2010, created for testing parser of SQL queries.<p/>
 * 
 * A class to store the reserved words of MS SQL server queries.<p/>
 * Theoretically, these words should have a hierarchical structure, e.g<p/>
 *      DB-Table-View functions:    create tables, drop tables, ...<p/>
 *      Column functions:           add, delete columns, ...<p/>
 *      Record functions:           add, delete, change rows, ...<p/>
 *      Keyword for query:          "select", ...<p/>
 *      Operators:                  ">", ",", ...<p/>
 *      Functions:                  "ABC", ...<p/>
 *
 * Nov. 2th, 2010:  start to parse real SQL queries.<p/>
 *                  Change hierarchical into two levels<p/>
 *                  1. Overall type level, defined in QUERYCONSTANTS inteface, including<p/>
 *                     6 types + 1 unknown type<p/>
 *                  2. Structure level, including 6 types of tokens + 1 unknown type<p/>
 *                      0 - keyword, e.g. select, create, alter, drop,......<p/>
 *                      1 - function, e.g. ABS, AVG, CAST,......<p/>
 *                      2 - operator, e.g. arithmetic operators, logical operators<p/>
 *                      3 - stored functions and procedures,<p/>
 *                      4 - special chars, e.g. (, ), [, ], ', {, ......<p/>
 *                      5 - comments, start with --<p/>
 *                      6 - others, unidentified tokens<p/>
 * @author James<p/>
 */
@SuppressWarnings("rawtypes")
public class MSSQL extends AbstractSQL {

    /**
     * Nov. 5th, modify hierarchical structure to this version.<p/>
     *
     * For prove of concept only, not fully implement MS SQL server's words.<p/>
     */
    protected static HashSet sqlkeywords = new HashSet();    //MSSQL reserved keywords
    protected static HashSet sqlfunctions = new HashSet();   //MSSQL mathematical + string functions
    protected static HashSet sqloperators = new HashSet();   //MSSQL operators arithmetic operator + logic
    protected static HashSet sqlcharacters = new HashSet();   //MSSQL special charaters
    protected static HashSet sqlcomments = new HashSet();    //MSSQL comments start with "--"

    /**
     * Nov. 11, 2010<p/>
     * Type of MSSQL words.<p/>
     * NOTE: Subclass should start from 7 if any new types<p/>
     */
    public static final int MSSQL_KEYWORD           = 0;
    public static final int MSSQL_FUNCTION          = 1;
    public static final int MSSQL_OPERATOR          = 2;
    public static final int MSSQL_CHARACTER         = 3;
    public static final int MSSQL_QUOTE             = 4;
    public static final int MSSQL_COMMENT           = 5;
    public static final int MSSQL_UNKNOWN           = 6;


    /**
     * A static variable used for lower case query. Static is for saving space.
     */
    private static String low_q = null;

    /**
     * constructor of this class to load all words in.<p/>
     */
    public MSSQL(){
        this(null);
    }

    /**
     * constructor of this class to load all words in. Could Add additional keywords
     * by the specified file path.<p/>
     * @param path - location of a file for additional keywords.<p/>
     */
    public MSSQL(String path){

        /**
         * First version, tree sql based on object operated.
         *
        dbwords=new HashSet();
        tbwords=new HashSet();
        viewwords=new HashSet();
        clwords=new HashSet();
        rcwords=new HashSet();
        tswords=new HashSet();
        pcwords=new HashSet();
        fcwords=new HashSet();
        idwords=new HashSet();
        mkwords=new HashSet();
        */

/*        sqlkeywords = new HashSet();
        sqlfunctions = new HashSet();
        sqloperators = new HashSet();
        sqlcharacters = new HashSet();
        sqlfun_procs = new HashSet();
        sqlcomments = new HashSet();
*/
        //load reserved words into memory
        loadDefaultMSSQLwords();
        //if something new, load from a defined txt file.
        loadPlusMSSQLwords(path);
    }

    @Override
    /**
     * parse a query and return a simple hierarchical type, like database 
     * operation and etc.<p/>
     *
     * Nov 3rd, this version is simple for prove-of-concept.<p/>
     *
     */
    public int getSQLtype(String query) {
        /**
         * Given the majority queries are select queries, the default would be
         */
//        int type = QUERYCONSTANTS.DATA_QUERY;

        /**
         * make all to lower case for string comparison
         */
        if (low_q ==null) low_q = "";
        low_q = query.toLowerCase();

        if (low_q.startsWith("create database")
                || low_q.startsWith("drop database")
                || low_q.startsWith("exec sp_renamedb")){
            return QUERYCONSTANTS.DATABASE_QUERY;
        } else {
            if (low_q.startsWith("create table")
                    || low_q.startsWith("drop table")
                    || (low_q.startsWith("sp_rename")&&!low_q.endsWith("'column'"))
                    || low_q.startsWith("create view")
                    || low_q.startsWith("alter view")
                    || low_q.startsWith("drop view")){
                return QUERYCONSTANTS.TABLE_VIEW_QUERY;
            } else {
            if (low_q.startsWith("alter table")
                    || (low_q.startsWith("sp_rename")&& low_q.endsWith("'column'"))
                    || low_q.startsWith("insert into")
                    || low_q.startsWith("update")
                    || low_q.startsWith("delete")){
                return QUERYCONSTANTS.DATA_MANIPULATE_QUERY;
                } else {
                    if (low_q.startsWith("select")){
                        return QUERYCONSTANTS.DATA_QUERY;
                    } else {
                        if (low_q.startsWith("create function")
                                || low_q.startsWith("drop function")
                                || low_q.startsWith("create procedure")
                                || low_q.startsWith("drop procedure")
                                || low_q.startsWith("alter procedure")){
                            return QUERYCONSTANTS.TRAN_PROC_QUERY;
                        } else {
                            if (low_q.startsWith("create index")
                                    || low_q.startsWith("drop index")
                                    || low_q.startsWith("create trigger")
                                    || low_q.startsWith("alter trigger")
                                    || low_q.startsWith("drop trigger")){
                                return QUERYCONSTANTS.INDEX_QUERY;

                            /**for all other case return unknown query*/
                            } else return QUERYCONSTANTS.UNKNOWN_QUERY;
                        }
                    }
                }
            }
        } //end if

    } //end getSQLtype method


    /**
     * the following 4 methods are not supported yet.
     */
    @Override
    String[] getColums(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    String[] getTables(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    Object[] getParameters(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    String abstractQuery(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * A set of isMETHOD() to tell the type of a token.<<p/>>
     * @param token
     * @return
     */
    public static boolean isKeyword(String token){
        return sqlkeywords.contains(token);
    }

    public static boolean isFunction(String token){
        return sqlfunctions.contains(token);
    }

    public static boolean isOperator(String token){
        return sqloperators.contains(token);
    }

    public static boolean isCharater(String token){
        return sqlcharacters.contains(token);
    }

/*    public static boolean isFun_Proc(String token){
        return sqlfun_procs.contains(token);
    }*/

    /**
     * getType method return a token's type. If not find, assign unknown<p/>
     * @return int type of the token in one of MSSQL fields<p/>
     */
    public static int getMSSQLTokenType(String token){

        if (isCharater(token)){
            return MSSQL.MSSQL_CHARACTER;
            } else {
                if (isOperator(token)){
                    return MSSQL.MSSQL_OPERATOR;
                } else {
                    if (isKeyword(token)){
                        return MSSQL.MSSQL_KEYWORD;
                    } else {
                        if (isFunction(token)){
                            return MSSQL.MSSQL_FUNCTION;
                        } else {
                            return MSSQL.MSSQL_UNKNOWN;
                        }
                    }
                }
            }
    }

    
    @SuppressWarnings("unchecked")
	private void loadDefaultMSSQLwords(){

        sqlkeywords.add("create");
        sqlkeywords.add("database");
        sqlkeywords.add("drop");
        sqlkeywords.add("sp_renamedb");
        sqlkeywords.add("table");
        sqlkeywords.add("alter");
        sqlkeywords.add("sp_rename");
        sqlkeywords.add("view");
        sqlkeywords.add("column");
        sqlkeywords.add("add");
        sqlkeywords.add("identity");
        sqlkeywords.add("insert");
        sqlkeywords.add("into");
        sqlkeywords.add("values");
        sqlkeywords.add("update");
        sqlkeywords.add("set");
        sqlkeywords.add("delete");
        sqlkeywords.add("from");
        sqlkeywords.add("where");
        sqlkeywords.add("as");
        sqlkeywords.add("begin");
        sqlkeywords.add("end");
        sqlkeywords.add("order");
        sqlkeywords.add("by");
        sqlkeywords.add("case");
        sqlkeywords.add("check");
        sqlkeywords.add("cross");
        sqlkeywords.add("join");
        sqlkeywords.add("outer");
        sqlkeywords.add("declare");
        sqlkeywords.add("if");
        sqlkeywords.add("else");
        sqlkeywords.add("execute");
        sqlkeywords.add("exec");
        sqlkeywords.add("asc");
        sqlkeywords.add("desc");
        sqlkeywords.add("constraint");
        sqlkeywords.add("default");
        sqlkeywords.add("distinct");
        sqlkeywords.add("foreign");
        sqlkeywords.add("key");
        sqlkeywords.add("references");
        sqlkeywords.add("primary");
        sqlkeywords.add("full");
        sqlkeywords.add("group");
        sqlkeywords.add("having");
        sqlkeywords.add("inner");
        /**left and right could be string function too. Here just categorize it as keyword*/
        sqlkeywords.add("left");
        sqlkeywords.add("right");
        sqlkeywords.add("is");
        sqlkeywords.add("print");
        sqlkeywords.add("select");
        sqlkeywords.add("then");
//        sqlkeywords.add("top");
        sqlkeywords.add("union");
        sqlkeywords.add("use");         //new in SQL server 2005
        sqlkeywords.add("when");
        sqlkeywords.add("while");
        sqlkeywords.add("with");
        sqlkeywords.add("commit");
        sqlkeywords.add("transaction");
        sqlkeywords.add("tran");
        sqlkeywords.add("rollback");
        sqlkeywords.add("function");
        sqlkeywords.add("return");
        sqlkeywords.add("procedure");
        sqlkeywords.add("proc");
        sqlkeywords.add("index");
        sqlkeywords.add("on");
        sqlkeywords.add("unique");

        sqlfunctions.add("abs");
        sqlfunctions.add("ascii");
        sqlfunctions.add("avg");
        sqlfunctions.add("cast");
        sqlfunctions.add("ceiling");
        sqlfunctions.add("char");
        sqlfunctions.add("cos");
        sqlfunctions.add("count");
        sqlfunctions.add("cot");
        sqlfunctions.add("degrees");
        sqlfunctions.add("exp");
        sqlfunctions.add("floor");
        sqlfunctions.add("len");
        sqlfunctions.add("log");
        sqlfunctions.add("log10");
        sqlfunctions.add("max");
        sqlfunctions.add("min");
        sqlfunctions.add("pi");
        sqlfunctions.add("power");
        sqlfunctions.add("radians");
        sqlfunctions.add("replace");
        sqlfunctions.add("rand");
        sqlfunctions.add("round");
        sqlfunctions.add("sin");
        sqlfunctions.add("sign");
        sqlfunctions.add("stddev");
        sqlfunctions.add("square");
        sqlfunctions.add("sqrt");
        sqlfunctions.add("top");
        sqlfunctions.add("tan");

        sqloperators.add("+");
        sqloperators.add("-");
        sqloperators.add("*");
        sqloperators.add("/");
        sqloperators.add("%");
        sqloperators.add("&");
        sqloperators.add("|");
        sqloperators.add("^");
        sqloperators.add("=");
        sqloperators.add(">");
        sqloperators.add("<");
        sqloperators.add(">=");
        sqloperators.add("<=");
        sqloperators.add("<>");
        sqloperators.add("!=");
        sqloperators.add("!<");
        sqloperators.add("!>");
        sqloperators.add("and");
        sqloperators.add("all");
        sqloperators.add("between");
        sqloperators.add("false");
        sqloperators.add("true");
        sqloperators.add("null");
        sqloperators.add("in");
        sqloperators.add("like");
        sqloperators.add("not");
        sqloperators.add("or");
        sqloperators.add("exits");
        sqloperators.add("~");

        sqlcharacters.add(",");
        sqlcharacters.add("(");
        sqlcharacters.add(")");
        sqlcharacters.add("@");
        sqlcharacters.add("#");
        sqlcharacters.add("'");

//        sqlfun_procs.add("dbo.");     //Nov. 17, 2010. Remove this hashset

        sqlcomments.add("--");


        //load database words
//        dbwords.add("CREATE DATABASE");
//        dbwords.add("DROP DATABASE");

        //load table words
//        tbwords.add("CREATE TABLE");
//        tbwords.add("DROP TABLE");

        //load view words
//        viewwords.add("CREATE VIEW");
//        viewwords.add("ALTER VIEW");

        //load column words
//        clwords.add("ADD");
//        clwords.add("ALTER TABLE");
//        clwords.add("INDENTITY");
//        clwords.add("DROP COLUMN");
//        clwords.add("SP_RENAME");

        //load row words
//        rcwords.add("INSERT INTO");
//        rcwords.add("VALUES");
//        rcwords.add("UPDATE");
//        rcwords.add("SET");
//        rcwords.add("DELETE FROM");
//        rcwords.add("DELETE");

        //load tranction words
//        tswords.add("ALL");
//        tswords.add("AND");
//        tswords.add("AS");
//        tswords.add("ASC");
//        tswords.add("BETWEEN");
//        tswords.add("BEGIN");
//        tswords.add("END");
//        tswords.add("ORDER BY");
//        tswords.add("CASE");
//        tswords.add("CONSTRAINT");
//        tswords.add("CHECK");
//        tswords.add("CROSS JOIN");
//        tswords.add("CROSS OUTER JOIN");
//        tswords.add("DECLARE");
//        tswords.add("DEFAULT");
//        tswords.add("DISTINCT");
//        tswords.add("ELSE");
//        tswords.add("EXECUTE");
//        tswords.add("FALSE");
//        tswords.add("TRUE");
//        tswords.add("FOREIGN KEY REFERENCES");
//        tswords.add("PRIMARY KEY");
//        tswords.add("FULL JOIN");
//        tswords.add("FULL OUTER JOIN");
//        tswords.add("GROUP BY");
//        tswords.add("HAVING");
//        tswords.add("IF");
//        tswords.add("IN");
//        tswords.add("INNER JOIN");
//        tswords.add("JOIN");
//        tswords.add("LEFT JOIN");
//        tswords.add("LEFT OUTER JOIN");
//        tswords.add("RIGHT JOIN");
//       tswords.add("RIGHT OUTER JOIN");
//        tswords.add("IS");
//        tswords.add("LIKE");
//        tswords.add("MERGE");         //not in sql server 2005
//        tswords.add("NOT");
//        tswords.add("NULL");
//        tswords.add("OR");
//        tswords.add("OUTPUT");        //not in sql server 2005
//        tswords.add("PRINT");
//        tswords.add("SELECT");
//        tswords.add("THEN");
//        tswords.add("TOP");
//        tswords.add("UNION");
//        tswords.add("USING");         //not in sql server 2005
//        tswords.add("WHEN");
//        tswords.add("WHERE");
//        tswords.add("WHILE");
//        tswords.add("WITH");

//        tswords.add("BEGIN TRANSACTION");
//        tswords.add("BEGIN TRAN");
//        tswords.add("COMIT TRANSACTION");
//        tswords.add("COMIT TRAN");
//        tswords.add("ROLLBACK TRANSACTION");
//        tswords.add("ROLLBACK TRAN");

        //load procedure words
//        pcwords.add("CREATE FUNCTION");
//        pcwords.add("RETURNS");
//        pcwords.add(".dbo.");
//        pcwords.add("CREATE PROCEDURE");
//        pcwords.add("CREATE PROC");
//        pcwords.add("ALTER PROCEDURE");
//        pcwords.add("DROP PROCEDURE");
//        pcwords.add("EXECUTE");
//        pcwords.add("EXEC");

        //load build-in functions
//        fcwords.add("ABS");
//        fcwords.add("ASCII");
//        fcwords.add("AVG");
//        fcwords.add("CAST");          //not in sql server 2005
//        fcwords.add("CEILING");
//        fcwords.add("CHAR");
//        fcwords.add("CONVERT");       //not in sql server 2005
//        fcwords.add("COS");
//        fcwords.add("COUNT");
//        fcwords.add("DATEADD");
//        fcwords.add("DATEDIFF");
//        fcwords.add("DEGREES");
//        fcwords.add("EXP");
//        fcwords.add("FLOOR");
//        fcwords.add("GETDATE");
//        fcwords.add("LEFT(");
//        fcwords.add("LEN");
//        fcwords.add("LOG");
//        fcwords.add("LOG10");
//        fcwords.add("MAX");
//        fcwords.add("MIN");
//        fcwords.add("POWER");
//        fcwords.add("PI");
//        fcwords.add("RADIANS");
//        fcwords.add("REPLACE");
//        fcwords.add("RIGHT(");
//        fcwords.add("SIN");
//        fcwords.add("STDDEV");
//        fcwords.add("SUM");
//        fcwords.add("TAN");
//        fcwords.add("SIGN");
//        fcwords.add("LOWER");
//        fcwords.add("SQRT");
//        fcwords.add("EXISTS");
//        fcwords.add("VAR");

        //load index words
//        idwords.add("CREATE INDEX");
//        idwords.add("ON");
//        idwords.add("CREATE CLUSTERED INDEX");
//        idwords.add("CREATE NONCLUSTERED INDEX");
//        idwords.add("CREATE UNIQUE INDEX");
//        idwords.add("DROP INDEX");

        //load marks
//        mkwords.add(",");
//        mkwords.add("(");
//        mkwords.add(")");
//        mkwords.add("@");
        }

        private void loadPlusMSSQLwords(String path){
            if (path!=null){
                //TODO read in local file for additional ms-sql words
            }
        }

}
