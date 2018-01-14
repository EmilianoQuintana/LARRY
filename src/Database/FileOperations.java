package Database;

import subsParser.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations
{
    private static final int MAX_FILES_TO_ADD = 999;
    private static final String regex_SxxExx = "[Ss]\\d\\d[Ee]\\d\\d"; // some characters, then: S__E__ (with digits), then some characters

    public static void updateSubsCollectionFromFolder(SubsCollection subsCollection, String filePrefix, String folderPath)
    {
        File workingFolder = new File(folderPath);
        File[] filesInFolder = workingFolder.listFiles();

        int amount = 0;
        if (filesInFolder != null)
        {
            for (File currFile : filesInFolder)
            {
                amount++;
                if (amount > MAX_FILES_TO_ADD)
                {
                    System.out.println("Maximum amount of files added! Returning to calling function.");
                    break;
                }
                if (currFile.isFile())
                {
                    try
                    {
                        String name = currFile.getName();

                        TimedTextFileFormat timedTextFileFormat;
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
                                timedTextFileFormat = null;
                                break; //will simply ignore the file
                        }
                        if (timedTextFileFormat == null)
                        {
                            continue;
                        }
                        
                        System.out.print(name + "\t\t");
                        if (!name.startsWith(filePrefix))
                        {
                            System.out.println("***** Doesn't start with Prefix " + filePrefix + " ***** ");
                            continue;
                        }
                        if (subsCollection.hasFileInLibrary(name))
                        {
                            System.out.println("Already in library! ");
                            continue;
                        }
                        else
                        {
                            subsCollection.addFileNameToLibrary(name);
                            System.out.print("Adding...");
                        }

                        int[] seasonAndEpisode = FileOperations.parseSxxExxFromFilename(name);
                        int seasonNum = Caption.NO_SEASON;
                        int episodeNum = Caption.NO_EPISODE;
                        if (seasonAndEpisode != null)
                        {
                            seasonNum = seasonAndEpisode[0];
                            episodeNum = seasonAndEpisode[1];
                        }

                        InputStream fileInputStream = new FileInputStream(currFile);

                        TimedTextObject tto = timedTextFileFormat.parseFile(currFile.getName(), fileInputStream, seasonNum, episodeNum);

                        for (Caption c : tto.captions.values())
                        {
                            subsCollection.addCaption(c);
                        }

                        System.out.println("...Added!"); //continues from the "Adding..." line


                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * (will return empty string if there is no extension.)
     *
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

    /**
     *
     * @param name file name (extension doesn't matter)
     * @return an array - {seasonNum, episodeNum} - if file name contains a 'SxxExx' style string, or null otherwise.
     */
    private static int[] parseSxxExxFromFilename(String name)
    {
        int seasonNum, episodeNum;
        Pattern pattern = Pattern.compile(regex_SxxExx);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())
        {
            String result = matcher.group();
            seasonNum = Integer.parseInt(result.substring(1, 3));
            episodeNum = Integer.parseInt(result.substring(4, 6));
        }
        else
        {
            return null;
        }

        return new int[]{seasonNum, episodeNum};
    }
}