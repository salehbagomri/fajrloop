import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

/**
 * onEmergencyPanic — يُفعَّل عند كتابة status=panic في dailyRecords.
 * يرسل إشعار استغاثة عاجل وعالي الأولوية لكل أعضاء الحلقة.
 */
export const onEmergencyPanic = functions.database.ref('/dailyRecords/{halqaId}/{date}/{userId}')
    .onWrite(async (change, context) => {
        const before = change.before.val();
        const after = change.after.val();

        // تفعيل فقط عند الانتقال إلى حالة panic
        if (!after || after.status !== 'panic' || (before && before.status === 'panic')) {
            return null;
        }

        const halqaId = context.params.halqaId;
        const userId = context.params.userId;

        // جلب اسم العضو المستغيث
        const userSnap = await admin.database().ref(`/users/${userId}`).once('value');
        const displayName = userSnap.child('displayName').val() || 'صديقك';

        // جلب أعضاء الحلقة
        const halqaSnap = await admin.database().ref(`/halqas/${halqaId}`).once('value');
        const members = halqaSnap.child('members').val() || {};

        const memberIds = Object.keys(members).filter(id => id !== userId);
        const tokenSnaps = await Promise.all(
            memberIds.map(id => admin.database().ref(`/users/${id}/fcmToken`).once('value'))
        );
        const tokens: string[] = tokenSnaps
            .map(snap => snap.val())
            .filter((token): token is string => !!token);

        if (tokens.length === 0) {
            console.log('No FCM tokens found for loop members');
            return null;
        }

        const messages = tokens.map(token => ({
            token: token,
            data: {
                type: 'emergency_panic',
                title: '🚨 نداء استغاثة عاجل من الفجر!',
                body: `🚨 نداء استغاثة عاجل من الفجر! صديقك [${displayName}] لا يستطيع الاستيقاظ ويرجو مساعدتكم والاتصال به فوراً!`,
                friendUid: userId,
                friendName: displayName
            }
        }));

        const response = await admin.messaging().sendEach(messages);
        console.log(`Panic notifications sent to ${tokens.length} devices. Success: ${response.successCount}, Failures: ${response.failureCount}`);
        return null;
    });

/**
 * onChallengeComplete — يُفعَّل عند كتابة status=challenge_done في dailyRecords.
 * يرسل إشعاراً عاجلاً للمسؤول المباشر ليقوم بتأكيد الاستيقاظ.
 */
export const onChallengeComplete = functions.database.ref('/dailyRecords/{halqaId}/{date}/{userId}')
    .onWrite(async (change, context) => {
        const before = change.before.val();
        const after = change.after.val();

        // تفعيل فقط عند الانتقال إلى حالة challenge_done
        if (!after || after.status !== 'challenge_done' || (before && before.status === 'challenge_done')) {
            return null;
        }

        const halqaId = context.params.halqaId;
        const date = context.params.date;
        const userId = context.params.userId;

        // حفظ challengeDoneAtMillis إذا لم يكن مسجلاً
        if (!after.challengeDoneAtMillis) {
            await admin.database().ref(`/dailyRecords/${halqaId}/${date}/${userId}/challengeDoneAtMillis`).set(Date.now());
        }

        // جلب اسم العضو
        const userSnap = await admin.database().ref(`/users/${userId}`).once('value');
        const displayName = userSnap.child('displayName').val() || 'صديقك';

        // جلب المسؤول المباشر عن هذا العضو
        const halqaSnap = await admin.database().ref(`/halqas/${halqaId}`).once('value');
        const responsibleForUserId = halqaSnap.child(`members/${userId}/responsibleForUserId`).val();

        if (!responsibleForUserId) {
            console.log(`No supervisor found for user: ${userId}`);
            return null;
        }

        // جلب FCM Token للمسؤول
        const supervisorTokenSnap = await admin.database().ref(`/users/${responsibleForUserId}/fcmToken`).once('value');
        const token = supervisorTokenSnap.val();
        if (!token) {
            console.log(`No FCM Token registered for supervisor: ${responsibleForUserId}`);
            return null;
        }

        const payload = {
            token: token,
            data: {
                type: 'challenge_done',
                title: '🌅 صديقك ينتظر تأكيدك!',
                body: `صديقك [${displayName}] حل تحدي الاستيقاظ، أكّد استيقاظه الآن!`,
                friendUid: userId
            }
        };

        await admin.messaging().send(payload);
        console.log(`Challenge complete notification sent to supervisor: ${responsibleForUserId}`);
        return null;
    });

