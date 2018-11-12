package zunzelf.org.facerecognition.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ImageChainCode {
    static final int white = 0xFFFFFFFF;
    static final int black = 0xFF000000;
    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}}, {{0, 2, 6},
            {0, 4, 6}}};
    int obj = 0;
    static final String TAG = "imProc";
    int[] temp_c = new int[4];

    public static Bitmap createBlackAndWhite(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final float factor = 255f;
        final float redBri = 0.2126f;
        final float greenBri = 0.2126f;
        final float blueBri = 0.0722f;

        int length = width * height;
        int[] inpixels = new int[length];
        int[] oupixels = new int[length];

        src.getPixels(inpixels, 0, width, 0, 0, width, height);

        int point = 0;
        for(int pix: inpixels){
            int R = (pix >> 16) & 0xFF;
            int G = (pix >> 8) & 0xFF;
            int B = pix & 0xFF;

            float lum = (redBri * R / factor) + (greenBri * G / factor) + (blueBri * B / factor);

            if (lum > 0.4) {
                oupixels[point] = white;
            }else{
                oupixels[point] = black;
            }
            point++;
        }
        bmOut.setPixels(oupixels, 0, width, 0, 0, width, height);
        return bmOut;
    }
    public static int[][] toBinary(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[][] res = new int[width][height];

        final float factor = 255f;
        final float redBri = 0.2126f;
        final float greenBri = 0.2126f;
        final float blueBri = 0.0722f;

        int length = width * height;
        int[] inpixels = new int[length];
        int[] oupixels = new int[length];
        int x = 0;
        int y = 0;
        src.getPixels(inpixels, 0, width, 0, 0, width, height);

        int point = 0;
        for(int pix: inpixels){
            int R = (pix >> 16) & 0xFF;
            int G = (pix >> 8) & 0xFF;
            int B = pix & 0xFF;

            float lum = (redBri * R / factor) + (greenBri * G / factor) + (blueBri * B / factor);

            if (lum > 0.4) {
                res[x][y] = 0;
            }else{
                res[x][y] = 1;
            }
            point++;
            x++;
            if(x > width){
                y++;
                x = 0;
            }
        }
        return res;
    }
    public List<String> seekObjects(Bitmap bm){
        bm = bm.copy(bm.getConfig(), true);
        int w = bm.getWidth();
        List<String> chainCode = new ArrayList<String>();
        List<Pair<Integer[], String>> res = new ArrayList<Pair<Integer[], String>>();
        double mean_box = 0;
        int h = bm.getHeight();
        int x = 1, y = 1;
        int clr;
        String ch = "";
        while(y < h-1){
            clr = bm.getPixel(x,y);
            if(clr != white){
                Log.d(TAG, "X : "+ x +", Y : "+y);
                ch = getChainCode(bm, x, y);
                Pair<Integer[], String> temp = new Pair(temp_c, ch);
                // xMax, yMax, xMin, yMin
                mean_box += (temp_c[0] - temp_c[2])*(temp_c[1] - temp_c[3]);
                res.add(temp);
            }
            if(x == w-2){
                x = 0;
                y += 1;
            }else
                x += 1;
        }
        mean_box = mean_box/res.size();
        Log.d("result", "mean object size = "+mean_box);
        for (Pair p : res){
            temp_c = (int[])p.first;
            double size = (temp_c[0] - temp_c[2])*(temp_c[1] - temp_c[3]);
            if (size >= mean_box) {
                chainCode.add((String)p.second);
            }
        }
        Log.d("result", "detected : "+chainCode.size());
        return chainCode;
    }
    // this method used for marking objects detected by seekobject
    public Pair<Bitmap, List<String>> seekObjects(Bitmap src, Bitmap bm){
        bm = bm.copy(bm.getConfig(), true);
        Bitmap origin = src.copy(src.getConfig(), true);
        int w = bm.getWidth();
        List<String> chainCode = new ArrayList<String>();
        List<Pair<Integer[], String>> res = new ArrayList<Pair<Integer[], String>>();
        double mean_box = 0;
        int h = bm.getHeight();
        int x = 1, y = 1;
        int clr;
        String ch = "";
        while(y < h-1){
            clr = bm.getPixel(x,y);
            if(clr != white){
                Log.d(TAG, "X : "+ x +", Y : "+y);
                ch = getChainCode(bm, x, y);
                Pair<Integer[], String> temp = new Pair(temp_c, ch);
                // xMax, yMax, xMin, yMin
                mean_box += (temp_c[0] - temp_c[2])*(temp_c[1] - temp_c[3]);
                res.add(temp);
            }
            if(x == w-2){
                x = 0;
                y += 1;
            }else
                x += 1;
        }
        mean_box = mean_box/res.size();
        Log.d("result", "mean object size = "+mean_box);
        for (Pair p : res){
            temp_c = (int[])p.first;
            double size = (temp_c[0] - temp_c[2])*(temp_c[1] - temp_c[3]);
            if (size >= mean_box) {
                origin = drawBox(origin, temp_c);
                chainCode.add((String)p.second);
            }
        }
        Log.d("result", "detected : "+chainCode.size());
        return new Pair<Bitmap, List<String>>(origin, chainCode);
    }
    public Bitmap drawBox(Bitmap bm, int[] points){
        Bitmap res = bm.copy(bm.getConfig(), true);
        for(int y = points[3]-1; y < points[1]+1; y++){
            for(int x = points[2]-1; x < points[0]+1; x++){
                if(x == points[2] || x == points[0] || y == points[3] || y == points[1])
                    res.setPixel(x, y, Color.GREEN);
            }
        }
        return res;
    }
    public String getChainCode(Bitmap bm, int initX, int initY){
        String chainCode = "0";
        boolean start = true;
        int x = initX;
        int y = initY;
        int xMax = x, xMin = x, yMax = y, yMin = y;
        int[] temp = new int[2];
        int dir = 0; //starting direction
        Log.d(TAG, " height : "+bm.getHeight());
        Log.d(TAG, " width : "+bm.getWidth());
        while(true){
            if(x == initX && y == initY && !start) break;
            int[] left = leftSide(dir); //get leftsides
            int[] right = rightSide(dir); //get rightsides
            int[] up = translate(x,y,dir); //get up coordinate
            int[] dirL = checkSide(bm, x, y, left);
            int[] dirR = checkSide(bm, x, y, right);
            int upV = bm.getPixel(up[0], up[1]);
            // checking per-dir
            if(dirL.length > 0){
                if(dirL.length > 1 && upV != white){
                    dir = dirL[dirL.length - 1];
                }else dir = dirL[0];
                temp = translate(x, y, dir);
            }
            else if(upV != white){
                temp = up;
            }
            else if(dirR.length > 0){
                if(dirR.length > 1 && upV != white){
                    dir = dirR[dirR.length - 1];
                }else dir = dirR[0];
                temp = translate(x, y, dir);
            }
            else {
                Log.d(TAG,"eop");
                break;
            }
            chainCode += ""+dir;
            x = temp[0];
            y = temp[1];
            if(x > xMax) xMax =x;
            if(x < xMin) xMin = x;
            if(y > yMax) yMax = y;
            if(y < yMin) yMin = y;
            start = false;
        }
        Log.d(TAG, chainCode);
        Log.d(TAG,xMax+","+ yMax+","+xMin+","+ yMin);
        temp_c = new int[]{xMax, yMax, xMin, yMin};
        eraseObject(bm, xMax, yMax, xMin, yMin);
        return chainCode;
    }
    public int[] checkSide(Bitmap bm, int x, int y, int[] side){
        String gets = "";
        for (int dir : side) {
            int[] temp = translate(x, y, dir);
            if(bm.getPixel(temp[0], temp[1]) != white){
                gets += ""+dir;
            }
        }
        int[] res = stringToInts(gets);
        return res;
    }
    public int[] translate(int x, int y, int pos){
        if(pos > 7) pos = 0;
        switch (pos){
            case 1 : return new int[]{x+1, y-1};
            case 0 : return new int[]{x+1, y};
            case 7 : return new int[]{x+1, y+1};
            case 6 : return new int[]{x, y+1};
            case 5 : return new int[]{x-1, y+1};
            case 4 : return new int[]{x-1, y};
            case 3 : return new int[]{x-1, y-1};
            case 2 : return new int[]{x, y-1};
            default: return null;
        }
    }
    public int[] leftSide(int pos){
        switch (pos){
            case 0 : return new int[]{1,2,3};
            case 1 : return new int[]{2,3,4};
            case 2 : return new int[]{3,4,5};
            case 3 : return new int[]{4,5,6};
            case 4 : return new int[]{5,6,7};
            case 5 : return new int[]{6,7,0};
            case 6 : return new int[]{7,0,1};
            default : return new int[]{0,1,2};
        }
    }
    public int[] rightSide(int pos){
        switch (pos){
            case 0 : return new int[]{7,6,5};
            case 1 : return new int[]{0,7,6};
            case 2 : return new int[]{1,0,7};
            case 3 : return new int[]{2,1,0};
            case 4 : return new int[]{3,2,1};
            case 5 : return new int[]{4,3,2};
            case 6 : return new int[]{5,4,3};
            default : return new int[]{6,5,4};
        }
    }
    public void eraseObject(Bitmap bm, int x, int y, int i, int j){
        for (int b = j; b <= y; b++){
            for (int a = i; a <= x; a++)
                bm.setPixel(a, b, white);
        }
    }
    public int[] stringToInts(String s){
        int[] res = new int[s.length()];
        char[] temp = s.toCharArray();
        for(int i = 0; i < s.length();i++)
            res[i] = Character.getNumericValue(temp[i]);
        return res;
    }
}