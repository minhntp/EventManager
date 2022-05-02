package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditEmployeeAddEventAdapter;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeAddEventAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEditEmployeeItemClicked;
import com.nqm.event_manager.interfaces.IOnEditReminderItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderItemClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.repositories.DefaultEmployeeRepository;
import com.nqm.event_manager.repositories.DefaultReminderRepository;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.EmployeeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoreSettingsFragment extends Fragment implements IOnDataLoadComplete, IOnEditReminderItemClicked,
        IOnSelectReminderItemClicked, IOnEditEmployeeItemClicked, IOnSelectEmployeeItemClicked {

    RecyclerView editReminderRecyclerView;
    EditReminderAdapter editReminderAdapter;
    Button selectReminderButton;

    WindowManager.LayoutParams lWindowParams;
    ListView selectReminderListView;
    SelectReminderAdapter selectReminderAdapter;
    Dialog selectReminderDialog;
    Button selectReminderOkButton;

    ArrayList<Reminder> selectedReminders;

    ArrayList<String> selectedEmployeesIds;
    Button selectEmployeeButton;
    EditEmployeeAddEventAdapter editEmployeeAdapter;
    RecyclerView editEmployeeRecyclerView;

    List<Employee> employees;
    Dialog selectEmployeeDialog;
    Button selectEmployeeOkButton;
    SearchView selectEmployeeSearchView;
    RecyclerView selectEmployeeRecyclerView;
    SelectEmployeeAddEventAdapter selectEmployeeAdapter;

    boolean isRemindersChanged, isEmployeesChanged;

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
        editReminderRecyclerView = view.findViewById(R.id.more_settings_reminder_recycler_view);
        selectEmployeeButton = view.findViewById(R.id.more_settings_add_employee_button);
        editEmployeeRecyclerView = view.findViewById(R.id.more_settings_employee_recycler_view);
    }

    private void init() {
        selectedReminders = new ArrayList<>();

        editReminderAdapter = new EditReminderAdapter(selectedReminders);
        editReminderAdapter.setListener(this);
        LinearLayoutManager linearLayoutManagerReminder = new LinearLayoutManager(context);
        linearLayoutManagerReminder.setOrientation(RecyclerView.VERTICAL);
        editReminderRecyclerView.setLayoutManager(linearLayoutManagerReminder);
        editReminderRecyclerView.setAdapter(editReminderAdapter);

        DefaultReminderRepository.getInstance().setListener(this);
        ArrayList<Integer> defaultReminders = new ArrayList<Integer>(DefaultReminderRepository.getInstance()
                .getDefaultReminders().values());
        if (defaultReminders != null && defaultReminders.size() > 0) {
            for (int minute : defaultReminders) {
                selectedReminders.add(new Reminder("", "", minute, ""));
            }
//            editReminderAdapter.notifyDataSetChanged();
            editReminderAdapter.customNotifyDataSetChanged();
        }

        initSelectReminderDialog();
        isRemindersChanged = false;

        selectedEmployeesIds = new ArrayList<>();
        editEmployeeAdapter = new EditEmployeeAddEventAdapter(selectedEmployeesIds, new HashMap<>());
        editEmployeeAdapter.setListener(this);
        LinearLayoutManager linearLayoutManagerEmployee = new LinearLayoutManager(context);
        linearLayoutManagerEmployee.setOrientation(RecyclerView.VERTICAL);
        editEmployeeRecyclerView.setLayoutManager(linearLayoutManagerEmployee);
        editEmployeeRecyclerView.setAdapter(editEmployeeAdapter);
        editEmployeeRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        DefaultReminderRepository.getInstance().setListener(this);
        HashMap<String, String> defaultEmployeesIds = DefaultEmployeeRepository.getInstance().getDefaultEmployeeIds();
        if (defaultEmployeesIds != null && defaultEmployeesIds.size() > 0) {
            for (String id : defaultEmployeesIds.values()) {
                selectedEmployeesIds.add(id);
            }
            EmployeeUtil.sortEmployeesIdsByNameNew(selectedEmployeesIds);
            editEmployeeAdapter.customNotifyDataSetChanged();
        }

        initSelectEmployeeDialog();
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
            selectReminderDialog.dismiss();
        });

        selectReminderDialog.setOnDismissListener(dialog -> editReminderAdapter.customNotifyDataSetChanged());
    }

    private void initSelectEmployeeDialog() {
        selectEmployeeDialog = new Dialog(context);
        selectEmployeeDialog.setContentView(R.layout.dialog_select_employee);

        //Connect views
        selectEmployeeRecyclerView = selectEmployeeDialog.findViewById(R.id.select_employee_recycler_view);
        selectEmployeeOkButton = selectEmployeeDialog.findViewById(R.id.add_employee_ok_button);

        employees = EmployeeRepository.getInstance().getEmployeesBySearchString("");
        selectEmployeeAdapter = new SelectEmployeeAddEventAdapter(selectedEmployeesIds,
                employees);
        selectEmployeeAdapter.setListener(this);
        selectEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        selectEmployeeRecyclerView.setAdapter(selectEmployeeAdapter);
        selectEmployeeRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        //SEARCH employees
        selectEmployeeSearchView = selectEmployeeDialog.findViewById(R.id.select_employee_search_view);
        selectEmployeeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Employee> resultEmployees = EmployeeRepository.getInstance().getEmployeesBySearchString(newText);
                employees.clear();
                employees.addAll(resultEmployees);
                selectEmployeeAdapter.notifyDataSetChanged();
                return true;
            }
        });

        //Add events
        selectEmployeeOkButton.setOnClickListener(view -> {
            editEmployeeAdapter.customNotifyDataSetChanged();
            System.out.println( "ok button clicked. employees size = " + selectedEmployeesIds.size());
            selectEmployeeDialog.dismiss();
        });

        selectEmployeeDialog.setOnDismissListener(dialog -> editEmployeeAdapter.customNotifyDataSetChanged());
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

        selectEmployeeButton.setOnClickListener(v -> {
            showSelectEmployeeDialog();
        });
    }

    private void showSelectEmployeeDialog() {
        if (!context.isFinishing()) {
            selectEmployeeAdapter.notifyDataSetChanged();
            selectEmployeeDialog.show();
            if (selectEmployeeDialog.getWindow() != null) {
                selectEmployeeDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    @Override
    public void notifyOnLoadComplete() {
        ArrayList<Integer> defaultReminders = new ArrayList<Integer>(DefaultReminderRepository.getInstance()
                .getDefaultReminders().values());
        if (defaultReminders.size() > 0) {
            selectedReminders.clear();
            for (int minute : defaultReminders) {
                selectedReminders.add(new Reminder("", "", minute, ""));
            }
            editReminderAdapter.customNotifyDataSetChanged();
        }
    }

    @Override
    public void onReminderClearButtonClicked(int minute) {
        for (Reminder r : selectedReminders) {
            if (r.getMinute() == minute) {
                selectedReminders.remove(r);
                editReminderAdapter.customNotifyDataSetChanged();
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
//                    editReminderAdapter.notifyDataSetChanged();
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
        if (isEmployeesChanged) {
            DefaultEmployeeRepository.getInstance().updateDefaultEmployees(selectedEmployeesIds);
        }
        super.onPause();
    }

    @Override
    public void onDeleteButtonClicked(String employeeId) {
        selectedEmployeesIds.remove(employeeId);
        editEmployeeAdapter.customNotifyDataSetChanged();

        isEmployeesChanged = true;
    }

    @Override
    public void onListItemClicked(String employeeId) {
        //do nothing
    }

    @Override
    public void onCheckBoxClicked(String employeeId, boolean isChecked) {
        if (isChecked) {
            selectedEmployeesIds.add(employeeId);
//            conflictsMap.put(employeeId, null);
        } else {
            selectedEmployeesIds.remove(employeeId);
//            conflictsMap.remove(employeeId);
        }
        isEmployeesChanged = true;
    }
}
