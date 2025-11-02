package com.example.votingpage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Agenda view: weekly headers + hourly slots. In-memory demo data.
 * Reuses colors, theme, bottom nav, and icons from Voting page.
 */
public class AgendaActivity extends AppCompatActivity {

    // UI
    private TextView tvMonthYear;
    private TableLayout tableGrid;

    // Calendar state (selected anchor date; we show its week)
    private final Calendar anchor = Calendar.getInstance(); // today as default

    // Demo events
    private final ArrayList<EventItem> events = new ArrayList<>();

    // Time bounds (change if you want more/less hours)
    private static final int START_HOUR = 6;   // 06:00
    private static final int END_HOUR = 21;    // 21:00 inclusive -> last row
    private static final int COLS = 7;         // Sun..Sat

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        // Header bar buttons
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnCalDrop = findViewById(R.id.btnCalendar);

        // Row: arrows + header text
        TextView btnPrevWeek = findViewById(R.id.btnPrevWeek);
        TextView btnPrevDay  = findViewById(R.id.btnPrevDay);
        TextView btnNextDay  = findViewById(R.id.btnNextDay);
        TextView btnNextWeek = findViewById(R.id.btnNextWeek);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        tableGrid   = findViewById(R.id.tableGrid);

        // Bottom nav
        BottomNavigationView bottom = findViewById(R.id.bottomNavigation);
        bottom.setSelectedItemId(R.id.nav_calendar);
        bottom.setOnItemSelectedListener(item -> true);

        btnBack.setOnClickListener(v -> finish());

        // Calendar picker
        btnCalDrop.setOnClickListener(v -> showDatePicker());

        // Arrows (≪ ‹ › ≫)
        btnPrevWeek.setOnClickListener(v -> { shiftDays(-7); });
        btnPrevDay.setOnClickListener(v  -> { shiftDays(-1); });
        btnNextDay.setOnClickListener(v  -> { shiftDays(1); });
        btnNextWeek.setOnClickListener(v -> { shiftDays(7); });

