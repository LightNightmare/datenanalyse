
package sdsslogviewer.data.io;

import au.com.bytecode.opencsv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import prefuse.data.Table;
import prefuse.data.io.AbstractTextTableReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.DataParser;
import prefuse.data.parser.ParserFactory;
import prefuse.data.parser.TypeInferencer;
import prefuse.util.io.IOLib;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * Jan. 5th, 2010: Class created to read a local file part by part and separated
 *                 from the original SDSSLogFileReader, which only read in all
 *                 contents at once.<p/>
 *                 Completed. Can deal with wrong line number.<p/>
 * @author JZhang
 */
public class SDSSLogFileReaderPlus extends AbstractTextTableReader {

    /**original variables. Control only parser and location*/
    private ParserFactory m_pfactory = ParserFactory.getDefaultFactory();
    private TypeInferencer di = new TypeInferencer(m_pfactory);

    /**new variables. */
    private String location;
    private BufferedReader bufR;
    private int z_currentpoint = 0;

    /**output variables*/
    @SuppressWarnings("rawtypes")
	private ArrayList z_headers = new ArrayList();
    @SuppressWarnings("unused")
	private int z_norows;
    private int z_nocols;

    /**control variables*/
    private int z_nolines;
    private boolean z_isScanned = false;

    private String z_preline = "";

    /**
     * Construct a file reader with given location of file, url<p/>
     * @param location - could be a URL<p/>
     */
    public SDSSLogFileReaderPlus(String location){

            this.location = location;
    }

    /**
     * Construct a file reader with given parserFactory.<p/>
     * @param parserFactory <p/>
     */
    public SDSSLogFileReaderPlus(ParserFactory parserFactory){
        super(parserFactory);
    }

    /**
     * Method to just scan table and find the number of rows.<p/>
     * @return an int array with file information.<p/>
     * @throws DataIOException<p/>
     * @throws IOException <p/>
     */
    public int[] scanTable() throws DataIOException, IOException{
        return scanTable(1);
    }

