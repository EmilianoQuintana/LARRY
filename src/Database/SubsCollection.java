package Database;

import LARRY.Messages;
import subsParser.Caption;
import subsParser.Const;
import subsParser.Time;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class SubsCollection
{
    private static final int NONEXISTENT_ID = -1;
    private static final int MINIMUM_SUBWORD_LENGTH = 3;

    public static final int MAX_FILES_TO_ADD = 999;

    // Main Subtitles Collection List:
    private DatabaseOperations databaseOperations;

    public SubsCollection(DatabaseOperations dataOp)
    {
        this.databaseOperations = dataOp;
    }

    public void addFileNameToLibrary(String fileName) throws SQLException
    {
        this.databaseOperations.executeUpdate(SQL.Query.insertFileNameToFilesSeen(fileName));

//                SQL.INSERT_INTO + SQL.TBL.FILES_SEEN + " (" + SQL.COL.FILE_NAME + ") " + SQL.VALUES + " ('" +
//                        SQL.sanitize(fileName) + "')");
    }

    public void removeFileNameFromLibrary(String fileName) throws SQLException
    {
        this.databaseOperations.executeUpdate(SQL.Query.deleteFileNameFromFilesSeen(fileName));
//                        SQL.DELETE + SQL.TBL.FILES_SEEN + SQL.WHERE + SQL.COL.FILE_NAME + " = '" +
//                                SQL.sanitize(fileName) +
//                                "'");
    }

    public boolean hasFileInLibrary(String fileName) throws SQLException
    {
        ResultSet results = this.databaseOperations.executeQueryLimit(SQL.Query.selectFileFromFilesSeen(fileName), 1);
//                SQL.SELECT_ALL + SQL.FROM + SQL.TBL.FILES_SEEN + " " + SQL.WHERE + SQL.COL.FILE_NAME + " = '" +
//                        SQL.sanitize(fileName) + "'", 1);
        return results.next();
    }

    public void addCaption(Caption caption) throws SQLException
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
        ResultSet results = this.databaseOperations.executeQuery(SQL.SELECT + " LAST_INSERT_ID()");
        results.next();
        int caption_id = results.getInt(1); //TODO test

        // Splitting the caption's text into words, using regex:
        String[] wordsInCaption = caption.content.split("[^\\p{L}0-9']+"); // alternative: "\\P{L}+"

        for (String fullWord : wordsInCaption)
        {
            fullWord = fullWord.toLowerCase();
            //First insert the entire word
            boolean firstTimeWordIsInThisCaption = this.insertCaptionWord(fullWord, caption_id);
            if (!firstTimeWordIsInThisCaption)
            {
                continue;
            }
            // Iterate on every possible substring of the full word (to insert partial words)
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

    public List<Caption> getAllCaptionsFor(String word, int captionCountLimit, boolean sortByQuality)
            throws SQLException, Messages.SeasonNumberTooBigException
    {
        String lowercaseWord = word.toLowerCase();

        int wordID = this.getWordID(lowercaseWord);

        if (wordID == NONEXISTENT_ID)
        {
            Messages.printInConsole(new Messages.WordNotFoundException(word).getMessage());
            return new LinkedList<>();
        }

        /*
        ResultSet captionIDsResults = databaseOperations.executeQueryLimit("SELECT caption_id FROM t_words_to_captions WHERE word_id = '" + wordID + "'", captionCountLimit);

        List<Integer> captionIDs = new LinkedList<>();

        while (captionIDsResults.next())
        {
            captionIDs.add(captionIDsResults.getInt(1));
        }
*/

        ResultSet resultSet = this.databaseOperations.executeQueryLimit(
                "SELECT " + SQL.TBL.CAPTIONS + ".*" +
                        " FROM " + SQL.TBL.CAPTIONS +
                        " INNER JOIN " + SQL.TBL.WORDS_TO_CAPTIONS +
                        " ON " + SQL.TBL.WORDS_TO_CAPTIONS + ".word_id = " + SQL.TBL.WORDS + ".word_id" +
                        " INNER JOIN " + SQL.TBL.WORDS +
                        " ON " + SQL.TBL.WORDS + ".word = " + "'" + SQL.sanitize(lowercaseWord) + "'" +
                        " WHERE " + SQL.TBL.CAPTIONS + ".caption_id = " + SQL.TBL.WORDS_TO_CAPTIONS + ".caption_id",
                captionCountLimit);

        List<Caption> result = new LinkedList<>();
        Dictionary<Caption, Integer> captionSortKeys = new Hashtable<>();

        while (resultSet.next())
        {
            Caption cap = new Caption();
            cap.content = resultSet.getString("content");
            cap.captionNum = resultSet.getInt("caption_id");
            cap.setSeasonNum(resultSet.getInt("season_num"));
            cap.setEpisodeNum(resultSet.getInt("episode_num"));
            cap.start = new Time(Const.TIME_FORMAT_SRT, resultSet.getString("start"));
            cap.end = new Time(Const.TIME_FORMAT_SRT, resultSet.getString("end"));
            captionSortKeys.put(cap, SubsCollection.calcSortKeyForCaption(cap, lowercaseWord));
            result.add(cap);
        }

        result.sort(Comparator.comparingInt(captionSortKeys::get));

        return result;
    }

    public List<Caption> getAllCaptionsInEpisodeFor(String word, int seasonNum, int episodeNum, int captionCountLimit,
                                                    boolean sortByQuality)
            throws SQLException, Messages.SeasonNumberTooBigException
    {
        List<Caption> list = new LinkedList<>();

        // Searching the list of captions for all occurrences of the given word:
        for (Caption caption : this.getAllCaptionsFor(word, captionCountLimit, sortByQuality))
        {
            // Adding only the captions in the desired episode in the desired season:
            if (caption.getSeasonNum() == seasonNum
                    && caption.getEpisodeNum() == episodeNum)
            {
                list.add(caption);
            }
        }

        // @ TODO: ADD EXCEPTION HANDLING
        return list;
    }

    public String getMediaName(int mediaID)
            throws SQLException
    {
        ResultSet resultSet = this.databaseOperations.executeQueryLimit(SQL.Query.selectMediaIDFromMedias(mediaID), 1);

        if (!resultSet.next())
        {
            return "";
        }

        return resultSet.getString(SQL.COL.MEDIA_NAME);
    }

    private int getWordID(String word) throws SQLException
    {
        ResultSet results = this.databaseOperations.executeQueryLimit(SQL.Query.selectWordIDFromWords(word), 1);

        if (!results.next())
        {
            return SubsCollection.NONEXISTENT_ID;
        }
        return results.getInt(SQL.COL.WORD_ID);
    }

    /**
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
            this.databaseOperations
                    .executeUpdate(
                            SQL.INSERT_INTO + " " + SQL.TBL.WORDS + " (" + SQL.COL.WORD + ") " + SQL.VALUES + " ('" +
                                    SQL.sanitize(word) + "')");
            ResultSet results = this.databaseOperations.executeQuery("SELECT LAST_INSERT_ID()");
            results.next();
            word_id = results.getInt(1); //TODO test
        }

        // Now the word is in the collection in any case

        //Now check if this word-caption combination is already in the words_to_captions table
        ResultSet results = this.databaseOperations.executeQueryLimit(
                "SELECT * FROM " + SQL.TBL.WORDS_TO_CAPTIONS + " WHERE word_id = " + SQL.sanitize(word_id) +
                        " AND caption_id = " + SQL.sanitize(caption_id), 1);
        if (!results.next())
        {
            this.databaseOperations.executeUpdate(
                    SQL.INSERT_INTO + SQL.TBL.WORDS_TO_CAPTIONS + " (word_id, caption_id) VALUES (" +
                            SQL.sanitize(word_id) +
                            ", " + SQL.sanitize(caption_id) + ")");
            return true;
        }
        //else the caption-word combination already exists
        return false;
    }

    private static int calcSortKeyForCaption(Caption cap, String lowercaseWord)
    {
        String regex = ".*\\b" + Pattern.quote(lowercaseWord) + "\\b.*";
        if (cap.content.matches(regex))
        {
            //Content contains *whole* word, not as part of another word
            return 0;
        }
        else
        {
            return 1;
        }
    }

}
