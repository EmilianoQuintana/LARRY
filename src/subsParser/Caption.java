package subsParser;

public class Caption
{

    public static final int NO_SEASON = -1;
    public static final int NO_EPISODE = -1;
    public Style style;
    public Region region;

    public int seasonNum;
    public int episodeNum;
    public int captionNum;
    public Time start;
    public Time end;

    // Raw content, before cleaning up templates, markup.
    public String rawContent = "";

    // Cleaned-up content:
    public String content = "";

    /**
     * Returns the Season Number of this Caption.
     *
     * @return This Caption's SeasonNumber.
     */
    public String getSeason()
    {
        return String.format("%03d", this.seasonNum);
    }

    /**
     * Returns this Caption's Episode Number, in 3 digits, including leading zeros.
     * @return This Caption's Episode Number (e.g.: '015').
     */
    public String getEpisode()
    {
        return this.getEpisode(3);
    }

    /**
     * Returns this Caption's Episode Number, in [i_Digits] digits, including leading zeros.
     * @param i_Digits - number of digits in which to format the Episode Number
     * @return This Caption's Episode Number.
     */
    public String getEpisode(int i_Digits)
    {
        return String.format("%0" + i_Digits + "d", this.episodeNum);
    }

    /**
     * Cleans this Caption's content from SRT garbage such as XML tags.
     */
    public void cleanContentForSRT()
    {
        String[] lines;
        String text = this.rawContent;
        StringBuilder sb = new StringBuilder();

        // Adding line breaks:
        lines = text.split("<br />");

        // Cleaning the XML:
        for (int i = 0; i < lines.length; i++)
        {
            // Destroying all remaining XML tags:
            lines[i] = lines[i].replaceAll("<.*?>", "");
        }

        // Appending spaces after each line:
        for (String line : lines)
        {
            sb.append(line).append(" ");
        }

        sb.deleteCharAt(sb.length() - 1);
        this.content = sb.toString();
    }

    /**
     * Prints this caption's details in an easy-to-read manner.
     * @return This Caption's SeasonNumber and EpisodeNumber if exist, Start- and End-Times.
     */
    @Override
    public String toString()
    {
        String printString = "";

        if (this.seasonNum != NO_SEASON)
        {
            printString += "S" + String.format("%02d", this.seasonNum);
        }

        if (this.episodeNum != NO_EPISODE)
        {
            printString += "E" + String.format("%02d", this.episodeNum);
        }

        return printString
                + "\t"
                + this.start.getTime(Const.TIME_FORMAT_SRT)//TODO use style format stuff
                + "---"
                + this.end.getTime(Const.TIME_FORMAT_SRT)
                + "      "
                + this.content;
    }


}
