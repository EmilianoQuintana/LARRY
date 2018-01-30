package Database;

import LARRY.Errors;
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
        this.initializeDatabase();
    }
    
    /**
     * Tries to find a path for the file containing the given Caption.
     * It will return a found file if its name beings with the prefix string, including matching seasons/episode if necessary.
     * @param caption       Caption, according to which to search for a matching file.
     * @param folderPath    Path to the folder, in which to search for the file.
     * @param filePrefix    String prefix for the filename.
     * @return Absolute path for file, or null if no matching file was found
     */
    public static String findAbsoluteFilePathForCaption(Caption caption, String folderPath, String filePrefix)
            throws Errors.GhostFolderException
    {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null)
        {
            throw new Errors.GhostFolderException(folderPath);
        }
        // Iterating over the files in the given folder
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
        return "S" + String.format("%02d", caption.seasonNum) + "E" + String.format("%02d", caption.episodeNum);
    }

    private void initializeDatabase() throws SQLException
    {
        DatabaseOperations databaseOperator = new DatabaseOperations();
        databaseOperator
                .createDatabaseAndLogin(DBLarry.LARRY_DB_NAME, DBLarry.defaultUsername, DBLarry.defaultPassword);
    
    
        String dbCreateTablesUpd = "" +
                // Words
                SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.WORDS +
                "(" + SQL.COL.WORD + SQL.VARCHAR_255 + SQL.NOT_NULL + SQL.PRIMARY_KEY + ", " +
                SQL.COL.WORD_ID + SQL.INT + SQL.NOT_NULL + SQL.AUTO_INCREMENT + "); " +

                // Captions
                SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.CAPTIONS +
                "(" + SQL.COL.CAPTION_ID + SQL.INT + SQL.NOT_NULL + SQL.AUTO_INCREMENT + ", " +
                SQL.COL.SEASON_NUM + SQL.INT + SQL.NOT_NULL + ", " +
                SQL.COL.EPISODE_NUM + SQL.INT + SQL.NOT_NULL + ", " +
                SQL.COL.START + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                SQL.COL.END + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                SQL.COL.CONTENT + SQL.VARCHAR_255 + SQL.NOT_NULL + " ); " +

                // Words to Captions
                SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.WORDS_TO_CAPTIONS +
                "(" + SQL.COL.WORD_ID + SQL.INT + SQL.NOT_NULL + ", " +
                SQL.COL.CAPTION_ID + SQL.INT + SQL.NOT_NULL + ", " +
                SQL.PRIMARY_KEY + " (" + SQL.COL.WORD_ID + ", " + SQL.COL.CAPTION_ID + ")); " +

                // Files seen
                SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.FILES_SEEN +
                "(" + SQL.COL.FILE_NAME + SQL.VARCHAR_255 + SQL.NOT_NULL + SQL.PRIMARY_KEY + ", " +
                SQL.COL.FILE_ID + SQL.INT + SQL.AUTO_INCREMENT + "); " +

                // Names of series/movies
                SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.MEDIA_NAMES +
                "(" + SQL.COL.MEDIA_NAME + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                SQL.COL.MEDIA_ID + SQL.INT + SQL.NOT_NULL + ")";

        databaseOperator.executeUpdate(dbCreateTablesUpd);


        this.collection = new SubsCollection(databaseOperator);
    }

    public void tempTests()
    {
        String filePrefix = "Curb Your Enthusiasm";
        String folderPath = "O:\\GOOGLE DRIVE --- HERE\\PC BACKUP\\I - Personal\\Documents\\Programming" +
                "\\InteliJ\\LARRY\\Subtitles\\Subtitles";

        this.updateSubsCollectionFromFolder(filePrefix, folderPath);
    }
    
    public void updateSubsCollectionFromFolder(String filePrefix, String folderPath)
    {
        FileOperations.updateSubsCollectionFromFolder(this.collection, filePrefix, folderPath);
    }

    public List<Caption> getAllCaptionsFor(String word, int captionCountLimit, boolean sortByQuality)
            throws SQLException
    {
        return this.collection.getAllCaptionsFor(word, captionCountLimit, sortByQuality);
    }
}
