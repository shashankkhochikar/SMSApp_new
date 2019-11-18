package com.androstock.smsapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class smsTemplatesActivity extends AppCompatActivity {

    EditText template_message;
    Button set_btn,setSim_btn;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    CheckBox chk_onIncomingCallStarted;
    CheckBox chk_onIncomingCallEnded;
    CheckBox chk_onOutgoingCallStarted;
    CheckBox chk_onOutgoingCallEnded;
    CheckBox chk_onMissedCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_templates);

        template_message = (EditText) findViewById(R.id.template_message);
        set_btn = (Button) findViewById(R.id.set_btn);
        setSim_btn = (Button) findViewById(R.id.btnSetSim);
        radioGroup = (RadioGroup) findViewById(R.id.radioGrpSim);

        chk_onIncomingCallStarted = (CheckBox)findViewById(R.id.onIncomingCallStarted);
        chk_onIncomingCallEnded = (CheckBox)findViewById(R.id.onIncomingCallEnded);
        chk_onOutgoingCallStarted = (CheckBox)findViewById(R.id.onOutgoingCallStarted);
        chk_onOutgoingCallEnded = (CheckBox)findViewById(R.id.onOutgoingCallEnded);
        chk_onMissedCall = (CheckBox)findViewById(R.id.onMissedCall);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Set Template");

        pref = getApplicationContext().getSharedPreferences("SMSApp", MODE_PRIVATE); // 0 - for private mode

        set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(template_message.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Please Enter Some Text...", Toast.LENGTH_SHORT).show();
                }else {
                    editor = pref.edit();
                    editor.putString("single_temp", template_message.getText().toString()); // Storing string
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "Message Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        });
        setSim_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedId);

                int pos1 =radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));

                editor = pref.edit();
                editor.putString("def_sim", ""+pos1); // Storing string
                editor.commit();

                Toast.makeText(smsTemplatesActivity.this,radioButton.getText()+" set to default !! "+pos1, Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        String str = pref.getString("single_temp", null); // getting String
        if(str != null) {
            template_message.setText(str);
        }
        setPref();
        setListners();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish();
                onBackPressed();
                break;
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        //Execute your code here
        finish();

    }

    public void setListners(){
        //setListner of onIncomingCallStarted
        chk_onIncomingCallStarted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    editor = pref.edit();
                    editor.putString("onIncomingCallStarted", "1"); // Storing string
                    editor.commit();
                }else {
                    editor = pref.edit();
                    editor.putString("onIncomingCallStarted", "0"); // Storing string
                    editor.commit();
                }
            }
        });
        //setListner of onIncomingCallEnded
        chk_onIncomingCallEnded.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    editor = pref.edit();
                    editor.putString("onIncomingCallEnded", "1"); // Storing string
                    editor.commit();
                }else {
                    editor = pref.edit();
                    editor.putString("onIncomingCallEnded", "0"); // Storing string
                    editor.commit();
                }
            }
        });
        //setListner of onOutgoingCallStarted
        chk_onOutgoingCallStarted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    editor = pref.edit();
                    editor.putString("onOutgoingCallStarted", "1"); // Storing string
                    editor.commit();
                }else {
                    editor = pref.edit();
                    editor.putString("onOutgoingCallStarted", "0"); // Storing string
                    editor.commit();
                }
            }
        });
        //setListner of onOutgoingCallEnded
        chk_onOutgoingCallEnded.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    editor = pref.edit();
                    editor.putString("onOutgoingCallEnded", "1"); // Storing string
                    editor.commit();
                }else {
                    editor = pref.edit();
                    editor.putString("onOutgoingCallEnded", "0"); // Storing string
                    editor.commit();
                }
            }
        });
        //setListner of onMissedCall
        chk_onMissedCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    editor = pref.edit();
                    editor.putString("onMissedCall", "1"); // Storing string
                    editor.commit();
                }else {
                    editor = pref.edit();
                    editor.putString("onMissedCall", "0"); // Storing string
                    editor.commit();
                }
            }
        });


    }

    public void setPref(){
        String str_sim = pref.getString("def_sim", null); // getting String
        if(str_sim != null) {
            //radioGroup.check(Integer.parseInt(str_sim));
            ((RadioButton)radioGroup.getChildAt(Integer.parseInt(str_sim))).setChecked(true);
        }else{
            int selectedId = radioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) findViewById(selectedId);
            int pos1 =radioGroup.indexOfChild(findViewById(radioGroup.getCheckedRadioButtonId()));
            editor = pref.edit();
            editor.putString("def_sim", ""+pos1); // Storing string
            editor.commit();

            ((RadioButton)radioGroup.getChildAt(pos1)).setChecked(true);
        }
        //set Values For onIncomingCallEnded
        String onIncomingCallStarted = pref.getString("onIncomingCallStarted", null); // getting String
        if(onIncomingCallStarted != null) {
            if(onIncomingCallStarted.equals("1")){
                chk_onIncomingCallStarted.setChecked(true);
            }
        }
        //set Values For onIncomingCallEnded
        String onIncomingCallEnded = pref.getString("onIncomingCallEnded", null); // getting String
        if(onIncomingCallEnded != null) {
            if(onIncomingCallEnded.equals("1")){
                chk_onIncomingCallEnded.setChecked(true);
            }
        }
        //set Values For onOutgoingCallStarted
        String onOutgoingCallStarted = pref.getString("onOutgoingCallStarted", null); // getting String
        if(onOutgoingCallStarted != null) {
            if(onOutgoingCallStarted.equals("1")){
                chk_onOutgoingCallStarted.setChecked(true);
            }
        }
        //set Values For onOutgoingCallEnded
        String onOutgoingCallEnded = pref.getString("onOutgoingCallEnded", null); // getting String
        if(onOutgoingCallEnded != null) {
            if(onOutgoingCallEnded.equals("1")){
                chk_onOutgoingCallEnded.setChecked(true);
            }
        }
        //set Values For onMissedCall
        String onMissedCall = pref.getString("onMissedCall", null); // getting String
        if(onMissedCall != null) {
            if(onMissedCall.equals("1")){
                chk_onMissedCall.setChecked(true);
            }
        }

    }
}
