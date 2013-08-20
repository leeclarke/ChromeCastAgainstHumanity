package com.meadowhawk.cah;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.SessionError;
import com.meadowhawk.cah.model.Card;
import com.meadowhawk.cah.ui.OnSwipeTouchListener;

public class GameActivity extends Activity {
	private static final String API_KEY = "a5db9048-8024-476f-9b3d-73fad64f328c";

	private enum MOVE_CARD {
		BACK(-1),NEXT(1),START(0);
		
		private int value = 0;
		MOVE_CARD(int value){
			this.value =value;
		}
		
		public int getValue(){
			return this.value;
		}
	}
	private static final String CAH_PLAYER_NAME = "CAH_PLAYER_NAME";
	private static final String CAH_APP_PREFERENCES = "CAH_APP_PREFERENCES";
	private static final String TAG = GameActivity.class.getSimpleName();
	private static final String PLAYER1 = "player1";
	
	private ApplicationSession mSession;
    private SessionListener mSessionListener;
	private CAHMessageStream cahMessageStream;
	
	String playerName = PLAYER1;
	int awesomePoints = 0;
	List<Card> cards = new ArrayList<Card>();
	List<Long> submitCards = new ArrayList<Long>();
	protected int currentCard = 0;
	
	private TextView cardView;
	private TextView awesomePointsView;
	private ImageView selectedCardImg;
	private LinearLayout cardLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		mSessionListener = new SessionListener();
		cahMessageStream = new CAHMessageStream();
		playerName = getSharedPreferences(CAH_APP_PREFERENCES,MODE_MULTI_PROCESS).getString(CAH_PLAYER_NAME, PLAYER1);
		setupButtons();
		this.cardView = ((TextView)findViewById(R.id.cardView));
		awesomePointsView = ((TextView)findViewById(R.id.awesomePoints));
		
		this.cardLayout = (LinearLayout) findViewById(R.id.cardLayout);
		setUpCardLayout();
		
		this.selectedCardImg = (ImageView) findViewById(R.id.imageStar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		checkPlayerName();
        
		//Consider moving this to the OnJoin
		makeShortToast("Welcome to the game, ");
		
		awesomePointsView.setText(awesomePoints+"");
		
		//TODO: The GameActvity fails to render the view.  What happened?  This happens even if the below code is commented.
        CastDevice selectedDevice = CastCAHApplication.getInstance().getDevice();
        CastContext castContext = CastCAHApplication.getInstance().getCastContext();

        mSession = new ApplicationSession(castContext, selectedDevice);
        mSession.setListener(mSessionListener); 
        try {
            mSession.startSession(API_KEY);
        } catch (IOException e) {
            Log.e(TAG, "Failed to open a session", e);
        }
		
        
	}
	
    /**
     * Removes the activity from memory when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    /**
     * Attempts to end the current game session when the activity stops.
     */
    @Override
    protected void onStop() {
        if (mSession != null) {
            if (mSession.hasChannel()) {
            	cahMessageStream.leave(playerName);
            }
            try {
                if (mSession.hasStarted()) {
                    mSession.endSession();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to end the session.", e);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Unable to end session.", e);
            }
        }
        mSession = null;
        super.onStop();
    }
    
