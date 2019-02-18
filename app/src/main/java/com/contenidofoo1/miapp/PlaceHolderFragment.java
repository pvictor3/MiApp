package com.contenidofoo1.miapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlaceHolderFragment extends Fragment {
    public static final String SECTION_TITLE = "section number";

    public static PlaceHolderFragment newInstance(String sectionTitle){
        PlaceHolderFragment fragment = new PlaceHolderFragment();
        Bundle args = new Bundle();
        args.putString(SECTION_TITLE, sectionTitle);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceHolderFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.section_fragment,container,false);

        String title = getArguments().getString(SECTION_TITLE);
        TextView titulo = view.findViewById(R.id.title_text);
        titulo.setText(title);
        return view;
    }
}
