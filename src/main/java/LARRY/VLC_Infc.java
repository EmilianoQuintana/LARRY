package LARRY;

import java.io.File;
import java.io.FileInputStream;

import javax.swing.JFrame;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import subsParser.Caption;
import subsParser.SubsCollection;
import subsParser.TimedTextObject;
import subsParser.FormatSRT;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

public class VLC_Infc
{
    private final EmbeddedMediaPlayerComponent mediaPlayerComp;

    public static void main(String[] args) throws Exception
    {
        

        /*
         * VLC_Infc vlcinf = new VLC_Infc();
         * 
         * 
         * System.out.println("  version: {}" +
         * LibVlc.INSTANCE.libvlc_get_version());
         * System.out.println(" compiler: {}" +
         * LibVlc.INSTANCE.libvlc_get_compiler());
         * System.out.println("changeset: {}" +
         * LibVlc.INSTANCE.libvlc_get_changeset());
         */
    }

    private VLC_Infc()
    {
        addSearchPath();

        mediaPlayerComp = new EmbeddedMediaPlayerComponent();

        playFileDummy();
    }

    private void playFileDummy()
    {
        String strTempVidFile = "P:\\Curb Your Enthusiasm - Seasons 1-6 + Extras\\Curb Your Enthusiasm - Season 1\\Curb Your Enthusiasm - S01E01 - The Pants Tent.avi";

        JFrame frame = new JFrame("Larry Dummy Try");

        frame.setContentPane(mediaPlayerComp);

        frame.setLocation(100, 100);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        mediaPlayerComp.getMediaPlayer().playMedia(strTempVidFile);

    }

    private void addSearchPath()
    {
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VLC");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        LibXUtil.initialise();
    }
}
