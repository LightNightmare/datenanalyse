import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;


public class Shell {

	public static void main(String... args) throws SQLException {
		
		Connection connection = null;
		
		System.out.println("HELLO! :)");
		
		String SQL = "SELECT YY, MM, COUNT(MM) "
				+ "FROM PARSED_STATEMENTS "
				+ "WHERE YY >= 2003 AND YY <= 2005 "
				+ "GROUP BY YY, MM "
				+ "ORDER BY YY, MM";
		
		connection = DriverManager.getConnection("jdbc:oracle:thin:@marsara.ipd.kit.edu:1521:student","bdcourse","bdcourse");
		System.out.println(connection.prepareStatement(SQL).executeQuery());
		connection.close();
	}	
	
}
