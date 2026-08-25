# Backend for DermaCareAI - Setup Guide

This guide explains how to set up and run the Python backend for your DermaCareAI application.

## Prerequisites
1.  **Python 3.8+** installed on your system.
2.  **pip** (Python package manager).

## Setup Instructions

### 1. Install Dependencies
Open your terminal in the `backend` folder and run:
```bash
pip install -r requirements.txt
```

### 2. Run the Server
While in the `backend` folder, start the FastAPI server:
```bash
python main.py
```
Alternatively:
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

The server will be available at `http://localhost:8000`. You can view the automatic API documentation at `http://localhost:8000/docs`.

## Key Endpoints
- `POST /signup`: Register a new user.
- `POST /token`: Login to get an access token (JWT).
- `GET /users/me`: Get current user info.
- `POST /analyze`: Upload a skin image for AI analysis (Mocked for now).
- `GET /history`: Get a list of past analyses for the current user.

## Connecting Android to Backend
The Android app is already configured to connect to `http://10.0.2.2:8000` (which is the special IP Android Emulator uses to access your computer's localhost).

### Testing on a Real Device
If you use a real Android phone instead of an emulator:
1.  Find your computer's local IP address (e.g., `192.168.1.5`).
2.  Update `NetworkClient.kt` in the Android project:
    ```kotlin
    private const val BASE_URL = "http://YOUR_COMPUTER_IP:8000/"
    ```
3.  Ensure your phone and computer are on the same Wi-Fi network.

## Next Steps
- Integrate `NetworkClient.apiService` into your `LoginActivity.kt` and `SignUpActivity.kt` using `CoroutineScope(Dispatchers.IO).launch`.
- Pass the actual image file to the `/analyze` endpoint in `AnalyzingActivity.kt`.
