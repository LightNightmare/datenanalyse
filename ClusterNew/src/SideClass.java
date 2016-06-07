import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.*;

import largespace.business.*;
import largespace.clustering.*;

public class SideClass 
{
	private static Pattern operatorRegex = Pattern.compile("\\s(\\<|\\>|\\<=|\\>=|=|\\<\\>)\\s");
	
	public static void main(String[] args)
	{
		//for (int i = 1; i <= 3575; i++)
		//	System.out.println(i);
		
		try
		{
			// read all fields
			Constants.TABLES = new ArrayList<String>();
			Scanner scanner = new Scanner(new File(args[0]));
			while (scanner.hasNext())
				Constants.TABLES.add(scanner.nextLine());
			scanner.close();
			
			String fileInput = args[1];
			String fileOutput = args[2];
			String delimiter = Constants.FIELD_DELIMITER;
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileOutput)));
			
			//System.out.println("max number of points = " + Constants.MAX_POINTS);
			Query[] data = Loader.readInputData(fileInput, delimiter);
			String[] tables = null;
			String[] clauses = null;
			String[] terms = null;
			Matcher m = null;
			String field = null;
			ArrayList<String> all = new ArrayList<String>();
			int dataLen = data.length;
			if (dataLen >  Constants.MAX_POINTS)
				dataLen = Constants.MAX_POINTS;
			System.out.println("number of points = " + dataLen);
			//Constants.NUM_POINTS = 5000;
			for (int i = 0; i < dataLen; i++)
			{
				//clauses = data[i].where.split("\\sAND\\s");
				//tables = data[i].from.split("\\,\\s");
				
				clauses = data[i].getWhereString().split("\\sAND\\s");
				tables = data[i].getFromString().split("\\,\\s");

				for (int j = 0; j < clauses.length; j++)
				{
					terms = clauses[j].split("\\sOR\\s");
					for (int k = 0; k < terms.length; k++)
					{
						m = operatorRegex.matcher(terms[k]);
						if (!m.find())
							System.out.println("\n" + terms[k] + "\n");
						field = terms[k].substring(0, m.start());
						if (tables.length == 1 && !field.contains("."))
							field = tables[0] + "." + field;
						
						if (!all.contains(field))
							all.add(field);
					}
				}
			}
			
			for (String s : all)
			{
				writer.write(s);
				writer.newLine();
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}