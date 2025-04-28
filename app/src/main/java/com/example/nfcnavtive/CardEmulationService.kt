package com.example.nfcnavtive

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.Charset

class CardEmulationService : HostApduService() {

    private var message: String = ""

    companion object {
        private const val TAG = "CardEmulationService"
        private const val AID = "F0010203040506"
        private const val SELECT_APDU_HEADER = "00A40400"
        private const val GET_DATA_APDU_HEADER = "00CA0000"
        private const val STATUS_SUCCESS = "9000"
        private const val STATUS_FAILED = "6F00"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.hasExtra("MESSAGE")) {
            message = intent.getStringExtra("MESSAGE") ?: ""
            Log.d(TAG, "Service started with message: $message")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val hexCommandApdu = toHex(commandApdu)
        Log.d(TAG, "Received APDU: $hexCommandApdu")

        // Check if it's a SELECT command for our AID
        if (hexCommandApdu.startsWith(SELECT_APDU_HEADER)) {
            if (hexCommandApdu.substring(10, 10 + AID.length) == AID) {
                Log.d(TAG, "AID selected")
                return hexStringToByteArray(STATUS_SUCCESS)
            }
        }

        // Check if it's a GET DATA command
        else if (hexCommandApdu.startsWith(GET_DATA_APDU_HEADER)) {
            Log.d(TAG, "GET DATA command received")
            val messageBytes = message.toByteArray(Charset.forName("UTF-8"))
            val response = ByteArray(messageBytes.size + 2)
            System.arraycopy(messageBytes, 0, response, 0, messageBytes.size)

            // Append success status
            val statusBytes = hexStringToByteArray(STATUS_SUCCESS)
            System.arraycopy(statusBytes, 0, response, messageBytes.size, 2)

            return response
        }

        // Unknown command
        return hexStringToByteArray(STATUS_FAILED)
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }

    private fun toHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) +
                    Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
