# تحليل معمّق لتطبيق «دفتر الحسابات»

> تحليل هندسي عكسي كامل لملف APK المُقدَّم، يغطي: الهوية، البنية المعمارية، قاعدة البيانات، الميزات، نظام التقارير، المزامنة، الطباعة، الأمان، والمكتبات — بهدف فهم التطبيق ثم تطويره.

---

## 1) الملخص التنفيذي

التطبيق اسمه التجاري **«دفتر الحسابات»** (App Name)، وهو تطبيق **محاسبي/دفتر ذمم (حسابات عملاء وموردين)** باللغة العربية. بُني أساسًا فوق مشروع **Valdio Veliu (RecyclerView Demo)** مفتوح المصدر ثم تحوّل بالكامل إلى تطبيق دفتر حسابات متكامل.

- **Package:** `com.valdio.valdioveliu.recyclerview`
- **الإصدار:** `1.235` (versionCode = 195)
- **الحد الأدنى:** Android 4.0 (API 14) — **الهدف:** Android 12 (API 31)
- **النشاط الرئيسي:** `FragmentStatePagerSupport_Main`

أهم خلاصة هندسية: التطبيق **معتَّم بشدة (R8/ProGuard/DexGuard)**. النشاطات والخدمات المُشار إليها في الـ Manifest احتفظت بأسمائها الأصلية (`com.valdio...`)، بينما كل كود المنطق الداخلي (قاعدة البيانات، التقارير، الطباعة، المزامنة) تم تقليصه إلى أسماء أحرف مفردة في الحزمة الافتراضية (`Aaa`, `Daa`, `Fe`, `cZ`...).

---

## 2) الهوية والبيانات الأساسية (من AndroidManifest.xml)

| العنصر | القيمة |
|---|---|
| التطبيق (Application class) | `com.valdio.valdioveliu.recyclerview.lang.App2` (MultiDexApplication) |
| Backup Agent | `MyBackupAgent` |
| التفعيل متعدد الـ DEX | `androidx.multidex` |
| دعم RTL | `supportsRtl = false` (يُدار يدويًا) |
| التخزين | `requestLegacyExternalStorage=true` + `preserveLegacyExternalStorage=true` |
| Network Security | `res/xml/network_security_config.xml` |
| المكتبة | `org.apache.http.legacy` (اختيارية) |
| AdMob App ID | `ca-app-pub-4681893199239723~6581051891` |
| Google Drive API Key | موجود في meta-data (`com.google.android.backup.api_key`) |

