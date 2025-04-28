package com.example.nfcnavtive

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.nio.charset.Charset

class NfcReaderActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var receivedMessageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_reader)

        receivedMessageTextView = findViewById(R.id.received_message_text_view)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        try {
            // Select our AID
            val command = "00A40400" + "07" + "F0010203040506"
            val selectApdu = hexStringToByteArray(command)
            val result = isoDep.transceive(selectApdu)

            // Check if the selection was successful
            if (result[0].toInt() and 0xff == 0x90 && result[1].toInt() and 0xff == 0x00) {
                // Now send a command to get the message
                val getDataCommand = "00CA0000FF"
                val getDataApdu = hexStringToByteArray(getDataCommand)
                val response = isoDep.transceive(getDataApdu)

                // Parse the response
                val dataLength = response.size - 2 // Exclude status word
                val data = response.copyOfRange(0, dataLength)
                val messageStr = String(data, Charset.forName("UTF-8"))

                runOnUiThread {
                    try {
                        // Try to parse as JSON
                        val jsonObject = JSONObject(messageStr)
                        receivedMessageTextView.text = "Received JSON: $jsonObject"
                    } catch (e: Exception) {
                        // If not JSON, display as string
                        receivedMessageTextView.text = "Received Message: $messageStr"
                    }
                    Toast.makeText(this, "Message received!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error reading NFC: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            try {
                isoDep.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
