/*
 * Project: CassavaCare
 * File: ScanResult.java
 * Description: Entity class representing a scan result in the local Room database,
 *              including the disease result, timestamp, and optional captured image.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_results")
public class ScanResult {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String result;
    public long timestamp;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] image; // Add this

    public ScanResult(String result, long timestamp, byte[] image) {
        this.result = result;
        this.timestamp = timestamp;
        this.image = image;
    }
}
