package com.example.habittracker;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddHabitActivity extends AppCompatActivity {
    EditText etHabitName;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        etHabitName = findViewById(R.id.etHabitName);
        databaseReference = FirebaseDatabase.getInstance().getReference("Habits");

        findViewById(R.id.btnSaveHabit).setOnClickListener(view -> saveHabit());
    }

    private void saveHabit() {
        String habitName = etHabitName.getText().toString();
        if (!habitName.isEmpty()) {
            String id = databaseReference.push().getKey();
            Habit habit = new Habit(id, habitName);
            databaseReference.child(id).setValue(habit).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Habit added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add habit", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Enter a habit name", Toast.LENGTH_SHORT).show();
        }
    }
}
