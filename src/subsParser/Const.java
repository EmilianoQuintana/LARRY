package subsParser;

public class Const
{	
	// General:
	public static final String STR_BOM_CHAR        = "\uFEFF";
	
	// Time formats:
	public static final String TIME_FORMAT_SRT = "hh:mm:ss,ms";
	public static final String TIME_FORMAT_SSA = "h:mm:ss.cs"; 
	public static final String TIME_FORMAT_STL = "h:m:s:f/fps";
	public static final String TIME_FORMAT_SCC = "hh:mm:ss:ff/";
	
	// Delimiters:
	public static final String DLM_SRT_ARROW   = " --> ";
	
	// Error messages
	public static final String MSG_SAME_START_TIME       = "caption with same start time found...\n\n";
	public static final String MSG_INCORRECT_TIME_FORMAT = "incorrect time format at line ";
	public static final String MSG_EXPECTED_AT           = " expected at line ";
	public static final String MSG_SKIPPING_TO_NEXT_LINE = "\n skipping to next line\n\n";
	public static final String MSG_UNEXP_END_OF_FILE     = "unexpected end of file, maybe last caption is not complete.\n\n";

}