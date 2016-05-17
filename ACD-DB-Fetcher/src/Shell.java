import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class Shell {

	public static void main(String... args) {

		/******
		 * Try connecting to the server.
		 ******/

		Connection connection = null;
		ResultSet result = null;
		ResultSetMetaData rsmd = null;
		String SQL_DISTRIBUTION = "SELECT YY, MM, COUNT(MM) " 
								+ "FROM PARSED_STATEMENTS "
								+ "WHERE YY >= 2003 AND YY <= 2005 "
								+ "GROUP BY YY, MM "
								+ "ORDER BY YY, MM";
		String SQL_DATA_SLICE_1 = "SELECT * FROM "
								+ "BDCOURSE.PARSED_STATEMENTS "
								+ "WHERE TRUNC(THETIME) >= TO_DATE('2003-04-01', 'YYYY-MM-DD') "
								//+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD')";
								+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD')";

		try {
			connection = DriverManager.getConnection("jdbc:oracle:thin:@marsara.ipd.kit.edu:1521:student", "bdcourse",
					"bdcourse");
			result = connection.prepareStatement(SQL_DATA_SLICE_1).executeQuery();
			rsmd = result.getMetaData();
			
			System.out.println("We got a result!");
			
			/******
			 * Print result 
			 ******/
			
			int colCount = rsmd.getColumnCount();
			for (int i = 1; i <= colCount; i++) {
				if (i > 1)
					System.out.print("; ");
				System.out.print(rsmd.getColumnName(i));
			}
			System.out.print("\n");
			while (result.next()) {
				for (int i = 1; i <= colCount; i++) {
					if (i > 1)
						System.out.print(",  ");
					String columnValue = result.getString(i);
					System.out.print(columnValue);
				}
				System.out.println("");
			}
			
			connection.close();
		} catch (SQLException e) {
			System.out.println("Could not establish server connection. " + e);
		}

		/******
		 * Try writing a CSV file. A tutorial can be found here:
		 * http://viralpatel.net/blogs/java-read-write-csv-file/
		 ******/

		String csv = "country-capital.csv";
		CSVWriter writer = null;

		try {
			File f = new File(csv);
			if(f.exists() && !f.isDirectory()) { 
				System.out.println("Found existing file!");
				writer = new CSVWriter(new FileWriter(f),';');
			} else if (!f.exists() && !f.isDirectory()){
				System.out.println("File not found, creating...");
				f.createNewFile();
				writer = new CSVWriter(new FileWriter(f),';');
			} else {
				System.out.println("That is no file, it's a directory!");
			}

			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "India", "New Delhi" });
			data.add(new String[] { "United States", "Washington D.C" });
			data.add(new String[] { "Germany", "Berlin" });

			writer.writeAll(data);
			writer.close();
		} catch (IOException e) {
			System.out.println("Could not write file." + e);
		}
		
		
	}

}
