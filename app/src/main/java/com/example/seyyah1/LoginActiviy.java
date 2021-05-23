package com.example.seyyah1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActiviy extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    EditText emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        //halihazırda giriş yapmış bir kullanıcı varsa tekrar şifre sormuyoruz
        if (firebaseUser != null){
            Intent intent = new Intent(LoginActiviy.this,MainActivity.class);
            startActivity(intent);
            finish(); //giriş yaptıktan sonra login ekranı kapatılıyor
        }
    }

    public void signInClicked(View view){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent = new Intent(LoginActiviy.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActiviy.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }
    public void signUpClicked(View view){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        if (email.matches("") || password.matches("")){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActiviy.this);
            alertDialog.setTitle("HATA");
            alertDialog.setMessage("Kullanıcı Adı ve/veya Şifre Boş Bırakılamaz");
            alertDialog.show();
        }else{
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(LoginActiviy.this,"Kayıt Başarılı",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActiviy.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActiviy.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}