### الأذونات المطلوبة (مجموعة كبيرة تكشف الميزات)
`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `READ/WRITE_EXTERNAL_STORAGE`, `READ_PHONE_STATE`, `WAKE_LOCK`, `SET_ALARM`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `GET_ACCOUNTS`, `READ_PROFILE`, `READ/WRITE_CONTACTS`, `USE_FINGERPRINT`, `BLUETOOTH` + `BLUETOOTH_ADMIN` + `BLUETOOTH_CONNECT/SCAN/ADVERTISE`, و أذونات FCM (`com.google.android.c2dm.permission.RECEIVE`).

### مكوّنات الـ Manifest (خريطة التطبيق)

**النشاطات (Activities):**
- `FragmentStatePagerSupport_Main` — الشاشة الرئيسية (ViewPager أفقي بين الحسابات + Navigation Drawer)
- `LoginActivity` — تسجيل الدخول (كلمة سر + بصمة)
- `SettingsActivity` / `UserSettingActivity` — الإعدادات
- `SearchResultsActivity` — نتائج البحث (Searchable)
- `CustomerList` / `CustomerList2` / `CustomerList_tmp` / `CustomerList_edit` — قائمة العملاء
- `Customer_Det_List` / `_All` / `_edit` / `_edit2` — كشف حساب العميل (تفاصيل)
- `CusLimit_edit` — تعديل سقف (حد) الحساب
- `Report1_customer_bal`, `Report2_customer_late(_exp)`, `Report3_customer_date`, `Report3_balance_month(_exp)`, `Account_Balance_Report` — التقارير
- `frag.GroupList_edit` / `frag.CurrList_edit` — إدارة التصنيفات / العملات
- `frag.Calculator` — آلة حاسبة
- `frag.Reminders` — التنبيهات
- `frag.Share_IP` — مشاركة البيانات عبر الشبكة المحلية (خادم ويب على المنفذ 8888)
- `fcm.nodeJS` — المزامنة الأونلاين (WebSocket مع خادم Node.js)
- `printBT.MainActivity` / `MainActivity2` / `DeviceListActivity` — الطباعة عبر البلوتوث
- `Google_drive_list` — نسخ احتياطي على Google Drive
- `help` — المساعدة، `pdf_view` — عارض PDF

**الخدمات (Services):**
- `fcm.MyGcmPushReceiver`, `fcm.GcmIntentService`, `fcm.MyInstanceIDListenerService` — إشعارات Firebase
- `online.DbBackupListener_Service` — نسخ احتياطي تلقائي (JobService)
- `online.ReminderListener_Service` — خدمة التنبيهات (JobService)
- `threads.SocketService` / `SocketService_Job` — مزامنة WebSocket
- `threads.HelloService` / `HelloService_Job` — خدمة "نبض/إبقاء الاتصال"
- `threads.RSSPullService` — جلب تحديثات من الخادم

**المستقبِلات (Receivers):**
- `online.PhoneStatListener` — مراقبة حالة الشبكة/الهاتف
- `online.DbBackupListener`, `online.ReminderListener` — مشغّلات خلفية (process `:remote`)
- `online.DeviceBootReceiver` — عند إقلاع الجهاز
- `frag.TutWidgetProvider` — ودجت (Widget) للشاشة الرئيسية

---

## 3) البنية المعمارية والتقنيات

### نمط الطبقات
التطبيق **ليس** مبنيًا على MVVM/MVP حديث. البنية بسيطة وقديمة نسبيًا:
- **أنشطة Android** (AppCompatActivity) + أجزاء داخل ViewPager.
- **فئة "إلهية" واحدة `Aaa`** (ملف decompiled بحجم ~600KB) تحتوي على كل منطق الأعمال: استعلامات SQL، حساب الأرصدة، توليد PDF، مشاركة، طباعة، تنسيق قوائم... إلخ.
- **`Daa`** — فئة مساعدة للنسخ الاحتياطي/الاسترجاع (نسخ ملف `market.db` عبر `FileChannel`).
- **`Qaa`** — خادم ويب مضمّن (ServerSocket) لخدمة الصفحات من `assets`.
- **`Ek`/`lk`/`zda$a`** — طبقة شبكة HTTP/WebSocket (مصغّرة).
- **`SP`/`LP`/`MQ`/`sL`/`l`** — محوّلات (Adapters) ونماذج بيانات داخلية.

### قاعدة البيانات
- المحرك: **SQLite** بدون ORM — استعلامات SQL خام مكتوبة داخل `Aaa`.
- اسم ملف القاعدة: **`market.db`** (مُثبت في `MyBackupAgent`: `getDatabasePath("market.db")`).
- ترقيات المخطط تتم بقراءة `res/raw/sql*.xml` وتنفيذ كل وسم `<statement>` بالترتيب.

### التعتيم (Obfuscation)
- الأسماء المرئية في الـ Manifest بقيت كما هي.
- كل الفئات المساعدة أصبحت أسماءً مثل `Aaa`, `Daa`, `Fe`, `Ep`, `Dp`, `Ca`, `cZ` في الحزمة الجذرية (default package).
- المتغيرات المحلية في الـ decompilation أسماء مولّدة (`p0`, `v0_1`...)، لذا القراءة تتطلب تتبّعًا.

### المكتبات (من فحص محتويات الـ DEX)
| المكتبة | الغرض |
|---|---|
| AndroidX (appcompat, recyclerview, viewpager, cardview, preference, multidex, transition) | واجهة المستخدم |
| Google Material Components | التصميم |
| Google Play Services (auth, drive, ads, gcm, measurement) | تسجيل الدخول، Drive، إعلانات، تحليلات |
| Firebase (messaging + iid) | الإشعارات الفورية |
| Google API Client + Drive v3 | نسخ احتياطي على Drive |
| **iText PDF** (`com.itextpdf`) | توليد ملفات PDF |
| **Apache POI** (`org.apache.poi`) | تصدير Excel |
| **Zebra SDK** (`com.zebra.sdk`) | طباعة الطابعات الحرارية Zebra |
| **OkHttp3** + Okio | HTTP/WebSocket |
| **Jackson** (`com.fasterxml.jackson`) | JSON |
| **Guava** (`com.google.common`) | أدوات مساعدة |

---

## 4) تدفّق التطبيق

1. **Splash** — شاشة بداية 100ms ثم الانتقال.
2. **LoginActivity** — يتحقق من كلمة السر المخزنة في `Aaa.l` (ثابت ثابت). عند النجاح يضع `Aaa.k="1"`. يدعم **البصمة** عبر `FingerprintManager` + `AndroidKeyStore` (مفتاح AES باسم `inv_key`).
3. **FragmentStatePagerSupport_Main** — الشاشة الرئيسية:
   - **ViewPager** أفقي يعرض الحسابات (صفحة لكل مجموعة/عميل) عبر المحوّل `cZ` (FragmentStatePagerAdapter).
   - **Navigation Drawer** للتنقل.
   - يطلب الأذونات: `READ_PHONE_STATE`, `READ_CONTACTS`, `WRITE_EXTERNAL_STORAGE`, `GET_ACCOUNTS` (+ `WRITE_CONTACTS`).
   - عند أول تشغيل يُنفّذ ترقية قاعدة البيانات من `res/raw/sql.xml`.
4. **Customer_Det_List** — كشف حساب العميل: قائمة العمليات (وارد/صادر) مع الأرصدة والتحويلات.

---

## 5) قاعدة البيانات (المخطط الكامل)

من ملفات `res/raw/sql.xml`, `sql1.xml`, `sql2.xml`, `sql3.xml` والاستعلامات داخل `Aaa`:

### الجداول الأساسية

**`customers`** (العملاء/الزبائن)
```sql
ID INTEGER PRIMARY KEY AUTOINCREMENT,
name TEXT UNIQUE,      -- اسم العميل
gsm TEXT,              -- رقم الهاتف
g_id INTEGER DEFAULT 0,-- معرّف التصنيف (groups)
param1 TEXT, param2 TEXT,   -- حقول مرنة
f1 TEXT, f2 TEXT, f3 TEXT,  -- حقول إضافية
cus_type_id INTEGER DEFAULT 0 -- 0=عميل، 1=مورد
```

**`transactions`** (العمليات/الحركات) — قلب التطبيق
```sql
ID INTEGER PRIMARY KEY AUTOINCREMENT,
cus_id INTEGER,        -- صاحب العملية (الدائن أو المدين الأساسي)
in  TEXT,              -- الاتجاه: +1 = "له/لصالحه" (دائن)، -1 = "عليه" (مدين)
out TEXT,              -- المبلغ (القيمة)
date_ TEXT,            -- التاريخ بصيغة dd-mm-yyyy
remarks TEXT,          -- الملاحظات/التفاصيل
now_ TEXT,             -- تاريخ الإدخال الفعلي
param1 TEXT, param2 TEXT,
t_cus_id INTEGER,      -- الطرف المقابل عند التحويل بين حسابين
f1 TEXT, f2 TEXT, f3 TEXT,
curr_id INTEGER DEFAULT 0,  -- العملة
online INTEGER, online_ref TEXT, action_type, acc_type, f_br_id  -- حقول المزامنة
```

> **دلالة الحساب:** الرصيد = `SUM(case when t_cus_id = b.id then -out else (in * out) end)`.
> أي أن `in` مخزّن كـ +1/−1 ويُضرب في المبلغ `out` لإنتاج القيمة الموقّعة، والتحويل (`t_cus_id`) يُعامل كإشارة سالبة على الطرف المستلم.

**`groups`** (التصنيفات): `ID, name UNIQUE, param1, param2`
- قيم افتراضية: `0=عام`, `1=عملاء`, `2=موردين` (في sql.xml) و `0=عام, 1=دولار, 2=سعودي` (في sql1.xml — إعادة استخدام للجدول في سياق آخر).

**`currency`** (العملات): `ID, name UNIQUE, param1, param2`
- افتراضية: `0=محلي`, `1=دولار`, `2=سعودي`.

**`cus_type`** (نوع الطرف): `id, name UNIQUE, param1, param2` — `0=عميل`, `1=مورد`.

**`cus_limit`** (سقف/حد الحساب): `id, cus_id, curr_id, cr (سقف له), db (سقف عليه)`.

**`reminders`** (التنبيهات): `id, cus_id, date_, time_, flag, online, ...`.

**`valid`** (تفعيل الرخصة): `imei, imei_code`.

**`act_req`** (طلبات التفعيل): `imei, name, phone, country, email, status, date_, reply, user_reply, reply_from`.

**`requests`** (طلبات المزامنة): `id, status, err, online...` (الـ status: `-2` معلّق، `1` منفّذ، `0`...).

**`contacts`**: `id, name, type_, sort_id` (جهات اتصال مرتبة).

**`sys_conf`**: `id, value_` (إعدادات نظام — مثل `id=7` اسم المستخدم).

**جداول مؤقتة/احتياطية:** `t1`, `t2`, `transactions_bk4`.

**جداول staging للمزامنة:** `doc_hdr` (رؤوس المستندات الواردة)، `requests_items` (الأصناف/الأسماء الواردة).

**View:** `cus_tr_curr_view` — دمج العملاء + العمليات + العملات + التصنيفات + الأنواع.

### فهارس
`transactions(cus_id)`, `transactions(t_cus_id)`, `transactions(curr_id)`, `customers(g_id)`.

---

## 6) الميزات بالتفصيل

### أ) دفتر الحسابات (الوظيفة الأساسية)
- إضافة عملاء/موردين بمجموعات وعملات.
- إضافة مبلغ **له** (دائن / لصالحه) أو **عليه** (مدين / عليه).
- **كشف حساب** لكل عميل مع: رصيد سابق، عمليات الفترة، رصيد نهائي.
- **تحويل بين حسابين** (`t_cus_id`) — يظهر كـ"من حساب X" / "إلى حساب Y".
- **إغلاق رصيد** (فتح رصيد افتتاحي يحذف التفاصيل ويحتفظ بالرصيد).
- **الإغلاق السنوي للحسابات** (yearly closing).
- **سقف الحساب** (حد ائتمان/مدين) لكل عميل وعملة.
- **نقل عملية** إلى حساب آخر، **تغيير التصنيف**، **تغيير العملة**.

### ب) التقارير (خمسة تقارير + تقرير رصيد)
- **Report1_customer_bal**: أرصدة العملاء (مجموع له/عليه لكل عميل).
- **Report2_customer_late**: العملاء **المتأخرون** — يحسب `days_late` عبر `julianday('now') - julianday(max(date))`.
- **Report3_customer_date**: حركات عميل خلال **فترة تاريخ** (من/إلى).
- **Report3_balance_month**: أرصدة **شهرية** (مجمعة حسب `substr(date_,7,4)||'-'||substr(date_,4,2)`).
- **Report3_balance_month_exp** + `Report2_..._exp`: نسخ موسّعة (حسب العملة/المجموعة).
- **Account_Balance_Report**: تقرير أرصدة حسب المجموعة مع `cr_amount`/`db_amount` لكل تصنيف.

### ج) البحث والترتيب
- بحث سريع (SearchView) + **بحث متقدم**.
- فرز حسب: **المبلغ**، **نوع العملية**، **التاريخ** (تصاعدي/تنازلي بالنقر على رأس العمود).
- بحث كـ"فلتر": إدخال `2016-02` يعرض حركات فبراير فقط (موضح في ملفات المساعدة).

### د) التصدير والمشاركة
- **PDF** عبر iText (كشوف، تقارير، فواتير).
- **Excel** عبر Apache POI.
- **CSV**.
- مشاركة عبر **SMS** (نص "رصيدك الحالي هو:...") و **WhatsApp** و البريد/المشاركة العامة.

### هـ) الطباعة الحرارية
- طباعة عبر **بلوتوث مباشر** (BluetoothSocket + أوامر ESC/POS مثل `! UTILITIES\r\nIN-MILLIMETERS\r\nCENTER\r\nSETFF 10 2\r\nPRINT`).
- دعم **طابعات Zebra** (مكتبة Zebra SDK مضمّنة).
- إعدادات: نوع الطابعة، حجم الورق، رقم صفحة اللغة العربية (Code Page)، ملاحظة أسفل الورقة، طباعة رصيد الفترة فقط.

### و) النسخ الاحتياطي والاسترجاع
- نسخ ملف `market.db` إلى مجلد (`prefBackup_path`).
- **Google Drive** (Google API Client + OAuth scope `drive`) و **Huawei Drive**.
- **نسخ تلقائي** (JobService + BroadcastReceiver).
- استرجاع من ملف `.db`.

### ز) المزامنة الأونلاين (بالتفصيل في القسم 8)
- مزامنة متعددة المستخدمين: إرسال/استقبال **عمليات** و**أسماء** من/إلى خادم.
- خادم Node.js عبر **WebSocket** (okhttp3) + سحب دوري.
- مشاركة البيانات عبر **الشبكة المحلية** (خادم ويب على المنفذ 8888 يعرض HTML من assets).

### ح) التنبيهات (Reminders)
- تنبيهات لكل عميل بتاريخ ووقت، مع إشعار، وخدمة JobService تعمل في الخلفية.

### ط) آلة حاسبة + واجهات إضافية
- `frag.Calculator` — آلة حاسبة بسيطة.
- `pdf_view` — عارض ملفات PDF المولّدة.
- `help` — صفحات مساعدة (HTML في assets).

---

## 7) أهم الاستعلامات (فهم المنطق المحاسبي)

### رصيد عميل (كشف حساب)
```sql
SELECT b.name AS _id,
       abs(a.out * a.[in]) AS amount,
       (CASE WHEN (a.out * a.[in]) >= 0 THEN 2 ELSE 0 END) AS _in,
       a.remarks, a.date_,
       strftime('%Y-%m-%d', substr(a.date_,7,4)||'-'||substr(a.date_,4,2)||'-'||substr(a.date_,1,2)) d
