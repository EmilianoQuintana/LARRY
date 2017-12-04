package Database;

import subsParser.Caption;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class DBLarry
{
    private SubsCollection collection;

    private final static String LARRY_DB_NAME = "DBLarry";
    private final static String defaultUsername = "LARRY";
    private final static String defaultPassword = "DigLazarus2008";
    
    public DBLarry() throws SQLException
    {
        initializeDatabase();
    }
    
    /**
     * Tries to find and return a path for the file containing the caption. Will take a file if its
     * name beings with the prefix format, including matching seasons/episode if necessary
     *
     * @return absolute path for file, or null if unable to find a file
     */
    public static String getAbsoluteFilePathForCaption(Caption caption, String filePrefix, String folderPath)
    {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles)
        {
            if (file.getName().startsWith(filePrefix))
            {
                if (caption.seasonNum == Caption.NO_SEASON && caption.episodeNum == Caption.NO_EPISODE)
                {
                    return file.getAbsolutePath();
                }
                String SxxExx = formatAsSxxExx(caption);
                if (file.getName().contains(SxxExx))
                {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static String formatAsSxxExx(Caption caption)
    {
        return "S" + String.format("%02d", caption.seasonNum) + "E" + String.format("%02d", caption.seasonNum);
    }

    private void initializeDatabase() throws SQLException
    {
        DatabaseOperations databaseOperator = new DatabaseOperations();
        databaseOperator
                .createDatabaseAndLogin(DBLarry.LARRY_DB_NAME, DBLarry.defaultUsername, DBLarry.defaultPassword);
    
    
        String dbCreateTablesUpd = "" +
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
                "CREATE TABLE IF NOT EXISTS t_words_to_captions (word_id INT NOT NULL PRIMARY KEY, caption_id INT NOT NULL); " +
                // Files seen
                "CREATE TABLE IF NOT EXISTS t_files_seen (file_name varchar(255) NOT NULL PRIMARY KEY, file_id INT AUTO_INCREMENT); ";
        databaseOperator.executeUpdate(dbCreateTablesUpd);


        collection = new SubsCollection(databaseOperator);
    }

    public void tempTests() throws SQLException
    {
        String filePrefix = "Curb Your Enthusiasm";
        String folderPath = "O:\\GOOGLE DRIVE --- HERE\\PC BACKUP\\I - Personal\\Documents\\Programming" +
                "\\InteliJ\\LARRY\\Subtitles\\Subtitles";

        updateSubsCollectionFromFolder(filePrefix, folderPath);
    }
    
    public void updateSubsCollectionFromFolder(String filePrefix, String folderPath)
    {
        FileOperations.updateSubsCollectionFromFolder(collection, filePrefix, folderPath);
    }

    public List<Caption> getAllCaptionsFor(String word, int captionCountLimit) throws SQLException
    {
        return collection.getAllCaptionsFor(word, captionCountLimit);
    }
}
