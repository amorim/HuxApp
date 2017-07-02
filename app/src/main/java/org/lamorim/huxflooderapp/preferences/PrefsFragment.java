package org.lamorim.huxflooderapp.preferences;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import org.lamorim.huxflooderapp.R;

/**
 * Created by lucas on 25/12/2016.
 */

public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
        if (fab != null && fab.isShown())
            fab.hide();
        addPreferencesFromResource(R.xml.preferences);
    }
}
