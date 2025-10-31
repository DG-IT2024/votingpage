package com.example.votingpage;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter with two view types:
 *  - Open poll (select option -> details panel -> Vote button)
 *  - Closed poll (result bars + winner + total)
 */
public class PollAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_OPEN = 0;
    private static final int TYPE_CLOSED = 1;

    private final Context ctx;
    private final List<Poll> data;
    private final Runnable onChanged; // callback to request list refresh if needed

    public PollAdapter(Context ctx, List<Poll> polls, Runnable onChanged) {
        this.ctx = ctx;
        this.data = polls;
        this.onChanged = onChanged;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).closed ? TYPE_CLOSED : TYPE_OPEN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_OPEN) {
            View v = inf.inflate(R.layout.item_poll, parent, false);
            return new OpenVH(v);
        } else {
            View v = inf.inflate(R.layout.item_poll_closed, parent, false);
            return new ClosedVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Poll p = data.get(position);
        if (holder instanceof OpenVH) ((OpenVH) holder).bind(p, position);
        else ((ClosedVH) holder).bind(p, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // ---------------- OPEN POLL ----------------
    class OpenVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvLink, tvSelected;
        LinearLayout optionsContainer, detailsPanel;
        ImageButton btnDelete;
        Button btnVote;

        OpenVH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDesc = v.findViewById(R.id.tvDescription);
            tvLink = v.findViewById(R.id.tvLink);
            optionsContainer = v.findViewById(R.id.containerOptions);
            detailsPanel = v.findViewById(R.id.panelDetails);
            tvSelected = v.findViewById(R.id.tvSelected);
            btnVote = v.findViewById(R.id.btnVote);
            btnDelete = v.findViewById(R.id.btnDelete);
        }

        void bind(Poll poll, int pos) {
            tvTitle.setText(poll.question);

            if (TextUtils.isEmpty(poll.description)) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setVisibility(View.VISIBLE);
                tvDesc.setText(poll.description);
            }

            if (TextUtils.isEmpty(poll.link)) {
                tvLink.setVisibility(View.GONE);
            } else {
                tvLink.setVisibility(View.VISIBLE);
                tvLink.setText(poll.link); // autoLink is set in XML
            }

            // Build option chips
            optionsContainer.removeAllViews();
            LayoutInflater inf = LayoutInflater.from(optionsContainer.getContext());
            for (int i = 0; i < poll.options.size(); i++) {
                final int idx = i;
                PollOption opt = poll.options.get(i);

                View row = inf.inflate(R.layout.view_option_chip, optionsContainer, false);
                TextView tvLabel = row.findViewById(R.id.tvLabel);
                TextView tvChip = row.findViewById(R.id.tvChip);

                tvLabel.setText("Option #" + (i + 1));
                tvChip.setText(opt.label);
                tvChip.setBackgroundResource(
                        (poll.selectedIndex != null && poll.selectedIndex == idx)
                                ? R.drawable.bg_chip_active
                                : R.drawable.bg_chip
                );

                row.setOnClickListener(v -> {
                    poll.selectedIndex = idx;
                    notifyItemChanged(getBindingAdapterPosition());
                });

                optionsContainer.addView(row);
            }

            // Details + Vote panel
            if (poll.selectedIndex != null) {
                tvSelected.setText("Selected: " + poll.options.get(poll.selectedIndex).label);
                detailsPanel.setVisibility(View.VISIBLE);
            } else {
                detailsPanel.setVisibility(View.GONE);
            }

            btnVote.setOnClickListener(v -> {
                if (poll.selectedIndex == null) {
                    Toast.makeText(ctx, "Select an option first", Toast.LENGTH_SHORT).show();
                    return;
                }
                poll.options.get(poll.selectedIndex).votes += 1;
                poll.closed = true;             // close after vote (sample behavior)
                poll.selectedIndex = null;
                notifyItemChanged(pos);
                if (onChanged != null) onChanged.run();
            });

            btnDelete.setOnClickListener(v -> {
                int adapterPos = getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    data.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                    if (onChanged != null) onChanged.run();
                }
            });
        }
    }

    // ---------------- CLOSED POLL ----------------
    class ClosedVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvFinal, tvTotal;
        LinearLayout resultsContainer;
        ImageButton btnDelete;

        ClosedVH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvFinal = v.findViewById(R.id.tvFinalVote);
            tvTotal = v.findViewById(R.id.tvTotalVotes);
            resultsContainer = v.findViewById(R.id.containerResults);
            btnDelete = v.findViewById(R.id.btnDelete);
        }

        void bind(Poll poll, int pos) {
            tvTitle.setText(poll.question);

            resultsContainer.removeAllViews();
            int total = Math.max(1, poll.totalVotes());
            int win = poll.winningIndex();

            for (int i = 0; i < poll.options.size(); i++) {
                PollOption opt = poll.options.get(i);

                // Row container
                LinearLayout wrap = new LinearLayout(ctx);
                wrap.setOrientation(LinearLayout.VERTICAL);
                wrap.setPadding(0, dp(6), 0, dp(6));

                // Label
                TextView label = new TextView(ctx);
                int pct = (int) Math.round(100.0 * opt.votes / total);
                label.setText("Option #" + (i + 1) + ": " + opt.label + "  (" + pct + "%)");
                label.setTextColor(i == win
                        ? Color.parseColor("#FF7A1A")
                        : Color.parseColor("#6B7280"));

                // Progress bar
                ProgressBar bar = new ProgressBar(ctx, null, android.R.attr.progressBarStyleHorizontal);
                bar.setMax(100);
                bar.setProgress(pct);
                bar.setProgressDrawable(ContextCompat.getDrawable(ctx, R.drawable.progress_orange));

                wrap.addView(label);
                wrap.addView(bar);
                resultsContainer.addView(wrap);
            }

            tvFinal.setText("Final Vote: " + poll.options.get(win).label);
            tvTotal.setText(poll.totalVotes() + " Votes");

            btnDelete.setOnClickListener(v -> {
                int adapterPos = getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    data.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                    if (onChanged != null) onChanged.run();
                }
            });
        }

        private int dp(int px) {
            float d = ctx.getResources().getDisplayMetrics().density;
            return Math.round(px * d);
        }
    }
}
