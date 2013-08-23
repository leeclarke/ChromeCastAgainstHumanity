package com.meadowhawk.cah.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Card object for JSON data returned from service.
 * @author leeclarke
 */
public class Card {
	public enum CardType {
		BLACK, WHITE;

		public static CardType getByString(String value) {
			if(BLACK.toString().equalsIgnoreCase(value) || "B".equalsIgnoreCase(value)){
				return BLACK;
			}
			return WHITE;
		}
	}
	
	private String content = "";
	private CardType type;
	private int  pickCt = 1;
	private int draw = 1;
	private int submitOrder = 1;
	private long id = -1;
	private boolean submitCard = false;

	public Card() {
		
	}
	
	public Card(JSONObject json) throws JSONException{
		this.id = json.getLong("id");
		this.content = json.getString("content");
		this.type = CardType.getByString(json.getString("type"));
		if(json.has("pickCt"))
			this.pickCt = json.getInt("pickCt");
		if(json.has("draw"))
			this.draw = json.getInt("draw");
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CardType getType() {
		return type;
	}

	public void setType(CardType type) {
		this.type = type;
	}

	public int getPickCt() {
		return pickCt;
	}

	public void setPickCt(int pickCt) {
		this.pickCt = pickCt;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}
	
	public int getSubmitOrder() {
		return submitOrder;
	}

	public void setSubmitOrder(int submitOrder) {
		this.submitOrder = submitOrder;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isSubmit() {
		return this.submitCard;
	}
	
	public void setSubmit(boolean submit){
		this.submitCard = submit;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Card){
			if(((Card)o).getId() == this.id){
				return true;
			}
		}
		return false;
	}
}

