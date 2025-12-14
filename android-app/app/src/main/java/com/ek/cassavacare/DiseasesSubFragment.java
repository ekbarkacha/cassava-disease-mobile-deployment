/*
 * Project: CassavaCare
 * File: DiseasesSubFragment.java
 * Description: Displays educational information on common cassava diseases
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.ek.cassavacare.R;

public class DiseasesSubFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diseases_sub, container, false);

        TextView tvContent = root.findViewById(R.id.tv_diseases_content);
        tvContent.setText(Html.fromHtml(
                "Common Cassava Diseases:<br><br>" +

                        "1. <b>Cassava Mosaic Disease (CMD)</b>:<br>" +
                        "&nbsp;&nbsp;• Symptoms: Mottled or mosaic patterns on leaves, leaf curling, stunted growth, reduced root size.<br>" +
                        "&nbsp;&nbsp;• Cause: Viruses from the Geminiviridae family, primarily transmitted by whiteflies.<br>" +
                        "&nbsp;&nbsp;• Impact: Significant yield losses due to reduced photosynthesis and root development.<br>" +
                        "&nbsp;&nbsp;• Reference: <a href='https://en.wikipedia.org/wiki/Cassava_mosaic_viruses'>Wikipedia</a><br><br>" +

                        "2. <b>Cassava Bacterial Blight (CBB)</b>:<br>" +
                        "&nbsp;&nbsp;• Symptoms: Angular necrotic spots on leaves, wilting, dieback, and vascular necrosis.<br>" +
                        "&nbsp;&nbsp;• Cause: Bacterial pathogen Xanthomonas axonopodis pv. manihotis.<br>" +
                        "&nbsp;&nbsp;• Impact: Affects plant growth and tuber quality, leading to economic losses.<br>" +
                        "&nbsp;&nbsp;• Reference: <a href='https://en.wikipedia.org/wiki/Bacterial_blight_of_cassava'>Wikipedia</a><br><br>" +

                        "3. <b>Cassava Brown Streak Disease (CBSD)</b>:<br>" +
                        "&nbsp;&nbsp;• Symptoms: Chlorosis and necrosis on leaves, brown streaks on stems, and dry rot in roots.<br>" +
                        "&nbsp;&nbsp;• Cause: Cassava brown streak virus (CBSV) and Ugandan cassava brown streak virus (UCBSV).<br>" +
                        "&nbsp;&nbsp;• Impact: Major threat to food security, especially in East Africa, due to severe root yield loss.<br>" +
                        "&nbsp;&nbsp;• Reference: <a href='https://en.wikipedia.org/wiki/Cassava_brown_streak_virus_disease'>Wikipedia</a><br><br>" +

                        "4. <b>Cassava Green Mottle (CGM)</b>:<br>" +
                        "&nbsp;&nbsp;• Symptoms: Green mottling or yellow patches on leaves, often with distorted growth.<br>" +
                        "&nbsp;&nbsp;• Cause: Cassava green mottle virus (CGMV), transmitted by whiteflies.<br>" +
                        "&nbsp;&nbsp;• Impact: Reduces leaf development and plant vigor, leading to lower yields.<br>" +
                        "&nbsp;&nbsp;• Reference: <a href='https://en.wikipedia.org/wiki/Cassava_green_mottle_virus'>Wikipedia</a><br><br>" +

                        "<b>Use the Scan feature for detection</b><br>"
        ));

        // Enable clickable links
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());


        return root;
    }
}