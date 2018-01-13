package Database;

public class SQL
{
    public static final String  CREATE_TABLE               = " CREATE TABLE ",
                                IF_NOT_EXISTS              = " IF NOT EXISTS ",
                                CREATE_TABLE_IF_NOT_EXISTS = SQL.CREATE_TABLE + SQL.IF_NOT_EXISTS,
                                NOT_NULL                   = " NOT NULL ",
                                PRIMARY_KEY                = " PRIMARY KEY ",
                                AUTO_INCREMENT             = " AUTO INCREMENT ",
                                TBL_WORDS                  = " t_words ",
                                TBL_CAPTIONS               = " t_captions ",
                                TBL_WORDS_TO_CAPTIONS      = " t_captions ",
                                TBL_FILES_SEEN             = " t_files_seen ",
                                TBL_MEDIA_NAMES            = " t_media_names ";

}
