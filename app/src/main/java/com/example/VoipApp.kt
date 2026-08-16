package com.example

import android.app.Application
import android.content.Context
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VoipApp : Application(), SignalingClient.SignalingListener {

    lateinit var signalingClient: SignalingClient
    lateinit var webRtcManager: WebRtcManager

    override fun onCreate() {
        super.onCreate()
        
        val prefs = getSharedPreferences("voip_prefs", Context.MODE_PRIVATE)
        val udcin = prefs.getString("my_udcin", null)

        if (udcin != null) {
            initializeVoip(udcin)
        }
    }

    fun initializeVoip(udcin: String) {
        signalingClient = SignalingClient(udcin, this)
        webRtcManager = WebRtcManager(this, signalingClient)
        signalingClient.connect()
    }

    override fun onOfferReceived(senderUdcin: String, sdp: SessionDescription) {
        webRtcManager.handleOffer(senderUdcin, sdp)
        
        // Handle missed calls if not answered in time, etc. For now, assume it's logged as INCOMING by Activity
    }

    override fun onAnswerReceived(sdp: SessionDescription) {
        webRtcManager.handleAnswer(sdp)
    }

    override fun onIceCandidateReceived(candidate: IceCandidate) {
        webRtcManager.handleIceCandidate(candidate)
    }

    override fun onTerminate() {
        super.onTerminate()
        webRtcManager.release()
        signalingClient.destroy()
    }
}
