package LARRY;

import Database.DBLarry;
import subsParser.Caption;

import javax.swing.*;
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
        String resultFileAddress = DBLarry.getAbsoluteFilePathForCaption(caption, "Curb Your Enthusiasm - ", testFolderPath);
        System.out.println("Chosen caption:");
        System.out.println(caption.toString());
        System.out.println("Absolute address:");
        System.out.println(resultFileAddress);
        
        gui.startPlayingMedia(resultFileAddress, caption.start.getMseconds());
    }
    
    private static void test2(GUI gui, DBLarry DB)
    {
        final long subtitlesDelay = -52000;
        String wordToFind = "okay";
        
        
        List<Caption> results;
        try
        {
            results = DB.getAllCaptionsFor(wordToFind, 60);
            //remove captions with starting time after negative delay (fixes videos with extra subs at beginning)
            if (subtitlesDelay < 0)
            {
                results.removeIf(caption -> caption.start.getMseconds() < -subtitlesDelay);
            }
        }
        catch (SQLException e)
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
        
        String testFolderPath = "C:\\Itamar\\Workspace\\Larry\\LARRY\\resources\\temporary";
        Caption firstCaption = results.get(0);
        String resultFileAddress = DBLarry.getAbsoluteFilePathForCaption(firstCaption, "Dirk", testFolderPath);
        /*
        System.out.println("First caption:");
        System.out.println(firstCaption.toString());
        System.out.println("Absolute address:");
        System.out.println(resultFileAddress);
        */
        gui.setSubtitleDelay(subtitlesDelay); //hardcoded value for one specific video, don't keep this
        gui.startPlayingMedia(resultFileAddress, 0);
        
        gui.setToTimestampWithSubtitleDelay(firstCaption.start.getMseconds());
    }
    
    // Temporary for TESTING
    public static void main(String[] args) throws SQLException
    {
        DBLarry DB = new DBLarry();
        
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            test2(gui, DB);
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
    
}
