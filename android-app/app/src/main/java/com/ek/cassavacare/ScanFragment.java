/*
 * Project: CassavaCare
 * File: ScanFragment.java
 * Description: Fragment handling image capture and classification of cassava leaves.
 *              Supports camera preview, image upload from gallery, model inference,
 *              displaying results, remedies, and storing scan history.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.ek.cassavacare.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {
    private static final String TAG = "ScanFragment";
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Classifier classifier;
    private AppDatabase db;
    private ExecutorService executorService;
    private ProgressBar progressBar;
    private Button retakeButton, captureButton, uploadButton;
    private ImageView imgThumbnail;
    private TextView tvResult, tvRemedy;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scan, container, false);

        previewView = root.findViewById(R.id.preview_view);
        captureButton = root.findViewById(R.id.btn_capture);
        uploadButton = root.findViewById(R.id.btn_upload);
        retakeButton = root.findViewById(R.id.btn_retake);
        progressBar = root.findViewById(R.id.progress_bar);
        imgThumbnail = root.findViewById(R.id.img_thumbnail);
        tvResult = root.findViewById(R.id.tv_result);
        tvRemedy = root.findViewById(R.id.tv_remedy);

        // Initialize Room database and executor service
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "database-name").build();
        executorService = Executors.newSingleThreadExecutor();

        // Initialize classifier asynchronously
        initializeClassifier();

        // Initialize gallery launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                processImageFromUri(imageUri);
            } else {
                Toast.makeText(requireContext(), "Image selection canceled", Toast.LENGTH_SHORT).show();
                resetToCamera();
            }
        });

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        // Check storage permission for Android 9 and below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        // Set button listeners
        captureButton.setOnClickListener(v -> takePhoto());
        uploadButton.setOnClickListener(v -> openGallery());
        retakeButton.setOnClickListener(v -> resetToCamera());

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                // Always pop back to HomeFragment
                navController.popBackStack(R.id.navigation_home, false);
                return true;
            } else {
                // Navigate to Scan or History normally
                navController.navigate(id);
                return true;
            }
        });

        return root;
    }

    private void initializeClassifier() {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Initializing Classifier...");
                classifier = new Classifier(requireContext());
                Log.d(TAG, "Classifier initialized successfully");
            } catch (IOException e) {
                Log.e(TAG, "Failed to initialize Classifier: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Model loading failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void startCamera() {
        Log.d(TAG, "Starting camera...");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                Log.d(TAG, "Camera provider acquired");
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                Log.d(TAG, "Camera bound successfully");
                captureButton.setVisibility(View.VISIBLE);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization failed: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Camera setup failed", Toast.LENGTH_SHORT).show());
                captureButton.setVisibility(View.GONE);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null || classifier == null) {
            Toast.makeText(requireContext(), "Camera or model not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.animate().alpha(1f).setDuration(200).start();
            previewView.setVisibility(View.GONE);
            captureButton.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            captureButton.setEnabled(false);
            uploadButton.setEnabled(false);
        });

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                Bitmap bitmap = imageProxyToBitmap(image);
                image.close();
                processImage(bitmap);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.animate().alpha(0f).setDuration(200).withEndAction(() -> progressBar.setVisibility(View.GONE)).start();
                    previewView.setVisibility(View.VISIBLE);
                    captureButton.setEnabled(true);
                    uploadButton.setEnabled(true);
                    captureButton.setVisibility(View.VISIBLE);
                    uploadButton.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            galleryLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fallbackIntent.setType("image/*");
            try {
                galleryLauncher.launch(fallbackIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(requireContext(), "No gallery or file picker available", Toast.LENGTH_SHORT).show();
                resetToCamera();
            }
        }
    }

    private void processImageFromUri(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            if (bitmap != null) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.animate().alpha(1f).setDuration(200).start();
                    previewView.setVisibility(View.GONE);
                    captureButton.setEnabled(false);
                    uploadButton.setEnabled(false);
                    captureButton.setVisibility(View.GONE);
                    uploadButton.setVisibility(View.GONE);
                });
                processImage(bitmap);
            } else {
                requireActivity().runOnUiThread(() -> {
                    progressBar.animate().alpha(0f).setDuration(200).withEndAction(() -> progressBar.setVisibility(View.GONE)).start();
                    previewView.setVisibility(View.VISIBLE);
                    captureButton.setEnabled(true);
                    uploadButton.setEnabled(true);
                    captureButton.setVisibility(View.VISIBLE);
                    uploadButton.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            requireActivity().runOnUiThread(() -> {
                progressBar.animate().alpha(0f).setDuration(200).withEndAction(() -> progressBar.setVisibility(View.GONE)).start();
                previewView.setVisibility(View.VISIBLE);
                captureButton.setEnabled(true);
                uploadButton.setEnabled(true);
                captureButton.setVisibility(View.VISIBLE);
                uploadButton.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void processImage(Bitmap bitmap) {
        if (classifier == null) {
            Log.e(TAG, "Classifier not initialized");
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Model not ready, please try again.", Toast.LENGTH_SHORT).show();
                resetToCamera();
            });
            return;
        }
        executorService.execute(() -> {
            try {
                String result = classifier.classify(bitmap);
                if (result.contains("uncertain about this image")) {
                    // Classification was not confident
                    requireActivity().runOnUiThread(() -> {
                        int textColor = ContextCompat.getColor(requireContext(), R.color.error_red);
                        tvResult.setTextColor(textColor);
                        progressBar.animate().alpha(0f).setDuration(200).withEndAction(() -> progressBar.setVisibility(View.GONE)).start();
                        imgThumbnail.setImageBitmap(bitmap);
                        imgThumbnail.setVisibility(View.VISIBLE);
                        tvResult.setText(result);
                        tvResult.setVisibility(View.VISIBLE);
                        tvRemedy.setVisibility(View.GONE);
                        retakeButton.setVisibility(View.VISIBLE);
                        captureButton.setEnabled(true);
                        uploadButton.setEnabled(true);
                        captureButton.setVisibility(View.GONE);
                        uploadButton.setVisibility(View.GONE);
                    });
                } else {

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 70, outputStream);
                    byte[] imageBytes = outputStream.toByteArray();

                    ScanResult scanResult = new ScanResult(result, System.currentTimeMillis(), imageBytes);
                    executorService.execute(() -> db.scanResultDao().insert(scanResult));

                    requireActivity().runOnUiThread(() -> {
                        int textColor = ContextCompat.getColor(requireContext(), R.color.text_primary);
                        tvResult.setTextColor(textColor);
                        progressBar.animate().alpha(0f).setDuration(200).withEndAction(() -> progressBar.setVisibility(View.GONE)).start();
                        imgThumbnail.setImageBitmap(bitmap);
                        imgThumbnail.setVisibility(View.VISIBLE);
                        tvResult.setText(result);
                        tvResult.setVisibility(View.VISIBLE);
                        tvRemedy.setText(getRemedy(result.split(" - ")[0]));
                        tvRemedy.setVisibility(View.VISIBLE);
                        retakeButton.setVisibility(View.VISIBLE);
                        captureButton.setEnabled(true);
                        uploadButton.setEnabled(true);
                        captureButton.setVisibility(View.GONE);
                        uploadButton.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Classification failed: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Classification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetToCamera();
                });
            }
        });
    }

    private Bitmap imageProxyToBitmap(androidx.camera.core.ImageProxy image) {
        androidx.camera.core.ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        java.nio.ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String getRemedy(String disease) {
        Map<String, String> remedies = new HashMap<>();

        remedies.put("Cassava Bacterial Blight",
                "1. Remove and safely destroy infected plants to prevent spread.\n" +
                        "2. Apply copper-based bactericides as recommended.\n" +
                        "3. Ensure proper spacing and good drainage to reduce humidity.\n" +
                        "4. Practice crop rotation to minimize disease buildup."
        );

        remedies.put("Cassava Brown Streak Disease",
                "1. Use certified disease-free planting material.\n" +
                        "2. Control whitefly populations, the primary virus vector.\n" +
                        "3. Monitor plants regularly for early symptoms.\n" +
                        "4. Implement proper field sanitation and crop rotation."
        );

        remedies.put("Cassava Green Mottle",
                "1. Remove infected leaves and plants to reduce viral spread.\n" +
                        "2. Monitor whitefly populations and apply organic or approved controls.\n" +
                        "3. Maintain healthy soil with organic mulch and balanced nutrients.\n" +
                        "4. Use resistant varieties if available."
        );

        remedies.put("Cassava Mosaic Disease",
                "1. Plant resistant or tolerant cassava varieties.\n" +
                        "2. Remove and destroy severely infected plants.\n" +
                        "3. Control whiteflies to reduce virus transmission.\n" +
                        "4. Practice proper spacing and crop rotation to limit disease."
        );

        remedies.put("Healthy",
                "Maintain good agricultural practices:\n" +
                        "- Proper soil preparation and fertilization.\n" +
                        "- Adequate spacing and irrigation.\n" +
                        "- Regular monitoring for pests and diseases.\n" +
                        "- Use certified clean planting material."
        );

        return remedies.getOrDefault(disease,
                "The disease was not recognized. Please consult a certified agricultural extension officer for guidance."
        );
    }


    private void resetToCamera() {
        imgThumbnail.setVisibility(View.GONE);
        tvResult.setVisibility(View.GONE);
        tvRemedy.setVisibility(View.GONE);
        retakeButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        captureButton.setEnabled(true);
        uploadButton.setEnabled(true);
        captureButton.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
        } else if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (classifier != null) classifier.close();
        if (executorService != null) executorService.shutdown();
        if (db != null) db.close();
    }
}