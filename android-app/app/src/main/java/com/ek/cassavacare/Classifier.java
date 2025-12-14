/*
 * Project: CassavaCare
 * File: Classifier.java
 * Description: TensorFlow Lite image classifier for cassava leaf disease detection
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Classifier {
    private Interpreter tflite;
    private final String[] labels = {
            "Cassava Bacterial Blight",
            "Cassava Brown Streak Disease",
            "Cassava Green Mottle",
            "Cassava Mosaic Disease",
            "Healthy"
    };
    private static final int INPUT_SIZE = 380; // Matches CenterCrop size
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};
    private static final String TAG = "Classifier";

    public Classifier(Context context) throws IOException {
        tflite = new Interpreter(FileUtil.loadMappedFile(context, "cassava_model.tflite"));
        Log.d(TAG, "Model loaded successfully");
    }

    public String classify(Bitmap bitmap) {
//        if (bitmap == null || bitmap.getWidth() < INPUT_SIZE || bitmap.getHeight() < INPUT_SIZE) {
//            Log.e(TAG, "Invalid input image: " + (bitmap == null ? "null" : bitmap.getWidth() + "x" + bitmap.getHeight()));
//            throw new IllegalArgumentException("Invalid input image");
//        }
        if (bitmap == null) {
            Log.e(TAG, "Invalid input image: " + "null");
            throw new IllegalArgumentException("Invalid input image");
        }


        Bitmap adjustedBitmap = adjustBrightnessContrast(bitmap);
        Bitmap resizedBitmap = resizeBitmap(adjustedBitmap, 400);
        Log.d(TAG, "Resized dimensions: " + resizedBitmap.getWidth() + "x" + resizedBitmap.getHeight());
        Bitmap croppedBitmap = centerCropBitmap(resizedBitmap, INPUT_SIZE, INPUT_SIZE);
        Log.d(TAG, "Cropped dimensions: " + croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(croppedBitmap);

        float[][] output = new float[1][labels.length];
        tflite.run(inputBuffer, output);
        float[] probabilities = softmax(output[0]);
        Log.d(TAG, "Probabilities: " + java.util.Arrays.toString(probabilities));

        int maxIndex = 0;
        float maxConfidence = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxConfidence) {
                maxIndex = i;
                maxConfidence = probabilities[i];
            }
        }
        maxConfidence = Math.min(Math.max(maxConfidence, 0.0f), 1.0f);
        if (maxConfidence < 0.8f) {
            String warningMessage = "The model is uncertain about this image. Please ensure the leaf is clearly visible and well-lit, then try again.";
            Log.w(TAG, "Low confidence: " + (maxConfidence * 100) + "%");
            return warningMessage;
        }
        String result = String.format("%s - %.0f%% sure", labels[maxIndex], maxConfidence * 100);
        Log.d(TAG, "Classification result: " + result);
        return result;
    }

    public float[] getProbabilities(Bitmap bitmap) {
        Bitmap adjustedBitmap = adjustBrightnessContrast(bitmap);
        Bitmap resizedBitmap = resizeBitmap(adjustedBitmap, 400);
        Bitmap croppedBitmap = centerCropBitmap(resizedBitmap, INPUT_SIZE, INPUT_SIZE);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(croppedBitmap);
        float[][] output = new float[1][labels.length];
        tflite.run(inputBuffer, output);
        return softmax(output[0]);
    }

    private Bitmap adjustBrightnessContrast(Bitmap bitmap) {
//        Bitmap adjusted = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
//        android.graphics.Canvas canvas = new android.graphics.Canvas(adjusted);
//        android.graphics.Paint paint = new android.graphics.Paint();
//        ColorMatrix colorMatrix = new ColorMatrix();
//        colorMatrix.set(new float[] {
//                1.2f, 0, 0, 0, 20,
//                0, 1.2f, 0, 0, 20,
//                0, 0, 1.2f, 0, 20,
//                0, 0, 0, 1, 0
//        });
//        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
//        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmap;
    }

    private float[] softmax(float[] logits) {
        float maxLogit = logits[0];
        for (float logit : logits) {
            if (logit > maxLogit) maxLogit = logit;
        }
        float sum = 0.0f;
        float[] probabilities = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - maxLogit);
            sum += probabilities[i];
        }
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= sum;
        }
        return probabilities;
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int targetSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = targetSize / (float) Math.min(width, height);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
    }

    private Bitmap centerCropBitmap(Bitmap bitmap, int targetWidth, int targetHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newX = (width - targetWidth) / 2;
        int newY = (height - targetHeight) / 2;
        return Bitmap.createBitmap(bitmap, newX, newY, targetWidth, targetHeight);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        buffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        for (int pixel : pixels) {
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;
            buffer.putFloat((r - MEAN[0]) / STD[0]);
            buffer.putFloat((g - MEAN[1]) / STD[1]);
            buffer.putFloat((b - MEAN[2]) / STD[2]);
        }
        return buffer;
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
}
