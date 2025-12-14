/*
 * Project: CassavaCare
 * File: HistoryAdapter.java
 * Description: Adapter for displaying the history of scanned cassava leaves, including results, timestamps, and images.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ek.cassavacare.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<ScanResult> results;

    public HistoryAdapter(List<ScanResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult result = results.get(position);
        holder.tvResult.setText(result.result);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(result.timestamp));
        holder.tvDate.setText(date);

        // Load image from byte array
        if (result.image != null && result.image.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(result.image, 0, result.image.length);
            holder.imgThumbnail.setImageBitmap(bitmap);
            holder.imgThumbnail.setVisibility(View.VISIBLE);
        } else {
            holder.imgThumbnail.setVisibility(View.GONE); // hide if no image
        }

        holder.itemView.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                ScanResult rst = results.get(position);
                showScanDetailDialog(rst,v.getContext());
            }
        });
    }

    private void showScanDetailDialog(ScanResult result, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.Theme_CassavaCare_Dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_scan_detail, null);
        builder.setView(dialogView);

        ImageView img = dialogView.findViewById(R.id.img_dialog_thumbnail);
        TextView tvResult = dialogView.findViewById(R.id.tv_dialog_result);
        TextView tvDate = dialogView.findViewById(R.id.tv_dialog_date);
        TextView tvRemedy = dialogView.findViewById(R.id.tv_dialog_remedy);

        // Load image
        if (result.image != null && result.image.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(result.image, 0, result.image.length);
            img.setImageBitmap(bitmap);
        }

        // Set texts
        tvResult.setText(result.result);
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(result.timestamp)));
        tvRemedy.setText(getRemedy(result.result.split(" - ")[0]));

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        //dialogView.setBackgroundColor(ContextCompat.getColor(context, R.color.card));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getRemedy(String disease) {
        Map<String, String> remedies = new HashMap<>();
        remedies.put("Cassava Bacterial Blight",
                "1. Remove and safely destroy infected plants to prevent spread.\n" +
                        "2. Apply copper-based bactericides as recommended.\n" +
                        "3. Ensure proper spacing and good drainage to reduce humidity.\n" +
                        "4. Practice crop rotation to minimize disease buildup."
        );

        remedies.put("Cassava Brown Streak Disease",
                "1. Use certified disease-free planting material.\n" +
                        "2. Control whitefly populations, the primary virus vector.\n" +
                        "3. Monitor plants regularly for early symptoms.\n" +
                        "4. Implement proper field sanitation and crop rotation."
        );

        remedies.put("Cassava Green Mottle",
                "1. Remove infected leaves and plants to reduce viral spread.\n" +
                        "2. Monitor whitefly populations and apply organic or approved controls.\n" +
                        "3. Maintain healthy soil with organic mulch and balanced nutrients.\n" +
                        "4. Use resistant varieties if available."
        );

        remedies.put("Cassava Mosaic Disease",
                "1. Plant resistant or tolerant cassava varieties.\n" +
                        "2. Remove and destroy severely infected plants.\n" +
                        "3. Control whiteflies to reduce virus transmission.\n" +
                        "4. Practice proper spacing and crop rotation to limit disease."
        );

        remedies.put("Healthy",
                "Maintain good agricultural practices:\n" +
                        "- Proper soil preparation and fertilization.\n" +
                        "- Adequate spacing and irrigation.\n" +
                        "- Regular monitoring for pests and diseases.\n" +
                        "- Use certified clean planting material."
        );

        return remedies.getOrDefault(disease,
                "The disease was not recognized. Please consult a certified agricultural extension officer for guidance."
        );
    }


    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvResult, tvDate;
        ImageView imgThumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            tvResult = itemView.findViewById(R.id.tv_history_result);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            imgThumbnail = itemView.findViewById(R.id.img_history_thumbnail);


        }

    }
}
