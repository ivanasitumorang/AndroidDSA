package azuka.com.learndsa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import azuka.com.learndsa.Preferences.Constants;
import azuka.com.learndsa.Preferences.Preferences;
import azuka.com.learndsa.Preferences.Strings;

public class GenerateSignatureActivity extends AppCompatActivity {

    EditText etMessage;
    Button btnSendMessage, btnFilePicker, btnClear;
    public static KeyPair keyPair = generatePairKey();

    String msg, isiFile;
    Preferences preferences;

    public static byte[] signature;

    File source, destination;
    private static final int PICKFILE_RESULT_CODE = 1;

    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_signature);
        init();

        btnFilePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("text/plain");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etMessage.setText("");
            }
        });

        //Accepting text from user
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg = etMessage.getText().toString();
                if (msg.isEmpty()){
                    etMessage.setError("Masukkan Pesan");
                    return;
                }
                preferences.put(Strings.TAG_MESSAGE, msg);
                signature = getSignature(keyPair, msg);
                Log.e("KEY PAIR STATIC", keyPair.toString());
                preferences.put(Strings.TAG_SIGNATURE, signature.toString());
                Log.e("SIGNATURE VALUE", preferences.get(Strings.TAG_SIGNATURE));
                showGenerateDialog();
            }
        });
    }

    public static byte[] getSignature(){
        return signature;
    }

    public static byte[] getSignature(KeyPair keyPair, String message) {
        byte[] bytes = message.getBytes();
        Signature dsa = null;

        dsa = createSignature();

        try {
            dsa.initSign(getPrivateKey(keyPair));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            dsa.update(bytes);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        byte[] signature = new byte[0];
        try {
            signature = dsa.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public static PrivateKey getPrivateKey(KeyPair keyPair) {
        return keyPair.getPrivate();
    }

    public static KeyPair generatePairKey() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("dsa");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGenerator.initialize(Constants.KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    public static Signature createSignature(){
        try {
            return Signature.getInstance("SHA256withDSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == Activity.RESULT_OK){
            Uri content_describer = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(content_describer);
                isiFile = getStringFromInputStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String src = content_describer.getPath();
            source = new File(src);
            Log.d("src is ", source.toString());
            String filename = content_describer.getLastPathSegment();
            Log.d("FileName is ",filename);
            etMessage.setText(isiFile);
            //destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test/TestTest/" + filename);
            //Log.d("Destination is ", destination.toString());
            //SetToFolder.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("PERMISSION","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void showGenerateDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_generate_result, null);
        dialogBuilder.setView(dialogView)
                .setCancelable(false);
        final TextView tvSignature = dialogView.findViewById(R.id.tv_signature);
        final EditText etMessage = dialogView.findViewById(R.id.et_message);
        Button btnRetry = dialogView.findViewById(R.id.btn_retry);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        dialog = dialogBuilder.create();
        etMessage.setFocusable(false);
        etMessage.clearFocus();

        tvSignature.setText(signature.toString());
        etMessage.setText(msg);

        dialog.show();

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                signature = null;
                msg = "";
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                GenerateSignatureActivity.this.finish();
            }
        });
    }

    private void init(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.red)));
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        preferences = Preferences.getInstance(getApplicationContext());
        etMessage = findViewById(R.id.et_message);
        btnSendMessage = findViewById(R.id.btn_send_message);
        btnFilePicker = findViewById(R.id.btn_file_picker);
        btnClear = findViewById(R.id.btn_clear);
    }

}
