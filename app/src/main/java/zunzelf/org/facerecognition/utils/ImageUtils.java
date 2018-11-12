package zunzelf.org.facerecognition.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageUtils {
    public Bitmap  skinFilter(Bitmap bm){
        Bitmap res = bm.copy(bm.getConfig(), true);
        int h = bm.getHeight();
        int w = bm.getWidth();
        int[] pixels = new int[w*h];
        res.getPixels(pixels, 0, w, 0, 0, w, h);
        //
        for (int y = 0; y < h; y++){
            for (int x = 0; x < w; x++)
            {
                int index = y * w + x;
                int R = (pixels[index] >> 16) & 0xff;     //bitwise shifting
                int G = (pixels[index] >> 8) & 0xff;
                int B = pixels[index] & 0xff;
                double c = 0.5*R - 0.419*G - 0.081*B;
                if (c >= 10 && c <= 45){
                    pixels[index] = Color.BLACK;
                }else {
                    pixels[index] = Color.WHITE;
                }
            }}
        res.setPixels(pixels, 0, w, 0, 0, w, h);
        return res;
    }
}