FROM transactions AS a, customers AS b
WHERE a.cus_id = b.id AND a.date_ = ?
ORDER BY date(d) DESC
```

### الرصيد الصافي (يُستخدم في كل مكان)
```sql
ifnull((sum(case when (c.t_cus_id = b.id) then -1*c.out else (c.[in]*c.out) end)), 0)
```

### أرصدة المجموعات (تقرير الرصيد)
```sql
SELECT a.id AS id,
       ifnull((sum(case when (c.t_cus_id=b.id) then -1*c.out else (c.[in]*c.out) end))*1, 0) AS net_balance,
       sum(case when c.[in]=1 and (c.t_cus_id<>b.id or c.t_cus_id is null) then c.out else 0 end)*1.0 AS cr_amount,
       sum(case when not(c.[in]=1 and (c.t_cus_id<>b.id or c.t_cus_id is null)) then c.out else 0 end)*1.0 AS db_amount,
       a.name AS _id
FROM groups a LEFT JOIN customers b ON a.id=b.g_id
              LEFT JOIN transactions c ON (c.cus_id=b.id OR c.t_cus_id=b.id)
GROUP BY a.name, a.id
```

### العملاء المتأخرون
```sql
SELECT b.name AS _id, ifnull(abs(SUM(a.out*a.[in])),0) AS amount,
       ..., b.gsm AS phone,
       max(strftime('%Y-%m-%d', substr(a.date_,7,4)||'-'||substr(a.date_,4,2)||'-'||substr(a.date_,1,2))) d,
       (SELECT julianday(strftime('%Y-%m-%d','now')) - julianday(max(strftime(...)))
        FROM transactions c WHERE c.cus_id=b.id) AS days_late
