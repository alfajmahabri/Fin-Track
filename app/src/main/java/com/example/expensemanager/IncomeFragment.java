package com.example.expensemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expensemanager.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;


public class IncomeFragment extends Fragment {

   //Firebase database
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private FirebaseRecyclerAdapter<Data, MyViewHolder> adapter;

    //Recyclerview..

    private RecyclerView recyclerView;

    //Textview

    private TextView incomeTotalSum;

    //Update edit Text

    private EditText edtAmmount;
    private EditText edtType;
    private EditText edtNote;

    //Button for update and delete

    private Button btnUpdate;
    private Button btnDelete;

    //Data item

    private String type;
    private String note;
    private int amount;

    private String post_key;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview= inflater.inflate(R.layout.fragment_income, container, false);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid = Objects.requireNonNull(mUser).getUid();
        if (mAuth != null) {
            //FirebaseUser mUser = mAuth.getCurrentUser();
            if (mUser != null) {
                //String uid = mUser.getUid();
                mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);
            } else {
                Toast.makeText(getActivity(), "Login First...", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "mAuth is Null", Toast.LENGTH_SHORT).show();
        }

        mIncomeDatabase= FirebaseDatabase.getInstance().getReference().child("IncomeData").child(uid);

        incomeTotalSum = myview.findViewById(R.id.income_text_result);
        recyclerView=myview.findViewById(R.id.recycler_id_income);

        if(recyclerView!=null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
        }else {
            Log.e("IncomeFragment", "RecyclerView is null");
        }

        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalvalue=0;
                for (DataSnapshot mysnapshot:snapshot.getChildren()){

                    Data data=mysnapshot.getValue(Data.class);
                    totalvalue+=data.getAmount();
                    String stTotal=String.valueOf(totalvalue);
                    incomeTotalSum.setText(stTotal+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();

        adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>(options){
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MyViewHolder viewHolder, @SuppressLint("RecyclerView") int position, Data model) {
                viewHolder.setType((model.getType()));
                viewHolder.setNote(model.getNote());
                viewHolder.setDate(model.getDate());
                viewHolder.setAmount(model.getAmount());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key=getRef(position).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();
                        updateDataItem();
                    }
                });
            }

        };

        recyclerView.setAdapter(adapter);
        if(adapter!=null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mType, mNote, mDate, mAmmount;

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
             mView=itemView;
            mNote=mView.findViewById(R.id.note_txt_income);
            mType=mView.findViewById(R.id.type_txt_income);
            mDate=mView.findViewById(R.id.date_txt_income);
            mAmmount=mView.findViewById(R.id.ammount_txt_income);
        }

        private void setType(String type){
            mType.setText(type);
        }

        private void setNote(String note){
            
            mNote.setText(note);
        }

        private void setDate(String date){

            mDate.setText(date);
        }

        private void setAmount(int ammount){

            String stammount=String.valueOf(ammount);
            mAmmount.setText(stammount);
        }
    }


    @SuppressLint("MissingInflatedId")
    private void updateDataItem(){

        AlertDialog.Builder mydialog= new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myview=inflater.inflate(R.layout.update_data_item,null);
        mydialog.setView(myview);

        edtAmmount=myview.findViewById(R.id.amount_edt);
        edtType=myview.findViewById(R.id.type_edt);
        edtNote=myview.findViewById(R.id.note_edt);

        //Set data to edt text

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmmount.setText(String.valueOf(amount));
        edtAmmount.setSelection(String.valueOf(amount).length());

        btnUpdate=myview.findViewById(R.id.btnUpdate);
        btnDelete=myview.findViewById(R.id.btnDelete);

        AlertDialog dialog=mydialog.create();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type=edtType.getText().toString().trim();
                note=edtNote.getText().toString().trim();

                String mdammount=String.valueOf(amount);
                mdammount=edtAmmount.getText().toString().trim();

                int myAmmount=Integer.parseInt(mdammount);

                String mDate= DateFormat.getDateInstance().format(new Date());
                Data data=new Data(myAmmount,type,note,post_key,mDate);

                mIncomeDatabase.child(post_key).setValue(data);
                dialog.dismiss();;
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    mIncomeDatabase.child(post_key).removeValue();
                   dialog.dismiss();
            }
        });
        dialog.show();
    }
}