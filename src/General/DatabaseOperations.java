package General;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOperations
{
    private Statement statement;

    public void login(String databaseName, String username, String password) throws SQLException
    {
        this.login(databaseName, true, username, password);
    }

    public void login(String databaseName, boolean createIfNotExists, String username, String password) throws SQLException
    {
        this.loginDetails("jdbc:h2:file:" + "./databases/" + databaseName + ";IFEXISTS=" + String.valueOf(createIfNotExists).toUpperCase(),
                username, password);
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
        //TODO ESCAPE ALL QUERIES!!!!!!!!!!!!!!!
        return statement.executeQuery(query);
    }

    public ResultSet executeQueryLimit(String query, int limit) throws SQLException
    {
        String limitStr;

        if (limit <= 0)
        {
            limitStr = "";
        }
        else
        {
            limitStr = " LIMIT " + limit;
        }

        return statement.executeQuery(query + limitStr);
    }

    public static String escapeSingleQuotes(String string)
    {
        //replaces ' with ''
        return string.replace("'", "''");
    }

    public void createDatabaseAndLogin(String databaseName, String username, String password) throws SQLException
    {
        this.login(databaseName, false, username, password);
    }
}
