# قيم التصميم الدقيقة — Design Tokens
## تطبيق حلقة الفجر (FajrLoop)

> **المصدر:** مستخرجة مباشرة من كود Dart دون أي تعديل.  
> **الهدف:** مرجع دقيق لإعادة بناء نفس التصميم بـ Kotlin (Android).

---

## 1. الألوان (`app_colors.dart`)

### لوحة الألوان الأساسية

| الاسم | قيمة Hex | الوصف / موضع الاستخدام |
|---|---|---|
| `primaryNight` | `#1A1A3E` | الأزرق الليلي — لون Primary في ColorScheme، خلفية التدرج العلوي |
| `primaryPurple` | `#4A1A6B` | البنفسجي — لون Secondary في ColorScheme، خلفية التدرج السفلي |
| `accentGold` | `#FFD700` | الذهبي — لون كل الأزرار الرئيسية، العناصر المميزة، الأوسمة، أسهم الحلقة |
| `successGreen` | `#2ECC71` | الأخضر — حالة مستيقظ، نجاح التحدي، حالة نشط |
| `dangerRed` | `#E74C3C` | الأحمر — حالة فاته الفجر، أزرار الطوارئ، رنين المنبه، حالة يرن |
| `background` | `#07071B` | خلفية الشاشات الكاملة (scaffoldBackground) |
| `surface` | `#0D0D2B` | خلفية البطاقات (Cards)، حقول الإدخال، النواة الدائرية للمنبه |
| `border` | `#25254A` | حدود البطاقات، حدود حقول الإدخال، الخطوط الفاصلة (Divider) |
| `textPrimary` | `#FFFFFF` | النصوص الأساسية (onSurface, onPrimary) |
| `textSecondary` | `#B0B0C5` | النصوص الثانوية (وصف، تسميات حقول) |
| `textMuted` | `#6B6B8A` | النصوص الخافتة (hints, captions, تسميات ثانوية صغيرة) |

### ألوان إضافية (في الكود بدون ثوابت)

| السياق | قيمة Hex | الموضع |
|---|---|---|
| shuruqGradient — نهاية | `#E67E22` | تدرج شروق الشمس (بنفسجي إلى برتقالي) |
| successGradient — بداية | `#27AE60` | تدرج النجاح الأخضر الداكن |
| حالة السفر (travel) | `Colors.blue` (system blue) | أعضاء في وضع السفر بدائرة الحلقة |
| Colors.white70 | أبيض 70% opacity | دور العضو "عضو حلقة" |
| Colors.white38 | أبيض 38% opacity | نص اقتباس داخل بطاقة السلسلة |
| Colors.white60 | أبيض 60% opacity | تسميات إحصاء ثانوية |

### التدرجات (Gradients)

| الاسم | الألوان | الاتجاه |
|---|---|---|
| `nightGradient` | `#1A1A3E` ← `#4A1A6B` | topRight → bottomLeft |
| `shuruqGradient` | `#4A1A6B` ← `#E67E22` | topRight → bottomLeft |
| `successGradient` | `#27AE60` ← `#2ECC71` | topRight → bottomLeft |
| `AnimatedGradientBg` (خلفية الشاشات) | `#07071B` → `#1A1A3E` → `#4A1A6B` → `#07071B` | دوّار بانيميشن 15 ثانية (يتحرك بين 4 نقاط زاوية دائرياً) |

### ألوان حالات أعضاء الحلقة

| الحالة | اللون | القيمة |
|---|---|---|
| `awake` (مستيقظ مؤكد) | `successGreen` | `#2ECC71` |
| `challenge_done` (انتظار الصديق) | `accentGold` | `#FFD700` |
| `missed` (فاته الفجر) | `dangerRed` | `#E74C3C` |
| `pending` (يرن) | `dangerRed` — مع نبض | `#E74C3C` |
| `travel` (مسافر) | Blue | system blue |
| `null` (نائم / لا سجل) | `textSecondary` | `#B0B0C5` |

### ألوان عداد الفجر (FajrCountdown) حسب الوقت المتبقي

