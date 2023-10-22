package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.view.View;
import adapter.MessageAdapter;
import model.Message;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;

public class MainActivity extends AppCompatActivity {

    RecyclerView recycler_view;
    TextView tv_welcome;
    EditText et_msg;
    ImageButton btn_send;

    List<Message> messageList;
    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String MY_SECRET_KEY = "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";
    OkHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //연결시간 설정. 60초/120초/60초
        client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        recycler_view = findViewById(R.id.recycler_view);
        tv_welcome = findViewById(R.id.tv_welcome);
        et_msg = findViewById(R.id.et_msg);
        btn_send = findViewById(R.id.btn_send);

        recycler_view.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recycler_view.setLayoutManager(manager);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recycler_view.setAdapter(messageAdapter);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String question = et_msg.getText().toString().trim();
                addToChat(question, Message.SENT_BY_ME);
                et_msg.setText("");
                callAPI(question);
                tv_welcome.setVisibility(View.GONE);
            }
        });

    }

    void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recycler_view.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    void callAPI(String question){
        //okhttp
        messageList.add(new Message("...", Message.SENT_BY_BOT));

        //추가된 내용
        JSONArray arr = new JSONArray();
        JSONObject baseAi = new JSONObject();
        JSONObject userMsg = new JSONObject();
        try {
            //AI 속성설정
            baseAi.put("role", "user");
            baseAi.put("content", "As a counselor for INTJ patients, your approach should be warm, understanding, and sympathetic.\n" +
                    "  Engage in active listening and show genuine empathy towards their thoughts and feelings.\n" +
                    "  Avoid robotic language or overly technical explanations. Instead, use words and phrases that convey compassion and a human touch.\n" +
                    "\n" +
                    "  Remember to acknowledge the INTJ's analytical nature and respect their need for logic and clarity.\n" +
                    "  Encourage them to express their thoughts and ideas freely while providing a non-judgmental and safe space to explore their emotions.\n" +
                    "\n" +
                    "  Use open-ended questions to prompt deeper introspection and encourage them to share their experiences openly.\n" +
                    "  Validate their concerns and accomplishments, and offer encouragement for their personal growth.\n" +
                    "\n" +
                    "  Above all, prioritize building a strong rapport with the INTJ patients, allowing them to feel understood, accepted, and supported through out the therapy session.");
            //유저 메세지
            userMsg.put("role", "user");
            userMsg.put("content", question);
            //array로 담아서 한번에 보낸다
            arr.put(baseAi);
            arr.put(userMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JSONObject object = new JSONObject();
        try {
            //모델명 변경
            object.put("model", "gpt-3.5-turbo");
            object.put("messages", arr);
//            아래 put 내용은 삭제하면 된다
//            object.put("model", "text-davinci-003");
//            object.put("prompt", question);
//            object.put("max_tokens", 4000);
//            object.put("temperature", 0);

        } catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")  //url 경로 수정됨
                .header("Authorization", "Bearer "+MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        //아래 result 받아오는 경로가 좀 수정되었다.
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        addResponse(result.trim());
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to "+response.body().string());
                }
            }
        });
    }
}