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

تطبيق أندرويد لحدائق السطوح. صوّر النبتة والتطبيق بيقلك شو هي، وبيعطيك نصايح للعناية فيها، وبيذكّرك متى تسقيها. التطبيق بالعربي أول شي، وبيشتغل من اليمين لليسار.

### شو بيعمل

- بيقلك اسم النبتة من صورة بتلتقطها
- بيعطيك نصايح عناية حسب المكان اللي ساكن فيه
- بيبعتلك تذكيرات منشان تعتني بنباتاتك
- تسجيل دخول بحساب Google
- شاشات عربية، من اليمين لليسار

### شو بتحتاج

صلاحيات عالتلفون: الكاميرا، والموقع، والإشعارات (عأندرويد 13 أو أحدث).

### كيف بتبنيه

**عبر GitHub Actions (الأسهل):** ارفع تعديلاتك عالريبو، وملف التطبيق بيننشر بـ[صفحة الإصدارات](https://github.com/tareknahas85-star/hadiqati-android/releases/tag/latest).

**عجهازك:**

```bash
gradle assembleDebug
```

ملف التطبيق بيطلع بـ `app/build/outputs/apk/debug/app-debug.apk`.

### كيف بيشتغل

التطبيق عبارة عن غلاف أندرويد صغير مكتوب بـ Kotlin. وجواتو بيفتح تطبيق حديقتي عالويب. الغلاف بيضيف الشغلات اللي الويب ما بيقدر يعملها لحالو: التقاط الصورة بالكاميرا، وتصغير الصورة قبل ما تنبعت، وقراءة موقعك، وعرض التذكيرات.
