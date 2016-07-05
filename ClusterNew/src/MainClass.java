import com.beust.jcommander.JCommander;


import largespace.business.Loader;
import largespace.business.Options;
import largespace.clustering.DBScan;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        Options opt = new Options();
        new JCommander(opt, args);

        if (opt.FILE_TABLES != null) {
            opt.TABLES = new ArrayList<>();
            Scanner scanner = new Scanner(new File(opt.FILE_TABLES));
            while (scanner.hasNext()) {
                opt.TABLES.add(scanner.nextLine());
            }
            scanner.close();
        }

        if (opt.FILE_PRE_OUTPUT != null) {
            Loader.preprocess(opt.FILE_INPUT, opt.FILE_PRE_OUTPUT);
            opt.FILE_INPUT = opt.FILE_PRE_OUTPUT;
        }

        DBScan.mineClusters(opt);
        long endTime = System.currentTimeMillis();
        long runtime_seconds = (endTime - startTime)/1000;
        System.out.println("Clustering took " + runtime_seconds + " seconds.");
        System.out.println("Preprocessing was turned " + opt.PREPROCESS);
        System.out.println("(FILE_PRE_OUTPUT is " + opt.FILE_PRE_OUTPUT + ")");
    }
}
