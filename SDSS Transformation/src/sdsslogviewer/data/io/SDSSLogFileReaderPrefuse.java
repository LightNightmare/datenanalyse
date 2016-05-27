
package sdsslogviewer.data.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import prefuse.data.parser.ParserFactory;
import prefuse.data.io.AbstractTextTableReader;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;

/**
 * <p>A new version of prefuse.data.io.CSVTableReader by merging multiple lines
 * of log data into one line. And use CSVTableReader's read() method.
 * </p>
 *
 * <p>PROBLEM: Cannot handle the complexity of SDSS log file, e.g. multiple queto
 * marks in a SQL statement; or long log records that excessing the column number
 * SDSS Log record.
 * </p>
 * Therefore, replaced by SDSSLogFileReaderPlus.<p/>
 * @author JZhang<p/>
 */
public class SDSSLogFileReaderPrefuse extends AbstractTextTableReader {

    /**
     * Construct an SDSSLogFileReaderPrefuse.<p/>
     */
    public SDSSLogFileReaderPrefuse(){
        super();
    }

    /**
     * Construct an SDSSLogFileReaderPrefuse with given parserFactory.<p/>
     * @param parserFactory 
     */
    public SDSSLogFileReaderPrefuse(ParserFactory parserFactory){
        super(parserFactory);
    }

    @Override
    protected void read(InputStream is, TableReadListener trl)
            throws IOException, DataParseException {

        String line;

        BufferedReader bufR=new BufferedReader(new InputStreamReader(is));

        StringBuffer sbuf=new StringBuffer();
        boolean inRecord=false;
        int inQuote =   0;
        int col     =   0;
        int lineno  =   0;
        
        //--1. Merge the multiple line of SQL log. -----------------------------
        String templine=null;

        while ((line=bufR.readLine())!=null){
           //-- the sign of a new record.
            if (line.startsWith("yy") || line.startsWith("200") || line.startsWith("201")) {
                if (templine!=null){
                    //increment the line number
                    ++lineno;
                    //clean multiple whitespaces
                    templine=templine.replaceAll(" {2,}", " ");

                    //read this line into table
                    char[] c = templine.toCharArray();
                    int last = c.length-1;

                    /**
                     * This block of codes is directly copied from JHeer's
                     * CSVTableReader with a little change, replacing line with
                     * templine in this application
                     */
                    for (int i=0;i<last;i++){
                        if (!inRecord){
                            if (Character.isWhitespace(c[i])){
                                continue;
                            } else {
                                if (c[i]=='\"'){
                                    inRecord=true;
                                    inQuote=1;
                                }else {
                                    if (c[i]==','){
                                        String s = sbuf.toString().trim();
                                        trl.readValue(lineno, ++col, s);
                                        sbuf.delete(0, sbuf.length());
                                    } else {
                                        inRecord = true;
                                        sbuf.append(c[i]);
                                    }
                                }
                            }
                        } else {
                            if (inQuote==1){
                                if (c[i]== '\"' && (i==last || c[i+1] != '\"')){
                                    inQuote = 2;
                                } else {
                                    if (c[i] == '\"'){
                                        sbuf.append(c[i++]);
                                    } else {
                                        sbuf.append(c[i]);
                                    }
                                }
                            } else {
                                if (Character.isWhitespace(c[i])){
                                    sbuf.append(c[i]);
                                } else {
                                    if (c[i] != ',' && inQuote == 2){
//                                        throw new IllegalStateException (
//                                        System.out.println("Invalid data format. " + "Error at line "+
//                                                lineno + ", col "+ i + "\nLine contents: "+
//                                                templine);
                                    } else if (c[i] != ','){
                                        sbuf.append(c[i]);
                                    } else {
                                        String s = sbuf.toString().trim();
                                        trl.readValue(lineno, ++col, s);
                                        sbuf.delete(0, sbuf.length());
                                        inQuote = 0;
                                        inRecord = false;
                                    }
                                }
                            }
                        }
                    }   //end of for loop
                    //processing the rest of string
                    if (inQuote !=1) {
                        String s = sbuf.toString().trim();
                        trl.readValue(lineno, ++col, s);
                        sbuf.delete(0, sbuf.length());
                        inQuote = 0;
                        inRecord = false;
                    }
                    if (!inRecord && col > 0){
                        col = 0;
                    }

                    //clean up this line
                    templine=null;
                }
                templine=line; //After check if it is new row, store new line into templine
            } else
                templine=templine+line+"\n";
        }

        //process the last line of record which is stored in the templine string
        if (!templine.equalsIgnoreCase("")){
            ++lineno;
            templine=templine.replaceAll(" {2,}", " ");
            //read this line into table
            char[] c = templine.toCharArray();
            int last = c.length-1;

            for (int i=0;i<last;i++){
                if (!inRecord){
                    if (Character.isWhitespace(c[i])){
                        continue;
                            } else {
                            if (c[i]=='\"'){
                                inRecord=true;
                                inQuote=1;
                            }else {
                                if (c[i]==','){
                                    String s = sbuf.toString().trim();
                                    trl.readValue(lineno, ++col, s);
                                    sbuf.delete(0, sbuf.length());
                                } else {
                                    inRecord = true;
                                    sbuf.append(c[i]);
                                }
                            }
                        }
                    } else {
                        if (inQuote==1){
                            if (c[i]== '\"' && (i==last || c[i+1] != '\"')){
                                inQuote = 2;
                            } else {
                                if (c[i] == '\"'){
                                    sbuf.append(c[i++]);
                                } else {
                                    sbuf.append(c[i]);
                                }
                            }
                        } else {
                            if (Character.isWhitespace(c[i])){
                                sbuf.append(c[i]);
                            } else {
                                if (c[i] != ',' && inQuote == 2){
                                    throw new IllegalStateException (
                                           "Invalid data format. " + "Error at line "+
                                            lineno + ", col "+ i);
                                } else if (c[i] != ','){
                                    sbuf.append(c[i]);
                                } else {
                                    String s = sbuf.toString().trim();
                                    trl.readValue(lineno, ++col, s);
                                    sbuf.delete(0, sbuf.length());
                                    inQuote = 0;
                                    inRecord = false;
                                }
                            }
                        }
                    }
                }   //end of for loop
                //processing the rest of this string
                if (inQuote !=1) {
                    String s = sbuf.toString().trim();
                    trl.readValue(lineno, ++col, s);
                    sbuf.delete(0, sbuf.length());
                    inQuote = 0;
                    inRecord = false;
                }
                if (!inRecord && col > 0){
                    col = 0;
                }
            }   //end of processing last line of record
        }   //end of read() method

}
