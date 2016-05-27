/*
 */

package sdsslogviewer.SQL;

import de.congrace.exp4j.PostfixExpression;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparseableExpressionException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * Nov. 14th, 2010:  Class create to tokenize SDSS sql
 * Nov. 17th, 2010:  Tokenize is functional
 * Dec. 9th, 2010:   simpleAreaParser is functional
 * 
 * //TODO: need a simple math expression parser for advanced processing. Done by
 *         March 3rd, 2011.
 *
 * March 3rd, 2011: Use expj4 library to handle complex math expressions.
 *                  Not support POWER() founction yet.
 * March 4th, 2011: separate the LogParseException to an individual public class.
 * March 4th, 2011. Add new parsing regExpr and Add new math expression processor.<processor />
 * 
 * March 21th, 2011: convert arcmin to degree. Now it is precise.
 *         NOTE XXX: Precise BUT useless!!! Too small to be visible. So change it
 *                   back to degree.
 *
 * March 25th, 2011: Modify the parse to accomodate to new rectangle functions.
 * 
 *
 * @author James
 */
public class SQLParser {

    /** Constants for indicating SDSS area types */
    public final static int SQLParser_NO_AREA_INDICATOR = 0;
    public final static int SQLParser_CIRCLE_AREA       = 1;
    public final static int SQLParser_RECT_AREA         = 2;
    public final static int SQLParser_ID_INDICATOR      = 3;

    private String SQL_linebreaker = SDSSLOGVIEWCONSTANTS.LINEBREAKER;
    private char SQL_quote1 = '\'';
    private char SQL_quote2 = '\"';
    private char SQL_separator = ',';

    //-Separators for parsing a SQL string
    private String regx_separators = "[\\*\\(\\)\\+\\-\\/\\%\\&\\|\\^\\=\\>\\<\\!\\~\\@\\#]";
    private Pattern pat_separator = Pattern.compile(regx_separators);

    //-Separators for extract spatial information from functions.
    //-March 4th, 2011. Add new SDSS spatial function regExpr
    //-May 4th, 2011. Add two new SDSS circle function regExpr
    private String expr_spatialfunction = "objeq\\s*\\(.*?,.*?,.*?\\)|"
                                    + "objalleq\\s*\\(.*?,.*?,.*?\\)|"
                                    + "objideq\\s*\\(.*?,.*?,.*?\\)|"
                                    + "footprinteq\\s*\\(.*?,.*?,.*?\\)|"
                                    + "circleeq\\s*\\(.*?,.*?,.*?\\)|"
                                    + "rect\\s*\\(.*?,.*?,.*?,.*?\\)";  //for rect
    private Pattern pat_spatialfunction = Pattern.compile(expr_spatialfunction);

    //-March 25th, 2011: Deal with RectEq differently
    private String expr_rectfunction = "recteq\\s*\\(.*?,.*?,.*?,.*?\\)"; //for a special rect
    private Pattern pat_rectfunction = Pattern.compile(expr_rectfunction);

    /**
     * March 5th, 2011:
     * Revise ra and dec regExpr for math expression parsing
     */
    private String expr_ra = "ra\\s*[><]=?\\s*\\(?.*?\\)?\\s+and\\s+.*?ra\\s*[><]=?\\s*\\(?.*?\\)?\\s+[a-z]|" //for ra range
                           + "ra\\s*[><]=?\\s*\\(?.*?\\)?\\s+and\\s+.*?ra\\s*[><]=?\\s*\\(?.*?\\)?\\z|"
                           + "ra\\s*[><]=?\\s*\\(?.*?\\)?\\s+&\\s+.*?ra\\s*[><]=?\\s*\\(?.*?\\)?\\s+[a-z]|"
                           + "ra\\s*[><]=?\\s*\\(?.*?\\)?\\s+&\\s+.*?ra\\s*[><]=?\\s*\\(?.*?\\)?\\z|"
                           + "ra\\s+between\\s+.*?and.*?\\s+[a-z]|"
                           + "ra\\s+between\\s+.*?and.*?\\s+\\z";
    private Pattern pat_ra = Pattern.compile(expr_ra);

