package Database;

import LARRY.Messages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import subsParser.Caption;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBLarry
{
    private SubsCollection collection;

    private static final String LARRY_DB_NAME = "DBLarry",
            defaultUsername = "LARRY",
            defaultPassword = "DigLazarus2008";

    private static ArrayList m_MediasList;

    /**
     * Constructs and initializes a new DBLarry object.
     *
     * @throws SQLException
     */
    public DBLarry() throws SQLException
    {
        this.initializeDatabase();
    }

    /**
     * Creates the whole DB structure, including tables, columns and their attributes.
     * @throws SQLException
     */
    private void initializeDatabase() throws SQLException
    {
        DatabaseOperations databaseOperator = new DatabaseOperations();
        databaseOperator
                .createDatabaseAndLogin(DBLarry.LARRY_DB_NAME, DBLarry.defaultUsername, DBLarry.defaultPassword);

        databaseOperator.executeUpdate(SQL.Query.initializeDB());

        this.collection = new SubsCollection(databaseOperator);
    }

    public void tempTests()
            throws Messages.EmptyFolderException
    {
        String filePrefix = "Curb Your Enthusiasm";
        String folderPath = "O:\\GOOGLE DRIVE --- HERE\\PC BACKUP\\I - Personal\\Documents\\Programming" +
                "\\InteliJ\\LARRY\\Subtitles\\Subtitles";

        this.updateSubsCollectionFromFolder(filePrefix, folderPath);
    }

    /**
     * //TODO What is the purpose of this method? It seems I've wrote it but I cannot remember why. ~~~~ Cuky 18.04.2018
     * Tries to find a path for the file containing the given Caption.
     * It will return a found file if its name begins with the prefix string, including matching seasons/episode if necessary.
     * @param caption       Caption, according to which to search for a matching file.
     * @param folderPath    Path to the folder, in which to search for the file.
     * @param fileFilterPrefix    String prefix for the filename.
     * @return Absolute path for file, or null if no matching file was found
     */
    public static String findAbsoluteFilePathForCaption(Caption caption, String folderPath, String fileFilterPrefix)
            throws Messages.GhostFolderException
    {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();


        if (listOfFiles == null)
        {
            throw new Messages.GhostFolderException(folderPath);
        }
        // Iterating over the files in the given folder
        for (File file : listOfFiles)
        {
            if (!MediaOperations.getSupportedVideoExtensionsSet().contains(FileOperations.getCleanExtension(file))) {
                continue;
            }

            if (!file.getName().startsWith(fileFilterPrefix)) {
                continue;
            }

            if (caption.getSeasonNum() == Caption.NO_SEASON && caption.getEpisodeNum() == Caption.NO_EPISODE) {
                return file.getAbsolutePath();
            }

            String captionSxxExx = formatAsSxxExx(caption.getSeasonNum(), caption.getEpisodeNum());
            int[] seasonAndEpisode = FileOperations.parseSxxExxFromFilename(file.getName());
            String fileSxxExx = formatAsSxxExx(seasonAndEpisode[0], seasonAndEpisode[1]);

            if (fileSxxExx.equals(captionSxxExx)) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    private static String formatAsSxxExx(int seasonNum, int episodeNum)
    {
        return "S" + String.format("%02d", seasonNum) + "E" +
                String.format("%02d", episodeNum);
    }

    public void updateSubsCollectionFromFolder(String filePrefix, String folderPath)
            throws Messages.EmptyFolderException
    {
        FileOperations.updateSubsCollectionFromFolder(this.collection, filePrefix, folderPath);
    }

    public List<Caption> getAllCaptionsFor(String word, int captionCountLimit, boolean sortByQuality)
            throws SQLException, Messages.SeasonNumberTooBigException
    {
        return this.collection.getAllCaptionsFor(word, captionCountLimit, sortByQuality);
    }

    public String getMediaName(int mediaID)
            throws SQLException
    {
        return this.collection.getMediaName(mediaID);
    }

    /* ***Obsolete***
    // DBLarry's DB objects. Access by objects instead of by SQL.
    public class DB_Objects
    {
        public ArrayList getList()
        {
            return DBLarry.m_MediasList;
        }
    }
    */

}
