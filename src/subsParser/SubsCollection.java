package subsParser;

import General.DatabaseOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class SubsCollection
{
    private final static int NONEXISTENT_ID = -1;
    // Main Subtitles Collection List:
    DatabaseOperations databaseOperations;

    public SubsCollection(DatabaseOperations dataOp)
    {
        databaseOperations = dataOp;
    }

    public void addFileNameToLibrary(String fileName) throws SQLException
    {
        databaseOperations.executeUpdate("INSERT INTO t_files_seen (file_name) VALUES ('" + fileName + "')");
    }

    public void removeFileNameFromLibrary(String fileName) throws SQLException
    {
        databaseOperations.executeUpdate("DELETE t_files_seen WHERE file_name = '" + fileName + "'");
    }

    public boolean hasFileInLibrary(String fileName) throws SQLException
    {
        ResultSet results = databaseOperations.executeQueryLimit("SELECT * FROM t_files_seen WHERE file_name = '" + DatabaseOperations.escapeSingleQuotes(fileName) + "'", 1);
        return results.next();
    }

    public int getWordID(String word) throws SQLException
    {
        ResultSet results = databaseOperations.executeQueryLimit("SELECT word_id FROM t_words WHERE word = '" + DatabaseOperations.escapeSingleQuotes(word) + "'", 1);
        if (!results.next())
        {
            return NONEXISTENT_ID;
        }
        return results.getInt("word_id");
    }

    public void addCaption(Caption caption) throws SQLException
    {
        // No such caption is yet in the collection?
        databaseOperations.executeUpdate("INSERT INTO t_captions (season_num, episode_num, start, end, content) VALUES ("
                + caption.seasonNum + ", " + caption.episodeNum + ", '" + caption.start.toSQLTime3() + "', '" + caption.end.toSQLTime3() + "', '" + DatabaseOperations.escapeSingleQuotes(caption.content) + "')");
        ResultSet results = databaseOperations.executeQuery("SELECT LAST_INSERT_ID()");
        results.next();
        int caption_id = results.getInt(1); //TODO test

        // Splitting the caption's text into words, using regex:
        String[] wordsInCaption = caption.content.split("[^\\p{L}0-9']+"); // alternative: "\\P{L}+"

        for (String word : wordsInCaption)
        {
            word = word.toLowerCase();

            for (int i = 0; i < word.length(); i++)
            {
                for (int j = i + 1; j <= word.length(); j++)
                {
                    String str = word.substring(i, j);

                    // No such word is yet in the collection? - adding it
                    int word_id = getWordID(word);
                    if (word_id == NONEXISTENT_ID)
                    {
                        databaseOperations.executeUpdate("INSERT INTO t_words (word) VALUES ('" + DatabaseOperations.escapeSingleQuotes(word) + "')");
                        results = databaseOperations.executeQuery("SELECT LAST_INSERT_ID()");
                        results.next();
                        word_id = results.getInt(1); //TODO test
                    }

                    // Now the word is in the collection in any case

                    //Now check if this word-caption combination is already in the words_to_captions table
                    results = databaseOperations.executeQueryLimit("SELECT * FROM t_words_to_captions WHERE word_id = " + word_id + " AND caption_id = " + caption_id, 1);
                    if (!results.next())
                    {
                        databaseOperations.executeUpdate("INSERT INTO t_words_to_captions (word_id, caption_id) VALUES (" + word_id + ", " + caption_id + ")");
                    }
                    //else the caption-word combination already exists
                }
            }
        }
    }


    public List<Caption> getAllCaptionsFor(String word, int captionCountLimit) throws SQLException
    {
        String lowercaseWord = word.toLowerCase();

        int wordID = getWordID(lowercaseWord);

        if (wordID == NONEXISTENT_ID)
        {
            System.out.println("This word does not exist in the database:   " + word);
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

        ResultSet resultSet = databaseOperations.executeQueryLimit(
                "SELECT t_captions.* " +
                        "FROM t_captions " +
                        "INNER JOIN t_words_to_captions ON t_words_to_captions.word_id = t_words.word_id " +
                        "INNER JOIN t_words ON t_words.word = " + "'" + lowercaseWord + "'" +
                        "WHERE t_captions.caption_id = t_words_to_captions.caption_id ", captionCountLimit);

        List<Caption> result = new LinkedList<Caption>();

        while (resultSet.next())
        {
            Caption cap = new Caption();
            cap.content = resultSet.getString("content");
            cap.captionNum = resultSet.getInt("caption_id");
            cap.seasonNum = resultSet.getInt("season_num");
            cap.episodeNum = resultSet.getInt("episode_num");
            cap.start = new Time(Const.TIME_FORMAT_SRT, resultSet.getString("start"));
            cap.end = new Time(Const.TIME_FORMAT_SRT, resultSet.getString("end"));

            result.add(cap);
        }

        return result;
    }

    public List<Caption> getAllCaptionsInEpisodeFor(String word, int seasonNum, int episodeNum, int captionCountLimit) throws SQLException
    {
        List<Caption> list = new LinkedList<Caption>();

        // Searching the list of captions for all occurrences of the given word:
        for (Caption caption : getAllCaptionsFor(word, captionCountLimit))
        {
            // Adding only the captions in the desired episode in the desired
            // season:
            if (caption.seasonNum == seasonNum && caption.episodeNum == episodeNum)
            {
                list.add(caption);
            }
        }

        // @ TODO: ADD EXCEPTION HANDLING
        return list;
    }

}
