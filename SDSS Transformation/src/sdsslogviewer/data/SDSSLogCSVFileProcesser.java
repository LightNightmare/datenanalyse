package sdsslogviewer.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.PrintWriter;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;

import sdsslogviewer.data.io.SDSSLogFileReader;
import sdsslogviewer.data.io.SDSSLogFileWriter;

/**
 * A class to pre-process SDSS log file.<p/>
 *      From              CSV --> TXT file<p/>
 *      From Original Columns --> New Columns + seq column<p/>
 *
 * NOTE: Seq column works as an index for inner join multiple tables.<p/>
 *
 * CAUTION: the Integer.MIN_VALUE and Double.MIN_VALUE cannot be imported into DB2<p/>
 *
 * Jan. 6th 2011: revise to static methods for directly use just like prefuse.data.io.CSVTextFileReader.<p/>
 * @author James<p/>
 */
public class SDSSLogCSVFileProcesser {

    private final static String POSDIX = ".csv";

    /**
     * Static method to divid a large SDSS .csv log file into a set of small .csv
     * files in a folder named after the .csv file's name.<p/>
     * @param csvfile - the SDSS .csv log file<p/>
     * @param rowlimit - the number of row that each small .csv file will contain<p/>
     * @return a if the divide action is successful or not<p/>
     */
    public static boolean divideFile(File csvfile, int rowlimit) {

        boolean isSuccessful = true;
        try {
            //-- . get the file information and create a folder in the current folder
            //     named after the given file. If failed return a false;
            String location = csvfile.getParent();
            String name = csvfile.getName();
            name = name.substring(0, name.length()-4);

            String newfolder = location + File.separator + name;

            File filefolder = new File(newfolder);
            boolean isCreated = false;

            if(!filefolder.exists()){
                isCreated = filefolder.mkdir();
            } else {
                return true;
            }

            if (!isCreated){    //if cannot create the folder, return false
                return false;
            }

            int fileno = 1;
            File smallerfile = new File(newfolder + File.separator + name + "_" + fileno + POSDIX);
            PrintWriter pWriter = new PrintWriter(new BufferedWriter(new FileWriter(smallerfile)));

            //-- . If create the folder, start to read file and divid into smaller.
            BufferedReader bufReader = new BufferedReader(new FileReader(csvfile));
            String line, templine = "", header;
            int recordno = 0;

            header = bufReader.readLine();
            pWriter.println(header);

            while((line = bufReader.readLine())!=null){
                if (line.startsWith("200") || line.startsWith("201")) {
                //-- a new record, now see the buffered line
                if (!templine.equals("")){
                    recordno++;
                    if (recordno < rowlimit){
                        pWriter.println(templine);
                    } else {
                        pWriter.println(templine); //output the last line
                        pWriter.close();           //close old writer;
                        recordno = 0;              //set record to 0;
                        fileno++;                  //create a new file and writer
                        smallerfile = new File(newfolder + File.separator + name + "_" + fileno + POSDIX);
                        pWriter = new PrintWriter(new BufferedWriter(new FileWriter(smallerfile)));
                        pWriter.println(header);   //output header line
                    }

                    //clean up this line
                    templine="";
                } else{
                    templine=templine+line;
                    continue;
                }
                templine=line; //After check if it is new row, store new line into templine
            } else
                templine=templine + line + " ";
            } //end of while

            //-- . output the last record.
            if (!templine.equalsIgnoreCase("")){
                pWriter.println(templine);
            }

            bufReader.close();
            pWriter.close();

        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ioe){
            return false;
        }

        return isSuccessful;
    }

    /**
     * A static method to convert SDSS .csv file into processed file that could
     * be used to load into DB2 database.<p/>
     * @param csvfile - the SDSS .csv log file<p/>
     * @return a boolean value to indicate if the action is successful or not<p/>
     */
    public static boolean derivedFile(File csvfile){
        boolean isSuccessful = true;

        String DBfile = csvfile.getPath();
        try {
            FileInputStream fins = new FileInputStream(csvfile);
            Table t = new SDSSLogFileReader().readTable(fins);
            SDSSLogTable st = new SDSSLogTable(t);

            DBfile = DBfile.replaceAll("\\.csv|\\.CSV", "_derived.txt");
            File outputfile = new File(DBfile);
            if (!outputfile.exists()){
                outputfile.createNewFile();
            }
            FileOutputStream fops = new FileOutputStream(outputfile);
            SDSSLogFileWriter output = new SDSSLogFileWriter(false);
            output.writeTable(st.convertToDBOutput(), fops);

        } catch (DataIOException ex) {
            ex.printStackTrace();
        } catch (IOException ioe){
            System.err.println("Can not create a file.");
        }

        return isSuccessful;
    }

}
