package com.example.pettrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Iterator;

public class DetailViewActivity extends AppCompatActivity {

    String name;
    String type;
    int id, position;

    ImageView imageView;
    TextView petNameTextView;
    TextView petTypeTextView;
    Button deleteButton;
    public com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private PetDatabaseHelper petDatabaseHelper;
    SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\">" + getString(R.string.app_name) + "</font>"));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_detail_view);
        Intent intent = getIntent();
        petNameTextView = findViewById(R.id.DetailPetNameTextView);
        petTypeTextView = findViewById(R.id.DetailPetTypeTextView);
        deleteButton = findViewById(R.id.deleteButton);
        imageView = findViewById(R.id.imageView);

        PetDatabaseHelper petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("pets", new String[]{"_id", "name", "type", "drawable", "homeLat", "homeLong", "petLat", "petLong"}, null, null, null, null, null);
        position = intent.getIntExtra("pos", 0);
        cursor.moveToPosition(position);

        int column = cursor.getColumnIndex("_id");
        id = Integer.parseInt(cursor.getString(column));
        column = cursor.getColumnIndex("name");
        name = cursor.getString(column);
        column = cursor.getColumnIndex("type");
        type = cursor.getString(column);
        petNameTextView.setText(name);
        petTypeTextView.setText(type);

        if(type.equals("cat") || type.equals("CAT") || type.equals("Cat")){
            imageView.setImageResource(R.drawable.cat);
        } else if(type.equals("dog") || type.equals("DOG") || type.equals("Dog")){
            imageView.setImageResource(R.drawable.dog);
        } else{
            imageView.setImageResource(R.drawable.pet);
        }
    }

    public void onLaunchLocation(View view){
        Intent intent = new Intent(getApplicationContext(), LocationStringActivity.class);
        intent.putExtra("_id", id);
        intent.putExtra("name", name);
        intent.putExtra("type", type);
        intent.putExtra("pos", position);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
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

    public void onClickDelete(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Pet?");
        builder.setMessage("Are you sure you want to delete this pet?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(DetailViewActivity.this, "pet deleted", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
                sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
                sqLiteDatabase.delete("pets",
                            "name = ?",
                            new String[]{name});
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog alertDialog = builder.create();
                alertDialog.dismiss();
            }
        });

        builder.show();
    }

    public void onClickSetCurrentLocation(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        Toast.makeText(DetailViewActivity.this, "Current location set as home location", Toast.LENGTH_SHORT).show();
                        ContentValues updatedPetValues = new ContentValues();
                        updatedPetValues.put("homeLat", location.getLatitude());
                        updatedPetValues.put("homeLong", location.getLongitude());
                        petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
                        sqLiteDatabase = petDatabaseHelper.getWritableDatabase();
                        sqLiteDatabase.update("pets",
                                updatedPetValues,
                                "_id = ?",
                                new String[] {String.valueOf(id)});
                    } else{
                        Toast.makeText(DetailViewActivity.this, "Location not received", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}