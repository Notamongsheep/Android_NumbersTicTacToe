package com.christineberger.android.numberstictactoe;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

/**
 * TwoPlayerGame
 * This class is responsible for executing a two player Numbers Tic Tac Toe game.
 *
 * Created by: Christine Berger
 * Last Modified: 10/17/2017
 */

public class TwoPlayerGame extends AppCompatActivity {

    /*===== VARIABLE DECLARATIONS =====*/
    //References to the player controls.
    private ImageView bottomPlayerSlots[];
    private ImageView topPlayerSlots[];
    private ImageView boardContainers[][];

    //Score board textview references.
    private TextView p1_score;
    private TextView p2_score;

    //View ids, image ids, and values for player pieces and board.
    private int firstPlayerResources[][];
    private int secondPlayerResources[][];
    private int boardResources[][];

    //Holds the values played on the board.
    private int boardValues[][];

    //For the reusable modal.
    private AlertDialog.Builder modal;

    //Remembers which player's turn it is.
    private int playerTurn = 0;

    //Keeps track of whether a game has ended by totaling the values on the board (if no one wins).
    private int gameEnder = 0;

    //Keeps the scores of each player.
    private int scores[] = new int[] { 0, 0 };

    //Flag for if the game should keep going or not.
    private boolean continueGame = true;

    //Flag for if the dialog after a game has been won or ended (Dialog can be dismissed so players
    //can take a look at the board before choosing another action.
    private boolean endGameDialogDismissed = false;

    //Holds the number of elements for rows and columns (current is a 3x3 board, so the unit is 3).
    private static final int UNITS = 3;

    //Holds the rootView so that it can be used in getting references - other views use the
    //same findViewById function.
    private View rootView;

    /*--------------------------onRestoreInstanceState(Bundle)--------------------------------------
     * When the activity is recreated, it will call this when savedInstanceState has a value that
     * can be retrieved.
     *--------------------------------------------------------------------------------------------*/
    @Override
    protected final void onRestoreInstanceState(final Bundle onRestoreInstanceState)
    {
        //Call the superclass so it can save the view hierarchy.
        super.onRestoreInstanceState(onRestoreInstanceState);

        //Restore the saved variables.
        scores[0] = onRestoreInstanceState.getInt("scores1_key");
        scores[1] = onRestoreInstanceState.getInt("scores2_key");

        //Set the TextView text to the retrieved values.
        p1_score.setText(String.format(Locale.US, "%d", scores[0]));
        p2_score.setText(String.format(Locale.US, "%d", scores[1]));
    }

    /*----------------------------onSavedInstanceState(Bundle)--------------------------------------
     * When the activity is recreated, it will call this when savedInstanceState has a value that
     * can be retrieved.
     *--------------------------------------------------------------------------------------------*/
    @Override
    protected final void onSaveInstanceState(final Bundle savedInstanceState)
    {
        //Save the scores.
        savedInstanceState.putInt("scores1_key", scores[0]);
        savedInstanceState.putInt("scores2_key", scores[1]);

        //Call the superclass so it can save the view hierarchy.
        super.onSaveInstanceState(savedInstanceState);
    }

    /*-------------------------------------onCreate(Bundle)-----------------------------------------
     * When the activity is recreated, it will call this when savedInstanceState has a value that
     * can be retrieved.
     *--------------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player_game);
        rootView = getWindow().getDecorView().getRootView();

        //Set the TextView references.
        p1_score = $txt(rootView, R.id.player_one_score);
        p2_score = $txt(rootView, R.id.player_two_score);

        //Set the text of the score board.
        p1_score.setText(String.format(Locale.US, "%d", scores[0]));
        p2_score.setText(String.format(Locale.US, "%d", scores[1]));

        //Create a new Alert Dialog Builder.
        modal = new AlertDialog.Builder(this);

        //Reference the gameplay navigation buttons on the UI.
        Button home_btn = $btn(rootView, R.id.btn_main_home);
        Button reset_btn = $btn(rootView, R.id.btn_main_reset);
        Button info_btn = $btn(rootView, R.id.btn_main_info);

        //Setup the board view ids and board values.
        setBoardResources();

        //Get a random true/false for the starting player (Like flipping a coin).
        boolean startingPlayer = getRandom();

        //Set the player resources based on the coin flip. This is based on Player 1. If true,
        //Player 1 gets the starting hand. If false, Player 2 gets the starting hand.
        setPlayerResources(startingPlayer);

        //Activate the first player's controls and disable the second player's controls.
        showActiveControls();

        //Set the OnClick events for each navigation control.
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(v);
            }
        });

        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(v);
            }
        });

        info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(v);
            }
        });
    }

    /*==============================================================================================
    =                              FUNCTIONS AND  INNER CLASSES                                    =
    ==============================================================================================*/