| الوقت المتبقي | اللون |
|---|---|
| 60 دقيقة أو أكثر | `successGreen` `#2ECC71` |
| بين 15 و 60 دقيقة | `accentGold` `#FFD700` |
| أقل من 15 دقيقة | `dangerRed` `#E74C3C` |

---

## 2. الخطوط والنصوص (Typography)

**عائلة الخط الوحيدة:** `Noto Sans Arabic` — مُحمَّل عبر Google Fonts  
**الاتجاه:** RTL بالكامل

### جدول TextStyles المُعرَّفة

| الاسم | الحجم (sp) | الوزن | line-height | اللون الافتراضي | موضع الاستخدام |
|---|---|---|---|---|---|
| `displayLarge` | 32 | Bold (700) | 1.3 | `#FFFFFF` | عداد الفجر الكبير (مُعاد تخصيصه إلى 42sp) |
| `headingBold` | 24 | Bold (700) | 1.4 | `#FFFFFF` | عناوين الشاشات الرئيسية |
| `titleLarge` | 20 | Bold (700) | — | `#FFFFFF` | عناوين AppBar، عناوين شاشة المنبه |
| `titleMedium` | 16 | SemiBold (600) | — | `#FFFFFF` | أسماء الأعضاء، عناوين البطاقات الثانوية، أوقات الصلاة |
| `bodyRegular` | 14 | Regular (400) | 1.5 | `#B0B0C5` | نصوص وصفية، شرح المنبه، محتوى البطاقات |
| `bodyMedium` | 14 | Medium (500) | — | `#FFFFFF` | نصوص تفاعلية أو اسم المستخدم |
| `caption` | 12 | Regular (400) | — | `#6B6B8A` | تسميات صغيرة، hints، نص كود الدعوة الصغير |
| `quoteStyle` | 16 | Medium (500) | 1.6 | `#FFD700` | اقتباسات دينية (italic) |

### أحجام مخصصة إضافية (غير معرّفة كـ TextStyle)

| الموضع | الحجم | الوزن | اللون |
|---|---|---|---|
| AppBar titleTextStyle | 16 sp | Bold | `#FFFFFF` |
| زر AppButton (نص) | 16 sp | Bold | حسب نوع الزر |
| تسمية حقل الإدخال (label) | 14 sp | SemiBold (600) | `#B0B0C5` |
| نص حقل الإدخال (typed) | 16 sp | Regular | `#FFFFFF` |
| عداد الفجر الرئيسي (override) | 42 sp | Bold | حسب الوقت المتبقي |
| عداد السلسلة (currentStreak) | 36 sp | Bold | `#FFD700` مع glow shadow |
| إحصاء ثانوي (longestStreak, total) | 20 sp | Bold | `#FFFFFF` |
| اسم الذكر في الدائرة | 11 sp | Bold | `#6B6B8A` |
| حالة العضو في الدائرة | 9 sp | Bold | لون الحالة |
| letterSpacing عداد الفجر | 2.0 | — | — |

---

## 3. المسافات والأبعاد (Spacing & Sizing)

> ملاحظة: التطبيق لا يستخدم ثوابت spacing مركزية، بل قيم مباشرة. هذه هي القيم المتكررة:

### Padding القياسية المتكررة

| الحجم | القيمة | مكان الاستخدام |
|---|---|---|
| `padding.all(20)` | 20dp جميع الاتجاهات | GlassCard الافتراضي |
| `padding.all(24)` | 24dp جميع الاتجاهات | بطاقة عداد الفجر، ElevatedButton (horizontal) |
| `padding.all(16)` | 16dp جميع الاتجاهات | MemberCardWidget، بطاقات إحصاء ثانوية |
| `symmetric(h:24, v:14)` | 24dp أفقي، 14dp رأسي | ElevatedButton في الـ Theme |
| `symmetric(h:16, v:16)` | 16dp أفقي، 16dp رأسي | InputDecoration (حقول الإدخال) |
| `symmetric(v:16, h:24)` | 16dp رأسي، 24dp أفقي | AppButton الافتراضي |
| `symmetric(v:24, h:16)` | 24dp رأسي، 16dp أفقي | StreakCard الرئيسية |
| `symmetric(h:6, v:2)` | 6dp أفقي، 2dp رأسي | badge "أنت" الصغير |
| `symmetric(h:8, v:4)` | 8dp أفقي، 4dp رأسي | role badge (مشرف/عضو) |
| `padding.only(bottom:12)` | 12dp أسفل فقط | بين بطاقات الأعضاء |

