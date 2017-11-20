package com.christineberger.android.numberstictactoe;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/* HomeActivity
 * This is the main activity (Main Menu) of the Numbers Tic Tac Toe game.
 *
 * Created by: Christine Berger
 * Last Modified: 10/17/2017
 */
public class HomeActivity extends AppCompatActivity {

    //UI Button reference variables
    Button newGameBtn;
    Button quitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Reference the UI button elements.
        newGameBtn = (Button) findViewById(R.id.bv_new_game);
        quitBtn = (Button) findViewById(R.id.bv_quit);

        //OnClick listener for "New Game" button
        newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start the TwoPlayerGame activity.
                Intent intent = new Intent(getApplicationContext(), TwoPlayerGame.class);
                startActivity(intent);
            }
        });

        //OnClick listener for "Quit" button.
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                System.exit(0);
            }
        });
    }
}
