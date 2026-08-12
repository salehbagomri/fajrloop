package com.bagomri.fajrloop.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * HalqaManager — المسؤول عن إدارة الحلقات الدائرية (إنشاء، انضمام، مغادرة، وتعديل السلسلة)
 *
 * يعتمد على Firebase Realtime Database بشكل كامل وبطرق برمجية متزامنة وآمنة.
 */
object HalqaManager {

    private const val TAG = "HalqaManager"
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase get() = FirebaseDatabase.getInstance()

    /**
     * إنشاء حلقة جديدة سحابياً
     *
     * @param name اسم الحلقة المدخل
     * @param onComplete كولباك (الحالة، رسالة الخطأ أو معرف الحلقة الجديدة)
     */
    fun createHalqa(name: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false, "المستخدم غير مسجل الدخول")
            return
        }

        val uid = currentUser.uid
        val halqasRef = database.getReference("halqas")
        val secretsRef = database.getReference("halqaSecrets")
        val usersRef = database.getReference("users")

        // 1. توليد معرّف عشوائي للحلقة الجديدة
        val halqaId = halqasRef.push().key
        if (halqaId == null) {
            onComplete(false, "فشل في توليد معرف الحلقة")
            return
        }

        // 2. توليد كود الدعوة والمفتاح السري
        val inviteCode = HalqaUtils.generateInviteCode()
        val sharedSecret = HalqaUtils.generateSharedSecret()
        val isoDate = getIso8601String(Date())

        // 3. هيكلة بيانات الحلقة الجديدة
        val memberMap = mapOf(
            "userId" to uid,
            "displayName" to (currentUser.displayName ?: "مسؤول الحلقة"),
            "photoUrl" to (currentUser.photoUrl?.toString() ?: ""),
            "role" to "admin", // منشئ الحلقة هو المسؤول دائماً
            "position" to 0,
            "responsibleForUserId" to uid, // بما أنه العضو الوحيد، فهو مسؤول عن نفسه حالياً
            "status" to "active",
            "joinedAt" to isoDate
        )

        val halqaMap = mapOf(
            "id" to halqaId,
            "name" to name,
            "inviteCode" to inviteCode,
            "createdBy" to uid,
            "createdAt" to isoDate,
            "type" to "fajr", // حقل type إلزامي للتوسع مستقبلاً
            "chain" to listOf(uid),
            "members" to mapOf(uid to memberMap)
        )

        // 4. هيكلة بيانات المفتاح السري المشترك (في عقدة مستقلة لمنع التعديل)
        val secretMap = mapOf(
            "sharedSecret" to sharedSecret
        )

        // 5. التحديث الذري للـ Realtime Database
        val updates = hashMapOf<String, Any>()
        updates["/halqas/$halqaId"] = halqaMap
        updates["/halqaSecrets/$halqaId"] = secretMap
        updates["/users/$uid/currentHalqaId"] = halqaId
        updates["/users/$uid/joinedHalqas/$halqaId"] = true

        database.reference.updateChildren(updates)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Halqa created successfully: $name (Code: $inviteCode)")
                saveSharedSecretLocally(halqaId, sharedSecret)
                onComplete(true, halqaId)
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Failed to create Halqa", it)
                onComplete(false, it.localizedMessage)
            }
    }

    /**
     * الانضمام إلى حلقة قائمة باستخدام كود الدعوة
     *
     * @param inviteCode كود الدعوة بصيغة FJR-XXXX أو XXXX
     */
    fun joinHalqa(inviteCode: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false, "المستخدم غير مسجل الدخول")
            return
        }

        val uid = currentUser.uid
        val formattedCode = if (inviteCode.startsWith("FJR-")) inviteCode else "FJR-$inviteCode"

        /*
        // [Legacy implementation without Transaction - kept for reference]
        database.getReference("halqas")
            .orderByChild("inviteCode")
            .equalTo(formattedCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onComplete(false, "كود الدعوة غير صحيح أو منتهي الصلاحية")
                        return
                    }
                    val halqaSnapshot = snapshot.children.first()
                    val halqaId = halqaSnapshot.key ?: return
                    val halqaName = halqaSnapshot.child("name").value as? String ?: "حلقة"
                    val currentChain = (halqaSnapshot.child("chain").value as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.toMutableList() ?: mutableListOf()
                    if (currentChain.contains(uid)) {
                        database.getReference("users").child(uid).child("currentHalqaId").setValue(halqaId)
                            .addOnCompleteListener { onComplete(true, halqaId) }
                        return
                    }
                    currentChain.add(uid)
                    val membersSnapshot = halqaSnapshot.child("members")
                    val updatedMembers = mutableMapOf<String, Any>()
                    val isoDate = getIso8601String(Date())
                    val newMemberMap = mutableMapOf(
                        "userId" to uid,
                        "displayName" to (currentUser.displayName ?: "عضو جديد"),
                        "photoUrl" to (currentUser.photoUrl?.toString() ?: ""),
                        "role" to "member",
                        "position" to (currentChain.size - 1),
                        "status" to "active",
                        "joinedAt" to isoDate
                    )
                    updatedMembers[uid] = newMemberMap
                    for (memberChild in membersSnapshot.children) {
                        val mId = memberChild.key ?: continue
                        val mData = memberChild.value as? Map<*, *> ?: continue
                        updatedMembers[mId] = mData.toMutableMap()
                    }
                    recalculateLoopResponsibility(currentChain, updatedMembers)
                    val updates = hashMapOf<String, Any>()
                    updates["/halqas/$halqaId/chain"] = currentChain
                    updates["/halqas/$halqaId/members"] = updatedMembers
                    updates["/users/$uid/currentHalqaId"] = halqaId
                    updates["/users/$uid/joinedHalqas/$halqaId"] = true
                    database.reference.updateChildren(updates)
                        .addOnSuccessListener { onComplete(true, halqaId) }
                        .addOnFailureListener { onComplete(false, it.localizedMessage) }
                }
                override fun onCancelled(error: DatabaseError) { onComplete(false, error.message) }
            })
        */

        // 1. البحث عن الحلقة بكود الدعوة للحصول على halqaId
        database.getReference("halqas")
            .orderByChild("inviteCode")
            .equalTo(formattedCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onComplete(false, "كود الدعوة غير صحيح أو منتهي الصلاحية")
                        return
                    }

                    val halqaSnapshot = snapshot.children.first()
                    val halqaId = halqaSnapshot.key ?: return
                    val isoDate = getIso8601String(Date())

                    val currentChain = (halqaSnapshot.child("chain").value as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.toMutableList() ?: mutableListOf()

                    // إذا كان العضو منضماً مسبقاً، نربطه بالحلقة فقط
                    if (currentChain.contains(uid)) {
                        val userUpdates = hashMapOf<String, Any>(
                            "/users/$uid/currentHalqaId" to halqaId,
                            "/users/$uid/joinedHalqas/$halqaId" to true
                        )
                        database.reference.updateChildren(userUpdates)
                            .addOnSuccessListener {
                                ensureLocalSharedSecret(halqaId) {
                                    onComplete(true, halqaId)
                                }
                            }
                            .addOnFailureListener { onComplete(false, it.localizedMessage) }
                        return
                    }

                    // إضافة العضو للسلسلة وتحديث بيانات الأعضاء
                    currentChain.add(uid)

                    val membersSnapshot = halqaSnapshot.child("members")
                    val updatedMembers = mutableMapOf<String, Any>()

                    for (memberChild in membersSnapshot.children) {
                        val mId = memberChild.key ?: continue
                        val mData = memberChild.value as? Map<*, *> ?: continue
                        updatedMembers[mId] = mData.toMutableMap()
                    }

                    val newMemberMap = mapOf(
                        "userId" to uid,
                        "displayName" to (currentUser.displayName ?: "عضو جديد"),
                        "photoUrl" to (currentUser.photoUrl?.toString() ?: ""),
                        "role" to "member",
                        "position" to (currentChain.size - 1),
                        "status" to "active",
                        "joinedAt" to isoDate
                    )
                    updatedMembers[uid] = newMemberMap

                    // إعادة حساب مسؤوليات الاستيقاظ الدائرية
                    recalculateLoopResponsibility(currentChain, updatedMembers)

                    // التحديث الذري الشامل للسلسلة، الأعضاء، وبيانات مستخدم الانضمام
                    val updates = hashMapOf<String, Any>(
                        "/halqas/$halqaId/chain" to currentChain,
                        "/halqas/$halqaId/members" to updatedMembers,
                        "/users/$uid/currentHalqaId" to halqaId,
                        "/users/$uid/joinedHalqas/$halqaId" to true
                    )

                    database.reference.updateChildren(updates)
                        .addOnSuccessListener {
                            ensureLocalSharedSecret(halqaId) {
                                onComplete(true, halqaId)
                            }
                        }
                        .addOnFailureListener {
                            onComplete(false, it.localizedMessage ?: "فشل الانضمام للحلقة")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false, error.message)
                }
            })
    }

    /**
     * مغادرة الحلقة الحالية (نسخة suspend قابلة للاختبار ومجردة من الـ callbacks المتداخلة)
     */
    suspend fun leaveHalqaSuspend(): Result<Unit> = runCatching {
        val currentUser = auth.currentUser ?: throw Exception("المستخدم غير مسجل الدخول")
        val uid = currentUser.uid

        val userHalqaSnap = database.getReference("users").child(uid).child("currentHalqaId").awaitSingleValue()
        val halqaId = userHalqaSnap.value as? String
        if (halqaId.isNullOrEmpty()) {
            return@runCatching Unit
        }

        val halqaSnapshot = database.getReference("halqas").child(halqaId).awaitSingleValue()
        if (!halqaSnapshot.exists()) {
            clearUserHalqaRefSuspend(uid, halqaId)
            return@runCatching Unit
        }

        val currentChain = (halqaSnapshot.child("chain").value as? List<*>)
            ?.filterIsInstance<String>()
            ?.toMutableList() ?: mutableListOf()

        val membersSnapshot = halqaSnapshot.child("members")
        currentChain.remove(uid)

        if (currentChain.isEmpty()) {
            val updates = hashMapOf<String, Any?>(
                "/halqas/$halqaId" to null,
                "/halqaSecrets/$halqaId" to null,
                "/users/$uid/currentHalqaId" to "",
                "/users/$uid/joinedHalqas/$halqaId" to null
            )
            database.reference.updateChildren(updates).awaitTask()
            return@runCatching Unit
        }

        val updatedMembers = mutableMapOf<String, Any>()
        var wasAdmin = false

        for (memberChild in membersSnapshot.children) {
            val mId = memberChild.key ?: continue
            if (mId == uid) {
                val role = memberChild.child("role").value as? String
                if (role == "admin") wasAdmin = true
                continue
            }
            val mData = memberChild.value as? Map<*, *> ?: continue
            updatedMembers[mId] = mData.toMutableMap()
        }

        if (wasAdmin && currentChain.isNotEmpty()) {
            val newAdminId = currentChain[0]
            val adminData = updatedMembers[newAdminId] as? MutableMap<*, *>
            if (adminData != null) {
                @Suppress("UNCHECKED_CAST")
                val mutableAdminData = adminData as MutableMap<String, Any>
                mutableAdminData["role"] = "admin"
            }
        }

        recalculateLoopResponsibility(currentChain, updatedMembers)

        val updates = hashMapOf<String, Any?>(
            "/halqas/$halqaId/chain" to currentChain,
            "/halqas/$halqaId/members" to updatedMembers,
            "/users/$uid/currentHalqaId" to "",
            "/users/$uid/joinedHalqas/$halqaId" to null
        )

        database.reference.updateChildren(updates).awaitTask()
        Unit
    }

    private suspend fun clearUserHalqaRefSuspend(uid: String, halqaId: String) {
        val updates = hashMapOf<String, Any?>(
            "/users/$uid/currentHalqaId" to "",
            "/users/$uid/joinedHalqas/$halqaId" to null
        )
        database.reference.updateChildren(updates).awaitTask()
    }

    /**
     * مغادرة الحلقة الحالية (للتوافق العكسي)
     */
    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun leaveHalqa(onComplete: (Boolean, String?) -> Unit) {
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            leaveHalqaSuspend().fold(
                onSuccess = { onComplete(true, null) },
                onFailure = { onComplete(false, it.message) }
            )
        }
    }

    /**
     * حذف عضو من الحلقة بواسطة المسؤول (Admin)
     */
    fun removeMemberFromHalqa(halqaId: String, targetUid: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false, "المستخدم غير مسجل الدخول")
            return
        }

        val uid = currentUser.uid
        val halqaRef = database.getReference("halqas").child(halqaId)

        halqaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onComplete(false, "الحلقة غير موجودة")
                    return
                }

                // التحقق من صلاحية المسؤول
                val adminRole = snapshot.child("members").child(uid).child("role").value as? String
                if (adminRole != "admin") {
                    onComplete(false, "عفواً، لا يملك صلاحية حذف الأعضاء إلا مسؤول الحلقة")
                    return
                }

                if (targetUid == uid) {
                    onComplete(false, "لا يمكنك حذف نفسك بهذه الطريقة، استخدم زر مغادرة الحلقة")
                    return
                }

                val currentChain = (snapshot.child("chain").value as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toMutableList() ?: mutableListOf()

                val membersSnapshot = snapshot.child("members")

                // إزالة العضو المستهدف من السلسلة
                currentChain.remove(targetUid)

                val updatedMembers = mutableMapOf<String, Any>()
                for (memberChild in membersSnapshot.children) {
                    val mId = memberChild.key ?: continue
                    if (mId == targetUid) continue // تجاهل العضو المطرود
                    val mData = memberChild.value as? Map<*, *> ?: continue
                    updatedMembers[mId] = mData.toMutableMap()
                }

                // إعادة حساب الترتيب الدائري والمسؤوليات للأعضاء المتبقين
                recalculateLoopResponsibility(currentChain, updatedMembers)

                // إجراء التحديث الذري على عقدة الحلقة
                // ملاحظة: تنظيف بيانات المستخدم المحذوف يتم تلقائياً عبر كشف الطرد في HalqaViewModel
                val updates = hashMapOf<String, Any?>()
                updates["/halqas/$halqaId/chain"] = currentChain
                updates["/halqas/$halqaId/members"] = updatedMembers

                database.reference.updateChildren(updates)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { onComplete(false, it.localizedMessage) }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    /**
     * إعادة ترتيب السلسلة يدوياً (خاص بالمسؤول Admin فقط)
     */
    fun reorderChain(halqaId: String, newChain: List<String>, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false, "المستخدم غير مسجل الدخول")
            return
        }

        val uid = currentUser.uid
        val halqaRef = database.getReference("halqas").child(halqaId)

        halqaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onComplete(false, "الحلقة غير موجودة")
                    return
                }

                // التحقق من دور المستخدم الحالي (يجب أن يكون Admin لترتيب الدائرة)
                val role = snapshot.child("members").child(uid).child("role").value as? String
                if (role != "admin") {
                    onComplete(false, "عفواً، لا يملك صلاحية إعادة الترتيب إلا مسؤول الحلقة")
                    return
                }

                val membersSnapshot = snapshot.child("members")
                val updatedMembers = mutableMapOf<String, Any>()

                for (memberChild in membersSnapshot.children) {
                    val mId = memberChild.key ?: continue
                    val mData = memberChild.value as? Map<*, *> ?: continue
                    updatedMembers[mId] = mData.toMutableMap()
                }

                // إعادة حساب الترتيب والمسؤوليات بناءً على الترتيب الجديد للسلسلة
                recalculateLoopResponsibility(newChain, updatedMembers)

                val updates = hashMapOf<String, Any>()
                updates["/halqas/$halqaId/chain"] = newChain
                updates["/halqas/$halqaId/members"] = updatedMembers

                database.reference.updateChildren(updates)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { onComplete(false, it.localizedMessage) }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false, error.message)
            }
        })
    }

    private val activeHalqaListenersMap = java.util.concurrent.ConcurrentHashMap<ValueEventListener, Pair<String, ValueEventListener>>()

    /**
     * المراقبة المستمرة للحلقة النشطة الخاصة بالمستخدم الحالي
     */
    fun observeUserHalqa(onUpdate: (DataSnapshot?) -> Unit): ValueEventListener {
        val uid = auth.currentUser?.uid ?: return createEmptyListener()
        val userHalqaRef = database.getReference("users").child(uid).child("currentHalqaId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val halqaId = snapshot.value as? String
                if (halqaId.isNullOrEmpty()) {
                    detachHalqaChildListener(this)
                    onUpdate(null)
                    return
                }

                ensureLocalSharedSecret(halqaId)

                val currentPair = activeHalqaListenersMap[this]
                if (currentPair?.first == halqaId) return // لم تتغير الحلقة الحالية

                detachHalqaChildListener(this)

                val halqaRef = database.getReference("halqas").child(halqaId)
                val childListener = object : ValueEventListener {
                    override fun onDataChange(halqaSnap: DataSnapshot) {
                        onUpdate(halqaSnap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Halqa observation cancelled", error.toException())
                    }
                }
                activeHalqaListenersMap[this] = Pair(halqaId, childListener)
                halqaRef.addValueEventListener(childListener)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "User profile observation cancelled", error.toException())
            }
        }

        userHalqaRef.addValueEventListener(listener)
        return listener
    }

    private fun detachHalqaChildListener(userListener: ValueEventListener) {
        activeHalqaListenersMap.remove(userListener)?.let { (hId, childListener) ->
            try {
                database.getReference("halqas").child(hId).removeEventListener(childListener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to detach child listener", e)
            }
        }
    }

    /**
     * إزالة مستمع
     */
    fun removeObserver(listener: ValueEventListener) {
        val uid = auth.currentUser?.uid ?: return
        try {
            database.getReference("users").child(uid).child("currentHalqaId").removeEventListener(listener)
        } catch (e: Exception) {}
        detachHalqaChildListener(listener)
    }

    // =================================================================
    //  دوال داخلية مساعدة (Internal Helpers)
    // =================================================================

    private fun clearUserHalqaRef(uid: String, halqaId: String, onComplete: (Boolean, String?) -> Unit) {
        val updates = hashMapOf<String, Any?>()
        updates["/users/$uid/currentHalqaId"] = ""
        updates["/users/$uid/joinedHalqas/$halqaId"] = null
        database.reference.updateChildren(updates)
            .addOnCompleteListener { onComplete(true, null) }
    }

    private fun saveSharedSecretLocally(halqaId: String, sharedSecret: String) {
        if (sharedSecret.isEmpty()) return
        try {
            val context = com.bagomri.fajrloop.FajrLoopApp.instance
            val prefs = context.getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("halqa_shared_secret_$halqaId", sharedSecret).apply()
            Log.d(TAG, "🔑 Shared secret saved locally for Halqa: $halqaId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save shared secret locally", e)
        }
    }

    fun ensureLocalSharedSecret(halqaId: String, onComplete: (() -> Unit)? = null) {
        try {
            val context = com.bagomri.fajrloop.FajrLoopApp.instance
            val prefs = context.getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val existingSecret = prefs.getString("halqa_shared_secret_$halqaId", "")
            if (!existingSecret.isNullOrEmpty()) {
                onComplete?.invoke()
                return
            }
            database.getReference("halqaSecrets").child(halqaId).child("sharedSecret").get()
                .addOnSuccessListener { snap ->
                    val secret = snap.value as? String ?: ""
                    if (secret.isNotEmpty()) {
                        saveSharedSecretLocally(halqaId, secret)
                    }
                    onComplete?.invoke()
                }
                .addOnFailureListener {
                    onComplete?.invoke()
                }
        } catch (e: Exception) {
            onComplete?.invoke()
        }
    }

    /**
     * إعادة حساب المسؤوليات والمراكز الدائرية بناءً على مصفوفة السلسلة الفعالة
     *
     * خوارزمية السلسلة الدائرية (المحددة في المواصفات التقنية):
     * Responsible Partner = chain[(i + 1) mod N] (المسؤول عن إيقاظ العضو الحالي i)
     */
    private fun recalculateLoopResponsibility(chain: List<String>, membersMap: MutableMap<String, Any>) {
        val n = chain.size
        for (i in 0 until n) {
            val currentUid = chain[i]
            val responsibleUid = chain[(i + 1) % n] // العضو في الموقع التالي هو المسؤول عن إيقاظه

            val memberData = membersMap[currentUid] as? MutableMap<*, *>
            if (memberData != null) {
                @Suppress("UNCHECKED_CAST")
                val mutableData = memberData as MutableMap<String, Any>
                mutableData["position"] = i
                mutableData["responsibleForUserId"] = responsibleUid
            }
        }
    }

    private fun getIso8601String(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    /**
     * إرسال إشارة اختبار منبه الحلقة لجميع الأعضاء (رنين بعد دقيقة واحدة)
     */
    fun triggerTestLoopAlarm(halqaId: String, onComplete: (Boolean, String?) -> Unit) {
        val triggerTime = System.currentTimeMillis() + 60_000L
        val updates = hashMapOf<String, Any>(
            "testAlarmTime" to triggerTime,
            "testAlarmTriggeredAt" to System.currentTimeMillis()
        )
        database.getReference("halqas").child(halqaId).updateChildren(updates)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
    }

    private fun createEmptyListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {}
        override fun onCancelled(error: DatabaseError) {}
    }
}

// Extension functions تحويل Firebase callbacks إلى Coroutines علّاقة معلقة (suspend)

suspend fun com.google.firebase.database.Query.awaitSingleValue(): DataSnapshot =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cont.resumeWith(Result.success(snapshot))
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resumeWith(Result.failure(error.toException()))
            }
        })
    }

suspend fun com.google.firebase.database.DatabaseReference.awaitVoid(): Unit =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        setValue(null).addOnSuccessListener { cont.resumeWith(Result.success(Unit)) }
            .addOnFailureListener { cont.resumeWith(Result.failure(it)) }
    }

suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resumeWith(Result.success(it)) }
        addOnFailureListener { cont.resumeWith(Result.failure(it)) }
    }
