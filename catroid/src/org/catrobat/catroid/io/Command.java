package org.catrobat.catroid.io;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Command implements Serializable{
	public static enum commandType{SINGLE_KEY, KEY_COMBINATION, MOUSE};
	private int key;
	private commandType type;
	private int[] key_comb;
	
	Command(int command_key, commandType type_){
		key = command_key;
		type = type_;
	}
	
	Command(int[] command_key_comb, commandType type_){
		key_comb = command_key_comb;
		type = type_;
	}
	
	public int getKey(){
		return key;
	}
	
	public int[] getKeyComb(){
		return key_comb;
	}
	
	public commandType getCommandType(){
		return type;
	}
}
