package com.biotecnum.crearusuariossipe;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Locale;

public class RecargarPasajes extends AppCompatActivity {

    private EditText etPasajesNuevos;
    private TextView tvTagId, tvNombre, tvPasajesActuales, tvMensaje2;
    private Button btRecargarP;

    private boolean tglReadWrite = false, existeUsuario = false, validar = false, banderaTarjetaSipe = false;
    private String id, usuario, pasajes;
    private int pasajesVendedor;

    // NFC
    NfcAdapter nfcAdapter;
    IntentFilter intentFilter;
    private Tag tag;

    // Fierebase
    private DatabaseReference mDatabaseU, mDatabaseUe, mDatabaseTR, mDatabaseRT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recargar_pasajes);

        tvTagId = (TextView)findViewById(R.id.tvTagId);
        etPasajesNuevos = (EditText)findViewById(R.id.etPasajes);
        tvPasajesActuales = (TextView) findViewById(R.id.tvPasajesActuales);
        tvNombre = (TextView)findViewById(R.id.tvNombre);
        tvMensaje2 = (TextView)findViewById(R.id.tvMensaje2);

        btRecargarP = (Button) findViewById(R.id.btRecargarPas);

        visibilidadOff();

        tvMensaje2.setText("Recarga Exitosa!");
        tvMensaje2.setVisibility(View.INVISIBLE);

        Intent intent= getIntent();
        Bundle b = intent.getExtras();
        if(b!=null) {
            usuario =(String) b.get("datos");
            //Toast.makeText(this, usuario, Toast.LENGTH_SHORT).show();
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        intentFilter = new IntentFilter();

        btRecargarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validarCampos();

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(RecargarPasajes.this.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(btRecargarP.getWindowToken(), 0);

                if(validar == true) {

                    inputMethodManager.hideSoftInputFromWindow(btRecargarP.getWindowToken(), 0);

                    verificarSaldo();

                    visibilidadOff();


                }

            }
        });
    }

    private void verificarSaldo() {

        mDatabaseTR = FirebaseDatabase.getInstance().getReference().child("t/" + usuario);
        mDatabaseTR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoUserPass infoT = dataSnapshot.getValue(InfoUserPass.class);

                if (infoT != null) {
                    pasajesVendedor = infoT.numPasajes;
                    //Toast.makeText(RecargarPasajes.this, Integer.toString(pasajesVendedor), Toast.LENGTH_SHORT).show();

                    if(pasajesVendedor >= Integer.parseInt(etPasajesNuevos.getText().toString()) ){

                        mDatabaseU = FirebaseDatabase.getInstance().getReference().child("u/");
                        mDatabaseU.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                mDatabaseU.child(id + "/pasajes").setValue(Integer.parseInt(pasajes) + Integer.parseInt(etPasajesNuevos.getText().toString()));

                                int pp = pasajesVendedor - Integer.parseInt(etPasajesNuevos.getText().toString());
                                mDatabaseTR.child("numPasajes").setValue(pp);

                                if(pp == 0){
                                    tvMensaje2.setVisibility(View.INVISIBLE);
                                    Toast.makeText(RecargarPasajes.this, "Recarga Exitosa", Toast.LENGTH_SHORT).show();
                                    tvTagId.setText("No puede realizar mas recargas, por favor, recargue su cuenta");
                                }else{
                                    tvMensaje2.setVisibility(View.VISIBLE);
                                    tvTagId.setText("Si desea realizar una nueva recarga, acerque una nueva tarjeta");
                                    Toast.makeText(RecargarPasajes.this, "Informacion almacenada en sistema", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else{
                        tvTagId.setText("Recargue su cuenta");
                        tvMensaje2.setVisibility(View.INVISIBLE);
                        Toast.makeText(RecargarPasajes.this, "No puede realizar la recarga, cupo de pasajes excedido", Toast.LENGTH_SHORT).show();

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void validarCampos() {

        if(etPasajesNuevos.getText().toString() != null) {
            if (etPasajesNuevos.getText().toString().equals("")) {
                validar = false;
                Toast.makeText(this, "Por favor, no deje el campo vacío", Toast.LENGTH_SHORT).show();
            } else if(Integer.parseInt(etPasajesNuevos.getText().toString()) < 1){
                Toast.makeText(this, "Por favor, ingrese un número igual o mayor a 1", Toast.LENGTH_SHORT).show();
                validar = false;
            }else{
                validar = true;
            }
        }else {
            Toast.makeText(this, "Por favor, ingrese un número de pasajes", Toast.LENGTH_SHORT).show();
            validar = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){

            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte a[] = tag.getId();
            id = ByteArrayToHexString(a);

            tvMensaje2.setVisibility(View.INVISIBLE);
            etPasajesNuevos.setText("");

            ////////////////////////////////
            mDatabaseUe = FirebaseDatabase.getInstance().getReference().child("u/" + id);
            mDatabaseUe.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    InfoUsuarioExiste infoUe = dataSnapshot.getValue(InfoUsuarioExiste.class);

                    if (infoUe == null) {
                        Toast.makeText(RecargarPasajes.this, "Tarjeta no asignada a usuario", Toast.LENGTH_LONG).show();
                        tvTagId.setText("Por favor acerque la tarjeta \n del ususario a recargar pasajes");

                        visibilidadOff();

                    } else {

                        tvTagId.setText("Usuario Correcto");
                        visibilidadOn();

                        tvNombre.setText("Nombre: " + infoUe.nombre.toString().trim());
                        pasajes = Integer.toString(infoUe.pasajes);
                        tvPasajesActuales.setText("Pasajes Actuales: "+ pasajes);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //////////////////

        }
    }

    private void visibilidadOn() {
        etPasajesNuevos.setVisibility(View.VISIBLE);
        tvPasajesActuales.setVisibility(View.VISIBLE);
        tvNombre.setVisibility(View.VISIBLE);
        btRecargarP.setVisibility(View.VISIBLE);
    }

    private void visibilidadOff() {
        etPasajesNuevos.setVisibility(View.INVISIBLE);
        tvPasajesActuales.setVisibility(View.INVISIBLE);
        tvNombre.setVisibility(View.INVISIBLE);
        btRecargarP.setVisibility(View.INVISIBLE);
    }



    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent = new Intent(this, RecargarPasajes.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);

    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }


}
