# Hadiqati (حديقتي)

**[⬇️ حمّل الـ APK (آخر نسخة)](https://github.com/tareknahas85-star/hadiqati-android/releases/download/latest/hadiqati.apk)** &nbsp;|&nbsp; **[⬇️ Download latest APK](https://github.com/tareknahas85-star/hadiqati-android/releases/download/latest/hadiqati.apk)**

---

## بالعربي

تطبيق أندرويد لحدائق السطوح: صوّر النبتة وتطبيق بيقلك شو هي، وبيعطيك نصائح عناية، وبيفكّرك بمواعيد سقي/عناية. عربي أولاً، مع دعم كامل لـ RTL.

### شو فيه
- تمييز النبتة من صورة (تصوير مباشر من الكاميرا، بيتصغّر تلقائياً قبل الرفع)
- نصائح عناية حسب موقعك
- تذكيرات عناية عبر الإشعارات
- تسجيل دخول بحساب Google
- واجهة عربية أولاً مع RTL كامل

### التقنية
- تطبيق هجين (WebView) بيغلّف تجربة حديقتي على الويب
- غلاف Kotlin أصلي
- بناء APK تلقائي عبر GitHub Actions

### شو بدك
- صلاحيات أندرويد: كاميرا، موقع (دقيق وتقريبي)، إشعارات (أندرويد 13+)
- FileProvider لصور الكاميرا

### البناء
**GitHub Actions (موصى فيه):** ادفع (push) للريبو، والـ workflow بينشر الـ APK مباشرة على [صفحة الـ Releases](https://github.com/tareknahas85-star/hadiqati-android/releases/tag/latest).

**محلياً:**
```bash
gradle assembleDebug
```
المخرج: `app/build/outputs/apk/debug/app-debug.apk`

### كيف بيشتغل
التطبيق بيغلّف تطبيق حديقتي (ويب) جوا WebView أندرويد، وبيضيف تصوير كاميرا أصلي مع تصغير الصورة قبل التمييز، موقع الجهاز، وإشعارات لتذكيرات العناية. تسجيل الدخول بـ Google بيصير من جوا الـ WebView مع اعتراض الـ redirect.

---

## In English

Android app for rooftop garden care: snap a photo of a plant to identify it, get care tips, and keep track of your rooftop garden. Arabic-first, with full RTL support.

### Features
- Plant identification from a photo (camera capture, auto-resized before upload)
- Location-aware care tips
- Care reminders via notifications
- Google sign-in
- Arabic-first UI with full RTL support

### Tech stack
- Android WebView-based hybrid app wrapping the Hadiqati web experience
- Kotlin native shell
- Automated APK builds via GitHub Actions

### Requirements
- Android permissions: Camera, Location (fine and coarse), Notifications (Android 13+)
- FileProvider for camera photo output

### Build
**GitHub Actions (recommended):** push to the repo and the workflow publishes the APK straight to the [Releases page](https://github.com/tareknahas85-star/hadiqati-android/releases/tag/latest).

**Locally:**
```bash
gradle assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### How it works
The app wraps the Hadiqati web app in an Android WebView, adding native camera capture with image resizing before plant identification, device location, and push notifications for care reminders. Google sign-in is handled through the WebView with redirect interception.
