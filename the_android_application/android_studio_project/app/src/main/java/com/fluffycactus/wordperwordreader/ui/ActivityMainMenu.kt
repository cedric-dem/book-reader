package com.fluffycactus.wordperwordreader.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.fluffycactus.wordperwordreader.R
import com.fluffycactus.wordperwordreader.domain.Config

class ActivityMainMenu : ComponentActivity() {

    private lateinit var selectedFileText: TextView

    private val openEpubPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val selectedFileName = resolveDisplayName(uri)
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val readingIntent = Intent(this, ActivityReadingBook::class.java).apply {
                putExtra(Config.EXTRA_BOOK_URI, uri.toString())
                putExtra(Config.EXTRA_BOOK_PATH, selectedFileName)
            }
            startActivity(readingIntent)
        } else {
            Toast.makeText(this, getString(R.string.epub_pick_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        val browseEpubButton = findViewById<Button>(R.id.browseEpubButton)
        val quitButton = findViewById<Button>(R.id.button_quit)
        selectedFileText = findViewById(R.id.selectedFileText)

        browseEpubButton.setOnClickListener {
            openEpubPicker.launch(arrayOf("application/epub+zip"))
        }

        quitButton.setOnClickListener {
            finishAffinity()
        }
    }

    private fun resolveDisplayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)?.trim()
                if (!displayName.isNullOrEmpty()) {
                    return displayName
                }
            }
        }

        return uri.lastPathSegment ?: uri.toString()
    }
}