/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.meadowhawk.cah;

import com.google.cast.MessageStream;
import com.meadowhawk.cah.model.Card;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * An abstract class which encapsulates control and game logic for sending and receiving messages 
 * during a TicTacToe game.
 */
public abstract class GameMessageStream extends MessageStream {
    private static final String PLAYER_NAME = "playerName";

	private static final String TAG = GameMessageStream.class.getSimpleName();

    private static final String GAME_NAMESPACE = "com.meadowhawk.chromecast.cah";

    public static final String END_STATE_X_WON = "X-won";
    public static final String END_STATE_O_WON = "O-won";
    public static final String END_STATE_DRAW = "draw";
    public static final String END_STATE_ABANDONED = "abandoned";

    public static final String PLAYER_X = "X";
    public static final String PLAYER_O = "O";

    
    /**
     * List of expected messages that the remote will accept.
     *
     */
    public enum SEND_MESSAGES {
    	JOIN("join"),DROPOUT("leave"),CARD_REQUEST("req-card"),PLAY_CARDS("play-card");
    	
    	private String jsValue;

		SEND_MESSAGES(String value){
    		this.jsValue = value;
    	}
		
		public String getJSValue(){
			return this.jsValue;
		}
    }
    
    // Receivable event types
    public enum RECIEVE_MESSAGES {
    	JOINED, PLAYER_DROP, ERROR, GAME_STATUS_UPDATE, CARD_PLAYED, GOT_CARDS;

		@SuppressLint("DefaultLocale")
		public static RECIEVE_MESSAGES getByString(String msg) {
			for (RECIEVE_MESSAGES recvMesg : RECIEVE_MESSAGES.values()) {
				if(recvMesg.name().equalsIgnoreCase(msg.toUpperCase())){
					return recvMesg;
				}
			}
			return ERROR;
		} 
    }

    public enum STATUS_UPDATE{
    	END_GAME, NEXT_ROUND_START, GOT_AWESOME,NONE;

		public static STATUS_UPDATE getByString(String status) {
			for (STATUS_UPDATE statUpdate : STATUS_UPDATE.values()) {
				if(statUpdate.name().equalsIgnoreCase(status)){
					return statUpdate;
				}
			}
			return NONE;
		}
    }
    
//    private static final String KEY_BOARD_LAYOUT_RESPONSE = "board_layout_response";
    private static final String KEY_EVENT = "event";

    // Commands
    private static final String KEY_COMMAND = "command";

    private static final String KEY_MESSAGE = "message";
    private static final String KEY_NAME = "name";
    private static final String KEY_CARDS = "cards";
    private static final String KEY_PLAYER = PLAYER_NAME;

	private static final String STATUS_TYPE = "status_type";

	private static final String KEY_CARD_COUNT = "cardsInHand";

    /**
     * Constructs a new GameMessageStream with GAME_NAMESPACE as the namespace used by 
     * the superclass.
     */
    protected GameMessageStream() {
        super(GAME_NAMESPACE);
    }

    /**
     * Performs some action upon a player joining the game.
     * 
     * @param playerName
     */
    protected abstract void onGameJoined(String playerName);

    /**
     * Adds new cards to players Hand.
     * @param imCzar 
     * @param list of cards.
     * @param current black card in play
     */
    protected abstract void onGotCards(boolean imCzar, JSONArray cards, JSONObject jsonObject);

    /**
     * Performs some action upon a game error.
     * 
     * @param errorMessage the string description of the error
     */
    protected abstract void onGameError(String errorMessage);

    /**
     * 
     * @param newStatus
     */
    protected abstract void onGameStatusUpdate(STATUS_UPDATE newStatus) ;

    protected abstract void onCardsPlayed(String[] strings) ;
    
    
    /**
     * 
     * @param player
     */
    protected abstract void onPlayerDrop(String player);
    
