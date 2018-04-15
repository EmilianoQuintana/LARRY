package Database;

import subsParser.Caption;
import subsParser.Const;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.filter.SubTitleFileFilter;
import uk.co.caprica.vlcj.filter.VideoFileFilter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaOperations

{
    private static final long EXTRA_TIME_BEFORE_CAPTION = 491;
    private static final long EXTRA_TIME_AFTER_CAPTION = 100;
    private static EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent;
    private static EmbeddedMediaPlayer mediaPlayer;
    private static int multiplier = 1;
    private long subtitleDelayMilliseconds;
    private int lastMarkedCaptionIndex;
    private String searchedWord;
    private List<Caption> markedCaptionMoments;

    public static String[] getSupportedVideoExtensions()
    {
        return new VideoFileFilter().getExtensions();
    }

    public static Set<String> getSupportedVideoExtensionsSet()
    {
        return new VideoFileFilter().getExtensionSet();
    }

    /**
     * Returns all the Subtitle Formats supported by LARRY.
     *
     * @return String Array of the Subtitle Formats supported both by the subtitle parser API and the media playback API.
     */
    public static String[] getSupportedSubtitleFormats()
    {
        // Getting the supported subtitle extensions by SubsParser and by VLCJ:
        HashSet<String> supportedBySubsParser = Const.getSupportedSubtitlesFormatsSet();
        Set<String> supportedByVLCJ = new SubTitleFileFilter().getExtensionSet();

        String[] supportedByBoth;
        int indexToAdd = 0;

        // Taking only the extensions supported by both SubsParser and VLCJ.

        if (supportedBySubsParser.size() < supportedByVLCJ.size())
        {
            // Building the list of supported Subtitle Files according to SubsParser:
            supportedByBoth = new String[supportedBySubsParser.size()];

            for (String strFormat : supportedBySubsParser)
            {
                if (supportedByVLCJ.contains(strFormat))
                {
                    supportedByBoth[indexToAdd] = strFormat;

                    indexToAdd++;
                }
            }
        }
        else
        {
            // Building the list of supported Subtitle Files according to VLCJ:
            supportedByBoth = new String[supportedByVLCJ.size()];

            for (String strFormat : supportedByVLCJ)
            {
                if (supportedBySubsParser.contains(strFormat))
                {
                    supportedByBoth[indexToAdd] = strFormat;

                    indexToAdd++;
                }
            }
        }

        return supportedByBoth;
    }

    public MediaOperations()
    {
        this.getEmbeddedMPComp();
    }

    public EmbeddedMediaPlayerComponent getEmbeddedMPComp()
    {
        if ((MediaOperations.embeddedMediaPlayerComponent == null) || (MediaOperations.mediaPlayer == null))
        {
            embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            mediaPlayer = MediaOperations.embeddedMediaPlayerComponent.getMediaPlayer();
        }

        return embeddedMediaPlayerComponent;
    }

    public void startPlayingMedia(String mediaString, long startTimeInMilliseconds)
    {
//        String mediaShortenedPath = mediaString
//                .substring(mediaString.lastIndexOf(File.separatorChar) + 1, mediaString.length());

//        this.frame.setTitle("\"" + this.searchedWord + "\" in " + mediaShortenedPath + "- LARRY");

        mediaPlayer.playMedia(mediaString);
        mediaPlayer.setTime(startTimeInMilliseconds + this.subtitleDelayMilliseconds);
        if (this.subtitleDelayMilliseconds != 0)
        {
            //A-sync problem fix
//            this.mediaPlayer.setSpuDelay(this.mediaOperations.subtitleDelayMilliseconds * 1000);
            this.setSpuDelay();
        }
    }

    /**
     * Will not allow negative times!
     */
    public void setToTimestampWithSubtitleDelay(long timeInMilliseconds)
    {
        long newTime = timeInMilliseconds + this.subtitleDelayMilliseconds;
        if (newTime < 0 || newTime >= mediaPlayer.getLength())
        {
            newTime = 0;
        }

        mediaPlayer.setTime(newTime);
    }

    public long getSubtitleDelay()
    {
        return this.subtitleDelayMilliseconds;
    }

    public void setSubtitleDelay(long delayInMilliseconds)
    {
        this.subtitleDelayMilliseconds = delayInMilliseconds;
//        this.mediaPlayer.setSpuDelay(this.subtitleDelayMilliseconds * 1000);
        this.setSpuDelay();
        //this.subtitleDelayAmountText.setText(String.format("%d", this.mediaOperations.subtitleDelayMilliseconds) + " ms");
    }

    public void skipToCaption(Caption caption)
    {
        this.setToTimestampWithSubtitleDelay(caption.start.getMseconds() - EXTRA_TIME_BEFORE_CAPTION);
    }

    public void skipToNextOrPrevCaption(boolean nextOrPrev)
    {
        if (this.markedCaptionMoments.size() == 0)
        {
            return;
        }
        this.lastMarkedCaptionIndex += nextOrPrev ? +1 : -1;

        //rotate around
        this.lastMarkedCaptionIndex += this.markedCaptionMoments.size();
        this.lastMarkedCaptionIndex %= this.markedCaptionMoments.size();

        this.skipToCaption(this.markedCaptionMoments.get(this.lastMarkedCaptionIndex));
    }

    public void setMarkedCaptionMoments(List<Caption> captions)
    {
        this.markedCaptionMoments = captions;
        if (this.markedCaptionMoments.size() == 0)
        {
            return;
        }

        this.lastMarkedCaptionIndex = 0;
    }

    public void skipToMarkedCaptionByIndex(int index)
    {
        if (index < 0 || index >= this.markedCaptionMoments.size())
        {
            throw new IndexOutOfBoundsException("No such caption index!");
        }

        this.lastMarkedCaptionIndex = index;
        this.skipToCaption(this.markedCaptionMoments.get(this.lastMarkedCaptionIndex));
    }

    /**
     * This is sort of weird functionality, later I'll probably remove this or change
     * it to depend on frequency of clicks in a short time period ~~~~ Itamar
     *
     * @param increaseOrDecrease you know
     */
    public void smartChangeSubsDelay(boolean increaseOrDecrease)
    {
        if (increaseOrDecrease == (multiplier > 0))
        {
            if (multiplier < 99 && multiplier > -99)
            {
                multiplier *= 2;
            }
        }
        else
        {
            multiplier = increaseOrDecrease ? +1 : -1;
        }

        long deltaAmount = multiplier * 100; //100 ms minimum unit
        this.subtitleDelayMilliseconds += deltaAmount;

        //this.mediaPlayer.setSpuDelay(this.subtitleDelayMilliseconds * 1000);
        this.setSpuDelay();

        //belongs to GUI
        // this.subtitleDelayAmountText.setText(String.format("%d", this.subtitleDelayMilliseconds) + " ms");
    }

    public void pause()
    {
        mediaPlayer.pause();
    }

    public void pauseOrResume()
    {
        this.pause();
    }

    public void skipTimeBy(long skipTimeInMilliseconds)
    {
        mediaPlayer.skip(skipTimeInMilliseconds);
    }

    public void setSearchedWord(String searchedWord)
    {
        this.searchedWord = searchedWord;
    }

    public void setSpuDelay()
    {
        mediaPlayer.setSpuDelay(this.getDefaultSubtitleDelayInMs());
    }

    public String getShortenedVideoTitle(String mediaString)
    {
        String mediaShortenedPath = mediaString
                .substring(mediaString.lastIndexOf(File.separatorChar) + 1, mediaString.length());
        return ("\"" + this.searchedWord + "\" in " + mediaShortenedPath + "- LARRY");
    }

    private long getDefaultSubtitleDelayInMs()
    {
        return this.subtitleDelayMilliseconds * 1000;
    }
}
