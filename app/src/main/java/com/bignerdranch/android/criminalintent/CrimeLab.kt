package com.bignerdranch.android.criminalintent

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import com.bignerdranch.android.criminalintent.database.CrimeBaseHelper
import com.bignerdranch.android.criminalintent.database.CrimeCursorWrapper
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable

import java.io.File
import java.util.ArrayList
import java.util.UUID

import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable.*
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable.Cols.*

class CrimeLab private constructor(context: Context) {
    private val mContext: Context
    private val mDatabase: SQLiteDatabase

    val crimes: List<Crime>
        get() {
            val crimes = ArrayList<Crime>()
            val cursor = queryCrimes(// having
                    null  // orderBy
                    , null)
            try {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    crimes.add(cursor.crime)
                    cursor.moveToNext()
                }
            } finally {
                cursor.close()
            }
            return crimes
        }

    init {
        mContext = context.applicationContext
        mDatabase = CrimeBaseHelper(mContext)
                .writableDatabase

    }

    fun addCrime(c: Crime) {
        val values = getContentValues(c)
        mDatabase.insert(CrimeTable.NAME, null, values)
    }

    fun getCrime(id: UUID): Crime? {
        val cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                arrayOf(id.toString())
        )
        try {
            if (cursor.count == 0) {
                return null
            }
            cursor.moveToFirst()
            return cursor.crime
        } finally {
            cursor.close()
        }
    }

    fun getPhotoFile(crime: Crime): File {
        val filesDir = mContext.filesDir
        return File(filesDir, crime.photoFilename)
    }

    fun updateCrime(crime: Crime) {
        val uuidString = crime.id.toString()
        val values = getContentValues(crime)
        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                arrayOf(uuidString))
    }

    private fun queryCrimes(whereClause: String?, whereArgs: Array<String>?): CrimeCursorWrapper {
        val cursor = mDatabase.query(
                CrimeTable.NAME, null, // Columns - null selects all columns
                whereClause,
                whereArgs, null, null, null
        )// groupBy
        return CrimeCursorWrapper(cursor)
    }

    companion object {
        private var sCrimeLab: CrimeLab? = null

        operator fun get(context: Context): CrimeLab {
            if (sCrimeLab == null) {
                sCrimeLab = CrimeLab(context)
            }

            return sCrimeLab
        }

        private fun getContentValues(crime: Crime): ContentValues {
            val values = ContentValues()
            values.put(UUID, crime.id.toString())
            values.put(TITLE, crime.title)
            values.put(DATE, crime.date!!.time)
            values.put(SOLVED, if (crime.isSolved) 1 else 0)
            values.put(CrimeTable.Cols.SUSPECT, crime.suspect)

            return values
        }
    }
}
