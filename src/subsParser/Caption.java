package subsParser;

public class Caption
{

    public Style style;
    public Region region;

    public int seasonNum;
    public int episodeNum;
    public int captionNum;
    public Time start;
    public Time end;

    /**
     * Raw content, before cleaning up templates and markup.
     */
    public String rawContent = "";
    /**
     * Cleaned-up subtitle content.
     */
    public String content = "";

    public String getSeason()
    {
        return String.format("%03d", this.seasonNum);
    }

    public String getEpisode()
    {
        return this.getEpisode(3);
    }

    public String getEpisode(int i_Digits)
    {
        return String.format("%0" + i_Digits + "d", this.episodeNum);
    }

    public void cleanContentForSRT()
    {
        String[] lines;
        String text = this.rawContent;
        // add line breaks
        lines = text.split("<br />");
        // clean XML
        for (int i = 0; i < lines.length; i++)
        {
            // this will destroy all remaining XML tags
            lines[i] = lines[i].replaceAll("\\<.*?\\>", "");
        }
        StringBuilder sb = new StringBuilder();
        for (String line : lines)
            sb.append(line + " ");
        sb.deleteCharAt(sb.length() - 1);
        this.content = sb.toString();
    }

    @Override
    public String toString()
    {
        return "S"
                + seasonNum
                + "E"
                + episodeNum
                + "    "
                + start.getTime(Const.TIME_FORMAT_SRT)//TODO use style format stuff
                + "---"
                + end.getTime(Const.TIME_FORMAT_SRT)
                + "      "
                + content;
    }
}
