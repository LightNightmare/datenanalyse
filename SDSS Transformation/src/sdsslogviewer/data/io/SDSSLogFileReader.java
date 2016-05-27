package sdsslogviewer.data.io;

import au.com.bytecode.opencsv.CSVParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.parser.ParserFactory;
import prefuse.data.io.AbstractTextTableReader;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.DataParser;
import prefuse.data.parser.TypeInferencer;
import prefuse.util.io.IOLib;

import sdsslogviewer.SDSSLOGVIEWCONSTANTS;

/**
 * <p>A simple version of prefuse.data.io.CSVTableReader to increase performance
 * by knonwing the default data fields, column names, and data types.
 * </p>
 * <p>Remove all irregular records or wrong format values. Or set a default value
 * to the target column.
 * </p>
 * Use it to read SDSS log from local file system.<p/>
 * Sept. 2nd, 2010: Create own parser to deal with errors and irregular CSV format<p/>
 *
 * Nov. 14, 2010:   Revise the original CSVReader and solve memory problem.<p/>
 *                  Use String as input, instead of inputstream.<p/>
 *                  Now, can handle ~1 million log rows, ~=300MB file.<p/>
 *
 * @author JZhang<p/>
 */
public class SDSSLogFileReader extends AbstractTextTableReader {

    private ParserFactory m_pfactory = ParserFactory.getDefaultFactory();

    /**
     * Construct an SDSSLogFileReader.<p/>
     */
    public SDSSLogFileReader(){
        super();
    }

    /**
     * Construct an SDSSLogFileReader with specified parserFactory.<p/>
     * @param parserFactory - parser Factory to process each cell<p/>
     */
    public SDSSLogFileReader(ParserFactory parserFactory){
        super(parserFactory);
    }

    @SuppressWarnings({ "rawtypes", "unused" })
	@Override
    /**
     * Replace AbstractTextTableReader's readTable() method.<p/>
     * Override the AbstractTableReader's readTable() method.<p/>
     * The original method will store the input stream twice, therefore have severe
     * memory leak problem. The new readTable method only store once, hence greatly
     * increase the useful memory.<p/>
     */
    public Table readTable(String location) throws DataIOException {

        // determine input stream capabilities
        // if we can't reset the stream, we read in all the bytes
        // and make our own local stream
        // Nov. 14, James: in my case read entity.
        InputStream is =  null;
        try {
            is  = IOLib.streamFromString(location);
        } catch (IOException ex) {
            Logger.getLogger(SDSSLogFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        final TypeInferencer di = new TypeInferencer(m_pfactory);
        final ArrayList headers = getColumnNames();
        final int[] dim = new int[] { 0, 0 };

        TableReadListener scanner = new TableReadListener() {
            int prevLine = -1;
            @SuppressWarnings("unchecked")
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
                        if(dim[0]%500 == 0) System.out.println(dim[0]);
                    }
                } else if ( line == 1 && true ) {
                    headers.add(value);
                }

                // update num cols
                if ( col > dim[1] )
                    dim[1] = col;
            }
        };

        // do a scan of the stream, collecting length and type data
        try {
            read(is, scanner);
            is.close();
        } catch ( IOException ioe ) {
            throw new DataIOException(ioe);
        } catch ( DataParseException de ) {
            // can't happen
        }

        // create the table
        int nrows = dim[0];
        int ncols = dim[1];
        final Table table = new Table(nrows, ncols);

        // create the table columns
        for ( int i=0; i < ncols; ++i ) {
            String header;
            if ( true || i < headers.size() ) {
                header = (String)headers.get(i);
            } else {
                header = getDefaultHeader(i);
            }
            table.addColumn(header, di.getType(i));
            table.getColumn(i).setParser(di.getParser(i));
        }

        // reset dim array, will hold row/col indices
        dim[0] = dim[1] = -1;

        TableReadListener parser = new TableReadListener() {
            int prevLine = -1;
            public void readValue(int line, int col, String value)
                throws DataParseException
            {
                // early exit on header value
                if ( line == 1 && true )
                    return;
                if ( line != prevLine ) {
                    prevLine = line;
                    ++dim[0];
                }
                dim[1] = col-1;

                // XXX NOTE-2005.08.29-jheer
                // For now we use generic routines for filling column values.
                // This results in the autoboxing of primitive types, slowing
                // performance a bit and possibly triggering avoidable garbage
                // collections. If this proves to be a problem down the road,
                // we can add more nuance later.
                DataParser dp = di.getParser(dim[1]);
                table.set(dim[0], dim[1], dp.parse(value));
            }
        };

        // read the data into the table
        try {
            // read the data
            is = IOLib.streamFromString(location);
            read(is, parser);
            is.close();
        } catch ( IOException ioe ) {
            throw new DataIOException(ioe);
        } catch ( DataParseException de ) {
            throw new DataIOException("Parse exception for column "
                    + '\"' + dim[1] + '\"' + " at row: " + dim[0], de);
        }

