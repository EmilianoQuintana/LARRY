package subsParser;

import java.util.HashSet;

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

	// Subtitles File Formats:
	public static final String SUBS_FORMAT_ASS = "ass",
			SUBS_FORMAT_SCC = "scc",
			SUBS_FORMAT_SRT = "srt",
			SUBS_FORMAT_STL = "stl",
			SUBS_FORMAT_TTML = "ttml";

	//region Supported Subtitle Formats

	public static String[] supportedSubtitlesFormatsStr =
			{
					SUBS_FORMAT_ASS,
					SUBS_FORMAT_SCC,
					SUBS_FORMAT_SRT,
					SUBS_FORMAT_STL,
					SUBS_FORMAT_TTML,
			};

	public static HashSet<String> supportedSubtitlesFormatsSet;

	/**
	 * Returns a HashSet of all the supported Subtitle File Formats.
	 */
	public static HashSet<String> getSupportedSubtitlesFormatsSet()
	{
		// The HashSet of supported Subtitle Formats is a static singleton property of class SubsCollection:
		if (supportedSubtitlesFormatsSet.isEmpty())
		{
			supportedSubtitlesFormatsSet = new HashSet<>();

			for (String strFormat : getSupportedSubtitlesFormats())
			{
				supportedSubtitlesFormatsSet.add(strFormat);
			}
		}

		return supportedSubtitlesFormatsSet;
	}

	/**
	 * Returns a String Array of all the supported Subtitle File Formats.
	 */
	public static String[] getSupportedSubtitlesFormats()
	{
		return supportedSubtitlesFormatsStr;
	}

	//endregion

}