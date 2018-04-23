package LARRY;

import Database.FileOperations;
import Database.MediaOperations;
import Database.SubsCollection;
import subsParser.Caption;

import javax.swing.*;
import javax.tools.FileObject;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class GUI
{
    //    private static final long EXTRA_TIME_BEFORE_CAPTION = 491;
//    private static final long EXTRA_TIME_AFTER_CAPTION = 100;
    private final JFrame frame;
    private MediaOperations mediaOperations;
    //    private final EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent;
//    private EmbeddedMediaPlayer mediaPlayer;
    private JTextArea subtitleDelayAmountText;
//    private long subtitleDelayMilliseconds;
//    private int lastMarkedCaptionIndex;
//    private List<Caption> markedCaptionMoments;
//    private String searchedWord;

    public GUI()
    {
        // Setting up Media Player Frame with a MediaOperations object:
        this.frame = new JFrame("LARRY app: no video playing");
        this.frame.setBounds(100, 100, 720, 480);
        this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                GUI.this.mediaOperations.getEmbeddedMPComp().release();
                GUI.this.frame.dispose();
            }
        });

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(this.mediaOperations.getEmbeddedMPComp(), BorderLayout.CENTER);

        //region Adding the Controls to GUI object:

        JPanel controlsPane = new JPanel();

        JButton pauseButton = new JButton("||");//"\u23F8");
        pauseButton.addActionListener(event -> {
            this.pauseOrResume();
            pauseButton.setText(pauseButton.getText().equals("▶") ? "||" : "▶");
        });

        pauseButton.setFont(Font.getFont("dialog"));
        controlsPane.add(pauseButton);

        JButton rewindButton = new JButton("⏪");
        rewindButton.addActionListener(event -> this.skipTimeBy(-10000));
        controlsPane.add(rewindButton);

        JButton skipButton = new JButton("⏩");
        skipButton.addActionListener(event -> this.skipTimeBy(10000));
        controlsPane.add(skipButton);

        JButton decreaseSubtitleDelay = new JButton("- Sub Delay");
        decreaseSubtitleDelay.addActionListener(event -> this.smartChangeSubsDelay(false));
        controlsPane.add(decreaseSubtitleDelay);

        JButton increaseSubtitleDelay = new JButton("+ Sub Delay");
        increaseSubtitleDelay.addActionListener(event -> this.smartChangeSubsDelay(true));
        controlsPane.add(increaseSubtitleDelay);

        this.subtitleDelayAmountText = new JTextArea("0");
        controlsPane.add(this.subtitleDelayAmountText);

        JButton prevCaptionButton = new JButton("Prev");
        prevCaptionButton.addActionListener(event -> this.skipToNextOrPrevCaption(false));
        controlsPane.add(prevCaptionButton);

        JButton nextCaptionButton = new JButton("Next");
        nextCaptionButton.addActionListener(event -> this.skipToNextOrPrevCaption(true));
        controlsPane.add(nextCaptionButton);

        //endregion

        contentPane.add(controlsPane, BorderLayout.SOUTH);

        this.frame.setContentPane(contentPane);

        this.frame.setVisible(true);
    }

    //region Comment this out, I moved all to inside this MediaOperations object:

    public void startPlayingMedia(String mediaString, long startTimeInMilliseconds)
    {
//        String mediaShortenedPath = mediaString
//                .substring(mediaString.lastIndexOf(File.separatorChar) + 1, mediaString.length());
//
//        this.frame.setTitle("\"" + this.searchedWord + "\" in " + mediaShortenedPath + "- LARRY");
        this.frame.setTitle(this.mediaOperations.getShortenedVideoTitle(mediaString));

//        this.mediaPlayer.playMedia(mediaString);
        this.mediaOperations.startPlayingMedia(mediaString, startTimeInMilliseconds);

//        this.mediaPlayer.setTime(startTimeInMilliseconds + this.mediaOperations.subtitleDelayMilliseconds);
//        if (this.subtitleDelayMilliseconds != 0)
//        {
//            //A-sync problem fix
//            this.mediaPlayer.setSpuDelay(this.mediaOperations.subtitleDelayMilliseconds * 1000);
//        }
    }

    private void pauseOrResume()
    {
        this.mediaOperations.pause();
    }

    /**
     * Will not allow negative times!
     */
    public void setToTimestampWithSubtitleDelay(long timeInMilliseconds)
    {
//        long newTime = timeInMilliseconds + this.mediaOperations.subtitleDelayMilliseconds;
//        if (newTime < 0 || newTime >= this.mediaPlayer.getLength())
//        {
//            newTime = 0;
//        }
//
//        this.mediaPlayer.setTime(newTime);

        this.mediaOperations.setToTimestampWithSubtitleDelay(timeInMilliseconds);
    }

    private void skipTimeBy(long skipTimeInMilliseconds)
    {
//        this.mediaPlayer.skip(skipTimeInMilliseconds);
        this.mediaOperations.skipTimeBy(skipTimeInMilliseconds);
    }

    //region Subtitles manipulation methods

    public void setSubtitleDelay(long delayInMilliseconds)
    {
//        this.mediaOperations.subtitleDelayMilliseconds = delayInMilliseconds;
//        this.mediaPlayer.setSpuDelay(this.mediaOperations.subtitleDelayMilliseconds * 1000);
        this.mediaOperations.setSubtitleDelay(delayInMilliseconds);

        this.subtitleDelayAmountText.setText(String.format("%d", this.mediaOperations.getSubtitleDelay()) + " ms");
    }

    /**
     * This is sort of weird functionality, later I'll probably remove this or change
     * it to depend on frequency of clicks in a short time period ~~~~ Itamar
     *
     * @param increaseOrDecrease you know
     */
    private void smartChangeSubsDelay(boolean increaseOrDecrease)
    {
//        if (increaseOrDecrease == (this.multiplier > 0))
//        {
//            if (this.multiplier < 99 && this.multiplier > -99)
//            {
//                this.multiplier *= 2;
//            }
//        }
//        else
//        {
//            this.multiplier = increaseOrDecrease ? +1 : -1;
//        }
//
//        long deltaAmount = this.multiplier * 100; //100 ms minimum unit
//        this.mediaOperations.subtitleDelayMilliseconds += deltaAmount;
////        this.mediaPlayer.setSpuDelay(this.mediaOperations.subtitleDelayMilliseconds * 1000);
//        this.mediaOperations.setSpuDelay(this.mediaOperations.subtitleDelayMilliseconds * 1000);
        this.mediaOperations.smartChangeSubsDelay(increaseOrDecrease);
        this.subtitleDelayAmountText.setText(String.format("%d", this.mediaOperations.getSubtitleDelay()) + " ms");
    }

    private void skipToCaption(Caption caption)
    {
        //this.setToTimestampWithSubtitleDelay(caption.start.getMseconds() - EXTRA_TIME_BEFORE_CAPTION);
        this.mediaOperations.skipToCaption(caption);
    }

    private void skipToNextOrPrevCaption(boolean nextOrPrev)
    {
//        if (this.markedCaptionMoments.size() == 0)
//        {
//            return;
//        }
//        this.lastMarkedCaptionIndex += nextOrPrev ? +1 : -1;
//
//        //rotate around
//        this.lastMarkedCaptionIndex += this.markedCaptionMoments.size();
//        this.lastMarkedCaptionIndex %= this.markedCaptionMoments.size();
//
//        this.skipToCaption(this.markedCaptionMoments.get(this.lastMarkedCaptionIndex));
        this.mediaOperations.skipToNextOrPrevCaption(nextOrPrev);

    }

    public void setMarkedCaptionMoments(List<Caption> captions)
    {
//        this.markedCaptionMoments = captions;
//        if (this.markedCaptionMoments.size() == 0)
//        {
//            return;
//        }
//
//        this.lastMarkedCaptionIndex = 0;
        this.mediaOperations.setMarkedCaptionMoments(captions);
    }

    public void skipToMarkedCaptionByIndex(int index)
    {
//        if (index < 0 || index >= this.markedCaptionMoments.size())
//        {
//            throw new IndexOutOfBoundsException("No such caption index!");
//        }
//
//        this.lastMarkedCaptionIndex = index;
//        this.skipToCaption(this.markedCaptionMoments.get(this.lastMarkedCaptionIndex));
        this.skipToMarkedCaptionByIndex(index);
    }

    //endregion

    public void setSearchedWord(String searchedWord)
    {
//        this.searchedWord = searchedWord;
        this.mediaOperations.setSearchedWord(searchedWord);
    }

    //region File input methods

    public void inputWholeFolder()
    {
        //TODO display open directory dialog
        String folderPath = null;

        File[] filesInFolder = FileOperations.getFilesFromFolder(folderPath,
                MediaOperations.getSupportedMediaExtensions().toArray(new String[MediaOperations.getSupportedMediaExtensions().size()]),
                false);
    }

    public void inputFolderForSubtitles()
    {

    }
}
