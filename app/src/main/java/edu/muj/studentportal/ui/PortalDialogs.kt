package edu.muj.studentportal.ui

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

/** The native alert title is capped at two lines, which clips large accessibility text. */
fun AlertDialog.fitTitle() {
    findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.apply {
        setSingleLine(false)
        maxLines = Int.MAX_VALUE
        ellipsize = null
        gravity = Gravity.CENTER
        textAlignment = View.TEXT_ALIGNMENT_CENTER
    }
}
