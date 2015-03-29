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

        Button refreshButton = (Button) view.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);

        Button connectButton = (Button) view.findViewById(R.id.connect);
        connectButton.setOnClickListener(this);

        Button disconnectButton = (Button) view.findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);

        Button pauseButton = (Button) view.findViewById(R.id.pause);
        pauseButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        MainActivity mainActivity = ((MainActivity) getActivity());
        Spinner musesSpinner = (Spinner) getView().findViewById(R.id.muses_spinner);
        if (v.getId() == R.id.refresh) {
            MuseManager.refreshPairedMuses();
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            List<String> spinnerItems = new ArrayList<String>();
            for (Muse m: pairedMuses) {
                String dev_id = m.getName() + "-" + m.getMacAddress();
                Log.i("Muse Headband", dev_id);
                spinnerItems.add(dev_id);
            }
            ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (
                    getActivity(), android.R.layout.simple_spinner_item, spinnerItems);
            musesSpinner.setAdapter(adapterArray);
        }
        else if (v.getId() == R.id.connect) {
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            if (pairedMuses.size() < 1 ||
                    musesSpinner.getAdapter().getCount() < 1) {
                Log.w("Muse Headband", "There is nothing to connect to");
            }
            else {
                mainActivity.setMuse(pairedMuses.get(musesSpinner.getSelectedItemPosition()));
                ConnectionState state = mainActivity.getMuse().getConnectionState();
                if (state == ConnectionState.CONNECTED ||
                        state == ConnectionState.CONNECTING) {
                    Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
                    return;
                }

                mainActivity.configure_library();
                /**
                 * In most cases libmuse native library takes care about
                 * exceptions and recovery mechanism, but native code still
                 * may throw in some unexpected situations (like bad bluetooth
                 * connection). Print all exceptions here.
                 */
                try {
                    mainActivity.getMuse().runAsynchronously();
                } catch (Exception e) {
                    Log.e("Muse Headband", e.toString());
                }
            }
        }
        else if (v.getId() == R.id.disconnect) {
            if (mainActivity.getMuse() != null) {
                /**
                 * true flag will force libmuse to unregister all listeners,
                 * BUT AFTER disconnecting and sending disconnection event.
                 * If you don't want to receive disconnection event (for ex.
                 * you call disconnect when application is closed), then
                 * unregister listeners first and then call disconnect:
                 * muse.unregisterAllListeners();
                 * muse.disconnect(false);
                 */
                mainActivity.getMuse().disconnect(true);
            }
        }
        else if (v.getId() == R.id.pause) {
            boolean dataTransmission = mainActivity.isDataTransmission();
            dataTransmission = !dataTransmission;
            if (mainActivity.getMuse() != null) {
                mainActivity.getMuse().enableDataTransmission(dataTransmission);
            }
        }
    }
}