package com.fluffycactus.wordperwordreader.domain

object Config {
    const val GENERAL_DELAY = 0.15  // initial delay for 100% speed and word size reference
    const val STEP_SPEED_FACTOR = 0.1  // granularity of speed factor
    const val WORD_SIZE_REFERENCE = 5  // words longer than that will be displayed for longer
    const val OFFSET_DELAY_MS = 0.15  // constant offset
    const val PUNCTUATION_DELAY = 2.0  // words ending with punctuation will take this as long
    const val JUMP_WORDS_QTY = 10 //jump that amount when required

    const val EXTRA_BOOK_URI = "extra_book_uri"
    const val EXTRA_CHAPTER_PAGES_WORDS = "extra_chapter_pages_words"
    const val EXTRA_BOOK_PATH = "extra_book_path"
}