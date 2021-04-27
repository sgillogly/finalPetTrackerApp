package com.example.pettrackerapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PetAdapter extends RecyclerView.Adapter{

    PetDatabaseHelper petDatabaseHelper;
    Context context;

    public PetAdapter(Context applicationContext){
        petDatabaseHelper = new PetDatabaseHelper(applicationContext);
        this.context = applicationContext;
    }

    private Listener listener;
    public static interface Listener {
        public void onClick(int position);
    }
    public void setListener(Listener listener){
        this.listener = listener;
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder{

        public LinearLayout linearLayout;
        public TextView textViewName;
        public TextView textViewType;

        public PetViewHolder(@NonNull View itemView){
            super(itemView);
            this.linearLayout = itemView.findViewById(R.id.recLinearLayout);
            this.textViewName = itemView.findViewById(R.id.nameTextView);
            this.textViewType = itemView.findViewById(R.id.typeTextView);
        }

        public LinearLayout getLinearLayout(){ return linearLayout; }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_layout, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        PetViewHolder petViewHolder = (PetViewHolder) holder;
        View view = petViewHolder.getLinearLayout();

        final SQLiteDatabase sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
        Cursor cursorRecyclerView = sqLiteDatabase.query("pets", new String[]{"_id", "name", "type", "drawable", "homeLat", "homeLong", "petLat", "petLong"}, null, null, null, null, null);
        cursorRecyclerView.moveToPosition(position);

        int column = cursorRecyclerView.getColumnIndex("name");
        petViewHolder.textViewName.setText(cursorRecyclerView.getString(column));
        column = cursorRecyclerView.getColumnIndex("type");
        petViewHolder.textViewType.setText(cursorRecyclerView.getString(column));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(position);
            }
        });

        if(position %2 == 1){
            holder.itemView.setBackgroundColor(Color.parseColor("#D5D6EA"));
        }
    }

    @Override
    public int getItemCount() {
        SQLiteDatabase sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("pets", new String[]{"_id", "name", "type", "drawable", "homeLat", "homeLong", "petLat", "petLong"}, null, null, null, null, null);
        return cursor.getCount();
    }
}
