# حلقة الفجر 🌙 (FajrLoop)
[![Android CI](https://github.com/salehbagomri/fajrloop/actions/workflows/android-ci.yml/badge.svg)](https://github.com/salehbagomri/fajrloop/actions/workflows/android-ci.yml)

تطبيق أندرويد أصلي (Kotlin + ViewBinding + MVVM) مبتكر يهدف لمساعدة المسلمين على الاستيقاظ لأداء صلاة الفجر في جماعة عبر نظام حلقات تفاعلي دائري مغلق.

---

## 🛠️ هندسة التطبيق والتقنيات المستخدمة:
- **اللغة**: Kotlin
- **العمارة البرمجية**: MVVM (Model-View-ViewModel) بدون مكتبات خارجية.
- **التصميم**: تصميم زجاجي زاهٍ (Glassmorphism) مع ألوان ليلية ذهبية متطورة.
- **قاعدة البيانات**: Firebase Realtime Database
- **المصادقة**: Firebase Auth (إضافة تسجيل الدخول الحديث عبر Google Credential Manager).
- **المنبه الأمني**: جدولة دقيقة للمنبهات بالاعتماد على `AlarmManager` و `WorkManager` fallback لمنع إيقاف التطبيق أو التلاعب بالصوت.
- **الإشعارات**: Firebase Cloud Messaging (FCM) لدعم الرسائل اللحظية والإنذارات المشتركة.
- **الحسابات الفلكية**: مكتبة Adhan الرسمية لحساب مواقيت الفجر بدقة متناهية بناءً على الموقع الجغرافي.
