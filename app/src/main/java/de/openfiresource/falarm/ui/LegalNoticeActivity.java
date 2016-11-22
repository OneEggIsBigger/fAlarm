package de.openfiresource.falarm.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.openfiresource.falarm.R;

public class LegalNoticeActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView mTextView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_notice);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String softwareInfo = GoogleApiAvailability.getInstance()
                .getOpenSourceSoftwareLicenseInfo(this);

        if(softwareInfo != null) {
            mTextView.setText(softwareInfo);
        }
    }

}
