package com.biotecnum.crearusuariossipe;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by ingluismb on 21/10/17.
 */

public class SignIn extends AppCompatActivity {


    //
    Button logOut, mCrearUsusario, btRecargarPasajes;
    private String usuario;

    // RealTime Database Atributos
    TextView mtvName;
    private DatabaseReference mDatabaseT;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        logOut = (Button)findViewById(R.id.signOut);
        mtvName = (TextView)findViewById(R.id.tvName);

        /// Botones
        mCrearUsusario = (Button)findViewById(R.id.btCrearUsuario);
        btRecargarPasajes = (Button)findViewById(R.id.btRecargarPasajes);

        Intent intent= getIntent();
        Bundle b = intent.getExtras();

        if(b!=null) {
            usuario =(String) b.get("datos");
            //Toast.makeText(this, usuario, Toast.LENGTH_SHORT).show();
        }


        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        mCrearUsusario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignIn.this, CrearUsuario.class);
                i.putExtra("datos", usuario);
                startActivity(i);
            }
        });

        btRecargarPasajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignIn.this, RecargarPasajes.class);
                i.putExtra("datos", usuario);
                startActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mDatabaseT = FirebaseDatabase.getInstance().getReference().child("t/"+usuario);

        mDatabaseT.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoUserPass infoUP = dataSnapshot.getValue(InfoUserPass.class);

                if(infoUP == null){

                }else {

                    if(infoUP.sancion == true){
                        Toast.makeText(SignIn.this, "Cuenta deshabilitada", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }else{
                        mtvName.setText(infoUP.nombre);
                    }
                }


                /*

                dataSnapshot.getValue(RecaudoInfo.class);

                RecaudoInfo infoT = dataSnapshot.getValue(RecaudoInfo.class);

                if(infoR == null){
                    Toast.makeText(MainActivity.this, "Bus no válido", Toast.LENGTH_SHORT).show();
                }else {

                    // Actualizar fecha  aqui OJO

                    int pasajesR = infoR.pasajes;
                    int pR = pasajesR + 1;
                    mDatabaseR.child("pasajes").setValue(pR);
                }

                */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




}
