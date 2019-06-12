package com.example.android.releviumfinal;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class AgentActivity extends AppCompatActivity {

    EditText mSendText;
    ImageView mImageSendText;
    ImageView mImageSendVoice;
    TextView test1;
    TextView test2;
    StringBuilder sb = new StringBuilder();
    TextToSpeech mTextToSpeech;
    static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent);
        mSendText = findViewById(R.id.agent_editText);
        mImageSendText = findViewById(R.id.agent_btn_text);
        mImageSendVoice = findViewById(R.id.agent_btn_voice);
        test1 = findViewById(R.id.text1);
        test2 = findViewById(R.id.text2);

        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        mSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count < 1) {
                    Log.v("TEST", "Focus lost");
                    //mImageSendText.setBackground(getResources().getDrawable(R.drawable.ic_mic));
                    mImageSendText.setVisibility(View.INVISIBLE);
                    mImageSendVoice.setVisibility(View.VISIBLE);
                } else {
                    Log.v("TEST", "Focus gained");
                    //mImageSendText.setBackground(getResources().getDrawable(R.drawable.ic_send));
                    mImageSendText.setVisibility(View.VISIBLE);
                    mImageSendVoice.setVisibility(View.INVISIBLE);
                }
            }


        });

        mImageSendVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceRecognitionActivity();
            }
        });

        mImageSendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPost(mSendText.getText().toString());
            }
        });
    }
    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sendPost(result.get(0));
        }
        else{
            Toast.makeText(this, "Please re-do the voice command", Toast.LENGTH_SHORT).show();
        }


    }
    public void sendPost(final String question) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    URL url = new URL("http://dummy.elrwsh.me/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();

                    jsonParam.put("question", question);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    int HttpResult = conn.getResponseCode();
                    if (HttpResult == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                conn.getInputStream(), "utf-8"));
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    test1.setText(question);
                                    readJsonFile(sb.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        br.close();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    public void readJsonFile(String result) throws JSONException {
        JSONObject jObject = new JSONObject(result);
        String aJsonAnswer = jObject.getString("question");
        test2.setText(aJsonAnswer);
        mTextToSpeech.speak(aJsonAnswer, TextToSpeech.QUEUE_FLUSH, null, null);
        sb = new StringBuilder();
    }

}
