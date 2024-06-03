# Familia

Familia is an Android application designed to make family management easier. Built using Jetpack Compose and Kotlin, the app leverages Firebase for authentication and Firestore for database management. It utilizes dependency injection and context managers to efficiently manage tasks and events. Additional features include CameraX integration and a barcode scanner.

![Familia Demo](./familia-demo.gif)

## Features

- **User Authentication**: Secure login and registration using Firebase Authentication.
- **Event Management**: Add, view, and manage family events on a shared calendar.
- **Firestore Database**: Real-time data storage and retrieval with Firestore.
- **Dependency Injection**: Efficient and scalable code management using dependency injection.
- **Context Managers**: Simplified context handling to add items to the calendar.
- **CameraX Integration**: Capture photos directly within the app using CameraX.
- **Barcode Scanner**: Scan barcodes to quickly add items to the family inventory.

## Installation

1. Clone the repository:
    ```sh
    https://github.com/leemabhena/familia.git
    ```
2. Open the project in Android Studio.
3. Build the project and run it on an emulator or a physical device.

## Usage

1. Launch the Familia app.
2. Register or log in using your Firebase account.
3. Navigate through the bottom navigation bar to access different features:
   - **Calendar**: View and manage family events.
   - **Tools**: Access utility tools like the barcode scanner.
   - **Chats**: Communicate with family members.
   - **Profile**: Manage your profile and app settings.
4. Add new events or items using the floating action button.

## Technical Details

- **Built with Kotlin**: Utilizes Kotlin for concise and expressive code.
- **Jetpack Compose**: Modern Android UI toolkit for building native interfaces.
- **Firebase Authentication**: Secure authentication for users.
- **Firestore Database**: Real-time data handling with Firestore.
- **CameraX**: Integration for capturing photos.
- **Barcode Scanner**: Easily add items to the inventory using barcode scanning.
