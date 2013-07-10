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
package org.catrobat.catroid.content.actions;

import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.utils.UtilSpeechRecognition;

import android.util.Log;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

public class AskAction extends TemporalAction {

	private static final String TAG = AskAction.class.getSimpleName();
	private Sprite sprite;
	private String question;
	private String answer;

	@Override
	protected void update(float percent) {
		UtilSpeechRecognition.getInstance().recognise(this);
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQuestion() {
		return question;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
		Log.v(TAG, "Got best answer: " + this.answer);
	}
}
