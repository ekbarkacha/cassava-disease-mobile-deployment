"""
Module: verify_tflite.py
========================

Author: Emmanuel
Email: ebarkacha@aimsammi.org
Created: December 2025

Description:
------------
Utilities for verifying and testing the exported TensorFlow Lite model for
Cassava Leaf Disease Classification.

This script provides two verification modes:

1. Dummy Input Verification:
   --------------------------
   Confirms that the TFLite model loads correctly, accepts the expected input
   tensor shape (1, 3, 380, 380), and produces an output tensor. This is useful
   for quick sanity checks immediately after model conversion.

2. Real Image Verification with Preprocessing:
   -------------------------------------------
   Loads a real cassava leaf image, applies the same preprocessing pipeline
   used during PyTorch model training (Resize → CenterCrop → ToTensor →
   Normalize), runs inference on the TFLite model, and visualizes the predicted
   disease class.

Features:
---------
- Dummy input verification.
- Real image inference with torchvision-style preprocessing.
- Softmax probability computation.
- Clean prediction printing.

Usage:
------
    python verify_tflite.py --mode dummy
    python verify_tflite.py --mode image --image docs/test_image/train-cbb-0.jpg

"""

import argparse
import os
import glob
import numpy as np
import tensorflow as tf
from PIL import Image
from torchvision import transforms
from torch.utils.data import Dataset
import warnings
warnings.filterwarnings("ignore")


## OPTION 1: Dummy Input Verification
def verify_with_dummy(model_path="models/cassava_model.tflite"):
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    dummy_input = np.random.rand(1, 3, 380, 380).astype(np.float32)

    interpreter.set_tensor(input_details[0]['index'], dummy_input)
    interpreter.invoke()

    pred = interpreter.get_tensor(output_details[0]['index'])

    expected_shape = (1, 5)

    if pred.shape != expected_shape:
        raise ValueError(f"Invalid output shape: {pred.shape}. Expected {expected_shape}.")
    else:
        print("Output shape is correct:", pred.shape)


## OPTION 2: Real Image Verification with Full Preprocessing
# Preprocessing (same as PyTorch training)
mean = [0.485, 0.456, 0.406]
std = [0.229, 0.224, 0.225]

test_val_transforms = transforms.Compose([
    transforms.Resize(400),
    transforms.CenterCrop(380),
    transforms.ToTensor(),
    transforms.Normalize(mean=mean, std=std)
])


class TestDataset(Dataset):
    """Loads a single image or all images in a directory."""
    def __init__(self, image_dir=None, image_path=None, transform=None):
        if image_dir:
            self.image_paths = sorted(glob.glob(os.path.join(image_dir, "*.jpg")))
        elif image_path:
            self.image_paths = [image_path]
        else:
            raise ValueError("Provide either image_dir or image_path")

        self.transform = transform

    def __len__(self):
        return len(self.image_paths)

    def __getitem__(self, idx):
        image = Image.open(self.image_paths[idx]).convert("RGB")
        if self.transform:
            image = self.transform(image)
        return image, self.image_paths[idx]


def verify_with_image(model_path="models/cassava_model.tflite",
                      img_path="docs/test_image/train-cbb-0.jpg"):

    # Load image using the PyTorch-style dataset
    dataset = TestDataset(image_path=img_path, transform=test_val_transforms)
    img_tensor, _ = dataset[0]  # Shape: (3, 380, 380)

    # Convert to NumPy and add batch dimension
    img_array = img_tensor.numpy()[np.newaxis, :, :, :]  # NCHW

    # Load TFLite model
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    interpreter.set_tensor(input_details[0]['index'], img_array.astype(np.float32))
    interpreter.invoke()

    logits = interpreter.get_tensor(output_details[0]['index'])

    # Softmax probabilities
    probs = np.exp(logits) / np.sum(np.exp(logits), axis=1, keepdims=True)
    pred_class = int(np.argmax(probs, axis=1)[0])

    cassava_labels = [
        "Cassava Bacterial Blight",
        "Cassava Brown Streak Disease",
        "Cassava Green Mottle",
        "Cassava Mosaic Disease",
        "Healthy"
    ]

    print(f"Predicted label: {cassava_labels[pred_class]}")
    print("Probabilities:", probs)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="TFLite Model Verification")

    parser.add_argument(
        "--mode",
        type=str,
        choices=["dummy", "image"],
        default="dummy",
        help="Verification mode: dummy or image."
    )

    parser.add_argument(
        "--image",
        type=str,
        default="docs/test_image/train-cbb-0.jpg",
        help="Path to image for 'image' mode."
    )

    args = parser.parse_args()

    if args.mode == "dummy":
        verify_with_dummy()

    elif args.mode == "image":
        verify_with_image(img_path=args.image)

    else:
        raise ValueError("Unknown verification mode: choose dummy or image")