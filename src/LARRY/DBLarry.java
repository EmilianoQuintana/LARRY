package LARRY;

import General.DatabaseOperations;
import subsParser.Caption;
import subsParser.SubsCollection;

import java.sql.SQLException;
import java.util.List;

public class DBLarry
{
    final public static String LARRY_DB_NAME = "DBLarry";
    final public static String defaultUsername = "LARRY";
    final public static String defaultPassword = "DigLazarus2008";

    static String dbCreateTablesUpd = "" +
            // Words
            "CREATE TABLE IF NOT EXISTS t_words (word VARCHAR(255) NOT NULL PRIMARY KEY, word_id INT NOT NULL AUTO_INCREMENT); " +
            // Captions
            "CREATE TABLE IF NOT EXISTS t_captions (caption_id INT NOT NULL AUTO_INCREMENT, " +
            "season_num INT NOT NULL, " +
            "episode_num INT NOT NULL, " +
            "start VARCHAR(255) NOT NULL, " +
            "end VARCHAR(255) NOT NULL, " +
            "content VARCHAR(255) NOT NULL ); " +
            // Words to Captions
            "CREATE TABLE IF NOT EXISTS t_words_to_captions (word_id INT NOT NULL, caption_id INT NOT NULL); " +
            // Files seen
            "CREATE TABLE IF NOT EXISTS t_files_seen (file_name varchar(255) NOT NULL PRIMARY KEY, file_id INT AUTO_INCREMENT); ";

    public static void printCaptionsForWord(String databaseName, String word)
    {

    }

    public SubsCollection initializeDatabase() throws SQLException
    {
        DatabaseOperations databaseOperator = new DatabaseOperations();
        databaseOperator.createDatabaseAndLogin(DBLarry.LARRY_DB_NAME, DBLarry.defaultUsername, DBLarry.defaultPassword);


        databaseOperator.executeUpdate(dbCreateTablesUpd);


        return new SubsCollection(databaseOperator);
    }

    public void temp() throws SQLException
    {
        SubsCollection collection = this.initializeDatabase();


        String filePrefix = "Avengers";
        String folderPath = "O:\\GOOGLE DRIVE --- HERE\\PC BACKUP\\I - Personal\\Documents\\Programming\\InteliJ\\LARRY\\Subtitles";
        FileOperations.updateSubsCollectionFromFolder(collection, filePrefix, folderPath);


        List<Caption> results = collection.getAllCaptionsFor("tesseract", 60);

        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }
    }


}
