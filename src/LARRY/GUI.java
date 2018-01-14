package LARRY;

import subsParser.Caption;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    
    public GUI()
    {
        frame = new JFrame("LARRY GUI!");
        frame.setBounds(100, 100, 720, 480);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                embeddedMediaPlayerComponent.release();
                frame.dispose();
            }
        });
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        
        //Media player screen area
        embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayer = embeddedMediaPlayerComponent.getMediaPlayer();
        contentPane.add(embeddedMediaPlayerComponent, BorderLayout.CENTER);
        
        //Controls
        JPanel controlsPane = new JPanel();
        
        JButton pauseButton = new JButton("\u23F8");
        pauseButton.addActionListener(event -> {
            this.pauseOrResume();
            pauseButton.setText(pauseButton.getText().equals("▶") ? "\u23F8" : "▶");
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
        
        subtitleDelayAmountText = new JTextArea("0");
        controlsPane.add(subtitleDelayAmountText);
        
        JButton prevCaptionButton = new JButton("Prev");
        prevCaptionButton.addActionListener(event -> this.skipToNextOrPrevCaption(false));
        controlsPane.add(prevCaptionButton);
        
        JButton nextCaptionButton = new JButton("Next");
        nextCaptionButton.addActionListener(event -> this.skipToNextOrPrevCaption(true));
        controlsPane.add(nextCaptionButton);
        
        contentPane.add(controlsPane, BorderLayout.SOUTH);
        
        //Controls action listeners
        
        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }
    
    public void startPlayingMedia(String mediaString, long startTimeInMilliseconds)
    {
        mediaPlayer.playMedia(mediaString);
        mediaPlayer.setTime(startTimeInMilliseconds + subtitleDelayMilliseconds);
        if (subtitleDelayMilliseconds != 0)
        {
            //A-sync problem fix
            mediaPlayer.setSpuDelay(subtitleDelayMilliseconds * 1000);
        }
    }
    
    /**
     * Will not allow negative times!
     */
    public void setToTimestampWithSubtitleDelay(long timeInMilliseconds)
    {
        long newTime = timeInMilliseconds + subtitleDelayMilliseconds;
        if (newTime < 0 || newTime >= mediaPlayer.getLength())
            newTime = 0;
        
        mediaPlayer.setTime(newTime);
    }
    
    public void setSubtitleDelay(long delayInMilliseconds)
    {
        subtitleDelayMilliseconds = delayInMilliseconds;
        mediaPlayer.setSpuDelay(subtitleDelayMilliseconds * 1000);
        subtitleDelayAmountText.setText(String.format("%d", subtitleDelayMilliseconds) + " ms");
    }
    
    private void skipToCaption(Caption caption)
    {
        setToTimestampWithSubtitleDelay(caption.start.getMseconds() - EXTRA_TIME_BEFORE_CAPTION);
    }
    
    private void skipToNextOrPrevCaption(boolean nextOrPrev)
    {
        if (markedCaptionMoments.size() == 0)
            return;
        lastMarkedCaptionIndex += nextOrPrev ? +1 : -1;
        
        //rotate around
        lastMarkedCaptionIndex += markedCaptionMoments.size();
        lastMarkedCaptionIndex %= markedCaptionMoments.size();
        
        skipToCaption(markedCaptionMoments.get(lastMarkedCaptionIndex));
    }
    
    public void setMarkedCaptionMoments(List<Caption> captions)
    {
        markedCaptionMoments = captions;
        if (markedCaptionMoments.size() == 0)
            return;
        
        lastMarkedCaptionIndex = 0;
    }
    
    public void skipToMarkedCaptionByIndex(int index)
    {
        if (index < 0 || index >= markedCaptionMoments.size())
            throw new IndexOutOfBoundsException("No such caption index!");
        
        lastMarkedCaptionIndex = index;
        skipToCaption(markedCaptionMoments.get(lastMarkedCaptionIndex));
    }
    
    /**
     * Belongs to smartChangeSubsDelay.
     */
    private int multiplier = 1;
    
    /**
     * This is sort of weird functionality, later I'll probably remove this or change
     * it to depend on frequency of clicks in a short time period
     * @param increaseOrDecrease
     */
    private void smartChangeSubsDelay(boolean increaseOrDecrease)
    {
        if (increaseOrDecrease == (multiplier > 0))
        {
            if (multiplier < 99 && multiplier > -99)
                multiplier *= 2;
        }
        else
            multiplier = increaseOrDecrease ? +1 : -1;
        
        long deltaAmount = multiplier * 100; //100 ms minimum unit
        subtitleDelayMilliseconds += deltaAmount;
        mediaPlayer.setSpuDelay(subtitleDelayMilliseconds * 1000);
        subtitleDelayAmountText.setText(String.format("%d", subtitleDelayMilliseconds) + " ms");
    }
    
    private void pauseOrResume()
    {
        mediaPlayer.pause();
    }
    
    private void skipTimeBy(long skipTimeInMilliseconds)
    {
        mediaPlayer.skip(skipTimeInMilliseconds);
    }
}
