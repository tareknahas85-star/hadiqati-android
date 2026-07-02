# Hadiqati (my garden)

Android app for rooftop garden care: identify plants from a photo, get care tips, and keep track of your rooftop garden. Arabic-first, with full RTL support.

## Features
- Plant identification from a photo (camera capture, auto-resized before upload)
- Location-aware tips for your garden
- Care reminders via notifications
- Google sign-in
- Arabic-first UI with RTL support

## Tech stack
- Android WebView-based hybrid app wrapping the Hadiqati web experience
- Kotlin native shell
- GitHub Actions for automated APK builds

## Requirements
- Android permissions: Camera, Location (fine and coarse), Notifications (Android 13+)
- FileProvider for camera photo output

## Build

GitHub Actions (recommended): push to GitHub, the build workflow produces an APK as an artifact.

Locally:
```
gradle assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

## How it works

The app wraps the Hadiqati web app in an Android WebView, adding native camera capture with image resizing before plant identification, device location, and push notifications for care reminders. Google sign-in is handled through the WebView with redirect interception.
