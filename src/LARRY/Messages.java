package LARRY;

import Database.SubsCollection;
import minlog_master.src.com.esotericsoftware.minlog.*;

public class Messages {
    // Error messages
    // TODO find usages and replace with Exception
    public static final String MSG_SAME_START_TIME = "caption with same start time found...\n\n";
    public static final String MSG_INCORRECT_TIME_FORMAT = "incorrect time format at line ";
    public static final String MSG_EXPECTED_AT = " expected at line ";
    public static final String MSG_SKIPPING_TO_NEXT_LINE = "\n skipping to next line\n\n";
    public static final String MSG_UNEXP_END_OF_FILE = "unexpected end of file, maybe last caption is not complete.\n\n";

    private static final String
            MSG_WORD_NOT_FOUND = "Couldn't find '%s' in the database.",
            MSG_EMPTY_FOLDER = "No files were found in folder: %s",
            MSG_GHOST_FOLDER = "Unknown folder: %s",
            MSG_DOES_NOT_START_PREFIX = "File does not start with Prefix %s",
            MSG_FILE_ALREADY_IN_LIBRARY = "File \"%s\" is already in library. ",
            MSG_MAXIMUM_FILES_ADDED = "Cannot add any more files; maximum amount is %d)", // Returning to calling function.",
            MSG_SUPPORTED_SUBTITLE_FORMATS = "Supported subtitle formats: %s",
            MSG_SUPPORTED_VIDEO_FORMATS = "Supported video formats: %s",

    MSG_SxxExx_NOT_FOUND = "Could not identify SeasonNumber and EpisodeNumber in file %s",
            MSG_SEASON_NUM_TOO_BIG = "The given Season Number %d is too big";

    public static final String
            MSG_ADDING_FILE = "Adding…",
            MSG_ADDED_FILE = "…Added!";

    public static void printInConsole(String message) {
        System.out.println(message);
    }

    public static void log(String message) {
         Log.error(message);
    }

    public static class GhostFolderException extends Exception {
        private String folderPath;

        /*
        public GhostFolderException()
        {
            this.folderPath = Messages.MSG_UNKNOWN_FOLDER;
        }
        */

        public GhostFolderException(String folderPath) {
            this.folderPath = folderPath;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_GHOST_FOLDER, this.folderPath));
        }
    }

    public static class EmptyFolderException extends Exception {
        private String folderPath;

        /*
        public EmptyFolderException()
        {
            this.folderPath = Messages.MSG_GHOST_FOLDER;
        }
        */

        public EmptyFolderException(String folderPath) {
            this.folderPath = folderPath;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_EMPTY_FOLDER, this.folderPath));
        }
    }

    public static class WordNotFoundException extends Exception {
        private String word;

        public WordNotFoundException(String word) {
            this.word = word;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_WORD_NOT_FOUND, this.word));
        }
    }

    public static class FileDoesNotStartWithPrefixException extends Exception {
        private String prefix;

        public FileDoesNotStartWithPrefixException(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_DOES_NOT_START_PREFIX, this.prefix));
        }
    }

    public static class FileAlreadyInLibraryException extends Exception {
        private String fileName;

        public FileAlreadyInLibraryException(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_FILE_ALREADY_IN_LIBRARY, this.fileName));
        }
    }

    public static class FileNotFormattedWithSxxExxException extends Exception {
        private String fileName;

        public FileNotFormattedWithSxxExxException(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_SxxExx_NOT_FOUND, this.fileName));
        }
    }

    public static class MaximumAmountOfFilesAddedException extends Exception {
        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_MAXIMUM_FILES_ADDED, SubsCollection.MAX_FILES_TO_ADD));
        }
    }

    public static class SupportedSubtitleFormats extends Exception {
        @Override
        public String getMessage() {
            return super.getMessage();
        }
    }

    public static class SeasonNumberTooBigException extends Exception {
        private int seasonNum;

        public SeasonNumberTooBigException(int seasonNum) {
            this.seasonNum = seasonNum;
        }

        @Override
        public String getMessage() {
            return (String.format(Messages.MSG_SEASON_NUM_TOO_BIG, this.seasonNum));
        }
    }

}