FROM customers b LEFT JOIN transactions a ON a.cus_id=b.id
GROUP BY b.name, b.id
HAVING ifnull(abs(SUM(a.out*a.[in])),0) <> 0
```

> ملاحظة: التواريخ مخزنة نصيًا بصيغة `dd-mm-yyyy`، لذا كل استعلام يحوّلها عبر `substr(date_,7,4)||'-'||substr(date_,4,2)||'-'||substr(date_,1,2)`.

---

## 8) المزامنة والبنية التحتية الأونلاين

### الخوادم المكتشفة (من سلاسل الكود)
| الرابط | الغرض |
|---|---|
| `http://easycard.work/abd/v1/imei/_ID_/check` | التحقق من تفعيل IMEI |
| `http://easycard.work/abd/v1/imei/request` | إرسال طلب تفعيل |
| `http://easycard.work/abd/v1/black_list/_ID_/check` | فحص القائمة السوداء |
| `http://easycard.work/abd/v1/support/_ID_` | الدعم |
| `http://goin.dyndns.org:4000` | خادم مزامنة WebSocket (Dynamic DNS) |

### آلية المزامنة
- الاتصال عبر **WebSocket** (فئة `wda` = واجهة WebSocket من okhttp المُصغّر) مع معالجات أحداث `zda$a`.
- **`SocketService`** تدير الاتصال الدائم + **`SocketService_Job`** (JobScheduler) للخلفية + **`HelloService`** نبض + **`RSSPullService`** سحب.
- عند وصول بيانات، تُستورد عبر سكربتات SQL جاهزة:
  - `requests_doc.txt` → يستورد **الحركات/الفواتير** من جدول staging `doc_hdr` إلى `transactions` (مع `online=2` و `online_ref=p_id`).
  - `requests_items.txt` → يستورد **الأسماء/الأصناف** من `requests_items` إلى `customers` و `groups`.
