package com.example.votingpage;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AddPollDialogFragment extends DialogFragment {

    public interface OnPollCreated { void onCreated(Poll poll); }

    private final OnPollCreated callback;

    public AddPollDialogFragment(OnPollCreated cb) { this.callback = cb; }

    private EditText etTitle, etDescription, etLink;
    private LinearLayout containerOptionFields;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate WITHOUT attaching to a parent
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_poll, (ViewGroup) null, false);

        etTitle = v.findViewById(R.id.etTitle);
        etDescription = v.findViewById(R.id.etDescription);
        etLink = v.findViewById(R.id.etLink);
        containerOptionFields = v.findViewById(R.id.containerOptionFields);
        Button btnAddOptionField = v.findViewById(R.id.btnAddOptionField);

        // Seed with two rows
        addOptionField("Option 1");
        addOptionField("Option 2");

        btnAddOptionField.setOnClickListener(view ->
                addOptionField("Option " + (containerOptionFields.getChildCount() + 1)));

        // Build dialog; intercept positive button in onStart()
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Create Poll")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d == null) return;
        Button positive = d.findViewById(android.R.id.button1);
        if (positive == null) return;

        positive.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(getContext(), "Poll name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> opts = collectOptions();
            if (opts.size() < 2) {
                Toast.makeText(getContext(), "Enter at least two options", Toast.LENGTH_SHORT).show();
                return;
            }
            Poll p = new Poll("Poll : " + title);
            p.description = etDescription.getText().toString().trim();
            String link = etLink.getText().toString().trim();
            p.link = TextUtils.isEmpty(link) ? null : link;
            for (String s : opts) p.options.add(new PollOption(s));

            if (callback != null) callback.onCreated(p);
            dismiss();
        });
    }

    private void addOptionField(String hint) {
        EditText et = new EditText(getContext());
        et.setHint(hint);
        et.setSingleLine();
        containerOptionFields.addView(et);
    }

    private List<String> collectOptions() {
        List<String> res = new ArrayList<>();
        for (int i = 0; i < containerOptionFields.getChildCount(); i++) {
            View child = containerOptionFields.getChildAt(i);
            if (child instanceof EditText) {
                String t = ((EditText) child).getText().toString().trim();
                if (!TextUtils.isEmpty(t)) res.add(t);
            }
        }
        return res;
    }
}
