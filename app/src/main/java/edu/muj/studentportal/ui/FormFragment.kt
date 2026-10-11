package edu.muj.studentportal.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.muj.studentportal.MainActivity
import edu.muj.studentportal.R
import edu.muj.studentportal.data.PortalSnapshot
import edu.muj.studentportal.databinding.FragmentFormBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/** One form host with separate builders for each concrete workflow. Drafts survive recreation. */
class FormFragment : Fragment(R.layout.fragment_form) {
    private val host get() = requireActivity() as MainActivity
    private val model get() = host.model
    private val kind get() = requireArguments().getString("kind")!!
    private val courseId get() = requireArguments().getLong("course")
    private val recordId get() = requireArguments().getLong("record")
    private var binding: FragmentFormBinding? = null
    private val inputs = linkedMapOf<String, TextInputLayout>()
    private val selections = linkedMapOf<String, Spinner>()
    private val checks = linkedMapOf<String, MaterialCheckBox>()
    private val radios = linkedMapOf<String, RadioGroup>()
    private val buttons = mutableListOf<MaterialButton>()
    private var draft = Bundle()
    private var baseline = Bundle()
    private var built = false
    private var pendingCompletion = 0L
    private var finished = false
    private var sessionDate = dateKey()
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentFormBinding.bind(view)
        draft = savedInstanceState?.getBundle("draft") ?: Bundle()
        baseline = savedInstanceState?.getBundle("baseline") ?: Bundle()
        pendingCompletion = savedInstanceState?.getLong("pending") ?: 0
        sessionDate = draft.getString("date") ?: dateKey()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    if (finished) return@collect
                    if (pendingCompletion > 0 && state.completedAction >= pendingCompletion) {
                        finished = true
                        pendingCompletion = 0
                        host.supportFragmentManager.popBackStack()
                        return@collect
                    }
                    if (state.snapshot != null && !built) {
                        built = true
                        build(state.snapshot)
                        if (baseline.isEmpty) baseline = capture()
                        restoreDraft()
                    }
                    buttons.forEach { it.isEnabled = !state.busy }
                    inputs.values.forEach { it.isEnabled = !state.busy }
                    selections.values.forEach { it.isEnabled = !state.busy && it.tag != "locked" }
                    radios.values.forEach { group -> for (index in 0 until group.childCount) group.getChildAt(index).isEnabled = !state.busy }
                    checks.values.forEach { it.isEnabled = !state.busy }
                }
            }
        }
        view.post {
            val fields = binding?.fields ?: return@post
            val max = view.dp(640)
            if (fields.width > max) fields.layoutParams = fields.layoutParams.apply { width = max }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle("draft", if (built) capture() else draft)
        outState.putBundle("baseline", baseline); outState.putLong("pending", pendingCompletion)
        super.onSaveInstanceState(outState)
    }
    fun hasChanges(): Boolean = built && capture().let { current -> current.keySet().any { current.getString(it) != baseline.getString(it) } }
    private fun capture() = Bundle().apply {
        inputs.forEach { (key, field) -> putString(key, field.editText?.text?.toString().orEmpty()) }
        selections.forEach { (key, select) -> putString(key, select.selectedItemPosition.toString()) }
        checks.forEach { (key, check) -> putString(key, check.isChecked.toString()) }
        radios.forEach { (key, group) -> putString(key, (0 until group.childCount).indexOfFirst { group.getChildAt(it).id == group.checkedRadioButtonId }.toString()) }
        putString("date", sessionDate)
    }
    private fun restoreDraft() {
        inputs.forEach { (key, field) -> draft.getString(key)?.let { field.editText?.setText(it) } }
        selections.forEach { (key, select) -> draft.getString(key)?.toIntOrNull()?.takeIf { it in 0 until select.count }?.let { select.setSelection(it) } }
        checks.forEach { (key, check) -> draft.getString(key)?.let { check.isChecked = it == "true" } }
        radios.forEach { (key, group) -> draft.getString(key)?.toIntOrNull()?.let { index ->
            if (index in 0 until group.childCount) group.check(group.getChildAt(index).id) else group.clearCheck()
        } }
    }
    private fun build(s: PortalSnapshot) {
        val permitted = when (kind) {
            "attendance", "marks" -> s.user.isTeacher && s.courses.any { it.id == courseId }
            "notice" -> s.user.isTeacher
            "request" -> !s.user.isTeacher
            "reply" -> s.user.isTeacher && s.requests.any { it.id == recordId }
            "profile" -> true
            else -> false
        }
        if (!permitted) { text("This action is unavailable for your account."); return }
        host.title(when (kind) {
            "attendance" -> "Attendance"; "marks" -> "Internal marks"; "notice" -> "Announcement"
            "request" -> if (recordId == 0L) "New request" else "Edit request"; "reply" -> "Reply"; else -> "Contact details"
        })
        when (kind) { "attendance" -> attendance(s); "marks" -> marks(s); "notice" -> notice(s); "request" -> request(s); "reply" -> reply(s); "profile" -> profile(s) }
    }
    private fun text(value: String, title: Boolean = false) {
        val view = TextView(requireContext()).apply {
            setTextAppearance(if (title) com.google.android.material.R.style.TextAppearance_Material3_TitleLarge else R.style.PortalBody)
            text = value
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(if (title) 8 else 20) }
        }
        binding!!.fields.addView(view)
    }
    private fun field(key: String, label: String, value: String = "", multiline: Boolean = false,
        inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
        maximum: Int = 120): TextInputLayout {
        val layout = layoutInflater.inflate(R.layout.form_field, binding!!.fields, false) as TextInputLayout
        layout.hint = label
        val edit = layout.getChildAt(0).let { layout.editText ?: it as? TextInputEditText } ?: error("Missing input")
        edit.id = View.generateViewId(); edit.tag = key
        edit.inputType = inputType or if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0
        edit.isSingleLine = !multiline
        if (multiline) { edit.minLines = 4; edit.gravity = android.view.Gravity.TOP or android.view.Gravity.START }
        edit.filters = arrayOf(InputFilter.LengthFilter(maximum)); edit.setText(value)
        binding!!.fields.addView(layout); inputs[key] = layout
        return layout
    }
    private fun input(key: String) = inputs.getValue(key).editText?.text?.toString()?.trim().orEmpty()
    private fun validate(key: String, min: Int, max: Int, message: String): Boolean {
        val field = inputs.getValue(key)
        field.error = if (input(key).length !in min..max) message else null
        if (field.error != null) field.editText?.requestFocus()
        return field.error == null
    }
    private fun select(key: String, label: String, options: List<String>, selected: Int = 0, enabled: Boolean = true): Spinner {
        val caption = TextView(requireContext()).apply { text = label; setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) } }
        binding!!.fields.addView(caption)
        val spinner = Spinner(requireContext()).apply {
            id = View.generateViewId(); contentDescription = label
            adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selection, options).also { it.setDropDownViewResource(R.layout.item_spinner_dropdown) }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
            minimumHeight = dp(56)
            setSelection(selected.coerceIn(0, maxOf(0, options.lastIndex))); isEnabled = enabled
            setBackgroundResource(R.drawable.bg_select)
        }
        binding!!.fields.addView(spinner); selections[key] = spinner
        // Immutable course selections remain disabled even after a busy-state update.
        spinner.tag = if (enabled) "editable" else "locked"
        return spinner
    }
    private fun radio(key: String, label: String, options: List<String>, initial: Int = -1): RadioGroup {
        text(label)
        val group = RadioGroup(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        options.forEach { option -> group.addView(MaterialRadioButton(requireContext()).apply {
            id = View.generateViewId(); text = option; minHeight = dp(48)
        }) }
        if (initial >= 0) group.check(group.getChildAt(initial).id)
        group.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
        binding!!.fields.addView(group); radios[key] = group
        return group
    }
    private fun button(label: String, secondary: Boolean = false, action: () -> Unit): MaterialButton {
        val button = MaterialButton(requireContext()).apply {
            text = label; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
            setOnClickListener { action() }
        }
        if (secondary) {
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            button.setTextColor(resources.getColor(R.color.primary, null))
            button.strokeWidth = dp(1)
            button.strokeColor = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.outline, null))
        }
        buttons.add(button); binding!!.fields.addView(button); return button
    }
    private fun runAction(message: String, action: suspend edu.muj.studentportal.data.PortalRepository.() -> Unit) {
        if (finished || model.state.value.busy) return
        pendingCompletion = model.state.value.completedAction + 1
        model.perform(message, action)
    }
    private fun selectedCourse(s: PortalSnapshot): Long = s.courses[selections.getValue("course").selectedItemPosition].id
    private fun courseSelect(s: PortalSnapshot, preselected: Long, enabled: Boolean = true) {
        select("course", "Course", s.courses.map { "${it.code} · ${it.name}" }, s.courses.indexOfFirst { it.id == preselected }.coerceAtLeast(0), enabled)
    }
    private fun request(s: PortalSnapshot) {
        val existing = s.requests.firstOrNull { it.id == recordId }
        if (recordId != 0L && existing == null) { text("This request is no longer available."); return }
        if (s.courses.isEmpty()) { text("You need an enrolled course to submit a request."); return }
        text("Request details", true)
        text("Choose a course and describe the issue for its teacher.")
        courseSelect(s, existing?.courseId ?: courseId, enabled = existing == null)
        val categories = listOf("Attendance", "Marks", "Coursework", "Other")
        select("category", "What is this about?", categories, categories.indexOf(existing?.category).coerceAtLeast(0))
        field("subject", "Subject", existing?.subject.orEmpty())
        field("description", "Describe what you need help with", existing?.description.orEmpty(), multiline = true, maximum = 3000)
        radio("priority", "Priority", listOf("Normal", "Urgent"), if (existing?.priority == "Urgent") 1 else 0)
        text("Use urgent for a time-sensitive issue. Requests can be edited or deleted until the teacher begins review.")
        button(if (existing == null) "Submit request" else "Save request") {
            val a = validate("subject", 3, 120, "Enter a subject of at least 3 characters")
            val b = validate("description", 10, 3000, "Include at least 10 characters of detail")
            if (a && b) {
                val priority = if (radios.getValue("priority").checkedRadioButtonId == radios.getValue("priority").getChildAt(1).id) "Urgent" else "Normal"
                val course = selectedCourse(s); val subject = input("subject"); val description = input("description")
                val category = categories[selections.getValue("category").selectedItemPosition]
                runAction("Request saved") { saveRequest(model.userId, course, subject, description, category, priority, recordId) }
            }
        }
    }
    private fun notice(s: PortalSnapshot) {
        val existing = s.notices.firstOrNull { it.id == recordId }
        if (recordId != 0L && existing == null) { text("This announcement is no longer available."); return }
        if (s.courses.isEmpty()) { text("You need an assigned class to publish an announcement."); return }
        text("Announcement to your class", true); text("Only students enrolled in the selected course will see this notice.")
        courseSelect(s, existing?.courseId ?: courseId, enabled = existing == null)
        field("title", "Title", existing?.title.orEmpty())
        field("body", "Announcement", existing?.body.orEmpty(), multiline = true, maximum = 3000)
        button(if (existing == null) "Publish announcement" else "Save announcement") {
            val a = validate("title", 3, 120, "Enter a title of at least 3 characters")
            val b = validate("body", 10, 3000, "Include at least 10 characters of detail")
            if (a && b) {
                val course = selectedCourse(s); val title = input("title"); val body = input("body")
                runAction("Announcement published") { saveNotice(model.userId, course, title, body, recordId) }
            }
        }
    }
    private fun profile(s: PortalSnapshot) {
        text("Your contact details", true); text("Account identity is fixed for this demo. You can update your contact information.")
        field("phone", "Phone number (optional)", s.user.phone, inputType = InputType.TYPE_CLASS_PHONE, maximum = 30)
        field("email", "Email address", s.user.email, inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, maximum = 254)
        button("Save contact details") {
            val phone = input("phone"); val email = input("email")
            inputs.getValue("phone").error = if (phone.isNotEmpty() && !(phone.count { it in '0'..'9' } in 10..15 && phone.matches(Regex("\\+?[0-9][0-9 ()-]*")))) "Enter 10–15 digits, or leave blank" else null
            inputs.getValue("email").error = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Enter a valid email address" else null
            if (inputs.values.none { it.error != null }) runAction("Contact details saved") { saveProfile(model.userId, phone, email) }
        }
    }
    private fun reply(s: PortalSnapshot) {
        val request = s.requests.first { it.id == recordId }
        text(request.subject, true); text("${s.person(request.studentId).name}\n${s.course(request.courseId).name}\n\n${request.description}")
        field("reply", "Reply to the student", multiline = true, maximum = 3000)
        val states = listOf("In review", "Resolved")
        select("status", "Status after your reply", states, if (request.status == "Resolved") 1 else 0)
        button("Send reply") {
            if (validate("reply", 3, 3000, "Enter a reply of at least 3 characters")) {
                val message = input("reply"); val status = states[selections.getValue("status").selectedItemPosition]
                runAction("Reply saved") { respond(model.userId, recordId, message, status) }
            }
        }
    }
    private fun attendance(s: PortalSnapshot) {
        val course = s.course(courseId)
        text(course.name, true); text("Choose a session date. Existing attendance records can be corrected.")
        val register = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        val dateButton = button("Date · ${dateLabel(sessionDate)}", secondary = true) { }
        fun loadRegister() {
            register.removeAllViews()
            radios.keys.filter { it.startsWith("student") }.toList().forEach { radios.remove(it) }
            val session = s.sessions.firstOrNull { it.courseId == courseId && it.date == sessionDate }
            val hint = TextView(requireContext()).apply {
                text = if (session == null) "New session · Mark every student" else "Saved session · Edit attendance"
                setTextAppearance(R.style.PortalBody); setPadding(0, 0, 0, dp(16))
            }
            register.addView(hint)
            s.roster(courseId).forEach { student ->
                val title = TextView(requireContext()).apply { text = "${student.name}\n${student.login}"; setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium) }
                register.addView(title)
                val vertical = resources.configuration.screenWidthDp < 360 || resources.configuration.fontScale > 1.2f
                val group = RadioGroup(requireContext()).apply { orientation = if (vertical) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL }
                listOf("Present", "Absent").forEach { option -> group.addView(MaterialRadioButton(requireContext()).apply { id = View.generateViewId(); text = option; contentDescription = "${student.name}, $option"; minHeight = dp(48) }, if (vertical) RadioGroup.LayoutParams(-1, -2) else RadioGroup.LayoutParams(0, -2, 1f)) }
                group.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
                val record = s.attendance.firstOrNull { it.sessionId == session?.id && it.studentId == student.id }
                record?.let { group.check(group.getChildAt(if (it.present) 0 else 1).id) }
                radios["student${student.id}"] = group; register.addView(group)
            }
        }
        binding!!.fields.addView(register); loadRegister()
        dateButton.setOnClickListener {
            val parts = sessionDate.split('-').map { it.toInt() }
            DatePickerDialog(requireContext(), { _, year, month, day ->
                sessionDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                dateButton.text = "Date · ${dateLabel(sessionDate)}"; loadRegister()
            }, parts[0], parts[1] - 1, parts[2]).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        button("Mark everyone present", secondary = true) {
            radios.filterKeys { it.startsWith("student") }.values.forEach { it.check(it.getChildAt(0).id) }
        }
        val errorLabel = TextView(requireContext()).apply { setTextColor(resources.getColor(R.color.error, null)); accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        binding!!.fields.addView(errorLabel)
        button("Save attendance") {
            val choices = s.roster(courseId).associate { student ->
                val group = radios.getValue("student${student.id}")
                student.id to if (group.checkedRadioButtonId == -1) null else group.checkedRadioButtonId == group.getChildAt(0).id
            }
            if (choices.values.any { it == null }) { errorLabel.text = "Choose present or absent for every student." }
            else {
                errorLabel.text = ""; val date = sessionDate
                runAction("Attendance saved") { saveAttendance(model.userId, courseId, date, choices.mapValues { it.value!! }) }
            }
        }
    }
    private fun marks(s: PortalSnapshot) {
        text(s.course(courseId).name, true)
        text("Internal assessment · Maximum 30 marks\nSave incomplete work as a draft, or publish a complete set of results to students.")
        val roster = s.roster(courseId)
        roster.forEach { student ->
            val mark = s.marks.firstOrNull { it.courseId == courseId && it.studentId == student.id }
            field("mark${student.id}", "${student.name} · / 30", mark?.score?.toString().orEmpty(), inputType = InputType.TYPE_CLASS_NUMBER, maximum = 2)
        }
        val publish = MaterialCheckBox(requireContext()).apply {
            text = "Publish results to students"; minHeight = dp(48)
            isChecked = s.marks.filter { it.courseId == courseId }.let { it.isNotEmpty() && it.all { mark -> mark.published } }
        }
        checks["publish"] = publish; binding!!.fields.addView(publish)
        text("Unpublished drafts are visible only to you. Saving published results as a draft hides them until you publish again.")
        val save = button(if (publish.isChecked) "Publish marks" else "Save draft") {
            var valid = true
            val scores = roster.associate { student ->
                val key = "mark${student.id}"; val value = input(key); val number = value.toIntOrNull()
                val error = when { value.isBlank() && publish.isChecked -> "Enter a mark before publishing"
                    value.isNotBlank() && (number == null || number !in 0..30) -> "Enter a mark between 0 and 30"; else -> null }
                inputs.getValue(key).error = error; if (error != null) valid = false
                student.id to number
            }
            if (valid) { val published = publish.isChecked
                runAction(if (published) "Marks published" else "Draft marks saved") { saveMarks(model.userId, courseId, scores, published) }
            }
        }
        publish.setOnCheckedChangeListener { _, checked -> save.text = if (checked) "Publish marks" else "Save draft" }
    }
    override fun onDestroyView() {
        draft = if (built) capture() else draft
        binding = null; inputs.clear(); selections.clear(); checks.clear(); radios.clear(); buttons.clear(); built = false
        super.onDestroyView()
    }
    companion object {
        fun create(kind: String, courseId: Long = 0, recordId: Long = 0) = FormFragment().apply {
            arguments = Bundle().apply { putString("kind", kind); putLong("course", courseId); putLong("record", recordId) }
        }
    }
}
