package edu.muj.studentportal

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.muj.studentportal.adapter.NoticeAdapter
import edu.muj.studentportal.data.MockData
import edu.muj.studentportal.databinding.ActivityNoticesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NoticesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        val binding = ActivityNoticesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setTitle(R.string.academic_notices)
        configureToolbar(binding.toolbar)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = NoticeAdapter(MockData.notices) { notice ->
            MaterialAlertDialogBuilder(this)
                .setTitle(notice.title)
                .setMessage(getString(R.string.academic_notice_meta, notice.date, notice.category) + "\n\n" + notice.description)
                .setPositiveButton(R.string.academic_close, null)
                .show()
        }
    }
}
