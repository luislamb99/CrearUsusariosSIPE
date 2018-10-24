package com.biotecnum.crearusuariossipe;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText email,password,name;
    private Button signin;
    private String correo = "crearUsuarios@sicpe.com", pass = "crearUsuariosSicpe";
    private boolean banderaUno = false;

    ////////DataBase
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // important Call

        signin = (Button)findViewById(R.id.signIn);
        email = (EditText)findViewById(R.id.etEmail);
        password = (EditText)findViewById(R.id.etPassword);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(MainActivity.this, "Usuario ya inició sesión ", Toast.LENGTH_SHORT).show();
                }else{
                    callsignin(correo, pass);
                }
            }
        };

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Boton Pulsado", Toast.LENGTH_SHORT).show();
                validarCampos();

                if(banderaUno == true) {

                    verificarUsuarioDataBase();

                }


            }
        });


    }

    private void validarCampos() {
        if(email.getText().toString().trim().equals("") || password.getText().toString().trim().equals("")){
            Toast.makeText(this, "Por favor, ingrese Usuario y contraseña", Toast.LENGTH_SHORT).show();
            banderaUno = false;
            email.setText("");
            password.setText("");
        }else{
            banderaUno = true;
        }
    }

    private void verificarUsuarioDataBase() {


        mDatabase = FirebaseDatabase.getInstance().getReference().child("t/"+email.getText().toString().trim());

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoUserPass infoUserPass = dataSnapshot.getValue(InfoUserPass.class);

                if(infoUserPass == null){
                    Toast.makeText(MainActivity.this, "Usuario no válido", Toast.LENGTH_SHORT).show();
                    email.setText("");
                    password.setText("");
                }else {
                    //Toast.makeText(MainActivity.this, infoUserPass.password, Toast.LENGTH_SHORT).show();

                    String p = infoUserPass.password;
                    if(p.equals(password.getText().toString().trim())){



                        /////////Lanzar nueva Actividad

                        Intent intent = new Intent(MainActivity.this, SignIn.class);
                        intent.putExtra("datos", email.getText().toString().trim());

                        email.setText("");
                        password.setText("");

                        startActivity(intent);

                        /*
                        Intent i = new Intent(MainActivity.this, SignIn.class);
                        finish();
                        startActivity(i);
                        */


                    }else{
                        email.setText("");
                        password.setText("");
                        Toast.makeText(MainActivity.this, "Usuario y/0 Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    //Now start Sign In Process
    //SignIn Process
    private void callsignin(String email,String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Falló el inicio de sesión", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Inicio de Sesión correcto", Toast.LENGTH_SHORT).show();
                    }
                    }
                });

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



}