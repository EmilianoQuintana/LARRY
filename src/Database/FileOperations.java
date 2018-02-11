package Database;

import LARRY.Messages;
import subsParser.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations
{
    private static final int MAX_FILES_TO_ADD = 999;
    private static final String REGEX_SxxExx = "[Ss]\\d\\d[Ee]\\d\\d"; // some characters, then: S__E__ (with digits), then some characters

    public static void updateSubsCollectionFromFolder(SubsCollection subsCollection, String filePrefix,
                                                      String folderPath)
            throws Messages.EmptyFolderException
    {
        File workingFolder = new File(folderPath);
        File[] filesInFolder = workingFolder.listFiles();

        int amount = 0;

        if (filesInFolder == null)
        {
            throw new Messages.EmptyFolderException(folderPath);
        }
        for (File currFile : filesInFolder)
        {
            amount++;
            if (amount > FileOperations.MAX_FILES_TO_ADD)
            {
                Messages.printInConsole(new Messages.MaximumAmountOfFilesAddedException().getMessage());
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
                        Messages.printInConsole(
                                new Messages.FileDoesNotStartWithPrefixException(filePrefix).getMessage());
                        continue;
                    }
                    if (subsCollection.hasFileInLibrary(name))
                    {
                        Messages.printInConsole(new Messages.FileAlreadyInLibraryException(name).getMessage());
                        continue;
                    }
                    else
                    {
                        subsCollection.addFileNameToLibrary(name);
                        Messages.printInConsole(Messages.MSG_ADDING_FILE);
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

                    TimedTextObject tto = timedTextFileFormat
                            .parseFile(currFile.getName(), fileInputStream, seasonNum, episodeNum);

                    for (Caption c : tto.captions.values())
                    {
                        subsCollection.addCaption(c);
                    }

                    Messages.printInConsole(Messages.MSG_ADDED_FILE); //continues from the "Adding..." line


                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns the extension of a given file name.
     * <p>
     * If there is no extension, returns an empty string.
     * @return File Extension after the dot character. e.g. "SomeSubtitles.srt" will return "srt"
     */
    private static String getFileExtension(String fileName)
    {
        try
        {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } catch (Exception e)
        {
            return "";
        }
    }

    /**
     * Finds the SeasonNumber and EpisodeNumber in a given FileName formatted with 'SxxExx' in it.
     * @param fileName FileName (The extension is irrelevant).
     * @return <code>Array (type int, length 2)</code> with the found SeasonNumber and EpisodeNumber.
     * @exception Messages.FileNotFormattedWithSxxExxException
     * if the given FileName is not formatted with 'SxxExx' in it.
     */
    private static int[] parseSxxExxFromFilename(String fileName)
            throws Messages.FileNotFormattedWithSxxExxException
    {
        int seasonNum, episodeNum;
        Pattern pattern = Pattern.compile(REGEX_SxxExx);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find())
        {
            String result = matcher.group();
            seasonNum = Integer.parseInt(result.substring(1, 3));
            episodeNum = Integer.parseInt(result.substring(4, 6));
        }
        else
        {
            throw new Messages.FileNotFormattedWithSxxExxException(fileName);
        }

        return new int[]{seasonNum, episodeNum};
    }
}
