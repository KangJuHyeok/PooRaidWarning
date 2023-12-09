package com.example.pooraidwarning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class conditionActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_MENU = 101;
    final Boolean[] man = {true};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition);
        RadioGroup radioGroup=findViewById(R.id.radioGroup);
        TextView textViewUrinals=findViewById(R.id.textViewUrinals);
        CheckBox checkboxDisabledToilet=findViewById(R.id.checkboxDisabledToilet);
        CheckBox checkboxChildToilet=findViewById(R.id.checkboxChildToilet);
        EditText editTextToilets=findViewById(R.id.editTextToilets);
        EditText editTextOpenHours=findViewById(R.id.editTextOpenHours);
        EditText editTextUrinals=findViewById(R.id.editTextUrinals);
        Button startButton = findViewById(R.id.searchButton);
        radioGroup.check(R.id.radioButtonMen);//디폴트로 남자가 체크
        RatingBar ratingBar=findViewById(R.id.ratingBar);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextToilets.getText().toString().length()<1 || editTextOpenHours.getText().toString().length()<1){
                    Toast.makeText(conditionActivity.this, "조건을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    int recentStar=(int)Math.round(ratingBar.getRating());

                    Intent intent = new Intent(getApplicationContext(), mapActivity.class);
                    intent.putExtra("stars",recentStar);
                    intent.putExtra("man",man[0].toString());
                    intent.putExtra("toilets",editTextToilets.getText().toString());
                    intent.putExtra("openHours",editTextOpenHours.getText().toString());
                    intent.putExtra("disabledToilet",checkboxDisabledToilet.isChecked());
                    intent.putExtra("childToilet",checkboxChildToilet.isChecked());
                    if(man[0]==true && editTextUrinals.getText().toString().length()>0){
                        intent.putExtra("urinals",editTextUrinals.getText().toString());
                    }
                    startActivity(intent);
                }
                // 버튼 클릭 시 mapActivity로 이동

            }
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId==R.id.radioButtonWomen){
                textViewUrinals.setVisibility(View.GONE);
                editTextUrinals.setVisibility(View.GONE);
                man[0]=false;
            }
            else{
                textViewUrinals.setVisibility(View.VISIBLE);
                editTextUrinals.setVisibility(View.VISIBLE);
                man[0]=true;
            }
        });
    }
}
