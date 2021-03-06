package com.heig.atmanager.addContent;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.heig.atmanager.calendar.local.LocalCalendarHandler;
import com.heig.atmanager.MainActivity;
import com.heig.atmanager.notifications.NotificationUtils;
import com.heig.atmanager.userData.PostRequests;
import com.heig.atmanager.R;
import com.heig.atmanager.Utils;
import com.heig.atmanager.taskLists.TaskList;
import com.heig.atmanager.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class AddTaskFragment extends Fragment {

    public static final String FRAG_ADD_TASK_ID = "Add_Task_Fragment";

    private static final String TAG = "AddTaskFragment";

    private static final int MINUTE = 0;
    private static final int HOUR   = 1;
    private static final int DAY    = 2;
    private static final int MONTH  = 3;
    private static final int YEAR   = 4;

    private String title;
    private String description;
    private DatePickerDialog picker;
    private final Calendar calendar = Calendar.getInstance();
    private Button cancelButton;

    private int[] dueDateValues;
    private int[] reminderDateValues;


    private ArrayList<Task> tasks;
    private RecyclerView tasksRecyclerView;

    private TaskList selectedDirectory;

    private EditText titleEditText;
    private EditText descriptionEditText;

    private TextView dueDateTextView;
    private TextView dueTimeTextView;

    private TextView reminderTextView;

    private TextInputLayout titleLayout;
    private Button validationButton;

    private ArrayList<String> tags;

    private NotificationUtils mNotificationUtils;


    public AddTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasksRecyclerView = (RecyclerView) getActivity().findViewById(R.id.tasks_rv);

        mNotificationUtils = new NotificationUtils(getContext());


        // Override OnBacPressed to show hidden components
        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                getFragmentManager().popBackStack();

                getActivity().findViewById(R.id.fab_container).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.dock).setVisibility(View.VISIBLE);
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View mView = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Bind all input with id
        titleEditText = mView.findViewById(R.id.frag_add_task_title);
        descriptionEditText = mView.findViewById(R.id.frag_add_task_description);
        cancelButton = mView.findViewById(R.id.frag_add_task_cancel_button);

        dueDateTextView = mView.findViewById(R.id.frag_add_task_due_date);
        dueTimeTextView = mView.findViewById(R.id.frag_add_task_due_time);
        dueDateValues   = new int[5];

        reminderTextView = mView.findViewById(R.id.frag_add_task_notification);
        reminderDateValues = new int[5];

        titleLayout = mView.findViewById(R.id.frag_add_task_title_layout);

        validationButton = mView.findViewById(R.id.frag_validation_button);

        // date setup here
        setupDatePicker(dueDateTextView, dueTimeTextView, dueDateValues);

        // Picker for notification
        reminderTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                reminderDateValues[DAY] = calendar.get(Calendar.DAY_OF_MONTH);
                reminderDateValues[MONTH] = calendar.get(Calendar.MONTH);
                reminderDateValues[YEAR] = calendar.get(Calendar.YEAR);

                reminderDateValues[HOUR] = calendar.get(Calendar.HOUR_OF_DAY);
                reminderDateValues[MINUTE] = calendar.get(Calendar.MINUTE);


                DatePickerDialog reminderDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        final String notifTime = Utils.formatNumber(dayOfMonth) + "." +
                                Utils.formatNumber(month + 1) + "." +
                                Utils.formatNumber(year);

                        TimePickerDialog reminderTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                reminderDateValues[HOUR] = hourOfDay;
                                reminderDateValues[MINUTE] = minute;
                                reminderTextView.setText(notifTime + " " + Utils.formatNumber(hourOfDay) + ":" + Utils.formatNumber(minute));

                            }
                        }, reminderDateValues[HOUR], reminderDateValues[MINUTE], true);
                        reminderTimePicker.show();


                        reminderDateValues[YEAR] = year;
                        reminderDateValues[MONTH] = month;
                        reminderDateValues[DAY] = dayOfMonth;

                    }
                }, reminderDateValues[YEAR], reminderDateValues[MONTH], reminderDateValues[DAY]);

                // Show the picker
                reminderDatePicker.show();


            }
        });

        if (MainActivity.getUser().getTags() != null) {
            for (String s : MainActivity.getUser().getTags())
                Log.d(TAG, "onCreateView: Tag : " + s);
        }

        // Tags
        tags = new ArrayList<>();

        // Enable the user to choose between his/her tags
        final ArrayAdapter<String> chipsAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, MainActivity.getUser().getTags());

        // App detect the input to suggest the tag
        final AutoCompleteTextView autoCompleteTextView = mView.findViewById(R.id.frag_add_task_autocomplete_textview);
        autoCompleteTextView.setAdapter(chipsAdapter);
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    ChipGroup chipGroup = mView.findViewById(R.id.frag_add_task_chipgroup);
                    addChipToGroup(autoCompleteTextView.getText().toString().trim(), chipGroup);
                    autoCompleteTextView.setText(null);
                }
                return false;
            }
        });

        // Directory spinner
        final Spinner folderSpinner = mView.findViewById(R.id.frag_directory_choice_tag_spinner);
        ArrayAdapter<TaskList> spinnerAdapter = new AddTaskSpinnerAdapter(getActivity(),
                R.layout.support_simple_spinner_dropdown_item,
                MainActivity.getUser().getAllTaskLists());
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        folderSpinner.setAdapter(spinnerAdapter);

        // Button
        validationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = titleEditText.getText().toString();
                description = descriptionEditText.getText().toString();
                selectedDirectory = (TaskList) folderSpinner.getSelectedItem();


                if (title.isEmpty()) {
                    titleLayout.setError(getString(R.string.input_missing));
                    return;
                } else {
                    titleLayout.setError(null);
                }

                Task newTask = new Task(title, description);

                if (dueDateValues[YEAR] != 0) {
                    newTask.setDueDate(getSelectedDate(dueDateValues));
                    LocalCalendarHandler.getInstance().setTask(newTask);
                    LocalCalendarHandler.getInstance().addTask(getActivity());
                }


                if (reminderDateValues[YEAR] != 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(reminderDateValues[YEAR], reminderDateValues[MONTH], reminderDateValues[DAY], reminderDateValues[HOUR], reminderDateValues[MINUTE]);
                    Notification.Builder nb = mNotificationUtils.getChannelNotification(getString(R.string.app_name), title);
                    mNotificationUtils.scheduleNotification(nb.build(), calendar.getTimeInMillis());
                    newTask.setReminderDate(calendar.getTime());
                }

                // Add the tags
                for (String tag : tags) {
                    newTask.addTag(tag);
                }

                // Add the task to a selected taskList
                for (TaskList taskList : MainActivity.getUser().getAllTaskLists()) {
                    if (taskList.equals(selectedDirectory)) {
                        // Assigning the tasklist and adding the task in the tasklist
                        // (which is already in the user)
                        newTask.setTasklist(taskList);
                        PostRequests.postTask(newTask, getContext());
                        MainActivity.getUser().addTask(newTask);
                        //update homeview
                        tasks = MainActivity.getUser().getHomeViewTasks();
                        break;

                    }
                }



                getActivity().findViewById(R.id.fab_container).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.dock).setVisibility(View.VISIBLE);

                getFragmentManager().popBackStack();
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();

                getActivity().findViewById(R.id.fab_container).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.dock).setVisibility(View.VISIBLE);
            }
        });

        return mView;
    }


    /**
     * Add a string as chip into the given chip group
     *
     * @param tag       a name
     * @param chipGroup where to add
     */
    private void addChipToGroup(String tag, final ChipGroup chipGroup) {
        final Chip chip = new Chip(this.getContext());

        chip.setText(tag);
        chip.setCloseIconEnabled(true);

        chip.setClickable(true);
        chip.setCheckable(false);
        chipGroup.addView(chip);
        tags.add(tag);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chipGroup.removeView(chip);
                tags.remove(chip.getText().toString());
            }
        });
    }

    private void setupDatePicker(final TextView date, final TextView time, final int[] values) {
        // Picker for date and time
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                values[DAY]   = calendar.get(Calendar.DAY_OF_MONTH);
                values[MONTH] = calendar.get(Calendar.MONTH);
                values[YEAR]  = calendar.get(Calendar.YEAR);

                // Bind the picker value to ours variable
                picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dueDateString = Utils.formatNumber(dayOfMonth) + "." +
                                Utils.formatNumber(month + 1) + "." +
                                Utils.formatNumber(year);
                        date.setText(dueDateString);
                        dueDateValues[DAY]   = dayOfMonth;
                        dueDateValues[MONTH] = month;
                        dueDateValues[YEAR]  = year;
                    }
                }, values[YEAR], values[MONTH], values[DAY]);

                // Show the picker
                picker.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values[MINUTE] = calendar.get(Calendar.MINUTE);
                values[HOUR]   = calendar.get(Calendar.HOUR_OF_DAY);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.setText(Utils.formatNumber(hourOfDay) + ":" + Utils.formatNumber(minute));
                        dueDateValues[HOUR]   = hourOfDay;
                        dueDateValues[MINUTE] = minute;
                    }
                }, values[MINUTE], values[HOUR], true);
                timePickerDialog.show();
            }
        });
    }

    private Date getSelectedDate(int[] values) {
        Date selectedDate;

        if (values[YEAR] == 0) {
            selectedDate = null;
        } else {
            selectedDate = new GregorianCalendar(values[YEAR], values[MONTH],
                    values[DAY], values[HOUR], values[MINUTE]).getTime();

        }

        return selectedDate;
    }

}
