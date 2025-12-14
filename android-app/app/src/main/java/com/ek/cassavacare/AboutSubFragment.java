/*
 * Project: CassavaCare
 * File: AboutSubFragment.java
 * Description: Displays application information, model details, and developer credits
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

public class AboutSubFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_sub, container, false);

        TextView tvContent = root.findViewById(R.id.tv_about_content);

        tvContent.setText(Html.fromHtml(
                "<b>About CassavaCare</b><br>" +
                        "AI-powered mobile app for real-time cassava leaf disease detection, enabling farmers to monitor crop health efficiently.<br><br>" +

                        "<b>Key Features</b><br>" +
                        "• AI-powered leaf scanning<br>" +
                        "• Works offline for field usage<br>" +
                        "• Educational resources on cassava diseases<br><br>" +

                        "<b>Model Performance</b><br>" +
                        "• DenseNet121 model trained on the Cassava Leaf Disease Kaggle dataset<br>" +
                        "• 88% validation accuracy<br>" +
                        "• Converted to TensorFlow Lite for mobile deployment<br><br>" +

                        "<b>Development</b><br>" +
                        "• Built with Android Studio, Java, and TensorFlow Lite<br><br>" +

                        "<b>Credits</b><br>" +
                        "• Kaggle community for dataset<br>" +
                        "• TensorFlow team for mobile ML tools<br><br>" +

                        "<b>Developer</b><br>" +
                        "Emmanuel Kirui Barkacha<br>" +
                        "Email: <a href=\"mailto:ebarkacha@aimsammi.org\">ebarkacha@aimsammi.org</a><br>" +
                        "GitHub: <a href=\"https://github.com/ekbarkacha\">https://github.com/ekbarkacha</a><br>"
        ));
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());

        return root;
    }
}