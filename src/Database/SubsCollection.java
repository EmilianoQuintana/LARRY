package Database;

import LARRY.Messages;
import subsParser.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class SubsCollection
{
    private static final int NONEXISTENT_ID = -1;
    private static final int MINIMUM_SUBWORD_LENGTH = 3;
    private static final String REGEX_SPLIT_TO_WORDS = "[^\\p{L}0-9']+"; // alternative: "\\P{L}+"

    public static final int MAX_FILES_TO_ADD = 999;

    // Main Subtitles Collection List:
    private DatabaseOperations databaseOperations;

    public SubsCollection(DatabaseOperations dataOp)
    {
        this.databaseOperations = dataOp;
    }

    //region General Getters

    public String getMediaName(int mediaID)
            throws SQLException
    {
        ResultSet resultSet = this.databaseOperations
                .executeQuerySingleRecord(SQL.Query.selectMediaNameFromMedias(mediaID));

        if (!resultSet.next())
        {
            return "";
        }

        return resultSet.getString(SQL.COL.MEDIA_NAME);
    }

    //endregion

    public void addFileNameToLibrary(String fileName) throws SQLException
    {
//        SQL.INSERT_INTO + SQL.TBL.FILES_SEEN + " (" + SQL.COL.FILE_NAME + ") " +
//          SQL.VALUES + " ('" + SQL.sanitize(fileName) + "')");
        this.databaseOperations.executeUpdate(SQL.Query.insertFileNameToFilesSeen(fileName));
    }

    public void removeFileNameFromLibrary(String fileName) throws SQLException
    {
//        SQL.DELETE + SQL.TBL.FILES_SEEN +
//          SQL.WHERE + SQL.COL.FILE_NAME + " = '" + SQL.sanitize(fileName) + "'");
        this.databaseOperations.executeUpdate(SQL.Query.deleteFileNameFromFilesSeen(fileName));
    }

    public boolean hasFileInLibrary(String fileName) throws SQLException
    {
//        SQL.SELECT_ALL + SQL.FROM + SQL.TBL.FILES_SEEN + " " +
//          SQL.WHERE + SQL.COL.FILE_NAME + " = '" + SQL.sanitize(fileName) + "'", 1);
        ResultSet results = this.databaseOperations
                .executeQuerySingleRecord(SQL.Query.selectFileFromFilesSeen(fileName));

        return results.next();
    }

    /**
     * Returns a TimedTextFileFormat for a given Subtitle Format.
     *
     * @param subtitleFormat Format of the Fubtitle file.
     * @return A TimedTextFileFormat object that matches the given Subtitle Format.
     */
    public static TimedTextFileFormat getTimedTextFileFormat(String subtitleFormat)
    {
        TimedTextFileFormat timedTextFileFormat;

        switch (subtitleFormat)
        {
            case Const.SUBS_FORMAT_SRT:
                timedTextFileFormat = new FormatSRT();
                break;
            case Const.SUBS_FORMAT_ASS:
                timedTextFileFormat = new FormatASS();
                break;
            case Const.SUBS_FORMAT_SCC:
                timedTextFileFormat = new FormatSCC();
                break;
            case Const.SUBS_FORMAT_STL:
                timedTextFileFormat = new FormatSTL();
                break;
            case Const.SUBS_FORMAT_TTML:
                timedTextFileFormat = new FormatTTML();
                break;
            default:
                timedTextFileFormat = null;
                break; //will simply ignore the file
        }

        return timedTextFileFormat;
    }

    public boolean parseFileAndAddCaptionsToLibrary(File file)
            throws SQLException, IOException, FatalParsingException
    {
        boolean isSuccess = false;
        TimedTextFileFormat timedTextFileFormat = SubsCollection
                .getTimedTextFileFormat(FileOperations.getCleanExtension(file));

        // Ignoring empty or unsupported Subtitle files:
        if (timedTextFileFormat != null)
        {
            // Getting Season Number and Episode Number from the filename:
            int[] seasonAndEpisode = FileOperations.parseSxxExxFromFilename(file.getName());
            int seasonNum = seasonAndEpisode[0];
            int episodeNum = seasonAndEpisode[1];

            // Reading the currently iterated Subtitles file, parsing it and adding to the SubsCollection:
            InputStream fileInputStream = new FileInputStream(file);

            TimedTextObject tto = timedTextFileFormat
                    .parseFile(file.getName(), fileInputStream, seasonNum, episodeNum);

            for (Caption caption : tto.captions.values())
            {
                this.addCaption(caption);
            }

            isSuccess = true;
        }

        return isSuccess;
    }

    private void addCaption(Caption caption) throws SQLException
    {
        /*
        int processedCaptionsCounter = 0;

        StringBuilder captionsValuesBuilder = new StringBuilder();

        String SEPARATOR = ",";
        for (Caption caption : captions)
        {
            processedCaptionsCounter++;
            captionsValuesBuilder.append("(" + caption.seasonNum + ", "
                    + caption.episodeNum + ", '"
                    + caption.start.toSQLTime3() + "', '"
                    + caption.end.toSQLTime3() + "', '"
                    + DatabaseOperations.escapeSingleQuotes(caption.content) + "')" + SEPARATOR);

            if (processedCaptionsCounter >= SubsCollection.MAX_CAPTIONS_INSERT_AMOUNT)
            {
                //Delete trailing separator
                captionsValuesBuilder
                        .delete(captionsValuesBuilder.length() - SEPARATOR.length(), captionsValuesBuilder.length());

                String captionPropsSQLValues = captionsValuesBuilder.toString();
                databaseOperations.executeUpdate(
                        "INSERT INTO t_captions (season_num, episode_num, start, end, content) VALUES "
                                + captionPropsSQLValues
                                + ";");
            }

        }
         */


        // No such caption is yet in the collection?
        this.databaseOperations.executeUpdate(
                SQL.INSERT_INTO + SQL.TBL.CAPTIONS + " (" + SQL.COL.SEASON_NUM + ", " + SQL.COL.EPISODE_NUM + ", " +
                        SQL.COL.START + ", " + SQL.COL.END + ", " + SQL.COL.CONTENT + ") " + SQL.VALUES + " ("
                        + SQL.sanitize(caption.getSeasonNum()) + ", " + SQL.sanitize(caption.getEpisodeNum()) + ", '" +
                        SQL.sanitize(caption.start.toSQLTime3()) + "', '" + SQL.sanitize(caption.end.toSQLTime3()) +
                        "', '" +
                        SQL.sanitize(caption.content) + "')");

        ResultSet results = this.databaseOperations.executeQuery(SQL.SELECT + " " + SQL.LAST_INSERT_ID);
        results.next();

        int caption_id = results.getInt(1); //TODO test

        // Splitting the caption's text into words, using regex:
        String[] wordsInCaption = caption.content.split(SubsCollection.REGEX_SPLIT_TO_WORDS);

        for (String fullWord : wordsInCaption)
        {
            fullWord = fullWord.toLowerCase();

            // First, inserting the entire word as in the original caption:
            boolean firstTimeWordIsInThisCaption = this.insertCaptionWord(fullWord, caption_id);

            if (!firstTimeWordIsInThisCaption)
            {
                continue;
            }

            // Iterating on every possible substring of the full word (to insert partial words, not yet variations on word's root etc.)
            for (int i = 0; i < fullWord.length(); i++)
            {
                for (int j = i + 1; j <= fullWord.length(); j++)
                {
                    if (j - i < MINIMUM_SUBWORD_LENGTH)
                    {
                        continue;
                    }

                    String subWord = fullWord.substring(i, j);
                    /*
                     * TODO optimize:
                     * possibly include a check for words that are 'obviously' sub-words, yet will not be in a
                     * dictionary. For example the caption "Let's go to Johnson's Bar, everyone!" should
                     * include the name "Johnson".
                     */
                    if (!ValidWordsDictionary.instance().contains(subWord))
                    {
                        continue;
                    }


                    this.insertCaptionWord(subWord, caption_id);
                }
            }
        }
    }

    /**
     * Returns all the captions that contain a given word.
     *
     * @param word              The desired word to be contained in the found captions.
     * @param captionCountLimit Maximum number of captions to return.
     * @param sortByQuality     Whether to sort the found captions by their quality. //TODO I'm not sure I understood that myself. ~~~~ Cuky
     * @return All captions that contain the given word.
     * @throws SQLException
     * @throws Messages.SeasonNumberTooBigException
     */
    public List<Caption> getAllCaptionsFor(String word, int captionCountLimit, boolean sortByQuality)
            throws SQLException, Messages.SeasonNumberTooBigException
    {
        return this.getAllCaptionsFor(SQL.Query.selectAllCaptionsForWord(word), word, captionCountLimit, sortByQuality);

        //region I have moved this code into the private method getAllCaptionsFor, in order to use it both here and in the other overloaded method. ~~~~ Cuky

//        String lowercaseWord = word.toLowerCase();
//        int wordID = this.getWordID(lowercaseWord);
//
//        List<Caption> result = new LinkedList<>();
//
//        if (wordID == NONEXISTENT_ID)
//        {
//            Messages.printInConsole(new Messages.WordNotFoundException(word).getMessage());
//        }
//        else
//        {
//            ResultSet resultSet = this.databaseOperations
//                    .executeQuery(SQL.Query.selectAllCaptionsForWord(wordID), captionCountLimit);
//
//            Dictionary<Caption, Integer> captionSortKeys = new Hashtable<>();
//
//            while (resultSet.next())
//            {
//                Caption cap = new Caption(
//                    resultSet.getInt(SQL.COL.SEASON_NUM),
//                    resultSet.getInt(SQL.COL.EPISODE_NUM),
//                    resultSet.getInt(SQL.COL.CAPTION_ID),
//                    new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.START)),
//                    new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.END)),
//                    resultSet.getString(SQL.COL.CONTENT));
//
////                cap.content = resultSet.getString(SQL.COL.CONTENT);
////                cap.captionID = resultSet.getInt(SQL.COL.CAPTION_ID);
////                cap.setSeasonNum(resultSet.getInt(SQL.COL.SEASON_NUM));
////                cap.setEpisodeNum(resultSet.getInt(SQL.COL.EPISODE_NUM));
////                cap.start = new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.START));
////                cap.end = new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.END));
//
//                captionSortKeys.put(cap, SubsCollection.calcSortKeyForCaption(cap, lowercaseWord));
//                result.add(cap);
//            }
//
//            result.sort(Comparator.comparingInt(captionSortKeys::get));
//        }
//
//        return result;

        //endregion
    }

    /**
     * Returns all captions of a single episode that contain a given word.
     *
     * @param word              The desired word to be contained in the found captions.
     * @param seasonNum         Number of the desired season to search in.
     * @param episodeNum        Number of the desired episode to search in.
     * @param captionCountLimit Maximum number of captions to return.
     * @param sortByQuality     Whether to sort the found captions by their quality. //TODO I'm not sure I understood that myself. ~~~~ Cuky
     * @return All captions of a single episode that contain the given word.
     * @throws SQLException
     * @throws Messages.SeasonNumberTooBigException
     */
    public List<Caption> getAllCaptionsInEpisodeFor(String word, int seasonNum, int episodeNum, int captionCountLimit,
                                                    boolean sortByQuality)
            throws SQLException, Messages.SeasonNumberTooBigException
    {
        return this.getAllCaptionsFor(SQL.Query.selectAllCaptionsForWordInEpisode(word, seasonNum, episodeNum), word,
                captionCountLimit, sortByQuality);

        //region old code, see comment inside

        // This code fetched ALL captions from the DB and then filtered it according to the given seasonNum and episodeNum.
        // To improve performance, I instead used a SELECT query that already fetches the filtered results without the need
        // to iterate over them.

//        List<Caption> list = new LinkedList<>();
//
//        // Searching the list of captions for all occurrences of the given word:
//        for (Caption caption : this.getAllCaptionsFor(word, captionCountLimit, sortByQuality))
//        {
//            // Adding only the captions in the desired episode in the desired season:
//            if (caption.getSeasonNum() == seasonNum
//                    && caption.getEpisodeNum() == episodeNum)
//            {
//                list.add(caption);
//            }
//        }
//
//        // @ TODO: ADD EXCEPTION HANDLING
//        return list;

        //endregion
    }

    /**
     * Returns all captions for a given SQL query, using a given word. Should only be used in SubsCollection class, hence its privacy.
     *
     * @param sqlQuery          SQL Query to execute to get relevant captions.
     * @param word              Desired word to search all captions for.
     * @param captionCountLimit Maximum number of captions to return.
     * @param sortByQuality     Whether to sort the found captions by their quality. //TODO I'm not sure I understood that myself. ~~~~ Cuky
     * @return All found captions matching the given SQL Query.
     * @throws SQLException
     * @throws Messages.SeasonNumberTooBigException
     */
    private List<Caption> getAllCaptionsFor(String sqlQuery,
                                            String word, int captionCountLimit, boolean sortByQuality)
            throws SQLException, Messages.SeasonNumberTooBigException
    {
        String lowercaseWord = word.toLowerCase();
        int wordID = this.getWordID(lowercaseWord);

        List<Caption> result = new LinkedList<>();

        if (wordID == NONEXISTENT_ID)
        {
            Messages.printInConsole(new Messages.WordNotFoundException(word).getMessage());
        }
        else
        {
            ResultSet resultSet = this.databaseOperations
                    .executeQuery(sqlQuery, captionCountLimit);

            Dictionary<Caption, Integer> captionSortKeys = new Hashtable<>();

            while (resultSet.next())
            {
                Caption cap = new Caption(
                        resultSet.getInt(SQL.COL.SEASON_NUM),
                        resultSet.getInt(SQL.COL.EPISODE_NUM),
                        resultSet.getInt(SQL.COL.CAPTION_ID),
                        new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.START)),
                        new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.END)),
                        resultSet.getString(SQL.COL.CONTENT));

