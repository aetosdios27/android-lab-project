package edu.muj.studentportal.ui

import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.muj.studentportal.data.PortalSnapshot

class NoticeFragment : BaseListFragment() {
    private val id get() = requireArguments().getLong("id")
    override fun render(s: PortalSnapshot): List<PortalRow> = buildList {
        host.title("Announcement")
        val notice = s.notices.firstOrNull { it.id == id } ?: run { add(PortalRow("missing", "Announcement unavailable", "It may have been removed by its author.")); return@buildList }
        add(heading("title", notice.title, "${s.course(notice.courseId).name}\n${s.person(notice.authorId).name} · ${timestamp(notice.updatedAt)}"))
        add(PortalRow("body", "Announcement", notice.body))
        if (s.user.isTeacher && notice.authorId == s.user.id) {
            add(PortalRow("edit", "Edit announcement", "Changes will be visible to enrolled students.", badge = "Edit", click = { host.open(FormFragment.create("notice", notice.courseId, id)) }))
            add(PortalRow("delete", "Delete announcement", "Remove it from this class’s notice list.", badge = "Delete", click = {
                MaterialAlertDialogBuilder(requireContext()).setTitle("Delete announcement?").setMessage("Students will no longer see this notice.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Delete") { _, _ -> model.perform("Announcement deleted") { deleteNotice(userId = model.userId, id = id) } }.show()
            }))
        }
    }
    companion object { fun create(id: Long) = NoticeFragment().apply { arguments = Bundle().apply { putLong("id", id) } } }
}

class RequestFragment : BaseListFragment() {
    private val id get() = requireArguments().getLong("id")
    override fun render(s: PortalSnapshot): List<PortalRow> = buildList {
        host.title("REQ-${id.toString().padStart(4, '0')}")
        val request = s.requests.firstOrNull { it.id == id } ?: run { add(PortalRow("missing", "Request unavailable", "It may have been deleted.")); return@buildList }
        add(heading("subject", request.subject, "${s.course(request.courseId).name}\nSubmitted ${timestamp(request.createdAt)}"))
        add(PortalRow("status", request.status, "${request.category} · ${request.priority} priority\n${s.person(request.studentId).name}", badge = "Assigned to ${s.person(s.course(request.courseId).teacherId).name}"))
        add(PortalRow("description", "Student’s request", request.description))
        val responses = s.responses.filter { it.requestId == id }
        add(heading("responses", "Teacher responses"))
        if (responses.isEmpty()) add(PortalRow("waiting", "Awaiting a response", "Your teacher will review this request in their workspace."))
        responses.forEach { response -> add(PortalRow("response${response.id}", s.person(response.authorId).name, response.body, timestamp(response.createdAt))) }
        if (!s.user.isTeacher && request.status == "Open" && responses.isEmpty()) {
            add(PortalRow("edit", "Edit request", "You can edit it until your teacher begins review.", badge = "Edit", click = { host.open(FormFragment.create("request", request.courseId, id)) }))
            add(PortalRow("delete", "Delete request", "Remove this unreviewed request.", badge = "Delete", click = {
                MaterialAlertDialogBuilder(requireContext()).setTitle("Delete this request?").setMessage("This removes the unreviewed request from both accounts.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Delete") { _, _ -> model.perform("Request deleted") { deleteRequest(model.userId, id) } }.show()
            }))
        }
    }
    override fun primary(snapshot: PortalSnapshot) = if (snapshot.user.isTeacher && snapshot.requests.any { it.id == id })
        "Reply to request" to { host.open(FormFragment.create("reply", recordId = id)) } else null
    companion object { fun create(id: Long) = RequestFragment().apply { arguments = Bundle().apply { putLong("id", id) } } }
}
