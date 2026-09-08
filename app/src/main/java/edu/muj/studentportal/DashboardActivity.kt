package edu.muj.studentportal

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import edu.muj.studentportal.databinding.ActivityDashboardBinding

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fitRow(binding.summaryRow, 76f, 24)
        fitRow(binding.moduleRowOne, 105f, 36)
        fitRow(binding.moduleRowTwo, 105f, 36)
        fitRow(binding.moduleRowThree, 105f, 36)
        binding.attendanceButton.setOnClickListener { startActivity(Intent(this, AttendanceActivity::class.java)) }
        binding.marksButton.setOnClickListener { startActivity(Intent(this, MarksActivity::class.java)) }
        binding.timetableButton.setOnClickListener { startActivity(Intent(this, TimetableActivity::class.java)) }
        binding.coursesButton.setOnClickListener { startActivity(Intent(this, CoursesActivity::class.java)) }
        binding.noticesButton.setOnClickListener { startActivity(Intent(this, NoticesActivity::class.java)) }
        binding.serviceRequestButton.setOnClickListener { startActivity(Intent(this, ServiceRequestActivity::class.java)) }
        binding.profileButton.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.upcomingCard.setOnClickListener { startActivity(Intent(this, TimetableActivity::class.java)) }
        binding.noticeCard.setOnClickListener { startActivity(Intent(this, NoticesActivity::class.java)) }
    }

    /** Stack cards before labels become narrow enough to split individual words. */
    private fun fitRow(row: LinearLayout, textWidthDp: Float, paddingDp: Int) {
        val configuration = resources.configuration
        val availablePerCard = (configuration.screenWidthDp - 40 - (row.childCount - 1) * 12) / row.childCount
        val requiredWidth = textWidthDp * configuration.fontScale + paddingDp
        if (availablePerCard >= requiredWidth) return

        row.orientation = LinearLayout.VERTICAL
        val gap = (12 * resources.displayMetrics.density).toInt()
        for (index in 0 until row.childCount) {
            val card = row.getChildAt(index)
            val params = card.layoutParams as LinearLayout.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.weight = 0f
            params.marginEnd = 0
            if (index < row.childCount - 1) params.bottomMargin = gap
            card.layoutParams = params
        }
    }
}
