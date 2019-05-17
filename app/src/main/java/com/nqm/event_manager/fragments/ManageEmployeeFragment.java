package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.AddEmployeeActivity;
import com.nqm.event_manager.activities.AddEventActivity;
import com.nqm.event_manager.activities.ViewEmployeeActivity;
import com.nqm.event_manager.adapters.ViewEmployeeListAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnCustomViewClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;

public class ManageEmployeeFragment extends Fragment implements IOnDataLoadComplete, IOnCustomViewClicked {

    private static final int RESULT_FROM_ADD_EMPLOYEE_INTENT = 8;
    private static final int RESULT_FROM_DELETE_EMPLOYEE_INTENT = 9;
    static int RESULT_FROM_VIEW_EMPLOYEE_INTENT = 6;

    CustomListView employeeListView;
    ViewEmployeeListAdapter employeeAdapter;
    ArrayList<String> resultEmployeesIds;
    String searchString;

    public ManageEmployeeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectViews(view);
        EmployeeRepository.getInstance(this);
        searchString = "";
        resultEmployeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsBySearchString(searchString);
        employeeAdapter = new ViewEmployeeListAdapter(getActivity(), resultEmployeesIds);
        employeeListView.setAdapter(employeeAdapter);
        employeeAdapter.setListener(this);

        addEvents();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage_employee_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.manage_employee_search_action);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //set adapter search string = newText
                //notifyOnDataSetChanged()
                resultEmployeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsBySearchString(newText);
                employeeAdapter.notifyDataSetChanged(resultEmployeesIds);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.manage_employee_add_action) {
            Intent intent = new Intent(getActivity(), AddEmployeeActivity.class);
            startActivityForResult(intent, RESULT_FROM_ADD_EMPLOYEE_INTENT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectViews(View view) {
        employeeListView = view.findViewById(R.id.manage_employee_view_employee_list_view);
    }

    private void addEvents() {
    }

    @Override
    public void notifyOnLoadComplete() {

    }

    @Override
    public void onDeleteButtonClicked(int position) {

    }

    @Override
    public void onTimeEditTextSet(int position, String timeText) {

    }

    @Override
    public void onEmployeeListItemClicked(String employeeId) {
        Intent intent = new Intent(getActivity(), ViewEmployeeActivity.class);
        intent.putExtra("employeeId", employeeId);
        startActivityForResult(intent, RESULT_FROM_VIEW_EMPLOYEE_INTENT);
    }

    @Override
    public void onAddScheduleItemMoved() {

    }

    @Override
    public void onAddScheduleItemRemoved() {

    }

    @Override
    public void onResume() {
        searchString = "";
        resultEmployeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsBySearchString(searchString);
        employeeAdapter.notifyDataSetChanged(resultEmployeesIds);
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_FROM_VIEW_EMPLOYEE_INTENT && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra("delete?", false)) {
                String employeeId = data.getStringExtra("employeeId");
                DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.EMPLOYEE_COLLECTION)
                        .document(employeeId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Xóa nhân viên thành công", Toast.LENGTH_SHORT).show();
                                employeeAdapter.notifyDataSetChanged(EmployeeRepository.getInstance(null).getAllEmployeesIds());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Xóa nhân viên thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                employeeAdapter.notifyDataSetChanged(EmployeeRepository.getInstance(null).getAllEmployeesIds());
            }
        } else if (requestCode == RESULT_FROM_ADD_EMPLOYEE_INTENT && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra("add?", false)) {
                if(data.getBooleanExtra("add succeed", false)) {
                    Toast.makeText(getContext(), "Thêm nhân viên thành công", Toast.LENGTH_SHORT).show();
                    employeeAdapter.notifyDataSetChanged(EmployeeRepository.getInstance(null).getAllEmployeesIds());
                } else {
                    Toast.makeText(getContext(), "Thêm nhân viên thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
