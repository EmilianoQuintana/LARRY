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

    @SuppressWarnings("WeakerAccess")
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
            WHERE = " WHERE ";
}
