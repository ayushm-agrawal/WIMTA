package com.manali.wimta;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ActivityVerifyTA extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_ta);

        final TextInputLayout course = findViewById(R.id.course);
        final TextInputLayout type = findViewById(R.id.taType);
        final Button verify = findViewById(R.id.verifyButton);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseText = Objects.requireNonNull(course.getEditText()).getText().toString().trim();
                String typeText = Objects.requireNonNull(type.getEditText()).getText().toString().trim();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                assert currentUser != null;
                mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

                HashMap<String, Object> taMap = new HashMap<>();
                taMap.put("course", courseText);
                taMap.put("type", typeText);

                mDatabaseReference.updateChildren(taMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Intent mainActivity = new Intent(ActivityVerifyTA.this, MainActivity.class);
                            startActivity(mainActivity);
                            final Snackbar snackBar =  Snackbar.make(findViewById(R.id.coordinatorLayout1), "Your TA Status will be updated after verification", Snackbar.LENGTH_LONG);

                            snackBar.setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackBar.dismiss();
                                }
                            })
                                    .setActionTextColor(Color.RED)
                                    .show();
                            finish();

                        }
                    }
                });
            }
        });

    }


}
