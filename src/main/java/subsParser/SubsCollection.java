package subsParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubsCollection
{

    // Main Subtitles Collection List:
    private Map<String, LinkedList<Caption>> allCaptionsForAllWords;

    public SubsCollection()
    {
        allCaptionsForAllWords = new HashMap<String, LinkedList<Caption>>();
    }

    /*
     * public HashMap<String, LinkedList<Caption>> getCollection() { return
     * SubsCollection.m_allSubsCaptions; }
     */

    public void addCaption(Caption caption)
    {
        // Splitting the caption's text into words, using regex:
        String[] wordsInCaption = caption.content.split("[^\\p{L}0-9']+"); // alternative:
                                                                           // "\\P{L}+"

        for (String word : wordsInCaption)
        {
            word = word.toLowerCase();

            for (int i = 0; i < word.length(); i++)
                for (int j = i + 1; j <= word.length(); j++)
                {
                    String str = word.substring(i, j);

                    // No such word is yet in the collection - adding it with a
                    // new List with the caption in it:
                    if (!allCaptionsForAllWords.containsKey(str))
                    {
                        LinkedList<Caption> captionsListForWord = new LinkedList<Caption>();

                        captionsListForWord.add(caption);

                        allCaptionsForAllWords.put(str, captionsListForWord);
                    }
                    // The word is in the collection - adding the caption to the
                    // existing List:
                    else
                    {
                        // This specific caption is not yet in the collection -
                        // adding it to the List:
                        if (allCaptionsForAllWords.get(str).indexOf(caption) == -1)
                        {
                            // Will put the more relevant (complete) words first, but sadly in reverse order. TODO
                            if (str.equals(word))
                                allCaptionsForAllWords.get(str).addFirst(caption);
                            else
                                allCaptionsForAllWords.get(str).addLast(caption);
                        }
                    }
                }
        }
    }

    /**
     * Will convert word to lower case.
     */
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

    public void clearCollection()
    {
        allCaptionsForAllWords.clear();
    }

    public Map<String, LinkedList<Caption>> getAll()
    {
        return allCaptionsForAllWords;
    }

}