    private String expr_dec = "dec\\s*[><]=?\\s*\\(?.*?\\)?\\s+and\\s+.*?dec\\s*[><]=?\\s*\\(?.*?\\)?\\s+[a-z]|" //for dec range
                            + "dec\\s*[><]=?\\s*\\(?.*?\\)?\\s+and\\s+.*?dec\\s*[><]=?\\s*\\(?.*?\\)?\\z|"
                            + "dec\\s*[><]=?\\s*\\(?-?\\d.*?\\s+&\\s+.*?dec\\s*[><]=?\\s*\\(?-?\\d.*?\\s+[a-z]|"
                            + "dec\\s*[><]=?\\s*\\(?-?\\d.*?\\s+&\\s+.*?dec\\s*[><]=?\\s*\\(?-?\\d.*?\\z|"
                            + "dec\\s+between\\s+.*?and.*?\\s+[a-z]|"
                            + "dec\\s+between\\s+.*?and.*?\\s+\\z";
    private Pattern pat_dec = Pattern.compile(expr_dec);

    private String expr_id = "id\\s*=\\s*\\d+\\)?\\s+|id\\s*=\\s*\\d+\\)?<br>|id\\s*=\\s*\\d+\\)?\\z";
    private Pattern pat_id = Pattern.compile(expr_id);

    private Matcher matcher;

    //--------------------------------------------------------------------------
    private double[] z_area = new double[]{SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE, SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                               SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE, SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                               SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE};
    @SuppressWarnings("unused")
	private String[] z_cordinate;

    private ArrayList<String> tokens = null;
    private ArrayList<Integer> types = null;

    //--------------------------------------------------------------------------

    /**
     * Constructor, with a single quotation mark as separators.<p/>
     */
    public SQLParser(){
        this('\'');
    }

    /**
     * Constructor, with a quotation mark as separators.<p/>
     */
    public SQLParser(char quote1){
        this(quote1, '\"');
    }

    /**
     * Constructor, with both single quotation marks and quotation marks as separators<p/>
     */
    public SQLParser(char quote1, char quote2){
        this(quote1, quote2, ',');
    }

    /**
     * Constructor, with both single quotation marks and quotation marks as separators
     * and specified separators.<p/>
     */
    public SQLParser(char quote1, char quote2, char separator){
        this(quote1, quote2, separator, SDSSLOGVIEWCONSTANTS.LINEBREAKER);
    }

    /**
     * Constructor, with both single quotation marks and quotation marks as separators
     * and specified separators. And an indicator of line breakers.<p/>
     */
    public SQLParser(char quote1, char quote2, char separator, String linebreaker){
        SQL_quote1 = quote1;
        SQL_quote2 = quote2;
        SQL_separator = separator;
        SQL_linebreaker = linebreaker;
    }

    /**
     * set the line breaker for parsing process.<p/>
     * @param lb - string for line breaker<p/>
     */
    public void setLineBreaker(String lb){
        SQL_linebreaker = lb;
    }

    /**
     * Get current used line breaker.
     * @return 
     */
    public String getLineBreaker (){
        return SQL_linebreaker;
    }

    /**
     * After tokenize, retrieve the tokens arraylist<p/>
     */
    public String[] getTokens(){

        if (tokens != null){
            return tokens.toArray(new String[tokens.size()]);
        } else {
            String[] sa = {"N/A"};
            return sa;
        }
    }  //end gettokens method

    /**
     * After tokenize, retrieve the token length<p/>
     */
    public int[] getTokenLength(){
        String token = "";
        if (tokens != null) {
            int[] length = new int[tokens.size()];
            for (int i=0;i<tokens.size();i++){
                token = (String) tokens.get(i);
                length[i] = token.length();
            }
            return length;
        } else {
            int[] ia = {Integer.MIN_VALUE};
            return ia;
        }
    }  //end of gettokenlength method

    /**
     * after tokenize, retrieve the token's type arraylist<p/>
     */
    public int[] getTypes(){

        if (types != null){
            int[] ia = new int[types.size()];
            for (int i=0;i<types.size();i++){
                ia[i] = types.get(i);
            }
            return ia;
        } else {
            int[] ia = {Integer.MIN_VALUE};
            return ia;
        }
    }  //end of gettypes method

