package com.fluffycactus.wordperwordreader.domain.model

import android.content.ContentResolver
import android.net.Uri
import java.io.InputStream
import java.util.zip.ZipInputStream

fun extractChapterPagesFromEpub(contentResolver: ContentResolver, uri: Uri): List<List<String>> {
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            extractWordsByChapterPageFromStream(inputStream)
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun extractWordsByChapterPageFromStream(inputStream: InputStream): List<List<String>> {
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