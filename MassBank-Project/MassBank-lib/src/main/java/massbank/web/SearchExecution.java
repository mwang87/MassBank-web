package massbank.web;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.configuration2.ex.ConfigurationException;
import massbank.DatabaseManager;


public class SearchExecution {
	private DatabaseManager database = null;
	private HttpServletRequest request = null;
	
	
	public SearchExecution(HttpServletRequest request) throws SQLException, ConfigurationException {
		this.database = new DatabaseManager("MassBank"); 
		this.request = request;
	}
	
	public <E> ArrayList<E> exec(SearchFunction<E> function) {
		function.getParameters(this.request);
		ArrayList<E> result = function.search(this.database);
		this.database.closeConnection();
		return result;
	}
}
