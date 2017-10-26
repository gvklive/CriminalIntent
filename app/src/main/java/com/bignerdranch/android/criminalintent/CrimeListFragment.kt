package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView

class CrimeListFragment : Fragment() {

    private var mCrimeRecyclerView: RecyclerView? = null
    private var mAdapter: CrimeAdapter? = null
    private var mSubtitleVisible: Boolean = false
    private var mCallbacks: Callbacks? = null

    /**
     * Required interface for hosting activities.
     */
    interface Callbacks {
        fun onCrimeSelected(crime: Crime?)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mCallbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_crime_list, container, false)

        mCrimeRecyclerView = view
                .findViewById(R.id.crime_recycler_view) as RecyclerView
        mCrimeRecyclerView!!.layoutManager = LinearLayoutManager(activity)

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE)
        }

        updateUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible)
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.fragment_crime_list, menu)

        val subtitleItem = menu!!.findItem(R.id.show_subtitle)
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle)
        } else {
            subtitleItem.setTitle(R.string.show_subtitle)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                CrimeLab.get(activity).addCrime(crime)
                updateUI()
                mCallbacks!!.onCrimeSelected(crime)
                return true
            }
            R.id.show_subtitle -> {
                mSubtitleVisible = !mSubtitleVisible
                activity.invalidateOptionsMenu()
                updateSubtitle()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateSubtitle() {
        val crimeLab = CrimeLab.get(activity)
        val crimeCount = crimeLab.crimes.size
        var subtitle: String? = getString(R.string.subtitle_format, crimeCount)

        if (!mSubtitleVisible) {
            subtitle = null
        }

        val activity = activity as AppCompatActivity
        activity.supportActionBar!!.subtitle = subtitle
    }

    fun updateUI() {
        val crimeLab = CrimeLab.get(activity)
        val crimes = crimeLab.crimes

        if (mAdapter == null) {
            mAdapter = CrimeAdapter(crimes)
            mCrimeRecyclerView!!.adapter = mAdapter
        } else {
            mAdapter!!.setCrimes(crimes)
            mAdapter!!.notifyDataSetChanged()
        }

        updateSubtitle()
    }

    private inner class CrimeHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_crime, parent, false)), View.OnClickListener {

        private var mCrime: Crime? = null

        private val mTitleTextView: TextView
        private val mDateTextView: TextView
        private val mSolvedImageView: ImageView

        init {
            itemView.setOnClickListener(this)

            mTitleTextView = itemView.findViewById(R.id.crime_title) as TextView
            mDateTextView = itemView.findViewById(R.id.crime_date) as TextView
            mSolvedImageView = itemView.findViewById(R.id.crime_solved) as ImageView
        }

        fun bind(crime: Crime) {
            mCrime = crime
            mTitleTextView.text = mCrime!!.title
            mDateTextView.text = mCrime!!.date!!.toString()
            mSolvedImageView.visibility = if (crime.isSolved) View.VISIBLE else View.GONE
        }

        override fun onClick(view: View) {
            mCallbacks!!.onCrimeSelected(mCrime)
        }
    }

    private inner class CrimeAdapter(private var mCrimes: List<Crime>?) : RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return CrimeHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = mCrimes!![position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int {
            return mCrimes!!.size
        }

        fun setCrimes(crimes: List<Crime>) {
            mCrimes = crimes
        }
    }

    companion object {

        private val SAVED_SUBTITLE_VISIBLE = "subtitle"
    }
}
