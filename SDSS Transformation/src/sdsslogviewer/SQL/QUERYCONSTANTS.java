
package sdsslogviewer.SQL;

/**
 * <p>Constants used for differentiating SQL queries
 * </p>
 *
 * @author James
 */
public interface QUERYCONSTANTS {

    /** A query that operates on database level*/
    public final static int DATABASE_QUERY=0;
    /** A query that operates on table and view level*/
    public final static int TABLE_VIEW_QUERY=1;
    /** A query that operates on data level*/
    public final static int DATA_MANIPULATE_QUERY=2;
    /** A query that operates on data level*/
    public final static int DATA_QUERY=3;
    /** A query that operates on transactions and procedures*/
    public final static int TRAN_PROC_QUERY=4;
    /** A query that operates on index level*/
    public final static int INDEX_QUERY=5;
    /** A query with unknown type*/
    public final static int UNKNOWN_QUERY=6;
    /** an index array for query type*/
    int[] QUERYTYPE = new int[] {DATABASE_QUERY, TABLE_VIEW_QUERY,
                                 DATA_MANIPULATE_QUERY, DATA_QUERY,
                                 TRAN_PROC_QUERY, INDEX_QUERY, UNKNOWN_QUERY};

    /** Overall view level*/
    public final static int OVERALL_VIEW=0;
    /** Structure view level*/
    public final static int STRUCTURE_VIEW=1;
    /** Detailed view level*/
    public final static int DETAIL_VIEW=2;

} //end of interface queryconstants