    /**
     * ASSUME: No comments between quote marks.<p/>
     *
     * WEEKNESS: Not independent from specific sql. Should be uniform and use an
     *           interface to pass preference of sql.<p/>
     * @param SQL a sql query<p/>
     * @return String array with tokens + SDSSSQL types<p/>
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	public void tokenize(String SQL){
        /**
         * local variable to control tokenization
         */
        boolean inString = false;
        //-initialize the two lists.
        tokens = new ArrayList();
        types = new ArrayList();

        StringBuilder sbuf = new StringBuilder();
        
        //-1. devide into lines
        SQL = SQL.trim();
        SQL = SQL.replaceAll("\\s+", " ");  //replace multiple white spaces
        String[] lines = SQL.toLowerCase().split(SQL_linebreaker);  //separate lines

        //-2. conqur each line
        //-- first scan +  assign type to comment lines only. Other is 0 as default.
        for (String line: lines){

            //-3. check if is a comment line
            if (!line.startsWith("--")){                        //not a comment line
                char[] ca = line.toCharArray();
                for (char c: ca){
                    if (inString){
                        if (c == SQL_quote1 || c == SQL_quote2){
                            //end of a quote
                            inString = !inString;
                            sbuf.append(c);
                            tokens.add(sbuf.toString().trim());
                            types.add(MSSQL.MSSQL_QUOTE);
                            sbuf.delete(0, sbuf.length());
                        } else
                            //no end of a quote, just append
                            sbuf.append(c);
                    } else { //not in a string
                        //-- first check if start of a string
                        if (c == SQL_quote1 || c == SQL_quote2){
                            inString = !inString;
                            if (sbuf.length() != 0){    //save before quote
                                tokens.add(sbuf.toString().trim());
                                types.add(MSSQL.MSSQL_UNKNOWN);
                                sbuf.delete(0, sbuf.length());
                            }
                            sbuf.append(c);
                        } else { //if not
                            //-- check if whitespace
                            if (((c == SQL_separator) || Character.isWhitespace(c))
                                    && (sbuf.length() != 0)){ //if so, insert the token
                                tokens.add(sbuf.toString().trim());
                                types.add(MSSQL.MSSQL_UNKNOWN);
                                sbuf.delete(0, sbuf.length());
                            } else {    //if not, append to a token
                                sbuf.append(c);
                            }
                        }

                    } //end not in string
                } //end of one line
                if (sbuf.length() !=0 ){
                    tokens.add(sbuf.toString().trim());
                    types.add(MSSQL.MSSQL_UNKNOWN);
                    sbuf.delete(0, sbuf.length());
                }
            } else {
                //in a comment line
                tokens.add(line.trim());
                types.add(MSSQL.MSSQL_COMMENT);
            } // end if startwith
        } // end for first round:
          // SQL => token + quotes + comments.

        //-- second round scan for each token
        sbuf.delete(0, sbuf.length());                          //clean the stringbuilder
        int size = tokens.size();
        String[] tokenArray = tokens.toArray(new String[size]); //-- change to array for performance
        tokens.clear();                                         //clear memory
        Integer[] typeArray = types.toArray(new Integer[size]);
        types.clear();

