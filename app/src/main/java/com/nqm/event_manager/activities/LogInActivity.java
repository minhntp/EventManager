package com.nqm.event_manager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nqm.event_manager.R;
import com.nqm.event_manager.utils.Constants;

public class LogInActivity extends BaseActivity {

    EditText emailEditText, passwordEditText;
    Button signUpButton, logInButton;

    private static FirebaseAuth firebaseAuth;

    public static FirebaseAuth getFirebaseAuth() {
        if(firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        connectViews();
        addEvents();
        signUpButton.setEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void connectViews() {
        emailEditText = findViewById(R.id.log_in_email_edit_text);
        passwordEditText = findViewById(R.id.log_in_password_edit_text);
        signUpButton = findViewById(R.id.log_in_sign_up_button);
        logInButton = findViewById(R.id.log_in_log_in_button);
    }

    private void addEvents() {
        signUpButton.setOnClickListener(v -> {
            createAccount(emailEditText.getText().toString(), passwordEditText.getText().toString());
//            Toast.makeText(LogInActivity.this, "sign-in button clicked", Toast.LENGTH_SHORT).show();
        });

        logInButton.setOnClickListener(v -> {
            logIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
//            Toast.makeText(LogInActivity.this, "log-in button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(getFirebaseAuth().getCurrentUser());
    }

    private void createAccount(String email, String password) {
        if (formIsInvalid()) {
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LogInActivity.this, "Đăng ký thành công",
                                Toast.LENGTH_SHORT).show();
                        updateUI(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(LogInActivity.this, "Đăng ký thất bại",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void logIn(String email, String password) {
        if (formIsInvalid()) {
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        updateUI(null);
                        Toast.makeText(LogInActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private boolean formIsInvalid() {
        boolean invalid = false;

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Xin mời nhập Email");
            invalid = true;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Xin mời nhập Mật khẩu");
            invalid = true;
        } else {
            passwordEditText.setError(null);
        }

        return invalid;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, user.getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RootActivity.class));
            finish();
        }
    }

}