- جدول `requests` يتتبع حالة كل طلب مزامنة (`status=-2` قيد التنفيذ، `1` مكتمل).

### المشاركة عبر الشبكة المحلية (Share_IP)
- فئة `Qaa` = **خادم ويب بسيط** (ServerSocket) على المنفذ **8888** يخدم صفحات HTML من `assets` (template.html, table_hdr.html).
- يعرض بياناتك لأي متصفح على نفس الشبكة (العنوان يظهر للمستخدم).
- مثال عنوان في الموارد: `http://192.168.100.2:7777/index.html`.

---

## 9) الأمان والترخيص

1. **تفعيل IMEI**: التطبيق يقرأ IMEI (`Aaa.g`) ويطلب كود تفعيل (`Aaa.h`). عند إدخال الكود:
   - يتحقق محليًا بالمقارنة `substring(0,14)`، أو
   - عبر الخادم (`/imei/_ID_/check` → JSON `imei_cnt`).
   - عند النجاح: `INSERT INTO valid(imei, imei_code) VALUES(...)`.
2. **طلبات التفعيل**: نموذج (اسم، هاتف، دولة، بريد) يُرسل للخادم عبر `_Z` (POST) ويُحفظ في `act_req`.
3. **قفل التطبيق**: كلمة سر مخزنة في `Aaa.l` + دعم **بصمة** (AndroidKeyStore، AES/CBC/PKCS7Padding، مفتاح `inv_key`).
4. **جهة اتصال للترخيص**: رقم يظهر في رسائل الخطأ `967770203819`.
5. رسالة منع الاستخدام: `غير مسموح بالإستخدام-يرجي التواصل مع 967770203819`.

