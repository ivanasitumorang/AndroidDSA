package azuka.com.learndsa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnGenSig, btnVerSig;
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    TextView btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        btnGenSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GenerateSignatureActivity.class));
            }
        });

        btnVerSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), VerifySignatureActivity.class));
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    private void init(){
        btnGenSig = findViewById(R.id.btn_generate_signature);
        btnVerSig = findViewById(R.id.btn_verify_signature);
        btnAbout = findViewById(R.id.tv_about);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleButton(btnVerSig);
    }

    private void showAboutDialog(){
        dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_about, null);
        dialogBuilder.setView(dialogView)
                .setCancelable(false);
        TextView btnClose = dialogView.findViewById(R.id.tv_close);

        dialog = dialogBuilder.create();
        dialog.show();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void toggleButton(Button button){
        if (GenerateSignatureActivity.getSignature() == null){
            button.setEnabled(false);
            button.setBackground(getResources().getDrawable(R.drawable.dis_button));
        } else {
            button.setEnabled(true);
            button.setBackground(getResources().getDrawable(R.drawable.red_button));
        }
    }
}
