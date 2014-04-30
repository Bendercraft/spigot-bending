package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MySQL {

	private java.sql.Connection connection;
	private Logger log;
	private String host;
	private String username;
	private String password;
	private String database;
	private String port;

	public MySQL(String host, String username, String password,
			String database, String port) {

		this.host = host;
		this.username = username;
		this.password = password;
		this.database = database;
		this.port = port;
		this.log = Bending.log;

		//if (!initialize())
	}

	public boolean initialize() {
		{
			try {
				Class.forName("com.mysql.jdbc.Driver");
				this.connection = DriverManager.getConnection("jdbc:mysql://"
						+ this.host + ":" + this.port + "/" + this.database,
						this.username, this.password);
				// String createTable1 =
				// "CREATE TABLE IF NOT EXISTS Bending ('player' TEXT NOT NULL, 'bending' TEXT NOT NULL)";
				// String createTable2 =
				// "CREATE TABLE IF NOT EXISTS Bending ('player' TEXT NOT NULL, 'setter' TEXT NOT NULL, 'ability' TEXT NOT NULL)";
				// this.connection.createStatement().executeQuery(createTable1);
				// this.connection.createStatement().executeQuery(createTable2);
				return true;
			} catch (ClassNotFoundException e) {
				//this.log.severe("ClassNotFoundException! " + e.getMessage());
				return false;
			} catch (SQLException e) {
				this.log.severe("MySQL connection failed. Be sure to check if your config file was set properly. Defaulting to flatfiles.");
				return false;
			}
		}

	}

	public Connection getConnection() {
		if (this.connection == null) {
			initialize();
		}

		return this.connection;
	}

	public Boolean checkConnection() {
		return Boolean.valueOf(getConnection() != null);
	}

	public void close() {
		try {
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (Exception e) {
			this.log.severe("Failed to close database connection! "
					+ e.getMessage());
		}
	}

	public ResultSet select(String query) {
		try {
			return getConnection().createStatement().executeQuery(query);
		} catch (SQLException ex) {
			this.log.severe("Error at SQL Query: " + ex.getMessage());
			this.log.severe("Query: " + query);
		}
		return null;
	}

	public void insert(String query) {
		try {
			getConnection().createStatement().executeUpdate(query);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet")) {
				this.log.severe("Error at SQL INSERT Query: " + ex);
				this.log.severe("Query: " + query);
			}
		}
	}

	public void update(String query) {
		try {
			getConnection().createStatement().executeUpdate(query);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet")) {
				this.log.severe("Error at SQL UPDATE Query: " + ex);
				this.log.severe("Query: " + query);
			}
		}
	}

	public void delete(String query) {
		try {
			getConnection().createStatement().executeUpdate(query);
		} catch (SQLException ex) {
			if (!ex.toString().contains("not return ResultSet")) {
				this.log.severe("Error at SQL DELETE Query: " + ex);
				this.log.severe("Query: " + query);
			}
		}
	}

	public Boolean execute(String query) {
		try {
			getConnection().createStatement().execute(query);
			return Boolean.valueOf(true);
		} catch (SQLException ex) {
			this.log.severe(ex.getMessage());
			this.log.severe("Query: " + query);
		}
		return Boolean.valueOf(false);
	}

	public Boolean existsTable(String table) {
		try {
			ResultSet tables = getConnection().getMetaData().getTables(null,
					null, table, null);
			return Boolean.valueOf(tables.next());
		} catch (SQLException e) {
			this.log.severe("Failed to check if table '" + table + "' exists: "
					+ e.getMessage());
		}
		return Boolean.valueOf(false);
	}

	public Boolean existsColumn(String tabell, String colum) {
		try {
			ResultSet colums = getConnection().getMetaData().getColumns(null,
					null, tabell, colum);
			return Boolean.valueOf(colums.next());
		} catch (SQLException e) {
			this.log.severe("Failed to check if colum '" + colum + "' exists: "
					+ e.getMessage());
		}
		return Boolean.valueOf(false);
	}
}
