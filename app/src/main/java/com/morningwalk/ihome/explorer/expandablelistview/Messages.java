package com.morningwalk.ihome.explorer.expandablelistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import com.morningwalk.ihome.explorer.JSONParser;
import com.morningwalk.ihome.explorer.NameValue;
import com.morningwalk.ihome.explorer.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Messages {
    JSONParser jsonParser = new JSONParser();
    Context context;
    String lastMsg="";
    int count = -1;

    public Messages(Context context) {
        this.context = context;
    }
    public String getMessage() { return count>0 ? lastMsg: ""; }

    public void SendMessage(int uid, int opt, String msg) {
        Register register = new Register();
        register.execute(uid+"", msg, opt + "");
    }

    //  Register Class
    @SuppressLint("NewApi")
    private class Register extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String uid = args[0];
            String message = args[1];
            String option = args[2];

            ArrayList<NameValue> params = new ArrayList<NameValue>();
            params.add(new NameValue("uid", uid));
            params.add(new NameValue("opt", option));
            params.add(new NameValue("msg", message));
            return jsonParser.makeHttpRequest(context.getString(R.string.base_url) + "message.php", "POST", params);
        }

        protected void onPostExecute(JSONObject result) {
            try {
                count = -1;
                if (result != null) {
                    String res = result.getString("success");
                    int success = Integer.parseInt(res);
                    if(success >= 0) {
                        count = success;
                        lastMsg = result.getString("message");
                   } else {
//                      makeText(getApplicationContext(), "Error reading url data", LENGTH_LONG).show();
                   }
                } else {
//                    makeText(getApplicationContext(), "Error reading url data", LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
