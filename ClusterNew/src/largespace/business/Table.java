package largespace.business;
import java.util.ArrayList;;
public class Table {
	public String Name = "";
	public long Count = 0;
	public ArrayList<String> Links = new ArrayList<String>();
	
	public Table()
	{}
	
	public Table (String[] vals)
	{
		int tblpropertySize = vals.length;
		if (tblpropertySize >= 4)
		{
			Name = vals[0];
			Count = Long.parseLong(vals[1]);
			int i = 3;
			while (i < tblpropertySize)
			{
				Links.add(vals[i]);
				i++;
			}
		}
			
	}
}
