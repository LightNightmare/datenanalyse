package sdsslogviewer.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.util.PrefuseLib;
import sdsslogviewer.Event.DataTableListener;

import sdsslogviewer.Util.timeUtil;
import sdsslogviewer.data.io.SDSSLogFileReader;

/**
 * <p>Class to manage the three part of log data. A limit number to see how many rows
 * could be in one block. </p>
 * <p> A total of three blocks is used. One block for viz; one as previous N records
 * and one as next N records. Users can proceed or go back to explore the data.</p>
 * <p>Default limit is set as 50,000 rows. This value could be change in constructor.</p>
 *
 * <p>This class should take in charge of the number of blocks and give the info
 *  to caller of the manager. Meanwhile also can help caller<p/><p/>
 * Dec 31st, 2010:  Class created to manage SDSS Log File bufferred data<p/>
 *
 * Jan. 4th, 2011:  separate the detailed file function from SDSSLogFileReader.<p/>
 *
 * Jan. 5th, 2011:  Dr. Chen suggested to divid large file into smaller ones and
 *                  manage them here.<p/>
 *                  Good suggestion!!<p/>
 *      So now, I have two methods for reading blocks of SDSS SQL log files:<p/>
 *      1. Use SDSSLogFileReaderPlus;<p/>
 *      2. Physically divid large file into smaller ones.<p/>
 *      Method 1 is more flexilble than method 2. Could read any size of data if
 *      the computer is advanced enough<p/>
 *      Method 2 is much easier than method 1 and easy to control. But not flexible
 *      I will use this method first.<p/>
 *
 * Jan. 17th, 2011:  Add new func to check if a file is smaller than row limit.
 *                   If smaller, not divid and copy, just build a file list with
 *                   one file and read the file for use.<p/>
 *
 * April 13th, 2011: Add new func to allow user specify conditions for reading data<p/>
 * @author JZhang<p/>
 */

public class SDSSLogFileManager {
    /**Control variable*/
    private int z_rowlimit = 50000;

    /**File information variable*/
    private File z_logfile;
    private String z_datalocation;

    private int[] z_rows_records;
    @SuppressWarnings("rawtypes")
	private ArrayList z_datafiles;

    /**Data variables*/
    private int z_currentdatafilenumber;

    private Table z_vizTable = null;
//    private Table z_preTable;
//    private Table z_nextTable;
    private SDSSLogFileReader z_reader;

    private boolean z_isSmallEnough = false;

    private boolean z_hasPre = false;
//                    z_isPreAvailable = false;
    private boolean z_hasNext = false;
//                    z_isNextAvailable = false;

    /**Manager status*/
    private String z_status;

    /**Event list*/
    private EventListenerList z_listeners;

    /** 
     * Construct a manager with given file and use default row limit, 50000.<p/>
     */
    public SDSSLogFileManager(File f) throws DataIOException, IOException{
        this(f, 100000);
    }

    /**
     * Construct a manager with given file and given row limit number.<p/>
     * @param f, an SDSS SQL log file in CSV format<p/>
     * @param rows, limit of rows will be stored in a block for viz.<p/>
     */
    public SDSSLogFileManager(File f, int rows) {
        z_logfile = f;
        z_rowlimit = rows;
        z_listeners = new EventListenerList();
        z_reader = new SDSSLogFileReader();
    }

    /**
     * Method to initialize this manage. Use another thread to process the initialization.<p/>
     * @throws DataIOException
     * @throws IOException 
     * @throws InterruptedException 
     */
    public void initManager() throws DataIOException, IOException, InterruptedException{
        //-- . Start a new thread to get the
        initManager.start();
        //initManager.join();
    }

    private void scanFile() throws DataIOException, IOException{
        z_status = "Start scaning the file";
        long time1 = System.currentTimeMillis();
        fireStatusChangeEvent();    //fire status change event, let parent class
                                    //to know what happens.
        z_rows_records = scan();

        long time2 = System.currentTimeMillis();

        int[] runtime = timeUtil.getRunningTime(time1, time2);
        z_status = "Scan finished. It took " + runtime[0] + " mintues and " +
                runtime[1] + " seconds";
        fireStatusChangeEvent();

        boolean isSuccessful = false;
        System.out.println("Total Number of tuples: #"+z_rows_records[1]);
        if (z_rows_records[1] > z_rowlimit){
            z_status = "Dividing the file into smaller blocks....";
            fireStatusChangeEvent();
            isSuccessful = SDSSLogCSVFileProcesser.divideFile(z_logfile, z_rowlimit);
        } else {
            isSuccessful = true;
            z_isSmallEnough = true;
        }

        if (isSuccessful){
            z_status = "Finish dividing. Start to create data block list...";
            fireStatusChangeEvent();
        } else {
            z_status = "Dividing the file failed. Please try do it again.";
            fireStatusChangeEvent();
        }

    }

