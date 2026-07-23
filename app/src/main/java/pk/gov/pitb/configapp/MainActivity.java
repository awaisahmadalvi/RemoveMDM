package pk.gov.pitb.configapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pk.gov.pitb.configapp.mdm.MDMManager;
import pk.gov.pitb.configapp.mdm.MDMReplaceManager;
import pk.gov.pitb.configapp.utils.LogHelper;


public class MainActivity extends AppCompatActivity {

        private TextView txtLog;
        private Button btnRunAgain;

        private MDMManager mdmManager;
        private MDMReplaceManager replaceManager;
        private LogHelper logger;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            txtLog = findViewById(R.id.txtLog);
            btnRunAgain = findViewById(R.id.btnRunAgain);

            logger = new LogHelper(this, txtLog);

            mdmManager = new MDMManager(this, logger);

            replaceManager = new MDMReplaceManager(this, logger);

            btnRunAgain.setOnClickListener(v ->
                    new Thread(() -> replaceManager.start()).start());
        }

        @Override
        protected void onDestroy() {
            mdmManager.disconnect();
            replaceManager.unregister();
            super.onDestroy();
        }
    }