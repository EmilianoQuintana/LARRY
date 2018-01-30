package LARRY;

import subsParser.Caption;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class GUI
{
    private static final long EXTRA_TIME_BEFORE_CAPTION = 491;
    private static final long EXTRA_TIME_AFTER_CAPTION = 100;
    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent;
    private EmbeddedMediaPlayer mediaPlayer;
    private JTextArea subtitleDelayAmountText;
    private long subtitleDelayMilliseconds;
    private int lastMarkedCaptionIndex;
    private List<Caption> markedCaptionMoments;
    private String searchedWord;

    public GUI()
    {
        this.frame = new JFrame("LARRY app: no video playing");
        this.frame.setBounds(100, 100, 720, 480);
        this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                GUI.this.embeddedMediaPlayerComponent.release();
                GUI.this.frame.dispose();
            }
        });

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        //Media player screen area
        this.embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        this.mediaPlayer = this.embeddedMediaPlayerComponent.getMediaPlayer();
        contentPane.add(this.embeddedMediaPlayerComponent, BorderLayout.CENTER);

        //Controls
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

        contentPane.add(controlsPane, BorderLayout.SOUTH);

        //Controls action listeners

        this.frame.setContentPane(contentPane);
        this.frame.setVisible(true);
    }

    public void startPlayingMedia(String mediaString, long startTimeInMilliseconds)
    {
        String mediaShortenedPath = mediaString
                .substring(mediaString.lastIndexOf(File.separatorChar) + 1, mediaString.length());

        this.frame.setTitle("\"" + this.searchedWord + "\" in " + mediaShortenedPath + "- LARRY");

        this.mediaPlayer.playMedia(mediaString);
        this.mediaPlayer.setTime(startTimeInMilliseconds + this.subtitleDelayMilliseconds);
        if (this.subtitleDelayMilliseconds != 0)
        {
            //A-sync problem fix
            this.mediaPlayer.setSpuDelay(this.subtitleDelayMilliseconds * 1000);
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

    public void setSubtitleDelay(long delayInMilliseconds)
    {
        this.subtitleDelayMilliseconds = delayInMilliseconds;
        this.mediaPlayer.setSpuDelay(this.subtitleDelayMilliseconds * 1000);
        this.subtitleDelayAmountText.setText(String.format("%d", this.subtitleDelayMilliseconds) + " ms");
    }

    private void skipToCaption(Caption caption)
    {
        this.setToTimestampWithSubtitleDelay(caption.start.getMseconds() - EXTRA_TIME_BEFORE_CAPTION);
    }

    private void skipToNextOrPrevCaption(boolean nextOrPrev)
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
     * Belongs to smartChangeSubsDelay.
     */
    private int multiplier = 1;

    /**
     * This is sort of weird functionality, later I'll probably remove this or change
     * it to depend on frequency of clicks in a short time period
     *
     * @param increaseOrDecrease you know
     */
    private void smartChangeSubsDelay(boolean increaseOrDecrease)
    {
        if (increaseOrDecrease == (this.multiplier > 0))
        {
            if (this.multiplier < 99 && this.multiplier > -99)
            {
                this.multiplier *= 2;
            }
        }
        else
        {
            this.multiplier = increaseOrDecrease ? +1 : -1;
        }

        long deltaAmount = this.multiplier * 100; //100 ms minimum unit
        this.subtitleDelayMilliseconds += deltaAmount;
        this.mediaPlayer.setSpuDelay(this.subtitleDelayMilliseconds * 1000);
        this.subtitleDelayAmountText.setText(String.format("%d", this.subtitleDelayMilliseconds) + " ms");
    }

    private void pauseOrResume()
    {
        this.mediaPlayer.pause();
    }

    private void skipTimeBy(long skipTimeInMilliseconds)
    {
        this.mediaPlayer.skip(skipTimeInMilliseconds);
    }

    public void setSearchedWord(String searchedWord)
    {
        this.searchedWord = searchedWord;
    }
}