    /* =============================================================================================
     * HELPER FUNCTIONS:
     * Function to get the view by id for UI elements
     * ===========================================================================================*/
    private ImageView $img(View view, int resourceId) {
        return (ImageView) view.findViewById(resourceId);
    }

    private Button $btn(View view, int resourceId) { return (Button) view.findViewById(resourceId); }

    private TextView $txt(View view, int resourceId) { return (TextView) view.findViewById(resourceId); }


    /* =============================================================================================
     * openDialog(View):
     * Builds and shows a dialog based on the button that was clicked.
     * ===========================================================================================*/
    private void openDialog(View view) {

        //Inflate the dialog layout.
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog, null);

        //Reference the buttons in the dialog layout's UI.
        Button close_btn = $btn(dialogLayout, R.id.btn_close);
        Button left_btn = $btn(dialogLayout, R.id.btn_left);
        Button right_btn = $btn(dialogLayout, R.id.btn_right);

        ImageView imageContainer = $img(dialogLayout, R.id.image_game_result);

        //Reference the TextViews from the dialog's UI.
        TextView content = $txt(dialogLayout, R.id.text_content);
        TextView titleContainer = $txt(dialogLayout, R.id.text_title);

        //Set the modal's view as the dialog layout.
        modal.setView(dialogLayout);

        //Turn off back arrow functionality.
        modal.setCancelable(false);

        //Create the alert dialog.
        final AlertDialog dialog = modal.create();

        //Set the default color of the modal title container to the main background color.
        titleContainer.setBackgroundResource(R.color.colorPrimary);

