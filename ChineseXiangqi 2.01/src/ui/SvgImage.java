package ui;


import javafx.embed.swing.SwingFXUtils;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javafx.scene.image.Image;
import java.io.IOException;
import java.io.InputStream;

public class SvgImage {

    public static Image load(String path) {
        return load(path, -1, -1);
    }
    
    public static Image load(String path, float width, float height) {
        if (path == null || path.isEmpty())
            return null;
        
        String resourcePath = path.startsWith("@") ? path.substring(1) : path;
        
        try (InputStream is = SvgImage.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("SvgImage Error: Could not find resource: " + resourcePath);
                return null;
            }
            
            return decode(is, width, height);
        } catch (IOException e) {
            e.printStackTrace();;
            return null;
        }
    }
    
    public static Image load(InputStream inputStream) {
        return decode(inputStream, -1, -1);
    }

    private static Image decode(InputStream stream, float width, float height) {
        try {
            BufferedImageTranscoder transcoder = new BufferedImageTranscoder();

            if (width > 0)
                transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, width);
            if (height > 0)
                transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, height);

            TranscoderInput input = new TranscoderInput(stream);
            transcoder.transcode(input, null);


            java.awt.image.BufferedImage awtImage = transcoder.getBufferImage();
            if (awtImage != null)
                return SwingFXUtils.toFXImage(awtImage, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
