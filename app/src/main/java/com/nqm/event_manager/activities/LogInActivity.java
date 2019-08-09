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

public class LogInActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button signUpButton, logInButton;

    public static FirebaseAuth firebaseAuth;
    public static FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        connectViews();
        addEvents();

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
        currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void createAccount(String email, String password) {
//        Log.d(Constants.DEBUG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

//        showProgressDialog();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
//                        Log.d(Constants.DEBUG, "createUserWithEmail:success");
                        Toast.makeText(LogInActivity.this, "Đăng ký thành công",
                                Toast.LENGTH_SHORT).show();
                        currentUser = firebaseAuth.getCurrentUser();
                        updateUI(currentUser);
                    } else {
//                        Log.d(Constants.DEBUG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LogInActivity.this, "Đăng ký thất bại",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // [START_EXCLUDE]
//                        hideProgressDialog();
                    // [END_EXCLUDE]
                });
        // [END create_user_with_email]
    }

    private void logIn(String email, String password) {
//        Log.d(Constants.DEBUG, "logIn:" + email);
        if (!validateForm()) {
            return;
        }

//        showProgressDialog();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
//                        Log.d(Constants.DEBUG, "signInWithEmail:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
//                        Log.d(Constants.DEBUG, "signInWithEmail:failure", task.getException());
//                        Toast.makeText(LogInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                        Toast.makeText(LogInActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();

                    }
//                        hideProgressDialog();
                });
    }

    private void signOut() {
        firebaseAuth.signOut();
        updateUI(null);
    }

//    private void sendEmailVerification() {
//        // Disable button
//        findViewById(R.id.verifyEmailButton).setEnabled(false);
//
//        // Send verification email
//        // [START send_email_verification]
//        final FirebaseUser user = firebaseAuth.getCurrentUser();
//        user.sendEmailVerification()
//                .addOnCompleteListener(this, task -> {
//                    // [START_EXCLUDE]
//                    // Re-enable button
//                    findViewById(R.id.verifyEmailButton).setEnabled(true);
//
//                    if (task.isSuccessful()) {
//                        Toast.makeText(LogInActivity.this,
//                                "Verification email sent to " + user.getEmail(),
//                                Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.d(Constants.DEBUG, "sendEmailVerification", task.getException());
//                        Toast.makeText(LogInActivity.this,
//                                "Failed to send verification email.",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    // [END_EXCLUDE]
//                });
//        // [END send_email_verification]
//    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Xin mời nhập");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Xin mời nhập");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                    user.getEmail(), user.isEmailVerified()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
//
//            findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
//            findViewById(R.id.emailPasswordFields).setVisibility(View.GONE);
//            findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);
//
//            findViewById(R.id.verifyEmailButton).setEnabled(!user.isEmailVerified());

            Toast.makeText(this, user.getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
//
//            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
//            findViewById(R.id.emailPasswordFields).setVisibility(View.VISIBLE);
//            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
            Toast.makeText(this, "Xin mời đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.emailCreateAccountButton) {
//            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
//        } else if (i == R.id.log_in_log_in_button) {
//            logIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
//        } else if (i == R.id.signOutButton) {
//            signOut();
//        } else if (i == R.id.verifyEmailButton) {
//            sendEmailVerification();
//        }

//    }

}