### SizedBox (Gaps) المتكررة

| القيمة | الاستخدام الشائع |
|---|---|
| 4dp | فجوات صغيرة جداً (بين عناصر نفس السطر) |
| 6dp | بين الأيقونة والنص في صف الحالة، تحت الأفاتار |
| 8dp | فجوة قياسية (بين label والحقل، بين الأزرار المتوازية) |
| 12dp | بين عناصر الصف في AppButton، بين البطاقات |
| 16dp | الفجوة القياسية بين البطاقات، بين الأقسام |
| 20dp | فجوة واسعة بين مجموعات عناصر |
| 24dp | فجوة كبيرة بين الأقسام الرئيسية |

### Border Radius

| العنصر | نصف القطر |
|---|---|
| GlassCard (افتراضي) | 24dp |
| AppButton (primary + secondary) | 16dp |
| ElevatedButton (Theme) | 12dp |
| حقول الإدخال (InputDecoration) | 12dp |
| Card (Theme) | 16dp |
| Badge "أنت" الصغير | 6dp |
| Role badge (مشرف/عضو) | 8dp |
| دائرة زر إزالة العضو | BoxShape.circle |
| دائرة أيقونة الإنجاز (streak card) | BoxShape.circle |

### الحدود (Borders)

| العنصر | سماكة | اللون |
|---|---|---|
| Card (Theme) | 1.5dp | `#25254A` |
| GlassCard (افتراضي) | 1.5dp | `#25254A` مع opacity 30% |
| حقل إدخال — عادي | 1dp | `#25254A` |
| حقل إدخال — محدد (focused) | 1dp | `#FFD700` |
| حقل إدخال — خطأ (error) | 1dp | `#E74C3C` |
| AppButton secondary | 2dp | `border` أو لون مخصص |
| الأفاتار في الدائرة — عادي | 2.5dp | لون الحالة |
| الأفاتار في الدائرة — يرن | 3.5dp | لون الحالة |
| حلقة رادار العضو الرنان | 2dp | `#E74C3C` مع opacity متحرك |
| دائرة المنبه الرئيسية | 3dp | ringColor مع opacity 50% |
| خط تدرج رسم السلسلة | 2dp | `#FFD700` مع opacity 30%-50% |
| Divider عداد الفجر | 1dp | `#25254A` |
| فاصل عمودي (وقت الفجر/شروق) | 1.5dp | `#25254A` — ارتفاع 30dp |

### الظلال (BoxShadow)

| العنصر | اللون | blurRadius | spreadRadius | offset |
|---|---|---|---|---|
| AppButton (glow مفعّل) | لون الزر مع opacity 40% | 15 | 1 | (0, 4) |
| دائرة المنبه — رنين | accentGold مع opacity 30% | 40 | 5 | — |
| دائرة المنبه — مخفف | successGreen مع opacity 30% | 20 | 2 | — |
| حلقة رادار الأعضاء — عادي | statusColor مع opacity 35% | 8 | 1 | — |
| حلقة رادار الأعضاء — يرن | statusColor مع opacity 80% | 20 | 4 | — |
| مركز دائرة الحلقة (glow) | `#4A1A6B` مع opacity 15% | 40 | 10 | — |
| streak card icon | accentGold مع opacity 30% | 20 | 2 | — |
| نص عداد السلسلة (text shadow) | `#FFD700` | 15 | — | — |
| نص عداد الفجر (text shadow) | displayColor مع opacity 30% | 15 | — | (0, 2) |

---

## 4. المكونات المتكررة (Reusable Components)

---

### 4.1 `AppButton` — زر أساسي مخصص

**الملف:** `lib/widgets/app_button.dart`

**المظهر بحسب النوع:**

