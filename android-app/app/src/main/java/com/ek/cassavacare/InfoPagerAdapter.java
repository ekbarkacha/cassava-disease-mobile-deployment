/*
 * Project: CassavaCare
 * File: InfoPagerAdapter.java
 * Description: Adapter for ViewPager2 used in InfoFragment to manage three sub-fragments:
 *              Diseases, Tips, and About.
 *
 * Author: Emmanuel Kirui Barkacha
 * Email: ebarkacha@aimsammi.org
 * GitHub: https://github.com/ekbarkacha
 *
 * Created: 2025
 * License: MIT
 */

package com.ek.cassavacare;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InfoPagerAdapter extends FragmentStateAdapter {
    public InfoPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new DiseasesSubFragment();
            case 1: return new TipsSubFragment();
            case 2: return new AboutSubFragment();
            default: return new DiseasesSubFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
