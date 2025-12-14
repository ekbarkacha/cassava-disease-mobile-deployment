/*
 * Project: CassavaCare
 * File: AppDatabase.java
 * Description: Room database configuration for storing cassava scan results
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ScanResult.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ScanResultDao scanResultDao();
}