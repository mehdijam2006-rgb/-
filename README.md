# سامانه مدیریت نامه و امور مالی
## Letter Management & Financial System — Android App

---

## معرفی / Overview

اپلیکیشن اندروید حرفه‌ای برای مدیریت نامه‌های اداری رسمی، پیگیری مهلت‌ها، ثبت پاسخ‌ها، مدیریت فیش‌های بانکی، و همگام‌سازی با Google Sheets.

---

## پشته فناوری / Tech Stack

| لایه | فناوری |
|------|--------|
| زبان | Kotlin |
| UI | Jetpack Compose + Material 3 |
| معماری | MVVM + Clean Architecture |
| پایگاه داده | Room |
| تزریق وابستگی | Hilt |
| پس‌زمینه | WorkManager |
| همزمانی | Coroutines + Flow |
| شبکه | Retrofit + OkHttp |
| امنیت | Biometric + DataStore |
| تقویم | Jalali/Shamsi (Custom Implementation) |

---

## ساختار پروژه / Project Structure

```
app/src/main/java/com/lettermanager/
├── data/
│   ├── local/
│   │   ├── dao/           ← Room DAOs
│   │   ├── entity/        ← Room Entities + Mappers
│   │   └── AppDatabase.kt
│   ├── remote/            ← Retrofit API Service
│   └── repository/        ← Repository implementations
├── di/                    ← Hilt modules
├── domain/
│   ├── model/             ← Domain models
│   └── usecase/           ← (قابل گسترش)
├── presentation/
│   ├── components/        ← Shared UI components
│   ├── navigation/        ← NavGraph
│   ├── theme/             ← Material 3 Theme
│   └── ui/
│       ├── auth/          ← PIN + Biometric screens
│       ├── letters/       ← Active, Create, Detail screens
│       ├── archived/      ← Archived letters
│       ├── search/        ← Search & filter
│       └── financial/     ← Financial dashboard
├── service/               ← NumberGenerator, Notifications, Backup
├── util/                  ← PersianCalendarUtils, SecurityPreferences
└── worker/                ← WorkManager Workers
```

---

## نصب و راه‌اندازی / Build Instructions

### پیش‌نیازها:
- Android Studio Hedgehog (2023.1.1) یا بالاتر
- JDK 17
- Android SDK 34
- Gradle 8.4

### مراحل Build:

```bash
# ۱. کلون یا کپی پروژه
# ۲. باز کردن در Android Studio
# ۳. Sync Gradle
# ۴. Build APK

# از طریق خط فرمان:
cd LetterManager
chmod +x gradlew
./gradlew assembleDebug

# APK خروجی:
# app/build/outputs/apk/debug/app-debug.apk
```

### Build APK نهایی (release):
```bash
./gradlew assembleRelease
```

---

## تنظیم Google Sheets Sync

### مرحله ۱: ایجاد Google Sheet
1. یک Google Spreadsheet جدید بسازید
2. Sheet ID را از URL کپی کنید

### مرحله ۲: تنظیم Apps Script
1. در Spreadsheet: Extensions → Apps Script
2. کد فایل `google_apps_script/Code.gs` را کپی کنید
3. `SPREADSHEET_ID` را با ID واقعی جایگزین کنید
4. Deploy → New Deployment → Web App
   - Execute as: Me
   - Who has access: Anyone
5. آدرس Deployment URL را کپی کنید

### مرحله ۳: تنظیم در اپ
1. وارد اپ شوید
2. تنظیمات → همگام‌سازی Google Sheets
3. آدرس Apps Script را وارد کنید

---

## ویژگی‌های اصلی / Features

### 📋 مدیریت نامه
- شماره‌گذاری خودکار فرمت `{سال}/{شماره}` (مثال: ۱۴۰۵/۰۰۱۲)
- ریست خودکار شماره در ابتدای سال جدید
- پشتیبانی از شماره دستی (بدون تأثیر بر شماره‌گذاری خودکار)
- مدیریت وضعیت: فعال / بایگانی

### 📅 تقویم شمسی
- تاریخ‌پیکر کامل شمسی (سفارشی، بدون کتابخانه خارجی)
- تمام تاریخ‌ها به فرمت شمسی نمایش داده می‌شوند
- ذخیره‌سازی داخلی به صورت timestamp میلادی

### 🔔 اعلان‌های هوشمند
- ۳ روز قبل از مهلت
- ۱ روز قبل از مهلت
- روز سررسید
- بازمانده پس از ریست دستگاه (BroadcastReceiver)

### 💰 مدیریت مالی
- ثبت فیش بانکی با جزئیات کامل
- تأیید دریافت وجه
- داشبورد مالی با جمع کل
- گزارش ماهانه به تفکیک ماه شمسی

### 🔍 جستجوی پیشرفته
- جستجو در: شماره اتوماتیک، شماره دستی، نام، فرستنده
- فیلتر بر اساس وضعیت
- فیلتر بازه تاریخی

### 🔐 امنیت
- احراز هویت PIN (حداقل ۴ رقم)
- بیومتریک (اثر انگشت)
- قفل خودکار

### ☁️ همگام‌سازی
- ارسال اتوماتیک به Google Sheets
- صف انتظار برای زمان آفلاین
- تلاش مجدد خودکار

### 💾 پشتیبان‌گیری
- پشتیبان‌گیری روزانه خودکار
- صدور JSON دستی
- نگهداری ۷ نسخه آخر

---

## مجوزها / Permissions

| مجوز | کاربرد |
|------|--------|
| INTERNET | همگام‌سازی |
| POST_NOTIFICATIONS | اعلان‌های مهلت |
| RECEIVE_BOOT_COMPLETED | زمانبندی بعد از ریست |
| USE_BIOMETRIC | اثر انگشت |
| SCHEDULE_EXACT_ALARM | اعلان‌های دقیق |

---

## نکات مهم برای Build

1. در فایل `di/AppModule.kt`، آدرس پیش‌فرض Retrofit را به آدرس Apps Script خود تغییر دهید
2. برای release build، keystore مناسب تنظیم کنید
3. حداقل Android 8.0 (API 26) مورد نیاز است

---

## پشتیبانی RTL

تمام صفحات با `android:supportsRtl="true"` و layout های Compose به صورت RTL نمایش داده می‌شوند.
