package sdsslogviewer.data.io;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import prefuse.data.Table;
import prefuse.data.io.AbstractTableWriter;
import prefuse.data.io.DataIOException;
import prefuse.util.collections.IntIterator;

/**
 * Dec. 11 2010:    Modify the prefuse.data.io.CSVTableWriter to deal with empty
 *                  cells, which will cause DataIOException.<p/>
 * @author James<p/>
 */
public class SDSSLogFileWriter extends AbstractTableWriter {

        private boolean m_printHeader;

    /**
     * Create a new CSVTableWriter that writes comma separated values files.<p/>
     */
    public SDSSLogFileWriter() {
        this(true);
    }

    /**
     * Create a new CSVTableWriter.<p/>
     * @param printHeader indicates if a header row should be printed<p/>
     */
    public SDSSLogFileWriter(boolean printHeader) {
        m_printHeader = printHeader;
    }

    // ------------------------------------------------------------------------

    /**
     * Indicates if this writer will write a header row with the column names.<p/>
     * @return true if a header row will be printed, false otherwise<p/>
     */
    public boolean isPrintHeader() {
        return m_printHeader;
    }

    /**
     * Sets if this writer will write a header row with the column names.<p/>
     * @param printHeader true to print a header row, false otherwise<p/>
     */
    public void setPrintHeader(boolean printHeader) {
        m_printHeader = printHeader;
    }

    // ------------------------------------------------------------------------

    /**
     * @see prefuse.data.io.TableWriter#writeTable(prefuse.data.Table, java.io.OutputStream)<p/>
     */
    public void writeTable(Table table, OutputStream os) throws DataIOException {
        try {
            // get print stream
            PrintStream out = new PrintStream(new BufferedOutputStream(os));

            // write out header row
            if ( m_printHeader ) {
                for ( int i=0; i<table.getColumnCount(); ++i ) {
                    if ( i>0 ) out.print(',');
                    out.print(makeCSVSafe(table.getColumnName(i)));
                }
                out.println();
            }

            // write out data
            for ( IntIterator rows = table.rows(); rows.hasNext(); ) {
                int row = rows.nextInt();
                for ( int i=0; i<table.getColumnCount(); ++i ) {
                    if ( i>0 ) out.print(',');
                    String str = table.getString(row, table.getColumnName(i));
                    out.print(makeCSVSafe(str));
                }
                out.println();
            }

            // finish up
            out.flush();
        } catch ( Exception e ) {
            throw new DataIOException(e);
        }
    }

    private String makeCSVSafe(String s) {
        int q = -1;

        if (s==null){                   //only place that I modified.
            return s = "\"\"";          //to deal with empty cells.
        }                               //let them to be circled with " ".

        if ( (q=s.indexOf('\"')) >= 0 ||
             s.indexOf(',')  >= 0 || s.indexOf('\n') >= 0 )
        {
            if ( q >= 0 ) s = s.replaceAll("\"", "\"\"");
            s = "\""+s+"\"";
        }
        return s;
    }

}
