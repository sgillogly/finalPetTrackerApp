package com.example.pettrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PetAdapter.Listener{

    SQLiteDatabase sqLiteDatabase;
    PetDatabaseHelper petDatabaseHelper;
    RecyclerView recyclerView;
    PetAdapter petAdapter;
    int petNumber = 1;
    String name, type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));

        recyclerView = findViewById(R.id.recyclerView);
        petAdapter = new PetAdapter(this);
        recyclerView.setAdapter(petAdapter);
        petAdapter.setListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
        sqLiteDatabase = petDatabaseHelper.getReadableDatabase();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuAddPet:
                showAddDialog();
                return true;
            case R.id.menuHome:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog(){
        FragmentManager fm = getSupportFragmentManager();
        AddPetDialogFragment addPetDialogFragment = AddPetDialogFragment.newInstance("Add New Pet");
        addPetDialogFragment.show(fm, "fragment_add_pet");
    }

    @Override
    protected void onResume() {
        super.onResume();
        petAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(int position) {
        Intent intent = new Intent(getApplicationContext(), DetailViewActivity.class);
        intent.putExtra("pos", position);
        startActivity(intent);
    }
}