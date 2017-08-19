package LARRY;

import subsParser.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations
{
    private static String regex_SxxExx = "[Ss]\\d\\d[Ee]\\d\\d"; // some characters, then: S__E__ (with digits), then some characters

    public static void updateSubsCollectionFromFolder(SubsCollection subsCollection, String filePrefix, String folderPath)
    {
        File workingFolder = new File(folderPath);
        File[] filesInFolder = workingFolder.listFiles();

    	/*
        FileReader     fileReader = null;
    	BufferedReader buffReader = null;
    	TimedTextFileFormat ttff;
		OutputStream output = null;
		*/

        int amount = 0;
        for (File currFile : filesInFolder)
        {
            amount++;
            if (amount > 6)
                break;
            if (currFile.isFile())
            {
                try
                {
                    String name = currFile.getName();
                    System.out.println(name);
                    if (!name.startsWith(filePrefix))
                        continue;
                    if (subsCollection.hasFileInLibrary(name))
                        continue;
                    else
                        subsCollection.addFileNameToLibrary(name);

                    TimedTextFileFormat timedTextFileFormat = null;
                    String extension = getFileExtension(name).toLowerCase();
                    switch (extension)
                    {
                        case "srt":
                            timedTextFileFormat = new FormatSRT();
                            break;
                        case "ass":
                            timedTextFileFormat = new FormatASS();
                            break;
                        case "scc":
                            timedTextFileFormat = new FormatSCC();
                            break;
                        case "stl":
                            timedTextFileFormat = new FormatSTL();
                            break;
                        case "ttml":
                            timedTextFileFormat = new FormatTTML();
                            break;
                        default:
                            break; //will simply ignore the file
                    }
                    if (timedTextFileFormat == null)
                        continue;

                    int[] seasonAndEpisode = FileOperations.parseSeasonEpisodeFromFileName(name);
                    if (seasonAndEpisode != null)
                    {
                        int seasonNum = seasonAndEpisode[0];
                        int episodeNum = seasonAndEpisode[1];
                        InputStream fileInputStream = new FileInputStream(currFile);

                        TimedTextObject tto = timedTextFileFormat.parseFile(currFile.getName(), fileInputStream, seasonNum, episodeNum);

                        for (Caption c : tto.captions.values())
                            subsCollection.addCaption(c);

                    } else //File's name did not contain SxxExx
                    {
                        System.out.println("This file's name does not have season and episode numbers and will be ignored:    " + currFile.getName());
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * @return extension without the dot. e.g. "blah.srt" will return "srt"
     */
    private static String getFileExtension(String fileName)
    {
        try
        {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private static int[] parseSeasonEpisodeFromFileName(String name)
    {
        int seasonNum, episodeNum;
        Pattern pattern = Pattern.compile(regex_SxxExx);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())
        {
            String result = matcher.group();
            seasonNum = Integer.parseInt(result.substring(1, 3));
            episodeNum = Integer.parseInt(result.substring(4, 6));
        } else
            return null;

        return new int[]{seasonNum, episodeNum};
    }
}
