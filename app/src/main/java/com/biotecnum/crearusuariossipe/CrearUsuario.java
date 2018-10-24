package com.biotecnum.crearusuariossipe;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
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
import java.util.Locale;

public class CrearUsuario extends AppCompatActivity {

    private EditText nombre, cedula, empresa;
    private TextView tagId, tvMensaje;
    private Button bCrear;
    private boolean tglReadWrite = false, existeUsuario = false, validar = false, banderaTarjetaSipe = false;
    private String id, usuario;

    // NFC
    NfcAdapter nfcAdapter;
    IntentFilter intentFilter;
    private Tag tag;

    // Fierebase
    private DatabaseReference mDatabaseU, mDatabaseUe, mDatabaseT, mDatabaseRT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_usuario);

        tagId = (TextView)findViewById(R.id.tvTagId);
        nombre = (EditText)findViewById(R.id.etNombre);
        cedula = (EditText)findViewById(R.id.etCedula);
        empresa = (EditText)findViewById(R.id.etEmpresa);
        tvMensaje = (TextView)findViewById(R.id.tvMensaje);

        bCrear = (Button) findViewById(R.id.btCrear);

        nombre.setVisibility(View.INVISIBLE);
        cedula.setVisibility(View.INVISIBLE);
        empresa.setVisibility(View.INVISIBLE);
        tvMensaje.setVisibility(View.INVISIBLE);

        bCrear.setVisibility(View.INVISIBLE);

        Intent intent= getIntent();
        Bundle b = intent.getExtras();
        if(b!=null) {
            usuario =(String) b.get("datos");
            //Toast.makeText(this, usuario, Toast.LENGTH_SHORT).show();
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        intentFilter = new IntentFilter();

        bCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validarCampos();

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(CrearUsuario.this.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(bCrear.getWindowToken(), 0);

                if(validar == true) {
                    Toast.makeText(CrearUsuario.this, "Acerque nuevamente la tarjeta", Toast.LENGTH_SHORT).show();
                    tglReadWrite = true;

                    inputMethodManager.hideSoftInputFromWindow(bCrear.getWindowToken(), 0);
                }
            }
        });
    }

    private void almacenarEnBasededatos() {
        mDatabaseU = FirebaseDatabase.getInstance().getReference().child("u/");

        mDatabaseU.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mDatabaseU.child(tagId.getText().toString().trim()+"/nombre").setValue(nombre.getText().toString().trim());
                mDatabaseU.child(tagId.getText().toString().trim()+"/cedula").setValue(cedula.getText().toString().trim());
                mDatabaseU.child(tagId.getText().toString().trim()+"/empresa").setValue(empresa.getText().toString().trim());
                mDatabaseU.child(tagId.getText().toString().trim()+"/pasajes").setValue(0);

                Toast.makeText(CrearUsuario.this, "Informacion almacenada en sistema", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void validarCampos() {
        if(nombre.getText().toString().trim().equals("") || cedula.getText().toString().trim().equals("") || empresa.getText().toString().trim().equals("")){
            Toast.makeText(this, "Por favor, ingrese toda la información", Toast.LENGTH_SHORT).show();
            validar = false;
        }else{
            validar = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){

            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte a[] = tag.getId();
            id = ByteArrayToHexString(a);


            ////////////////////////////////
            mDatabaseRT = FirebaseDatabase.getInstance().getReference().child("rt/tarjetas/" + id);
            mDatabaseRT.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    InfoTarjeta infoTar = dataSnapshot.getValue(InfoTarjeta.class);

                    if (infoTar == null) {
                        Toast.makeText(CrearUsuario.this, "Tarjeta no válida, utilice una tarjeta SIPE", Toast.LENGTH_SHORT).show();
                    } else {
                        ////////////////////////////////
                        mDatabaseUe = FirebaseDatabase.getInstance().getReference().child("u/" + id);
                        mDatabaseUe.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                InfoUsuarioExiste infoUe = dataSnapshot.getValue(InfoUsuarioExiste.class);

                                if (infoUe == null) {

                                    if (tglReadWrite == false) {
                                        tagId.setText(id);
                                        setVisibilidad();
                                    } else {
                                        NdefMessage ndefMessage = createNdefMessage(nombre.getText() + "\n" + cedula.getText() + "\n" + empresa.getText());
                                        writeNdefMessage(tag, ndefMessage);
                                        almacenarEnBasededatos();
                                        setVisibilidad();
                                        tglReadWrite = false;
                                        tagId.setVisibility(View.INVISIBLE);
                                        tvMensaje.setVisibility(View.VISIBLE);

                                        tvMensaje.setText("Usuario Creado Exitosamente!");
                                        ////////////// Restar Tarjetas al trabajador
                                        mDatabaseT = FirebaseDatabase.getInstance().getReference().child("t/" + usuario);
                                        mDatabaseT.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                InfoUserPass infoT = dataSnapshot.getValue(InfoUserPass.class);

                                                if (infoT != null) {

                                                    // Actualizar fecha  aqui OJO

                                                    int tarjetarT = infoT.numTarjetas;
                                                    int tT = tarjetarT - 1;
                                                    mDatabaseT.child("numTarjetas").setValue(tT);

                                                    //finish();
                                                    //startActivity(new Intent(getApplicationContext(), SignIn.class));
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                        //////////////
                                    }
                                } else {
                                    Toast.makeText(CrearUsuario.this, "Esta tarjeta ya está asignada, use una nueva", Toast.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        //////////////////////////////////
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //////////////////////////////////

/*

*/


        }
    }

    private void setVisibilidad() {
        if(tglReadWrite == false) {
            nombre.setVisibility(View.VISIBLE);
            cedula.setVisibility(View.VISIBLE);
            empresa.setVisibility(View.VISIBLE);
            bCrear.setVisibility(View.VISIBLE);
        }else{
            nombre.setVisibility(View.INVISIBLE);
            cedula.setVisibility(View.INVISIBLE);
            empresa.setVisibility(View.INVISIBLE);
            bCrear.setVisibility(View.INVISIBLE);
        }
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
        Intent intent = new Intent(this, CrearUsuario.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] intentFilter = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);

    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage){

        try{
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);

            if (ndefFormatable == null){
                Toast.makeText(this, "Tag is not ndef Formatable!", Toast.LENGTH_SHORT).show();
            }

            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

            Toast.makeText(this,"Tag Writen!", Toast.LENGTH_SHORT).show();

        }catch (Exception e) {
            Log.e("formatTag", e.getMessage());
        }

    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage){

        try{

            if(tag == null){
                Toast.makeText(this,"Tag object cannot be Null", Toast.LENGTH_SHORT).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if(ndef == null){
                // Format tag with the ndef format and writes the massage.
                formatTag(tag, ndefMessage);
            }else{
                ndef.connect();
                if(!ndef.isWritable()){
                    Toast.makeText(this,"Tag is not writable!", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();

            }

        }catch(Exception e){
            Log.e("WriteNdefMessage", e.getMessage());
        }

    }

    private NdefRecord createTextRecord(String content){

        try{
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) languageSize & 0x1F);
            payload.write(language, 0 , languageSize);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());

        }catch (UnsupportedEncodingException e){
            Log.e("CreateTextRecord", e.getMessage());
        }

        return null;

    }

    private NdefMessage createNdefMessage(String content){

        NdefRecord ndefRecord = createTextRecord(content);

        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ ndefRecord });

        return ndefMessage;
    }

}
