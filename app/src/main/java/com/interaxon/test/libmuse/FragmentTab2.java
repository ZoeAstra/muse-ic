package com.interaxon.test.libmuse;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseManager;

import java.util.ArrayList;
import java.util.List;


public class FragmentTab2 extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get the view from fragmenttab2.xml
        View view = inflater.inflate(R.layout.fragmenttab2, container, false);

        return view;
    }

    @Override
    public void onClick(View v) {
        MainActivity mainActivity = ((MainActivity) getActivity());
        Spinner musesSpinner = (Spinner) getView().findViewById(R.id.muses_spinner);
    }
}