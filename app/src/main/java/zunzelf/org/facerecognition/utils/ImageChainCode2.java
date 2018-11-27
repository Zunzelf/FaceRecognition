package zunzelf.org.facerecognition.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ImageChainCode2 {
    static final int white = 0xFFFFFFFF;
    static final int black = 0xFF000000;
    final static int[][][] nbrGroups = {{{0, 2, 4}, {2, 4, 6}}, {{0, 2, 6},
            {0, 4, 6}}};
    int obj = 0;
    int w = 0;
    int h = 0;
    static final String TAG = "imProc";
    int[] temp_c = new int[4];
    public Pair<Bitmap, int[]> seekObjects(Bitmap src, Bitmap inp){
        Bitmap origin = src.copy(src.getConfig(), true);
        w = inp.getWidth();
        List<int[]> objs = new ArrayList<int[]>();
        List<String> chainCode = new ArrayList<String>();
        List<Pair<Integer[], String>> res = new ArrayList<Pair<Integer[], String>>();
        double mean_box = 0;
        h = inp.getHeight();
        int[] bm = new int[w * h];
        inp.getPixels(bm, 0, w, 0, 0, w, h);
        int x = 1, y = 1;
        int clr, index;
        String ch = "";
        while(y < h-2){
            index = (y * w)+ x;
            clr = bm[index];
            if(clr != white){
                Log.d(TAG, "X : "+ x +", Y : "+y);
                ch = getChainCode(bm, x, y);
                Pair<Integer[], String> temp = new Pair(temp_c, ch);
                // xMax, yMax, xMin, yMin
                if(ch.length() > 10)
                    mean_box += (temp_c[0] - temp_c[2])*(temp_c[1] - temp_c[3]);
                res.add(temp);
            }
            if(x == w-2){
                x = 1;
                y += 1;
            }else
                x += 1;
        }
        mean_box = mean_box/res.size();
        Log.d("result", "mean object size = "+mean_box);
        int pts = 0;
        for (Pair p : res){
            temp_c = (int[])p.first;
            double size = (temp_c[0] - temp_c[2])*(temp_c[1] - temp_c[3]);
            if (size >= mean_box && ((String)p.second).length() > 10) {
                origin = drawBox(origin, temp_c);
                Log.d("result", pts+"-object : ");
                Log.d("result", "length : " + ((String)p.second).length());
                Log.d("result", "size   : " + size);
                Log.d("result", "width   : " + (temp_c[0] - temp_c[2]));
                Log.d("result", "height   : " + (temp_c[1] - temp_c[3]));
                chainCode.add((String)p.second);
                objs.add(temp_c);
                pts++;
            }
        }
        Log.d("result", "detected : "+chainCode.size());
        return new Pair<Bitmap, int[]>(origin, objs.get(0));
    }
    public Bitmap drawBox(Bitmap bm, int[] points){
        Bitmap res = bm.copy(bm.getConfig(), true);
        for(int y = points[3]; y <= (points[1] + 1); y++){
            for(int x = points[2]; x <= points[0] + 1; x++){
                if((x >= points[2]) && (x <= points[2] + 10))
                    res.setPixel(x, y, Color.GREEN);
                else if ((x <= points[0]) && (x >= points[0] - 10))
                    res.setPixel(x, y, Color.GREEN);
                else if ((y >= points[3]) && (y <= points[3] + 10))
                    res.setPixel(x, y, Color.GREEN);
                else if(y >= points[1] - 10 && y <= points[1]){
                    res.setPixel(x, y, Color.GREEN);}
            }
        }
        return res;
    }
    public String getChainCode(int[] bm, int initX, int initY){
        String chainCode = "0";
        boolean start = true;
        int count = 0;
        int x = initX;
        int y = initY;
        int xMax = x, xMin = x, yMax = y, yMin = y;
        int[] temp = new int[2];
        int dir = 0; //starting direction
        while(true){
            if(x == initX && y == initY && !start) break;
            int[] left = leftSide(dir); //get leftsides
            int[] right = rightSide(dir); //get rightsides
            int[] up = translate(x, y, dir); //get up coordinate
            int[] dirL = checkSide(bm, x, y, left);
            int[] dirR = checkSide(bm, x, y, right);
            int index = (up[1] * w)+ up[0];
            if(dirL.length <= 0 && dirR.length <= 0 && bm[index] == white){
                chainCode = "";
                break;
            }
            int upV = white;
            boolean safe;
            if( index < bm.length){
                safe = true;
                upV = bm[index];
            }
            else
                safe = false;
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
            count += 1;
        }
        Log.d(TAG, chainCode);
        Log.d(TAG,xMax+","+ yMax+","+xMin+","+ yMin);
        temp_c = new int[]{xMax, yMax, xMin, yMin};
        eraseObject(bm, xMax, yMax, xMin, yMin);
        return chainCode;
    }
    public int[] checkSide(int[] bm, int x, int y, int[] side){
        String gets = "";
        for (int dir : side) {
            int[] temp = translate(x, y, dir);
            int index = (temp[1] * w) + temp[0];
            if (index < bm.length)
                if(bm[index] != white)
                    gets += ""+dir;

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
    public void eraseObject(int[] bm, int x, int y, int i, int j){
        for (int b = j; b <= y; b++){
            for (int a = i; a <= x; a++){
                int index = (b * w) + a;
                bm[index] = white;
            }
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