    /**
     * Public method to scan the SDSS log and get information back to caller.
     * Meanwhile, get the info of table, including header, row, col.<p/>
     * @param location, the input file<p/>
     * @param rowLimit, the size of a block<p/>
     * @return int array with line number of each block and total number of record<p/>
     *         int[0]-int[size-1] the start line number of block<p/>
     *         int[size]          total number of records<p/>
     *
     * @throws prefuse.data.io.DataIOException<p/>
     * @throws java.io.IOException<p/>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public int[] scanTable(int rowlimit) throws DataIOException, IOException{

        final ArrayList headers = getColumnNames();
        final int[] dim = new int[] { 0, 0 };

        TableReadListener scanner = new TableReadListener() {
            int prevLine = -1;
            public void readValue(int line, int col, String value)
                throws DataParseException
            {
                // sample value to determine data type
                if ( line > 1 || !true ) {
                    di.sample(col-1, value);

                    // update num rows
                    if ( line != prevLine ) {
                        prevLine = line;
                        dim[0]++;
                    }
                } else if ( line == 1 && true ) {
                    headers.add(value);
                }

                // update num cols
                if ( col > dim[1] )
                    dim[1] = col;
            }
        };

        //-- . start to read line in
        InputStream is = IOLib.streamFromString(location);

        ArrayList line_row = new ArrayList();
        String line;
        bufR = new BufferedReader(new InputStreamReader(is));
        int lineno = 0;
        int recordno = 0;

        String templine="";
        String[] splitedline=null;

        //-- . Scab each line and read line header for record recognition
        while ((line=bufR.readLine())!=null){
            //for each line, get a line number.
            lineno++;

            //-- the sign of a new record.
            //NOTE: this method could miss some rows which start with 201, e.g.
            //      201.48 in a ra or dec, or in an object id line
            //      But, not affect to much.
            if (line.startsWith("yy") || line.startsWith("200") || line.startsWith("201")) {
                if (!templine.equals("")){
                    //clean multiple whitespaces and tabs in each record for future processing
                    templine=templine.replaceAll(" {2,}", " ");
                    templine=templine.replaceAll("\t{1,}", "");

                    //Format each record into a Tab-seperated line for read in values
//                    templine=parseRecord(templine);
                    templine = CSVparser(templine);

                    //Read in values
                    splitedline=null;                   //clean this variable
                    splitedline=templine.split("\t");   //seperate each column
                    int cols=splitedline.length;
                    if (cols==21){          //Rigous set the column number
                        ++recordno;
//System.out.println(lineno);
                        for (int j=0;j<cols;j++){           //read values in
                            try {
                                //read values in
                                scanner.readValue(recordno, j + 1, splitedline[j].trim());
                            } catch (DataParseException ex) {
                                System.err.println("SDSSLogView.error(102): data parse error" + j
                                        + " column in " + templine);
                            }
                        }
                    } else {
                        System.err.println("SDSSLogView.error(101): SDSS log file read in error" + templine);
                    }
                } else {
                    templine = templine + line + SDSSLOGVIEWCONSTANTS.LINEBREAKER;
                    continue;
                }
                templine = line;
            } else {
                templine = templine + line + SDSSLOGVIEWCONSTANTS.LINEBREAKER;
                continue;
            }
            //-- check the rowlimit and build return array
            if ((recordno % rowlimit) == 0){
                line_row.add(lineno);
            }
        }   //end of while loop
        
        line_row.add(recordno);

        z_nolines = lineno;

        //-- .store header, num row, num col in local variables
        z_headers = headers;
        z_norows = dim[0];
        z_nocols = dim[1];
        z_isScanned = true;

        //after eacg scab set the pointer to begining point
        z_currentpoint = lineno;

        //-- . covert to an int array and return block informatioin to
        int[] rowinfo = new int[line_row.size()];
        for (int i=0;i<line_row.size();i++){
            rowinfo[i] = (Integer) line_row.get(i);
        }

        return rowinfo;
    }

    /**
     * Read table from a specified row number.<p/>
     * @param start - row number to start reading<p/>
     * @return a Table, part of the original file<p/>
     * @throws DataIOException
     * @throws IOException 
     */
    public Table readPartialTable(int start) throws DataIOException, IOException{
        /**if no scanned, scan first to get local variables*/
        if (!z_isScanned){
            scanTable();
        }
        return readPartialTable(start, z_nolines);
    }

    /**
     * Public method, read a part of table file.<p/>
     * @param start- the row number to start reading<p/>
     * XXX NOTE: 1 should be the beginning if read a new
     * @param rowlimit - the number of rows<p/>
     *
     * @throws prefuse.data.io.DataIOException<p/>
     * @throws java.io.IOException<p/>
     */
    @SuppressWarnings("unused")
	public Table readPartialTable(int start, int rowlimit)
            throws DataIOException, IOException{

        /**if no scanned, scan first to get local variables*/
        if (!z_isScanned){
            scanTable(rowlimit);
        }

        final int[] dim = new int[]{-1, -1};
        final Table table = new Table(0, z_nocols);

        // create the table columns
        for ( int i=0; i < z_nocols; ++i ) {
            String header;
            if ( true || i < z_headers.size() ) {
                header = (String)z_headers.get(i);
            } else {
                header = getDefaultHeader(i);
            }
            table.addColumn(header, di.getType(i));
            table.getColumn(i).setParser(di.getParser(i));
        }

        TableReadListener parser = new TableReadListener() {
            int prevLine = -1;
            public void readValue(int line, int col, String value)
                throws DataParseException
            {
                // early exit on header value
//                if ( line == 1 && true )
//                    return;
                if ( line != prevLine ) {
                    prevLine = line;
                    dim[0] = table.addRow();     //different from above, here add a new instead of preset row number.
                }
                dim[1] = col-1;

                DataParser dp = di.getParser(dim[1]);
                table.set(dim[0], dim[1], dp.parse(value));
            }
        };

        // read the data into the table
        try {
            // check the current read point. If > start, then need to go back to
            // the begin.
            //XXX NOTE: this method is slow for going back, should have smarter
            //method to read buffered data if not flushed yet.
            if (z_currentpoint > start){
                bufR.close();
                InputStream is = IOLib.streamFromString(location);
                bufR = new BufferedReader(new InputStreamReader(is));
                z_currentpoint = 0;
            }
            read(parser, start, rowlimit);
        } catch ( IOException ioe ) {
            throw new DataIOException(ioe);
        } catch ( DataParseException de ) {
            throw new DataIOException("Parse exception for column "
                    + '\"' + dim[1] + '\"' + " at row: " + dim[0], de);
        }

        return table;
    }