	/**
	 * Add Gesture listener to the cards.
	 */
	private void setUpCardLayout() {
		
		this.cardLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
		    public void onSwipeTop() {
		    	//no op
		    }
		    public void onSwipeRight() {
		        Log.i(TAG, "Swipe Right");
		        updateCurrentCard(MOVE_CARD.BACK);
		    }
		    public void onSwipeLeft() {
		    	Log.i(TAG, "Swipe Left");
		    	updateCurrentCard(MOVE_CARD.NEXT);
		    }
		    public void onSwipeBottom() {
		    	//no op
		    }
		    public void onTap() {  
		    	Log.i(TAG, "SINGLE TAP");
			   if(!cards.isEmpty()){
		    		Card card = cards.get(currentCard);
		    		card.setSubmit(!card.isSubmit());
		    		selectedCardImg.setVisibility((card.isSubmit())?View.VISIBLE:View.INVISIBLE);
		    	}
		    }
		});
	}
    
	/**
	 * For testing, can be removed.
	 */
	private void loadTestData() {
		// TODO remove - this is to simulate an onGotCartds Response call
		//Test cards
		try {
			JSONArray cardArray = new JSONArray("[{\"content\": \"Darth Vader.\", \"type\": \"W\", \"cardId\":1}," +
					"{\"content\": \"Women.\",\"type\": \"W\", \"cardId\":2}," +
					"{\"content\": \"World of Warcraft.\",\"type\": \"W\", \"cardId\":3}]");
			cahMessageStream.onGotCards(cardArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Do what needs doing to update the CardView
	 */
	private void updateCardView(){
		
		//Update card with currentCard Data.
		if(!cards.isEmpty()){
			Card card = cards.get(currentCard);
			this.cardView.setText(card.getContent());
			selectedCardImg.setVisibility((card.isSubmit())?View.VISIBLE:View.INVISIBLE);
		} else {
			makeShortToast("You don't have any cards yet.");
		}
		//TODO Check to see if CardCzar and switch to that card
		///ref  mTextView.setBackgroundResource(R.drawable.myResouce);
	}
	
	/**
	 * Notify the user that they got more cards.
	 */
	private void makeShortToast(String message){
		Toast userNameToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		userNameToast.show();
	}
	
	/**
	 * Toggle the currently displayed card.
	 * @param moveAmount -  direction to move and by how much. -1 is back 1 is next.
	 */
	private void updateCurrentCard(MOVE_CARD move) {
		int newCurrent = currentCard + move.getValue();
		if(newCurrent > (cards.size()-1)){
			newCurrent = 0;
		}
		else if(newCurrent <0){
			newCurrent += (cards.size()-1);
		}
		else{
			newCurrent = currentCard + move.getValue();
		}
		currentCard = newCurrent; 
		updateCardView();
	}
	
	 /** 
     * Performs onClick setup on game buttons. 
     */
    private void setupButtons() {
        findViewById(R.id.getCards).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	loadTestData();
            	updateCardView(); //TODO: remove testing call
            	cahMessageStream.playNextHand(cards.size());
            	makeShortToast("Recieved  "+ (cards.size() - currentCard)+ " new cards.");
            }
        });

        findViewById(R.id.submit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	//loop cards for cards with submitOrder.
            	
            	//TODO: compare submit cards to card.getPickCt()
            	for (Card card : cards) {
					if(card.isSubmit()){
						submitCards.add(card.getId());
						//TODO need to remove the card from the list. but need to decide what to do with it while waiting for ACK.
					}
				}
            	
            	if(submitCards.size()<1){
            		makeShortToast("You need to tap a card to mark it to be played.");
            	}
				cahMessageStream.submitCards(submitCards);
				//Clear cards for now. w/o waiting on an ACK
				
				submitCards.clear();
            }
        });
        
        findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	updateCurrentCard(MOVE_CARD.BACK);
            }
        });
        
        findViewById(R.id.next).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	updateCurrentCard(MOVE_CARD.NEXT);
            }
        });
    }
    
	/**
	 * Check to see if player has set their name.
	 */
	private void checkPlayerName() {
		if(PLAYER1.equalsIgnoreCase(this.playerName)){
			//prompt for Player Name.
			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.name_prompt, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			
			// set prompts.xml to alert dialog builder
			alertDialogBuilder.setView(promptsView);

			final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

			// set dialog message
			alertDialogBuilder.setCancelable(false)
				.setPositiveButton("OK",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
				    	playerName = userInput.getText().toString();
				    	
				    	SharedPreferences.Editor prefEditor = getSharedPreferences(CAH_APP_PREFERENCES,MODE_MULTI_PROCESS).edit();  
				    	prefEditor.putString(CAH_PLAYER_NAME, playerName);  
				    	prefEditor.commit();  
				    }
				  })
				.setNegativeButton("Cancel",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
				    	dialog.cancel();
				    }
				  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
		
	}

	/**
     * A class which listens to session start events. On detection, it attaches the game's message
     * stream and joins a player to the game.
     */
    private class SessionListener implements ApplicationSession.Listener {
        @Override
        public void onSessionStarted(ApplicationMetadata appMetadata) {
            //TODO: mInfoView.setText(R.string.waiting_for_player_assignment);
            ApplicationChannel channel = mSession.getChannel();
            if (channel == null) {
                Log.w(TAG, "onStarted: channel is null");
                return;
            }
            channel.attachMessageStream(cahMessageStream);
            cahMessageStream.join(playerName);
        }

        @Override
        public void onSessionStartFailed(SessionError error) {
            Log.d(TAG, "start session failed: " + error.toString());
        }

        @Override
        public void onSessionEnded(SessionError error) {
            Log.d(TAG, "session ended: " + ((error == null) ? "OK" : error.toString()));
        }
    }
    
	/**
	 * Manages Messages sent/received from server.
	 */
	private class CAHMessageStream extends GameMessageStream{

		@Override
		protected void onGameJoined(String playerSymbol, String opponentName) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onGotCards(JSONArray jsoncards) {
			try{
				for (int i = 0; i < jsoncards.length(); i++) {
					JSONObject cardJson = (JSONObject) jsoncards.get(i);
					cards.add(new Card(cardJson));
				}
				updateCardView();
			} catch(JSONException je){
				Log.w(TAG, "problem parsing server response for cards: "+je.getMessage());
			}
		}

		@Override
		protected void onGameError(String errorMessage) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onGameStatusUpdate(STATUS_UPDATE newStatus) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onPlayerDrop(String player) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
