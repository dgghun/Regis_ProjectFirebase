package com.dgarcia.project_firebase.view_logic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dgarcia.project_firebase.R;
import com.dgarcia.project_firebase.model.TestObject;


public class RecyclerItemInfoFragment extends Fragment{

    private View view;
    public static final String PARAM_IN_TESTOBJECT = "in_testObject";

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstances) {

        view = inflater.inflate(R.layout.fragment_recycler_item_info, container, false);

        TextView mHeaderView = (TextView)view.findViewById(R.id.item_info_header);
        TextView mIdView = (TextView)view.findViewById(R.id.item_info_id);
        TextView mDateView = (TextView)view.findViewById(R.id.item_info_date);

        TestObject testObject = (TestObject) getActivity().getIntent().getSerializableExtra(PARAM_IN_TESTOBJECT);

        mIdView.setText("ID: " + testObject.getId());
        mDateView.setText("DATE: "+ testObject.getDate());

        return view;
    }
}
