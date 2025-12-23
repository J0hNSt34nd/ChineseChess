package Effect;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class JavaFXSound {
    static MediaPlayer mediaPlayer;

    public static void playEffect() {
        String uri = new File("res/soundeffect/chess-move.mp3").toURI().toString();

        AudioClip clip = new AudioClip(uri);
        clip.play(); // 播放一次
    }

    public static void check() {
        String uri = new File("res/check.mp3").toURI().toString();

        AudioClip clip = new AudioClip(uri);
        clip.play();
    }

    public void playLoopMusic() {
        try {
            String uri = new File("res/soundTrack/background1.mp3").toURI().toString();
            Media media = new Media(uri);

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);


            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
