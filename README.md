# Fin Track - Advanced Finance Tracking APP

## Overview
This Android application reads SMS messages, processes them, and ensures that each message is processed only once using Firebase. The app also includes a refresh button to check for new messages after opening the app.

## Features
- **Firebase Integration**: Store and track processed SMS messages in Firebase Realtime Database.
- **SMS Permissions**: Requests necessary permissions to read SMS messages on the device.
- **Pattern Matching**: Extracts transaction details from SMS messages related to account credits, debits, and transfers.
- **One-Time Processing**: Ensures each SMS message is processed only once by checking against Firebase.
- **Refresh Button**: Manually trigger a check for new SMS messages that may have arrived after the app was opened.

## Project Structure
- `MainActivity.java`: Main activity that handles the initialization, SMS reading, Firebase interaction, and user authentication.
- `activity_main.xml`: Layout file containing the UI components, including the refresh button.
- `smsReceiver.java`: (Future Implementation) Handles background SMS receiving and processing.

## Firebase Setup
1. Add the necessary Firebase dependencies to your `build.gradle` file.
2. Initialize Firebase Authentication and Database in `MainActivity.java`.
3. Set up Firebase Database rules to allow read and write access for authenticated users.

## Permissions
This app requires the following permissions:
- `android.permission.READ_SMS`
- `android.permission.INTERNET`

## How to Use
1. **Login**: Use your Firebase credentials to log in.
2. **Grant Permissions**: Allow the app to read SMS messages when prompted.
3. **Read SMS**: The app will automatically read and process SMS messages, ensuring each one is processed only once.


## Future Enhancements
- Implement background SMS processing using `smsReceiver.java`.
- Add more transaction types and improve pattern matching.

## Download APK
- https://drive.google.com/drive/folders/1eEahRaO8MO4mSQDdvZoLjntexg1Bo4K9
