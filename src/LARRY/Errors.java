package LARRY;

public class Errors
{
    public static final String MSG_NONEXISTENT_WORD = "This word does not exist in the database: {0}";

    public static class GhostFolderException extends Exception
    {
        private String folderPath;

        public GhostFolderException()
        {
            this.folderPath = "(unknown folder, fix your code)";
        }

        public GhostFolderException(String folderPath)
        {
            this.folderPath = folderPath;
        }

        @Override
        public String getMessage()
        {
            return "ERROR: Folder does not contain any files: " + this.folderPath;
        }
    }
}
