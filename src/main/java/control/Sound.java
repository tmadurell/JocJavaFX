package control;

import javafx.scene.media.AudioClip;


/**
 * Manager que fa reproduir els nostres efectes de so que realitzar el joc JavaFX
 */
public class Sound {
    private AudioClip soundEffect;

    public Sound(String filePath) {
        soundEffect = new AudioClip(getClass().getResource(filePath).toExternalForm());
    }

    public void playClip() {
        soundEffect.play();
    }
}
