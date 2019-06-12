package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEditReminderItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderItemClicked;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.repositories.DefaultReminderRepository;

import java.util.ArrayList;

public class MoreSettingsFragment extends Fragment implements IOnDataLoadComplete, IOnEditReminderItemClicked,
        IOnSelectReminderItemClicked {

    CustomListView editReminderListView;
    EditReminderAdapter editReminderAdapter;
    Button selectReminderButton;

    WindowManager.LayoutParams lWindowParams;
    CustomListView selectReminderListView;
    SelectReminderAdapter selectReminderAdapter;
    Dialog selectReminderDialog;
    Button selectReminderOkButton;

    ArrayList<Reminder> selectedReminders;
    boolean isRemindersChanged;

    Activity context;

    public MoreSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity();

        connectViews(view);
        init();
        addEvents();
    }

    private void connectViews(View view) {
        selectReminderButton = view.findViewById(R.id.more_settings_add_reminder_button);
        editReminderListView = view.findViewById(R.id.more_settings_reminder_list_view);
    }

    private void init() {
        selectedReminders = new ArrayList<>();

        editReminderAdapter = new EditReminderAdapter(context, selectedReminders);
        editReminderAdapter.setListener(this);
        editReminderListView.setAdapter(editReminderAdapter);

        DefaultReminderRepository.getInstance().setListener(this);
        ArrayList<Integer> defaultReminders = DefaultReminderRepository.getInstance().getDefaultReminders();
        if (defaultReminders.size() > 0) {
            for (int minute : defaultReminders) {
                selectedReminders.add(new Reminder("", "", minute, ""));
            }
            editReminderAdapter.notifyDataSetChanged();
        }

        initSelectReminderDialog();
        isRemindersChanged = false;
    }

    private void initSelectReminderDialog() {
        selectReminderDialog= new Dialog(context);
        selectReminderDialog.setContentView(R.layout.dialog_select_reminder);

        lWindowParams = new WindowManager.LayoutParams();
        if (selectReminderDialog.getWindow() != null) {
            lWindowParams.copyFrom(selectReminderDialog.getWindow().getAttributes());
        }
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        selectReminderListView = selectReminderDialog.findViewById(R.id.select_reminder_list_view);
        selectReminderOkButton = selectReminderDialog.findViewById(R.id.select_reminder_ok_button);

        selectReminderAdapter = new SelectReminderAdapter(context, selectedReminders);
        selectReminderAdapter.setListener(this);
        selectReminderListView.setAdapter(selectReminderAdapter);

        //Add events
        selectReminderOkButton.setOnClickListener(view -> {
            editReminderAdapter.notifyDataSetChanged();
            selectReminderDialog.dismiss();
        });
    }

    private void addEvents() {
        selectReminderButton.setOnClickListener(v -> {
            if (!context.isFinishing()) {
                selectReminderAdapter.notifyDataSetChanged();
                selectReminderDialog.show();
                if (selectReminderDialog.getWindow() != null) {
                    selectReminderDialog.getWindow().setAttributes(lWindowParams);
                }
            }
        });
    }


    @Override
    public void notifyOnLoadComplete() {
        ArrayList<Integer> defaultReminders = DefaultReminderRepository.getInstance().getDefaultReminders();
        if (defaultReminders.size() > 0) {
            selectedReminders.clear();
            for (int minute : defaultReminders) {
                selectedReminders.add(new Reminder("", "", minute, ""));
            }
            editReminderAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onReminderClearButtonClicked(int minute) {
        for (Reminder r : selectedReminders) {
            if (r.getMinute() == minute) {
                selectedReminders.remove(r);
                editReminderAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void remindersChanged() {
        isRemindersChanged = true;
    }

    @Override
    public void onSelectReminderCheckBoxClicked(int minute, boolean isChecked) {
        if (isChecked) {
            selectedReminders.add(new Reminder("", "", minute, ""));
        } else {
            for (Reminder r : selectedReminders) {
                if (r.getMinute() == minute) {
                    selectedReminders.remove(r);
                    editReminderAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    public void onPause() {
        if (isRemindersChanged) {
            DefaultReminderRepository.getInstance().updateDefaultReminders(selectedReminders);
        }
        super.onPause();
    }
}
