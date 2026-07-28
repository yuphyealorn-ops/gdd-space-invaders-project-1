package gdd;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

// Fire-and-forget one-shot sound effects (WAV). Each call opens its own Clip
// so effects can overlap the looping music without cutting it off.
public final class SoundFx {

    private static boolean muted = false;

    private SoundFx() {
    }

    public static void setMuted(boolean value) {
        muted = value;
    }

    public static void play(String path) {
        if (muted) {
            return;
        }
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            // Missing or unsupported effect should never crash gameplay.
        }
    }
}
