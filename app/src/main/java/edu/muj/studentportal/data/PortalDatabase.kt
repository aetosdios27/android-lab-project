package edu.muj.studentportal.data

import androidx.room.*

@Dao
interface PortalDao {
    @Query("SELECT * FROM users WHERE id = :id") suspend fun user(id: Long): UserEntity?
    @Query("SELECT * FROM users WHERE login = :login") suspend fun login(login: String): UserEntity?
    @Query("SELECT * FROM users") suspend fun users(): List<UserEntity>
    @Query("SELECT * FROM courses") suspend fun courses(): List<CourseEntity>
    @Query("SELECT * FROM enrollments") suspend fun enrollments(): List<EnrollmentEntity>
    @Query("SELECT * FROM schedule ORDER BY weekday, start") suspend fun schedule(): List<ScheduleEntity>
    @Query("SELECT * FROM sessions ORDER BY date DESC") suspend fun sessions(): List<SessionEntity>
    @Query("SELECT * FROM attendance") suspend fun attendance(): List<AttendanceEntity>
    @Query("SELECT * FROM marks") suspend fun marks(): List<MarkEntity>
    @Query("SELECT * FROM notices ORDER BY updatedAt DESC, id DESC") suspend fun notices(): List<NoticeEntity>
    @Query("SELECT * FROM requests ORDER BY updatedAt DESC, id DESC") suspend fun requests(): List<RequestEntity>
    @Query("SELECT * FROM responses ORDER BY createdAt, id") suspend fun responses(): List<ResponseEntity>
    @Insert suspend fun insertUsers(rows: List<UserEntity>)
    @Insert suspend fun insertCourses(rows: List<CourseEntity>)
    @Insert suspend fun insertEnrollments(rows: List<EnrollmentEntity>)
    @Insert suspend fun insertSchedule(rows: List<ScheduleEntity>)
    @Insert suspend fun insertSession(row: SessionEntity): Long
    @Upsert suspend fun saveAttendance(rows: List<AttendanceEntity>)
    @Upsert suspend fun saveMarks(rows: List<MarkEntity>)
    @Insert suspend fun insertNotice(row: NoticeEntity): Long
    @Update suspend fun updateNotice(row: NoticeEntity)
    @Query("DELETE FROM notices WHERE id = :id") suspend fun deleteNotice(id: Long)
    @Insert suspend fun insertRequest(row: RequestEntity): Long
    @Update suspend fun updateRequest(row: RequestEntity)
    @Query("DELETE FROM requests WHERE id = :id") suspend fun deleteRequest(id: Long)
    @Insert suspend fun insertResponse(row: ResponseEntity): Long
    @Query("UPDATE users SET phone = :phone, email = :email WHERE id = :id")
    suspend fun updateProfile(id: Long, phone: String, email: String)
}

@Database(entities = [UserEntity::class, CourseEntity::class, EnrollmentEntity::class, ScheduleEntity::class,
    SessionEntity::class, AttendanceEntity::class, MarkEntity::class, NoticeEntity::class,
    RequestEntity::class, ResponseEntity::class], version = 1, exportSchema = true)
abstract class PortalDatabase : RoomDatabase() { abstract fun dao(): PortalDao }