---

## 10) الإعلانات (Monetization)
- **AdMob** معرّف التطبيق: `ca-app-pub-4681893199239723~6581051891`.
- وحدات: Banner (`ca-app-pub-4681893199239723/8057785095`) + Interstitial (`.../5962858696`).

---

## 11) الإعدادات (SettingsActivity + pref XMLs)

| Fragment / ملف pref | المحتوى |
|---|---|
| `GeneralPreferenceFragment` / `pref_general` | اللغة (عربي/إنجليزي)، حجم الخط، نوع التسطير |
| `SecurityPreferenceFragment` / `pref_security` | كلمة السر، البصمة |
| `BackupPreferenceFragment` / `pref_backup` | النسخ الاحتياطي، المسار، التوقيت |
| `DataSyncPreferenceFragment` / `pref_data_sync` | المزامنة الأونلاين |
| `IPPreferenceFragment` | مشاركة IP / الشبكة |
| `OthersPreferenceFragment` / `pref_others` | العملات، خيارات أخرى |
| `pref_thermal` | إعدادات الطابعة الحرارية |
| `pref_curr`, `pref_group_curr` | العملات والتصنيفات |
| `pref_activation` | التفعيل |
| `pref_ads` | الإعلانات |
| `pref_notification` | الإشعارات |

