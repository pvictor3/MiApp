package com.contenidofoo1.miapp;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Button logInButton;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private EditText campoEmail;
    private TextView retrievePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);

        campoEmail = findViewById(R.id.email_input_text);
        campoEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logInButton = findViewById(R.id.login_button);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarDatos();
            }
        });

        retrievePass = findViewById(R.id.retrieve_password);
        retrievePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nueva activity
            }
        });
    }

    private boolean esCorreoValido(String correo){
        if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            tilEmail.setError("Correo electrónico inválido");
            return false;
        }else{
            tilEmail.setError(null);
        }
        return true;
    }

    private boolean passValido(String password){
        if(!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)([A-Za-z\\d]|[^ ]){8,15}$", password)){
            tilPassword.setError("Contraseña inválida");
        }else{
            tilPassword.setError(null);
        }
        return true;
    }

    private void validarDatos(){
        String email = tilEmail.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();

        if( esCorreoValido(email) && passValido(password) ){
            Toast.makeText(this, "Ingresando", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }
    }
}
