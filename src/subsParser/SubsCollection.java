package subsParser;

import LARRY.DatabaseOperations;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        ResultSet results = databaseOperations.executeQuery("SELECT * FROM t_files_seen WHERE file_name = '" + fileName + "' LIMIT 1");
        return results.next();
    }

    public int getWordID(String word) throws SQLException
    {
        ResultSet results = databaseOperations.executeQuery("SELECT word_id FROM t_words WHERE word = '" + word + "' LIMIT 1");
        if (!results.next())
            return NONEXISTENT_ID;
        return results.getInt("word_id");
    }

    public void addCaption(Caption caption) throws SQLException
    {
        // No such caption is yet in the collection?
        databaseOperations.executeUpdate("INSERT INTO t_captions (season_num, episode_num, start, end, content) VALUES ("
                + caption.seasonNum + ", " + caption.episodeNum + ", '" + caption.start.toSQLTime3() + "', '" + caption.end.toSQLTime3() + "', '" + caption.content + "')");
        ResultSet results = databaseOperations.executeQuery("SELECT LAST_INSERT_ID()");
        results.next();
        int caption_id = results.getInt(1); //TODO test

        // Splitting the caption's text into words, using regex:
        String[] wordsInCaption = caption.content.split("[^\\p{L}0-9']+"); // alternative: "\\P{L}+"

        for (String word : wordsInCaption)
        {
            word = word.toLowerCase();

            for (int i = 0; i < word.length(); i++)
                for (int j = i + 1; j <= word.length(); j++)
                {
                    String str = word.substring(i, j);

                    // No such word is yet in the collection? - adding it
                    int word_id = getWordID(word);
                    if (word_id == NONEXISTENT_ID)
                    {
                        databaseOperations.executeUpdate("INSERT INTO t_words (word) VALUES ('" + word + "')");
                        results = databaseOperations.executeQuery("SELECT LAST_INSERT_ID()");
                        results.next();
                        word_id = results.getInt(1); //TODO test
                    }

                    // Now the word is in the collection in any case

                    //Now check if this word-caption combination is already in the words_to_captions table
                    results = databaseOperations.executeQuery("SELECT * FROM t_words_to_captions WHERE word_id = " + word_id + " AND caption_id = " + caption_id + " LIMIT 1");
                    if (!results.next())
                    {
                        databaseOperations.executeUpdate("INSERT INTO t_words_to_captions (word_id, caption_id) VALUES (" + word_id + ", " + caption_id + ")");
                    }
                    //else the caption-word combination already exists
                }
        }
    }

    /*
    public List<Caption> getAllCaptionsFor(String word)
    {

        List<Caption> result = allCaptionsForAllWords.get(word.toLowerCase());

        if (result == null)
        {
            result = new LinkedList<Caption>();
        }

        return result;
    }

    public List<Caption> getAllCaptionsInEpisodeFor(String word, int seasonNum, int episodeNum)
    {
        List<Caption> list = new LinkedList<Caption>();

        // Searching the list of captions for all occurrences of the given word:
        for (Caption caption : allCaptionsForAllWords.get(word))
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
    */
}
