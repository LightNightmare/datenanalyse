package largespace.business;

import largespace.clustering.Column;
import largespace.clustering.Column.GlobalColumnType;
import largespace.clustering.DictionaryField;
import largespace.clustering.DistributedFieldWithEmissions;
import largespace.clustering.Predicate;
import largespace.clustering.ValueState;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
public class HttpURLConnectionExt {
	
	private String baseUrl = "http://skyserver.sdss.org/SkyserverWS/dr12/SearchTools/SqlSearch";
	private final String USER_AGENT = "Mozilla/5.0";
	private final int CorrectresponseCode = 200;
	
	public String prepareQuery(String cmd) {
		return cmd.replaceAll(" " , "%20");
	}
	
	public String GetParams(String cmd, String format) {
		return "?cmd="+cmd+"&format="+format;
	}
	
	public Table sendGetTableCount(String tableName) throws Exception {
		Table t = new Table();
		t.Name = tableName;
		String prepareCmd = prepareQuery("select count(1) from " + tableName);
		
        String url = baseUrl + GetParams(prepareCmd, "csv");
        
        URL obj = new URL(url);
        try {
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        //con.setReadTimeout(20000);
	
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();
	        
	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        while ((inputLine = in.readLine()) != null) {
		        	if (iLineCount == 2) {
		            response.append(inputLine);
		        	}
		        	iLineCount++;
		        }
		        in.close();
		
		        //print result
		        //System.out.println(response.toString());
		        try {
		        int foo = Integer.parseInt(response.toString());
		        t.Count = foo;
		        } catch(Exception ex) {
		        	System.out.println(ex);
		        }
	        }
        } catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + t.Name);
        	
     	   //return false;
     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + t.Name);
     		
     	   //return false;
     	}
        return t;
    }

	
	public long sendGetDistinctColumnCount(String tableName, String columnName, Options opt) throws Exception {
		long distColumnCount = -1;

		String prepareCmd = prepareQuery("select count(distinct " + columnName + ") from " + tableName);
        String url = baseUrl + GetParams(prepareCmd, "csv");
   
        URL obj = new URL(url);
        try
        {
        	HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        //we shouldn't set timeout here because of we need to know at any case
	        //Modified Readtimeout to 30s when getting distinct counts to try to reduce execution time. 
	        //There are a lot of distributed columns that cause a time out. If the query requires more than 30 seconds we assume it is distrubuted.
	        con.setReadTimeout(30000);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();

	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        while ((inputLine = in.readLine()) != null) {
		        	if (iLineCount == 2) {
		            response.append(inputLine);
		        	}
		        	iLineCount++;		        	
		        }
		        in.close(); 
		        
		        try {
		        	String respString = response.toString();
		        	if (respString.equals("")) {
		        		System.out.println("Didn't have distinct value for column " + columnName + "in table" + tableName);
		        	}
		        	else {
				        long foo = Long.parseLong(respString);
				        distColumnCount = foo;
		        	}
		        } catch(Exception ex) {
		        	System.out.println(ex);
		        }
	        }
        }
        catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + tableName + "." + columnName);
        	distColumnCount=-2;
        	return distColumnCount;
     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + tableName + "." + columnName);
     	   //return false;
     	}
        System.out.println("opt.COLUMNS_DISTRIBUTION.size() =  " + opt.COLUMNS_DISTRIBUTION.size());
        return distColumnCount;
    }
	
	public List<Long> sendGetMinMaxColumnFromId(String tableName, String columnName) throws Exception {
		List<Long> minMaxList = new ArrayList<Long>();

		String prepareCmd = prepareQuery("select min(" + columnName + "), max(" + columnName + ") from " + tableName);
        String url = baseUrl + GetParams(prepareCmd, "csv");
   
        URL obj = new URL(url);
        try {
        	HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        //we shouldn't set timeout here because of we need to know at any case
	        con.setReadTimeout(50000);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();

	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        while ((inputLine = in.readLine()) != null) {
		        	if (iLineCount == 2) {
		            response.append(inputLine);
		        	}
		        	iLineCount++;
		        }
		        in.close(); 
		        
		        try {
		        	String[] valueProperty = response.toString().split(",");
		        	long minValue = Long.parseLong(valueProperty[0]);
		        	long maxValue = Long.parseLong(valueProperty[1]);
		        	minMaxList.add(minValue);
		        	minMaxList.add(maxValue);
		        } catch(Exception ex) {
		        	System.out.println(ex);
		        }
	        }
        } catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + tableName + "." + columnName);
     	   //return false;
     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + tableName + "." + columnName);
     	   //return false;
     	}
        return minMaxList;
    }
	
	public List<Double> sendGetMinMaxColumnFromDistrField(String tableName, String columnName) throws Exception {
		List<Double> minMaxList = new ArrayList<Double>();

		String prepareCmd = prepareQuery("select min(" + columnName + "), max(" + columnName + ") from " + tableName);
        String url = baseUrl + GetParams(prepareCmd, "csv");
   
        URL obj = new URL(url);
        try {
        	HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        //we shouldn't set timeout here because of we need to know at any case
	        con.setReadTimeout(50000);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();

	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        while ((inputLine = in.readLine()) != null) {
		        	if (iLineCount == 2) {
		            response.append(inputLine);
		        	}
		        	iLineCount++;
		        }
		        in.close(); 
		        
		        try {
		        	String[] valueProperty = response.toString().split(",");
		        	Double minValue = Double.parseDouble(valueProperty[0]);
		        	Double maxValue = Double.parseDouble(valueProperty[1]);
		        	minMaxList.add(minValue);
		        	minMaxList.add(maxValue);
		        } catch(Exception ex) {
		        	System.out.println(ex);
		        }
	        }
        } catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + tableName + "." + columnName);
     	   //return false;
     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + tableName + "." + columnName);
     	   //return false;
     	}
        return minMaxList;
    }
	
	public Column sendGetColumnDistribution(String tableName, String columnName, Options opt) throws Exception {
		Column c = new Column(tableName + '.' + columnName);
		c.GlobalColumnType = GlobalColumnType.DictionaryField;
		c.Distribution = new DictionaryField();
		String prepareCmd = prepareQuery("select " + columnName + ", count(*) from " + tableName + " group by " + columnName + " order by " + columnName);
        String url = baseUrl + GetParams(prepareCmd, "csv");
   
        URL obj = new URL(url);
        try {
        	HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        con.setReadTimeout(50000);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();
	        
	        
	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        long accumulateCount = 0;
		        
		        boolean somethingwrong = false;
		       
		        
			        while ((inputLine = in.readLine()) != null) {
			        	if (iLineCount < 2) {}
			        	else  {
			        		String[] valueProperty = inputLine.split(",");
				        	String columnValue =valueProperty[0].toLowerCase();
				        	Long valueCount = Long.parseLong(valueProperty[1]);
				        	accumulateCount = accumulateCount + valueCount;
				        	try {
					        ((DictionaryField)c.Distribution).AddValue(columnValue, new ValueState(valueCount, accumulateCount, columnValue));
				        	} catch (Exception e) { }
		
			            response.append(inputLine + ";");
			        	}
			        	iLineCount++;
			        }
		        
			        
			        if (iLineCount <= 2)
			        	somethingwrong = true;
		        if (somethingwrong) {
		        	c.SomethingWrong = somethingwrong;
		        }
		        in.close(); 
	        }
        } catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + c.Name);

        	WritePenaltyColumn(opt, c);
        	
     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + c.Name);

     		WritePenaltyColumn(opt, c);
     	}
        System.out.println("opt.COLUMNS_DISTRIBUTION.size() =  " + opt.COLUMNS_DISTRIBUTION.size());
        return c;
    }
	
	public void WritePenaltyColumn(Options opt, Column c) throws Exception
	{
		Column c1 = opt.PENALTY_COLUMNS_DISTRIBUTION.get(c.Name);
		if (c1 == null) {
			
			opt.PENALTY_COLUMNS_DISTRIBUTION.put(c.Name, c);
			
			String columnspenOutputFile = opt.FILE_CLMN_OUTPUT.replaceAll(".csv", "_penalty.csv");
			File f = new File(columnspenOutputFile);
	    	if(f.exists() && !f.isDirectory()) { 
	    	    // do something
	    	}
	    	else {
	    		f.createNewFile();
	    		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(columnspenOutputFile)));
	    		writer.close();
	    	}
			BufferedWriter writer = new BufferedWriter(new FileWriter(columnspenOutputFile, true));
	
	        writer.write(c.Name);
	        writer.write(";");
	        writer.newLine();
	        writer.close();
		}
	}
	public Column sendGetColumnEmissions(String tableName, String columnName, Options opt, Table t) throws Exception {
		Column c = new Column(tableName + '.' + columnName);
		c.Distribution = new DistributedFieldWithEmissions();
		Long minValueToBeEmission = Math.round(t.Count * opt.MIN_PART_TO_BE_EMISSION);
		if (minValueToBeEmission == 0)
			minValueToBeEmission++;
		
		String prepareCmd = prepareQuery("select " + columnName + ", count(*) from " 
		+ tableName + " group by " + columnName 
		+ " having count(*) > " + minValueToBeEmission.toString() + " order by " + columnName);
        String url = baseUrl + GetParams(prepareCmd, "csv");
   
        URL obj = new URL(url);
        try {
        	HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        //con.setReadTimeout(50000);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();
	        
	        
	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        long accumulateCount = 0;
		        
		        boolean somethingwrong = false;
		       
		        
			        while ((inputLine = in.readLine()) != null) {
			        	if (iLineCount < 2) { }
			        	else  {
			        		String[] valueProperty = inputLine.split(",");
				        	Object columnValue =valueProperty[0];
				        	Long valueCount = Long.parseLong(valueProperty[1]);
				        	accumulateCount = accumulateCount + valueCount;
				        	c.GlobalColumnType = GlobalColumnType.DistributedFieldWithEmissions;
					        ((DistributedFieldWithEmissions)c.Distribution).AddValue(columnValue, new ValueState(valueCount, accumulateCount, columnValue));
		
			            response.append(inputLine + ";");
			        	}
			        	iLineCount++;
			        }
		        
			        
			        if (iLineCount <= 2)
			        	somethingwrong = true;
			        
		        if (somethingwrong) {
		        	c.SomethingWrong = somethingwrong;
		        }
		        in.close(); 
	        }
        } catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + c.Name);

     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + c.Name);

     	}
        System.out.println("opt.COLUMNS_DISTRIBUTION.size() =  " + opt.COLUMNS_DISTRIBUTION.size());
        return c;
    }
	
	public Column sendGetColumnWithoutEmissions(String tableName, String columnName, Options opt, Column cEm) throws Exception {
		Column c = new Column(tableName + '.' + columnName);
		c.Distribution = new DistributedFieldWithEmissions();
		String inClause = "(";
		for (ValueState vs : ((DistributedFieldWithEmissions)cEm.Distribution).Values.values()) {
			inClause = inClause + vs.Value.toString() + ",";
		}
		inClause = inClause + ")";
		inClause = inClause.replace(",)", ")");
		

		String prepareCmd = prepareQuery("select min(" + columnName + "), max("+ columnName + ") from " 
		+ tableName + " where " + columnName 
		+ " not in " + inClause);
        String url = baseUrl + GetParams(prepareCmd, "csv");
   
        URL obj = new URL(url);
        try {
        	HttpURLConnection.setFollowRedirects(false);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setConnectTimeout(5000);
	        //con.setReadTimeout(5000);
	        // optional default is GET
	        con.setRequestMethod("GET");
	
	        //add request header
	        con.setRequestProperty("User-Agent", USER_AGENT);
	
	        int responseCode = con.getResponseCode();
	        
	        
	        if (responseCode == CorrectresponseCode) {
		        BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response = new StringBuffer();
		
		        int iLineCount = 0;
		        while ((inputLine = in.readLine()) != null) {
		        	if (iLineCount == 2) {
		            response.append(inputLine);
		        	}
		        	iLineCount++;
		        }
		        in.close(); 
		        
		        try {
		        	String[] valueProperty = response.toString().split(",");
		        	double minValue = Double.parseDouble(valueProperty[0]);
		        	double maxValue = Double.parseDouble(valueProperty[1]);
		        	((DistributedFieldWithEmissions)c.Distribution).MinValue = minValue;
		        	((DistributedFieldWithEmissions)c.Distribution).MaxValue = maxValue;

		        } catch(Exception ex) {
		        	System.out.println(ex);
		        }
	        }
        } catch (java.net.SocketTimeoutException e) {
        	System.out.println("java.net.SocketTimeoutException for column: " + c.Name);

     	} catch (java.io.IOException e) {
     		System.out.println("java.io.IOException for column: " + c.Name);

     	}
        System.out.println("opt.COLUMNS_DISTRIBUTION.size() =  " + opt.COLUMNS_DISTRIBUTION.size());
        return c;
    }
    // HTTP GET request
    public void sendGet() throws Exception {

        String url = "http://skyserver.sdss.org/dr12/en/tools/search/x_sql.aspx?cmd=select%20top%202%20ra,dec%20from%20star&format=csv";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        //System.out.println("\nSending 'GET' request to URL : " + url);
        //System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }

    // HTTP POST request
    public void sendPost(String cmd) throws Exception {

        String url = "http://skyserver.sdss3.org/public/en/tools/search/x_sql.aspx";
        
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = "cmd=" + cmd + "&format=csv";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }
}