        /**
         * March 4th, 2011 new comments
         *  start second scan to assign tokens with their types. If could be divided
         *  into further tokens, do it.
         */
        SDSSSQL sdss = new SDSSSQL();
        int start = 0, end = 0, tokentype;                      //control variables
        for (int i=0; i<size; i++){

            if ((typeArray[i] != MSSQL.MSSQL_COMMENT)&&
                (typeArray[i] != MSSQL.MSSQL_QUOTE)) {          //if not comment or quote, proceed

                tokentype = sdss.getMSSQLTokenType(tokenArray[i].trim());  //check type

                //- March 4th, 2011: if is unknow, process for another tokenization.
                if (tokentype == MSSQL.MSSQL_UNKNOWN){          //if unknown, proceed

                    matcher = pat_separator.matcher(tokenArray[i].trim());
                    //separate token to new tokens. Each check with sql types.
                    while (matcher.find()){
                        if (matcher.start()>start){
                            end = matcher.start();
                            tokens.add(tokenArray[i].substring(start, end));
                            types.add(sdss.getSDSSTokenType(tokenArray[i].substring(start, end).trim()));
                            start = end;
                            end = matcher.end();
                        } else {
                            start = matcher.start();
                            end = matcher.end();
                        }
                        tokens.add(tokenArray[i].substring(start, end));
                        types.add(sdss.getSDSSTokenType(tokenArray[i].substring(start, end).trim()));
                        start = end;
                    }
                    if (start<tokenArray[i].length()){
                        tokens.add(tokenArray[i].substring(start, tokenArray[i].length()));
                        types.add(sdss.getSDSSTokenType(tokenArray[i].substring(start, tokenArray[i].length()).trim()));
                    }
                } else {
                    //has MSSQL type, just copy
                    tokens.add(tokenArray[i]);
                    types.add(tokentype);
                }
            } else {
                //comments or quotes, just copy
                tokens.add(tokenArray[i]);
                types.add(typeArray[i]);
            } //end if comments
            start = 0;
            end = 0;
        } //end for second round
    
    }   //end of tokenize method

    /**
     * March 4th, 2011: Disable original findArea method for clarity.<p/>
     * 
     * A complex version of findArea(), use regular expression to find area indicators<p/>
     * @param sql, a sql query text string<p/>
     * @return a double array, as follow:<p/>
     *      double[0]: indicator of type of area. 0: Not area indicator<p/>
     *                                            1: Circular area<p/>
     *                                            2: Rectangular area<p/>
     *                                            3: object ID<p/>
     *                             Double.MIN_VALUE: fail to process<p/>
     *                                               see, SQLParser's constants.<p/>
     *
     *      double[1]: RA of center point;        if no area indicator, = double.MIN_VALUE<p/>
     *      double[2]: DEC of center point;       if no area indicator, = double.MIN_VALUE<p/>
     *      double[3]: Width or Radius            if no area indicator, = double.MIN_VALUE<p/>
     *      double[4]: Height                     if no area or circle, = double.MIN_VALUE<p/>
     *
     *  March 4th, 2011: Use an advanced math expression library to calculate the
     *                   RAs and DECs in the form of math expression.<p/>
     */
    public double[] parseArea (String sql){
        //local variables
        String substring1 = "", substring2 = "";

        //-- . to lower case
        sql = sql.replaceAll(SDSSLOGVIEWCONSTANTS.LINEBREAKER, " ").toLowerCase();
        //-- . find first pattern, the ObjID pattern.
        matcher  = pat_id.matcher(sql);
        if (matcher.find()){
            z_area[0] = 3;                  //just indicate this is an id query
        } else {

            matcher = pat_spatialfunction.matcher(sql);
            if (matcher.find()){            //find functions
                substring1 = sql.substring(matcher.start(), matcher.end());

                try {
                z_area = parseFunction(substring1);
                } catch (LogParseException lpe) {
                    System.err.println("SQLParser error (01): Functions have wrong format. " + sql);
                } 

            } else {                        //find ra and dec range
                matcher = pat_ra.matcher(sql);
                if (matcher.find()){
                    substring1 = sql.substring(matcher.start(), matcher.end());

                    matcher = pat_dec.matcher(sql);
                    if (matcher.find()){
                        substring2 = sql.substring(matcher.start(), matcher.end());
                        /**
                         * march 5th, 2011:
                         * both RA and DEC strings found, then do ParseRange
                         */
                        try {
                            z_area = ParseRange(substring1, substring2);
                        } catch (LogParseException lpe){
                            System.err.println("SQLParser error (02): RA and DEC range strings have wrong format\n" + sql);
                        } catch (ArrayIndexOutOfBoundsException aobe){
                            System.err.println("SQLParser error (03): RA and DEC oordinates missing\n" + sql);
                        }
                    } else {
                        z_area[0] = 0;
                    }
                } else {
                    matcher = pat_rectfunction.matcher(sql);
                    if (matcher.find()){
                        substring1 = sql.substring(matcher.start(), matcher.end());

                        try {
                            z_area = parseRectFunction(substring1);
                        } catch (LogParseException lpe) {
                        System.err.println("SQLParser error (01): Functions have wrong format. " + sql);
                }
                    } else {
                        z_area[0] = 0;
                    }
                }
            }
        } //end if

        return z_area;
    }

    /**
     * A method to find area info from the string retrieved from a function parameter<p/>
     *
     * March 4th, 2011: This method is simple, and assume parameters for functions
     *                  are numbers. Not a math expression. Works fine for most cases
     *                  But not completed.<p/>
     *                  Improvement use math expression parse to do the work.<p/>
     * 
     * @param fstring, string like objeq(190, 2.5, 0) or rect(1,2,1,2)<p/>
     * @return double array, see above parseArea method comment<p/>
     *
     * March 4th, 2011: Parse each filed in a function with math expression parser.
     *                  Replace original Double.doubleParse() method with evaluateMathExpr<p/>
     *
     */
    private double[] parseFunction(String fstring) 
            throws LogParseException {

        double[] farray = {SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE};

        String temp = fstring.substring(fstring.indexOf("(")+1, fstring.indexOf(")"));
        String[] coor = temp.split(",");
        switch (coor.length) {
            case 3: farray[0] = 1;
                    farray[1] = evaluateMathExpr(coor[0]);
                    farray[2] = evaluateMathExpr(coor[1]);
//                    farray[3] = evaluateMathExpr(coor[2])/60;
//radius divid by 60 is the presicse measure, but can hardly be seen in viz. So
//keep the original arcmin = degree measure.
                    farray[3] = evaluateMathExpr(coor[2]);
                    break;
            case 4: farray[0] = 2;
                    farray[1] = (evaluateMathExpr(coor[0]) + evaluateMathExpr(coor[1]))/2.0;
                    farray[2] = (evaluateMathExpr(coor[2]) + evaluateMathExpr(coor[3]))/2.0;
                    farray[3] = Math.abs(evaluateMathExpr(coor[1]) - evaluateMathExpr(coor[0]));
                    farray[4] = Math.abs(evaluateMathExpr(coor[3]) - evaluateMathExpr(coor[2]));
                    break;
            default: throw new LogParseException();
        }

        return farray;
    }

    /**
     * A method to find area info from the string retrieved from a special rect
     * function<p/>
     *
     * March 25th, 2011: Deal with this kind of rect function separately<p/>
     *
     * @param fstring, string rect(RA1,DEC1,RA2,DEC2)<p/>
     * @return double array, see above parseArea method comment<p/>
     *
     */
    private double[] parseRectFunction(String fstring)
            throws LogParseException {

        double[] farray = {SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE};

        String temp = fstring.substring(fstring.indexOf("(")+1, fstring.indexOf(")"));
        String[] coor = temp.split(",");
        if (coor.length != 4){
            throw new LogParseException();
        } else {
            farray[0] = 2;
            farray[1] = (evaluateMathExpr(coor[0]) + evaluateMathExpr(coor[2]))/2.0;
            farray[2] = (evaluateMathExpr(coor[1]) + evaluateMathExpr(coor[3]))/2.0;
            farray[3] = Math.abs(evaluateMathExpr(coor[2]) - evaluateMathExpr(coor[0]));
            farray[4] = Math.abs(evaluateMathExpr(coor[3]) - evaluateMathExpr(coor[1]));
        }

        return farray;
    }

    /**
     * March 4th, 2011:
     * Revise the method. It can process math expression.
     *
     * Dec. 6th, 2010: A simplified version of get the ra and dec range.
     * NOTE: this method assume the ra and dec range are two double numbers.
     *
     *
     * @param rastring, string like ra > (198.1 -1) and ra < (198.2+1)
     * @param decstring, string like dec > -10.2 and dec < -1.5
     * @return doulbe array, following above definitions.
     * double[0]: indicator of type of area. 0: Not area indicator
     *                                       1: Ciriclur area
     *                                       2: Rectangular area
     *                                       3: object ID
     * see, SQLParser's constants.
     *
     *      double[1]: RA of center point;        if no area indicator, = SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE
     *      double[2]: DEC of center point;       if no area indicator, = SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE
     *      double[3]: Width or Radius            if no area indicator, = SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE
     *      double[4]: Height                     if no area or circle, = SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE
     * 
     * @throws LogParseException, ArrayIndexOutOfBoundsException, 
     *         UnparseableExpressionException, UnknownFunctionException,
     *         so upper level methods can catch this exception and indicate what is wrong.
     */
    private double[] ParseRange(String rastring, String decstring)
            throws LogParseException, ArrayIndexOutOfBoundsException {

        double[] rarray = {SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE,
                           SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE};

        //--1. remove non-numbers
        String[] temp1 = rastring.split("and|&");   //should be 2 unit array
        String[] temp2 = decstring.split("and|&");  //should be 2 unit array

        //--2. find start point of the expression
        rarray[0] = SQLParser.SQLParser_RECT_AREA;
        rarray[1] = (ParseValue(temp1[0]) + ParseValue(temp1[1]))/2;
        rarray[2] = (ParseValue(temp2[0]) + ParseValue(temp2[1]))/2;
        rarray[3] = Math.abs(ParseValue(temp1[0]) - ParseValue(temp1[1]));
        rarray[4] = Math.abs(ParseValue(temp2[0]) - ParseValue(temp2[1]));

        return rarray;
    }

    /**
     * March 4th, 2011:
     * Revise to use evaluateMathExpr method.
     *
     * Dec 6th, 2010:
     * A simple method to remove non-numbers and get the double value of an ra or
     * dec string.
     *
     * @param value, a string like "ra> 19.8" or "ra < 20.6 a"
     * @return a double value of the query parameters
     * @throws LogParseException, UnparseableExpressionException, UnknownFunctionException,
     *         telling upper level method there is an error
     */
    private double ParseValue(String value)
            throws LogParseException, ArrayIndexOutOfBoundsException {

        double number = SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE;
//System.out.println(value);
        //--1. find value start point.
        int start = -1;
        if (value.indexOf(">=") != -1 ){
            start = value.indexOf(">=")+2;
        } else {
            if (value.indexOf("<=") != -1){
                start = value.indexOf("<=")+2;
            } else {
                if (value.indexOf("between") != -1){
                    start = value.indexOf("between")+7;
                } else {
                    if (value.indexOf(">") != -1){
                        start = value.indexOf(">") +1;
                    } else {
                        if (value.indexOf("<") != -1){
                            start = value.indexOf("<")+1;
                        } else {
                            start = 0;
                        }
                    }
                }
            }
        } //end if check start point

        //--2. extract value string
        /**
         * March 4th, 2011:
         * XXX NOTE: exp4j does not deal with scientific numerics, e.g. "19.8e-02+1"
         *
         * So stick with below string processing. Then after extract the string
         */
        value = value.substring(start);       //expression like (19.8e-02+1) a
        if ((value.charAt(value.length()-1) > 96) && (value.charAt(value.length()-1) < 123)){
            value = value.substring(0, value.length()-1);
        }
        value = value.trim();
//System.out.println(value);
        //--3. extract expression if it is a scientific number.
        /**
         * March 5th, simplify the step with two simple justment
         *     1. Check if is a number with Double.parseDouble();
         *     2. If not, use evaluateMathExpress.
         * For both wrong, throw an exception.
         */
         number = evaluateMathExpr(value);

        return number;
    }

    /**
     * March 4th, 2011: Method created to evaluate a math expression by using
     *                  exp4j libraries
     * exp4j is created by Frank Asseg at Germany here is the
     * <a href="http://objecthunter.congrace.de/tinybo/blog/articles/86">link</a>
     * of this library.
     *
     * @param mathexpr, a string of math expression, like "2 * 17.41 + (12*2)sin(0-1)")
     * @return a double value of the math expression.
     *
     * @throws UnparseableExpressionException, something wrong with the expression
     * @throws UnknownFunctionException, something wrong with functions used, e.g.
     * power
     */
    private double evaluateMathExpr(String mathexpr)
            throws LogParseException {
        double result = SDSSLOGVIEWCONSTANTS.DEFAULT_DOUBLE;

        try{
            PostfixExpression processor = PostfixExpression.fromInfix(mathexpr);
            result = processor.calculate();
        } catch (Exception e){
            try{
                mathexpr = mathexpr.replaceAll("[()]", "");
                result = Double.parseDouble(mathexpr);
            } catch (NumberFormatException nfe){
                throw new LogParseException();
            }
        }

        return result;
    }



} //end of SQLParser class

