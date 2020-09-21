package com.hypertrack.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.models.Visit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_visit_detail.*


class VisitDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var visitDetailsViewModel: VisitDetailsViewModel

    private val visitPosition: String
        get() = intent?.getStringExtra(KEY_EXTRA_VISIT_POS)?:""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_detail)


        val visitId = intent?.getStringExtra(KEY_EXTRA_VISIT_ID)!!
        visitDetailsViewModel = (application as MyApplication).injector
            .provideVisitStatusViewModel(this.applicationContext, visitId)

        visitDetailsViewModel.visit.observe(this, { updateView(it) })

        addActionListeners()
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)
        visitDetailsViewModel.showNoteUpdatedToast.observe(this, { show ->
            if (show) Toast
                .makeText(this, "Visit note was updated", Toast.LENGTH_LONG)
                .show()
        })

    }

    override fun onMapReady(p0: GoogleMap?) {

        p0?.let { map ->
            map.uiSettings.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = true
            }
            val latLng = visitDetailsViewModel.getLatLng()?:return
            Log.d(TAG, "Got latlng $latLng")
            val label = visitDetailsViewModel.getLabel()

            map.addMarker(MarkerOptions().position(latLng).title(label))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f))
        }
    }

    private fun updateView(newValue: Visit) {
        Log.v(TAG, "updated view with value $newValue")
        tvCustomerNote.text = newValue.customerNote
        tvAddress.text = newValue.address.street
        if (newValue.visitNote != etVisitNote.text.toString()) {
            etVisitNote.setText(newValue.visitNote)
        }
        val completeEnabled = !newValue.isCompleted
        Log.d(TAG, "Complete button enabled is $completeEnabled")
        tvComplete.isEnabled = completeEnabled
        etVisitNote.isEnabled = completeEnabled
        tvComplete.background = ContextCompat.getDrawable(this,
            if (completeEnabled) R.drawable.bg_button
            else R.drawable.bg_button_disabled
        )
        tvComplete.text = if (completeEnabled)
            getString(R.string.check_out)
        else getString(R.string.completed)

        when(newValue.tripVisitPickedUp) {
            null -> tvPickup.visibility = View.GONE
            false -> {
                tvPickup.visibility = View.VISIBLE
                tvPickup.text = getText(R.string.pick_up)
                tvPickup.isEnabled = true
            }
            true -> {
                tvPickup.visibility = View.VISIBLE
                tvPickup.text = getText(R.string.cancel)
                tvPickup.isEnabled = completeEnabled
                tvPickup.background = ContextCompat.getDrawable(this,
                    if (completeEnabled) R.drawable.bg_button
                    else R.drawable.bg_button_disabled
                )
            }
        }

    }

    private fun addActionListeners() {
        ivBack.setOnClickListener {
            visitDetailsViewModel.onBackPressed()
            onBackPressed()
        }
        tvComplete.setOnClickListener {
            Log.d(TAG, "Complete button pressed")
            tvComplete.isEnabled = false
            etVisitNote.isEnabled = false
            visitDetailsViewModel.onMarkedCompleted(true)
        }
        tvPickup.setOnClickListener {
            Log.d(TAG, "Pickup/Cancel pressed")
            tvPickup.isEnabled = false
            visitDetailsViewModel.onPickupClicked()
        }

        etVisitNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) =
                visitDetailsViewModel.onVisitNoteChanged(s.toString())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }


    override fun onBackPressed() {
        val data = Intent()
        data.data = Uri.parse(visitPosition)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {const val TAG = "VisitDetailsActivity"}

}


