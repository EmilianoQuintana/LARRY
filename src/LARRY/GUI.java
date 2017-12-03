package LARRY;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GUI
{
    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent;
    private EmbeddedMediaPlayer mediaPlayer;

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
        JButton pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        JButton rewindButton = new JButton("Rewind");
        controlsPane.add(rewindButton);
        JButton skipButton = new JButton("Skip");
        controlsPane.add(skipButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        //Controls action listeners
        pauseButton.addActionListener(event -> this.pauseOrResume());
        rewindButton.addActionListener(event -> this.skipTime(-10000));
        skipButton.addActionListener(event -> this.skipTime(-10000));

        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }

    public void PlayMedia(String mediaString, long startTimeInMilliseconds)
    {
        mediaPlayer.playMedia(mediaString);
        mediaPlayer.setTime(startTimeInMilliseconds);
    }

    private void pauseOrResume()
    {
        mediaPlayer.pause();
    }

    private void skipTime(long skipTimeInMilliseconds)
    {
        mediaPlayer.skip(skipTimeInMilliseconds);
    }
}
