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

        const tokens: string[] = [];
        for (const memberId of Object.keys(members)) {
            if (memberId === userId) continue; // تخطي المستغيث نفسه

            const tokenSnap = await admin.database().ref(`/users/${memberId}/fcmToken`).once('value');
            const token = tokenSnap.val();
            if (token) {
                tokens.push(token);
            }
        }

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
        const userId = context.params.userId;

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

        const tokens: string[] = [];
        for (const memberId of Object.keys(members)) {
            if (memberId === senderId) continue; // تخطي المرسل

            const tokenSnap = await admin.database().ref(`/users/${memberId}/fcmToken`).once('value');
            const token = tokenSnap.val();
            if (token) {
                tokens.push(token);
            }
        }

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
