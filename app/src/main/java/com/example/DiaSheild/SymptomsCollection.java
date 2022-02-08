package com.example.DiaSheild;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class SymptomsCollection extends AppCompatActivity {
    String[] Symptoms = new String[] {"Nausea", "Headache", "Diarea", "SoarThroat", "Fever", "Muscle Ache", "Loss of Smell or Taste", "cough", "Shortness of breath", "Feeling Tired" };
//    Pair[] symptomRatings = new Pair[10];
    HashMap<String, Float> symptomRatings = new HashMap(10);
    private static TextView ActiveSymptom, LastNameView;
    private static String LastName;
    private static Integer HeartRate, RespRate;
    private static ListView symptomsList;
    private static RatingBar ratingBar;
    private static String activeSymptom;
    DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button uploadSymptoms = findViewById(R.id.UploadSymptoms);
        LastNameView = findViewById(R.id.LastNameView);

        LastName = getIntent().getStringExtra("KeyLastName");
        HeartRate = getIntent().getIntExtra("KeyHeartRate",0);
        RespRate = getIntent().getIntExtra("KeyRespRate",0);
        ActiveSymptom = findViewById(R.id.selectedSymtpom);
        activeSymptom = Symptoms[4];
        ActiveSymptom.setText(activeSymptom);
        LastNameView.setText(LastName);

        Toast.makeText(SymptomsCollection.this, LastName, Toast.LENGTH_SHORT).show();
        symptomsList = (ListView) findViewById(R.id.SymptomsList);
        ratingBar = findViewById(R.id.SymptomRatingBar);
        db = new DBHelper(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Symptoms);
        symptomsList.setAdapter(adapter);

        for (int i=0; i<10; i++){
            symptomRatings.put(Symptoms[i], 0.0F);
        }


        symptomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                activeSymptom = Symptoms[pos];
                ActiveSymptom.setText(activeSymptom);
                ratingBar.setRating(symptomRatings.get(activeSymptom));
                symptomsList.setVisibility(View.INVISIBLE);
                ratingBar.setVisibility(View.VISIBLE);
                //Toast.makeText(MainActivity2.this,Symptoms[pos], Toast.LENGTH_SHORT).show();

            }
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                try {
                    symptomRatings.put(activeSymptom, ratingBar.getRating());
                    Thread.sleep(500);
                    //ratingBar.setVisibility(View.INVISIBLE);
                    symptomsList.setVisibility(View.VISIBLE);

                    Toast.makeText(SymptomsCollection.this, activeSymptom+String.valueOf(symptomRatings.get(activeSymptom)), Toast.LENGTH_SHORT).show();
                    //ratingBar.setRating(0.0F);
                }catch(Exception ex){
                    Log.i("Exception", "Exception during Symptoms upload" + ex);
                }

            }
        });

        uploadSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean checkupdatedata = db.updateSymptomsDatatoDB(LastName, HeartRate, RespRate, symptomRatings);
                if(checkupdatedata){
                    Toast.makeText(SymptomsCollection.this, "Symptoms data updated", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SymptomsCollection.this, "Symptoms data not updated", Toast.LENGTH_SHORT).show();
                }
                // clearing data locally
                for (int i=0; i<10; i++){
                    symptomRatings.put(Symptoms[i], 0.0F);
                }
                activeSymptom = Symptoms[4];
                ActiveSymptom.setText(activeSymptom);
                ratingBar.setRating(0.0F);
                symptomsList.setVisibility(View.INVISIBLE);
            }
        });
    }
}