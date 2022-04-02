package com.nqm.event_manager.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.AddEmployeeActivity;
import com.nqm.event_manager.activities.ViewEmployeeActivity;
import com.nqm.event_manager.adapters.ViewEmployeeListAdapter;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnManageEmployeeItemClicked;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class ManageEmployeeFragment extends Fragment implements IOnDataLoadComplete, IOnManageEmployeeItemClicked {

    public static IOnDataLoadComplete thisListener;
//    CustomListView employeeListView;
    ListView employeeListView;
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

        thisListener = this;
        EmployeeRepository.getInstance().addListener(this);

        connectViews(view);

        searchString = "";
        resultEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsBySearchString(searchString);
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
                resultEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsBySearchString(newText);
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
            startActivity(intent);
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
        resultEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsBySearchString(searchString);
        employeeAdapter.notifyDataSetChanged(resultEmployeesIds);
    }

    @Override
    public void onResume() {
        EmployeeRepository.getInstance().addListener(this);
        searchString = "";
        resultEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsBySearchString(searchString);
        employeeAdapter.notifyDataSetChanged(resultEmployeesIds);
        super.onResume();
    }

    @Override
    public void onEmployeeListItemClicked(String employeeId) {
        Intent intent = new Intent(getActivity(), ViewEmployeeActivity.class);
        intent.putExtra("employeeId", employeeId);
        startActivity(intent);
    }
}
