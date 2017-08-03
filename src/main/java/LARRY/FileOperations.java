package LARRY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import subtitleFile.Caption;
import subtitleFile.FormatSRT;
import subtitleFile.TimedTextObject;

public class FileOperations
{
	public static final String ONLY_TXT = ".txt";
	public static final String ONLY_SRT = ".srt";
	public static final String ONLY_SUB = ".sub";
	public static final String ONLY_ALL = ".*";
	
	private static FilenameFilter getFilenameFilterByExt(final String istrExt)
	{
		return new FilenameFilter()
		{	
			public boolean accept(File istrPath, String strFileName)
			{
				return strFileName.toLowerCase().endsWith(istrExt);
	
			}
		};
	}
	
    public static Caption[] readFilesInFolderByExt(String i_strFolderPath, String i_strExt)
    {
    	File workingFolder = new File(i_strFolderPath);
    	File[] filesInFolder = workingFolder.listFiles(FileOperations.getFilenameFilterByExt(i_strExt));
    	FormatSRT formatSrt;
    	TimedTextObject tto;
    	InputStream fileInputStream;
		
    	/*
    	FileReader     fileReader = null;
    	BufferedReader buffReader = null;
    	TimedTextFileFormat ttff;
		OutputStream output = null;
		*/
    	
    	for (File currFile : filesInFolder)
    	{
    		if (currFile.isFile())
    		{
    			try
    			{
    				fileInputStream = new FileInputStream(currFile);
    				formatSrt = new FormatSRT();
    				tto = formatSrt.parseFile(currFile.getName(), fileInputStream);    				
    				
    				/*
    				fileReader = new FileReader(currFile.getAbsolutePath());
    				buffReader = new BufferedReader(fileReader);
    				
    				String strCurrLine = buffReader.readLine();
    				
    				while (!strCurrLine.isEmpty())
    				{
    					/////////////////////////////////////////////
    				}
    				
    				*/
    				
    			}
    			catch (IOException ex)
    			{
    				
    			}
    		}
    	}
    	
    	return null;
    }
    
 
}
