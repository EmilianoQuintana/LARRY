package Database;

import subsParser.Const;
import uk.co.caprica.vlcj.filter.SubTitleFileFilter;
import uk.co.caprica.vlcj.filter.VideoFileFilter;

import java.util.HashSet;
import java.util.Set;

public class MediaOperations
{
    public static String[] getSupportedVideoExtensions()
    {
        return new VideoFileFilter().getExtensions();
    }

    public static Set<String> getSupportedVideoExtensionsSet()
    {
        return new VideoFileFilter().getExtensionSet();
    }

    /**
     * Returns all the Subtitle Formats supported by LARRY.
     *
     * @return String Array of the Subtitle Formats supported both by the subtitle parser API and the media playback API.
     */
    public static String[] getSupportedSubtitleFormats()
    {
        // Getting the supported subtitle extensions by SubsParser and by VLCJ:
        HashSet<String> supportedBySubsParser = Const.getSupportedSubtitlesFormatsSet();
        Set<String> supportedByVLCJ = new SubTitleFileFilter().getExtensionSet();

        String[] supportedByBoth;
        int indexToAdd = 0;

        // Taking only the extensions supported by both SubsParser and VLCJ.

        if (supportedBySubsParser.size() < supportedByVLCJ.size())
        {
            // Building the list of supported Subtitle Files according to SubsParser:
            supportedByBoth = new String[supportedBySubsParser.size()];

            for (String strFormat : supportedBySubsParser)
            {
                if (supportedByVLCJ.contains(strFormat))
                {
                    supportedByBoth[indexToAdd] = strFormat;

                    indexToAdd++;
                }
            }
        }
        else
        {
            // Building the list of supported Subtitle Files according to VLCJ:
            supportedByBoth = new String[supportedByVLCJ.size()];

            for (String strFormat : supportedByVLCJ)
            {
                if (supportedBySubsParser.contains(strFormat))
                {
                    supportedByBoth[indexToAdd] = strFormat;

                    indexToAdd++;
                }
            }
        }

        return supportedByBoth;
    }
}