//                cap.content = resultSet.getString(SQL.COL.CONTENT);
//                cap.captionID = resultSet.getInt(SQL.COL.CAPTION_ID);
//                cap.setSeasonNum(resultSet.getInt(SQL.COL.SEASON_NUM));
//                cap.setEpisodeNum(resultSet.getInt(SQL.COL.EPISODE_NUM));
//                cap.start = new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.START));
//                cap.end = new Time(Const.TIME_FORMAT_SRT, resultSet.getString(SQL.COL.END));

                captionSortKeys.put(cap, SubsCollection.calcSortKeyForCaption(cap, lowercaseWord));
                result.add(cap);
            }

            result.sort(Comparator.comparingInt(captionSortKeys::get));
        }

        return result;
    }

    /**
     * Returns the ID in the DB of a desired word.
     *
     * @param word The desired word.
     * @return The numeric ID of the desired word.
     * @throws SQLException
     */
    private int getWordID(String word) throws SQLException
    {
        ResultSet results = this.databaseOperations.executeQuerySingleRecord(SQL.Query.selectWordIDFromWords(word));

        if (!results.next())
        {
            return SubsCollection.NONEXISTENT_ID;
        }
        return results.getInt(SQL.COL.WORD_ID);
    }

    /**
     * //TODO not use this method, instead work in batches with a single COMMIT at the end
     * I have a feeling that this method is VERY WASTEFUL in resources. It accesses the DB multiple times and it only adds a single word!
     * ~~~~ Cuky.
     *
     * @return false if the word-caption pair was already in there
     */
    private boolean insertCaptionWord(String word, int caption_id) throws SQLException
    {
        //Add word to dictionary, for ease of checking other subwords later
        if (!ValidWordsDictionary.instance().contains(word))
        {
            ValidWordsDictionary.instance().addWord(word);
        }

        // No such word is yet in the collection? - adding it
        int word_id = this.getWordID(word);
        if (word_id == NONEXISTENT_ID)
        {
            this.databaseOperations.executeUpdate(SQL.Query.insertWordIntoWords(word));

            ResultSet results = this.databaseOperations.executeQuery(SQL.SELECT + SQL.LAST_INSERT_ID);
            results.next();
            word_id = results.getInt(1); //TODO test
        }

        // Now the word is in the collection in any case

        // Now check if this word-caption combination is already in the words_to_captions table
        ResultSet results = this.databaseOperations.executeQuerySingleRecord(
                SQL.SELECT + "*" + SQL.FROM + SQL.TBL.WORDS_TO_CAPTIONS + SQL.WHERE + " word_id = " +
                        SQL.sanitize(word_id) +
                        " AND caption_id = " + SQL.sanitize(caption_id));
        if (!results.next())
        {
            this.databaseOperations.executeUpdate(
                    SQL.INSERT_INTO + SQL.TBL.WORDS_TO_CAPTIONS + " (word_id, caption_id) " + SQL.VALUES + "(" +
                            SQL.sanitize(word_id) +
                            ", " + SQL.sanitize(caption_id) + ")");
            return true;
        }
        //else the caption-word combination already exists
        return false;
    }

    /**
     * Calculates the Sort Key for a given caption.
     *
     * @param cap           The desired Caption, the content of which should be scanned and calculated on.
     * @param lowercaseWord The desired word (in lower-case) to calculate the Sort Key for.
     * @return 0 if the caption contains the given word in its whole, or 1 otherwise.
     */
    private static int calcSortKeyForCaption(Caption cap, String lowercaseWord)
    {
        String regex = ".*\\b" + Pattern.quote(lowercaseWord) + "\\b.*";

        if (cap.content.matches(regex))
        {
            //Content contains WHOLE word, not as part of another word:
            return 0;
        }
        else
        {
            return 1;
        }
    }

}
