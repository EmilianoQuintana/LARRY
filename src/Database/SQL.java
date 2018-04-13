package Database;


public class SQL
{
    public static final class TBL
    {
        public static final String
                WORDS = "t_words",
                CAPTIONS = "t_captions",
                WORDS_TO_CAPTIONS = "t_words_to_captions",
                FILES_SEEN = "t_files_seen",
                MEDIA_NAMES = "t_media_names";
    }

    public static final class COL
    {
        public static final String
                ALL_COLUMNS = "*";

        public static final String
                WORD_ID = "word_id",
                WORD = "word";

        public static final String
                CAPTION_ID = "caption_id",
                SEASON_NUM = "season_num",
                EPISODE_NUM = "episode_num",
                START = "start",
                END = "end",
                CONTENT = "content";

        public static final String
                MEDIA_ID = "media_id",
                MEDIA_NAME = "media_name";

        public static final String
                FILE_ID = "file_id",
                FILE_NAME = "file_name";
    }

    public static final String
            CREATE_TABLE = " CREATE TABLE ",
            IF_NOT_EXISTS = " IF NOT EXISTS ",
            CREATE_TABLE_IF_NOT_EXISTS = SQL.CREATE_TABLE + SQL.IF_NOT_EXISTS,
            NOT_NULL = " NOT NULL ",
            PRIMARY_KEY = " PRIMARY KEY ",
            AUTO_INCREMENT = " AUTO_INCREMENT ",
            INT = " INT",
            VARCHAR = " VARCHAR",
            LENGTH_255 = "255",
            VARCHAR_255 = SQL.VARCHAR + "(" + SQL.LENGTH_255 + ")",
            INSERT_INTO = " INSERT INTO ",
            VALUES = " VALUES ",
            DELETE = " DELETE ",
            SELECT = " SELECT ",
            SELECT_ALL = " SELECT * ",
            FROM = " FROM ",
            WHERE = " WHERE ",
            INNER_JOIN = " INNER JOIN ",
            LEFT_OUTER_JOIN = " LEFT OUTER JOIN ",
            AND = " AND ",
            OR = " OR ",
            ON = " ON ",
            LIMIT = " LIMIT ",
            END_QUERY = ";";

    public static String escapeSingleQuotes(String string)
    {
        //TODO Use StringEscapeUtils from org.apache.commons.lang.????

        // Replacing ' with '' :
        return string.replace("'", "''");
    }

    public static <T> String sanitize(T objAsString)
    {
        return SQL.escapeSingleQuotes(objAsString.toString());
    }

    public static final class Query
    {
        public static String selectAllCaptionsForWord(int lowerCaseWord, int captionCountLimit)
        {
        /*
          SELECT t_captions.*
            FROM t_captions
            INNER JOIN t_word_to_captions ON t_words_to_captions.caption_id = t_captions.caption_id
            INNER JOIN t_words            ON t_words.word_id = t_words_to_captions.word_id
            WHERE t_words.word = {sanitize(lowerCaseWord)}
            LIMIT {sanitize(captionCountLimit)};
        */
            return (SQL.SELECT + SQL.TBL.CAPTIONS + "." + COL.ALL_COLUMNS +
                    SQL.FROM + SQL.TBL.CAPTIONS +
                    SQL.INNER_JOIN + SQL.TBL.WORDS_TO_CAPTIONS +
                    SQL.ON + SQL.TBL.WORDS_TO_CAPTIONS + "." + COL.CAPTION_ID + " = " + SQL.TBL.CAPTIONS + "." +
                    COL.CAPTION_ID +
                    SQL.INNER_JOIN + SQL.TBL.WORDS +
                    SQL.ON + SQL.TBL.WORDS + "." + COL.WORD_ID + " = " + SQL.TBL.WORDS_TO_CAPTIONS + "." +
                    SQL.COL.WORD_ID +
                    SQL.WHERE + SQL.TBL.WORDS + "." + COL.WORD + " = " + "'" + SQL.sanitize(lowerCaseWord) + "'" +
                    SQL.sanitize(captionCountLimit) +
                    SQL.END_QUERY);
        }

        public static String selectFileFromFilesSeen(String fileName)
        {
            /*
              SELECT *
                FROM t_files_seen
                WHERE file_name = '{sanitize(fileName)}'
             */
            return (SQL.SELECT_ALL +
                    SQL.FROM + SQL.TBL.FILES_SEEN +
                    SQL.WHERE + SQL.COL.FILE_NAME + " = '" + SQL.sanitize(fileName) + "'" +
                    SQL.END_QUERY);
        }

