package edu.muj.studentportal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout

/** Keeps Material's native validation while centering captions and fitting long input. */
class CenteredTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {
    init {
        addOnEditTextAttachedListener { input ->
            input.editText?.apply {
                // Some keyboard input types force horizontal scrolling after XML inflation.
                val originalTransformation = transformationMethod
                val originalMinimumLines = minLines.coerceAtLeast(1)
                setSingleLine(false)
                setHorizontallyScrolling(false)
                minLines = originalMinimumLines
                transformationMethod = originalTransformation // Preserve password masking.
            }
        }
    }

    override fun setError(errorText: CharSequence?) {
        super.setError(errorText)
        centerCaption(com.google.android.material.R.id.textinput_error)
    }

    override fun setHelperText(helperText: CharSequence?) {
        super.setHelperText(helperText)
        centerCaption(com.google.android.material.R.id.textinput_helper_text)
    }

    private fun centerCaption(id: Int) {
        findViewById<TextView>(id)?.apply {
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
    }
}
