package com.mirrorgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Queue;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    Button con, discon;
    EditText name, id, eTime, email, pass, timer;
    private  static final String KEY_NAME = "name_key";
    private  static final String KEY_ID = "id_key";
    Switch p2p, local;
    TextView mname, mid, trail, tTime, ttimer;
    Spinner sTrail;
    String[] trailData;
    boolean isLocal = true;
    //FirebaseAuth auth;
    //FirebaseUser firebaseUser;
    //DatabaseReference usersRef;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //auth = FirebaseAuth.getInstance();
        //usersRef = FirebaseDatabase.getInstance().getReference().child("Traillist");
        trailData =new String[] {getString(R.string.dot1),getString(R.string.dot2),getString(R.string.dot3),getString(R.string.dot4),getString(R.string.dot5),getString(R.string.dot6),getString(R.string.dot7),getString(R.string.dot8),getString(R.string.dot9),getString(R.string.dot10),getString(R.string.dot11) };
        name = (EditText) findViewById((R.id.editTextPersonName));
        id = (EditText) findViewById((R.id.editTextTextPersonID));
        try {
            name.setText(getIntent().getExtras().getString("Name"));
            id.setText(getIntent().getExtras().getString("Id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sTrail = (Spinner) findViewById(R.id.spinnerTrail);
        trail = (TextView) findViewById(R.id.textViewShowTrail);
        mname = (TextView) findViewById(R.id.textViewShowName);
        mid = (TextView) findViewById(R.id.textViewShowID);
        p2p = (Switch) findViewById(R.id.switchP2P);
        local = (Switch) findViewById(R.id.switchfirebaselocal);
        tTime = (TextView) findViewById(R.id.textViewTimeBPoint);
        eTime = (EditText) findViewById(R.id.editTextTimeBPoint);
        ttimer = (TextView) findViewById(R.id.textViewTimer);
        timer = (EditText) findViewById(R.id.editTextTimer);
        email = (EditText) findViewById(R.id.email);
        pass = (EditText) findViewById(R.id.password);
        con = (Button) findViewById(R.id.buttonconnect);
        discon = (Button) findViewById(R.id.buttondisconnect);
        email.setVisibility(View.INVISIBLE);
        pass.setVisibility(View.INVISIBLE);
        con.setVisibility(View.INVISIBLE);
        discon.setVisibility(View.INVISIBLE);
        if (savedInstanceState != null){
            String savedName = savedInstanceState.getString(KEY_NAME);
            mname.setText(savedName);
            String savedID = savedInstanceState.getString(KEY_ID);
            mid.setText(savedID);
        }else{
            Toast.makeText(this, R.string.newEntry, Toast.LENGTH_SHORT).show();
        }
        if (isLocal) {
            email.setVisibility(View.INVISIBLE);
            pass.setVisibility(View.INVISIBLE);
            con.setVisibility(View.INVISIBLE);
            discon.setVisibility(View.INVISIBLE);

            setsTraillocal();
        }
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveViewName();
            }
        });

        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveViewId();
            }
        });

        sTrail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                trail.setText(sTrail.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        p2p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p2p.isChecked()) {
                    sTrail.setVisibility(View.INVISIBLE);
                    trail.setVisibility(View.INVISIBLE);
                    tTime.setVisibility(View.INVISIBLE);
                    eTime.setVisibility(View.INVISIBLE);
                    ttimer.setVisibility(View.INVISIBLE);
                    timer.setVisibility(View.INVISIBLE);
                    local.setVisibility(View.INVISIBLE);
                    if (!local.isChecked()) {
                        email.setVisibility(View.VISIBLE);
                        pass.setVisibility(View.VISIBLE);
                        con.setVisibility(View.VISIBLE);
                        discon.setVisibility(View.VISIBLE);
                    }

                }else{
                    sTrail.setVisibility(View.VISIBLE);
                    trail.setVisibility(View.VISIBLE);
                    tTime.setVisibility(View.VISIBLE);
                    eTime.setVisibility(View.VISIBLE);
                    ttimer.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.VISIBLE);
                    local.setVisibility(View.VISIBLE);
                    email.setVisibility(View.INVISIBLE);
                    pass.setVisibility(View.INVISIBLE);
                    con.setVisibility(View.INVISIBLE);
                    discon.setVisibility(View.INVISIBLE);
                }
            }
        });
        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (local.isChecked()) {
                    //email.setVisibility(View.INVISIBLE);
                    //pass.setVisibility(View.INVISIBLE);
                    //con.setVisibility(View.INVISIBLE);
                    //discon.setVisibility(View.INVISIBLE);
                    isLocal = true;
                    setsTraillocal();
                }else{
                    isLocal = false;
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myref =database.getReference().child("Traillist");
                    Query query = myref.orderByKey();
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            firebasechange(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), R.string.errorFirebase, Toast.LENGTH_LONG).show();

                            Log.e("Error Firebase", "Failed to read value.", databaseError.toException());
                        }
                    });
                    //email.setVisibility(View.VISIBLE);
                    //pass.setVisibility(View.VISIBLE);
                    //con.setVisibility(View.VISIBLE);
                    //discon.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        saveInstanceState.putString(KEY_NAME, mname.getText().toString());
        saveInstanceState.putString(KEY_ID, mid.getText().toString());

        super.onSaveInstanceState(saveInstanceState);
    }

    public void onItemSelected1( View view){
        sTrail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                trail.setText(sTrail.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void saveViewName(){
        mname.setText(name.getText().toString().trim());
    }
    public void saveViewId(){
        mid.setText(id.getText().toString().trim());
    }
    public void setsTraillocal(){
        changesTrail(trailData);
    }
    private void changesTrail(String[] data){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sTrail.setAdapter(adapter);
    }
    private void firebasechange(DataSnapshot dataSnapshot){
        int len = (int)dataSnapshot.getChildrenCount();
        String[] dataFirebase = new String[len];
        String value;
        int i = 0;
        for(DataSnapshot child: dataSnapshot.getChildren()){
            value = child.getKey();
            Log.i("Firebase", "Value is: " + value);
            dataFirebase[i++] = value;
        }
        changesTrail(dataFirebase);
    }
    public void comfigureNextButton(View view){
        if (name.getText().toString().length()>0 && id.getText().toString().length()>=4) {
            saveViewId();
            saveViewName();
            Intent secScreGa;
            if (p2p.isChecked()){
                secScreGa = new Intent(MainActivity.this, MainScreenP2P.class);
                secScreGa.putExtra("screen", getString(R.string.WifiP2P));
            }else {
                secScreGa = new Intent(MainActivity.this, MainScreenP2P.class);
                secScreGa.putExtra("screen", getString(R.string.screenGame));
                secScreGa.putExtra("local", isLocal);
                EditText time = (EditText) findViewById(R.id.editTextTimeBPoint);
                String data = time.getText().toString().trim();
                if (data.length() == 0) {
                    data = "10";
                }
                EditText timer_1 = (EditText) findViewById(R.id.editTextTimer);
                String timer_s = timer_1.getText().toString().trim();
                if (timer_s.length() == 0) {
                    timer_s = "10";
                }
                secScreGa.putExtra("timer", timer_s);
                secScreGa.putExtra("timeMillis", data);
                secScreGa.putExtra("trail", trail.getText());
            }
            secScreGa.putExtra("Name", name.getText().toString());
            secScreGa.putExtra("Id", id.getText().toString());
            startActivity(secScreGa);
            finish();
        }else{
            Toast.makeText(getApplicationContext(), R.string.errorNameOrId, Toast.LENGTH_LONG).show();
        }
    }
    public void disconnectFirebase(View view) {
       /* auth.signOut();
        con.setEnabled(true);
        email.setEnabled(true);
        pass.setEnabled(true);
        discon.setEnabled(false);*/
    }
        public void connectFirebase(View view) {
           /* auth.createUserWithEmailAndPassword(email.getText().toString(),
                pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), R.string.user_successfully, Toast.LENGTH_LONG).show();
                    con.setEnabled(false);
                    email.setEnabled(false);
                    pass.setEnabled(false);
                    discon.setEnabled(true);
                    //firebaseUser = auth.getCurrentUser();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.user_not_registered, Toast.LENGTH_LONG).show();
                }
            }
        });*/
    }

}