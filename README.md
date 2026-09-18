# Hadiqati (حديقتي)

**[⬇️ Download latest APK](https://github.com/tareknahas85-star/hadiqati-android/releases/download/latest/hadiqati.apk)** &nbsp;|&nbsp; **[⬇️ حمّل آخر نسخة APK](https://github.com/tareknahas85-star/hadiqati-android/releases/download/latest/hadiqati.apk)**

---

## In English

An Android app for rooftop gardens. Take a photo of a plant and the app tells you what it is, gives you care tips, and reminds you when to water it. The app is in Arabic first, and works right to left.

### What it does

- Tells you the name of a plant from a photo you take
- Gives care tips based on where you live
- Sends you reminders to take care of your plants
- Sign in with your Google account
- Arabic screens, right to left

### What you need

Permissions on the phone: camera, location, and notifications (on Android 13 or newer).

### How to build it

**With GitHub Actions (easier):** push your changes to the repo, and the app file is published on the [releases page](https://github.com/tareknahas85-star/hadiqati-android/releases/tag/latest).

**On your computer:**

```bash
gradle assembleDebug
```

The app file comes out at `app/build/outputs/apk/debug/app-debug.apk`.

### How it works

The app is a small Android shell written in Kotlin. Inside it, it opens the Hadiqati web app. The shell adds the parts the web cannot do alone: taking the photo with the camera, making the photo smaller before sending it, reading your location, and showing reminders.

---

## بالعربي

تطبيق أندرويد لحدائق السطوح. تصوّر النبتة والتطبيق يخبرك ما هي، ويعطيك نصائح للعناية بها، ويذكّرك بموعد سقايتها. التطبيق بالعربي أولاً، ويعمل من اليمين إلى اليسار.

### ماذا يفعل

- يخبرك باسم النبتة من صورة تلتقطها
- يعطيك نصائح عناية حسب المكان الذي تسكن فيه
- يرسل لك تذكيرات للعناية بنباتاتك
- تسجيل الدخول بحساب Google
- شاشات عربية، من اليمين إلى اليسار

### ما الذي يحتاجه

صلاحيات على الهاتف: الكاميرا، والموقع، والإشعارات (على أندرويد 13 أو أحدث).

### كيف تبنيه

**عبر GitHub Actions (الأسهل):** ارفع تعديلاتك إلى الريبو، وسيُنشر ملف التطبيق في [صفحة الإصدارات](https://github.com/tareknahas85-star/hadiqati-android/releases/tag/latest).

**على جهازك:**

```bash
gradle assembleDebug
```

يخرج ملف التطبيق في `app/build/outputs/apk/debug/app-debug.apk`.

### كيف يعمل

التطبيق عبارة عن غلاف أندرويد صغير مكتوب بـ Kotlin. وبداخله يفتح تطبيق حديقتي على الويب. الغلاف يضيف الأشياء التي لا يستطيع الويب فعلها وحده: التقاط الصورة بالكاميرا، وتصغير الصورة قبل إرسالها، وقراءة موقعك، وعرض التذكيرات.
