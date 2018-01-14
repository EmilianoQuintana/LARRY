package LARRY;

import Database.DBLarry;
import subsParser.Caption;

import java.sql.SQLException;
import java.util.List;

public class Main
{
    private static void test1(GUI gui, DBLarry DB)
    {
        List<Caption> results;
        try
        {
            results = DB.getAllCaptionsFor("love", 60);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return;
        }
        
        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }
        
        String testFolderPath = "O:\\Movies\\aaa TV SHOWS\\Curb Your Enthusiasm - Seasons 1-6 + Extras" +
                "\\Curb Your Enthusiasm - Season 3";
        Caption caption = results.get(0);
        String resultFileAddress = DBLarry
                .findAbsoluteFilePathForCaption(caption, testFolderPath, "Curb Your Enthusiasm - ");
        System.out.println("Chosen caption:");
        System.out.println(caption.toString());
        System.out.println("Absolute address:");
        System.out.println(resultFileAddress);
        
        gui.PlayMedia(resultFileAddress, caption.start.getMseconds());
    }
    
    private static void test2(GUI gui, DBLarry DB)
    {
        List<Caption> results;
        try
        {
            results = DB.getAllCaptionsFor("dirk", 60);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return;
        }
        
        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }
        
        String testFolderPath = "C:\\Itamar\\Workspace\\Larry\\LARRY\\resources\\temporary";
        Caption caption = results.get(0);
        String resultFileAddress = DBLarry.findAbsoluteFilePathForCaption(caption, testFolderPath, "Dirk");
        System.out.println("Chosen caption:");
        System.out.println(caption.toString());
        System.out.println("Absolute address:");
        System.out.println(resultFileAddress);
        
        gui.PlayMedia(resultFileAddress, caption.start.getMseconds());
    }
    
    // Temporary for TESTING
    public static void main(String[] args) throws SQLException
    {
        DBLarry DB = new DBLarry();
        /*
        String filePrefix = "Dirk";
        String folderPath = "C:\\Itamar\\Workspace\\Larry\\LARRY\\resources\\temporary";
        DB.updateSubsCollectionFromFolder(filePrefix, folderPath);
        */
        /*
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            test2(gui, DB);
        });
*/
        long t1 = System.currentTimeMillis();
        List<Caption> results = DB.getAllCaptionsFor("you", 600);
        long t2 = System.currentTimeMillis();
        
        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }
    
        System.out.println("Number of results:\t\t"+results.size());
        System.out.println("Total time:\t\t"+(t2-t1)+" ms");

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