    /**
     * XXX NOTE: use 1 as the start point for reading a new one.
     */
    @SuppressWarnings("unused")
	protected void read(TableReadListener trl, int start, int rowlimit)
            throws DataIOException, IOException, DataParseException{

        String line;

        int lineno = 0;
        int recordno = 0;

        //--1. Merge the multiple line of SQL log. -----------------------------
        String templine=z_preline;

        String[] splitedline=null;

        while (((line=bufR.readLine())!=null) && (recordno < rowlimit) ){
            z_currentpoint++;
            if (z_currentpoint < start){
                continue;
            }
            // when reach start point, start to read and set lineno
            lineno++;

            //-- the sign of a new record.
            if (line.startsWith("yy") || line.startsWith("200") || line.startsWith("201")) {

                if (!templine.equals("")){

                    ++recordno;

                    //clean multiple whitespaces and tabs in each record for future processing
                    templine=templine.replaceAll(" {2,}", " ");
                    templine=templine.replaceAll("\t{1,}", "");

                    templine = CSVparser(templine);

                    //Read in values
                    if (!templine.equals("")){
                        splitedline=null;                   //clean this variable
                        splitedline=templine.split("\t");   //seperate each column
                        int cols=splitedline.length;
                        if (cols==21){          //Rigous set the column number
                            for (int j=0;j<cols;j++){           //read values in
                                trl.readValue(recordno, j+1, splitedline[j].trim());
                            }
                        } else {
//TODO: output error or wrong format records into a file
                            System.err.println("SDSSLogView.error(101): SDSS log file read in error"+templine);
                        }
                    }

                    //clean up this line
                    templine="";
                } else{
                    templine=templine+line;
                    continue;
                }
                templine=line; //After check if it is new row, store new line into templine
            } else
                templine=templine + line + SDSSLOGVIEWCONSTANTS.LINEBREAKER;
        }   //end of while loop

        z_preline = templine;
        //z_currentpoint += lineno;

        //process the last line of record which is stored in the templine string
        if (!templine.equalsIgnoreCase("") && (z_currentpoint==z_nolines)){

            templine=templine.replaceAll(" {2,}", " ");
            templine=templine.replaceAll("\t{1,}", "");
            
            templine = CSVparser(templine);

            //Read in values
            if (!templine.equalsIgnoreCase("")){
                splitedline=null;                   //clean this variable
                splitedline=templine.split("\t");   //seperate each column
                int cols=splitedline.length;
                if (cols==21){          //Rigous set the column number
                    ++recordno;
                    for (int j=0;j<cols;j++){           //read values in
                        trl.readValue(recordno, j+1, splitedline[j].trim());
                    }
                } else System.err.println("SDSSLogViewer Reader Error(01), not an"
                        + "SDSS Log record:" + line);
            }
        }   //end of processing last line of record

    }

    /**
     * check if file has been scanded.<p/>
     */
    public boolean isScanned(){
        return z_isScanned;
    }

    /**
     * Nov. 14, 2010:    Test Opencsv package for the CSV
     *                   Use it instead of original parseRecords()
     * @param line
     * @return
     */
    protected String CSVparser(String line){

        CSVParser parser = new CSVParser(',', '"');
        String[] col = null;
        try {
            col = parser.parseLine(line);
        } catch (IOException ex) {
            System.err.println("CSV parser error(1001): " + line);
        }

        String output = "";
        if (col !=  null){
            for (int i=0;i<col.length;i++){
                output += col[i] + "\t";
            }
            output.trim();
        }
        return output;
    }


    @Override
    protected void read(InputStream in, TableReadListener tl) throws IOException, DataParseException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}

