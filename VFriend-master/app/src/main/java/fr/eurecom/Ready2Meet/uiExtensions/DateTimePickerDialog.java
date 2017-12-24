package fr.eurecom.Ready2Meet.uiExtensions;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import fr.eurecom.Ready2Meet.R;

/**
 * Dialog which allows selecting a date and time at the same moment.
 */
public class DateTimePickerDialog extends Dialog {
    private TimePicker timePicker;
    private DatePicker datePicker;

    private Calendar result;
    private Calendar minDate;

    public DateTimePickerDialog(@NonNull Context context) {
        super(context);
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
                result = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(),
                        datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker
                        .getCurrentMinute());

                if(minDate != null && result.compareTo(minDate) < 0) {
                    Toast.makeText(getContext(), "The selected date is too small", Toast
                            .LENGTH_LONG).show();
                    result = null;
                    return;
                }

                DateTimePickerDialog.this.dismiss();
            }
        });
    }

    public Calendar getResult() {
        return result;
    }

    /**
     * Set the minimal date which can be selected in the calendar.
     *
     * @param date - The minimum date
     */
    public void setMinDate(Calendar date) {
        if(date != null) {
            datePicker.setMinDate(date.getTimeInMillis());
            minDate = date;
        }
    }
}
