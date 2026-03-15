package com.monpote.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monpote.core.database.MIGRATION_1_2
import com.monpote.core.database.MonPoteDatabase
import com.monpote.core.database.dao.CharacterDao
import com.monpote.core.database.dao.ConversationDao
import com.monpote.core.database.dao.MessageDao
import com.monpote.core.database.seed.CharacterSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MonPoteDatabase {
        lateinit var database: MonPoteDatabase
        database = Room.databaseBuilder(
            context,
            MonPoteDatabase::class.java,
            "monpote.db",
        )
            .addMigrations(MIGRATION_1_2)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        database.characterDao().insertAll(CharacterSeeder.getCharacters())
                    }
                }
            })
            .build()
        return database
    }

    @Provides
    fun provideCharacterDao(database: MonPoteDatabase): CharacterDao = database.characterDao()

    @Provides
    fun provideConversationDao(database: MonPoteDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: MonPoteDatabase): MessageDao = database.messageDao()
}
