package com.bagomri.fajrloop.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

        // 1. البحث عن الحلقة بكود الدعوة
        database.getReference("halqas")
            .orderByChild("inviteCode")
            .equalTo(formattedCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onComplete(false, "كود الدعوة غير صحيح أو منتهي الصلاحية")
                        return
                    }

                    // الحصول على الحلقة المطابقة
                    val halqaSnapshot = snapshot.children.first()
                    val halqaId = halqaSnapshot.key ?: return
                    val halqaName = halqaSnapshot.child("name").value as? String ?: "حلقة"

                    // جلب بيانات السلسلة والأعضاء الحالية
                    val currentChain = (halqaSnapshot.child("chain").value as? List<*>)
                        ?.filterIsInstance<String>()
                        ?.toMutableList() ?: mutableListOf()

                    // التحقق مما إذا كان المستخدم مسجلاً بالفعل
                    if (currentChain.contains(uid)) {
                        // العضو مسجل مسبقاً، نكتفي بتحويله للحلقة النشطة
                        database.getReference("users").child(uid).child("currentHalqaId").setValue(halqaId)
                            .addOnCompleteListener {
                                onComplete(true, halqaId)
                            }
                        return
                    }

                    // 2. تحديث السلسلة والأعضاء
                    currentChain.add(uid)

                    val membersSnapshot = halqaSnapshot.child("members")
                    val updatedMembers = mutableMapOf<String, Any>()

                    // إضافة العضو الجديد
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

                    // نسخ باقي الأعضاء وتجهيز التحديث
                    for (memberChild in membersSnapshot.children) {
                        val mId = memberChild.key ?: continue
                        val mData = memberChild.value as? Map<*, *> ?: continue
                        updatedMembers[mId] = mData.toMutableMap()
                    }

                    // 3. إعادة حساب الترتيب الدائري والمسؤوليات
                    recalculateLoopResponsibility(currentChain, updatedMembers)

                    // 4. إجراء تحديث ذري لقاعدة البيانات
                    val updates = hashMapOf<String, Any>()
                    updates["/halqas/$halqaId/chain"] = currentChain
                    updates["/halqas/$halqaId/members"] = updatedMembers
                    updates["/users/$uid/currentHalqaId"] = halqaId
                    updates["/users/$uid/joinedHalqas/$halqaId"] = true

                    database.reference.updateChildren(updates)
                        .addOnSuccessListener {
                            Log.d(TAG, "✅ Successfully joined Halqa: $halqaName")
                            onComplete(true, halqaId)
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "❌ Failed to join Halqa", it)
                            onComplete(false, it.localizedMessage)
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false, error.message)
                }
            })
    }

    /**
     * مغادرة الحلقة الحالية
     */
    fun leaveHalqa(onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(false, "المستخدم غير مسجل الدخول")
            return
        }

        val uid = currentUser.uid

        // 1. جلب معرف الحلقة الحالية للمستخدم من حسابه الشخصي
        database.getReference("users").child(uid).child("currentHalqaId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val halqaId = snapshot.value as? String
                    if (halqaId.isNullOrEmpty()) {
                        onComplete(true, null) // المستخدم ليس في أي حلقة
                        return
                    }

                    // 2. جلب تفاصيل الحلقة
                    database.getReference("halqas").child(halqaId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(halqaSnapshot: DataSnapshot) {
                                if (!halqaSnapshot.exists()) {
                                    // الحلقة غير موجودة سحابياً، نزيلها من حساب المستخدم
                                    clearUserHalqaRef(uid, halqaId, onComplete)
                                    return
                                }

                                val currentChain = (halqaSnapshot.child("chain").value as? List<*>)
                                    ?.filterIsInstance<String>()
                                    ?.toMutableList() ?: mutableListOf()

                                val membersSnapshot = halqaSnapshot.child("members")

                                // إزالة المستخدم الحالي من السلسلة
                                currentChain.remove(uid)

                                // إذا لم يتبقَ أي أعضاء في الحلقة، يتم حذفها وسرها بالكامل أمنياً
                                if (currentChain.isEmpty()) {
                                    val updates = hashMapOf<String, Any?>()
                                    updates["/halqas/$halqaId"] = null
                                    updates["/halqaSecrets/$halqaId"] = null
                                    updates["/users/$uid/currentHalqaId"] = ""
                                    updates["/users/$uid/joinedHalqas/$halqaId"] = null

                                    database.reference.updateChildren(updates)
                                        .addOnSuccessListener { onComplete(true, null) }
                                        .addOnFailureListener { onComplete(false, it.localizedMessage) }
                                    return
                                }

                                // إذا كان هناك أعضاء متبقون، نقوم بإعادة هيكلة الحلقة
                                val updatedMembers = mutableMapOf<String, Any>()
                                var wasAdmin = false

                                for (memberChild in membersSnapshot.children) {
                                    val mId = memberChild.key ?: continue
                                    if (mId == uid) {
                                        val role = memberChild.child("role").value as? String
                                        if (role == "admin") wasAdmin = true
                                        continue // تجاهل العضو المغادر
                                    }
                                    val mData = memberChild.value as? Map<*, *> ?: continue
                                    updatedMembers[mId] = mData.toMutableMap()
                                }

                                // إذا غادر الـ Admin، نقوم بترقية أول عضو متبقي في السلسلة ليصبح Admin
                                if (wasAdmin && currentChain.isNotEmpty()) {
                                    val newAdminId = currentChain[0]
                                    val adminData = updatedMembers[newAdminId] as? MutableMap<*, *>
                                    if (adminData != null) {
                                        @Suppress("UNCHECKED_CAST")
                                        val mutableAdminData = adminData as MutableMap<String, Any>
                                        mutableAdminData["role"] = "admin"
                                    }
                                }

                                // إعادة حساب الترتيب الدائري والمسؤوليات للأعضاء المتبقين
                                recalculateLoopResponsibility(currentChain, updatedMembers)

                                // إجراء التحديث الذري
                                val updates = hashMapOf<String, Any?>()
                                updates["/halqas/$halqaId/chain"] = currentChain
                                updates["/halqas/$halqaId/members"] = updatedMembers
                                updates["/users/$uid/currentHalqaId"] = ""
                                updates["/users/$uid/joinedHalqas/$halqaId"] = null

                                database.reference.updateChildren(updates)
                                    .addOnSuccessListener { onComplete(true, null) }
                                    .addOnFailureListener { onComplete(false, it.localizedMessage) }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                onComplete(false, error.message)
                            }
                        })
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

    /**
     * المراقبة المستمرة للحلقة النشطة الخاصة بالمستخدم الحالي
     */
    fun observeUserHalqa(onUpdate: (DataSnapshot?) -> Unit): ValueEventListener {
        val uid = auth.currentUser?.uid ?: return createEmptyListener()
        val userHalqaRef = database.getReference("users").child(uid).child("currentHalqaId")

        val listener = object : ValueEventListener {
            private var activeHalqaListener: ValueEventListener? = null
            private var currentActiveHalqaId: String? = null

            override fun onDataChange(snapshot: DataSnapshot) {
                val halqaId = snapshot.value as? String
                if (halqaId.isNullOrEmpty()) {
                    removeActiveListener()
                    onUpdate(null)
                    return
                }

                if (halqaId == currentActiveHalqaId) return // لم تتغير الحلقة الحالية

                // إلغاء المستمع القديم والبدء بمستمع جديد للحلقة الفعالة
                removeActiveListener()
                currentActiveHalqaId = halqaId

                val halqaRef = database.getReference("halqas").child(halqaId)
                activeHalqaListener = halqaRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(halqaSnap: DataSnapshot) {
                        onUpdate(halqaSnap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Halqa observation cancelled", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "User profile observation cancelled", error.toException())
            }

            private fun removeActiveListener() {
                activeHalqaListener?.let {
                    currentActiveHalqaId?.let { hId ->
                        database.getReference("halqas").child(hId).removeEventListener(it)
                    }
                }
                activeHalqaListener = null
                currentActiveHalqaId = null
            }
        }

        userHalqaRef.addValueEventListener(listener)
        return listener
    }

    /**
     * إزالة مستمع
     */
    fun removeObserver(listener: ValueEventListener) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).child("currentHalqaId").removeEventListener(listener)
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

    private fun createEmptyListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {}
        override fun onCancelled(error: DatabaseError) {}
    }
}
