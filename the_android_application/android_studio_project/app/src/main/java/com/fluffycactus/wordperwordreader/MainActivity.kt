package com.fluffycactus.wordperwordreader

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private lateinit var selectedFileText: TextView

    private val openEpubPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedFileText.text = getString(R.string.selected_epub, uri.lastPathSegment ?: uri.toString())
            contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } else {
            Toast.makeText(this, getString(R.string.epub_pick_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val browseEpubButton = findViewById<Button>(R.id.browseEpubButton)
        selectedFileText = findViewById(R.id.selectedFileText)

        browseEpubButton.setOnClickListener {
            openEpubPicker.launch(arrayOf("application/epub+zip"))
        }
    }
}