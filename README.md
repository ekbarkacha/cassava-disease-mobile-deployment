# Cassava Disease Mobile Deployment (Android + TensorFlow Lite)

![Platform](https://img.shields.io/badge/Platform-Android-blue)
![Model](https://img.shields.io/badge/Model-TensorFlow%20Lite-orange)
![Framework-PyTorch](https://img.shields.io/badge/Framework-PyTorch-red)
![ONNX](https://img.shields.io/badge/ONNX-Enabled-green)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

This repository contains the **full mobile deployment pipeline** for the Cassava Leaf Disease Classification system. It includes:

1. PyTorch → ONNX → TensorFlow Lite model conversion  
2. Optimized `cassava_model.tflite` model used in production  
3. Native Android mobile application  
4. APK for direct installation  
5. Verification and testing scripts  

This project brings **state-of-the-art deep learning directly to smartphones** for real-time cassava leaf disease diagnosis.

---

## Deployment Pipeline

```text
PyTorch (DenseNet121.pth)
      ↓
ONNX (model.onnx)
      ↓
TensorFlow SavedModel
      ↓
TensorFlow Lite (cassava_model.tflite)
      ↓
Android Mobile App (APK)
```
---

## Repository Structure

```bash
cassava-disease-mobile-deployment/
│
├── LICENSE
├── README.md
├── requirements.txt
├── .gitignore
│
├── models/
│   ├── DenseNet121.pth         # Original trained model from training repo
│   ├── model_tf/               # Generated TensorFlow SavedModel Dir
│   ├── model.onnx              # Exported ONNX model
│   └── cassava_model.tflite    # FINAL DEPLOYED MODEL
│
├── conversion/                 # Conversion scripts and notebook
│   ├── export_to_tflite_notebook.ipynb
│   ├── pytorch_onnx_to_tflite.py
│   └── verify_tflite.py
│
├── android-app/                # Android Studio project
│   ├── app/
│   ├── build.gradle
│   ├── gradlew
│   └── settings.gradle
│
└── docs/
    ├── screenshots/
    │   ├── splash.png
    │   ├── camera.png
    │   └── prediction.png
    │
    ├── test_image/
    │   └── train-cbb-0.jpg
    │
    └── cassava-disease-app.apk    # FINAL APK
```
---
# Model Overview

### **Supported Classes**

* Cassava Bacterial Blight (CBB)
* Cassava Brown Streak Disease (CBSD)
* Cassava Green Mottle (CGM)
* Cassava Mosaic Disease (CMD)
* Healthy Leaf

---

# Environment & Version Requirements

To ensure compatibility during conversion and Android deployment, the following versions are recommended:

### **Python / ML Stack**

* **Python:** 3.9+
* **PyTorch:** 2.0+
* **TensorFlow:** 2.9+
* **ONNX:** 1.14+
* **ONNX Runtime:** 1.16+

### **Android Development**

* **Android Studio:** Giraffe+ (AGP 8+)
* **Minimum Android SDK:** 23
* **Target SDK:** 33+

For full reproducibility, refer to:

```
requirements.txt
```

---

## Related Training Repository

All model training, experiments, and evaluation were performed in the main repository:

**[https://github.com/ekbarkacha/cassava-disease-classification](https://github.com/ekbarkacha/cassava-disease-classification)**

---

## Download Trained & Converted Models

To keep the repository lightweight, trained model weights and its conversions are stored externally [**Here**](https://drive.google.com/drive/folders/1tpFexu7ImzCprjWjDKHXjd2VC3ldeuR6?usp=drive_link).  

This folder contains:
- `DenseNet121.pth` - Original trained DenseNet121 model.
- `model/model_tf/` - Generated TensorFlow SavedModel Dir. 
- `model.onnx`- Exported ONNX model. 
- `cassava_model.tflite` - Final tflite deployed model

After downloading, place the files into there respective folders i.e `models/`.

---

## **Installation**

### Clone this repository

```bash
git clone https://github.com/ekbarkacha/cassava-disease-mobile-deployment.git
cd cassava-disease-mobile-deployment
```

### Install Python dependencies

```bash
pip install -r requirements.txt
```
---

## Model Conversion

### 1. PyTorch → ONNX → TensorFlow → TensorFlow Lite

```bash
python conversion/pytorch_onnx_to_tflite.py
```
### 2. Verify Generated TFLite Model

**Dummy input test:**

```bash
python conversion/verify_tflite.py --mode dummy
```

**Real image test:**

```bash
python conversion/verify_tflite.py --mode image --image docs/test_image/train-cbb-0.jpg
```

You can also do a step by step full conversion in the [notebook](./conversion/export_to_tflite_notebook.ipynb):

```text
conversion/export_to_tflite_notebook.ipynb
```
---

## Final Deployed Model

The final optimized model used in the Android application is located [here (cassava_model.tflite)](https://drive.google.com/file/d/1UTXexzt6sXtHY-5S_nkyZxY4f9OrI0jk/view?usp=sharing).

---

## Android Mobile Application

### Features

* Real-time camera-based disease detection
* Gallery image upload
* On-device inference using TensorFlow Lite
* Works offline (no internet required)
* Lightweight & farmer-friendly UI

---

### Run the App in Android Studio

1. Open `android-app/` in **Android Studio**
2. Let Gradle sync
3. Place the model here:

```text
android-app/app/src/main/assets/cassava_model.tflite
```

4. Connect a device or start an emulator
5. Click **Run**

---

## APK Download

You can directly install the application using:

```text
docs/cassava-disease-app.apk
```

---

## **License**

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.