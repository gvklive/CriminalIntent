package com.bignerdranch.android.criminalintent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import java.util.*

class CrimePagerActivity : AppCompatActivity(), CrimeFragment.Callbacks {

    private var mViewPager: ViewPager? = null
    private var mCrimes: List<Crime>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime_pager)

        val crimeId = intent
                .getSerializableExtra(EXTRA_CRIME_ID) as UUID

        mViewPager = findViewById(R.id.crime_view_pager) as ViewPager

        mCrimes = CrimeLab.get(this).crimes
        val fragmentManager = supportFragmentManager
        mViewPager!!.adapter = object : FragmentStatePagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {
                val crime = mCrimes!![position]
                return CrimeFragment.newInstance(crime.id)
            }

            override fun getCount(): Int {
                return mCrimes!!.size
            }
        }

        for (i in mCrimes!!.indices) {
            if (mCrimes!![i].id == crimeId) {
                mViewPager!!.currentItem = i
                break
            }
        }
    }

    override fun onCrimeUpdated(crime: Crime?) {

    }

    companion object {

        private val EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id"

        fun newIntent(packageContext: Context, crimeId: UUID): Intent {
            val intent = Intent(packageContext, CrimePagerActivity::class.java)
            intent.putExtra(EXTRA_CRIME_ID, crimeId)
            return intent
        }
    }
}