    /**
     * Attempts to connect to an existing session of the game by sending a join command.
     * 
     * @param name the name of the player that is joining
     */
    public final void join(String name) {
        try {
        	//TODO: update to send proper message
            Log.d(TAG, "join: " + name);
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, SEND_MESSAGES.JOIN.getJSValue());
            payload.put(KEY_NAME, name);
            sendMessage(payload);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to join a game", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send a join message", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Message Stream is not attached", e);
        }
    }

	public final void submitCards(List<Long> cardIds,String playerName){
		//TODO: update to send proper message
		try {
        	//TODO: update to send proper message
            Log.d(TAG, "submitCards: " + cardIds);
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, SEND_MESSAGES.PLAY_CARDS.getJSValue());
            payload.put(KEY_CARDS, cardIds);
            payload.put(PLAYER_NAME,playerName);
            sendMessage(payload);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to submit cards", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send submitcard message", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Message Stream is not attached", e);
        }
	}
	
	/**
	 * Bring hand up to max card count, Find out who Card Czar is and GET Black card for reference.
	 */
	public final void playNextHand(int cardsInHand){
		try {
        	//TODO: update to send proper message
            Log.d(TAG, "requestCards");
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, SEND_MESSAGES.CARD_REQUEST.getJSValue());
            payload.put(KEY_CARD_COUNT,cardsInHand);
            sendMessage(payload);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to request cards", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send a card request", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Message Stream is not attached", e);
        }
	}

    /**
     * Sends a command to leave the current game.
     */
    public final void leave(String name) {
        try {
            Log.d(TAG, "leave");
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, SEND_MESSAGES.DROPOUT.getJSValue());
            payload.put(KEY_NAME, name);
            sendMessage(payload);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to leave a game", e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send a leave message", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Message Stream is not attached", e);
        }
    }

    /**
     * Processes all JSON messages received from the receiver device and performs the appropriate 
     * action for the message. Recognizable messages are of the form:
     * 
     * <p>No other messages are recognized.
     */
    @Override
    public void onMessageReceived(JSONObject message) {
        try {
            Log.d(TAG, "onMessageReceived: " + message);
            if (message.has(KEY_EVENT)) {
                RECIEVE_MESSAGES msgRecieved = RECIEVE_MESSAGES.getByString(message.getString(KEY_EVENT));
            
				switch (msgRecieved) {
				case JOINED:
					Log.d(TAG, RECIEVE_MESSAGES.JOINED.toString());
                    try {
                        String player = message.getString(KEY_PLAYER);
                        onGameJoined(player);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
					break;
					
				case PLAYER_DROP:
					Log.d(TAG, RECIEVE_MESSAGES.PLAYER_DROP.toString());
                    try {
                        String player = message.getString(KEY_PLAYER);
                        //notify if a play drops the game.
                        onPlayerDrop(player);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
				case GAME_STATUS_UPDATE:   
					Log.d(TAG, RECIEVE_MESSAGES.GAME_STATUS_UPDATE.toString());
                    try {
                        String status = message.getString(STATUS_TYPE); //
                        STATUS_UPDATE newStatus = STATUS_UPDATE.getByString(status);
                        onGameStatusUpdate(newStatus);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                    
				case CARD_PLAYED:
					Log.d(TAG, RECIEVE_MESSAGES.CARD_PLAYED.toString());
					//Looking for an ACK so can clean out cards played from the hand. Should contain the ids of cards played to they can be removed.
					try{
						String cardIds = (message.has("cards"))?message.getString("cards"):null;
						//TODO: Strip off the [ ] s
						cardIds = cardIds.replace("[", "").replace("]", "");
						
						onCardsPlayed(cardIds.split(","));
					}catch (JSONException e) {
                        e.printStackTrace();
                    }
					break;
					
				case GOT_CARDS:
					Log.d(TAG, RECIEVE_MESSAGES.GOT_CARDS.toString());
					try{
					    JSONArray cards = message.getJSONArray("newCards");
					    boolean imCzar = message.getBoolean("imCzar");
					    JSONObject blkCardObj = (message.has("blackCardInPlay"))?message.getJSONObject("blackCardInPlay"):null;
						onGotCards(imCzar, cards, blkCardObj);
					}catch (JSONException e) {
                        e.printStackTrace();
                    }
					break;
					
				case ERROR:  
					
				default:
					Log.d(TAG, RECIEVE_MESSAGES.ERROR.toString());
                    try {
                        String errorMessage = message.getString(KEY_MESSAGE);
                        onGameError(errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i(TAG, "Recieved message: "+message.toString());
                    }
					break;
				}
     
            } else {
                Log.w(TAG, "Unknown message: " + message);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Message doesn't contain an expected key.", e);
        }
    }

}
