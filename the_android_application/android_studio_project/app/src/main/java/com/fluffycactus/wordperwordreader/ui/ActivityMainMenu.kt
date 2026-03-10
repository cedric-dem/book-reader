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
import java.io.InputStream
import java.io.Serializable
import java.util.zip.ZipInputStream

class ActivityMainMenu : ComponentActivity() {

    private lateinit var selectedFileText: TextView

    private val openEpubPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val selectedFileName = resolveDisplayName(uri)
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val chapterPagesWords = extractChapterPagesFromEpub(uri)

            // Log.d("MainActivity","Passing data to ActivityReadingBook - chapters/pages=${chapterPagesWords.size}, firstElementSize=${chapterPagesWords.firstOrNull()?.size ?: 0}")

            val readingIntent = Intent(this, ActivityReadingBook::class.java).apply {
                putExtra(Config.EXTRA_CHAPTER_PAGES_WORDS, ArrayList(chapterPagesWords) as Serializable)
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

    private fun extractChapterPagesFromEpub(uri: Uri): List<List<String>> {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                extractWordsByChapterPageFromStream(inputStream)
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun extractWordsByChapterPageFromStream(inputStream: InputStream): List<List<String>> {
        val chapterPagesWords = mutableListOf<List<String>>()

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val entryName = entry.name.lowercase()
                if (!entry.isDirectory && (entryName.endsWith(".xhtml") || entryName.endsWith(".html") || entryName.endsWith(".htm"))) {
                    val rawText = zip.readBytes().toString(Charsets.UTF_8)
                    val cleanedText = rawText
                        .replace(Regex("<[^>]+>"), " ")
                        .replace(Regex("&(rsquo|lsquo|apos);", RegexOption.IGNORE_CASE), "'")
                        .replace(Regex("&#39;|&#x27;", RegexOption.IGNORE_CASE), "'")
                        .replace(Regex("&[a-zA-Z#0-9]+;"), " ")

                    val words = cleanedText
                        .split(Regex("\\s+"))
                        .filter { it.isNotBlank() }

                    if (words.isNotEmpty()) {
                        chapterPagesWords.add(words)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        return chapterPagesWords
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