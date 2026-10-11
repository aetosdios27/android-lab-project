package edu.muj.studentportal

import android.app.Application
import androidx.room.Room
import edu.muj.studentportal.data.PortalDatabase
import edu.muj.studentportal.data.PortalRepository
import edu.muj.studentportal.data.LegacyImporter

class PortalApplication : Application() {
    val repository by lazy {
        PortalRepository(Room.databaseBuilder(this, PortalDatabase::class.java, "portal.db")
            .build(), LegacyImporter(this)::import)
    }
    val session by lazy { SessionStore(this) }
}
