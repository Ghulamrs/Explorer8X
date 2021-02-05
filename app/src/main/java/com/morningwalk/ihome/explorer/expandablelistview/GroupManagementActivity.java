package com.morningwalk.ihome.explorer.expandablelistview;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import com.morningwalk.ihome.explorer.JSONParser;
import com.morningwalk.ihome.explorer.NameValue;
import com.morningwalk.ihome.explorer.R;
import com.morningwalk.ihome.explorer.UserPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.widget.Toast.*;

public class GroupManagementActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    JSONParser jsonParser = new JSONParser();
    ExpandableListView listView;
    SearchView searchView;
    ListAdapter adapter;
    UserPreferences up;
    String groupName;
    int option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_group_management);
        listView = findViewById(R.id.expandable_listview);

        up = UserPreferences.Shared(getBaseContext());
        String options = getIntent().getStringExtra("option");
        option = Integer.parseInt(options);
        groupName = "";

        if (option >= 14) {
            searchView = findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);

            Register register = new Register();
            register.execute(up.getPid()+"", "dummy", option+"", "");
        }
        else {
            // Used to display New group screen for input name
            setContentView(R.layout.activity_group_creation);
        }
    }

    public void display(JSONObject job) {
        adapter = new ListAdapter(getApplicationContext(), job, this.option);
        listView.setAdapter(adapter);
    }

    public void nothing_to_display() {
        if(this.option == 11) {
            makeText(getApplicationContext(), "Group named '"+ groupName + "' already exist!", LENGTH_LONG).show();
            finish();
        }
    }

    public void onSubmitButton(View view) {
        boolean cancel = false;
        View focusView = null;

        EditText mNewName = (EditText) findViewById (R.id.edittext);
        mNewName.setError(null);
        groupName = mNewName.getText().toString();
        int length = groupName.length();
        if(length < 3 || length > 16) {
            if(length > 16) mNewName.setError("Name cannot exceed 16 chars!");
            else mNewName.setError("Name must have at least 3 chars!");
            focusView = mNewName;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
//        makeText(getApplicationContext(), "Submit button clicked", LENGTH_LONG).show();
        }
        else { // switch to list/search view
            setContentView(R.layout.activity_group_management);
            listView = findViewById(R.id.expandable_listview);

            Register register = new Register();
            register.execute(up.getPid() + "", groupName, option+"", "");
            searchView = findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }
    }

    public void onCancelButton(View view) {
//        makeText(getApplicationContext(), "Cancel button clicked", LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onClose() {
        adapter.filterData("");
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        adapter.filterData(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filterData(s);
        return false;
    }

    private class Register extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String uid = args[0];
            String name = args[1];
            String option = args[2];

            ArrayList<NameValue> params = new ArrayList<NameValue>();
            params.add(new NameValue("uid", uid));
            params.add(new NameValue("name", name));
            params.add(new NameValue("option", option)); // 3rd parameter option
            return jsonParser.makeHttpRequest(getString(R.string.base_url) + "glogin.php", "POST", params);
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    String res = result.getString("result");
                    if (res.length() > 0) display(result);
                    if(Integer.parseInt(res)==0) nothing_to_display();
                } else {
                    makeText(getApplicationContext(), "Error reading url data", LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
