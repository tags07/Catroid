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

package org.catrobat.catroid.test.utiltests;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.AskBrick;
import org.catrobat.catroid.stage.StageActivity;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.test.ActivityUnitTestCase;

public class SpeechRecognitionTest extends ActivityUnitTestCase<StageActivity> {

	private static final String testQuestion = "Somebody out there?";

	public SpeechRecognitionTest() {
		super(StageActivity.class);
	}

	public void testRecognitionIntent() {

		Sprite sprite = new Sprite("askingSprite");
		Script script = new StartScript(sprite);
		AskBrick brick = new AskBrick(sprite, testQuestion);
		script.addBrick(brick);
		sprite.addScript(script);

		Project project = new Project(this.getInstrumentation().getTargetContext(), "testProject");
		project.addSprite(sprite);
		ProjectManager.getInstance().setProject(project);

		Intent startIntent = new Intent(getInstrumentation().getTargetContext(), StageActivity.class);
		startActivity(startIntent, null, null);

		sprite.createStartScriptActionSequence();
		sprite.look.act(100f);

		Intent firedIntent = this.getStartedActivityIntent();
		assertNotNull("No intent was fired when running askProject.", firedIntent);
		assertEquals("Wrong intent was fired. requested action:", RecognizerIntent.ACTION_RECOGNIZE_SPEECH,
				firedIntent.getAction());

		String intentQuestion = firedIntent.getStringExtra(RecognizerIntent.EXTRA_PROMPT);
		assertNotNull("Intent has no prompt set.", intentQuestion);
		assertEquals("Question wasn't propper set in the intent.", testQuestion, intentQuestion);
	}
}
