package fr.eurecom.Ready2Meet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimePickerDialog extends Dialog {
    private TimePicker timePicker;
    private DatePicker datePicker;

    private Calendar result;

    public DateTimePickerDialog(@NonNull Context context) {
        super(context);
        this.setContentView(R.layout.dialog_date_time_picker);
        this.setTitle("Select date and time");
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        timePicker.setVisibility(View.GONE);
        datePicker = (DatePicker) findViewById(R.id.date_picker);
        datePicker.setVisibility(View.VISIBLE);
    }

    public DateTimePickerDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.setContentView(R.layout.dialog_date_time_picker);
        this.setTitle("Select date and time");
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        timePicker.setVisibility(View.GONE);
        datePicker = (DatePicker) findViewById(R.id.date_picker);
        datePicker.setVisibility(View.VISIBLE);
    }

    protected DateTimePickerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.setContentView(R.layout.dialog_date_time_picker);
        this.setTitle("Select date and time");
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        timePicker.setVisibility(View.GONE);
        datePicker = (DatePicker) findViewById(R.id.date_picker);
        datePicker.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btnDatePicker = (Button) findViewById(R.id.btn_date_picker);
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setVisibility(View.VISIBLE);
                timePicker.setVisibility(View.GONE);
            }
        });

        Button btnTimePicker = (Button) findViewById(R.id.btn_time_picker);
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.setVisibility(View.GONE);
                timePicker.setVisibility(View.VISIBLE);
            }
        });

        Button cancelBtn = (Button) findViewById(R.id.cancel_dialog);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimePickerDialog.this.dismiss();
            }
        });

        final Button setTimeButton = (Button) findViewById(R.id.ok_date_time_dialog);
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatePicker datePicker = (DatePicker) findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) findViewById(R.id.time_picker);

                result = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                DateTimePickerDialog.this.dismiss();
            }
        });
    }

    public Calendar getResult() {
        return result;
    }
}
