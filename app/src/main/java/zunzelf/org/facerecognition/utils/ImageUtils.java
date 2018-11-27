package zunzelf.org.facerecognition.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageUtils {

    public Bitmap skinFilter(Bitmap bm){
        return skinFilter(bm, false);
    }

    public Bitmap skinFilter(Bitmap bm, boolean inverse){
        Bitmap res = bm.copy(bm.getConfig(), true);
        int h = bm.getHeight();
        int w = bm.getWidth();
        int[] pixels = new int[w*h];
        res.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int y = 0; y < h; y++){
            for (int x = 0; x < w; x++)
            {
                int index = y * w + x;
                int R = (pixels[index] >> 16) & 0xff;     //bitwise shifting
                int G = (pixels[index] >> 8) & 0xff;
                int B = pixels[index] & 0xff;
                double c = 0.5*R - 0.419*G - 0.081*B;
                if (c >= 10 && c <= 45){
                    if(inverse)
                        pixels[index] = Color.BLACK;
                    else
                        pixels[index] = Color.WHITE;
                }else {
                    if(inverse)
                        pixels[index] = Color.WHITE;
                    else
                        pixels[index] = Color.BLACK;
                }
            }}
        res.setPixels(pixels, 0, w, 0, 0, w, h);
        return res;
    }
    // TODO : upgrade sobel for argb color space
    public Bitmap sobelFilter(Bitmap bm){
        int maxGval = 0;
        int h = bm.getHeight();
        int w = bm.getWidth();
        int[][] edgeColors = new int[w][h];
        int maxGradient = -1;
        int index = 0;
        int x,y;
        Bitmap res = bm.copy(bm.getConfig(), true);
        int[] pixels = new int[w*h];
        res.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                // i - 1, j - 1
                x = i - 1; y = j - 1;
                index = (y * w)+ x;
                int val00 = getGray(pixels[index]);
                // i - 1, j
                x = i - 1; y = j;
                index = (y * w)+ x;
                int val01 = getGray(pixels[index]);
                // i - 1, j + 1
                x = i - 1; y = j + 1;
                index = (y * w)+ x;
                int val02 = getGray(pixels[index]);

                // i, j - 1
                x = i; y = j - 1;
                index = (y * w)+ x;
                int val10 = getGray(pixels[index]);
                // i, j
                x = i; y = j;
                index = (y * w)+ x;
                int val11 = getGray(pixels[index]);
                // i, j + 1
                x = i; y = j + 1;
                index = (y * w)+ x;
                int val12 = getGray(pixels[index]);

                // i + 1, j - 1
                x = i + 1; y = j - 1;
                index = (y * w)+ x;
                int val20 = getGray(pixels[index]);
                // i + 1, j
                x = i + 1; y = j;
                index = (y * w)+ x;
                int val21 = getGray(pixels[index]);
                // i + 1, j + 1
                x = i + 1; y = j + 1;
                index = (y * w)+ x;
                int val22 = getGray(pixels[index]);

                // matrix filter
                int gx =  ((-1 * val00) + (0 * val01) + (1 * val02))
                        + ((-2 * val10) + (0 * val11) + (2 * val12))
                        + ((-1 * val20) + (0 * val21) + (1 * val22));

                int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02))
                        + ((0 * val10) + (0 * val11) + (0 * val12))
                        + ((1 * val20) + (2 * val21) + (1 * val22));

                double gval = Math.sqrt((gx * gx) + (gy * gy));
                int g = (int) gval;

                if(maxGradient < g) {
                    maxGradient = g;
                }
                edgeColors[i][j] = g;
            }
        }

        double scale = 255.0 / maxGradient;

        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                int edgeColor = edgeColors[i][j];
                edgeColor = (int)(edgeColor * scale);
                edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;
                index = (j*w)+i;
                pixels[index] = edgeColor;
            }
        }
        res.setPixels(pixels, 0, w, 0, 0, w, h);
        return res;
    }

    public int  getGray(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
        return gray;
    }

    /*
     * Dilation and Erosion Algorithm
     * source : https://blog.ostermiller.org/dilate-and-erode
     * modified by : zunzelf
     */
    int[] dilate(int[] image, int w, int h){
        for (int i=0; i < w; i++){
            for (int j=0; j < h; j++){
                int index = (w * j) + i;
                int index2 = (w * j) + i - 1;
                int index3 = (w * (j - 1)) + i;
                int index4 = (w * j) + i + 1;
                int index5 = (w * (j + 1)) + i;
                if (image[index] == Color.WHITE){
                    // i-1, j
                    if (i>0 && image[index2]==Color.BLACK) image[index2] = 2;
                    //  i, j-1
                    if (j>0 && image[index3]==Color.BLACK) image[index3] = 2;
                    //  i+1,j & i+1<w
                    if (i+1 < w && image[index4]==Color.BLACK) image[index4] = 2;
                    //  i, j+1 & j+1<h
                    if (j+1 < h && image[index5]==Color.BLACK) image[index5] = 2;
                }
            }
        }
        for (int i=0; i<w; i++){
            for (int j=0; j<h; j++){
                int index = (w * j) + i;
                if (image[index] == 2){
                    image[index] = Color.WHITE;
                }
            }
        }
        return image;
    }

    public Bitmap dilation(Bitmap bm){
        Bitmap res = bm.copy(bm.getConfig(), true);
        int w = bm.getWidth(); int h = bm.getHeight();
        int[] pxls = new int[w * h];
        res.getPixels(pxls, 0, w, 0, 0, w, h);
        pxls = dilate(pxls, w, h);
        res.setPixels(pxls, 0, w, 0, 0, w, h);
        return res;
    }

    public Bitmap dilation(Bitmap bm, int k){
        Bitmap res = bm.copy(bm.getConfig(), true);
        int w = bm.getWidth(); int h = bm.getHeight();
        int[] pxls = new int[w * h];
        res.getPixels(pxls, 0, w, 0, 0, w, h);
        for (int i = 0; i < k; i++)
            pxls = dilate(pxls, w, h);
        res.setPixels(pxls, 0, w, 0, 0, w, h);
        return res;
    }
