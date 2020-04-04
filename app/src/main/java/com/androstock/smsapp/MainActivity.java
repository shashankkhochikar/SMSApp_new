package com.androstock.smsapp;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{

    static final int REQUEST_PERMISSION_KEY = 1;
    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    static MainActivity inst;
    LoadSms loadsmsTask;
    InboxAdapter adapter, tmpadapter;;
    ListView listView;
    FloatingActionButton fab_new;
    ProgressBar loader;
    int i;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CacheUtils.configureCache(this);

        listView = (ListView) findViewById(R.id.listView);
        loader = (ProgressBar) findViewById(R.id.loader);
        fab_new = (FloatingActionButton) findViewById(R.id.fab_new);

        listView.setEmptyView(loader);
        getSupportActionBar().setTitle("SMS");

        setPref();

        fab_new.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewSmsActivity.class));
            }
        });

        if (!isAccessibilityOn(MainActivity.this, WhatsappAccessibilityService.class)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return;
        }
    }

    private boolean isAccessibilityOn(Context context, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                colonSplitter.setString(settingValue);
                while (colonSplitter.hasNext()) {
                    String accessibilityService = colonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setPref(){
        pref = getApplicationContext().getSharedPreferences("SMSApp", MODE_PRIVATE); // 0 - for private mode

        String str_sim = pref.getString("def_sim", null); // getting String
        if(str_sim == null) {
            editor = pref.edit();
            editor.putString("def_sim", "0"); // Storing string
            editor.commit();
        }

        //Default Values For onIncomingCallStarted
        String onIncomingCallStarted = pref.getString("onIncomingCallStarted", null); // getting String
        if(onIncomingCallStarted == null) {
            editor = pref.edit();
            editor.putString("onIncomingCallStarted", "0"); // Storing string
            editor.commit();
        }
        //Default Values For onIncomingCallEnded
        String onIncomingCallEnded = pref.getString("onIncomingCallEnded", null); // getting String
        if(onIncomingCallEnded == null) {
            editor = pref.edit();
            editor.putString("onIncomingCallEnded", "0"); // Storing string
            editor.commit();
        }
        //Default Values For onOutgoingCallStarted
        String onOutgoingCallStarted = pref.getString("onOutgoingCallStarted", null); // getting String
        if(onOutgoingCallStarted == null) {
            editor = pref.edit();
            editor.putString("onOutgoingCallStarted", "0"); // Storing string
            editor.commit();
        }
        //Default Values For onOutgoingCallEnded
        String onOutgoingCallEnded = pref.getString("onOutgoingCallEnded", null); // getting String
        if(onOutgoingCallEnded == null) {
            editor = pref.edit();
            editor.putString("onOutgoingCallEnded", "0"); // Storing string
            editor.commit();
        }
        //Default Values For onMissedCall
        String onMissedCall = pref.getString("onMissedCall", null); // getting String
        if(onMissedCall == null) {
            editor = pref.edit();
            editor.putString("onMissedCall", "0"); // Storing string
            editor.commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mainactivity, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.item1:
                //Toast.makeText(getApplicationContext(),"Item 1 Selected",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this,smsTemplatesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void init()
    {
        smsList.clear();
        try{
            tmpList = (ArrayList<HashMap<String, String>>)Function.readCachedFile  (MainActivity.this, "smsapp");
            tmpadapter = new InboxAdapter(MainActivity.this, tmpList);
            listView.setAdapter(tmpadapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    loadsmsTask.cancel(true);
                    Intent intent = new Intent(MainActivity.this, Chat.class);
                    intent.putExtra("name", tmpList.get(+position).get(Function.KEY_NAME));
                    intent.putExtra("address", tmpList.get(+position).get(Function.KEY_PHONE));
                    intent.putExtra("thread_id", tmpList.get(+position).get(Function.KEY_THREAD_ID));
                    startActivity(intent);
                }
            });
        }catch(Exception e) {}

    }

    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");

                Cursor inbox = getContentResolver().query(uriInbox, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "address IS NOT NULL) GROUP BY (thread_id", null, null); // 2nd null = "address IS NOT NULL) GROUP BY (address"
                Cursor c = new MergeCursor(new Cursor[]{inbox,sent}); // Attaching inbox and sent sms


                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String name = null;
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow("type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));



                        name = CacheUtils.readFile(thread_id);
                        if(name == null)
                        {
                            name = Function.getContactbyPhoneNumber(getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));
                            CacheUtils.writeFile(thread_id, name);
                        }


                        smsList.add(Function.mappingInbox(_id, thread_id, name, phone, msg, type, timestamp, Function.converToTime(timestamp)));
                        c.moveToNext();
                    }
                }
                c.close();

            }catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Collections.sort(smsList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging sms by timestamp decending
            ArrayList<HashMap<String, String>> purified = Function.removeDuplicates(smsList); // Removing duplicates from inbox & sent
            smsList.clear();
            smsList.addAll(purified);

            // Updating cache data
            try{
                Function.createCachedFile (MainActivity.this,"smsapp", smsList);
            }catch (Exception e) {}
            // Updating cache data

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {

            if(!tmpList.equals(smsList))
            {
                adapter = new InboxAdapter(MainActivity.this, smsList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            final int position, long id) {
                        Intent intent = new Intent(MainActivity.this, Chat.class);
                        intent.putExtra("name", smsList.get(+position).get(Function.KEY_NAME));
                        intent.putExtra("address", tmpList.get(+position).get(Function.KEY_PHONE));
                        intent.putExtra("thread_id", smsList.get(+position).get(Function.KEY_THREAD_ID));
                        startActivity(intent);
                    }
                });
            }



        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    init();
                    loadsmsTask = new LoadSms();
                    loadsmsTask.execute();
                } else
                {
                    Toast.makeText(MainActivity.this, "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG};
        if(!Function.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }else{

            init();
            loadsmsTask = new LoadSms();
            loadsmsTask.execute();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    class InboxAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<HashMap< String, String >> data;
        public InboxAdapter(Activity a, ArrayList < HashMap < String, String >> d) {
            activity = a;
            data = d;
        }
        public int getCount() {
            return data.size();
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            InboxViewHolder holder = null;
            if (convertView == null) {
                holder = new InboxViewHolder();
                convertView = LayoutInflater.from(activity).inflate(
                        R.layout.conversation_list_item, parent, false);

                holder.inbox_thumb = (ImageView) convertView.findViewById(R.id.inbox_thumb);
                holder.inbox_user = (TextView) convertView.findViewById(R.id.inbox_user);
                holder.inbox_msg = (TextView) convertView.findViewById(R.id.inbox_msg);
                holder.inbox_date = (TextView) convertView.findViewById(R.id.inbox_date);

                convertView.setTag(holder);
            } else {
                holder = (InboxViewHolder) convertView.getTag();
            }
            holder.inbox_thumb.setId(position);
            holder.inbox_user.setId(position);
            holder.inbox_msg.setId(position);
            holder.inbox_date.setId(position);

            HashMap < String, String > song = new HashMap < String, String > ();
            song = data.get(position);
            try {
                holder.inbox_user.setText(song.get(Function.KEY_NAME));
                holder.inbox_msg.setText(song.get(Function.KEY_MSG));
                holder.inbox_date.setText(song.get(Function.KEY_TIME));

                String firstLetter = String.valueOf(song.get(Function.KEY_NAME).charAt(0));
                ColorGenerator generator = ColorGenerator.MATERIAL;
                int color = generator.getColor(getItem(position));
                TextDrawable drawable = TextDrawable.builder()
                        .buildRound(firstLetter, color);
                holder.inbox_thumb.setImageDrawable(drawable);
            } catch (Exception e) {}
            return convertView;
        }
    }

    class InboxViewHolder {
        ImageView inbox_thumb;
        TextView inbox_user, inbox_msg, inbox_date;
    }
}


