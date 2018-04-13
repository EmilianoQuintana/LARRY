package Database;

import LARRY.Messages;
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
    public class FileExtension
    {
        private File file;
        private String extension;

        public FileExtension(File file)
        {
            this.setFile(file);
        }

        public FileExtension(String extension)
        {
            this.setExtension(extension);
        }

        public void setFile(File file)
        {
            this.file = file;
            this.setExtension(FilenameUtils.getExtension(this.getFile().getName()));
        }

        public File getFile()
        {
            return this.file;
        }

        public boolean hasFile()
        {
            boolean hasFile = false;

            if (this.getFile() != null)
            {
                hasFile = true;
            }

            return hasFile;
        }

        public void setExtension(String extension)
        {
            this.extension = extension.substring(extension.lastIndexOf(".") + 1, extension.length()).toUpperCase();
        }

        /**
         * @return This FileExtension's extension, in UPPER CASE.
         */
        public String getExtension()
        {
            return this.extension;
        }
    }

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

                    TimedTextFileFormat timedTextFileFormat;
                    String extension = FileOperations.getFileExtension(name).toUpperCase();

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

    /**
     * Returns a list of all the files within a given folder.
     *
     * @param folderPath Path of the desired folder
     * @return Array of files in the given folder.
     */
    public static File[] getListOfFilesInFolder(String folderPath)
    {
        return new File(folderPath).listFiles();
    }

    /**
     * Returns a list of all the files, of a certain extension, that are in a desired folder.
     *
     * @param folderPath Path of the desired folder
     * @param extension  Desired extension of the files
     * @return File Array of the files found.
     */
    public static File[] getListOfFilesInFolder(String folderPath, FileOperations.FileExtension extension)
    {
        FileOperations.FileExtension[] extensions = new FileOperations.FileExtension[1];
        extensions[0] = extension;

        return FileOperations.getListOfFilesInFolder(folderPath, extensions, false);
    }

    /**
     * Returns a list of all the files, of certain extensions, that are in a desired folder.
     * You can choose to also search the inner folders that are inside the given folder (recursive search).
     *
     * @param folderPath      Path of the desired folder
     * @param extensions      Array of the desired extensions
     * @param recursiveSearch Whether to also search the inner folders inside the given folder
     * @return File Array of the files found.
     */
    public static File[] getListOfFilesInFolder(String folderPath, FileOperations.FileExtension[] extensions,
                                                boolean recursiveSearch)
    {
        File folder = new File(folderPath);
        String[] strExtensions = new String[extensions.length];

        // Copying the FileExtensions to a String array:
        for (int iCurrStrExt = 0; iCurrStrExt <= extensions.length; iCurrStrExt++)
        {
            strExtensions[iCurrStrExt] = extensions[iCurrStrExt].getExtension();
        }

        //WildcardFileFilter wildcardFileFilter = new WildcardFileFilter("*." + extension.getExtension());
        Collection<File> filesCollection = FileUtils.listFiles(folder, strExtensions, recursiveSearch);
        File[] filesArray = new File[filesCollection.size()];

        return filesCollection.toArray(filesArray);

//        javax.swing.filechooser.FileFilter swingFileFilter = new javax.swing.filechooser.FileNameExtensionFilter(
//                "given extension", extension.getExtension());
//        java.io.FileFilter ioFileFilter = file -> swingFileFilter.accept(file);
//
//        return new File(folderPath).listFiles(ioFileFilter);
    }

    public static File[] getListOfSubtitleFilesInFolder(String folderPath)
    {
        //FileOperations.FileExtension[] extensions = SubsCollection.getSupportedSubtitlesFormats().toArray();
        //FileOperations.getListOfFilesInFolder(folderPath, extensions)

        return null;
    }

    public static boolean checkIfFileIsSupported(File checkedFile)
    {
        return SubsCollection.getSupportedSubtitlesFormats()
                .contains(FileOperations.getFileExtension(checkedFile.getName()));

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

//        try
//        {
//            return fileName.substring(fileName.lastIndexOf(".") + 1);
//        } catch (Exception e)
//        {
//            return "";
//        }
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
