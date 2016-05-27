package sdsslogviewer.SQL;

/**
 * An abstract class to represent general SQL language as a base for detailed SQL
 * classes in this package.<p/>
 * @author James<p/>
 */
public abstract class AbstractSQL {

    /** parse a sql query and return an int as the type of the query
     *  the type is defined in Constants class<p/>
     * @param query<p/>
     * @return query type<p/>
     */
    abstract int getSQLtype(String query);

    /** parse a sql query and return a list of strings that contains column names<p/>
     *
     * @param query<p/>
     * @return a list of colum names<p/>
     */
    abstract String[] getColums(String query);

    /** parse a sql query and return a list of strings that contains table names.<p/>
     *
     * @param query<p/>
     * @return a list of table names<p/>
     */
    abstract String[] getTables(String query);

    /** parse a sql query and return a list of objects that contains query parameters<p/>
     *
     * @param query<p/>
     * @return a list of parameters<p/>
     */
     abstract Object[] getParameters(String query);

    /** parse a sql query and return a marked string<p/>
     *
     * @param query<p/>
     * @return original query with markup labels<p/>
     */
     abstract String abstractQuery(String query);
}
