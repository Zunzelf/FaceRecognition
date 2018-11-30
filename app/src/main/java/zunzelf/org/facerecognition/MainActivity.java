package zunzelf.org.facerecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import zunzelf.org.facerecognition.utils.ImageChainCode2;
import zunzelf.org.facerecognition.utils.ImageUtils;
import zunzelf.org.facerecognition.utils.ImageChainCode;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    ImageView inp;
    Uri imageURI;
    Bitmap bitmap, bitmap2;
    int [][] rgb_hist = new int[3][256];
    int scale = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViewButton();
    }

    // image view button
    private void imageViewButton(){
        inp = (ImageView) findViewById(R.id.imageView);
        inp.setClickable(true);
        inp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery(){
        Intent gallery =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== RESULT_OK && requestCode == PICK_IMAGE){
            // Load Image File
            imageURI = data.getData();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                int sizeY = bitmap.getHeight() * scale / 100;
                int sizeX = bitmap.getWidth() * scale /100;
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false);
                inp.setImageBitmap(bitmap);
                bitmap2 = new ImageUtils().skinFilter(bitmap, true);
                Pair<Bitmap, int[]> prcssd = new ImageChainCode2().seekObjects(bitmap, bitmap2);
                inp.setImageBitmap(prcssd.first);
                int[] pts = prcssd.second;
                Log.d("result", "drawing @("+pts[0]+", "+pts[1]+", "+pts[2]+", "+pts[3]+")");
                bitmap2 = Bitmap.createBitmap(bitmap, pts[2], pts[3], pts[0] - pts[2], pts[1] - pts[3]);
                ((ImageView) findViewById(R.id.imageView3)).setImageBitmap(new ImageUtils().sobelFilter(bitmap2));
//                bitmap2 = new ImageUtils().dilation(bitmap2, 3);
                bitmap2 = new ImageUtils().skinFilter(bitmap2);
                ((ImageView) findViewById(R.id.imageView2)).setImageBitmap(bitmap2);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
