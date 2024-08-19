package com.anshu.antakshari;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword,etName;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    ImageView ivShowHidePassword, ivShowHideConfirmPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Objects.requireNonNull(getSupportActionBar()).hide();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        etName = findViewById(R.id.etName);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        ivShowHidePassword = findViewById(R.id.ivShowHidePassword);
        ivShowHideConfirmPassword = findViewById(R.id.ivShowHideConfirmPassword);
        ivShowHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(etPassword, ivShowHidePassword);
            }
        });

        ivShowHideConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(etConfirmPassword, ivShowHideConfirmPassword);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }
    private void signUpUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this,  task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserData(user);
                            //Toast.makeText(SignUpActivity.this,"Sign up successful",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user) {
        String userId = user.getUid();
        String email = user.getEmail();
        String name = etName.getText().toString();

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("email", email);
        userData.put("name", name);



        db.collection("users").document(userId)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                           initialise(name);
                            startActivity(new Intent(SignUpActivity.this, GameMode.class));
                            finish();
                            Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log or display the error message
                        Toast.makeText(SignUpActivity.this, "Failed to save user data in firebase storage", Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void initialise(String name)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("digANT",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FullName", name);
        String firstName = name.split(" ")[0];
        editor.putString("Name",firstName);
        editor.putInt("totalGamesPlayed", 1);
        editor.putInt("totalWins", 0);
        editor.putInt("winPercentage",0);
        editor.putInt("totalPoints", 0);
        editor.putInt("highestScore", 0);
        editor.putInt("avgScore",0);
        editor.putInt("totalWordsPlayed", 1);
        editor.putInt("avgWordLength",0);
        editor.putInt("fastestResponseTime", 30);
        editor.putInt("timeSpentPlaying", 0);
        editor.putBoolean("firstPlay", true);
        editor.putBoolean("Logged", true);
        editor.apply();
    }
    private void togglePasswordVisibility(EditText editText, ImageView imageView) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye_off);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye);
        }
        editText.setSelection(editText.getText().length());
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUpActivity.this, MainActivity2.class));
        finish();
    }

}