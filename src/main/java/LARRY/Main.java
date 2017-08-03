package LARRY;

import java.io.File;
import java.io.FileInputStream;

import subsParser.Caption;
import subsParser.FormatSRT;
import subsParser.SubsCollection;
import subsParser.TimedTextObject;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.condition.Condition;
import uk.co.caprica.vlcj.player.condition.UnexpectedErrorConditionException;
import uk.co.caprica.vlcj.player.condition.UnexpectedFinishedConditionException;
import uk.co.caprica.vlcj.player.condition.conditions.PausedCondition;
import uk.co.caprica.vlcj.player.condition.conditions.PlayingCondition;
import uk.co.caprica.vlcj.player.condition.conditions.SnapshotTakenCondition;
import uk.co.caprica.vlcj.player.condition.conditions.TimeReachedCondition;

public class Main {

/*
 * This code was copied from ConditionTest.java that is in:
 * [project folder]/vlcj-master/src/test/java/uk/co/caprica/vlcj/test/condition/ConditionTest.java
 */

/*
 * Demonstration of the synchronous approach to media player programming when
 * using media player condition objects.
 * <p>
 * This example generates a series of snapshots for a video file.
 * <p>
 * The snapshots will be saved in the current directory.
 * <p>
 * Specify two options on the command-line: first the MRL to play, second the
 * period at which to take snapshots (e.g. "20" for every 20 seconds).

/*

*    // Some standard options for headless operation
*    private static final String[] VLC_ARGS = {
*        "--intf", "dummy",          // no interface 
        "--vout", "dummy",          // we don't want video (output) 
        "--no-audio",               //.we don't want audio (decoding) 
        "--no-osd",
        "--no-spu",
        "--no-stats",               // no stats 
        "--no-sub-autodetect-file", // we don't want subtitles 
        "--no-inhibit",             // we don't want interfaces 
        "--no-disable-screensaver", // we don't want screensavers 
        "--no-snapshot-preview",    // no blending in dummy vout 
    };
    
    */
    
    // Temporary for TESTING
    public static void main(String[] args) throws Exception
    {
        SubsCollection collection = new SubsCollection();

        String filepath = "O:\\WD Backup.swstor\\Cuky\\ZWQ4MTk0NmUxZGRmNDYxNG\\Backup Personal (saved partition)\\Documents\\Programming\\Eclipse\\LARRY\\src\\main\\resources\\Avengers.2012.Eng.Subs.srt";
        String fileName = "Avengers";
        FormatSRT formatter = new FormatSRT();
        File file = new File(filepath);
        FileInputStream fis = new FileInputStream(file);
        TimedTextObject tto = formatter.parseFile(fileName, fis, 0, 0);
        for (Caption c : tto.captions.values())
            collection.addCaption(c);

        for (Caption c : collection.getAllCaptionsFor("VeNge"))
            System.out.println(c);
        
        
        
        
        
        
        
        /*
        if(args.length != 2) {
            System.err.println("Usage: <mrl> <seconds>");
            System.exit(1);
        }

        final String mrl = args[0];
        final int period = Integer.parseInt(args[1]) * 1000;

        MediaPlayerFactory factory = new MediaPlayerFactory(args);
        MediaPlayer mediaPlayer = factory.newHeadlessMediaPlayer();

        mediaPlayer.setSnapshotDirectory(new File(".").getAbsolutePath());

        // The sequence for creating the snapshots is...
        //
        // Start the media
        // Wait until playing
        // Loop...
        //  Set the target time
        //  Wait until the target time is reached
        //  Pause the media player
        //  Wait until paused
        //  Save the snapshot
        //  Wait until snapshot taken
        //  Play the media player
        //
        // The media player must be playing or else the required time changed events
        // will not be fired.

        try {
            Condition<?> playingCondition = new PlayingCondition(mediaPlayer) {
                @Override
                protected boolean onBefore() {
                    // You do not have to use onBefore(), but sometimes it is very convenient, and guarantees
                    // that the required media player event listener is added before your condition is tested
                    mediaPlayer.startMedia(mrl);
                    return true;
                }
            };
            playingCondition.await();

            long time = period;

            for(int i = 0; ; i++) {

                // Some special cases here...
                //
                // 1. The duration may not be available yet, even if the media player is playing
                // 2. For some media types it is not possible to set the position past the end - this
                //    means that you would have to wait for playback to reach the end normally
                long duration = mediaPlayer.getLength();
                if(duration > 0 && time >= duration) {
                    break;
                }

                System.out.println("Snapshot " + i);

                Condition<?> timeReachedCondition = new TimeReachedCondition(mediaPlayer, time) {
                    @Override
                    protected boolean onBefore() {
                        mediaPlayer.setTime(targetTime);
                        return true;
                    }
                };
                timeReachedCondition.await();

                Condition<?> pausedCondition = new PausedCondition(mediaPlayer) {
                    @Override
                    protected boolean onBefore() {
                        mediaPlayer.pause();
                        return true;
                    }
                };
                pausedCondition.await();

                Condition<?> snapshotTakenCondition = new SnapshotTakenCondition(mediaPlayer) {
                    @Override
                    protected boolean onBefore() {
                        mediaPlayer.saveSnapshot();
                        return true;
                    }
                };
                snapshotTakenCondition.await();

                playingCondition = new PlayingCondition(mediaPlayer) {
                    @Override
                    protected boolean onBefore() {
                        mediaPlayer.play();
                        return true;
                    }
                };
                playingCondition.await();

                time += period;
            }
        }
        catch(UnexpectedErrorConditionException e) {
            System.out.println("ERROR!");
        }
        catch(UnexpectedFinishedConditionException e) {
            System.out.println("FINISHED!");
        }

        System.out.println("All done");

        mediaPlayer.release();
        factory.release();
        
        */
    }
   
}
