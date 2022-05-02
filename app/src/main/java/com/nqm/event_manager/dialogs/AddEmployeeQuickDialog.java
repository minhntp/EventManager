package com.nqm.event_manager.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IAddEmployeeDialogListener;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

public class AddEmployeeQuickDialog extends Dialog {

    Button addButton;
    Button cancelButton;
    EditText nameEditText;
    AutoCompleteTextView specialityEditText;

    WindowManager.LayoutParams layoutParams;

    IAddEmployeeDialogListener listener;

    public AddEmployeeQuickDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_add_employee_quick);

        layoutParams = new WindowManager.LayoutParams();
        if (getWindow() != null) {
            layoutParams.copyFrom(getWindow().getAttributes());
        }
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        nameEditText = findViewById(R.id.add_employee_quick_name_edit_text);
        specialityEditText = findViewById(R.id.add_employee_quick_speciality_auto_complete_text_view);
        addButton = findViewById(R.id.add_employee_quick_ok_button);
        cancelButton = findViewById(R.id.add_employee_quick_cancel_button);

        addButton.setOnClickListener(v -> {
            addEmployeeToDatabase();
        });
        cancelButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            getWindow().setAttributes(layoutParams);
        }
    }

    public void setListener(IAddEmployeeDialogListener listener) {
        this.listener = listener;
    }

    private void addEmployeeToDatabase() {
        boolean error = false;

        if (nameEditText.getText().toString().isEmpty()) {
            nameEditText.setError("Xin mời nhập");
            error = true;
        } else {
            nameEditText.setError(null);
        }
        if (specialityEditText.getText().toString().isEmpty()) {
            specialityEditText.setError("Xin mời nhập");
            error = true;
        } else {
            specialityEditText.setError(null);
        }

        if (error) {
            return;
        }

        Employee employee = new Employee(nameEditText.getText().toString().trim(),
                specialityEditText.getText().toString().trim());
        EmployeeRepository.getInstance().addEmployee(employee);

        listener.onNewEmployeeSaved();

        dismiss();
    }
}
