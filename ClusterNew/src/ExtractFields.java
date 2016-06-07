import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class ExtractFields {
    public static void main(String[] args) throws Exception {
        String fileAllFields = args[0];
        String fileNumericalFields = args[1];
        String fileOutput = args[2];

        // read all fields
        ArrayList<String> allFields = new ArrayList<>();
        Scanner scanAll = new Scanner(new File(fileAllFields));
        while (scanAll.hasNext()) {
            allFields.add(scanAll.nextLine());
        }
        scanAll.close();

        // read numerical fields
        List<String> tmp = new LinkedList<>();
        Scanner scanNumerical = new Scanner(new File(fileNumericalFields));
        while (scanNumerical.hasNext()) {
            tmp.add(scanNumerical.nextLine().trim().toLowerCase());
        }
        scanNumerical.close();
        Set<String> numericalFields = new HashSet<>(tmp);

        // write output fields
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileOutput)));
        for (String f1 : allFields) {
            if (!numericalFields.contains(f1.trim().toLowerCase())) {
                writer.write(f1);
                writer.newLine();
            }
        }
        writer.close();
    }
}