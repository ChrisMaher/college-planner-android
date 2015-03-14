package com.chris.collegeplanner.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.chris.collegeplanner.R;
import com.chris.collegeplanner.helper.JSONParser;
import com.chris.collegeplanner.helper.SessionManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddNewGroupNoteActivity extends ActionBarActivity {

    JSONParser jsonParser = new JSONParser();

    private SessionManager session;
    private EditText detailsText;

    private Button saveButton2;
    List<String> subjectsArray = new ArrayList<>();
    AutoCompleteTextView subjectGroupText;
    private String urlUpload = "http://chrismaher.info/AndroidProjects2/group_notes_upload.php";
    private static final String subjectsURL = "http://chrismaher.info/AndroidProjects2/subjects.php";
    // Progress Dialog
    private ProgressDialog pDialog;
    List<HashMap<String, String>> fillSubjectsArray;


    // This is the date picker used to select the date for our notification
    private DatePicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_group_note);
        setTitle("Add New Group Note");

        // Instantiate
        //    details = (EditText)   findViewById(R.id.Details);

        subjectGroupText = (AutoCompleteTextView) findViewById(R.id.SubjectSpinnerGroup);
        subjectGroupText.setThreshold(1);
        session = new SessionManager(getApplicationContext());
        detailsText = (EditText) findViewById(R.id.NoteDetails);
        saveButton2 = (Button) findViewById(R.id.SaveButtonGroup);
        saveButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetworkAvailable()) {

                    // Date Validation
                    if (detailsText.getText().toString().matches("")) {

                        Toast.makeText(getApplicationContext(), "No Note Entered.", Toast.LENGTH_LONG).show();


                    } else {

                     new CreateNewProject().execute();

                    }


                } else {

                    Toast.makeText(getApplicationContext(), "Internet Connection Required.", Toast.LENGTH_LONG).show();

                }

            }
        });

        if(isNetworkAvailable()){

            getWebData();

        }else{

            Toast.makeText(getApplicationContext(), "Internet Connection Required.", Toast.LENGTH_LONG).show();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to check if device has internet connection.
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getWebData() {

        addSubjectsToListBackgroundTask task = new addSubjectsToListBackgroundTask();
        task.execute(new String[]{subjectsURL});

    }

    class addSubjectsToListBackgroundTask extends AsyncTask<String, Void, String> {

        private String jsonResult;


        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);
            try {
                HttpResponse response = httpclient.execute(httppost);
                jsonResult = inputStreamToString(
                        response.getEntity().getContent()).toString();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {
                // e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error..." + e.toString(), Toast.LENGTH_LONG).show();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            ListMaker();
        }

        public void ListMaker() {

            List<Map<String, String>> projectList = new ArrayList<Map<String, String>>();
            fillSubjectsArray = new ArrayList<HashMap<String, String>>();
            String[] from = new String[]{"projectSubject"};
            int[] to = new int[]{R.id.subjectName};


            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray jsonMainNode = jsonResponse.optJSONArray("subjects");

                Subject subjects = new Subject();


                for (int i = 0; i < jsonMainNode.length(); i++) {
                    JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                    subjects.setSubjectName(jsonChildNode.optString("projectSubject"));



                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("projectSubject", subjects.getSubjectName());
                    subjectsArray.add(map.get("projectSubject"));


                    fillSubjectsArray.add(map);

                }


            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Nothing Added Yet.", Toast.LENGTH_SHORT).show();
            }


            //   adapter = new SimpleAdapter(getApplicationContext(), fillCollegesArray, R.layout.layout_college_list, from, to);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, subjectsArray);

            subjectGroupText.setAdapter(adapter2);






        }

        ;


    }

    // Background task to Enter Details into Database
    class CreateNewProject extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pDialog = new ProgressDialog(AddNewGroupNoteActivity.this);
//            pDialog.setMessage("Creating Project..");
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(true);
//            pDialog.show();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {

            String subject = subjectGroupText.getText().toString();
            String details = detailsText.getText().toString();

            String projectEmail = session.getUserDetails();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("GroupAuthorEmail", projectEmail));
            params.add(new BasicNameValuePair("GroupNoteSubject", subject));
            params.add(new BasicNameValuePair("GroupNoteText", details));


            // getting JSON Object
            // Note that create product url accepts POST method
            Log.d("PARAMS HERE ", params.toString());
            JSONObject json = jsonParser.makeHttpRequest(urlUpload,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt("success");

                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), GroupNotesActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;


        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            //    pDialog.dismiss();
        }

    }


}