package Database;

import LARRY.Messages;
//import javafx.util.Pair; // was working until it was removed from JDK 11 (September 2018). So we use our own in-house FilePair class.
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import subsParser.Caption;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations
{
    public static class FilePair
    {
        public File key;
        public File value;

        public FilePair(File key, File value) {
            this.key = key;
            this.value = value;
        }
    }

    private static final String ALL_FILE_EXTENSIONS = "*.*";
    private static final String REGEX_SxxExx = "[Ss]\\d\\d[Ee]\\d\\d"; // some characters, then: S__E__ (with digits), then some characters

    /**
     * Enum for sorting collections of files in different ways.
     */
    public enum FileSorter implements Comparator<File>
    {
        // Concept and realization copied from https://codereview.stackexchange.com/a/97812

        //        BY_TYPE(Comparator.comparing(FileSorter::extractExtension)),
        BY_TYPE(Comparator.comparing(FileOperations::getCleanExtension)),
        BY_NAME(Comparator.comparing(File::getName)),
        BY_SIZE(Comparator.comparingLong(File::length));

        private final Comparator<File> comparator;

        /**
         * When creating an instance of this enum, setting the inner comparator as the given one.
         *
         * @param comparator The desired comparator type, one of the available types in this enum.
         */
        private FileSorter(Comparator<File> comparator)
        {
            this.comparator = comparator;
        }

//        private static final String extractExtension(File file)
//        {
//            try
//            {
//                String probeContentType = Files.probeContentType(file.toPath());
//                return probeContentType == null ? "probeContentTypeIsNull" : probeContentType;
//            }
//            catch (IOException e)
//            {
//                return null;
//            }
//        }

        /**
         * Compares two files using a desired comparator type from this enum.
         *
         * @param comparedFile1 The first file to be compared.
         * @param comparedFile2 The second file to be compared.
         * @return a negative integer, zero, or a positive integer, when the first argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(File comparedFile1, File comparedFile2)
        {
            return this.comparator.compare(comparedFile1, comparedFile2);
        }
    }

    /**
     * Scans a given folder for Subtitle files and inserts them into a given SubsCollection object.
     *
     * @param subsCollection SubsCollection object into which to put the found subtitles.
     * @param filePrefix     Fixed prefix for the subtitle files.
     * @param folderPath     Path to the desired folder.
     * @throws Messages.EmptyFolderException
     */
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

                    Messages.printInConsole(name + "\t\t");

                    if (!name.startsWith(filePrefix))
                    {
                        Messages.log(
                                new Messages.FileDoesNotStartWithPrefixException(filePrefix).getMessage());
                        continue;
                    }
                    if (subsCollection.hasFileInLibrary(name))
                    {
                        Messages.log(new Messages.FileAlreadyInLibraryException(name).getMessage());
                        continue;
                    }
                    else
                    {
                        subsCollection.addFileNameToLibrary(name);
                        Messages.log(Messages.MSG_ADDING_FILE);
                    }

                    //region Moved this code into SubsCollection, because it is its responsibility, not FileOperations'.

//                    String extension = FileOperations.getCleanExtension(name);
//
//                    TimedTextFileFormat timedTextFileFormat;
//
//                    switch (extension)
//                    {
//                        case Const.SUBS_FORMAT_SRT:
//                            timedTextFileFormat = new FormatSRT();
//                            break;
//                        case Const.SUBS_FORMAT_ASS:
//                            timedTextFileFormat = new FormatASS();
//                            break;
//                        case Const.SUBS_FORMAT_SCC:
//                            timedTextFileFormat = new FormatSCC();
//                            break;
//                        case Const.SUBS_FORMAT_STL:
//                            timedTextFileFormat = new FormatSTL();
//                            break;
//                        case Const.SUBS_FORMAT_TTML:
//                            timedTextFileFormat = new FormatTTML();
//                            break;
//                        default:
//                            timedTextFileFormat = null;
//                            break; //will simply ignore the file
//                    }
//
//                    // Ignoring empty or unsupported Subtitle files:
//                    if (timedTextFileFormat == null)
//                    {
//                        continue;
//                    }
//
//                    // Getting Season Number and Episode Number from the filename:
//                    int[] seasonAndEpisode = FileOperations.parseSxxExxFromFilename(name);
//                    int seasonNum = Caption.NO_SEASON;
//                    int episodeNum = Caption.NO_EPISODE;
//
//                    // Putting the found SeasonNum and EpisodeNum:
//                    if (seasonAndEpisode != null)
//                    {
//                        seasonNum = seasonAndEpisode[0];
//                        episodeNum = seasonAndEpisode[1];
//                    }
//
//                    // Reading the currently iterated Subtitles file, parsing it and adding to the SubsCollection:
//                    InputStream fileInputStream = new FileInputStream(currFile);
//
//                    TimedTextObject tto = timedTextFileFormat
//                            .parseFile(currFile.getName(), fileInputStream, seasonNum, episodeNum);
//
//                    for (Caption caption : tto.captions.values())
//                    {
//                        subsCollection.addCaption(caption);
//                    }

                    //endregion

                    // Parsing and Adding the file to the SubsCollection library:
                    if (subsCollection.parseFileAndAddCaptionsToLibrary(currFile))
                    {
                        Messages.log(Messages.MSG_ADDED_FILE);    //  "...Added!" - this ends the current line that was started with "Adding..." [filename]
                    }

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
        return FilenameUtils.getExtension(fileName).toLowerCase(Locale.ENGLISH);
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

//    Unneeded. We only need to get the supported files.
//    /**
//     * Returns a list of all the files within a given folder.
//     *
//     * @param folderPath Path of the desired folder
//     * @return Array of files in the given folder.
//     */
//    public static File[] getFilesFromFolder(String folderPath)
//    {
//        return new File(folderPath).listFiles();
//    }

//    I've discovered the java syntax for single/multiple parameters (String... extensions), so I commented out this variation. ~~~~ Cuky
//    /**
//     * Returns a list of all the files, of a certain extension, that are in a desired folder.
//     *
//     * @param folderPath Path of the desired folder
//     * @param extension  Desired extensions of the files.
//     * @return File Array of the files found.
//     */
//    public static File[] getFilesFromFolder(String folderPath, String extension)
//    {
//        String[] extensions = new String[1];
//        extensions[0] = extension;
//
//        return FileOperations.getFilesFromFolder(folderPath, extensions, false);
//    }

    /**
     * Returns a list of all the files, of certain extensions, that are in a desired folder.
     * You can choose to also search the inner folders that are inside the given folder (recursive search).
     *
     * @param folderPath      Path of the desired folder
     * @param fileExtensions  Array of the desired extensions
     * @param recursiveSearch Whether to also search the inner folders inside the given folder
     * @return File Array of the files found.
     */
    public static File[] getFilesFromFolder(String folderPath,
                                            boolean recursiveSearch, String... fileExtensions)
    {
        File folder = new File(folderPath);
//        String[] extensions = new String[fileExtensions.length];
//        extensions = fileExtensions;

        //WildcardFileFilter wildcardFileFilter = new WildcardFileFilter("*." + extension.getExtension());
        Collection<File> filesCollection = FileUtils
                .listFiles(folder, FileOperations.getCleanExtensions(fileExtensions), recursiveSearch);
        File[] filesArray = new File[filesCollection.size()];

        return filesCollection.toArray(filesArray);
    }

    /**
     * Returns all the Subtitle files that are in a desired folder (non-recursive).
     *
     * @param folderPath Path of the desired folder.
     * @return File Array of the Subtitle files found withing the given folder, non-recursive.
     */
    public static File[] getSubtitleFilesFromFolder(String folderPath)
    {
        // This calls the private variation of this method, with the default list of supported subtitle formats:
        return FileOperations.getSubtitleFilesFromFolder(folderPath, false);
    }

    /**
     * Returns all the Subtitle files that are in a desired folder. Can also search the folders within (recursive search).
     *
     * @param folderPath      Path of the desired folder.
     * @param recursiveSearch Whether to also search the folders within the given folder.
     * @return File Array of the Subtitle files found withing the given folder (and also the folders within, if required).
     */
    public static File[] getSubtitleFilesFromFolder(String folderPath, boolean recursiveSearch)
    {
        return FileOperations
                .getSubtitleFilesFromFolder(folderPath, MediaOperations.getSupportedSubtitleFormats(), recursiveSearch);
    }

    /**
     * Returns all the Subtitle files of a given format that are in a desired folder.
     *
     * @param folderPath       Path of the desired folder.
     * @param singleSubsFormat Desired format of the subtitle files.
     * @return File Array of the Subtitle files found.
     */
    public static File[] getSubtitleFilesFromFolder(String folderPath, String singleSubsFormat)
    {
        String[] subsFormats = new String[1];
        subsFormats[0] = singleSubsFormat;

        return FileOperations.getSubtitleFilesFromFolder(folderPath, subsFormats, false);
    }

//    // I think this variation is not needed. ~~~~ Cuky
//    /**
//     * Returns all the Subtitle files of a given format that are in a desired folder.
//     * @param folderPath Path of the desired folder.
//     * @param singleSubsFormat Desired format of the subtitle files.
//     * @return File Array of the Subtitle files found.
//     */
//    public static File[] getSubtitleFilesFromFolder(String folderPath, String singleSubsFormat, boolean recursiveSearch)
//    {
//        String[] singleSubsFormatArray = new String[1];
//        singleSubsFormatArray[0] = singleSubsFormat;
//
//        // This calls the private variation of this method, with only the given (single) subtitle format:
//        return FileOperations.getSubtitleFilesFromFolder(folderPath, singleSubsFormatArray, recursiveSearch);
//    }

    /**
     * This method is PRIVATE by design! Do not turn into public. I don't want the ability to search only SOME of
     * the supported subtitle formats instead of all of them.
     * Returns all the Subtitle files of multiple given formats that are in a desired folder.
     *
     * @param folderPath  Path of the desired folder.
     * @param subsFormats Desired formats of the subtitle files.
     * @return File Array of the Subtitle files found.
     */
    private static File[] getSubtitleFilesFromFolder(String folderPath, String[] subsFormats, boolean recursiveSearch)
    {
        return FileOperations.getFilesFromFolder(folderPath, recursiveSearch, subsFormats);
    }

    /**
     * Returns all the Video files that are in a given folder.
     *
     * @param folderPath Path of the desired folder.
     * @return File Array of the Video files found.
     */
    public static File[] getVideoFilesFromFolder(String folderPath)
    {
        return FileOperations.getFilesFromFolder(folderPath, false, MediaOperations.getSupportedVideoExtensions());
    }

    /**
     * Searches a given folder for Video and Subtitle files, and tries to match pairs of corresponding ones.
     *
     * @param folderPath      Path of the desired folder.
     * @param recursiveSearch Whether to also search the inner folders inside the given folder.
     * @return ArrayList of Pairs of Files, containing matching video and subtitle files that were found.
     */
    public static ArrayList<FilePair> getMatchingVideoAndSubtitleFiles(String folderPath,
                                                                               boolean recursiveSearch)
    {
        File[] videoFilesInFolder = FileOperations.getVideoFilesFromFolder(folderPath);
        File[] subtitleFilesInFolder = FileOperations.getSubtitleFilesFromFolder(folderPath);

        return FileOperations.matchVideoAndSubtitleFiles(videoFilesInFolder, subtitleFilesInFolder);
    }

    //endregion

    /**
     * Scans two given arrays of files (video and subtitle) and tries to find matching pairs. Compares their filenames
     * in various ways. Path of files is not relevant to the operation.
     *
     * @param videoFiles    Array of video files.
     * @param subtitleFiles Array of subtitle files.
     * @return ArrayList of Pairs of files, containing matching video and subtitle files that were found.
     */
    public static ArrayList<FilePair> matchVideoAndSubtitleFiles(File[] videoFiles, File[] subtitleFiles)
    {
        // Sorting both arrays alphabetically:
        Arrays.sort(videoFiles);
        Arrays.sort(subtitleFiles);

        ArrayList<FilePair> allFoundPairs = new ArrayList<>();

        for (int currFileIndex = 0; currFileIndex < videoFiles.length; currFileIndex++)
        {
            // Comparing only the base name (without path or extension) of the video file and subtitle file in the current index.
            // This will match pairs like "Curb.Your.Enthusiasm.S01E04.Xvid.mp4" and "Curb.Your.Enthusiasm.S01E04.Xvid.srt".
            if (FilenameUtils.getBaseName(videoFiles[currFileIndex].getAbsolutePath()) ==
                    FilenameUtils.getBaseName(subtitleFiles[currFileIndex].getAbsolutePath()))
            {
                FileOperations.addFilesPair(videoFiles[currFileIndex], subtitleFiles[currFileIndex], allFoundPairs);

                continue;
            }

            int[] videoSeasonAndEpisode;
            int[] subsSeasonAndEpisode;

            // Trying to find SeasonNumber and EpisodeNumber ("SxxExx") in each of the file names, and comparing the numbers:
            videoSeasonAndEpisode = FileOperations
                    .parseSxxExxFromFilename(FilenameUtils.getBaseName(videoFiles[currFileIndex].getName()));
            subsSeasonAndEpisode = FileOperations
                    .parseSxxExxFromFilename(FilenameUtils.getBaseName(subtitleFiles[currFileIndex].getName()));

            if ((videoSeasonAndEpisode[0] != Caption.NO_SEASON)
                    && (videoSeasonAndEpisode[0] == subsSeasonAndEpisode[0])
                    && (videoSeasonAndEpisode[1] != Caption.NO_EPISODE)
                    && (videoSeasonAndEpisode[1] == subsSeasonAndEpisode[1]))
            {
                FileOperations.addFilesPair(videoFiles[currFileIndex], subtitleFiles[currFileIndex], allFoundPairs);
            }

            //TODO try more ways to match pairs
        }

        return allFoundPairs;
    }

    /**
     * Adds a pair of matched files into a given ArrayList.
     *
     * @param videoFile    Matching Video file.
     * @param subtitleFile Matching Subtitle file.
     * @param allPairs     ArrayList of pairs, into which to add the new pair.
     */
    private static void addFilesPair(File videoFile, File subtitleFile, ArrayList<FilePair> allPairs)
    {
        allPairs.add(new FilePair(videoFile, subtitleFile));
    }

    /**
     * Returns a File Filter object the supported file extensions.
     * @return A FileNameExtensionFilter object with all the extensions supported by LARRY.
     */
    public static javax.swing.filechooser.FileNameExtensionFilter getSupportedMediaExtensionsFileFilter()
    {
        FileNameExtensionFilter fileExtensionsFilter = new FileNameExtensionFilter("Supported media files", MediaOperations.getSupportedMediaExtensionsArray());

//        FileFilter fileFilter = new ExtensionFileFilter() {
//            @Override
//            public String[] getExtensions()
//            {
//                return MediaOperations.getSupportedMediaExtensionsArray();
//            }
//        };

        return fileExtensionsFilter;
    }


    /**
     * Checks if a specified file is supported by LARRY.
     *
     * @param checkedFile Desired file to check.
     * @return True if the file is supported, otherwise false.
     */
    public static boolean checkIfFileIsSupported(File checkedFile)
    {
        boolean isSupported;

        isSupported = MediaOperations.getSupportedMediaExtensions()
                .contains(FileOperations.getCleanExtension(checkedFile));

//        if (!isSupported)
//        {
//            isSupported = MediaOperations.getSupportedVideoExtensionsSet().contains(checkedFile);
//        }

        return isSupported;
    }

    /**
     * Finds the SeasonNumber and EpisodeNumber in a given FileName formatted with 'SxxExx' in it.
     *
     * @param fileName FileName (The extension is irrelevant).
     * @return <code>Array (type int, length 2)</code> with the found SeasonNumber and EpisodeNumber, or 'no season' and 'no episode' constants.
     */
    public static int[] parseSxxExxFromFilename(String fileName)
//            throws Messages.FileNotFormattedWithSxxExxException
    {
        int[] seasonAndEpisode = new int[]{Caption.NO_SEASON, Caption.NO_EPISODE};

        Pattern pattern = Pattern.compile(REGEX_SxxExx);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find())
        {
            String result = matcher.group();
            seasonAndEpisode[0] = Integer.parseInt(result.substring(1, 3));
            seasonAndEpisode[1] = Integer.parseInt(result.substring(4, 6));
        }
//        else
//        {
//            throw new Messages.FileNotFormattedWithSxxExxException(fileName);
//        }

        return seasonAndEpisode;
    }
}
