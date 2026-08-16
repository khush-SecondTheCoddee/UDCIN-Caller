package com.example

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class VoipFCMService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val type = message.data["type"]
        if (type == "INCOMING_CALL") {
            val callerUdcin = message.data["callerUdcin"]
            val callId = message.data["callId"]
            Log.d("VoipFCMService", "Incoming call from: \$callerUdcin")
            
            val intent = Intent(this, IncomingCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("callerUdcin", callerUdcin)
                putExtra("callId", callId)
            }
            startActivity(intent)
        }
    }

    override fun onNewToken(token: String) {
        // Save token to Firestore /users/{udcin}/fcmToken
        Log.d("VoipFCMService", "New FCM Token: \$token")
    }
}
