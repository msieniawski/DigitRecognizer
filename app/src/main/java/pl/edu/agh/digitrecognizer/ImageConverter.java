package pl.edu.agh.digitrecognizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class ImageConverter {

    public double[] scaleAndGetVector(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createBitmap(bitmap, bitmap.getHeight() / 6, 0, bitmap.getHeight(), bitmap.getHeight());
        Bitmap scaled = Bitmap.createScaledBitmap(resized, 28, 28, false);
        Bitmap grey = toGreyscale(scaled);

        //storeImage(bitmap);
        //storeImage(scaled);
        //storeImage(grey);

        double[][] greyscale = new double[grey.getHeight()][grey.getWidth()];
        int offset = 5;

        for (int i = offset; i < grey.getHeight() - offset; i++) {
            for (int j = offset; j < grey.getWidth() - offset; j++) {
                int pixel = grey.getPixel(i, j) & 0xFF;
                double s = scale(pixel);
                greyscale[i][j] = s;
                greyscale[i][j] = s < 0.5 ? 0 : 1;
            }
        }

        for (int i = 0; i < greyscale.length; i++) {
            for (int j = 0; j < greyscale[i].length / 2; j++) {
                double tmp = greyscale[i][j];
                greyscale[i][j] = greyscale[i][greyscale[i].length - j - 1];
                greyscale[i][greyscale[i].length - j - 1] = tmp;
            }
        }

        for (double[] g : greyscale) {
            for (int j = 0; j < g.length; j++) {
                System.out.print(g[j] == 0 ? ' ' : 'X');
                System.out.print(' ');
            }
            System.out.println();
        }

        /*for (int i = 0; i < greyscale.length; i++) {
            for (int j = 0; j < greyscale[i].length; j++) {
                int a = (int) (greyscale[i][j] * 10);
                a = a == 10 ? 9 : a;
                System.out.print(a + " ");
            }
            System.out.println();
        }*/

        return flatten(greyscale);
    }

    private Bitmap toGreyscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);

        return bmpGrayscale;
    }

    private double scale(int pixel) {
        return 1 - (double) pixel / 255;
    }

    private double[] flatten(double[][] array) {
        double[] vector = new double[array.length * array[0].length];
        int index = 0;
        for (double[] arr : array)
            for (double a : arr)
                vector[index++] = a;
        return vector;
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + "DigitRecognizer"
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = String.format("%d", new Random().nextInt());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
}
