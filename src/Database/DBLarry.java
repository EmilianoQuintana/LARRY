package Database;

import subsParser.Caption;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class DBLarry
{
    private SubsCollection collection;

    private static final String LARRY_DB_NAME = "DBLarry",
                                defaultUsername = "LARRY",
                                defaultPassword = "DigLazarus2008";


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
                SQL.CREATE_TABLE_IF_NOT_EXISTS + " " + SQL.TBL_WORDS + " " +
                "(word VARCHAR(255) " + SQL.NOT_NULL + SQL.PRIMARY_KEY + ", " +
                "word_id INT " + SQL.NOT_NULL + SQL.AUTO_INCREMENT + "); " +

                // Captions
                SQL.CREATE_TABLE_IF_NOT_EXISTS + " " + SQL.TBL_CAPTIONS + " " +
                "(caption_id INT + " + SQL.NOT_NULL + SQL.AUTO_INCREMENT + ", " +
                "season_num INT " + SQL.NOT_NULL + ", " +
                "episode_num INT " + SQL.NOT_NULL + ", " +
                "start VARCHAR(255) " + SQL.NOT_NULL + ", " +
                "end VARCHAR(255) " + SQL.NOT_NULL + ", " +
                "content VARCHAR(255) " + SQL.NOT_NULL + " ); " +

                // Words to Captions
                SQL.CREATE_TABLE_IF_NOT_EXISTS + " " + SQL.TBL_WORDS_TO_CAPTIONS + " " +
                "(word_id INT " + SQL.NOT_NULL + ", " +
                "caption_id INT " + SQL.NOT_NULL + ", " +
                SQL.PRIMARY_KEY + " (word_id, caption_id)); " +

                // Files seen
                SQL.CREATE_TABLE_IF_NOT_EXISTS + " " + SQL.TBL_FILES_SEEN + " " +
                "(file_name VARCHAR(255) " + SQL.NOT_NULL + SQL.PRIMARY_KEY + ", " +
                "file_id INT " + SQL.AUTO_INCREMENT + "); " +

                // Names of series/movies
                SQL.CREATE_TABLE_IF_NOT_EXISTS + " " + SQL.TBL_MEDIA_NAMES + " " +
                "(media_name VARCHAR(255) " + SQL.NOT_NULL + ", " +
                "media_id INT " + SQL.NOT_NULL + ")";

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
