package com.fluffycactus.wordperwordreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.io.InputStream
import java.util.zip.ZipInputStream

class MainActivity : ComponentActivity() {

    private lateinit var selectedFileText: TextView

    private val openEpubPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedFileText.text = getString(R.string.selected_epub, uri.lastPathSegment ?: uri.toString())
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val firstWords = extractFirstWordsFromEpub(uri, 100)
            val readingIntent = Intent(this, ActivityReadingBook::class.java).apply {
                putExtra(ActivityReadingBook.EXTRA_PREVIEW_TEXT, firstWords)
            }
            startActivity(readingIntent)
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

    private fun extractFirstWordsFromEpub(uri: Uri, maxWords: Int): String {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                extractWordsFromStream(inputStream, maxWords)
            } ?: getString(R.string.epub_read_error)
        } catch (_: Exception) {
            getString(R.string.epub_read_error)
        }
    }

    private fun extractWordsFromStream(inputStream: InputStream, maxWords: Int): String {
        val words = mutableListOf<String>()

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null && words.size < maxWords) {
                val entryName = entry.name.lowercase()
                if (!entry.isDirectory && (entryName.endsWith(".xhtml") || entryName.endsWith(".html") || entryName.endsWith(".htm"))) {
                    val rawText = zip.readBytes().toString(Charsets.UTF_8)
                    val cleanedText = rawText
                        .replace(Regex("<[^>]+>"), " ")
                        .replace(Regex("&[a-zA-Z#0-9]+;"), " ")

                    cleanedText
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                        .forEach { word ->
                            if (words.size < maxWords) {
                                words.add(word)
                            }
                        }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        return if (words.isEmpty()) getString(R.string.epub_no_text_found) else words.joinToString(" ")
    }
}