| الخاصية | Primary (isSecondary=false) | Secondary (isSecondary=true) |
|---|---|---|
| خلفية | `accentGold` (#FFD700) أو لون مخصص | شفاف (Transparent) |
| لون النص | `background` (#07071B) | `textPrimary` (#FFFFFF) |
| نوع الزر | `ElevatedButton` | `OutlinedButton` |
| حد | لا يوجد | 2dp بلون `border` (#25254A) أو لون مخصص |
| border radius | 16dp | 16dp |
| disabled خلفية | لون الزر مع opacity 50% | — |
| elevation | 0 | — |
| padding افتراضي | vertical 16, horizontal 24 | vertical 16, horizontal 24 |
| حجم النص | 16sp Bold | 16sp Bold |
| glow effect (اختياري) | BoxShadow بلون الزر opacity 40%, blur 15, offset (0,4) | لا يوجد |
| حالة loading | CircularProgressIndicator عرض/ارتفاع 20dp، strokeWidth 2 | — |
| icon (اختياري) | Icon حجم 20dp، SizedBox width 8 فاصل | — |

---

### 4.2 `GlassCard` — بطاقة زجاجية (Glassmorphism)

**الملف:** `lib/widgets/glass_card.dart`

| الخاصية | القيمة الافتراضية |
|---|---|
| blur (BackdropFilter) | sigmaX = 15, sigmaY = 15 |
| opacity خلفية | 8% من `surface` (#0D0D2B) |
| border radius | 24dp |
| حد | 1.5dp — لون `border` (#25254A) مع opacity 30% |
| padding داخلي | 20dp جميع الاتجاهات |
| التأثير | `ClipRRect` + `BackdropFilter(ImageFilter.blur)` |

---

### 4.3 `AppTextField` — حقل إدخال مخصص

**الملف:** `lib/widgets/app_text_field.dart`

| الخاصية | القيمة |
|---|---|
| خلفية | `surface` (#0D0D2B) — filled: true |
| padding | horizontal 16, vertical 16 |
| border radius | 12dp |
| حد عادي | 1dp — `#25254A` |
| حد focused | 1dp — `#FFD700` |
| حد error | 1dp — `#E74C3C` |
| لون تسمية (label) | `#B0B0C5`، 14sp، SemiBold (600) |
| فجوة بين label والحقل | 8dp |
| لون hint | `#6B6B8A` |
| لون النص المكتوب | `#FFFFFF`، 16sp، Regular |
| prefix icon لون | `#6B6B8A` |
| عداد الأحرف | مخفي (counterText: "") |

---

### 4.4 `HalqaCircleWidget` — دائرة الحلقة الدائرية

**الملف:** `lib/screens/halqa/widgets/halqa_circle_widget.dart`

| الخاصية | القيمة |
|---|---|
| حجم الـ Canvas | min(maxWidth, 300dp) |
| نصف القطر الدائري للأعضاء | size / 2.8 |
| أفاتار العضو — radius | 24dp (diameter 48dp) |
| padding حول الأفاتار | 3dp |
| حجم الأيقونة المبدئية (initial) | 1 حرف، Bold، أبيض |
| خط الوسط في الدائرة | 1.5dp stroke، عرض 2dp (ارتفاع 30dp) |
| سهم الاتجاه | مثلث حجم 8dp، `#FFD700` |
| نبض الحالة العادية | scale 1.0+value*0.04 |
| نبض حالة يرن | scale 1.0+value*0.18 |
| دائرة رادار العضو الرنان | قطر 76dp * (1+value*0.25) |
| نص اسم العضو تحت الأفاتار | 11sp Bold |
| نص حالة العضو | 9sp Bold |

---

### 4.5 `MemberCardWidget` — بطاقة عضو في قائمة الحلقة

**الملف:** `lib/screens/halqa/widgets/member_card_widget.dart`

| الخاصية | القيمة |
|---|---|
| الحاوي | GlassCard بـ padding 16dp |
| فجوة بين البطاقات | 12dp أسفل |
| الأفاتار — radius | 26dp (diameter 52dp) |
| خلفية الأفاتار | `border` (#25254A) |
| أيقونة المبدئية | Bold، أبيض، 20sp |
| فجوة أفاتار ← نص | 16dp |
| اسم المستخدم الحالي | `#FFD700` (accentGold) |
| اسم الأعضاء الآخرين | `#FFFFFF` |
| badge "أنت" — خلفية | `#FFD700` مع opacity 15% |
| badge "أنت" — حد | `#FFD700` مع opacity 30%، border radius 6dp |
| badge "أنت" — نص | 9sp، `#FFD700`، caption style |
| أيقونة مسؤولية | `arrow_circle_left_outlined`، 16dp، `#FFD700` |
| فجوة أيقونة ← نص المسؤولية | 6dp |
| badge "مشرف الحلقة" — خلفية | `#4A1A6B` مع opacity 20% |
| badge "مشرف الحلقة" — حد | `#4A1A6B` مع opacity 40% |
| badge "مشرف الحلقة" — نص | 9sp Bold، `#FFD700` |
| badge "عضو حلقة" — خلفية | أبيض مع opacity 5% |
| badge "عضو حلقة" — حد | أبيض مع opacity 10% |
| badge "عضو حلقة" — نص | 9sp Bold، white70 |
| badge borderRadius | 8dp |
| أيقونة إزالة العضو | دائرة، خلفية `#E74C3C` opacity 15%، icon 14dp |
| أيقونة الحالة | 14dp |
| نص الحالة | 10sp Bold، لون الحالة |

---

### 4.6 `AlarmAnimationWidget` — أنيميشن دائرة المنبه

**الملف:** `lib/screens/alarm/widgets/alarm_animation_widget.dart`

| الخاصية | الرنين (isSoftened=false) | المخفف (isSoftened=true) |
|---|---|---|
| أيقونة مركزية | `notifications_active_rounded` | `hourglass_empty_rounded` |
| لون الدائرة الرئيسية | `#FFD700` | `#2ECC71` |
| قطر النواة المركزية | 140dp | 140dp |
| خلفية النواة | `surface` (#0D0D2B) | `surface` (#0D0D2B) |
| حد النواة | 3dp، ringColor مع opacity 50% | 3dp، ringColor مع opacity 50% |
| ظل النواة | blurRadius 40، spreadRadius 5 | blurRadius 20، spreadRadius 2 |
| حجم الأيقونة | 64dp | 64dp |
| حلقة خارجية 1 | قطر 160dp، تتمدد scale 1.0+(value*0.4) | نفسه |
| حلقة خارجية 2 | قطر 160dp، تتمدد scale 1.0+(value*0.8) | نفسه |
| سُمك حلقات الـ ripple | 4dp | 4dp |
| مدة الأنيميشن | 2 ثانية، repeat (لا عكس) | 2 ثانية، repeat |

---

### 4.7 `FajrCountdownWidget` — عداد الفجر التنازلي

**الملف:** `lib/screens/home/widgets/fajr_countdown_widget.dart`

| الخاصية | القيمة |
|---|---|
| الحاوي | GlassCard، opacity 0.08، padding 24dp |
| حجم رقم الوقت | 42sp Bold، letterSpacing 2.0 |
| text shadow الرقم | blurRadius 15، offset (0,2)، opacity 30% |
| نبض تحذير (< 5 دقائق) | ScaleTransition، scale 0.98↔1.02، 1000ms، Curves.easeInOut |
| فاصل Divider | `border` (#25254A)، height 1dp |
| فاصل عمودي | عرض 1.5dp، ارتفاع 30dp، `border` (#25254A) |
| حد البطاقة عند الاقتراب | displayColor مع opacity 50% |

---

### 4.8 `StreakWidget` — بطاقة سلسلة الأيام

**الملف:** `lib/screens/stats/widgets/streak_widget.dart`

| الخاصية | القيمة |
|---|---|
| الحاوي | GlassCard، padding vertical 24 / horizontal 16 |
| حد البطاقة | `#FFD700` مع opacity 40% |
| دائرة الأيقونة — خلفية | `#FFD700` مع opacity 15% |
| دائرة الأيقونة — padding | 16dp |
| دائرة الأيقونة — ظل | `#FFD700` opacity 30%، blurRadius 20، spreadRadius 2 |
| أيقونة النار | `local_fire_department_rounded`، 48dp، `#FFD700` |
| حجم عدد الأيام | 36sp Bold، `#FFD700`، text shadow blur 15 |
| البطاقات الثانوية | GlassCard، padding 16dp |
| أيقونة "أطول سلسلة" | `emoji_events_rounded`، 28dp، `#FFD700` |
| أيقونة "إجمالي الفجر" | `wb_sunny_rounded`، 28dp، `#2ECC71` |
| حجم أرقام الإحصاء الثانوي | 20sp Bold، `#FFFFFF` |
| تسميات الإحصاء الثانوي | 13sp، white60 |
| فجوة بين البطاقتين الثانويتين | 16dp أفقي |

---

### 4.9 `AnimatedGradientBg` — الخلفية المتحركة

**الملف:** `lib/widgets/animated_gradient_bg.dart`

| الخاصية | القيمة |
|---|---|
| الألوان | `#07071B` → `#1A1A3E` → `#4A1A6B` → `#07071B` (4 نقاط) |
| نوع الأنيميشن | TweenSequence يدور بين 4 زوايا (topLeft→topRight→bottomRight→bottomLeft) |
| مدة الدورة الكاملة | 15 ثانية |
| نوع التكرار | repeat مع عكس (reverse: true) |
| تُستخدم في | شاشة Login، شاشة Onboarding، ربما شاشة المنبه |

---

## 5. الحركات والانتقالات (Animations & Transitions)

### جدول كل الحركات الموجودة في التطبيق

| الحركة | النوع | المدة | المنحنى (Curve) | التكرار | الموضع |
|---|---|---|---|---|---|
| خلفية gradient متحركة | TweenSequence (4 محطات للاتجاه) | 15,000ms | — | لا نهاية (reverse) | جميع الشاشات عبر AnimatedGradientBg |
| ripple المنبه (2 حلقات) | AnimationController.repeat | 2,000ms | خطي | لا نهاية | شاشة الرنين (AlarmAnimationWidget) |
| نبض أفاتار العضو في الدائرة | AnimationController.repeat (reverse) | 2,000ms | خطي | لا نهاية | HalqaCircleWidget |
| نبض تحذير الفجر (< 5 دقائق) | ScaleTransition: 0.98 ↔ 1.02 | 1,000ms | Curves.easeInOut | repeat (reverse) | FajrCountdownWidget |
| نبض سلسلة خطوط الحلقة | تغيير opacity عبر pulseValue | 2,000ms | خطي | لا نهاية | HalqaCircleWidget (ChainLinesPainter) |
| انتقالات GoRouter | PageTransition افتراضي Flutter | ~300ms | — | مرة واحدة | بين جميع الشاشات |

### ملاحظات على الانتقالات

- **لا يوجد** انتقال مخصص معرّف صريحاً في GoRouter (يستخدم الافتراضي).
- القيمة `300ms` مذكورة في التوثيق الداخلي للمشروع كمعيار لكل انتقال.
- شاشة المنبه تظهر عبر Full-Screen Intent (Android) — لا انتقال عادي.
- أزرار AppButton لا تحتوي على `InkWell` مخصص لكن Flutter يضيف Ripple Effect تلقائياً على `ElevatedButton` و`OutlinedButton`.

---

## ملخص القيم السريع (Quick Reference)

```
═══════════════════════════════════════════════════════
COLORS
  Background:     #07071B
  Surface:        #0D0D2B
  Border:         #25254A
  Primary:        #1A1A3E
  Secondary:      #4A1A6B
  Accent Gold:    #FFD700
  Success Green:  #2ECC71
  Danger Red:     #E74C3C
  Text Primary:   #FFFFFF
  Text Secondary: #B0B0C5
  Text Muted:     #6B6B8A

TYPOGRAPHY
  Font: Noto Sans Arabic (Google Fonts)
  Sizes: 9 / 10 / 11 / 12 / 14 / 16 / 20 / 24 / 32 / 36 / 42 sp
  Direction: RTL

RADII
  GlassCard: 24dp | AppButton: 16dp
  Card/Input: 12dp | Role badge: 8dp | "You" badge: 6dp

SPACING
  xs: 4dp | sm: 6dp | md: 8dp | base: 12dp
  lg: 16dp | xl: 20dp | xxl: 24dp
═══════════════════════════════════════════════════════
```
