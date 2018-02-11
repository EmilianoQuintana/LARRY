package Database;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOperations
{
    // SQL Statements Constants:
//    public static final String SELECT = " SELECT ";
//    public static final String LIMIT = " LIMIT ";
//    public static final String UPDATE = " UPDATE ";
//    public static final String CREATE_TABLE = " CREATE_TABLE ";
//    public static final String IF_NOT_EXISTS = " IF NOT EXISTS ";

    private Statement statement;

    /***
     *
     * @param databaseName Name of the Database
     * @param username UserName to login to the DB
     * @param password Password to login to the DB
     * @throws SQLException
     */
    public void createDatabaseAndLogin(String databaseName, String username, String password) throws SQLException
    {
        this.login(databaseName, false, username, password);
    }

    /**
     * Creates and starts the connection to the application's Database.
     *
     * @param databaseName Name of the desired Database
     * @param username     Username to login with
     * @param password     Password to login with
     * @throws SQLException
     */
    public void login(String databaseName, String username, String password) throws SQLException
    {
        this.login(databaseName, true, username, password);
    }

    /**
     * Creates and starts the connection to the application's Database, if it doesn't already exist.
     * @param databaseName      Name of the desired Database
     * @param createIfNotExists Should I create the DB if it doesn't exist?
     * @param username          Username to login with
     * @param password          Password to login with
     * @throws SQLException
     */
    private void login(String databaseName, boolean createIfNotExists, String username, String password) throws SQLException
    {
        this.loginDetails("jdbc:h2:file:./databases/"
                        + databaseName
                        + ";IFEXISTS="
                        + String.valueOf(createIfNotExists).toUpperCase(),
                username,
                password);
    }

    /***
     * TODO - what does it do exactly????
     * @param url
     * @param username
     * @param password
     * @throws SQLException
     */
    private void loginDetails(String url, String username, String password) throws SQLException
    {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        Connection connection = dataSource.getConnection();
        this.statement = connection.createStatement();
    }

    public void executeUpdate(String command) throws SQLException
    {
        this.statement.executeUpdate(command); //maybe should be executeLargeUpdate later, TODO make sure
    }

    public ResultSet executeQuery(String query) throws SQLException
    {
        //TODO ESCAPE ALL QUERIES!!!!!!!!!!!!!!!
        return this.statement.executeQuery(query);
    }

    /**
     * Executes a query while limiting the number of results/rows to be processed.
     * @param query SQL query to execute.
     * @param limit Number of lines to be processed.
     * @return
     * @throws SQLException
     */
    public ResultSet executeQueryLimit(String query, int limit) throws SQLException
    {
        String limitStr;

        if (limit <= 0)
        {
            limitStr = "";
        }
        else
        {
            limitStr = SQL.LIMIT + limit;
        }

        return this.statement.executeQuery(query + limitStr);
    }

}
