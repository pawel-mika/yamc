package pl.wcja.yamc.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.frame.MFObject;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class Database extends MFObject {

	protected IMainFrame mf = null;
	protected Connection conn = null;
	
	public Database(IMainFrame mf) {
		super(mf);
		try {
			//connect()...
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void connect(String dbConnectString, String dbUserId, String dbPass) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
		Driver d = (Driver)Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
		conn = DriverManager.getConnection(dbConnectString, dbUserId, dbPass);
		System.out.println("Database connected!");
		Log(Level.INFO, "Database connected!");
		
		useDatabase("betorama");
		
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("select name from sysobjects where type='U'");
		while(rs.next()) {
			for(int i = 0; i < rs.getMetaData().getColumnCount(); i ++) {
				System.out.print(rs.getObject(i+1).toString());
			}
			System.out.println("");
		}
	}
	
	protected void useDatabase(String databaseName) throws SQLException {
		Statement statement = conn.createStatement();
        String queryString = "use " + databaseName;
        boolean b = statement.execute(queryString);
		System.out.println("Using database: " + databaseName);
		Log(Level.INFO, "Using database: " + databaseName);
	}
	
	public boolean isConnected() {
		try {
			return (conn != null && !conn.isClosed());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void disconnect() {
		try {
			conn.close();
			System.out.println("Database connection closed");
			Log(Level.INFO, "Database connection closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getLoggerName() {
		return this.getClass().getSimpleName();
	}
}
