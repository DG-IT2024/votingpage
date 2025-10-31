package com.example.votingpage;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class VotingActivity extends AppCompatActivity {

    private RecyclerView rvPolls;
    private PollAdapter adapter;
    private final List<Poll> polls = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        // --- Views ---
        rvPolls = findViewById(R.id.rvPolls);
        ImageButton btnAddPoll = findViewById(R.id.btnAddPoll);
        ImageButton btnBack = findViewById(R.id.btnBack);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // --- RecyclerView setup ---
        rvPolls.setLayoutManager(new LinearLayoutManager(this));
        seedData();
        adapter = new PollAdapter(this, polls, () -> rvPolls.post(() -> adapter.notifyDataSetChanged()));
        rvPolls.setAdapter(adapter);

        // --- Add Poll (+) ---
        btnAddPoll.setOnClickListener(v ->
                new AddPollDialogFragment(poll -> {
                    // Insert new poll at top
                    polls.add(0, poll);
                    adapter.notifyItemInserted(0);
                    rvPolls.scrollToPosition(0);
                }).show(getSupportFragmentManager(), "addPollDialog")
        );

        // --- Back ---
        btnBack.setOnClickListener(v -> finish());

        // --- Bottom Navigation (placeholder handlers) ---
        bottomNav.setSelectedItemId(R.id.nav_polls);
        bottomNav.setOnItemSelectedListener(item -> {
            // TODO: navigate to other sections if you have those activities
            // For now, keep Polls selected.
            return true;
        });
    }

    private void seedData() {
        polls.clear();

        // Open poll (no votes yet)
        Poll p1 = new Poll("Poll : Favorite Anime?");
        p1.description = "Pick your favorite.";
        p1.options.add(new PollOption("Naruto"));
        p1.options.add(new PollOption("Dragonball"));
        p1.options.add(new PollOption("One Piece"));
        polls.add(p1);

        // Closed poll (pre-populated votes)
        Poll p2 = new Poll("Poll : Favorite Food?");
        p2.description = "Team lunch choice.";
        p2.link = "https://example.com/menu";
        p2.options.add(new PollOption("Burger"));
        p2.options.add(new PollOption("Hotdog"));
        p2.options.add(new PollOption("Sandwich"));
        p2.options.get(0).votes = 30;
        p2.options.get(1).votes = 15;
        p2.options.get(2).votes = 5;
        p2.closed = true;
        polls.add(p2);
    }
}