/**
 * onWakeConfirmed — يُفعَّل عند كتابة status=awake في dailyRecords.
 * يرسل إشعاراً للمستخدم الذي استيقظ لتأكيد انتهاء التحدي.
 */
export const onWakeConfirmed = functions.database.ref('/dailyRecords/{halqaId}/{date}/{userId}')
    .onWrite(async (change, context) => {
        const before = change.before.val();
        const after = change.after.val();

        // تفعيل فقط عند الانتقال إلى حالة awake
        if (!after || after.status !== 'awake' || (before && before.status === 'awake')) {
            return null;
        }

        const userId = context.params.userId;

        const tokenSnap = await admin.database().ref(`/users/${userId}/fcmToken`).once('value');
        const token = tokenSnap.val();
        if (!token) {
            console.log(`No FCM Token found for user: ${userId}`);
            return null;
        }

        const payload = {
            token: token,
            data: {
                type: 'wake_confirmed',
                title: '🌅 تم تأكيد استيقاظك!',
                body: 'تم تأكيد استيقاظك بنجاح! بارك الله فيك ويومك مبارك 🌅'
            }
        };

        await admin.messaging().send(payload);
        console.log(`Wake confirmed notification sent to user: ${userId}`);
        return null;
    });

/**
 * onNewChatMessage — يُفعَّل عند إضافة رسالة في chatMessages.
 * يرسل إشعاراً لكل أعضاء الحلقة ما عدا المرسل.
 */
export const onNewChatMessage = functions.database.ref('/chatMessages/{halqaId}/{messageId}')
    .onCreate(async (snapshot, context) => {
        const message = snapshot.val();
        if (!message) return null;

        const halqaId = context.params.halqaId;
        const senderId = message.senderId;
        const senderName = message.senderName || 'عضو في الحلقة';
        const text = message.message || '';

        // جلب أعضاء الحلقة
        const halqaSnap = await admin.database().ref(`/halqas/${halqaId}`).once('value');
        const members = halqaSnap.child('members').val() || {};

        const memberIds = Object.keys(members).filter(id => id !== senderId);
        const tokenSnaps = await Promise.all(
            memberIds.map(id => admin.database().ref(`/users/${id}/fcmToken`).once('value'))
        );
        const tokens: string[] = tokenSnaps
            .map(snap => snap.val())
            .filter((token): token is string => !!token);

        if (tokens.length === 0) return null;

        const messages = tokens.map(token => ({
            token: token,
            data: {
                type: 'chat_message',
                title: `💬 رسالة جديدة من ${senderName}`,
                body: text,
                halqaId: halqaId
            }
        }));

        const response = await admin.messaging().sendEach(messages);
        console.log(`Chat notification sent to ${tokens.length} devices. Success: ${response.successCount}`);
        return null;
    });

/**
 * cleanupOldDailyRecords — يُفعَّل تلقائياً كل أسبوع (يوم الجمعة منتصف الليل UTC)
 * يحذف جميع سجلات dailyRecords الأقدم من 30 يوماً لجميع الحلقات.
 */
