package com.morningwalk.ihome.explorer.expandablelistview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.morningwalk.ihome.explorer.JSONParser;
import com.morningwalk.ihome.explorer.NameValue;
import com.morningwalk.ihome.explorer.R;
import com.morningwalk.ihome.explorer.UserPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListAdapter extends BaseExpandableListAdapter {
    Context context;
    ArrayList<Group> groupList;
    ArrayList<Group> originalGroupList;
    JSONParser jsonParser = new JSONParser();
    UserPreferences up;
    // Messager vars set
    Messages xMessage;
    String message="";
    int  msgReceiver = 0;

    private int option; // View option Admin/Member or New Group
    private int admin = 0, delete_count = 0; // Is it me
    public boolean meingroup = false;
    String query=""; // Remember last query string for filterData()

    ListAdapter(Context context, JSONObject job, int option) {
        this.context = context;
        up = UserPreferences.Shared(this.context);
        xMessage = new Messages(context);
        this.option = option;
        delete_count = 0; // incremented to 3 before deleting a group with only admin member - requesting to delete it!

        updateData(job);
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return groupList.get(i).getItems().size();
    }

    @Override
    public Object getGroup(int i) {
        return groupList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return groupList.get(i).getItems().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Group group = (Group)getGroup(i);
        if(view == null) {
            LayoutInflater layoutInflter = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflter.inflate(R.layout.list_group, null); // list_group file
        }

        meingroup = checkme(i);
        TextView group_header = view.findViewById(R.id.list_group); // list_group id for group heading
        group_header.setTypeface(null, Typeface.BOLD);
        group_header.setText(group.getTitle());
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        Member member = (Member)getChild(i, i1);
        if(view == null) {
            LayoutInflater layoutInflter = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflter.inflate(R.layout.list_member, null); // list_group file
        }

        ImageView image = (ImageView) view.findViewById(R.id.image_member);
        int status = member.getStatus();
        if(status > 1) image.setImageResource( R.drawable.admin);
        else if(status == 1) image.setImageResource(R.drawable.member);
        else image.setImageResource(R.drawable.request);

        ImageView action = (ImageView) view.findViewById(R.id.action_member);
        ArrayList<Member> mlist = groupList.get(i).getItems();
        if(admin == 1) {
            if (status < 1) action.setImageResource(R.drawable.image_insert);
            else if (status == 1 || mlist.size() == 1) action.setImageResource(R.drawable.image_delete);
            else action.setImageResource(R.drawable.image_disable);
        }
        else {
            if(status < 1)  action.setImageResource(R.drawable.image_disable);
            else if (status == 1) { // Disable all actions except one can delete himself
                if(member.getId() == up.getPid()) action.setImageResource(R.drawable.image_delete);
                else action.setImageResource(R.drawable.image_disable);
            }
            else { // Disable admin action - as me already in the group
                if(meingroup) action.setImageResource(R.drawable.image_disable);
                else action.setImageResource(R.drawable.image_insert);
            }
        }

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Member member = (Member)getChild(i, i1);
                ArrayList<Member> list = groupList.get(i).getItems();
                int status = member.getStatus();

                msgReceiver = 0;
                if (admin == 1) {
                    msgReceiver = member.getId();
                    if (status == 1) {
                        message = "Sorry " + member.getName() + ": Your request to join " + groupList.get(i).getTitle() + " group is declined!";
                        service_request(up.getPid() + "", groupList.get(i).getTitle(), "-" + member.getId(), "alogin.php", i + "", "");
                    } else if (list.size() == 1) {
                        if(++delete_count == 3) {
                            service_request(up.getPid() + "", groupList.get(i).getTitle(), "-" + member.getId(), "alogin.php", i + "", "");
                            notifyDataSetChanged();
                            delete_count = 0;
                        }
                    } else {
                        message = "Hello " + member.getName() + ": " + up.getName() + " has acknowledged you as member of " + groupList.get(i).getTitle() + " group!";
                        service_request(up.getPid() + "", groupList.get(i).getTitle(),  "" + member.getId(), "alogin.php", i + "", "");
                    }
                } else {
                    if (status == 0) return;
                    msgReceiver = groupList.get(i).getAdminId();
                    if (status == 1) {
                        if (member.getId() == up.getPid()) {
                            message = "Hello "+groupList.get(i).getAdmin() +": " + up.getName() + " has left the " + groupList.get(i).getTitle() + " group!";
                            service_request(up.getPid() + "", groupList.get(i).getTitle(), "13", "glogin.php", "");
                        }
                    } else {
                        message = "Hello " + groupList.get(i).getAdmin() + ": " + up.getName() + " has requested for membership of " + groupList.get(i).getTitle() + " group!";
                        service_request(up.getPid() + "", groupList.get(i).getTitle(), "12", "glogin.php", "");
                    }
                }
            }
        });

        TextView textView = view.findViewById(R.id.text_member);
        textView.setText(member.getName());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    boolean checkme(int groupPosition) {
        ArrayList<Member> list = groupList.get(groupPosition).getItems();
        for(Member item:list) {
            if(item.getId()==up.getPid()) return true;
        }
        return false;
    }

    private void updateGroup(JSONObject job, int index) { // updates only the members of required group -
        ArrayList<Member> mlist = new ArrayList<>(); // in response to admin's activity using alogin.php script
        try {
            JSONArray members = job.getJSONArray("members");
            for (int j = 0; j < members.length (); j++) {
                JSONObject member = members.getJSONObject(j);
                String name = member.getString("name"); // only to determine admin - which is me!
                int of = Integer.parseInt(member.getString("of"));
                if(name.equals(up.getName())) mlist.add(new Member(member.getInt("id"), name, 2));
                else mlist.add(new Member(member.getInt("id"), name, of));
            }

            Group group = groupList.remove(index);
            if(mlist.size() > 0) groupList.add(index, new Group(group.getTitle(), group.getAdmin(), mlist));
            notifyDataSetChanged();
            if(msgReceiver > 0 && message.length() > 10) {
                xMessage.SendMessage(msgReceiver, 2, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateData(JSONObject job) {
        JSONArray group, groups;

        try {
            group = job.getJSONArray("group");
            groups = job.getJSONArray("groups");

            admin = (option==14 ? 1 : 0);
            ArrayList<String> gname = new ArrayList<>();
            for (int i = 0; i < group.length (); i++) {
                JSONObject jgroup = group.getJSONObject(i);
                gname.add(jgroup.getString("name"));
            }

            originalGroupList = new ArrayList<>();
            for (int i = 0; i < groups.length (); i++) {
                JSONObject jgroup = groups.getJSONObject(i);
                String admins = jgroup.getString("admin");
                JSONArray members = jgroup.getJSONArray("members");

                ArrayList<Member> mlist = new ArrayList<>();
                for (int j = 0; j < members.length (); j++) {
                    JSONObject member = members.getJSONObject(j);
                    int id = Integer.parseInt(member.getString("id"));
                    int of = Integer.parseInt(member.getString("of"));
                    String name = member.getString("name"); // To find admin member from member's list
                    if(admins.equals(name)) mlist.add(new Member(id, name, 2));  // status admin=2
                    else if(of==1 || option==14) mlist.add(new Member(id, name, of));   // member=1
                    else if(id==up.getPid()) mlist.add(new Member(id, name, 0)); // Show only my request
                }
                if(groups.length()==group.length()) originalGroupList.add(new Group(gname.get(i), admins, mlist));
            }
            filterData(query);
            if(msgReceiver > 0 && message.length() > 10) {
                xMessage.SendMessage(msgReceiver, 2, message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void filterData(String query) {
        this.query = query; // remember last query string
        if(query.isEmpty()) {
            groupList = originalGroupList;
        } else {
            query = query.toLowerCase();
            groupList = new ArrayList<>();
            for(Group group: originalGroupList) {
                if(group.getTitle().toLowerCase().startsWith(query)) {
                    groupList.add(group);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void service_request(String... args) {
        Register register = new Register();
        register.execute(args);
    }

    //  Register Class
    @SuppressLint("NewApi")
    private class Register extends AsyncTask<String, String, JSONObject> {
        int opt = -1;
        int group = -1;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String uid = args[0];
            String name = args[1];
            String option = args[2];

            if(args[3].equals("alogin.php")) { this.opt = 9; group = Integer.parseInt(args[4]); }
            else this.opt = Integer.parseInt(option);
            ArrayList<NameValue> params = new ArrayList<NameValue>();
            params.add(new NameValue("uid", uid));
            params.add(new NameValue("name", name));
            params.add(new NameValue("option", option)); // 3rd parameter option
            return jsonParser.makeHttpRequest(context.getString(R.string.base_url) + args[3], "POST", params);
        }

        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    String res = result.getString("result");
                    if (res.length() > 0) {
                        if(this.opt > 9) updateData(result);
                        else updateGroup(result, this.group);
                    } else {
//                        makeText(getApplicationContext(), "Error reading url data", LENGTH_LONG).show();
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
