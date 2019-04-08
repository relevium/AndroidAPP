package com.example.android.releviumfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AgentActivity extends AppCompatActivity {

    EditText mSendText;
    ImageView mImageSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent);
        mSendText = findViewById(R.id.agent_send_text);
        mImageSend = findViewById(R.id.agent_send_icon);

        mSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < 1) {
                    Log.v("TEST", "Focus lost");
                    mImageSend.setBackground(getResources().getDrawable(R.drawable.ic_mic));
                } else {
                    Log.v("TEST", "Focus gained");
                    mImageSend.setBackground(getResources().getDrawable(R.drawable.ic_send));
                }
            }


        });
    }


}
