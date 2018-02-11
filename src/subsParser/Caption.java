package subsParser;

import LARRY.Messages;

public class Caption
{

    public static final int NO_SEASON = -1;
    public static final int NO_EPISODE = -1;
    public static final int MAX_SEASON = 999;
    public Style style;
    public Region region;

    public int mediaID;
    private int seasonNum;
    private int episodeNum;
    public int captionNum;
    public Time start;
    public Time end;

    // Raw content, before cleaning up templates, markup.
    public String rawContent = "";

    // Cleaned-up content:
    public String content = "";

    /**
     * @return This Caption's Media ID number.
     */
    public int getMediaID()
    {
        return this.mediaID;
    }

    /**
     * Sets this Caption's SeasonNumber.
     *
     * @param seasonNum SeasonNumber to set. Allowed value must be between 0-999 inclusive.
     * @throws Messages.SeasonNumberTooBigException
     */
    public void setSeasonNum(int seasonNum)
            throws Messages.SeasonNumberTooBigException
    {
        if (seasonNum < 0)
        {
            this.seasonNum = Caption.NO_SEASON;
        }
        else
        {
            if (seasonNum > Caption.MAX_SEASON)
            {
                this.seasonNum = Caption.MAX_SEASON;
                throw new Messages.SeasonNumberTooBigException(this.seasonNum);
            }
            else
            {
                this.seasonNum = seasonNum;
            }
        }
    }

    /**
     * @return This Caption's Season Number as an integer number.
     */
    public int getSeasonNum()
    {
        return this.seasonNum;
    }

    /**
     * @return This Caption's SeasonNumber, padded with leading zeros to fit 3 digits.
     */
    public String getSeason()
    {
        return String.format("%03d", this.seasonNum);
    }

    /**
     * Sets this Caption's Episode Number.
     *
     * @param episodeNum
     */
    public void setEpisodeNum(int episodeNum)
    {

    }

    /**
     * @return This Caption's Episode Number as an integer number.
     */
    public int getEpisodeNum()
    {
        return this.episodeNum;
    }

    /**
     * @return This Caption's Episode Number, padded with leading zeros to fit 3 digits. (e.g.: '015').
     */
    public String getEpisode()
    {
        return this.getEpisode(3);
    }

    /**
     * @param i_Digits - number of digits in which to format the Episode Number
     * @return This Caption's Episode Number, padded with leading zeros to fit <code>i_Digits</code> digits..
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
     * Returns this caption's details in an easy-to-read manner.
     * @return A <code>String</code> of this Caption's SeasonNumber, EpisodeNumber, Start- and End-Times.
     */
    @Override
    public String toString()
    {
        String printString = "";

        if (this.getMediaID() != 0)
        {
            //TODO add MediaName fetch and append to toString method
        }

        if (this.getSeasonNum() != NO_SEASON)
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