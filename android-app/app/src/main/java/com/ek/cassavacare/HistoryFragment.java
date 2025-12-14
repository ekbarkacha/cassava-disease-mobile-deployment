/*
 * Project: CassavaCare
 * File: HistoryFragment.java
 * Description: Fragment for displaying the history of scanned cassava leaves with navigation support.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.ek.cassavacare.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HistoryFragment extends Fragment {
    private AppDatabase db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "database-name").build();

        RecyclerView recyclerView = root.findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new Thread(() -> {
            List<ScanResult> results = db.scanResultDao().getAll();
            requireActivity().runOnUiThread(() -> {
                HistoryAdapter adapter = new HistoryAdapter(results);
                recyclerView.setAdapter(adapter);
            });
        }).start();


        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                // Always pop back to HomeFragment
                navController.popBackStack(R.id.navigation_home, false);
                return true;
            } else {
                // Navigate to Scan or History normally
                navController.navigate(id);
                return true;
            }
        });


        return root;
    }
}