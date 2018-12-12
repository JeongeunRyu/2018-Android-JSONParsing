package com.andrstudy.finaltest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public String myJSON;
    private ListView listView;
    private static String ip = null;
    private static final String TAG_RESULTS = "result";
    private static final String TAG_MENU = "menuName";
    private static final String TAG_PRICE = "price";
    private static final String TAG_SPECIAL = "isSpecial";

    private ImageView pop;
    private SharedPreferences preferences;

    //오늘의 메뉴
    private TextView todayName;
    private TextView todayPrice;

    JSONArray menus = null;
    private ArrayList<HashMap<String, String>> menuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menuList = new ArrayList<HashMap<String, String>>();
        listView=findViewById(R.id.listView);
        todayName = findViewById(R.id.todayName);
        todayPrice = findViewById(R.id.todayPrice);
        pop = findViewById(R.id.pop);
        preferences = getSharedPreferences("ip",MODE_PRIVATE);
        String getIp = preferences.getString("ip", null);

        if(getIp !=null)
            getData("http://"+getIp+"/finaltest/server.php");
    }

    public void getData(String url){
        class GetDataJson extends AsyncTask<String, Void, String>{
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                if(params==null || params.length<1)
                    return null;
                BufferedReader bufferedReader = null;
                try{
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setDoInput(true);
                    con.setUseCaches(false);
                    con.setDefaultUseCaches(false);
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!=null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                showList();
            }
        }
        GetDataJson g = new GetDataJson();
        g.execute(url);
    }


    protected void showList(){
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            menus = jsonObj.getJSONArray(TAG_RESULTS);
            for(int i=0; i<menus.length(); i++){
                JSONObject c = menus.getJSONObject(i);
                String menuName = c.getString(TAG_MENU);
                String price = c.getString(TAG_PRICE);
                String isSpecial = c.getString(TAG_SPECIAL);

                HashMap<String, String> menusArray = new HashMap<String,String>();
                menusArray.put(TAG_MENU, menuName);
                menusArray.put(TAG_PRICE, price);
                menuList.add(menusArray);
                if(isSpecial == "true"){
                    todayName.setText(menuName);
                    todayPrice.setText(price);
                }

            }

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, menuList, R.layout.linear_layout,
                    new String[]{TAG_MENU, TAG_PRICE},
                    new int[]{R.id.id, R.id.price,}
            );
            listView.setAdapter(adapter);
        }catch(JSONException e){
            e.printStackTrace();
        }

    }


    public void alertShow(View view) {
        final EditText editText = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ip 입력창");
        builder.setMessage("서버의 ip를 입력해주세요.");
        builder.setView(editText);
        builder.setPositiveButton("입력",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ip = editText.getText().toString();
                        Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("ip", ip);
                        editor.commit();
                        getData("http://"+ip+"/finaltest/server.php");
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }
}