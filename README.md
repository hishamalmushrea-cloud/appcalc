# دفتر الحسابات — نسخة معاد بناؤها (Android Studio)

إعادة بناء **نظيفة وقابلة للبناء** لتطبيق «دفتر الحسابات» (Package: `com.valdio.valdioveliu.recyclerview`)
استنادًا إلى التحليل الهندسي العكسي الكامل الموجود في [`ANALYSIS.md`](ANALYSIS.md).

الهدف: لأن الكود الأصلي كان **معتَّمًا (R8/DexGuard)** ولا يمكن تعديله وبناؤه مباشرة،
يعيد هذا المشروع بناء الوظيفة المحاسبية الأساسية بكود نظيف ومنظّم يمكنك تطويره بحرية.

---

## 1) كيف تفتح المشروع وتبنيه

1. افتح **Android Studio** ثم `File → Open` واختر **مجلد المشروع** (الذي يحتوي `settings.gradle`).
2. انتظر مزامنة Gradle (سيُنزّل Android Studio التبعيات تلقائيًا من `google()` و `mavenCentral()`).
3. اضغط **Run** على جهاز/محاكي.

> ملاحظة: ملف `gradle/wrapper/gradle-wrapper.jar` غير مرفق (ثنائي). إن طلب منك Android Studio
> تحديد Gradle فاختر استخدام Gradle المدمج فيه، أو شغّل `gradle wrapper` مرة واحدة لتوليد الـ wrapper.

**متطلبات البيئة:** JDK 17+ (مدمج في Android Studio)، Android SDK 34.
- `minSdk = 21` (Android 5.0) — `targetSdk = 34`
- AGP `8.2.2` — Gradle `8.2`

---

## 2) ما تم تنفيذه

| الميزة | الحالة |
|---|---|
| قاعدة بيانات SQLite مطابقة للأصل (`market.db`: customers, transactions, groups, currency, cus_type, cus_limit, reminders...) | ✅ |
| إدارة الحسابات (إضافة/تعديل/حذف) مع تصنيف ونوع (عميل/مورد) | ✅ |
| عمليات (له / عليه) بعملات متعددة | ✅ |
| التحويل بين حسابين (`t_cus_id`) | ✅ |
| كشف حساب مع إجماليات (له / عليه / الرصيد) | ✅ |
| إغلاق الرصيد (فتح رصيد افتتاحي) | ✅ |
| تقارير: أرصدة العملاء، المتأخرون، حسب الشهر | ✅ |
| بحث بالاسم/الهاتف/التفاصيل/التاريخ | ✅ |
| نسخ احتياطي واسترجاع CSV | ✅ |
| قفل بكلمة سر (PIN) | ✅ |
| لغة عربي/إنجليزي مع RTL | ✅ |
| آلة حاسبة | ✅ |
| تنبيهات (تخزين/عرض/حذف) | ✅ |

## 3) لم يُنفَّذ بعد (نقاط امتداد جاهزة)

البنية جاهزة لإضافتها لاحقًا — انظر القسم 13 في `ANALYSIS.md` لخارطة الطريق الكاملة:

- الطباعة الحرارية (بلوتوث ESC/POS / Zebra)
- تصدير PDF (iText) و Excel (POI)
- المزامنة الأونلاين (WebSocket / Firebase Firestore)
- النسخ الاحتياطي على Google/Huawei Drive
- الإشعارات الفورية (FCM) + إشعارات التنبيهات (AlarmManager)
- فتح التطبيق بالبصمة (BiometricPrompt)
- نظام التفعيل IMEI / الإعلانات (AdMob)

---

## 3) بنية الكود

```
app/src/main/
├── AndroidManifest.xml
├── java/com/valdio/valdioveliu/recyclerview/
│   ├── DB.java              # SQLiteOpenHelper (المخطط + البيانات الأولية)
│   ├── Dao.java             # كل الاستعلامات والمنطق المحاسبي
│   ├── CsvBackup.java       # نسخ احتياطي/استرجاع CSV
│   ├── models: Customer, Transaction, ReportRow, Reminder, Group, Currency, CustomerType
│   ├── adapters: CustomerAdapter, TransactionAdapter, ReportAdapter, ReminderAdapter
│   ├── activities: Splash, Login, Main, CustomerEdit, Account,
│   │               TransactionEdit, Reports, Search, Settings, Calculator, Reminders
│   └── util: Prefs, Fmt, Lang
└── res/
    ├── values/       # النصوص العربية (الافتراضية) + الألوان + الأنماط
    ├── values-en/    # الترجمة الإنجليزية
    ├── layout/       # 15 تخطيطًا
    └── menu/         # menu_main + menu_account
```

### القاعدة الذهبية المحاسبية (مطابقة للأصل)
```sql
-- الرصيد الموقّع لأي عملية:
CASE WHEN t.t_cus_id = العميل.ID THEN -1 * CAST(t.out AS REAL)
     ELSE (CAST(t.in AS INTEGER) * CAST(t.out AS REAL)) END
```
حيث `in = +1` (له / دائن) أو `-1` (عليه / مدين)، و `out` هو المبلغ،
و `t_cus_id` يمثّل الطرف الآخر في التحويلات بين الحسابات.

---

## 4) التوافق مع النسخ الاحتياطية القديمة

أسماء الجداول والأعمدة مطابقة للأصل، لذا يمكن استيراد ملف `market.db` القديم
(أو ملف CSV المُصدَّر) دون تغيير في البنية.

## 5) مخرجات الفكّ (مرجع)

المجلد `extracted/` يحتوي مخرجات التفكيك الكاملة (غير مطلوبة للبناء):
- `extracted/AndroidManifest_decoded.xml` — المانيفست الأصلي مفكوكًا
- `extracted/res_decoded/` — 580 ملف موارد XML بأسماء مراجع حقيقية
- `extracted/res/raw/sql*.xml` — مخطط وترقيات قاعدة البيانات الأصلية
- `extracted/decompiled_app_java/` و `decompiled_root_java/` — كود شبه-Java للقراءة
- `extracted/strings_decoded.xml` — كل نصوص الواجهة العربية
