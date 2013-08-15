package com.meadowhawk.cah;

import java.io.IOException;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.SessionError;

public class GameActivity extends Activity {
	private static final String TAG = GameActivity.class.getSimpleName();
	
	private ApplicationSession mSession;
    private SessionListener mSessionListener;
	private CAHMessageStream cahMessageStream;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		cahMessageStream = new CAHMessageStream();
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
        CastDevice selectedDevice = CastCAHApplication.getInstance().getDevice();
        CastContext castContext = CastCAHApplication.getInstance().getCastContext();

        mSession = new ApplicationSession(castContext, selectedDevice);
        mSession.setListener(mSessionListener);
        try {
            mSession.startSession("TicTacToe");
        } catch (IOException e) {
            Log.e(TAG, "Failed to open a session", e);
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
            cahMessageStream.join("MyName");
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
		protected void onGotCards(JSONArray cards) {
			// TODO Auto-generated method stub
			
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