    /**
     * method to read the file list in the folder created by divideFile() method.
     * Browsing the folder and create a list.
     * OPTIONAL: could check with default definition to see if the file in right
     *           naming rule.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void createFileList(){
        //--. check if the file is smaller than row limit. If smaller, just add
        //    the file into the file list.
        if (z_isSmallEnough){
            z_datafiles = new ArrayList();
            z_datafiles.add(z_logfile);
            z_isSmallEnough = false;
        } else {                                //if large, get the divid files
            String name = z_logfile.getName();
            String parentdir = z_logfile.getParent();

            name = name.substring(0, name.length()-4);
            z_datalocation = parentdir + File.separator + name;
            File filefolder = new File(z_datalocation);
            File[] files = null;

            //-- If the folder was not there, send an alter. But should not happen
            if (!filefolder.exists()){
                //should not happen
                z_status = "File folder does not exist. Please check any damage.";
                fireStatusChangeEvent();
                //if happen in case, set file list to null so next method know sth is wrong
                z_datafiles = null;
            } else {
                files = filefolder.listFiles();
                z_datafiles = new ArrayList();
            }
            z_datafiles.addAll(Arrays.asList(files));

            Collections.sort(z_datafiles, new FileNameComparator());
        }  // end if

        z_status = "Finish block list creation. Start to read in first block data...";
        fireStatusChangeEvent();

    }

    /**
     * After data file scanned, data list built, read in the first two blocks.
     * Read in first block in the main thread, then
     * read in the second block in another thread. When the second block is read
     * in, set hasNext true;
     */
    private void readInitialFile(){
        //-- . Check if the data file list is empty. If not go ahead to read in
        if (z_datafiles != null){
            try {
                z_currentdatafilenumber = 0;
                z_vizTable = null;
                z_vizTable = z_reader.readTable((File) z_datafiles.get(z_currentdatafilenumber));
                z_reader = null;
                z_reader = new SDSSLogFileReader();
                System.gc();
                Runtime.getRuntime().gc();
//System.out.println(PrefuseLib.getMemoryUsageInMB());
            } catch (DataIOException ex) {
                z_status= "Data IO exception occurred. Please choose the next block.";
                fireStatusChangeEvent();
            }
        }
        //-- . After read in the current table, start another thead to read in others
        if (z_currentdatafilenumber + 1 < z_datafiles.size()){
            z_hasNext = true;
        }

        z_status = "Finish first block data read in.";
        fireStatusChangeEvent();

    }

    /**
     * Return a set of data table for outsiders.<p/>
     * @return 
     */
    public Table getVizTable(){
        //trigger pointer to move
        return z_vizTable;
    }

    /**
     * Return the previous table used by outsiders.<p/>
     */
    public void getPreTable(){
        if (z_hasPre){
            z_status = "Reading in the previous block of data ...";
            fireStatusChangeEvent();
            z_currentdatafilenumber--;
            Thread rt = new Thread(new readTable());
            rt.start();
        }
    }

    /**
     * Return the next table for outsiders.<p/>
     * @throws InterruptedException 
     */
    public void getNextTable() throws InterruptedException{
        if (z_hasNext){
            z_status = "Reading in the next block of data ...";
            fireStatusChangeEvent();
            z_currentdatafilenumber++;
            Thread rt = new Thread(new readTable());
            rt.start();
            //rt.join();
        }
    }

/*    public boolean isPreAvailable(){
        return z_isPreAvailable;
    }

    public boolean isNextAvailable(){
        return z_isNextAvailable;
    }
*/

    /**
     * Check if there is a previous table.<p/>
     * @return 
     */
    public boolean hasPreTable(){
        return z_hasPre;
    }

    /**
     * Check if there is a next table.<p/>
     * @return 
     */
    public boolean hasNextTable(){
        return z_hasNext;
    }

    /**
     * Get the current running status of the file manager<p/>
     * @return status string for other classes to display<p/>
     */
    public String getStatus(){
        return z_status;
    }

    /**
     * get the total number of rows in the CSV file.<p/>
     * @return 
     */
    public int getRowNo(){
        return z_rows_records[0];
    }

    /**
     * get the total number of blocks<p/>
     * @return
     */
    public int getBlockNo(){
        return z_datafiles == null ? 0 : z_datafiles.size();
    }

    /**
     * Get the current file number.<p/>
     * @return int the current file number<p/>
     */
    public int getCurrentFileNumber(){
        return z_currentdatafilenumber;
    }

    /**
     * Set the current data file number, so that caller could control the reading
     * in position.<p/>
     *
     * @param fn, the file number. It must > 0 and < the z_datafiles.size()<p/>
     * @return if legible return true, else false;<p/>
     */
    public boolean setCurrentFileNumber(int fn){
        if ((fn >= 0) && fn < z_datafiles.size()){
            z_currentdatafilenumber = fn;
            if (z_currentdatafilenumber > 0) z_hasPre = true;
            if (z_currentdatafilenumber < z_datafiles.size()-1) z_hasNext = true;
            return true;
        } else
            return false;
    }

