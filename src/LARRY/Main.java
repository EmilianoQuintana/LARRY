package LARRY;

import Database.DBLarry;
import com.sun.jna.NativeLibrary;
import subsParser.Caption;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class Main
{
    // Temporary for TESTING
    public static void main(String[] args)
            throws SQLException
    {
        DBLarry DB = new DBLarry();

        try
        {
            DB.updateSubsCollectionFromFolder("Curb", "Subtitles\\Subtitles");
        } catch (Messages.EmptyFolderException exEmptyFolder)
        {
            System.out.println("Folder \"Subtitles/Subtitles\" not found when searching" +
                    " for the temporary Curb Your Enthusiasm subtitles.");
        }

        boolean discovered = new NativeDiscovery().discover();
        if (!discovered)
        {
            System.out.println("WARNING: LIBVLC WAS NOT DISCOVERED, APP WILL PROBABLY BREAK");
        }

        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();

            try
            {
                test1(gui, DB);
            }
//            catch (Messages.GhostFolderException e)
//            {
//                e.printStackTrace();
//            }
            catch (Messages.SeasonNumberTooBigException e)
            {
                System.out.println("Error: Season number too big!");
                e.printStackTrace();
            }
        });
        
        /*
        long t1 = System.currentTimeMillis();
        
        String filePrefix = "Dirk";
        String folderPath = "C:\\Itamar\\Workspace\\Larry\\LARRY\\resources\\temporary";
        DB.updateSubsCollectionFromFolder(filePrefix, folderPath);
    
        long t2 = System.currentTimeMillis();
        System.out.println("Total time:\t\t"+(t2-t1)+" ms");

        t1 = System.currentTimeMillis();
        List<Caption> results = DB.getAllCaptionsFor("you", 600);
        t2 = System.currentTimeMillis();
        
        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }
    
        System.out.println("Number of results:\t\t"+results.size());
        System.out.println("Total time:\t\t"+(t2-t1)+" ms");
        
        */

    }

    private static void test1(GUI gui, DBLarry DB)
            throws Messages.SeasonNumberTooBigException
    {
        final long subtitlesDelay = 0;

        final String testFolderPath = "E:\\Movies\\aaa TV SHOWS\\Curb Your Enthusiasm Season 1, 2, 3, 4, 5, 6, 7 & 8 + Extras DVDRip TSV\\Season 3";

        final String wordToFind = "hello";

        System.out.println("Running test 1---");

        List<Caption> results;
        try
        {
            results = DB.getAllCaptionsFor(wordToFind, 60, true);
            //remove captions with starting time after negative delay (fixes videos with extra subs at beginning)
            if (subtitlesDelay < 0)
            {
                results.removeIf(caption -> caption.start.getMseconds() < -subtitlesDelay);
                System.out.println(
                        "Removed some captions because they start too early, " + results.size() + " remaining.");
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            return;
        }

        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }

        gui.setMarkedCaptionMoments(results);

        Caption firstCaption = results.get(0);
        String resultFileAddress;
        try
        {
            resultFileAddress = DBLarry
                    .findAbsoluteFilePathForCaption(firstCaption, testFolderPath, "Curb Your Enthusiasm");

            gui.setSearchedWord(wordToFind);
            gui.setSubtitleDelay(subtitlesDelay); //hardcoded value for one specific video, don't keep this
            gui.startPlayingMedia(resultFileAddress, firstCaption.start.getMseconds());

            System.out.println("Playing media: " + resultFileAddress);
            //gui.setToTimestampWithSubtitleDelay(firstCaption.start.getMseconds());
        } catch (Messages.GhostFolderException e)
        {

        }
    }

    private static void test2(GUI gui, DBLarry DB)
            throws Messages.GhostFolderException, Messages.SeasonNumberTooBigException
    {
        String testFolderPath = "C:\\Itamar\\Workspace\\Larry\\LARRY\\resources\\temporary";
        final long subtitlesDelay = -52000;
        final String wordToFind = "okay";

        System.out.println("Running test 2---");


        List<Caption> results;
        try
        {
            results = DB.getAllCaptionsFor(wordToFind, 60, true);
            //remove captions with starting time after negative delay (fixes videos with extra subs at beginning)
            if (subtitlesDelay < 0)
            {
                results.removeIf(caption -> caption.start.getMseconds() < -subtitlesDelay);
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            return;
        }

        /*
        for (Caption cap : results)
        {
            System.out.println(cap.toString());
        }
        */

        gui.setMarkedCaptionMoments(results);

        Caption firstCaption = results.get(0);
        String resultFileAddress = DBLarry.findAbsoluteFilePathForCaption(firstCaption, "Dirk", testFolderPath);

        gui.setSearchedWord(wordToFind);
        gui.setSubtitleDelay(subtitlesDelay); //hardcoded value for one specific video, don't keep this
        gui.startPlayingMedia(resultFileAddress, 0);
        gui.setToTimestampWithSubtitleDelay(firstCaption.start.getMseconds());
    }

}
