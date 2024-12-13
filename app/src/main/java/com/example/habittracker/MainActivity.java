package com.example.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView habitRecyclerView;
    HabitAdapter habitAdapter;
    ArrayList<Habit> habitList = new ArrayList<>();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        habitRecyclerView = findViewById(R.id.recyclerViewHabits);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(habitList, this);
        habitRecyclerView.setAdapter(habitAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Habits");

        loadHabits();

        findViewById(R.id.btnAddHabit).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
            startActivity(intent);
        });
    }

    private void loadHabits() {
        DatabaseReference habitReference = FirebaseDatabase.getInstance().getReference("Habits");
        habitReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                habitList.clear(); // Clear the old list to avoid duplicates
                for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);
                    if (habit != null) {
                        habitList.add(habit);
                    }
                }
                habitAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load habits", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
