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
package org.catrobat.catroid.test.content.actions;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.AskBrick;
import org.catrobat.catroid.content.bricks.SetXBrick;
import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.test.utils.SimulatedSpeechRecognition;
import org.catrobat.catroid.utils.UtilSpeechRecognition;

import android.test.AndroidTestCase;

public class AskActionTest extends AndroidTestCase {

	public void testWaitingAsk() {
		Sprite sprite = new Sprite("testSprite");
		Script script = new StartScript(sprite);
		AskBrick testAskBrick = new AskBrick();
		script.addBrick(testAskBrick);

		int startPosition = 0;
		int testPosition = 100;
		SetXBrick testPositionBrick = new SetXBrick(sprite, testPosition);
		script.addBrick(testPositionBrick);

		sprite.addScript(script);

		Project project = new Project(getContext(), "testProject");
		project.addSprite(sprite);
		ProjectManager.getInstance().setProject(project);

		SimulatedSpeechRecognition speechRecognition = new SimulatedSpeechRecognition();
		Reflection.setPrivateField(UtilSpeechRecognition.getInstance(), "instance", speechRecognition);

		sprite.createStartScriptActionSequence();

		int i = 0;
		while (!speechRecognition.isRecognitionRequested()) {
			sprite.look.act(1.0f);
			if (i++ > 50) {
				fail("AskAction didn't request any speechrecognition.");
			}
		}

		i = 0;
		while (!sprite.look.getAllActionsAreFinished() && i < 50) {
			sprite.look.act(1.0f);
			i++;
		}

		assertEquals("AskAction did not wait for recognition! AllActionsAreFinished ", false,
				sprite.look.getAllActionsAreFinished());
		assertEquals("AskAction hasn't finished, but other actions where executed. Spriteposition ", startPosition,
				(int) sprite.look.getXInUserInterfaceDimensionUnit());

		speechRecognition.triggerReturnResults();

		i = 0;
		while (!sprite.look.getAllActionsAreFinished()) {
			sprite.look.act(1.0f);
			if (i++ > 50) {
				fail("AskAction failed to resume after getting results.");
			}
		}

		assertEquals("Simple AskAction wait failed", testPosition, (int) sprite.look.getXInUserInterfaceDimensionUnit());
	}
}
