
package sdsslogviewer.data.io;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import prefuse.data.Table;
import prefuse.data.io.sql.SQLDataHandler;

/**
 * Default data value handler for mapping SQL data types to Java objects.<p/>
 * Performs a straightforward mapping of common SQL data types to Java
 * primitives or objects.<p/>
 * Modification of prefuse SQLDataHandler.<p/>
 * @author James
 */
public class SDSSDefaultSQLDataHandler implements SQLDataHandler {

    private boolean m_ignoreUnknownTypes;

    // ------------------------------------------------------------------------

    /**
     * Create a new DefaultSQLDataHandler.<p/>
     */
    public SDSSDefaultSQLDataHandler() {
        this(true);
    }

    /**
     * Create a new DefaultSQLDataHandler.<p/>
     * @param ignoreUnknownTypes instructs the data handler whether or not
     * unknown or unrecognized SQL data types should simply be ignored<p/>
     */
    public SDSSDefaultSQLDataHandler(boolean ignoreUnknownTypes) {
        m_ignoreUnknownTypes = ignoreUnknownTypes;
    }

    // ------------------------------------------------------------------------

    /**
     * Set if unknown or unrecognized SQL data types should simply be ignored.<p/>
     * @param ignore the ignore unknown types setting to use<p/>
     */
    public void setIgnoreUnknownTypes(boolean ignore) {
        m_ignoreUnknownTypes = ignore;
    }

    /**
     * Indicates if unknown or unrecognized SQL data types should simply be
     * ignored.<p/>
     * @return the ignore unknown types setting<p/>
     */
    public boolean isIgnoreUnknownTypes() {
        return m_ignoreUnknownTypes;
    }

    // ------------------------------------------------------------------------

    /**
     * Modification is made here in this method. Because the problem with Double 
     * type.<p/>
     * @see prefuse.data.io.sql.SQLDataHandler#process(prefuse.data.Table, int, java.sql.ResultSet, int)
     */
    public void process(Table t, int trow, ResultSet rset, int rcol)
        throws SQLException
    {
        ResultSetMetaData metadata = rset.getMetaData();
        String field = metadata.getColumnName(rcol);
        int type = metadata.getColumnType(rcol);

        switch ( type ) {
        case Types.ARRAY:
            t.set(trow, field, rset.getArray(rcol));
            break;

        case Types.BIGINT:
            t.setLong(trow, field, rset.getLong(rcol));
            break;

        case Types.BINARY:
        case Types.LONGVARBINARY:
        case Types.VARBINARY:
            t.set(trow, field, rset.getBytes(rcol));
            break;

        case Types.BIT:
        case Types.BOOLEAN:
            t.setBoolean(trow, field, rset.getBoolean(rcol));
            break;

        case Types.BLOB:
            t.set(trow, field, rset.getBlob(rcol));
            break;

        case Types.CHAR:
        case Types.LONGVARCHAR:
        case Types.VARCHAR:
            t.setString(trow, field, rset.getString(rcol));
            break;

        case Types.CLOB:
            t.set(trow, field, rset.getClob(rcol));
            break;

        case Types.DATE:
            t.setDate(trow, field, rset.getDate(rcol));
            break;

        case Types.DECIMAL:
        case Types.DOUBLE:
        case Types.FLOAT:
        case Types.NUMERIC:
            t.setDouble(trow, field, rset.getDouble(rcol));
            break;

        case Types.INTEGER:
        case Types.SMALLINT:
        case Types.TINYINT:
            t.setInt(trow, field, rset.getInt(rcol));
            break;

        case Types.JAVA_OBJECT:
            t.set(trow, field, rset.getObject(rcol));
            break;

        case Types.REAL:
            t.setFloat(trow, field, rset.getFloat(rcol));
            break;

        case Types.REF:
            t.set(trow, field, rset.getRef(rcol));
            break;

        case Types.TIME:
            t.setDate(trow, field, rset.getTime(rcol));
            break;

        case Types.TIMESTAMP:
            t.setDate(trow, field, rset.getTimestamp(rcol));
            break;

        case Types.DATALINK:
        case Types.DISTINCT:
        case Types.NULL:
        case Types.OTHER:
        case Types.STRUCT:
        default:
            if ( !m_ignoreUnknownTypes ) {
                t.set(trow, field, rset.getObject(rcol));
            }
            break;
        }
    }

    /**
     * Return the data type of a cell.<p/>
     * @see prefuse.data.io.sql.SQLDataHandler#getDataType(java.lang.String, int)
     */
    @SuppressWarnings("rawtypes")
	public Class getDataType(String columnName, int sqlType) {
        switch ( sqlType ) {
        case Types.ARRAY:
            return Array.class;

        case Types.BIGINT:
            return long.class;

        case Types.BINARY:
        case Types.LONGVARBINARY:
        case Types.VARBINARY:
            return byte[].class;

        case Types.BIT:
        case Types.BOOLEAN:
            return boolean.class;

        case Types.BLOB:
            return Blob.class;

        case Types.CHAR:
        case Types.LONGVARCHAR:
        case Types.VARCHAR:
            return String.class;

        case Types.CLOB:
            return Clob.class;

        case Types.DATE:
            return Date.class;

        case Types.DECIMAL:
        case Types.NUMERIC:
        case Types.DOUBLE:
        case Types.FLOAT:
            return double.class;

        case Types.INTEGER:
        case Types.SMALLINT:
        case Types.TINYINT:
            return int.class;

        case Types.JAVA_OBJECT:
            return Object.class;

        case Types.REAL:
            return float.class;

        case Types.REF:
            return Ref.class;

        case Types.TIME:
            return Time.class;

        case Types.TIMESTAMP:
            return Timestamp.class;

        case Types.DATALINK:
        case Types.DISTINCT:
        case Types.NULL:
        case Types.OTHER:
        case Types.STRUCT:
        default:
            if ( !m_ignoreUnknownTypes ) {
                return Object.class;
            } else {
                return null;
            }
        }
    }

} // end of class DefaultSQLDataValueHandler