        seedDemoEvents();
        buildScreen();
    }

    private void shiftDays(int delta) {
        anchor.add(Calendar.DAY_OF_MONTH, delta);
        buildScreen();
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    anchor.set(Calendar.YEAR, y);
                    anchor.set(Calendar.MONTH, m);
                    anchor.set(Calendar.DAY_OF_MONTH, d);
                    buildScreen();
                },
                anchor.get(Calendar.YEAR),
                anchor.get(Calendar.MONTH),
                anchor.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /** Build the header and the table every time the anchor changes. */
    private void buildScreen() {
        // Month | Year label (center)
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM | yyyy", Locale.getDefault());
        tvMonthYear.setText(monthFmt.format(anchor.getTime()));

        // Build week header labels (Sun..Sat) dated from Sunday of that week
        Calendar weekStart = (Calendar) anchor.clone();
        // Normalize to beginning of week (Sunday)
        weekStart.set(Calendar.DAY_OF_WEEK, weekStart.getFirstDayOfWeek()); // often Sunday on many devices
        // Some locales start on Monday; if you want to force Sunday:
        // weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Clear old grid
        tableGrid.removeAllViews();

        // Header row: TIME | SUN 01 | MON 02 ... SAT 07
        TableRow header = new TableRow(this);
        header.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        header.setBackgroundColor(getColor(R.color.outline));

        // TIME cell
        TextView timeHdr = makeHeaderCell("TIME", true);
        header.addView(timeHdr);

        SimpleDateFormat dayName = new SimpleDateFormat("EEE", Locale.getDefault()); // SUN, MON
        SimpleDateFormat dayNum  = new SimpleDateFormat("dd", Locale.getDefault());  // 01, 02, ...

        Calendar colCal = (Calendar) weekStart.clone();
        for (int c = 0; c < COLS; c++) {
            String label = dayName.format(colCal.getTime()) + " " + dayNum.format(colCal.getTime());
            header.addView(makeHeaderCell(label, false));
            colCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        tableGrid.addView(header);

        // Rows for each hour
        Calendar rowCal = (Calendar) weekStart.clone();
        for (int h = START_HOUR; h <= END_HOUR; h++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            // Time label (e.g., 6:00)
            String label = toHourLabel(h);
            TextView timeCell = makeTimeCell(label);
            row.addView(timeCell);

            // Day cells (Sun..Sat)
            Calendar dayIter = (Calendar) rowCal.clone();
            for (int c = 0; c < COLS; c++) {
                TextView cell = makeSlotCell();

                // If an event matches this day+hour, render it in the cell
                EventItem match = findEvent(dayIter, h);
                if (match != null) {
                    cell.setText("• " + match.title);
                    cell.setBackground(getDrawable(R.drawable.bg_cell_event)); // light accent
                }

                row.addView(cell);
                dayIter.add(Calendar.DAY_OF_MONTH, 1);
            }
            tableGrid.addView(row);
        }
    }

    private EventItem findEvent(Calendar day, int hour) {
        for (EventItem e : events) {
            if (sameYMD(day, e.when) && e.hour24 == hour) return e;
        }
        return null;
    }

    private boolean sameYMD(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)  == b.get(Calendar.YEAR) &&
                a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
                a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH);
    }

    private String toHourLabel(int h24) {
        // 6 → 6:00, 13 → 1:00 PM (optional: keep 24h)
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, h24);
        c.set(Calendar.MINUTE, 0);
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(c.getTime());
    }

    // ---------- cell factories ----------
    private TextView makeHeaderCell(String text, boolean sticky) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(dp(8), dp(8), dp(8), dp(8));
        tv.setTextSize(13);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(getColor(R.color.surface));
        tv.setTextColor(getColor(R.color.dark_gray));
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                sticky ? dp(64) : 0, TableRow.LayoutParams.WRAP_CONTENT, sticky ? 0 : 1f);
        lp.setMargins(dp(1), dp(1), dp(1), dp(1));
        tv.setLayoutParams(lp);
        tv.setBackground(getDrawable(R.drawable.bg_cell_header));
        return tv;
    }

    private TextView makeTimeCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(dp(6), dp(10), dp(6), dp(10));
        tv.setTextSize(12);
        tv.setTextColor(getColor(R.color.gray_text));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(dp(64), TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(1), dp(1), dp(1), dp(1));
        tv.setLayoutParams(lp);
        tv.setBackground(getDrawable(R.drawable.bg_cell_time));
        return tv;
    }

    private TextView makeSlotCell() {
        TextView tv = new TextView(this);
        tv.setPadding(dp(6), dp(14), dp(6), dp(14));
        tv.setTextSize(12);
        tv.setTextColor(getColor(R.color.dark_gray));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(1), dp(1), dp(1), dp(1));
        tv.setLayoutParams(lp);
        tv.setBackground(getDrawable(R.drawable.bg_cell_empty)); // thin border
        return tv;
    }

    private int dp(int px) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(px * d);
    }

    // ---------- Demo data ----------
    private void seedDemoEvents() {
        events.clear();
        // Example events near 'anchor' day
        Calendar d1 = (Calendar) anchor.clone();
        d1.set(Calendar.HOUR_OF_DAY, 9);
        events.add(new EventItem("Stand-up", d1, 9));

        Calendar d2 = (Calendar) anchor.clone();
        d2.add(Calendar.DAY_OF_MONTH, 2);
        d2.set(Calendar.HOUR_OF_DAY, 14);
        events.add(new EventItem("Client Call", d2, 14));
    }

    // Simple event record
    static class EventItem {
        String title;
        Calendar when;
        int hour24;
        EventItem(String title, Calendar when, int hour24) {
            this.title = title; this.when = (Calendar) when.clone(); this.hour24 = hour24;
        }
    }
}
