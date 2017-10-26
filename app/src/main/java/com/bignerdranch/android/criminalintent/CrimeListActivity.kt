package com.bignerdranch.android.criminalintent

import android.content.Intent
import android.support.v4.app.Fragment

class CrimeListActivity : SingleFragmentActivity(), CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    protected override val layoutResId: Int
        get() = R.layout.activity_masterdetail

    override fun createFragment(): Fragment {
        return CrimeListFragment()
    }

    override fun onCrimeSelected(crime: Crime?) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            val intent = CrimePagerActivity.newIntent(this, crime!!.id)
            startActivity(intent)
        } else {
            val newDetail = CrimeFragment.newInstance(crime!!.id)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit()
        }
    }

    override fun onCrimeUpdated(crime: Crime?) {
        val listFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container) as CrimeListFragment
        listFragment.updateUI()
    }
}