        return table;
    }


    @Override
    /**
     * Note:  This method is called twice in super AbstractTextTableReader class.<p/>
     * The first round is to scan for the number of columns and rows.<p/>
     * The second round is actually to read in data.<p/>
     *
     */
    protected void read(InputStream is, TableReadListener trl)
            throws IOException, DataParseException {

        String line;

        BufferedReader bufR=new BufferedReader(new InputStreamReader(is));

        int lineno = 0;

        //--1. Merge the multiple line of SQL log. -----------------------------
        String templine="";
        String[] splitedline=null;

        while ((line=bufR.readLine())!=null){
           //-- the sign of a new record.
            //System.out.println(line);
            if (line.startsWith("yy") || line.startsWith("200") || line.startsWith("201")) {
                //System.out.println(Arrays.asList(templine));
                if (!templine.equals("")){
                    //clean multiple whitespaces and tabs in each record for future processing
                    templine=templine.replaceAll(" {2,}", " ");
                    templine=templine.replaceAll("\t{1,}", "");

                    //Format each record into a Tab-seperated line for read in values
//                    templine=parseRecord(templine);
                    templine = CSVparser(templine);
                    //System.out.println(Arrays.asList(templine));

                    //Read in values
                    if (!templine.equals("")){
                        splitedline=null;                   //clean this variable
                        splitedline=templine.split("\t");   //seperate each column
                        //System.out.println(Arrays.asList(splitedline));
                        int cols=splitedline.length;
                        if (cols==21){          //Rigous set the column number
                            ++lineno;
                            //System.out.println(lineno);
                            for (int j=0;j<cols;j++){           //read values in
                                trl.readValue(lineno, j+1, splitedline[j].trim());
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

        //process the last line of record which is stored in the templine string
        if (!templine.equalsIgnoreCase("")){
            ++lineno;
            templine=templine.replaceAll(" {2,}", " ");

            //Format each record into a Tab-seperated line for read in values
//            templine=parseRecord(templine);
            templine = CSVparser(templine);

            //Read in values
            if (!templine.equalsIgnoreCase("")){
            	//System.out.println(templine);
                splitedline=null;                   //clean this variable
                splitedline=templine.split("\t");   //seperate each column
                int cols=splitedline.length;
                if (cols==21){          //Rigous set the column number
                    ++lineno;
                    for (int j=0;j<cols;j++){           //read values in
                        trl.readValue(lineno, j+1, splitedline[j].trim());
                    }
                } else System.out.println(line);
            }
        }   //end of processing last line of record

        bufR.close();
    }   //end of read() method

    /**
     * <p>To deal errors and irregular log records. Subclass could override this
     * parser with other CSV identification algorithm
     * </p>
     * @param inputline, the original SDSS log record in CSV format<p/>
     * @return a line of string separated with Tab as delimited mark<p/>
     *
     * @TODO: Judge if commas exist in statement without \" to enbrace.<p/>
     * @TODO: Judge if \" exists in SQL statements without comma<p/>
     *
     * NOTE:Replaced by CSVParser.<p/>
     */
    protected String parseRecord(String inputline){
        String output = ""; //output string
        String templine=inputline;

        String statement=null,firstpart="",lastpart="";
        String[] splitedline,tempsplitedline;

        int i, firstquota=Integer.MIN_VALUE, lastquota=Integer.MIN_VALUE;

        //Format each record into a Tab-seperated line for read in values
        try
            {
            //Check for existance of quate marks
            if (templine.indexOf("\"")!=-1) {
                //has at least one quote mark
                firstquota=templine.indexOf("\"");      //First \"
                lastquota=templine.lastIndexOf("\"");   //Second \"

                if (firstquota<lastquota) {             //has at least two \"
                    try {
                        //use the first and last quotation mark to separate statement part out
                        firstpart=templine.substring(0, firstquota);
                        statement=templine.substring(firstquota+1, lastquota);
                        lastpart=templine.substring(lastquota+1, templine.length());

                        //seperate all columns with \t and merge them again
                        templine="";      //Clean this variable for recreatation
                        tempsplitedline=firstpart.split(",");
                        for (i=0;i<tempsplitedline.length;i++)
                            templine=templine+tempsplitedline[i]+"\t";
                        templine=templine+statement+"\t";
                        tempsplitedline=lastpart.split(",");
                        for (i=1;i<tempsplitedline.length;i++)
                            templine=templine+tempsplitedline[i]+"\t";

                        }catch (ArrayIndexOutOfBoundsException e) {
                        //warning of Wrong format in quate marks.
                        //particularly for less columns than general
                        System.out.println("Wrong formatted row! "+templine);
                            templine="";
                        }
                    }else {
                        //warning of Wrong format in quate marks.
                        //particularly for error of one quate mark only
//                        System.out.println("Wrong formatted row! in "+templine);
                        templine="";
                    }
                }else {
                    //No \", just split with commas
                    splitedline=templine.split(",");
                    if (splitedline.length == 21){
                        templine="";      //Clean this variable for recreatation
                        for (i=0;i<splitedline.length;i++)
                            templine=templine+splitedline[i]+"\t";
                    } else{
                        System.out.println("Wrong formatted row! "+
                               "Commas in statement without quote marks"
                               +"\n"+templine);
                        templine="";  //if sth wrong, clean this row and not read value
                    }
                }
            }catch (ArrayIndexOutOfBoundsException ex){
                //Warning of wrong format in log record
                //particularly for error of less or more columns
//                System.out.println("Wrong formatted row! in "+templine);
                templine="";  //if sth wrong, clean this row and not read value
            }

        output=templine.trim();
        templine = null;
        return output;
    }


    /**
     * Nov. 14, '10:    Test Opencsv package for the CSV<p/>
     * @param line - one line of SDSS log data
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


}
