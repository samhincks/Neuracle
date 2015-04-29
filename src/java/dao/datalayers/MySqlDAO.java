
package dao.datalayers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import timeseriestufts.kth.streams.bi.ChannelSet;
import timeseriestufts.kth.streams.uni.Channel;


public class MySqlDAO {
    private Connection conn = null;
    PreparedStatement statement = null;

    // connect to MySQL
    public void connSQL() {
            String url = "jdbc:mysql://localhost:3306/newttt?characterEncoding=latin1";
            String username = "root";
            String password = "fnirs196"; // loading drive to connet to the database 
            try { 
                    Class.forName("com.mysql.jdbc.Driver" ); 
                    conn = DriverManager.getConnection(url,username, password ); 
                    }
            //catch drive exception
             catch ( ClassNotFoundException cnfex ) {
                     System.err.println(
                     "Loading JDBC/ODBC drive failed." );
                     cnfex.printStackTrace(); 
             } 
             //catch connection exception
             catch ( SQLException sqlex ) {
                     System.err.println( "Cannot connect to database" );
                     sqlex.printStackTrace(); 
             }
	}

	// disconnect to MySQL
	public void deconnSQL() {
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			System.out.println("Problems with closing the database:");
			e.printStackTrace();
		}
	}

	// execute selection language
	public ResultSet selectSQL(String sql) {
		ResultSet rs = null;
		try {
			statement = conn.prepareStatement(sql);
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	// execute insertion language
	public boolean insertSQL(String sql) {
		try {
			statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.out.println("Exception while inserting data to database:");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception while inserting:");
			e.printStackTrace();
		}
		return false;
	}
	//execute delete language
	public boolean deleteSQL(String sql) {
		try {
			statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.out.println("Exception while deleting data from database:");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception while deleting:");
			e.printStackTrace();
		}
		return false;
	}
	//execute update language
	public boolean updateSQL(String sql) {
		try {
			statement = conn.prepareStatement(sql);
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.out.println("Exception while updating data for database:");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception while updating:");
			e.printStackTrace();
		}
		return false;
	}

    public void addColumnToTable(String database, String colId, Channel c){
        for (int i =0; i < c.numPoints; i++) {
            String insertion = "INSERT INTO " + database + " ( " + colId + " ) VALUES (" + c.getPointOrNull(i) +")";
            System.out.println(insertion);
            this.insertSQL(insertion);
        }
    }
   
    /**Create  a table with id and userId, and prepare each column to be a float,
       with the specified names. **/
    public void createTable(String id, String userId, String[] columnNames) {
        this.insertSQL("DROP TABLE IF EXISTS " + id);
        String createQuery = "CREATE TABLE " + id + " ( " + userId +" VARCHAR(256), "; 
        for (int i =0; i < columnNames.length; i++) {
            String col = columnNames[i];
            if ( i <columnNames.length -1)
                createQuery += col + " float ,";
            else
                createQuery += col + " float ";
        }
        createQuery += ");";
        System.out.println(createQuery);
        this.insertSQL(createQuery);
    }

    
}