export const cleanupOldDailyRecords = functions.pubsub
    .schedule('0 0 * * 5')  // كل يوم جمعة منتصف الليل UTC
    .timeZone('UTC')
    .onRun(async (_context) => {
        const db = admin.database();
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        const cutoffDateStr = thirtyDaysAgo.toISOString().split('T')[0]; // yyyy-MM-dd
        
        console.log(`Starting cleanup of dailyRecords older than: ${cutoffDateStr}`);
        
        let deletedCount = 0;
        
        try {
            // جلب كل الحلقات
            const halqasSnap = await db.ref('/dailyRecords').once('value');
            if (!halqasSnap.exists()) {
                console.log('No dailyRecords to clean up.');
                return null;
            }
            
            const deletePromises: Promise<void>[] = [];
            
            halqasSnap.forEach((halqaSnap) => {
                const halqaId = halqaSnap.key;
                if (!halqaId) return;
                
                halqaSnap.forEach((dateSnap) => {
                    const dateStr = dateSnap.key;
                    if (!dateStr) return;
                    
                    // إذا كان التاريخ أقدم من 30 يوماً، احذفه
                    if (dateStr < cutoffDateStr) {
                        deletePromises.push(
                            db.ref(`/dailyRecords/${halqaId}/${dateStr}`).remove()
                        );
                        deletedCount++;
                    }
                });
            });
            
            await Promise.all(deletePromises);
            console.log(`Cleanup complete. Deleted ${deletedCount} date records.`);
            return null;
        } catch (error) {
            console.error('Error during cleanup:', error);
            return null;
        }
    });

/**
 * escalateUnconfirmedWakes — يُفعَّل تلقائياً كل 10 دقائق.
 * إذا كانت حالة العضو challenge_done ومرّت أكثر من 10 دقائق دون تأكيد الاستيقاظ،
 * تتحول الحالة تلقائياً إلى panic ويُرسَل إشعار للعضو التالي في السلسلة.
 */
export const escalateUnconfirmedWakes = functions.pubsub
    .schedule('*/10 * * * *')  // كل 10 دقائق
    .timeZone('UTC')
    .onRun(async (_context) => {
        const db = admin.database();
        const now = Date.now();
        const tenMinutesAgo = now - (10 * 60 * 1000);
        const today = new Date().toISOString().split('T')[0];
        
        // جلب كل سجلات اليوم
        const recordsSnap = await db.ref('/dailyRecords').once('value');
        if (!recordsSnap.exists()) return null;
        
        const escalatePromises: Promise<void>[] = [];
        
        recordsSnap.forEach((halqaSnap) => {
            const halqaId = halqaSnap.key!;
            const todaySnap = halqaSnap.child(today);
            
            todaySnap.forEach((userSnap) => {
                const userId = userSnap.key!;
                const status = userSnap.child('status').val();
                const challengeDoneAt = userSnap.child('challengeDoneAtMillis').val() as number;
                
                // إذا كانت الحالة challenge_done ومرّت 10 دقائق
                if (status === 'challenge_done' && challengeDoneAt && challengeDoneAt < tenMinutesAgo) {
                    escalatePromises.push((async () => {
                        // جلب بيانات الحلقة
                        const halqaSnap2 = await db.ref(`/halqas/${halqaId}`).once('value');
                        const chain = halqaSnap2.child('chain').val() as string[] || [];
                        const members = halqaSnap2.child('members').val() || {};
                        
                        const responsibleId = members[userId]?.responsibleForUserId;
                        
                        // العضو التالي بعد المسؤول
                        const responsibleIndex = chain.indexOf(responsibleId);
                        const nextIndex = (responsibleIndex + 1) % chain.length;
                        const nextMemberId = chain[nextIndex];
                        
                        if (!nextMemberId || nextMemberId === userId) return;
                        
                        // تسجيل panic تلقائي
                        await db.ref(`/dailyRecords/${halqaId}/${today}/${userId}/status`).set('panic');
                        
                        // إرسال إشعار للعضو التالي
                        const tokenSnap = await db.ref(`/users/${nextMemberId}/fcmToken`).once('value');
                        const token = tokenSnap.val();
                        if (!token) return;
                        
                        const displayName = members[userId]?.displayName || 'صديقك';
                        await admin.messaging().send({
                            token,
                            data: {
                                type: 'emergency_panic',
                                title: '⚠️ تصعيد تلقائي — مساعدة عاجلة',
                                body: `[${displayName}] حل التحدي منذ 10 دقائق لكن لم يُؤكَّد استيقاظه — ساعده الآن!`,
                                friendUid: userId,
                                friendName: displayName
                            }
                        });
                        
                        console.log(`Auto-escalated wake for user ${userId} in halqa ${halqaId}`);
                    })());
                }
            });
        });
        
        await Promise.all(escalatePromises);
        return null;
    });
