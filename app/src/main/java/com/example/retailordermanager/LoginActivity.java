package com.example.retailordermanager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * LoginActivity — first screen shown when the app opens.
 *
 * Reads username and password, checks them against the database,
 * then navigates to DashboardActivity on success.
 * Default credentials: username="admin", password="1234".
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button   buttonLogin;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Make the status bar match the navy top section of the login screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        databaseHelper = new DatabaseHelper(this);

        // Connect XML views to Java variables
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin      = findViewById(R.id.buttonLogin);

        // Trigger login logic when the Sign In button is tapped
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    /**
     * Validates the entered credentials against the database.
     * Navigates to DashboardActivity on success; shows a Toast on failure.
     */
    private void handleLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Both fields must be non-empty before querying the database
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isValid = databaseHelper.checkUser(username, password);

        if (isValid) {
            // Credentials matched — open the Dashboard
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            // finish() prevents the user pressing Back to return to the login screen
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
