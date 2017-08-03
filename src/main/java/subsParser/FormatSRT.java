package subsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import subsParser.SubsCollection;

/**
 * This class represents the .SRT subtitle format
 * <br><br>
 * Copyright (c) 2012 J. David Requejo <br>
 * j[dot]david[dot]requejo[at] Gmail
 * <br><br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * <br><br>
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * <br><br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 * @author J. David Requejo
 *
 */
public class FormatSRT implements TimedTextFileFormat
{	
	public TimedTextObject parseFile(String fileName, 
									 InputStream is, 
									 int nSeasonNum, 
									 int nEpisodeNum) throws IOException
	{
		/** 
		* Example for a subtitle file: "Curb Your Enthusiasm - 1x01 - The Pants Tent.DVDRip.en.srt"
		* Example for captions in the subtitle file:
		* 
		* 204
		* 00:11:56,716 --> 00:11:59,879
		* - Gay Jew in Nazi Germany?
		* - Yeah.
		* 
		* 205
		* 00:12:00,186 --> 00:12:02,086
		* - He must have had a hard time.
		* - Yep.
		* 
	 	*/
		TimedTextObject tto = new TimedTextObject();
		Caption caption = new Caption();
		int captionNumber = 1;
		boolean allGood;
		int lineCounter = 0;
		String line;

		// First, loading the file:
		InputStreamReader in = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(in);

		// Saving the file name:
		tto.fileName = fileName;
		 
		line = br.readLine();
		
		// Removing Byte-Order-Mark (BOM) character:
		line = line.replace(Const.STR_BOM_CHAR, "");
		
		try
		{
			while(line != null)
			{
				line = line.trim();
				lineCounter++;

				// Ignoring blank lines:
				if (!line.isEmpty())
				{
					allGood = false;
					// The first line should be an increasing number:
					try 
					{
						int num = Integer.parseInt(line);
						if (num != captionNumber)
						{
							throw new Exception();
						}
						else
						{
							captionNumber++;
							allGood = true;
						}
					}
					catch (Exception e) 
					{
						tto.warnings += captionNumber + Const.MSG_EXPECTED_AT    // " expected at line "	
								     + lineCounter;
						tto.warnings += Const.MSG_SKIPPING_TO_NEXT_LINE;          // "\n skipping to next line\n\n"
					}
					
					if (allGood)
					{
						// The second line should contain the beginning time and ending time of the caption:
						try
						{	
							// Putting the caption's values in the 'caption' object before adding it to the returned collection: 
							lineCounter++;
							line = br.readLine().trim();							
							String start = line.substring(0, 12);
							String end   = line.substring(line.length()-12, line.length());							
							caption.start = new Time(Const.TIME_FORMAT_SRT, start);
							caption.end   = new Time(Const.TIME_FORMAT_SRT, end);
							caption.seasonNum  = nSeasonNum;
							caption.episodeNum = nEpisodeNum;
						}
						catch (Exception e){
							tto.warnings += Const.MSG_INCORRECT_TIME_FORMAT    // "incorrect time format at line " 
										 + lineCounter;
							allGood = false;
						}
					}
					if (allGood){
						// The third (and maybe fourth and fifth) line should contain the caption's text. 
						// Concatenating all the text into the 'text' object:
						lineCounter++;
						line = br.readLine().trim();
						String text = "";
						
						while (!line.isEmpty())
						{
							text += line + "<br />";
							line = br.readLine().trim();
							lineCounter++;
						}
						
						caption.rawContent = text;
						caption.cleanContentForSRT();
						
						// In case the key is already there, we increase it by a millisecond, since no duplicates are allowed:
						int key = caption.start.mseconds;
						
						while (tto.captions.containsKey(key))
						{ 
							key++;
						}
						
						if (key != caption.start.mseconds)
						{
							tto.warnings += Const.MSG_SAME_START_TIME;    // "caption with same start time found...\n\n"
						}
						
						// Adding the caption to the TimedTextObject:
						tto.captions.put(key, caption);
					}
					
					// Going on to the next blank line:
					while (!line.isEmpty())
					{
						line = br.readLine().trim();
						lineCounter++;
					}
					
					caption = new Caption();
				}
				
				line = br.readLine();
			}

		}
		catch (NullPointerException e)
		{
			tto.warnings += Const.MSG_UNEXP_END_OF_FILE;    //  "unexpected end of file, maybe last caption is not complete.\n\n"
		}
		
		finally
		{
			// Closing the reader:
			is.close();
	    }
		
		tto.built = true;
		return tto;
	}

	public String[] toFile(TimedTextObject tto) {
		
		//first we check if the TimedTextObject had been built, otherwise...
		if(!tto.built)
			return null;

		//we will write the lines in an ArrayList,
		int index = 0;
		//the minimum size of the file is 4*number of captions, so we'll take some extra space.
		ArrayList<String> file = new ArrayList<>(5 * tto.captions.size());
		//we iterate over our captions collection, they are ordered since they come from a TreeMap
		Collection<Caption> c = tto.captions.values();
		Iterator<Caption> itr = c.iterator();
		int captionNumber = 1;

		while(itr.hasNext()){
			//new caption
			Caption current = itr.next();
			//number is written
			file.add(index++, Integer.toString(captionNumber++));
			//we check for offset value:
			if(tto.offset != 0){
				current.start.mseconds += tto.offset;
				current.end.mseconds += tto.offset;
			}
			//time is written
			file.add(index++, current.start.getTime(Const.TIME_FORMAT_SRT) 
							  + Const.DLM_SRT_ARROW    // " --> " 
							  + current.end.getTime(Const.TIME_FORMAT_SRT));
			//offset is undone
			if(tto.offset != 0){
				current.start.mseconds -= tto.offset;
				current.end.mseconds -= tto.offset;
			}
			//text is added
			String[] lines = cleanTextForSRT(current);
			int i=0;
			while(i<lines.length)
				file.add(index++,""+lines[i++]);
			//we add the next blank line
			file.add(index++,"");
		}

		String[] toReturn = new String [file.size()];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = file.get(i);
		}
		return toReturn;
	}


	/* PRIVATE METHODS */

	/**
	 * This method cleans caption.content of XML and parses line breaks.
	 * 
	 */
	private String[] cleanTextForSRT(Caption current) {
		String[] lines;
		String text = current.rawContent;
		//add line breaks
		lines = text.split("<br />");
		//clean XML
		for (int i = 0; i < lines.length; i++){
			//this will destroy all remaining XML tags
			lines[i] = lines[i].replaceAll("\\<.*?\\>", "");
		}
		return lines;
	}
}