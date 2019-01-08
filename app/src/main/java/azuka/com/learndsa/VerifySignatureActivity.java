package azuka.com.learndsa;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.Signature;

import azuka.com.learndsa.Preferences.Constants;
import azuka.com.learndsa.Preferences.Preferences;
import azuka.com.learndsa.Preferences.Strings;

import static azuka.com.learndsa.GenerateSignatureActivity.keyPair;

public class VerifySignatureActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    Preferences preferences;
    String receivedMsg, modifMsg;
    Button btnVer, btnModifVer;
    EditText etVerMessage;
    LinearLayout msgContainer;
    byte[] bytes;
    boolean isVerified = false;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    private byte[] new_signature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_signature);
        init();

        btnVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bytes = receivedMsg.getBytes();
                isVerified = verifySignature();

                if (isVerified){
                    showVerificationDialog(Constants.SUC_VER);

                } else {
                    showVerificationDialog(Constants.FAIL_VER);
                }

            }
        });

        btnModifVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifMessage();
            }
        });
    }

    private boolean verifySignature(){
        //Creating a Signature object
        Signature dsa;

        dsa = GenerateSignatureActivity.createSignature();

        //Initialize the signature
        Log.e("KEY PAIR STATIC", keyPair.toString());

        try {
            dsa.initVerify(keyPair.getPublic());
            dsa.update(bytes);
            byte[] signature = GenerateSignatureActivity.getSignature();
            Log.e("SIGNATURE VALUE", signature.toString());
            Log.e("KEY PAIR STATIC", keyPair.toString());
            isVerified = dsa.verify(signature);
            new_signature = GenerateSignatureActivity.getSignature(keyPair, modifMsg);

            /*
            if (isVerified){
                Toast.makeText(getApplicationContext(),"Signature Verified", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),"Signature Failed", Toast.LENGTH_SHORT).show();
            }
             */

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isVerified;
    }

    private void showVerificationDialog(final int status) {
        dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_verification_result, null);
        dialogBuilder.setView(dialogView)
                .setCancelable(false);
        final TextView tvVerStatus = dialogView.findViewById(R.id.tv_ver_status);
        final TextView tvSignature = dialogView.findViewById(R.id.tv_signature);
        final TextView tvCompareResult = dialogView.findViewById(R.id.tv_compare_result);
        final EditText etMessage = dialogView.findViewById(R.id.et_message);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        dialog = dialogBuilder.create();
        etMessage.setFocusable(false);
        etMessage.clearFocus();
        if (status == Constants.SUC_VER){
            tvVerStatus.setText("Verifikasi Berhasil");
            tvSignature.setText(preferences.get(Strings.TAG_SIGNATURE));
            tvCompareResult.setText("Signature sama");
            etMessage.setText(receivedMsg);
        } else {
            tvVerStatus.setText("Verifikasi Gagal");
            tvSignature.setText(new_signature.toString());
            tvCompareResult.setText("Signature tidak sama (Original : " + preferences.get(Strings.TAG_SIGNATURE) + ")");
            etMessage.setText("Gagal menampilkan pesan karena signature tidak sama");
        }

        dialog.setOnDismissListener(this);
        dialog.show();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showModifMessage(){
        dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_modif_msg, null);
        dialogBuilder.setView(dialogView)
                .setCancelable(false);
        final EditText etModifMessage = dialogView.findViewById(R.id.et_modif_message);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnVerify = dialogView.findViewById(R.id.btn_verify);
        Button btnClear = dialogView.findViewById(R.id.btn_clear);
        dialog = dialogBuilder.create();
        etModifMessage.setText(receivedMsg);
        dialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                etModifMessage.setText("");
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etModifMessage.setText("");
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifMsg = etModifMessage.getText().toString();
                if (modifMsg.isEmpty()){
                    etModifMessage.setError("Masukkan Pesan");
                    return;
                }
                bytes = modifMsg.getBytes();
                isVerified = verifySignature();
                dialog.dismiss();

                if (isVerified){
                    showVerificationDialog(Constants.SUC_VER);

                } else {
                    showVerificationDialog(Constants.FAIL_VER);
                }
            }
        });
    }

    private void init(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.red)));
        preferences = Preferences.getInstance(getApplicationContext());
        receivedMsg = preferences.get(Strings.TAG_MESSAGE);
        btnModifVer = findViewById(R.id.btn_modif_message);
        btnVer = findViewById(R.id.btn_ver_message);
        etVerMessage = findViewById(R.id.et_ver_message);
        msgContainer = findViewById(R.id.ll_msg_container);
        etVerMessage.setFocusable(false);
        etVerMessage.clearFocus();
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isVerified){
            msgContainer.setVisibility(View.VISIBLE);
            etVerMessage.setText(receivedMsg);
        } else {
            msgContainer.setVisibility(View.INVISIBLE);
            etVerMessage.setText("");
        }
    }
}
