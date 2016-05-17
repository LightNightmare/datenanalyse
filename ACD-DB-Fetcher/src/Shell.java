import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
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
			 * Save result 
			 ******/
			String path = "whole-data-slice.csv";
			int colCount = rsmd.getColumnCount();
			List<String[]> buffer = new LinkedList<String[]>();
			int writtenRows = 0;
			final int bufferThreshold = 1000;
			
			String[] headers = new String[colCount];
			for (int i = 1; i <= colCount; i++) {
				headers[i-1] = rsmd.getColumnName(i);
			}
			buffer.add(headers);
			
			while (result.next()) {
				if (buffer.size() >= bufferThreshold) {
					writeBuffer(path, buffer);
					writtenRows += buffer.size();
					System.out.println(writtenRows + " rows written.");
					buffer = new LinkedList<String[]>();
				}
				
				String[] line = new String[colCount];
				for (int i = 1; i <= colCount; i++) {
					String columnValue = result.getString(i);
					line[i-1] = columnValue;
				}
				buffer.add(line);
			}
			
			writeBuffer(path, buffer);
			writtenRows += buffer.size();
			System.out.println(writtenRows + " rows written.");
			buffer = new LinkedList<String[]>();
			
			connection.close();
		} catch (SQLException e) {
			System.out.println("Could not establish server connection. " + e);
		}

		/******
		 * Try writing a CSV file. A tutorial can be found here:
		 * http://viralpatel.net/blogs/java-read-write-csv-file/
		 ******/

	}
	
	private static void writeBuffer(String path, List<String[]> buffer) {
		String csv = path;
		CSVWriter writer = null;

		try {
			File f = new File(csv);
			if(f.exists() && !f.isDirectory()) { 
				writer = new CSVWriter(new FileWriter(f, true),';');
			} else if (!f.exists() && !f.isDirectory()){
				f.createNewFile();
				writer = new CSVWriter(new FileWriter(f, true),';');
			} else {
				System.out.println("That is no file, it's a directory!");
			}

			writer.writeAll(buffer);
			writer.close();
		} catch (IOException e) {
			System.out.println("Could not write file." + e);
		}
	}

}
