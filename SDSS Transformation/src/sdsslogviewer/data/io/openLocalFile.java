package sdsslogviewer.data.io;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Dec. 30, 2010: class created for opening a local sdss sql log file with a GUI
 * created by JFileChooser, and do data validation.<p/>
 * @author JZhang<p/>
 */
public class openLocalFile {

    private File SDSSLogCSVFile;

    /**
     * construct a file chooser windows and get csv files handler and check validation
     * <p/>
     * @throws FileNotFoundException
     * @throws IOException
     */
    public openLocalFile(Component parent) throws FileNotFoundException, IOException{
        JFileChooser jfc = new JFileChooser("Please choose SDSS SQL log CSV file");
        jfc.setFileFilter(new CSVFileFilter());

        int result = jfc.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION){
            SDSSLogCSVFile = jfc.getSelectedFile();
        } else
            SDSSLogCSVFile = null;
        //if not a sdss log csv, set to null and give a warning.
        if (!isValidFile(SDSSLogCSVFile)){
            SDSSLogCSVFile = null;
            JOptionPane.showMessageDialog(parent, "This is not a valid "
                    + "SDSS SQL Log file. Please choose a new file.",
                    "Not an SDSS SQL Log File", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Return the file for further processes, e.g. scaning and reading.<p/>
     * @return 
     */
    public File getFile(){
        return SDSSLogCSVFile;
    }

    /** this version just check the header line. Other method could override this
     *  method with more complex means.
     */
    protected boolean isValidFile(File f) throws FileNotFoundException, IOException{
        FileReader fr = new FileReader(f);
        LineNumberReader lnr = new LineNumberReader(fr);

        String header;
        header = lnr.readLine();
        if ((header.indexOf("yy")==-1)||(header.indexOf("mm")==-1)||
                (header.indexOf("statement")==-1)||(header.indexOf("rows")==-1)){
            lnr.close();
            fr.close();
            return false;
        } else{
            lnr.close();
            fr.close();
            return true;
        }
    } // end of isValidFile

    private class CSVFileFilter extends FileFilter{

        public final String CSV = "csv";

        public boolean accept(File pathname) {

            if (pathname.isDirectory()){
                return true;
            }

            String ext = null;
            String s = pathname.getName();
            int i = s.lastIndexOf(".");
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }

            if ((ext != null) && ext.equalsIgnoreCase(CSV)){
                return true;
            } else
                return false;
        }

        @Override
        public String getDescription() {
            return "Just CSV file";
        }

    }

}