        public static String selectWordIDFromWords(String word)
        {
            /*
              SELECT word_id
                FROM t_words
                WHERE word = '{sanitize(word)}'
             */
            return (SQL.SELECT + SQL.COL.WORD_ID +
                    SQL.FROM + SQL.TBL.WORDS + " " +
                    SQL.WHERE + SQL.COL.WORD + " = '" + SQL.sanitize(word) + "'" +
                    SQL.END_QUERY);
        }

        public static String insertFileNameToFilesSeen(String fileName)
        {
            /*
              INSERT INTO t_files_seen (file_name)
                VALUES ('{sanitize(filename)}')
             */
            return (SQL.INSERT_INTO + SQL.TBL.FILES_SEEN + " (" + SQL.COL.FILE_NAME + ") " +
                    SQL.VALUES + " ('" + SQL.sanitize(fileName) + "') " +
                    SQL.END_QUERY);
        }

        public static String deleteFileNameFromFilesSeen(String fileName)
        {
            /*
              DELETE t_files_seen
                WHERE file_name = '{sanitize(fileName)}'
             */
            return (SQL.DELETE + SQL.TBL.FILES_SEEN +
                    SQL.WHERE + SQL.COL.FILE_NAME + " = '" + SQL.sanitize(fileName) + "'" +
                    SQL.END_QUERY);
        }

        public static String selectMediaIDFromMedias(int mediaID)
        {
            /*
              SELECT mediaID
                FROM t_media_names
                WHERE media_name = '{sanitize(mediaID)}'
             */
            return (SQL.SELECT + COL.MEDIA_ID +
                    SQL.FROM + TBL.MEDIA_NAMES +
                    SQL.WHERE + COL.MEDIA_NAME + " = '" + SQL.sanitize(mediaID) + "'" +
                    SQL.END_QUERY);
        }

        public static String initializeDB()
        {
            return (// Words
                    SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.WORDS + "(" +
                            SQL.COL.WORD + SQL.VARCHAR_255 + SQL.NOT_NULL + SQL.PRIMARY_KEY + ", " +
                            SQL.COL.WORD_ID + SQL.INT + SQL.NOT_NULL + SQL.AUTO_INCREMENT + ") " +
                            SQL.END_QUERY +

                            // Captions
                            SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.CAPTIONS + "(" +
                            SQL.COL.CAPTION_ID + SQL.INT + SQL.NOT_NULL + SQL.AUTO_INCREMENT + ", " +
                            SQL.COL.SEASON_NUM + SQL.INT + SQL.NOT_NULL + ", " +
                            SQL.COL.EPISODE_NUM + SQL.INT + SQL.NOT_NULL + ", " +
                            SQL.COL.START + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                            SQL.COL.END + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                            SQL.COL.CONTENT + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                            SQL.COL.MEDIA_ID + SQL.VARCHAR_255 + SQL.NOT_NULL + " ) " +
                            SQL.END_QUERY +

                            // Words to Captions
                            SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.WORDS_TO_CAPTIONS + "(" +
                            SQL.COL.WORD_ID + SQL.INT + SQL.NOT_NULL + ", " +
                            SQL.COL.CAPTION_ID + SQL.INT + SQL.NOT_NULL + ", " +
                            SQL.PRIMARY_KEY + " (" + SQL.COL.WORD_ID + ", " + SQL.COL.CAPTION_ID + ")) " +
                            SQL.END_QUERY +

                            // Files seen
                            SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.FILES_SEEN + "(" +
                            SQL.COL.FILE_NAME + SQL.VARCHAR_255 + SQL.NOT_NULL + SQL.PRIMARY_KEY + ", " +
                            SQL.COL.FILE_ID + SQL.INT + SQL.AUTO_INCREMENT + ") " +
                            SQL.END_QUERY +

                            // Names of series/movies
                            SQL.CREATE_TABLE_IF_NOT_EXISTS + SQL.TBL.MEDIA_NAMES + "(" +
                            SQL.COL.MEDIA_NAME + SQL.VARCHAR_255 + SQL.NOT_NULL + ", " +
                            SQL.COL.MEDIA_ID + SQL.INT + SQL.NOT_NULL + ")" +
                            SQL.END_QUERY);
        }
    }

}
