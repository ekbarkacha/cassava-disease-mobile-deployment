/*
 * Project: CassavaCare
 * File: ScanResultDao.java
 * Description: Data Access Object (DAO) for ScanResult entity, providing methods
 *              to insert new scan results and retrieve all scan results ordered by timestamp.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ScanResultDao {
    @Insert
    void insert(ScanResult scanResult);

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    List<ScanResult> getAll();
}
