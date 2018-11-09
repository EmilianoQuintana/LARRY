package LARRY;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Resources {
    public enum Icon{
        FAST_FORWARD("ic_fast_forward_white_48dp.png"),
        FAST_REWIND("ic_fast_rewind_white_48dp.png"),
        LOOP("ic_loop_white_48dp.png"),
        PAUSE("ic_pause_white_48dp.png"),
        PLAY("ic_play_arrow_white_48dp.png"),
        PLAYLIST_ADD("ic_playlist_add_white_48dp.png"),
        PLAYLIST_PLAY("ic_playlist_play_white_48dp.png"),
        REPEAT("ic_repeat_white_48dp.png"),
        REPLAY_10("ic_replay_10_white_48dp.png"),
        REPLAY_30("ic_replay_30_white_48dp.png"),
        REPLAY_5("ic_replay_5_white_48dp.png"),
        REPLAY("ic_replay_white_48dp.png");

        String filename;

        private Icon(String fileName) {
            this.filename = fileName;
        }

        public StretchIcon getIcon() {
            String iconFilePathRelative = "/resources/icons/" + this.filename;

            try {
                InputStream url = Resources.class.getResourceAsStream(iconFilePathRelative);
                if (url == null) {
                    throw new FileNotFoundException("Icon file not found: " + iconFilePathRelative);
                }

                BufferedImage buffImg = ImageIO.read(url);
                return new StretchIcon(buffImg);

            } catch (IOException ex){
                ex.printStackTrace();
                return null;
            }
        }
    }
}
