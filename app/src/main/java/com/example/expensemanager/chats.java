package com.example.expensemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class chats extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    FloatingActionButton fbbtnhelp;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        fbbtnhelp=findViewById(R.id.fb_helpline);

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v)->{
            String question = messageEditText.getText().toString().trim();
            addToChat(question,Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });

        fbbtnhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ihelp;
                ihelp=new Intent(getApplicationContext(), HelpLine.class);
                startActivity(ihelp);
            }
        });
    }

    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }

    void callAPI(String question){
        //okhttp
        messageList.add(new Message("Typing... ",Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");
            jsonBody.put("prompt",question);
            jsonBody.put("max_tokens",4000);
            jsonBody.put("temperature",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String apiKey = BuildConfig.OPENAI_API_KEY;

        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        addResponse("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    addResponse("Failed to load response due to: " + errorBody);
                }
            }

        });





    }


    public static class DashBoardFragment extends Fragment {

        // Floating button

        private FloatingActionButton fab_main_btn;
        private FloatingActionButton fab_income_btn;
        private FloatingActionButton fab_expense_btn;

        //Floating btn textview

        private TextView fab_income_text;
        private TextView fab_expense_text;


        //boolean
        private boolean isOpen = false;

        //Animation

        private Animation FadOpen, FadeClose;

        //Dashboard income and expense result..

        private TextView totalIncomeResult;
        private TextView totalExpenseResult;

        //Firebase...
        private FirebaseAuth mAuth;
        private DatabaseReference mIncomeDatabase;
        private DatabaseReference mExpenseDatabase;

        //Recycler view

        private RecyclerView mRecyclerIncome;
        private RecyclerView mRecyclerExpense;


        @SuppressLint("MissingInflatedId")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View myview = inflater.inflate(R.layout.fragment_dash_board, container, false);

            mAuth=FirebaseAuth.getInstance();
            FirebaseUser mUser=mAuth.getCurrentUser();
            String uid= Objects.requireNonNull(mUser).getUid();

            mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
            mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

            //mIncomeDatabase.keepSynced(true);
            //mExpenseDatabase.keepSynced(true);

            //connect floating button to layout
            fab_main_btn=myview.findViewById(R.id.fb_main_plus_btn);
            fab_income_btn=myview.findViewById(R.id.income_Ft_btn);
            fab_expense_btn=myview.findViewById(R.id.expense_Ft_btn);

            //Connect floating text

            fab_income_text=myview.findViewById(R.id.income_ft_text);
            fab_expense_text=myview.findViewById(R.id.expense_ft_text);

            //Total income and expense result set...

            totalIncomeResult=myview.findViewById(R.id.income_set_result);
            totalExpenseResult=myview.findViewById(R.id.expense_set_result);

            //Recycler

            mRecyclerIncome=myview.findViewById(R.id.recycler_income);
            mRecyclerExpense=myview.findViewById(R.id.recycler_expense);

           // Animation Connect

            FadOpen = AnimationUtils.loadAnimation(getActivity(),R.anim.fade_open);
            FadeClose= AnimationUtils.loadAnimation(getActivity(),R.anim.fade_close);

            fab_main_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addData();
                    ftAnimation();

                }
            });

            //Calculate total income

            mIncomeDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalsum=0;
                    for(DataSnapshot mysnap:snapshot.getChildren()){
                        Data data = mysnap.getValue(Data.class);
                        totalsum+= data.getAmount();

                        String stResult=String.valueOf(totalsum);

                        totalIncomeResult.setText(stResult+".00");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Calculate total expense

            mExpenseDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalsum=0;
                    for(DataSnapshot mysnapshot:snapshot.getChildren()){

                        Data data = mysnapshot.getValue(Data.class);
                        totalsum+= data.getAmount();

                        String strTotalSum=String.valueOf(totalsum);

                        totalExpenseResult.setText(strTotalSum+".00");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Recycler

            LinearLayoutManager layoutManagerIncome=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
            layoutManagerIncome.setStackFromEnd(true);
            layoutManagerIncome.setReverseLayout(true);
            mRecyclerIncome.setHasFixedSize(true);
            mRecyclerIncome.setLayoutManager(layoutManagerIncome);


            LinearLayoutManager layoutManagerExpense= new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
            layoutManagerExpense.setReverseLayout(true);
            layoutManagerExpense.setStackFromEnd(true);
            mRecyclerExpense.setHasFixedSize(true);
            mRecyclerExpense.setLayoutManager(layoutManagerExpense);

            return myview;
        }

        //Floating button animation

        private void ftAnimation(){
            if(isOpen){
                fab_income_btn.startAnimation(FadeClose);
                fab_expense_btn.startAnimation(FadeClose);
                fab_income_btn.setClickable(false);
                fab_expense_btn.setClickable(false);
                fab_income_text.startAnimation(FadeClose);
                fab_expense_text.startAnimation(FadeClose);
                fab_income_text.setClickable(false);
                fab_expense_text.setClickable(false);
                isOpen=false;
            }else {
                fab_income_btn.startAnimation(FadOpen);
                fab_expense_btn.startAnimation(FadOpen);
                fab_income_btn.setClickable(true);
                fab_expense_btn.setClickable(true);
                fab_income_text.startAnimation(FadOpen);
                fab_expense_text.startAnimation(FadOpen);
                fab_income_text.setClickable(true);
                fab_expense_text.setClickable(true);
                isOpen=true;
            }
        }

        private void addData(){
            // Fab Button income
            fab_income_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    incomeDataInsert();
                }
            });

            // Fab Button expense
            fab_expense_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expenseDataInsert();
                }
            });
        }

        public void incomeDataInsert(){

            AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());

            LayoutInflater inflater= LayoutInflater.from(getActivity());

            View myview=inflater.inflate(R.layout.custom_layout_for_insert_data,null);
            mydialog.setView(myview);
            final AlertDialog dialog=mydialog.create();

            dialog.setCancelable(false);

            final EditText edtAmount=myview.findViewById(R.id.amount_edt);
            final EditText edtType = myview.findViewById(R.id.type_edt);
            final EditText edtNote=myview.findViewById(R.id.note_edt);

            Button btnSave=myview.findViewById(R.id.btnSave);
            Button btnCancel=myview.findViewById(R.id.btnCancel);

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String type=edtType.getText().toString().trim();
                    String amount=edtAmount.getText().toString().trim();
                    String note=edtNote.getText().toString().trim();

                    if(TextUtils.isEmpty(type)){
                        edtType.setError("Required Field...");
                        return;
                    }
                    if(TextUtils.isEmpty(amount)){
                        edtAmount.setError("Required Field...");
                        return;
                    }
                    int ouramountint=Integer.parseInt(amount);

                    if(TextUtils.isEmpty(note)){
                        edtNote.setError("Required Field...");
                        return;
                    }

                    String id=mIncomeDatabase.push().getKey();

                    String mDate= DateFormat.getDateInstance().format(new Date());

                    Data data = new Data(ouramountint,type,note,id,mDate);



                    mIncomeDatabase.child(Objects.requireNonNull(id)).setValue(data)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    ftAnimation();
                    dialog.dismiss();

                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ftAnimation();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        public void expenseDataInsert(){
            AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View myview = inflater.inflate(R.layout.custom_layout_for_insert_data,null);
            mydialog.setView(myview);

            final AlertDialog dialog=mydialog.create();
            dialog.setCancelable(false);

            final EditText amount=myview.findViewById(R.id.amount_edt);
            final EditText type=myview.findViewById(R.id.type_edt);
            final EditText note=myview.findViewById(R.id.note_edt);

            Button btnSave=myview.findViewById(R.id.btnSave);
            Button btnCancel=myview.findViewById(R.id.btnCancel);

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tmAmount=amount.getText().toString().trim();
                    String tmtype=type.getText().toString().trim();
                    String tmnote=note.getText().toString().trim();

                    if(TextUtils.isEmpty(tmAmount)){
                        amount.setError("Required Field...");
                        return;
                    }
                    int inamount=Integer.parseInt(tmAmount);
                    if(TextUtils.isEmpty(tmtype)){
                        type.setError("Required Field...");
                        return;
                    }
                    if(TextUtils.isEmpty(tmnote)){
                        note.setError("Required Field...");
                        return;
                    }

                    String id=mExpenseDatabase.push().getKey();
                    String mDate= DateFormat.getDateInstance().format(new Date());

                    Data data= new Data(inamount,tmtype,tmnote,id,mDate);
                    mExpenseDatabase.child(Objects.requireNonNull(id)).setValue(data);

                    Toast.makeText(getActivity(), "Data Added", Toast.LENGTH_SHORT).show();
                    ftAnimation();
                    dialog.dismiss();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ftAnimation();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        @Override
        public void onStart() {
            super.onStart();

            FirebaseRecyclerOptions<Data> options =
                    new FirebaseRecyclerOptions.Builder<Data>()
                            .setQuery(mIncomeDatabase, Data.class)
                            .build();
            FirebaseRecyclerAdapter<Data, IncomeViewHolder> incomeAdapter=new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                    holder.setmIncomeType(model.getType());
                    holder.setIncomeAmmount(model.getAmount());
                    holder.setIncomeDate(model.getDate());
                }

                @NonNull
                @Override
                public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income, parent, false);
                    return new IncomeViewHolder(view);
                }
            };
            mRecyclerIncome.setAdapter(incomeAdapter);
            incomeAdapter.startListening();

            FirebaseRecyclerOptions<Data> optionsexpense =
                    new FirebaseRecyclerOptions.Builder<Data>()
                            .setQuery(mExpenseDatabase, Data.class)
                            .build();

            FirebaseRecyclerAdapter<Data, ExpenseViewHolder>expenseAdapter=new FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(optionsexpense) {
                @Override
                protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {

                    holder.setExpenseType(model.getType());
                    holder.setExpenseAmmount(model.getAmount());
                    holder.setExpenseDate(model.getDate());
                }

                @NonNull
                @Override
                public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense, parent, false);
                    return new ExpenseViewHolder(view);
                }
            };

            mRecyclerExpense.setAdapter(expenseAdapter);
            expenseAdapter.startListening();
        }

        //For Income Data
        public static class IncomeViewHolder extends RecyclerView.ViewHolder{

            View mIncomeView;
            TextView mtype, mAmmount, mDate;
            public IncomeViewHolder(View itemView){
                super(itemView);
                mIncomeView=itemView;
                mtype=itemView.findViewById(R.id.type_Income_ds);
                mAmmount=itemView.findViewById(R.id.amount_Income_ds);
                mDate=itemView.findViewById(R.id.date_income_ds);
            }

            public void setmIncomeType(String type){
                mtype.setText(type);
            }

            public void setIncomeAmmount(int ammount){
                String strAmmount=String.valueOf(ammount);
                mAmmount.setText(strAmmount);
            }

            public void setIncomeDate(String date){
                mDate.setText(date);
            }
        }

        //For Expense Data
        public static class ExpenseViewHolder extends RecyclerView.ViewHolder{

            TextView mtype, mAmmount, mDate;
            View mExpenseView;

            public ExpenseViewHolder(View itemView){
                super(itemView);
                mtype=itemView.findViewById(R.id.type_Expense_ds);
                mAmmount=itemView.findViewById(R.id.amount_Expense_ds);
                mDate=itemView.findViewById(R.id.date_expense_ds);
            }

            public void setExpenseType(String type){
                mtype.setText(type);
            }

            public void setExpenseAmmount(int amount){
                String strAmmount=String.valueOf(amount);
                mAmmount.setText(strAmmount);
            }
            public void setExpenseDate(String date){
                mDate.setText(date);
            }
        }
    }
}