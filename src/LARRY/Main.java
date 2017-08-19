package LARRY;

import subsParser.SubsCollection;

import java.sql.SQLException;

public class Main
{

    /*
     * This code was copied from ConditionTest.java that is in:
     * [project folder]/vlcj-master/src/test/java/uk/co/caprica/vlcj/test/condition/ConditionTest.java
     */
    static String dbCreateTablesUpd = "" +
            // Words
            "CREATE TABLE t_words (word VARCHAR(255) NOT NULL PRIMARY KEY, word_id INT NOT NULL AUTO_INCREMENT); " +
            // Captions
            "CREATE TABLE t_captions (caption_id INT NOT NULL AUTO_INCREMENT, " +
            "season_num INT NOT NULL, episode_num INT NOT NULL, " +
            "start TIME(3) NOT NULL, end TIME(3) NOT NULL, content VARCHAR(255) NOT NULL ); " +
            // Words to Captions
            "CREATE TABLE t_words_to_captions (word_id INT NOT NULL PRIMARY KEY, caption_id INT NOT NULL); " +
            // Files seen
            "CREATE TABLE t_files_seen (file_name varchar(255) NOT NULL PRIMARY KEY, file_id INT AUTO_INCREMENT); ";

    public static void printCaptionsForWord(String databaseName, String word)
    {

    }

    public static SubsCollection initializeDatabase(String databaseName) throws SQLException
    {
        DatabaseOperations databaseOperator = new DatabaseOperations();
        databaseOperator.createDatabaseAndLogin(databaseName);
        databaseOperator.executeUpdate(dbCreateTablesUpd);
        return new SubsCollection(databaseOperator);
    }

    // Temporary for TESTING
    public static void main(String[] args) throws Exception
    {
        SubsCollection collection = initializeDatabase("test curb");

        collection.addFileNameToLibrary("Curb file number one");
        collection.addFileNameToLibrary("Curb file number two");
        collection.addFileNameToLibrary("Curb file number two");











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
