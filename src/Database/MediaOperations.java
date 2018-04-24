package Database;

import subsParser.Caption;
import subsParser.Const;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.filter.ExtensionFileFilter;
import uk.co.caprica.vlcj.filter.SubTitleFileFilter;
import uk.co.caprica.vlcj.filter.VideoFileFilter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaOperations

{
    private static final long EXTRA_TIME_BEFORE_CAPTION = 491;
    private static final long EXTRA_TIME_AFTER_CAPTION = 100;
    private final EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent;
    public EmbeddedMediaPlayer mediaPlayer;
    private static int multiplier = 1;
    private long subtitleDelayMilliseconds;
    private int lastMarkedCaptionIndex;
    private String searchedWord;
    private List<Caption> markedCaptionMoments;

    private static String[] supportedSubtitleFormats;
//    private static String[] supportedVideoExtensions;
    private static ArrayList<String> supportedMediaExtensions;
    private static Set<String> supportedMediaExtensionsSet;

    /**
     * Returns all the Video-file Extensions supported by LARRY, in a String array.
     * @return String Array of the Video-file Extensions supported by the media playback API.
     */
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
        if ((MediaOperations.supportedSubtitleFormats == null) || (MediaOperations.supportedSubtitleFormats.length == 0)) {
            // Getting the supported subtitle extensions by SubsParser and by VLCJ:
            HashSet<String> supportedBySubsParser = Const.getSupportedSubtitlesFormatsSet();
            Set<String> supportedByVLCJ = new SubTitleFileFilter().getExtensionSet();

            int indexToAdd = 0;

            // Taking only the extensions supported by both SubsParser and VLCJ.

            if (supportedBySubsParser.size() < supportedByVLCJ.size()) {
                // Building the list of supported Subtitle Files according to SubsParser:
                MediaOperations.supportedSubtitleFormats = new String[supportedBySubsParser.size()];

                for (String strFormat : supportedBySubsParser) {
                    if (supportedByVLCJ.contains(strFormat)) {
                        MediaOperations.supportedSubtitleFormats[indexToAdd] = strFormat;

                        indexToAdd++;
                    }
                }
            } else {
                // Building the list of supported Subtitle Files according to VLCJ:
                MediaOperations.supportedSubtitleFormats = new String[supportedByVLCJ.size()];

                for (String strFormat : supportedByVLCJ) {
                    if (supportedBySubsParser.contains(strFormat)) {
                        MediaOperations.supportedSubtitleFormats[indexToAdd] = strFormat;

                        indexToAdd++;
                    }
                }
            }
        }

        return MediaOperations.supportedSubtitleFormats;
    }
    /**
     * Returns all the Subtitle Formats and Video-file Extensions supported by LARRY, in a String array.
     * @return String array of the Subtitle Formats and Video-file Extensions supported by LARRY.
     */
    public static ArrayList<String> getSupportedMediaExtensions()
    {
        // Initializing the static array of supported media extensions:
        if ((MediaOperations.supportedMediaExtensions == null) ||
                (MediaOperations.supportedMediaExtensions.size() == 0))
        {
            MediaOperations.supportedMediaExtensions = new ArrayList<>(
                    MediaOperations.getSupportedSubtitleFormats().length +
                            MediaOperations.getSupportedVideoExtensions().length);

            for (String videoExtension : MediaOperations.getSupportedVideoExtensions())
            {
                MediaOperations.supportedMediaExtensions.add(videoExtension);
            }

            for (String subtitleExtension : MediaOperations.getSupportedSubtitleFormats())
            {
                MediaOperations.supportedMediaExtensions.add(subtitleExtension);
            }

//            int currMediaExtension = 0;
//            for (; currMediaExtension < MediaOperations.getSupportedSubtitleFormats().length; currMediaExtension++) {
//                // Copying the subtitle format that matches the currently iterated index:
//                MediaOperations.supportedMediaExtensions[currMediaExtension] = MediaOperations.getSupportedSubtitleFormats()[currMediaExtension];
//            }
//            // Continuing iterating on the MediaExtensions array, without resetting the counter:
//            for (; currMediaExtension < MediaOperations.getSupportedMediaExtensions().length; currMediaExtension++) {
//                // Copying the video extensions that matches the currently iterated index minus the length of the subtitles formats array;
//                MediaOperations.supportedMediaExtensions[currMediaExtension] = MediaOperations.getSupportedVideoExtensions()[currMediaExtension - MediaOperations.getSupportedSubtitleFormats().length];
//            }
        }

        return MediaOperations.supportedMediaExtensions;
    }

    public static String[] getSupportedMediaExtensionsArray()
    {
        String[] extensionsArray = new String[MediaOperations.getSupportedMediaExtensions().size()];
        return MediaOperations.getSupportedMediaExtensions().toArray(extensionsArray);
    }


    public MediaOperations()
    {
//        this.getEmbeddedMPComp();
        this.embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        this.mediaPlayer = this.embeddedMediaPlayerComponent.getMediaPlayer();
    }

    /**
     * Returns an initialized Singleton instance of the inner embeddedMediaPlayerComponent.
     * @return This instance's Singleton embeddedMediaPlayerComponent object.
     */
    public EmbeddedMediaPlayerComponent getEmbeddedMPComp()
    {
//        if ((this.embeddedMediaPlayerComponent == null) || (this.mediaPlayer == null))
//        {
//            this.embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
//            this.mediaPlayer = this.embeddedMediaPlayerComponent.getMediaPlayer();
//        }

        return this.embeddedMediaPlayerComponent;
    }

    public void startPlayingMedia(String mediaString, long startTimeInMilliseconds)
    {
//        String mediaShortenedPath = mediaString
//                .substring(mediaString.lastIndexOf(File.separatorChar) + 1, mediaString.length());

//        this.frame.setTitle("\"" + this.searchedWord + "\" in " + mediaShortenedPath + "- LARRY");

        this.mediaPlayer.playMedia(mediaString);
        this.mediaPlayer.setTime(startTimeInMilliseconds + this.subtitleDelayMilliseconds);

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
        if (newTime < 0 || newTime >= this.mediaPlayer.getLength())
        {
            newTime = 0;
        }

        this.mediaPlayer.setTime(newTime);
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
        this.mediaPlayer.pause();
    }

    public void pauseOrResume()
    {
        this.pause();
    }

    public void skipTimeBy(long skipTimeInMilliseconds)
    {
        this.mediaPlayer.skip(skipTimeInMilliseconds);
    }

    public void setSearchedWord(String searchedWord)
    {
        this.searchedWord = searchedWord;
    }

    public void setSpuDelay()
    {
        this.mediaPlayer.setSpuDelay(this.getDefaultSubtitleDelayInMs());
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
