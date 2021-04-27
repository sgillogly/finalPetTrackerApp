package com.example.pettrackerapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddPetDialogFragment extends DialogFragment implements View.OnClickListener{

    private EditText editTextName;
    private EditText editTextType;
    private Button buttonAdd;
    private Button buttonCancel;
    private PetDatabaseHelper petDatabaseHelper;
    SQLiteDatabase sqLiteDatabase;

    public AddPetDialogFragment(){

    }

    public static AddPetDialogFragment newInstance(String title){
        AddPetDialogFragment fragment = new AddPetDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        petDatabaseHelper = new PetDatabaseHelper(getContext());
        editTextName = view.findViewById(R.id.editPetName);
        editTextType = view.findViewById(R.id.editTextType);
        buttonAdd = view.findViewById(R.id.buttonAdd);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        String title = getArguments().getString("title", "Enter name");
        getDialog().setTitle(title);

        buttonAdd.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        editTextName.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.buttonAdd:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                if(editTextName.getText().toString().equals("") || editTextType.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Must fill out all info to add a pet", Toast.LENGTH_LONG).show();
                }
                else{
                    PetEntry petEntry = new PetEntry(editTextName.getText().toString(), editTextType.getText().toString());
                    petDatabaseHelper.insertData(petEntry);
                }
                startActivity(intent);
                break;
            case R.id.buttonCancel:
                dismiss();
                break;
        }
    }

}