        //If this dialog was called because of a winning or ending game and it has not been
        //dismissed:
        if(!continueGame && !endGameDialogDismissed) {
            //Set the text of the buttons.
            left_btn.setText(R.string.play_again);
            right_btn.setText(R.string.main_menu);
            titleContainer.setText(R.string.game_over);

            //Find out who won by getting where the playerTurn stopped and set the modal contents
            // appropriately. PlayerTurn is set to a different number outside of this function if
            // it's determined that no one has won.

            //Set the text (Player wins will show this text, otherwise, it is reset).
            content.setText(R.string.end_info);
            content.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            switch(playerTurn) {
                case 1:
                    titleContainer.setBackgroundResource(R.color.colorAccentRed);
                    imageContainer.setImageResource(R.drawable.p1_win);
                    break;
                case 2:
                    titleContainer.setBackgroundResource(R.color.colorAccentBlue);
                    imageContainer.setImageResource(R.drawable.p2_win);
                    break;
                default:
                    imageContainer.setImageResource(R.drawable.draw);
                    content.setText(R.string.no_winner);
                    break;
            }

            //Set the OnClick listeners for each of the end game/win game buttons.
            //'X' close button.
            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    //If the close button ('x') is clicked, the user has dismissed the dialog.
                    endGameDialogDismissed = true;
                }
            });

            //PLAY AGAIN is selected.
            left_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    reset();
                }
            });

            //MAIN MENU is selected.
            right_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    goHome(dialog);
                }
            });

        //OTHERWISE, set the navigation modals.
        } else {

            imageContainer.setVisibility(View.GONE);

            //The left and right button will always be cancel and yes.
            left_btn.setText(R.string.no);
            right_btn.setText(R.string.yes);

            content.setTextSize(16);

            //Get the id of the button that invoked this function and set the appropriate content.
            switch (view.getId()) {
                case R.id.btn_main_home:
                    titleContainer.setText(R.string.exit);
                    content.setText(R.string.exit_content);
                    right_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            goHome(dialog);
                        }
                    });
                    break;
                case R.id.btn_main_reset:
                    titleContainer.setText(R.string.reset_game);
                    content.setText(R.string.reset_game_content);
                    right_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            reset();
                        }
                    });
                    break;
                case R.id.btn_main_info:
                    titleContainer.setText(R.string.how_to_play);
                    content.setText(R.string.how_to_play_content);
                    left_btn.setVisibility(View.GONE);
                    right_btn.setText(R.string.ok);
                    right_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    break;
            }

            //CANCEL button
            left_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            //CLOSE button
            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        //Show the dialog that was built.
        dialog.show();
    }


    /* =============================================================================================
     * setBoardResources():
     * Sets the ImageView ids for the board and an array that holds the
     * values of the board during game play.
     * ===========================================================================================*/
    private void setBoardResources() {

        //Counter integer (Because the array dimensions are different).
        boardContainers = new ImageView[UNITS][UNITS];

        //An array for the ImageView ids so that the drag listener can compare.
        boardResources = new int[][]{{R.id.drop00, R.id.drop01, R.id.drop02},
                {R.id.drop10, R.id.drop11, R.id.drop12},
                {R.id.drop20, R.id.drop21, R.id.drop22}};

        //Create ImageView references from each id in boardResources (So the DragListener can be
        //applied to each ImageView in the UI.
        for(int row = 0; row < UNITS; row++) {
            for(int col = 0; col < UNITS; col++) {
                boardContainers[row][col] = $img(rootView, boardResources[row][col]);
                boardContainers[row][col].setOnDragListener(new DragListener());
            }
        }

        //Create the boardSpace values that can be set during game play (Default = 0).
        boardValues = new int[][] {{0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}};
    }


    /* =============================================================================================
     * getRandom():
     * Returns a random boolean
     * ===========================================================================================*/
    private boolean getRandom() {
        Random randomize = new Random();
        return randomize.nextBoolean();
    }


    /* =============================================================================================
     * setPlayerResources():
     * Sets the ImageView and Drawable ids as well as the value of each slot (used to determine
     * the outcome of game play). Calls the setControls() function to attach programmatic
     * functionality.
     * ===========================================================================================*/
    private void setPlayerResources(boolean isFirstPlayer) {

        //If player one is first player
        if (isFirstPlayer) {

            //Player one slots get first player pieces.
            firstPlayerResources = new int[][]{{R.id.p1_slot1, R.drawable.one_red, 1},
                    {R.id.p1_slot2, R.drawable.three_red, 3},
                    {R.id.p1_slot3, R.drawable.five_red, 5},
                    {R.id.p1_slot4, R.drawable.seven_red, 7},
                    {R.id.p1_slot5, R.drawable.nine_red, 9}};
            //Player two slots get second player pieces.
            secondPlayerResources = new int[][]{{R.id.p2_slot1, R.drawable.two_blue, 2},
                    {R.id.p2_slot2, R.drawable.four_blue, 4},
                    {R.id.p2_slot3, R.drawable.six_blue, 6},
                    {R.id.p2_slot4, R.drawable.eight_blue, 8},
                    {R.id.p2_slot5, R.drawable.blank_tile, 0}};

            //The first player is player 1.
            playerTurn = 1;

        } else {
            //Player two slots get first player pieces.
            firstPlayerResources = new int[][]{{R.id.p1_slot1, R.drawable.two_red, 2},
                    {R.id.p1_slot2, R.drawable.four_red, 4},
                    {R.id.p1_slot3, R.drawable.six_red, 6},
                    {R.id.p1_slot4, R.drawable.eight_red, 8},
                    {R.id.p1_slot5, R.drawable.blank_tile, 0}};

            //Player one slots get second player pieces.
            secondPlayerResources = new int[][]{{R.id.p2_slot1, R.drawable.one_blue, 1},
                    {R.id.p2_slot2, R.drawable.three_blue, 3},
                    {R.id.p2_slot3, R.drawable.five_blue, 5},
                    {R.id.p2_slot4, R.drawable.seven_blue, 7},
                    {R.id.p2_slot5, R.drawable.nine_blue, 9}};

            //The first player is Player 2.
            playerTurn = 2;
        }

        //Update the UI and create references to each slot.
        bottomPlayerSlots = (setControls(firstPlayerResources)).clone();
        topPlayerSlots = (setControls(secondPlayerResources)).clone();
    }


    /* =============================================================================================
     * setControls(int[][]):
     * This function sets references and attributes to ImageViews of the UI.
     * Each ImageView ID in the player resources must have an ImageView
     * to reference the UI. The playerPieces array holds those ImageViews references.
     * Returns the array.
     * ===========================================================================================*/
    private ImageView[] setControls(int[][] playerResources) {

        //ImageView array to hold the references.
        ImageView[] playerPieces = new ImageView[playerResources.length];

        for (int count = 0; count < playerResources.length; count++) {
            //Reference ImageViews of UI.
            playerPieces[count] = $img(rootView, playerResources[count][0]);
            //Set the images that were assigned in setPlayerResources().
            playerPieces[count].setImageResource(playerResources[count][1]);
            //Set the tag to be the id name for the DragListener.
            playerPieces[count].setTag(playerResources[count][1]);
            //Set the onTouchListener for each ImageView reference.
            playerPieces[count].setOnTouchListener(new TouchListener());
        }

        //Return the array.
        return playerPieces;
    }


    /* =============================================================================================
     * showActiveControls():
     * Sets the disabled and enabled states of the player controls based on which player's turn it
     * is.
     * ===========================================================================================*/
    private void showActiveControls() {

        //If it's Player 1's turn:
        if(playerTurn == 1) {
            //Set Player 1's pieces to full opacity.
            for(ImageView piece:bottomPlayerSlots) {
                piece.setAlpha(1f);
            }
            //Set Player 2's pieces to 35% opacity.
            for(ImageView piece:topPlayerSlots) {
                piece.setAlpha(0.35f);
            }
        } else { //If it's Player 2's turn.
            //Set Player 1's pieces to 35% opacity.
            for(ImageView piece:bottomPlayerSlots) {
                piece.setAlpha(0.35f);
            }
            //Set Player 2's pieces to full opacity.
            for(ImageView piece:topPlayerSlots) {
                piece.setAlpha(1f);
            }
        }
    }


    /* =============================================================================================
     * CLASS TouchListener:
     * Listens for a touch event to happen and starts the drag and drop operation.
     * ===========================================================================================*/
    private final class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View imageView, MotionEvent motionEvent) {

            if(continueGame) {
                //Get the event action.
                switch (motionEvent.getAction()) {
                    //If a pressed gesture is started (ACTION_DOWN contains the initial starting location).
                    case MotionEvent.ACTION_DOWN:
                        //Get the player's turn.
                        switch (playerTurn) {
                            case 1: //If it's Player 1's turn.
                                return doDragOperation(imageView, firstPlayerResources);
                            case 2: //If it's Player 2's turn.
                                return doDragOperation(imageView, secondPlayerResources);
                        }
                }
            }
            return false;
        }
    }


    /* =============================================================================================
     * doDragOperation(View, int[][]):
     * Applies the drag operation to the Touch Listener.
     * ===========================================================================================*/
    private boolean doDragOperation(View imageView, int[][] resourceArray) {

        int viewId = imageView.getId();

        //Only set listeners for Player 1's controls.
        for (int[] slot : resourceArray) {
            //If the view id that invoked the touch listener matches the id in the first player resources.
            if (viewId == slot[0] && slot[2] != 0) {
                //Create a new shadow builder from the ImageView
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(imageView);
                //Start the drag and drop operation, passing the clipData
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageView.startDragAndDrop(null, shadowBuilder, imageView, 0);
                } else {
                    imageView.startDrag(null, shadowBuilder, imageView, 0);
                }
            }
        }

        return true;
    }



    /* =============================================================================================
     * CLASS DragListener:
     * Executes the appropriate actions for drag and drop operations.
     * ===========================================================================================*/
    private class DragListener implements View.OnDragListener {

        //Blank Tile and Blank Tile Hover drawables.
        Drawable blankTile = ContextCompat.getDrawable(getApplicationContext(), R.drawable.blank_tile);
        Drawable blankTileHover = ContextCompat.getDrawable(getApplicationContext(), R.drawable.blank_tile_hover);

        //ImageView objects to reference the dragged object and drop target.
        private ImageView dragView;
        private ImageView dropTarget;

        //Integers to hold the dragged item id an drop target id.
        private int dragId;
        private int dropId;

        @Override
        public boolean onDrag(View dropView, DragEvent event) {

            //Get the drag action
            switch (event.getAction()) {

                //If drag is started
                case DragEvent.ACTION_DRAG_STARTED:

                    //Initialize the views for the target and the currently held item.
                    dropTarget = (ImageView) dropView;
                    dragView = (ImageView) event.getLocalState();

                    //get the dragView (ImageView)'s id.
                    dragId = dragView.getId();

                    //Set the background (at the starting coordinates) of the drag to a blank tile.
                    dragView.setImageDrawable(blankTile);

                    return true;

                //If drop target area is entered with the drag item.
                case DragEvent.ACTION_DRAG_ENTERED:
                    //If the target has not been occupied:
                    if (!dropTarget.isSelected()) {
                        //Set the image to a highlighted version of the blank tile.
                        dropTarget.setImageDrawable(blankTileHover);
                    }
                    break;

                //If the drop target area is exited:
                case DragEvent.ACTION_DRAG_EXITED:
                    //If the target had not been occupied:
                    if (!dropTarget.isSelected()) {
                        //Set the image back to the blank tile image.
                        dropTarget.setImageDrawable(blankTile);
                    }
                    break;

                //If the dragged item is dropped:
                case DragEvent.ACTION_DROP:

                    //Set the dragged view id and drop target view id.
                    dragId = dragView.getId();
                    dropId = dropTarget.getId();

                    //If the drop target is not occupied:
                    if (!dropTarget.isSelected()) {

                        //If it's player 1's turn, set player 1's piece.
                        if(playerTurn == 1) {
                            setBoardPiece(dragId, dropId, dropTarget, firstPlayerResources);
                        //Otherwise, set player 2's piece in the board.
                        } else {
                            setBoardPiece(dragId, dropId, dropTarget, secondPlayerResources);
                        }

                        //Mark the selected drop target as taken.
                        dropTarget.setSelected(true);

                        //If there is a winning row, column, or diagonal:
                        if(isWinner()) {
                            //Do not continue the game (Set flag to false for other functions to use)
                            continueGame = false;

                            //Build a dialog for the winner.
                            openDialog(rootView);

                            //Set the TextViews of the UI to 35% opacity.
                            (findViewById(R.id.txt_player1)).setAlpha(.35f);
                            (findViewById(R.id.txt_player2)).setAlpha(.35f);
                            p1_score.setAlpha(.35f);
                            p2_score.setAlpha(.35f);

                            //Increment the winner's score and update the UI.
                            if(playerTurn == 1) {
                                scores[0]++;
                                p1_score.setText(String.format(Locale.US, "%d", scores[0]));
                            } else {
                                scores[1]++;
                                p2_score.setText(String.format(Locale.US, "%d", scores[1]));
                            }

                            //Set all player pieces to 35% opacity.
                            for(ImageView piece:bottomPlayerSlots) {
                                piece.setAlpha(.35f);
                            }
                            for(ImageView piece:topPlayerSlots) {
                                piece.setAlpha(.35f);
                            }
                        //Otherwise, no one has won. Show a dialog box reflecting this.
                        } else if (!continueGame) {
                            playerTurn = 0;
                            openDialog(rootView);
                        //Otherwise, the game is still going. Switch players.
                        } else {
                            if (playerTurn == 1) {
                                playerTurn = 2;
                            } else {
                                playerTurn = 1;
                            }
                            //Enable the active player's controls.
                            showActiveControls();
                        }
                    //If a drop target is selected, ensure the visual drop does not occur.
                    } else {
                        dragView.setImageDrawable(dragView.getDrawable());
                        if(playerTurn == 1) {
                            resetPiece(dragId, dragView, firstPlayerResources);
                        } else {
                            resetPiece(dragId, dragView, secondPlayerResources);
                        }
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //If a drop operation was unsuccessful, reset the pieces.
                    if(!event.getResult()) {
                        if(playerTurn == 1) {
                            resetPiece(dragId, dragView, firstPlayerResources);
                        } else {
                            resetPiece(dragId, dragView, secondPlayerResources);
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /* =============================================================================================
     * setBoardPiece(int, int, ImageView, int[][]):
     * Sets the board piece based on the dragged image.
     * ===========================================================================================*/
    private void setBoardPiece(int piece, int space, ImageView view, int[][] resources) {

        //For every row in the board values:
        for(int rowCount = 0; rowCount < boardValues.length; rowCount++) {
            //For every value in the row:
            for(int colCount = 0; colCount < boardValues[rowCount].length; colCount++) {
                //If the space (drop target) is the same as the board resource id of that space:
                if(space == boardResources[rowCount][colCount]) {
                    //for piece in the player's controls:
                    for(int slotCount = 0 ; slotCount < resources.length; slotCount++) {
                        //if the piece id is equal to the resources slot id:
                        if(piece == resources[slotCount][0]) {
                            //Set the board value to the value in the player's piece resource.
                            boardValues[rowCount][colCount] = resources[slotCount][2];
                            //Add the value to the game ender total.
                            gameEnder += resources[slotCount][2];
                            //Set the image to the dragged image resource.
                            view.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), resources[slotCount][1]));
                            //Set the dragged image's original slot to 0 value so the player cannot
                            //drag it again.
                            resources[slotCount][2] = 0;
                        }
                    }
                }
            }
        }

        //If the board is filled, do not continue the game (set flag to false for other functions)
        if (gameEnder == 45) {
            continueGame = false;
        }
    }


    /* =============================================================================================
     * resetPiece(int, ImageView, int[][]):
     * Resets the player's piece in it's slot.
     * ===========================================================================================*/
    private void resetPiece(int piece, ImageView view, int[][] resources) {
        //For each slot in the player's controls:
        for (int[] slot : resources) {
            //If the dragged piece is equal to the view id:
            if (piece == slot[0]) {
                //Set the resource id for the original slot.
                view.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), slot[1]));
            }
        }
    }

    /* =============================================================================================
     * isWinner():
     * Looks at the board values to see if there is a winner horizontally, diagonally, or
     * vertically.
     * ===========================================================================================*/
    private boolean isWinner() {

        //Counter to keep track of the row in For Each statements.
        int count = 0;

        //Set all tiles to 35% opacity. for each row in the ui containers:
        for (ImageView[] row : boardContainers) {
            //For each tile in the row:
            for (ImageView tile : row) {
                //Set the tile opacity to 35%.
                tile.setAlpha(.35f);
            }
        }

        //Check all rows for a win. Select the row:
        for (int[] group : boardValues) {
            //If all spaces in the row have a tile :
            if (group[0] != 0 && group[1] != 0 && group[2] != 0) {
                //Get the sum of all values in the row. If they equal 15:
                if (group[0] + group[1] + group[2] == 15) {
                    //Highlight the winning tiles.
                    for (int item = 0; item < UNITS; item++) {
                        boardContainers[count][item].setAlpha(1f);
                    }
                    //Tell the game that there is a winner.
                    return true;
                }
            }
            //Go to next row if no winner has been returned.
            count++;
        }

        //Reset the count to 0. If no winner has been returned, continue checking:
        count = 0;

        //Check all columns for a win. Select the column:
        for (int col = 0; col < boardValues[0].length; col++) {
            //If the all spaces in the column has a tile:
            if (boardValues[0][col] != 0 && boardValues[1][col] != 0 && boardValues[2][col] != 0) {
                //Get the sum of all tiles in the column. If they equal 15:
                if (boardValues[0][col] + boardValues[1][col] + boardValues[2][col] == 15) {
                    //Set the winning tiles to full opacity.
                    for (int item = 0; item < UNITS; item++) {
                        boardContainers[item][count].setAlpha(1f);
                    }
                    //Tell the game that there is a winner.
                    return true;
                }
            }
            //Go to the next column if no winner was returned.
            count++;
        }

        //If no winner has been returned, check the Left to Right diagonal line for a win. If the diagonal line has all tiles:
        if (boardValues[0][0] != 0 && boardValues[1][1] != 0 && boardValues[2][2] != 0) {
            //Get the sum of all tiles in the diagonal line. If they sum 15:
            if (boardValues[0][0] + boardValues[1][1] + boardValues[2][2] == 15) {
                //Set each tile to full opacity.
                for(int item = 0; item < UNITS; item++) {
                    boardContainers[item][item].setAlpha(1f);
                }
                //Tell the game there is a winner.
                return true;
            }
        }

        //If there was no winner returned, check the Right to Left diagonal line for a win. If the diagonal line has all tiles:
        if(boardValues[0][2] != 0 && boardValues[1][1] != 0 && boardValues[2][0] != 0) {
            //Get the sum of all tiles in the diagonal line. If they sum 15:
            if(boardValues[0][2] + boardValues[1][1] + boardValues[2][0] == 15) {
                //Set each tile to full opacity.
                boardContainers[0][2].setAlpha(1f);
                boardContainers[1][1].setAlpha(1f);
                boardContainers[2][0].setAlpha(1f);

                //Tell the game there is a winner.
                return true;
            }
        }

        //Set the tiles to full opacity because there were no winners found.
        for (ImageView[] row : boardContainers) {
            //For each tile in the row:
            for (ImageView tile : row) {
                //Set the tile opacity to 35%.
                tile.setAlpha(1f);
            }
        }

        //If no winner was found in any of the algorithms above, then return false to let the game know
        //there were no winners.
        return false;
    }

    /* =============================================================================================
     * goHome():
     * Dismisses the dialog and finishes the activity.
     * ===========================================================================================*/
    public void goHome(AlertDialog dialog) {
        dialog.dismiss();
        finish();
    }

    /* =============================================================================================
     * reset():
     * Recreates the activity (clears all values except the scores).
     * ===========================================================================================*/
    public void reset() {
        recreate();
    }
}


