package com.fluffycactus.wordperwordreader.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlin.math.roundToInt
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.fluffycactus.wordperwordreader.domain.model.BookLocation
import com.fluffycactus.wordperwordreader.domain.model.DataManager
import com.fluffycactus.wordperwordreader.R
import com.fluffycactus.wordperwordreader.domain.Config

class ActivityReadingBook : ComponentActivity() {

    private lateinit var dataManager: DataManager;

    private var currentSpeedFactor = 1.0
    private val jumpWordsCount = Config.JUMP_WORDS_QTY

    private val autoAdvanceHandler = Handler(Looper.getMainLooper())
    private lateinit var autoAdvanceRunnable: Runnable

    private var chapterPagesWords: List<List<String>> = emptyList()
    private var totalPages = 0
    private var isPaused = false

    private lateinit var currentBookLocation: BookLocation
    private lateinit var currentBookName: String
    //private var currentPageIndex = 0
    //private var currentWordIndex: Int = 0

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

        chapterPagesWords = (intent.getSerializableExtra(Config.EXTRA_CHAPTER_PAGES_WORDS) as? ArrayList<*>)
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

        // load progression if any
        dataManager = DataManager(this)
        currentBookName = extractBookName(intent.getStringExtra(Config.EXTRA_BOOK_PATH))
        loadState()

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
            val currentPageWords = chapterPagesWords[currentBookLocation.pageIndex]
            if (currentBookLocation.wordNumber < currentPageWords.lastIndex) {
                currentBookLocation.wordNumber += 1
                updateUi()
                autoAdvanceHandler.postDelayed(autoAdvanceRunnable, getDelayMilliseconds())
            }
        }

        previousPageButton.setOnClickListener {
            if (currentBookLocation.pageIndex > 0) {
                currentBookLocation.pageIndex -= 1
                currentBookLocation.wordNumber = 0
                updateUi()
                restartAutoAdvance()
            }
        }

        nextPageButton.setOnClickListener {
            if (currentBookLocation.pageIndex < chapterPagesWords.lastIndex) {
                currentBookLocation.pageIndex += 1
                currentBookLocation.wordNumber = 0
                updateUi()
                restartAutoAdvance()
            }
        }
        rewindWordButton.setOnClickListener {
            val targetWordIndex = currentBookLocation.wordNumber - jumpWordsCount
            if (targetWordIndex >= 0) {
                currentBookLocation.wordNumber = targetWordIndex
                updateUi()
                restartAutoAdvance()
            }
        }

        jumpWordsButton.setOnClickListener {
            val currentPageWords = chapterPagesWords[currentBookLocation.pageIndex]
            val targetWordIndex = currentBookLocation.wordNumber + jumpWordsCount
            if (targetWordIndex <= currentPageWords.lastIndex) {
                currentBookLocation.wordNumber = targetWordIndex
                updateUi()
                restartAutoAdvance()
            }
        }

        playPauseButton.setOnClickListener {
            isPaused = !isPaused
            restartAutoAdvance()
        }

        decreaseSpeedButton.setOnClickListener {
            currentSpeedFactor = (currentSpeedFactor - Config.STEP_SPEED_FACTOR)
                .coerceAtLeast(Config.STEP_SPEED_FACTOR)
            updateUi()
            restartAutoAdvance()
        }

        increaseSpeedButton.setOnClickListener {
            currentSpeedFactor += Config.STEP_SPEED_FACTOR
            updateUi()
            restartAutoAdvance()
        }

        homeButton.setOnClickListener {
            saveState() //save location before going home
            finish()
        }

        updateUi()
        restartAutoAdvance()
    }

    private fun getDelayMilliseconds(): Long {
        val word = chapterPagesWords[currentBookLocation.pageIndex][currentBookLocation.wordNumber]

        val wordSizeCoefficient = word.length.toDouble() / Config.WORD_SIZE_REFERENCE

        val punctuationCoefficient = if (word.endsWith(".") or word.endsWith("?") or word.endsWith(",") or word.endsWith(":")) {
            Config.PUNCTUATION_DELAY
        } else {
            1.0
        }

        val delaySeconds = Config.OFFSET_DELAY_MS + (
                punctuationCoefficient
                        * wordSizeCoefficient
                        * Config.GENERAL_DELAY
                        * (2 - currentSpeedFactor) //Or 1/speedfactor ?
                )

        return (delaySeconds * 1000).toLong().coerceAtLeast(1L)
    }

    private fun updateUi() {
        //saveState() //save location avery refresh ? safer but more access to memory

        val currentPageWords = chapterPagesWords[currentBookLocation.pageIndex]
        val safeWordIndex = currentBookLocation.wordNumber.coerceIn(0, currentPageWords.lastIndex)
        currentBookLocation.wordNumber = safeWordIndex

        contentTextView.text = currentPageWords[safeWordIndex]
        currentPageTextView.text = getString(R.string.page_counter, currentBookLocation.pageIndex + 1, totalPages)
        currentWordTextView.text = getString(
            R.string.word_counter,
            safeWordIndex + 1,
            currentPageWords.size
        )
        val speedPercent = (currentSpeedFactor * 100).roundToInt()
        currentSpeedTextView.text = getString(R.string.speed_counter, speedPercent)
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

    private fun saveState() { //todo
        dataManager.saveValue(currentBookName, currentBookLocation.pageIndex, currentBookLocation.wordNumber)

    }

    private fun loadState() {
        val result = dataManager.getValue(currentBookName)
        val messageUser: String;

        if (result != null) {
            //Log.d("save_x", "for " + currentBookName + " found page = ${result.pageIndex}, word index = ${result.wordNumber}")
            currentBookLocation = BookLocation(result.pageIndex, result.wordNumber)
            messageUser = "continuing book " + currentBookName + " at page " + result.pageIndex.toString() + " and word " + result.wordNumber.toString()

        } else {
            //Log.d("save_x", "for " + currentBookName + " not found")
            currentBookLocation = BookLocation(0, 0)
            messageUser = "Beginning book " + currentBookName
        }

        Toast.makeText(this, messageUser, Toast.LENGTH_SHORT).show();
    }


    private fun extractBookName(bookPath: String?): String {
        val fileName = bookPath
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.substringBeforeLast('.')
            ?.trim()

        return if (fileName.isNullOrEmpty()) {
            "unknown_book"
        } else {
            fileName
        }
    }
}