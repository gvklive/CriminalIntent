package com.bignerdranch.android.criminalintent

import java.util.Date
import java.util.UUID

class Crime @JvmOverloads constructor(val id: UUID = UUID.randomUUID()) {
    var title: String? = null
    var date: Date? = null
    var isSolved: Boolean = false
    var suspect: String? = null

    val photoFilename: String
        get() = "IMG_" + id.toString() + ".jpg"

    init {
        date = Date()
    }
}
