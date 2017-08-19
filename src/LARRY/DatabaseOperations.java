package LARRY;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOperations
{
    private Statement statement;

    final private static String defaultUsername = "LARRY";
    final private static String defaultPassword = "DigLazarus2008";

    public void login(String databaseName) throws SQLException
    {
        this.login(databaseName, true);
    }

    public void login(String databaseName, boolean createIfNotExists) throws SQLException
    {
        this.loginDetails("jdbc:h2:file:" + "./databases/" + databaseName + ";IFEXISTS=" + String.valueOf(createIfNotExists).toUpperCase(),
                DatabaseOperations.defaultUsername,
                DatabaseOperations.defaultPassword);
    }

    public void loginDetails(String url, String username, String password) throws SQLException
    {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);

        Connection connection = dataSource.getConnection();
        statement = connection.createStatement();
    }

    public void executeUpdate(String command) throws SQLException
    {
        statement.executeUpdate(command); //maybe should be executeLargeUpdate later, TODO make sure
    }

    public ResultSet executeQuery(String query) throws SQLException
    {
        return statement.executeQuery(query);
    }

    public void createDatabaseAndLogin(String databaseName) throws SQLException
    {
        this.login(databaseName, false);
    }

}
