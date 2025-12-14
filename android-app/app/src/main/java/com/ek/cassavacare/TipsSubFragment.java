/*
 * Project: CassavaCare
 * File: TipsSubFragment.java
 * Description: Fragment displaying cassava farming tips using formatted HTML content.
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

public class TipsSubFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tips_sub, container, false);

        TextView tvContent = root.findViewById(R.id.tv_tips_content);
        tvContent.setText(Html.fromHtml(
                "<b>Farming Tips for Cassava:</b><br><br>" +

                        "<b>1. Soil Preparation</b>:<br>" +
                        "&nbsp;&nbsp;• Use well-drained, fertile soil with pH between 5.5 and 6.5.<br>" +
                        "&nbsp;&nbsp;• Conduct a soil test before planting to ensure nutrient balance.<br><br>" +

                        "<b>2. Planting</b>:<br>" +
                        "&nbsp;&nbsp;• Use healthy stem cuttings planted about 1 meter apart in rows.<br>" +
                        "&nbsp;&nbsp;• Plant at the beginning of the rainy season for optimal growth.<br>" +
                        "&nbsp;&nbsp;• Avoid waterlogged areas to prevent rot.<br><br>" +

                        "<b>3. Disease Prevention</b>:<br>" +
                        "&nbsp;&nbsp;• Rotate crops every 2-3 years to reduce soil-borne disease buildup.<br>" +
                        "&nbsp;&nbsp;• Use certified disease-resistant varieties when available.<br>" +
                        "&nbsp;&nbsp;• Remove infected plants promptly.<br><br>" +

                        "<b>4. Monitoring</b>:<br>" +
                        "&nbsp;&nbsp;• Regularly scan leaves using this app for early detection of diseases.<br>" +
                        "&nbsp;&nbsp;• Apply organic or balanced fertilizers to maintain healthy plant growth.<br>" +
                        "&nbsp;&nbsp;• Maintain proper spacing and weed control. "
        ));
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());


        return root;
    }
}