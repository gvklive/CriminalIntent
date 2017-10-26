package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.util.*

class CrimeFragment : Fragment() {

    private var mCrime: Crime? = null
    private var mPhotoFile: File? = null
    private var mTitleField: EditText? = null
    private var mDateButton: Button? = null
    private var mSolvedCheckbox: CheckBox? = null
    private var mReportButton: Button? = null
    private var mSuspectButton: Button? = null
    private var mPhotoButton: ImageButton? = null
    private var mPhotoView: ImageView? = null
    private var mCallbacks: Callbacks? = null

    private val crimeReport: String
        get() {
            var solvedString: String? = null
            if (mCrime!!.isSolved) {
                solvedString = getString(R.string.crime_report_solved)
            } else {
                solvedString = getString(R.string.crime_report_unsolved)
            }
            val dateFormat = "EEE, MMM dd"
            val dateString = DateFormat.format(dateFormat, mCrime!!.date).toString()
            var suspect = mCrime!!.suspect
            if (suspect == null) {
                suspect = getString(R.string.crime_report_no_suspect)
            } else {
                suspect = getString(R.string.crime_report_suspect, suspect)
            }
            return getString(R.string.crime_report,
                    mCrime!!.title, dateString, solvedString, suspect)
        }

    /**
     * Required interface for hosting activities.
     */
    interface Callbacks {
        fun onCrimeUpdated(crime: Crime?)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mCallbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId = arguments.getSerializable(ARG_CRIME_ID) as UUID
        mCrime = CrimeLab.get(activity).getCrime(crimeId)
        mPhotoFile = CrimeLab.get(activity).getPhotoFile(this.mCrime!!)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_crime, container, false)

        mTitleField = v.findViewById(R.id.crime_title) as EditText
        mTitleField!!.setText(mCrime!!.title)
        mTitleField!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mCrime!!.title = s.toString()
                updateCrime()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        mDateButton = v.findViewById(R.id.crime_date) as Button
        updateDate()
        mDateButton!!.setOnClickListener {
            val manager = fragmentManager
            val dialog = DatePickerFragment
                    .newInstance(mCrime!!.date!!)
            dialog.setTargetFragment(this@CrimeFragment, REQUEST_DATE)
            dialog.show(manager, DIALOG_DATE)
        }

        mSolvedCheckbox = v.findViewById(R.id.crime_solved) as CheckBox
        mSolvedCheckbox!!.isChecked = mCrime!!.isSolved
        mSolvedCheckbox!!.setOnCheckedChangeListener { buttonView, isChecked ->
            mCrime!!.isSolved = isChecked
            updateCrime()
        }

        mReportButton = v.findViewById(R.id.crime_report) as Button
        mReportButton!!.setOnClickListener {
            var i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, crimeReport)
            i.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            i = Intent.createChooser(i, getString(R.string.send_report))
            startActivity(i)
        }

        val pickContact = Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI)
        mSuspectButton = v.findViewById(R.id.crime_suspect) as Button
        mSuspectButton!!.setOnClickListener { startActivityForResult(pickContact, REQUEST_CONTACT) }
        if (mCrime!!.suspect != null) {
            mSuspectButton!!.text = mCrime!!.suspect
        }

        val packageManager = activity.packageManager
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton!!.isEnabled = false
        }

        mPhotoButton = v.findViewById(R.id.crime_camera) as ImageButton
        val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null
        mPhotoButton!!.isEnabled = canTakePhoto

        mPhotoButton!!.setOnClickListener {
            val uri = FileProvider.getUriForFile(activity,
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile)
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            val cameraActivities = activity
                    .packageManager.queryIntentActivities(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)

            for (activity in cameraActivities) {
                getActivity().grantUriPermission(activity.activityInfo.packageName,
                        uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            startActivityForResult(captureImage, REQUEST_PHOTO)
        }

        mPhotoView = v.findViewById(R.id.crime_photo) as ImageView
        updatePhotoView()

        return v
    }

    override fun onPause() {
        super.onPause()

        CrimeLab.get(activity)
                .updateCrime(this.mCrime!!)
    }

    override fun onDetach() {
        super.onDetach()
        mCallbacks = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_DATE) {
            val date = data!!
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            mCrime!!.date = date
            updateCrime()
            updateDate()
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            val contactUri = data.data
            // Specify which fields you want your query to return
            // values for.
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            // Perform your query - the contactUri is like a "where"
            // clause here
            val c = activity.contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
            try {
                // Double-check that you actually got results
                if (c!!.count == 0) {
                    return
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst()
                val suspect = c.getString(0)
                mCrime!!.suspect = suspect
                updateCrime()
                mSuspectButton!!.text = suspect
            } finally {
                c!!.close()
            }
        } else if (requestCode == REQUEST_PHOTO) {
            val uri = FileProvider.getUriForFile(activity,
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile)

            activity.revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            updateCrime()
            updatePhotoView()
        }
    }

    private fun updateCrime() {
        CrimeLab.get(activity).updateCrime(this.mCrime!!)
        mCallbacks!!.onCrimeUpdated(mCrime)
    }

    private fun updateDate() {
        mDateButton!!.text = mCrime!!.date!!.toString()
    }

    private fun updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile!!.exists()) {
            mPhotoView!!.setImageDrawable(null)
            mPhotoView!!.contentDescription = getString(R.string.crime_photo_no_image_description)
        } else {
            val bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile!!.path, activity)
            mPhotoView!!.setImageBitmap(bitmap)
            mPhotoView!!.contentDescription = getString(R.string.crime_photo_image_description)
        }
    }

    companion object {

        private val ARG_CRIME_ID = "crime_id"
        private val DIALOG_DATE = "DialogDate"

        private val REQUEST_DATE = 0
        private val REQUEST_CONTACT = 1
        private val REQUEST_PHOTO = 2


        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle()
            args.putSerializable(ARG_CRIME_ID, crimeId)

            val fragment = CrimeFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
