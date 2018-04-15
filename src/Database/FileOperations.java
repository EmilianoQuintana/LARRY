package Database;

import LARRY.Messages;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import subsParser.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations
{
    private static final String ALL_FILE_EXTENSIONS = "*.*";
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
            if (amount > SubsCollection.MAX_FILES_TO_ADD)
            {
                Messages.printInConsole(new Messages.MaximumAmountOfFilesAddedException().getMessage());
                break;
            }
            if (currFile.isFile())
            {
                try
                {
                    String name = currFile.getName();
                    String extension = FileOperations.getCleanExtension(name);

                    TimedTextFileFormat timedTextFileFormat;

                    switch (extension)
                    {
                        case Const.SUBS_FORMAT_SRT:
                            timedTextFileFormat = new FormatSRT();
                            break;
                        case Const.SUBS_FORMAT_ASS:
                            timedTextFileFormat = new FormatASS();
                            break;
                        case Const.SUBS_FORMAT_SCC:
                            timedTextFileFormat = new FormatSCC();
                            break;
                        case Const.SUBS_FORMAT_STL:
                            timedTextFileFormat = new FormatSTL();
                            break;
                        case Const.SUBS_FORMAT_TTML:
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

    //region Getters for Extensions (original and Clean)

    /**
     * Returns the clean, lower-case extension of a given File.
     *
     * @param file The desired file to get the extension of.
     * @return The extension of the given File, cleaned and in lower-case.
     */
    public static String getCleanExtension(File file)
    {
        return FileOperations.getCleanExtension(file.getName());
    }

    /**
     * Returns the clean, lower-case extension of a given filename.
     *
     * @param fileName The desired filename to get the extension of.
     * @return The extension of the given filename, cleaned and in lower-case.
     */
    public static String getCleanExtension(String fileName)
    {
        return FilenameUtils.getExtension(fileName).toLowerCase();
    }

    /**
     * Returns the clean, lower-case extensions of multiple given files.
     *
     * @param files File Array of the desired files.
     * @return A String Array of file extensions from the given files, cleaned and in lower-case.
     */
    public static String[] getCleanExtensions(File[] files)
    {
        String[] extensions = null;

        if ((files != null) && (files.length > 0))
        {
            extensions = new String[files.length];

            for (int iCurrFile = 0; iCurrFile < files.length; iCurrFile++)
            {
                extensions[iCurrFile] = FileOperations.getCleanExtension(files[iCurrFile]);
            }

        }

        return extensions;
    }

    /**
     * Returns the clean, lower-case extensions of multiple given filenames.
     *
     * @param filenames String Array of the desired filenames.
     * @return A String Array of file extensions from the given filenames, cleaned and in lower-case.
     */
    public static String[] getCleanExtensions(String[] filenames)
    {
        String[] extensions = null;

        if ((filenames != null) && (filenames.length > 0))
        {
            extensions = new String[filenames.length];

            for (int iCurrFile = 0; iCurrFile < filenames.length; iCurrFile++)
            {
                extensions[iCurrFile] = FileOperations.getCleanExtension(filenames[iCurrFile]);
            }
        }

        return extensions;
    }

    /**
     * Returns the extension of a given file name.
     * <p>
     * If there is no extension, returns an empty string.
     *
     * @return File Extension after the dot character. e.g. "SomeSubtitles.srt" will return "srt"
     */
    private static String getFileExtension(String fileName)
    {
        return FilenameUtils.getExtension(fileName);
    }

    /**
     * Returns the extension of a given file.
     * <p>
     * If there is no extension, returns an empty string.
     *
     * @return File Extension after the dot character. e.g. "SomeSubtitles.srt" will return "srt"
     */
    private static String getFileExtension(File file)
    {
        return FileOperations.getFileExtension(file.getName());
    }

    //endregion

    //region Getters for List of Files from Folder

    /**
     * Returns a list of all the files within a given folder.
     *
     * @param folderPath Path of the desired folder
     * @return Array of files in the given folder.
     */
    public static File[] getFilesFromFolder(String folderPath)
    {
        return new File(folderPath).listFiles();
    }

    /**
     * Returns a list of all the files, of a certain extension, that are in a desired folder.
     *
     * @param folderPath Path of the desired folder
     * @param extension  Desired extensions of the files.
     * @return File Array of the files found.
     */
    public static File[] getFilesFromFolder(String folderPath, String extension)
    {
        String[] extensions = new String[1];
        extensions[0] = extension;

        return FileOperations.getFilesFromFolder(folderPath, extensions, false);
    }

    /**
     * Returns a list of all the files, of certain extensions, that are in a desired folder.
     * You can choose to also search the inner folders that are inside the given folder (recursive search).
     * @param folderPath      Path of the desired folder
     * @param extensions      Array of the desired extensions
     * @param recursiveSearch Whether to also search the inner folders inside the given folder
     * @return File Array of the files found.
     */
    public static File[] getFilesFromFolder(String folderPath, String[] extensions,
                                            boolean recursiveSearch)
    {
        File folder = new File(folderPath);

        //WildcardFileFilter wildcardFileFilter = new WildcardFileFilter("*." + extension.getExtension());
        Collection<File> filesCollection = FileUtils
                .listFiles(folder, FileOperations.getCleanExtensions(extensions), recursiveSearch);
        File[] filesArray = new File[filesCollection.size()];

        return filesCollection.toArray(filesArray);
    }

    /**
     * Returns a list of all the Subtitle files that are in a desired folder.
     *
     * @param folderPath Path of the desired folder.
     * @return File Array of the Subtitle files found.
     */
    public static File[] getSubtitleFilesFromFolder(String folderPath)
    {
        return FileOperations.getSubtitleFilesFromFolder(folderPath, Const.getSupportedSubtitlesFormats());
    }

    /**
     * Returns all the Subtitle files of a given format that are in a desired folder.
     *
     * @param folderPath Path of the desired folder.
     * @param subsFormat Desired format of the subtitle files.
     * @return File Array of the Subtitle files found.
     */
    public static File[] getSubtitleFilesFromFolder(String folderPath, String subsFormat)
    {
        String[] subsFormats = new String[1];
        subsFormats[0] = subsFormat;

        return FileOperations.getSubtitleFilesFromFolder(folderPath, subsFormats);
    }

    /**
     * This method is PRIVATE by design! Do not turn into public. I don't want the ability to search only SOME of
     * all the supported subtitle formats instead of all of them.
     * Returns a list of all the Subtitle files of multiple given formats that are in a desired folder.
     *
     * @param folderPath  Path of the desired folder.
     * @param subsFormats Desired formats of the subtitle files.
     * @return File Array of the Subtitle files found.
     */
    private static File[] getSubtitleFilesFromFolder(String folderPath, String[] subsFormats)
    {
        return FileOperations.getFilesFromFolder(folderPath, subsFormats, false);
    }

    /**
     * Returns all the Video files that are in a given folder.
     *
     * @param folderPath Path of the desired folder.
     * @return File Array of the Video files found.
     */
    public static File[] getVideoFilesFromFolder(String folderPath)
    {
        return FileOperations.getFilesFromFolder(folderPath, MediaOperations.getSupportedVideoExtensions(), false);
    }

    /**
     * Searches a given folder for Video and Subtitle files, and tries to match pairs of corresponding ones.
     *
     * @param folderPath      Path of the desired folder.
     * @param recursiveSearch Whether to also search the inner folders inside the given folder.
     * @return //TODO What data structure to use for storing matching pairs of files?
     */
    public static Pair<File, File> getMatchingVideoAndSubtitleFiles(String folderPath, boolean recursiveSearch)
    {
        int requiredExtensionsAmount = MediaOperations.getSupportedSubtitleFormats().length +
                MediaOperations.getSupportedVideoExtensions().length;
        String[] requiredExtensions = new String[requiredExtensionsAmount];
        File[] filesInFolder = FileOperations.getFilesFromFolder(folderPath, requiredExtensions, false);

//        filesInFolder.
//        Pair<File, File> videoPairs;
//        for (:
//             )
//        {
//
//        }

//        return videoToSubtitleMap;

        return null;
    }

    //endregion

    /**
     * Checks if a specified file is supported by LARRY.
     * @param checkedFile Desired file to check.
     * @return True if the file is supported, otherwise false.
     */
    public static boolean checkIfFileIsSupported(File checkedFile)
    {
        boolean isSupported;

        isSupported = Const.getSupportedSubtitlesFormatsSet()
                .contains(FileOperations.getCleanExtension(checkedFile));

        if (!isSupported)
        {
            isSupported = MediaOperations.getSupportedVideoExtensionsSet().contains(checkedFile);
        }

        return isSupported;
    }

    /**
     * Finds the SeasonNumber and EpisodeNumber in a given FileName formatted with 'SxxExx' in it.
     *
     * @param fileName FileName (The extension is irrelevant).
     * @return <code>Array (type int, length 2)</code> with the found SeasonNumber and EpisodeNumber.
     * @throws Messages.FileNotFormattedWithSxxExxException if the given FileName is not formatted with 'SxxExx' in it.
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

        //// TEST - TEMPORARY


    }
}
