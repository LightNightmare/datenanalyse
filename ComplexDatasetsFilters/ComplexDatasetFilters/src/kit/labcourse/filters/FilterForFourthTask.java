package kit.labcourse.filters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FilterForFourthTask {

	static BufferedWriter fourthTaskProcessed;
	
	public static void filter(BufferedReader fourthTaskNonprocessed) {
		String line = "";
		
		try {
			fourthTaskProcessed = new BufferedWriter (new FileWriter (new File(".\\fourthTaskFilteredStatements.csv")));
			boolean toBeRemoved = false;
			
			while((line = fourthTaskNonprocessed.readLine()) != null) {
				line = line.toLowerCase();
								
				if (line.contains("dbcolumns") || line.contains("dbobjectdescription") ||
						line.contains("dbobjects") || line.contains("dbviewcols") || line.contains("diagnostics") ||
						line.contains("filegroupmap") || line.contains("history") || line.contains("indexmap") ||
						line.contains("loadhistory") || line.contains("partitionmap") || line.contains("pubhistroy") ||
						line.contains("sitediagnostics")) {
					toBeRemoved = true;
				}
				
				if (!toBeRemoved) {
					fourthTaskProcessed.write(line);
					fourthTaskProcessed.write("\n");
				}
				
				toBeRemoved = false;
			}
			
		} catch (IOException e) {
			System.out.println("The writer for the filtered statements could not be opened!");
			e.printStackTrace();
		}
		
	}
}
