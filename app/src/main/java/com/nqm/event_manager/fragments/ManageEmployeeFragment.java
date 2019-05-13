package com.nqm.event_manager.fragments;

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

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.ViewEmployeeAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class ManageEmployeeFragment extends Fragment implements IOnDataLoadComplete {

    CustomListView employeeListView;
    ViewEmployeeAdapter employeeAdapter;
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
        employeeAdapter = new ViewEmployeeAdapter(getActivity(), resultEmployeesIds);
        employeeListView.setAdapter(employeeAdapter);
        EmployeeRepository.getInstance(null).normalizeString("ối giời ơi đá vào, rồi");
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

    private void connectViews(View view) {
        employeeListView = view.findViewById(R.id.manage_employee_view_employee_list_view);
    }

    @Override
    public void notifyOnLoadComplete() {

    }
}
