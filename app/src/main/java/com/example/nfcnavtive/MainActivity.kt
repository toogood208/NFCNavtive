package com.example.nfcnavtive

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var readerButton: Button
    private lateinit var writerButton: Button
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize UI components
        messageEditText = findViewById(R.id.message_edit_text)
        readerButton = findViewById(R.id.reader_button)
        writerButton = findViewById(R.id.writer_button)

        // Set up button click listeners
        readerButton.setOnClickListener {
            startReaderMode()
        }

        writerButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                startWriterMode(message)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startReaderMode() {
        val intent = Intent(this, NfcReaderActivity::class.java)
        startActivity(intent)
    }

    private fun startWriterMode(message: String) {
        val intent = Intent(this, CardEmulationService::class.java)
        intent.putExtra("MESSAGE", message)
        startService(intent)

        Toast.makeText(this, "Writer mode active. Bring devices close to share.", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        if (!nfcAdapter.isEnabled) {
            Toast.makeText(this, "Please enable NFC", Toast.LENGTH_LONG).show()
        }
    }
}
