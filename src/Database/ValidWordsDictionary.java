package Database;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ValidWordsDictionary
{
    private static final int BUFFER_SIZE = 100;
    private static final String FILE_ADDRESS = "./text_files/valid_words.txt";
    private Set<String> valid_words;
    private List<String> buffer;
    
    private static ValidWordsDictionary _instance;
    
    public static ValidWordsDictionary instance()
    {
        if (_instance == null)
        {
            _instance = new ValidWordsDictionary();
            _instance.initialize();
        }
        
        return _instance;
    }
    
    private void initialize()
    {
        this.valid_words = new HashSet<>();
        
        //Don't ask about this next line. Not my fault
        try (BufferedReader buffer = new BufferedReader(new FileReader(this.getFile())))
        {
            
            String line = buffer.readLine();
            
            while (line != null)
            
            {
                this.valid_words.add(line.trim()); //Adds entire line, does not trim any inner spaces from multi-words!
                
                
                //next iteration
                line = buffer.readLine();
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //TODO possibly read all words from the database's WORDS table

        this.buffer = new LinkedList<>();
    }
    
    private File getFile()
    {
        URL url = ValidWordsDictionary.class.getClassLoader().getResource(FILE_ADDRESS);
        try
        {
            if (url == null)
                throw new FileNotFoundException("File was not found at: " + FILE_ADDRESS);
            
            //HORRIBLE - but working - solution to a problem I had. Now the txt file will update the 'true' file,
            // not a copy created by maven.
            return new File(url.toURI().getPath().replace("target/classes/", "resources/"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new File("fuck this");
    }
    
    public boolean contains(String word)
    {
        return this.valid_words.contains(word);
    }
    
    /**
     * Won't check if word isn't already there!
     */
    public void addWord(String word)
    {
        //Will not add to buffer if word is already there
        if (this.valid_words.add(word))
        {
            this.buffer.add(word);
            if (this.buffer.size() > BUFFER_SIZE)
            {
                this.flushBuffer();
            }
        }
    }

    private void flushBuffer()
    {
        //Create big string to write
        StringBuilder sb = new StringBuilder();
        for (String word : this.buffer)
        {
            sb.append(word);
            sb.append("\n");
        }
        //If you edit this code, keep in mind that I assumed the file ends on a line break
        //Write to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.getFile(), true)))
        {
            writer.write(sb.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.buffer.clear();
    }
    
    private static void test()
    {
        ValidWordsDictionary dict = ValidWordsDictionary.instance();
        
        System.out.println(dict.contains("hello"));
        System.out.println(dict.contains("mozart"));
        System.out.println(dict.contains("zoophilia"));
        System.out.println(dict.contains("ThisWordIsATestWord"));
        dict.addWord("ThisWordIsATestWord");
        System.out.println(dict.contains("ThisWordIsATestWord"));
        dict.flushBuffer();
    }
}
