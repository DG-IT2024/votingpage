package com.example.votingpage;

import java.util.ArrayList;
import java.util.List;

public class Poll {
    public String question;           // e.g., "Poll : Favorite Anime?"
    public String description;        // optional
    public String link;               // optional (URL)
    public final List<PollOption> options = new ArrayList<>();
    public boolean closed = false;    // open vs closed card
    public Integer selectedIndex = null;   // user's selection before voting

    public Poll(String question) {
        this.question = question;
    }

    public int totalVotes() {
        int t = 0;
        for (PollOption o : options) t += o.votes;
        return t;
    }

    public int winningIndex() {
        int idx = 0, max = -1;
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).votes > max) {
                max = options.get(i).votes;
                idx = i;
            }
        }
        return idx;
    }
}
