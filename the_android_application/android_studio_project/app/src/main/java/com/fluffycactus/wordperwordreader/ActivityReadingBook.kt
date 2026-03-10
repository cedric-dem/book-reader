package com.fluffycactus.wordperwordreader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ActivityReadingBook : ComponentActivity() {
    private var delayPercentage = 100
    private val jumpWordsCount = ReaderConfig.JUMP_WORDS_QTY

    private val autoAdvanceHandler = Handler(Looper.getMainLooper())
    private lateinit var autoAdvanceRunnable: Runnable

    private var chapterPagesWords: List<List<String>> = emptyList()
    private var totalPages = 0
    private var currentPageIndex = 0
    private var isPaused = false

    private var currentWordIndex: Int = 0

    private lateinit var contentTextView: TextView
    private lateinit var currentPageTextView: TextView
    private lateinit var currentWordTextView: TextView
    private lateinit var currentSpeedTextView: TextView
    private lateinit var previousPageButton: Button
    private lateinit var nextPageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reading_book)

        chapterPagesWords = (intent.getSerializableExtra(EXTRA_CHAPTER_PAGES_WORDS) as? ArrayList<*>)
            ?.mapNotNull { page ->
                (page as? List<*>)?.mapNotNull { it as? String }
            }
            .orEmpty()

        contentTextView = findViewById(R.id.text_book_content)
        currentPageTextView = findViewById(R.id.text_current_page)
        currentWordTextView = findViewById(R.id.text_current_words)
        previousPageButton = findViewById(R.id.button_previous_page)
        nextPageButton = findViewById(R.id.button_next_page)
        val homeButton = findViewById<Button>(R.id.button_go_home)
        val rewindWordButton = findViewById<Button>(R.id.button_rewind_word)
        val jumpWordsButton = findViewById<Button>(R.id.button_jump_words)
        val playPauseButton = findViewById<Button>(R.id.button_play_pause)
        val decreaseSpeedButton = findViewById<Button>(R.id.button_decrease_speed)
        val increaseSpeedButton = findViewById<Button>(R.id.button_increase_speed)
        currentSpeedTextView = findViewById(R.id.text_current_speed)

        totalPages = chapterPagesWords.size

        if (chapterPagesWords.isEmpty()) {
            contentTextView.text = getString(R.string.no_preview_available)
            currentPageTextView.text = getString(R.string.page_counter, 0, 0)
            currentWordTextView.text = getString(R.string.word_counter, 0, 0)
            previousPageButton.isEnabled = false
            nextPageButton.isEnabled = false
            return
        }

        autoAdvanceRunnable = Runnable {
            val currentPageWords = chapterPagesWords[currentPageIndex]
            if (currentWordIndex < currentPageWords.lastIndex) {
                currentWordIndex += 1
                updateUi()
                autoAdvanceHandler.postDelayed(autoAdvanceRunnable, getDelayMilliseconds())
            }
        }

        previousPageButton.setOnClickListener {
            if (currentPageIndex > 0) {
                currentPageIndex -= 1
                currentWordIndex = 0
                updateUi()
                restartAutoAdvance()
            }
        }

        nextPageButton.setOnClickListener {
            if (currentPageIndex < chapterPagesWords.lastIndex) {
                currentPageIndex += 1
                currentWordIndex = 0
                updateUi()
                restartAutoAdvance()
            }
        }
        rewindWordButton.setOnClickListener {
            val targetWordIndex = currentWordIndex - jumpWordsCount
            if (targetWordIndex >= 0) {
                currentWordIndex = targetWordIndex
                updateUi()
                restartAutoAdvance()
            }
        }

        jumpWordsButton.setOnClickListener {
            val currentPageWords = chapterPagesWords[currentPageIndex]
            val targetWordIndex = currentWordIndex + jumpWordsCount
            if (targetWordIndex <= currentPageWords.lastIndex) {
                currentWordIndex = targetWordIndex
                updateUi()
                restartAutoAdvance()
            }
        }

        playPauseButton.setOnClickListener {
            isPaused = !isPaused
            restartAutoAdvance()
        }

        decreaseSpeedButton.setOnClickListener {
            delayPercentage = (delayPercentage - ReaderConfig.STEP_PERCENTAGE)
                .coerceAtLeast(ReaderConfig.STEP_PERCENTAGE)
            updateUi()
            restartAutoAdvance()
        }

        increaseSpeedButton.setOnClickListener {
            delayPercentage += ReaderConfig.STEP_PERCENTAGE
            updateUi()
            restartAutoAdvance()
        }

        homeButton.setOnClickListener {
            finish()
        }

        updateUi()
        restartAutoAdvance()
    }

    private fun getDelayMilliseconds(): Long {
        val word = chapterPagesWords[currentPageIndex][currentWordIndex]

        val wordSizeCoefficient = word.length.toDouble() / ReaderConfig.WORD_SIZE_REFERENCE

        val punctuationCoefficient = if (word.endsWith(".") or word.endsWith("?") or word.endsWith(",") or word.endsWith(":")) {
            ReaderConfig.PUNCTUATION_DELAY
        } else {
            1.0
        }

        val delaySeconds = ReaderConfig.OFFSET_DELAY_MS + (
                punctuationCoefficient
                        * wordSizeCoefficient
                        * ReaderConfig.GENERAL_DELAY
                        * (delayPercentage / 100.0)
                )

        return (delaySeconds * 1000).toLong().coerceAtLeast(1L)
    }

    private fun updateUi() {
        val currentPageWords = chapterPagesWords[currentPageIndex]
        val safeWordIndex = currentWordIndex.coerceIn(0, currentPageWords.lastIndex)
        currentWordIndex = safeWordIndex

        contentTextView.text = currentPageWords[safeWordIndex]
        currentPageTextView.text = getString(R.string.page_counter, currentPageIndex + 1, totalPages)
        currentWordTextView.text = getString(
            R.string.word_counter,
            safeWordIndex + 1,
            currentPageWords.size
        )
        currentSpeedTextView.text = getString(R.string.speed_counter, delayPercentage)
    }

    private fun restartAutoAdvance() {
        autoAdvanceHandler.removeCallbacks(autoAdvanceRunnable)
        if (!isPaused) {
            autoAdvanceHandler.postDelayed(autoAdvanceRunnable, getDelayMilliseconds())
        }
    }

    override fun onDestroy() {
        autoAdvanceHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CHAPTER_PAGES_WORDS = "extra_chapter_pages_words"
    }
}