    /**
     * scan the sdss sql log csv file<p/>
     *
     * @return return an int array
     * int[0]: the number of lines;
     * int[1]: the number of sdss log records;
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    private int[] scan() throws FileNotFoundException, IOException{

        BufferedReader bufR = new BufferedReader(new FileReader(z_logfile));

        String line = "";
        line = bufR.readLine(); //read header line

        String templine = "";
        int lineno = 0;
        int recordno = 0;

        while (((line=bufR.readLine())!=null)){
            lineno++;
           //-- the sign of a new record.
            if (line.startsWith("yy") || line.startsWith("200") || line.startsWith("201")) {
                //-- a new record, now see the buffered line
                if (!templine.equals("")){
                    recordno++;
                    //clean up this line
                    templine="";
                } else{
                    templine=templine+line;
                    continue;
                }

                templine=line; //After check if it is new row, store new line into templine

            } else
                templine=templine + line + " ";
        }   //end of while loop

        if(!templine.equalsIgnoreCase("")) recordno++;

        int info[] = new int[]{0,0};
        info[0] = lineno;
        info[1] = recordno;

        bufR.close();

        return info;
    }

    /**add change listeners*/
    public void addStatusChangeListener(ChangeListener ce){
        z_listeners.add(ChangeListener.class, ce);
    }

    /**remove change listeners*/
    public void removeStatusChangeListener(ChangeListener ce){
        z_listeners.remove(ChangeListener.class, ce);
    }

    /**fire change event*/
    public void fireStatusChangeEvent(){
        Object[] listeners = z_listeners.getListenerList();

        int numlistener = listeners.length;
        ChangeEvent ce = new ChangeEvent(this);
        for (int i=0;i<numlistener;i+=2){
            if (listeners[i] == ChangeListener.class){
                ((ChangeListener)listeners[i+1]).stateChanged(ce);
            }
        }
    }

    /** add a new data table listener*/
    public void addDataTableListener(DataTableListener dtl){
        z_listeners.add(DataTableListener.class, dtl);
    }

    /** remove a data table listener */
    public void removeDataTableListener(DataTableListener dtl){
        z_listeners.remove(DataTableListener.class, dtl);
    }

    /**
     * Fire this event to indicate listeners that the request data table is ready
     * @throws InterruptedException 
     */
    public void fireTableIsReadyEvent() throws InterruptedException{
        Object[] listeners = z_listeners.getListenerList();

        int numlistener = listeners.length;
        for (int i=0;i<numlistener;i+=2){
            if (listeners[i] == DataTableListener.class){
                ((DataTableListener)listeners[i+1]).TableIsReady();
            }
        }
    }

    /**
     * The two new threads to run scan table and run read in table.
     */
    Thread initManager = new Thread(new Runnable(){
        public void run() {
            try {
                //-- . Scan the CSV file, if too big, divid into smaller ones.
                scanFile();
                createFileList();
                readInitialFile();

                fireTableIsReadyEvent();    //tell caller data is ready

            } catch (DataIOException ex) {
                z_status= "Data IO error occurred. Please choose the next block.";
                fireStatusChangeEvent();
            } catch (IOException ex) {
                z_status= "Data IO error occurred. Please choose the next block.";
                fireStatusChangeEvent();
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    });


    private class readTable implements Runnable{

        public void run() {
        try {
            z_vizTable = null;
            System.gc();
            Runtime.getRuntime().gc();
            z_vizTable = z_reader.readTable((File) z_datafiles.get(z_currentdatafilenumber));
            z_reader = null;
            z_reader = new SDSSLogFileReader();
            System.gc();
            Runtime.getRuntime().gc();
System.out.println(PrefuseLib.getMemoryUsageInMB());
        } catch (DataIOException ex) {
            z_status= "Data IO exception occurred. Please choose the next block.";
            fireStatusChangeEvent();
        }

        //-- . After read in the current table, check if has next or previous
        if (z_currentdatafilenumber + 1 < z_datafiles.size()){
            z_hasNext = true;
        } else {
            z_hasNext = false;
        }
        if (z_currentdatafilenumber > 0){
            z_hasPre = true;
        } else {
            z_hasPre = false;
        }

        //-- . Tell caller, the table is ready.
        try {
			fireTableIsReadyEvent();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    }

    //------- new inner classes ------------------------------------------------
    @SuppressWarnings("rawtypes")
	private class FileNameComparator implements Comparator{

        public int compare(Object o1, Object o2) {
            File f1 = (File) o1;
            File f2 = (File) o2;

            String name1 = f1.getName();
            String name2 = f2.getName();

            name1 = name1.substring(name1.lastIndexOf("_")+1, name1.lastIndexOf("."));
            name2 = name2.substring(name2.lastIndexOf("_")+1, name2.lastIndexOf("."));

            int i1 = Integer.parseInt(name1);
            int i2 = Integer.parseInt(name2);

            if (i1 > i2){
                return 1;
            } else {
                if (i1 < i2){
                    return -1;
                } else
                    return 0;
            }

        }

    }
}