---

## 12) ملاحظات مهمة / مخاطر يجب معرفتها قبل التطوير

1. **تعتيم شديد**: لا توجد أسماء دوال ذات معنى في طبقة المنطق. أي تطوير جاد يتطلب إعادة هيكلة أو أداة فك تعتيم.
2. **فئة إلهية `Aaa`**: كل المنطق في فئة واحدة ضخمة — من أهداف إعادة الهيكلة الأساسية.
3. **إعادة تعريف أنشطة WhatsApp** (`com.whatsapp.Conversation`, `com.whatsapp.ContactPicker`): التطبيق يصرّح بنشاطات تحمل اسم حزمة WhatsApp ويستقبل `whatsapp://send` و `SENDTO/sms` — حيلة تكامل (يحتمل أن تتعارض مع سياسات المتجر).
4. **تخزين قديم**: `requestLegacyExternalStorage` + `preserveLegacyExternalStorage` — يعمل لكنه سيُمنع في إصدارات أندرويد الأحدث؛ يجب الانتقال إلى Storage Access Framework / MediaStore.
5. **HTTP غير مشفّر** (`easycard.work`, `goin.dyndns.org:4000`) — بدون TLS.
6. **صلاحيات واسعة** (قراءة حالة الهاتف، قراءة/كتابة جهات الاتصال) — ستحتاج تبريرًا في المتجر ومراجعة خصوصية.
7. **targetSdk 31 مع minSdk 14**: دعم قديم جدًا يُبطّئ التطوير الحديث (بدون Kotlin، بدون Jetpack Compose، بدون Room).
8. **رقم هاتف ثابت** مضمّن في الكود (967770203819).
9. **اسم الحزمة** ما زال `com.valdio.valdioveliu.recyclerview` (إرث من المشروع الأصلي) رغم أن اسم المنتج "دفتر الحسابات" — والرابط في `share_content` يشير إليه، بينما رابط آخر يشير لـ `com.valdio.valdioveliu.daftar` (الاسم الأصلي).

---

## 13) خارطة طريق مقترحة للتطوير

### إعادة الهيكلة (أولوية قصوى)
1. فكّ الاقتران: تقسيم `Aaa` إلى طبقات (`DatabaseHelper`, `Repository`, `ReportEngine`, `PdfExporter`, `PrintManager`...).
2. استخراج SQL إلى ملفات منظمة أو الانتقال إلى **Room**.
3. اعتماد معمارية واضحة (MVVM + LiveData/Flow).

### تحديث تقني
4. الترقية إلى Kotlin + Compose تدريجيًا (الأنشطة الجديدة أولًا).
5. رفع `minSdk` إلى 21+ و `targetSdk` إلى 34+.
6. استبدال التخزين القديم بـ SAF/MediaStore.
7. استخدام HTTPS للخوادم (أو نقلها لخدمة سحابية حديثة مثل Firebase/Cloud Functions).

