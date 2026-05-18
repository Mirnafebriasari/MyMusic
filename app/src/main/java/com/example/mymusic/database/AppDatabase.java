package com.example.mymusic.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * PERUBAHAN:
 *  • version: 1 → 2
 *  • Migrasi MIGRATION_1_2: tambah kolom localPath (TEXT, nullable)
 *
 *  Jika Anda ingin reset bersih (dev only), bisa pakai fallbackToDestructiveMigration()
 *  dan hapus MIGRATION_1_2, namun data lama akan hilang.
 */
@Database(
        entities = {SongEntity.class},
        version  = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract SongDao songDao();

    /** Migrasi 1 → 2: tambah kolom localPath */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE favorite_songs ADD COLUMN localPath TEXT"
            );
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "music_database"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return INSTANCE;
    }
}