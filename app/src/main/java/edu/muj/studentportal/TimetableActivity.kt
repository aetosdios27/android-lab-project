package edu.muj.studentportal

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import edu.muj.studentportal.adapter.TimetableAdapter
import edu.muj.studentportal.data.MockData
import edu.muj.studentportal.databinding.ActivityTimetableBinding

class TimetableActivity : BaseActivity() {
    private lateinit var binding: ActivityTimetableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.academic_timetable)
        configureToolbar(binding.toolbar)
        val timetableAdapter = TimetableAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = timetableAdapter
        binding.daySpinner.adapter = ArrayAdapter(this, R.layout.item_spinner_selection, MockData.weekdays).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        val selectedDay = (savedInstanceState?.getInt(SELECTED_DAY, 0) ?: 0).coerceIn(MockData.weekdays.indices)
        binding.daySpinner.setSelection(selectedDay)
        var displayedDay = selectedDay
        timetableAdapter.showDay(MockData.timetable.getValue(MockData.weekdays[selectedDay]))
        binding.daySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != displayedDay) {
                    displayedDay = position
                    timetableAdapter.showDay(MockData.timetable.getValue(MockData.weekdays[position]))
                    binding.recyclerView.scrollToPosition(0)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::binding.isInitialized) outState.putInt(SELECTED_DAY, binding.daySpinner.selectedItemPosition)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val SELECTED_DAY = "selected_weekday"
    }
}
