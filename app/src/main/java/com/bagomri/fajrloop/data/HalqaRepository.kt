package com.bagomri.fajrloop.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

class HalqaRepository {

    /**
     * إنشاء حلقة جديدة سحابياً
     */
    fun createHalqa(name: String, onComplete: (Boolean, String?) -> Unit) {
        HalqaManager.createHalqa(name, onComplete)
    }

    /**
     * الانضمام إلى حلقة قائمة بكود الدعوة
     */
    fun joinHalqa(inviteCode: String, onComplete: (Boolean, String?) -> Unit) {
        HalqaManager.joinHalqa(inviteCode, onComplete)
    }

    /**
     * مغادرة العضو للحلقة الحالية
     */
    fun leaveHalqa(onComplete: (Boolean, String?) -> Unit) {
        HalqaManager.leaveHalqa(onComplete)
    }

    /**
     * إعادة ترتيب السلسلة يدوياً
     */
    fun reorderChain(halqaId: String, newChain: List<String>, onComplete: (Boolean, String?) -> Unit) {
        HalqaManager.reorderChain(halqaId, newChain, onComplete)
    }

    /**
     * المراقبة المستمرة للحلقة النشطة الخاصة بالمستخدم
     */
    fun observeUserHalqa(onUpdate: (DataSnapshot?) -> Unit): ValueEventListener {
        return HalqaManager.observeUserHalqa(onUpdate)
    }

    /**
     * إزالة مستمع الحلقة
     */
    fun removeObserver(listener: ValueEventListener) {
        HalqaManager.removeObserver(listener)
    }
}