//    int[] dilate(int[] image, int w, int h){
//        for (int i=0; i < w; i++){
//            for (int j=0; j < h; j++){
//                int index = (w * j) + i;
//                int index2 = (w * j) + i - 1;
//                int index3 = (w * (j - 1)) + i;
//                int index4 = (w * j) + i + 1;
//                int index5 = (w * (j + 1)) + i;
//                if (image[index] == Color.WHITE){
//                    // i-1, j
//                    if (i>0 && image[index2]==Color.BLACK) image[index2] = 2;
//                    //  i, j-1
//                    if (j>0 && image[index3]==Color.BLACK) image[index3] = 2;
//                    //  i+1,j & i+1<w
//                    if (i+1 < w && image[index4]==Color.BLACK) image[index4] = 2;
//                    //  i, j+1 & j+1<h
//                    if (j+1 < h && image[index5]==Color.BLACK) image[index5] = 2;
//                }
//            }
//        }
//        for (int i=0; i<w; i++){
//            for (int j=0; j<h; j++){
//                int index = (w * j) + i;
//                if (image[index] == 2){
//                    image[index] = Color.WHITE;
//                }
//            }
//        }
//        return image;
//    }
//
//    public Bitmap dilation(Bitmap bm){
//        Bitmap res = bm.copy(bm.getConfig(), true);
//        int w = bm.getWidth(); int h = bm.getHeight();
//        int[] pxls = new int[w * h];
//        res.getPixels(pxls, 0, w, 0, 0, w, h);
//        pxls = dilate(pxls, w, h);
//        res.setPixels(pxls, 0, w, 0, 0, w, h);
//        return res;
//    }
//
//    public Bitmap dilation(Bitmap bm, int k){
//        Bitmap res = bm.copy(bm.getConfig(), true);
//        int w = bm.getWidth(); int h = bm.getHeight();
//        int[] pxls = new int[w * h];
//        res.getPixels(pxls, 0, w, 0, 0, w, h);
//        for (int i = 0; i < k; i++)
//            pxls = dilate(pxls, w, h);
//        res.setPixels(pxls, 0, w, 0, 0, w, h);
//        return res;
//    }
}
