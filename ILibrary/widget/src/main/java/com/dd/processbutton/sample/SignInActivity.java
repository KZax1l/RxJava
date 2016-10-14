package com.dd.processbutton.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.dd.processbutton.sample.utils.ProgressGenerator;

import org.zsago.widget.R;


public class SignInActivity extends Activity implements ProgressGenerator.OnCompleteListener {

    public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_process_button_ac_sign_in);

        final EditText editEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editPassword = (EditText) findViewById(R.id.editPassword);

        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        final ActionProcessButton btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);
//        Bundle extras = getIntent().getExtras();
//        if(extras != null && extras.getBoolean(EXTRAS_ENDLESS_MODE)) {
//            btnSignIn.setMode(ActionProcessButton.Mode.ENDLESS);
//        } else {
//            btnSignIn.setMode(ActionProcessButton.Mode.PROGRESS);
//        }
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                btnSignIn.setProgress(50);
                progressGenerator.start(btnSignIn);
                btnSignIn.setEnabled(false);
                editEmail.setEnabled(false);
                editPassword.setEnabled(false);
            }
        });
    }

    @Override
    public void onComplete() {
        Toast.makeText(this, R.string.sample_process_button_Loading_Complete, Toast.LENGTH_LONG).show();
    }

}
