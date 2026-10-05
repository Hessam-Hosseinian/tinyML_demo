# TinyML Image Classifier

An Android application that performs **image classification entirely on-device** using TensorFlow Lite.

The project demonstrates how a lightweight ML model can be embedded in a mobile application so images can be analyzed without sending them to a remote server.

## Features

- Select an image from the gallery
- Capture an image with the camera
- Run inference locally on the Android device
- Display the top three predictions
- Show confidence scores
- Display inference time
- Works without a backend or internet connection for inference

## Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- TensorFlow Lite Task Vision
- Quantized MobileNet model
- Gradle Kotlin DSL

## Why on-device inference?

Running the model locally provides several useful properties:

- **Lower latency** — no network round trip
- **Offline operation** — inference can work without connectivity
- **Privacy** — the selected image does not need to be uploaded to a server
- **Simple deployment** — no inference backend is required

## Requirements

- Android Studio
- Android SDK
- Android 7.0+ / API 24+
- JDK compatible with the Android Gradle configuration

The project currently targets Android API 36 and uses Java 11 compatibility for compilation.

## Getting started

Clone the repository:

```bash
git clone https://github.com/Hessam-Hosseinian/tinyML_demo.git
cd tinyML_demo
```

Open the project in Android Studio, let Gradle sync, and run the application on an emulator or physical Android device.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
gradlew.bat assembleDebug
```

## Project goal

This is a compact educational demo of the typical TinyML/mobile-ML pipeline:

```text
Image input
    ↓
Preprocessing
    ↓
TensorFlow Lite model
    ↓
On-device inference
    ↓
Top predictions + confidence
```

## Contributors

- Hessam Hosseinian
- Arian Kheirandish

## Status

Educational / demonstration project.

## License

No open-source license is currently provided.
