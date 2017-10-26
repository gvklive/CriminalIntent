package com.bignerdranch.android.criminalintent.database

import android.database.Cursor
import android.database.CursorWrapper
import com.bignerdranch.android.criminalintent.Crime
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable
import java.util.*

class CrimeCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {

    val crime: Crime
        get() {
            val uuidString = getString(getColumnIndex(Cols.UUID))
            val title = getString(getColumnIndex(Cols.TITLE))
            val date = getLong(getColumnIndex(Cols.DATE))
            val isSolved = getInt(getColumnIndex(Cols.SOLVED))
            val suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT))

            val crime = Crime(UUID.fromString(uuidString))
            crime.title = title
            crime.date = Date(date)
            crime.isSolved = isSolved != 0
            crime.suspect = suspect

            return crime
        }
}
