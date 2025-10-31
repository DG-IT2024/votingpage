package com.example.votingpage;

public class PollOption {
    public String label;   // visible option text
    public int votes;      // vote count

    public PollOption(String label) {
        this.label = label;
        this.votes = 0;
    }
}
