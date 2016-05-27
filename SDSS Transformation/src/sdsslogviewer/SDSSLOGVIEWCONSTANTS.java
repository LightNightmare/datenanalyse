package sdsslogviewer;

/**
 * Global constants used in the SDSS Log Viewer.<p/>
 * @author James
 */
public interface SDSSLOGVIEWCONSTANTS {

    /**
     * Sign to separate a line break in SQL statement contents
     * NOTE: use <br> to set a natural break in ToolTip text.
     */
    public final static String LINEBREAKER = "\\[br\\]";

    /*separator character for tokens.*/
    public final static String TOKENBREAKER = ";";

    /** new columns derived from existing ones*/
    public final static String TOKENS       = "tokens";
    public final static String TOKEN_LENGTH = "token_length";
    public final static String TOKEN_TYPES  = "token_types";
    public final static String SQL_TYPES    = "sql_type";
    public final static String TIMEID       = "timeid";
    public final static String COLORS       = "colors";
    public final static String CONSTANT     = "constant";
    public final static String AREATYPES    = "areatypes";
    public final static String AREA_RA      = "area_right_acesion";
    public final static String AREA_DEC     = "area_declination";
    public final static String AREA_WIDTH   = "area_width";
    public final static String AREA_HEIGHT  = "area_height";
    public final static String SEARCH_TABLES= "search_tables";
    public final static String SEARCH_VIEWS = "search_views";
    public final static String SEARCH_FUNC  = "search_functions";
    public final static String SEARCH_PROC  = "search_procedures";

    /**default double values for log data*/
    public final static double DEFAULT_DOUBLE = -1048576.0;
   }
