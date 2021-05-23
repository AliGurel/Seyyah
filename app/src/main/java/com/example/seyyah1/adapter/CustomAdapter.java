package com.example.seyyah1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.seyyah1.R;
import com.example.seyyah1.model.Yerler;
import com.google.android.material.transition.Hold;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class CustomAdapter extends ArrayAdapter<Yerler> {
    ArrayList<Yerler> yerListesi;
    Context context;

    public CustomAdapter(@NonNull Context context, ArrayList<Yerler> yerListesi ) {
        super(context, R.layout.custom_list, yerListesi); // kendi oluşturduğum custom_list.xml i biçim olarak al dedik
        this.yerListesi = yerListesi;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // .xml ile kodu birbirine bağlamak için inflater kullanıcaz
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View customView = layoutInflater.inflate(R.layout.custom_list,parent,false);
        TextView nameTextView = customView.findViewById(R.id.recycler_txt_name);
        nameTextView.setText(yerListesi.get(position).name);

        return customView;
    }
}
