    package com.example.jsw.mail;

    import android.Manifest;
    import android.content.ContentValues;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.location.Location;
    import android.location.LocationListener;
    import android.os.AsyncTask;
    import android.os.Build;
    import android.os.StrictMode;
    import android.support.annotation.NonNull;
    import android.support.v4.app.ActivityCompat;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import com.google.firebase.iid.FirebaseInstanceId;

    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.OutputStream;
    import java.io.OutputStreamWriter;
    import java.net.MalformedURLException;
    import java.net.ProtocolException;
    import java.net.URL;
    import java.util.Calendar;
    import java.util.Date;

    import javax.net.ssl.HttpsURLConnection;

    public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {
        private static final String TAG = "MainActivity";

        Button register;//Button for saving information
        EditText sender;// email which sends(google email account)
        EditText pw;//password of sender
        EditText receiver;//email which receives

        String getSender;//get sender email value from text box
        String getPw;//get password value from text box
        String getReceiver;//get receiver email value from text box

        String setSender;//get sender email which stored in database
        String setPw;//get password which stored in database
        String setReceiver;//get receiver email which stored in database

        StringBuffer stringBuffer = new StringBuffer();//string buffer for json parser
        SQLiteDatabase db;//use sqlite database
        MySQLiteOpenHelper helper;//use sqlite database

        double locationX;
        double locationY;

        int Xdo, Xbun;
        double Xcho;
        int Ydo, Ybun;
        double Ycho;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //get components id from xml
            register = (Button) findViewById(R.id.register);
            sender = (EditText) findViewById(R.id.sender);
            pw = (EditText) findViewById(R.id.pw);
            receiver = (EditText) findViewById(R.id.receiver);



            helper = new MySQLiteOpenHelper(MainActivity.this, "test.db", null, 1);

            //when you click "register" button, save your email account on database
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //get input values from text box
                    getSender = sender.getText().toString();
                    getPw = pw.getText().toString();
                    getReceiver = receiver.getText().toString();
                    Log.e("sendmail", "sender : "+ setSender + " pw : " + setPw + " receiver : "+ setReceiver);

                    //insert on database
                    insert(getSender,getPw,getReceiver);

                    //if success to save on database, go to next page
                    Intent intent = new Intent(MainActivity.this, Edit.class);
                    startActivity(intent);


                }
            });

            //get private application token which assigned by Firebase
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d("token", token);

            //grant permission for using internet
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .permitDiskReads()
                    .permitDiskWrites()
                    .permitNetwork().build());


            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TEST", "Permission is granted");
                } else {
                    Log.v("TEST", "Permission is revoked");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            } else {
                Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
                Log.d("TEST", "External Storage Permission is Grant ");
            }

            //if app is running on foreground, send location through an emil
            SingleShotLocationProvider.requestSingleUpdate(this,
                    new SingleShotLocationProvider.LocationCallback() {
                        @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                            Log.e("Location Changed", location.latitude + " and " + location.longitude);
                            locationX = location.latitude;
                            locationY = location.longitude;

                            Xdo = (int) locationX;
                            Xbun = (int) ((locationX - Xdo) * 60);
                            Xcho = (((locationX - Xdo) * 60) - Xbun) * 60;

                            Ydo = (int) locationY;
                            Ybun = (int) ((locationY - Ydo) * 60);
                            Ycho = (((locationY - Ydo) * 60) - Ybun) * 60;

                            try {
                                select();
                                //json parser
                                String startJson = "[";
                                String endJson = "]";

                                if(!stringBuffer.toString().equals("")){
                                    stringBuffer.append(",");
                                }
                                Date currentTime = Calendar.getInstance().getTime();
                                String temp = "{\"email\"" + ":" + "\"" + setReceiver + "\"" + ","
                                        + "\"date\"" + ":" + "\"" + currentTime + "\"" + ","
                                        + "\"latitude\"" + ":" + "\"" + locationX +"\"" + ","
                                        + "\"longitude\"" + ":" + "\"" + locationY +"\"" + "}";
                                stringBuffer.append(temp);
                                Log.d("Json parser test : ", temp);

                                //node.js connection
                                JSONTask jsonTask = new JSONTask();
                                jsonTask.setJsonData(temp);
                                jsonTask.execute("Your-Host-URL");//AsynkTask start;


                                GMailSender sender = new GMailSender(setSender, setPw);
                                sender.sendMail("Phone Locator",
                                                "X : " + locationX + "\n" +
                                                "Y : " + locationY + "\n" +
                                                "https://www.google.com/maps/place/" + Xdo + "°" + Xbun + "'" + Xcho + "\"N+" + Ydo + "°" + Ybun + "'" + Ycho + "\"E",
                                        setSender,
                                        setReceiver);
                                Log.e("sendmail", "sender : "+ setSender + " pw : " + setPw + " receiver : "+ setReceiver);
                            } catch (Exception e) {
                                Log.e("SendMail", e.getMessage(), e);
                            }
                        }
                    });
        }

        //insert in database
        public void insert(String _sender, String _pw, String _receiver){
            db = helper.getWritableDatabase();

            //if data is stored in database, delete first
            if(db != null){
                db.execSQL("DELETE FROM TEST_T");
            }
            //insert query
            ContentValues values = new ContentValues();
            values.put("SENDER", _sender);
            values.put("PW", _pw);
            values.put("RECEIVER", _receiver);
            db.insert("TEST_T", null, values);

        }

        //select from database
        public void select(){
            db = helper.getWritableDatabase();

            //select all from database
            Cursor cursor = db.rawQuery("SELECT * FROM TEST_T", null);

            //select query
            db.rawQuery("SELECT * FROM TEST_T", null);

            //get data from database
            while (cursor.moveToNext()){
                setSender = cursor.getString(cursor.getColumnIndex("SENDER"));
                setPw = cursor.getString(cursor.getColumnIndex("PW"));
                setReceiver = cursor.getString(cursor.getColumnIndex("RECEIVER"));
                Log.i("db", "sender : "+ setSender + " pw : " + setPw + " receiver : "+ setReceiver);
            }
    }



        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (Build.VERSION.SDK_INT >= 23) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TEST", "Permission: " + permissions[0] + "was " + grantResults[0]);
                    //resume tasks needing this permission
                }
            }
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        @Override
        public void onClick(View view) {

        }

    }

