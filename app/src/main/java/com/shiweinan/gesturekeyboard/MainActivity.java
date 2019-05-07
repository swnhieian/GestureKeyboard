package com.shiweinan.gesturekeyboard;

import android.content.Intent;

import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Constraints;

public class MainActivity extends AppCompatActivity {

    Preference preference;
    LinearLayout candidateView;
    InputView inputView;
    Button button;
    String word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preference = new Preference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        candidateView = (LinearLayout)findViewById(R.id.candidates);
        inputView = (InputView)findViewById(R.id.inputView);
        inputView.setMainView(this);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View v) {
                Random random = new Random();
                word = inputView.recognizer.ChangeWord(random.nextInt(500));
                showWord(word);
            }
        });
    }

    public void showCandidates(List<String> candidates) {
        candidateView.removeAllViews();
        for (int i=0; i<candidates.size(); i++) {
            TextView textview = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            layoutParams.setLayoutDirection(Constraints.LayoutParams.HORIZONTAL);
            textview.setText(candidates.get(i));
            textview.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textview.setTextSize(20);
            candidateView.addView(textview, layoutParams);
        }
    }

    public void showWord(String word) {
        candidateView.removeAllViews();
        TextView textview = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        layoutParams.setLayoutDirection(Constraints.LayoutParams.HORIZONTAL);
        textview.setText(word);
        textview.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textview.setTextSize(20);
        candidateView.addView(textview, layoutParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                break;
            default:
                 break;
        }
        return true;
    }
}
