package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.math.BigInteger
import java.security.MessageDigest

object UdcinGenerator {
    @SuppressLint("HardwareIds")
    fun generateBaseUdcin(context: Context, salt: Int = 0): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        val fingerprint = Build.FINGERPRINT
        val rawString = "$androidId:$fingerprint:$salt"

        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(rawString.toByteArray(Charsets.UTF_8))
        
        val first8Bytes = hashBytes.sliceArray(0..7)
        val bigInt = BigInteger(1, first8Bytes)

        val prefix = 3 + (bigInt.mod(BigInteger.valueOf(7))).toInt()
        val suffix = bigInt.divide(BigInteger.valueOf(7)).mod(BigInteger.valueOf(1_000_000_000)).toLong()

        return String.format("%d%09d", prefix, suffix)
    }
}
