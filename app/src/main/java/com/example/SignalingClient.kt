package com.example

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.concurrent.TimeUnit

class SignalingClient(private val myUdcin: String, private val listener: SignalingListener) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private var currentTarget: String? = null

    // Replace with your server URL, for local testing it would be ws://<ip>:8080
    // If you deployed Node.js to cloud, put the real URL here.
    private val serverUrl = "ws://10.0.2.2:8080" 

    fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val msg = mapOf("type" to "register", "udcin" to myUdcin)
                webSocket.send(gson.toJson(msg))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("SignalingClient", "Received: $text")
                try {
                    val data = gson.fromJson(text, Map::class.java)
                    when (data["type"]) {
                        "offer" -> {
                            currentTarget = data["senderUdcin"] as String
                            val sdp = SessionDescription(
                                SessionDescription.Type.OFFER,
                                data["sdp"] as String
                            )
                            listener.onOfferReceived(currentTarget!!, sdp)
                        }
                        "answer" -> {
                            val sdp = SessionDescription(
                                SessionDescription.Type.ANSWER,
                                data["sdp"] as String
                            )
                            listener.onAnswerReceived(sdp)
                        }
                        "candidate" -> {
                            val candidate = IceCandidate(
                                data["sdpMid"] as String,
                                (data["sdpMLineIndex"] as Double).toInt(),
                                data["candidate"] as String
                            )
                            listener.onIceCandidateReceived(candidate)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SignalingClient", "Error parsing message", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("SignalingClient", "WebSocket Error", t)
            }
        })
    }

    fun sendOffer(targetUdcin: String, sdp: SessionDescription) {
        currentTarget = targetUdcin
        val msg = mapOf(
            "type" to "offer",
            "targetUdcin" to targetUdcin,
            "sdp" to sdp.description
        )
        webSocket?.send(gson.toJson(msg))
    }

    fun sendAnswer(targetUdcin: String, sdp: SessionDescription) {
        val msg = mapOf(
            "type" to "answer",
            "targetUdcin" to targetUdcin,
            "sdp" to sdp.description
        )
        webSocket?.send(gson.toJson(msg))
    }

    fun sendIceCandidate(candidate: IceCandidate) {
        currentTarget?.let {
            val msg = mapOf(
                "type" to "candidate",
                "targetUdcin" to it,
                "sdpMid" to candidate.sdpMid,
                "sdpMLineIndex" to candidate.sdpMLineIndex,
                "candidate" to candidate.sdp
            )
            webSocket?.send(gson.toJson(msg))
        }
    }

    fun destroy() {
        webSocket?.close(1000, "App closed")
    }

    interface SignalingListener {
        fun onOfferReceived(senderUdcin: String, sdp: SessionDescription)
        fun onAnswerReceived(sdp: SessionDescription)
        fun onIceCandidateReceived(candidate: IceCandidate)
    }
}
