package net.dev.roomwordsample

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.dev.roomwordsample.data.WordRepository
import net.dev.roomwordsample.data.WordRoomDatabase

class WordsApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { WordRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { WordRepository(database.wordDao()) }
}