package com.fluffycactus.wordperwordreader

object ReaderConfig {
    const val GENERAL_DELAY = 0.15  // initial delay for 100% speed and word size reference
    const val STEP_PERCENTAGE = 10  // percentage granularity of speed setting
    const val WORD_SIZE_REFERENCE = 5  // words longer than that will be displayed for longer
    const val OFFSET_DELAY_MS = 0.15  // constant offset
    const val PUNCTUATION_DELAY = 2.0  // words ending with punctuation will take this as long
    const val JUMP_WORDS_QTY = 10 //jump that amount when required
}