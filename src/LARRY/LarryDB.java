package LARRY;

import General.DatabaseOperations;
import subsParser.SubsCollection;

import java.sql.SQLException;

public class LarryDB
{
    final public static String LARRY_DB_NAME = "LarryDB";
    final public static String defaultUsername = "LARRY";
    final public static String defaultPassword = "DigLazarus2008";

    static String dbCreateTablesUpd = "" +
            // Words
            "CREATE TABLE t_words (word VARCHAR(255) NOT NULL PRIMARY KEY, word_id INT NOT NULL AUTO_INCREMENT); " +
            // Captions
            "CREATE TABLE t_captions (caption_id INT NOT NULL AUTO_INCREMENT, " +
            "season_num INT NOT NULL, episode_num INT NOT NULL, " +
            "start TIME(3) NOT NULL, end TIME(3) NOT NULL, content VARCHAR(255) NOT NULL ); " +
            // Words to Captions
            "CREATE TABLE t_words_to_captions (word_id INT NOT NULL PRIMARY KEY, caption_id INT NOT NULL); " +
            // Files seen
            "CREATE TABLE t_files_seen (file_name varchar(255) NOT NULL PRIMARY KEY, file_id INT AUTO_INCREMENT); ";

    public static void printCaptionsForWord(String databaseName, String word)
    {

    }

    public SubsCollection initializeDatabase() throws SQLException
    {
        DatabaseOperations databaseOperator = new DatabaseOperations();
        databaseOperator.createDatabaseAndLogin(LarryDB.LARRY_DB_NAME, LarryDB.defaultUsername, LarryDB.defaultPassword);
        databaseOperator.executeUpdate(dbCreateTablesUpd);
        return new SubsCollection(databaseOperator);
    }

    public void temp() throws SQLException
    {
        SubsCollection collection = this.initializeDatabase();

        collection.addFileNameToLibrary("Curb file number one");
        collection.addFileNameToLibrary("Curb file number two");
        collection.addFileNameToLibrary("Curb file number three");
    }


}
