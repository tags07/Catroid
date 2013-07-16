/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.utils;

import java.util.ArrayList;

import org.catrobat.catroid.content.actions.AskAction;
import org.catrobat.catroid.stage.StageActivity;

public class UtilSpeechRecognition {

	private static UtilSpeechRecognition instance = null;
	private StageActivity activeStage = null;
	private String lastAnswer = "";
	private String lastBestAnswer = "";
	protected ArrayList<AskAction> askerList = new ArrayList<AskAction>();

	protected UtilSpeechRecognition() {

	}

	public static UtilSpeechRecognition getInstance() {
		if (instance == null) {
			instance = new UtilSpeechRecognition();
		}
		return instance;
	}

	public void onRecognitionResult(ArrayList<String> matches) {
		lastBestAnswer = "";
		lastAnswer = "";
		AskAction initiator = askerList.get(0);
		askerList.remove(initiator);
		if (matches == null) {
			initiator.onRecognizeResult();
			return;
		}

		lastBestAnswer = matches.get(0);
		for (String answer : matches) {
			lastAnswer += " " + answer;
		}
		initiator.onRecognizeResult();
	}

	public void setStage(StageActivity stage) {
		lastBestAnswer = "";
		lastAnswer = "";
		this.activeStage = stage;
	}

	public synchronized void recognise(AskAction asker) {
		askerList.add(asker);
		activeStage.askForSpeechInput(asker.getQuestion());
		return;
	}

	public String getLastAnswer() {
		return instance.lastAnswer;
	}

	public String getLastBestAnswer() {
		return instance.lastBestAnswer;
	}
}