### تحسينات وظيفية
8. استبدال WebSocket/Node.js القديم بـ **Firebase Firestore/Realtime DB** للمزامنة (أسهل وأأمن).
9. فصل طباعة Zebra والبلوتوث في وحدة قابلة للصيانة.
10. إضافة تصدير/استيراد JSON موحّد بدلًا من الاعتماد على سكربتات SQL الخام.
11. تحسين التقارير برسوم بيانية.
12. دعم RTL صحيح عبر `supportsRtl=true`.

### إصلاحات أمنية
13. إزالة/إصلاح أنشطة `com.whatsapp.*` المغشوشة.
14. تشفير النسخ الاحتياطية.
15. مراجعة الصلاحيات وتقليلها إلى الحد الأدنى.

---

## 14) مصادر التحليل
- `AndroidManifest.xml` (مفكوك).
- `classes.dex` + `classes2.dex` (8438 فئة) — تم تفكيك فئات التطبيق الـ 122 و الفئات المعتمة.
- `resources.arsc` (39160 سلسلة نصية).
- `res/raw/sql*.xml` (مخطط قاعدة البيانات والترقيات).
- `res/raw/requests_doc.txt` + `requests_items.txt` (منطق استيراد المزامنة).
- `res/menu/*`, `res/layout/*`, `res/xml/pref_*.xml`, `assets/*` (واجهة المستخدم والمساعدة).

---

## 15) مخرجات الفكّ ومدى قابليتها للبناء (مهم)

### ما تم إنتاجه في هذا المستودع
| المسار | المحتوى | قابل للبناء؟ |
|---|---|---|
| `extracted/AndroidManifest_decoded.xml` | المانيفست مفكوكًا بأسماء مراجع حقيقية | للقراءة |
| `extracted/res_decoded/` | **580** ملف موارد XML (تخطيطات/قوائم/إعدادات/أنيميشن) بأسماء `@id/` و`@string/` حقيقية | للقراءة/النسخ |
| `extracted/public_decoded.xml` | جدول معرّفات الموارد (public.xml) | مرجع |
| `extracted/strings_decoded.xml` | كل النصوص العربية | للقراءة |
| `extracted/decompiled_app_java/` + `decompiled_root_java/` | كود Java شبه-مفكوك (للقراءة فقط، الأسماء مشوّهة) | **لا** |
| `extracted/res/raw/sql*.xml` | مخطط قاعدة البيانات وترقياته | مرجع |
| `extracted/res/raw/requests_*.txt` | منطق استيراد المزامنة | مرجع |

### لماذا هذا ليس مشروع Android Studio جاهزًا؟
1. الكود **معتَّم (R8/DexGuard)**: أسماء الدوال والمتغيرات فُقدت (`Aaa`, `Daa`, `p0`, `v1_3`...)، فلا يوجد "كود مصدري نظيف" يمكن استرجاعه آليًا.
2. ملفات `decompiled_*` هي **نص شبه-Java للقراءة** من androguard — **ليست** smali حقيقية ولا Java قابلة للترجمة.
3. لبناء نسخة معدّلة تحتاج أدوات إعادة البناء (apktool/jadx) التي تتطلب تنزيلها من الإنترنت (انظر القسم التالي — تُنفَّذ على جهازك).

### المسارات الواقعية للتعديل والبناء (على جهازك بإنترنت عادي)
- **أ) jadx** — الأقرب لفتح الكود في Android Studio:
  `jadx --export-gradle app.apk` أو من الواجهة: File → Save as Gradle project.
  (توقع أسماء مشوّهة وإصلاحات قبل الترجمة).
- **ب) apktool** — الطريقة القياسية والأوثق للتعديل + إعادة البناء:
  `apktool d app.apk -o src` → عدّل smali/resources → `apktool b src -o mod.apk` → وقّع بـ uber-apk-signer.
  ولتحرير smali داخل Android Studio استخدم إضافة **Smalidea**.
- **ج) إعادة بناء نظيفة** — أنظف النتائج على المدى الطويل: مشروع جديد (Gradle + Kotlin/Java) يُستورد فيه `res_decoded/` والمخطط `sql*.xml` وتُعاد كتابة المنطق المحاسبي (القسم 7 يوثّق الاستعلامات بدقة).
