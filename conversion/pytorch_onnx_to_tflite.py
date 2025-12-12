"""
Module: pytorch_onnx_to_tflite.py
============================

Author: Emmanuel
Email: ebarkacha@aimsammi.org
Created: December 2025

Description:
------------
End-to-end conversion pipeline for the Cassava Disease Classification model.

This script:
1. Loads a PyTorch DenseNet121 model and its trained weights.
2. Exports the model to ONNX format.
3. Validates the ONNX graph.
4. Converts ONNX -> TensorFlow SavedModel.
5. Converts the SavedModel -> TensorFlow Lite format.
6. Computes the file size of the generated .tflite model.

"""

import os
import torch
import torch.nn as nn
import torchvision.models as models
import onnx
from onnx_tf.backend import prepare
import tensorflow as tf
import warnings
warnings.filterwarnings("ignore")

# Absolute paths
ROOT = os.path.dirname(os.path.abspath(__file__))
MODELS = os.path.join(ROOT, "..", "models")

ONNX_PATH = os.path.join(MODELS, "model.onnx")
TF_PATH = os.path.join(MODELS, "model_tf")
WEIGHTS_PATH = os.path.join(MODELS, "DenseNet121.pth")
TFLITE_PATH = os.path.join(MODELS, "cassava_model.tflite")

# Validate directories and input paths
def validate_paths():
    print("[Checking Paths]")

    # Models directory
    if not os.path.exists(MODELS):
        print(f"[INFO] Creating models directory: {MODELS}")
        os.makedirs(MODELS, exist_ok=True)

    # Trained weights
    if not os.path.isfile(WEIGHTS_PATH):
        raise FileNotFoundError(
            f"\nMissing trained weights:\n{WEIGHTS_PATH}\n"
            "Place DenseNet121.pth in the models/ directory.\n"
        )

    # output directories
    for path in [os.path.dirname(ONNX_PATH), os.path.dirname(TF_PATH), os.path.dirname(TFLITE_PATH)]:
        if not os.path.exists(path):
            print(f"[INFO] Creating directory: {path}")
            os.makedirs(path, exist_ok=True)

    print("All required paths are valid.\n")

# Load PyTorch Model
def export_pytorch_to_onnx():
    print("Loading PyTorch model (DenseNet121)...")

    num_classes = 5

    model = models.densenet121(pretrained=False)
    model.classifier = nn.Linear(model.classifier.in_features, num_classes)

    model.load_state_dict(torch.load(WEIGHTS_PATH, map_location="cpu"))
    model.eval()

    # Dummy input for tracing
    dummy_input = torch.randn(1, 3, 380, 380)

    print("Exporting to ONNX...")
    torch.onnx.export(
        model,
        dummy_input,
        ONNX_PATH,
        input_names=["input"],
        output_names=["output"],
        dynamic_axes={"input": {0: "batch"}, "output": {0: "batch"}},
        verbose=False
    )

    print("ONNX model generated.")


# Validate ONNX Model
def validate_onnx():
    print("Validating ONNX model...")
    onnx_model = onnx.load(ONNX_PATH)
    onnx.checker.check_model(onnx_model)
    print("ONNX model is valid.")


# Convert ONNX -> TensorFlow
def convert_onnx_to_tf():
    print("Converting ONNX -> TensorFlow SavedModel...")
    onnx_model = onnx.load(ONNX_PATH)
    tf_rep = prepare(onnx_model)
    tf_rep.export_graph(TF_PATH)
    print("TensorFlow SavedModel created.")


# Convert TensorFlow -> TFLite
def convert_tf_to_tflite():
    print("Converting TensorFlow -> TFLite...")

    converter = tf.lite.TFLiteConverter.from_saved_model(TF_PATH)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]

    tflite_model = converter.convert()

    with open(TFLITE_PATH, "wb") as f:
        f.write(tflite_model)

    print(f"TFLite model saved: {TFLITE_PATH}")


# Get TFLite File Size
def print_tflite_size():
    size_bytes = os.path.getsize(TFLITE_PATH)
    size_mb = size_bytes / (1024 * 1024)
    print(f"TFLite Model Size: {size_mb:.2f} MB")

if __name__ == "__main__":
    print("\n=== Cassava Model Conversion Pipeline ===\n")

    validate_paths()
    export_pytorch_to_onnx()
    validate_onnx()
    convert_onnx_to_tf()
    convert_tf_to_tflite()
    print_tflite_size()

    print("\n Conversion completed successfully \n")
