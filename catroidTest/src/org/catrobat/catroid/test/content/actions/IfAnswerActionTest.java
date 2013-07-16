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

import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.AskBrick;
import org.catrobat.catroid.content.bricks.IfAnswerLogicBeginBrick;
import org.catrobat.catroid.content.bricks.IfLogicElseBrick;
import org.catrobat.catroid.content.bricks.IfLogicEndBrick;
import org.catrobat.catroid.content.bricks.SetVariableBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.test.utils.Reflection;
import org.catrobat.catroid.test.utils.SimulatedSpeechRecognition;
import org.catrobat.catroid.utils.UtilSpeechRecognition;

import android.test.AndroidTestCase;

public class IfAnswerActionTest extends AndroidTestCase {

	private static final int IF_TRUE_VALUE = 42;
	private static final int IF_FALSE_VALUE = 32;
	private static final String TEST_USERVARIABLE = "testUservariable";
	private static final String TEST_ANSWER_TRUE = "run";
	private IfAnswerLogicBeginBrick ifAnswerLogicBeginBrick;
	private IfLogicElseBrick ifLogicElseBrick;
	private IfLogicEndBrick ifLogicEndBrick;

	public void testIfBrick() throws InterruptedException {

		Project project = new Project(getContext(), "testProject");
		Sprite sprite = new Sprite("testSprite");
		Script script = new StartScript(sprite);

		SimulatedSpeechRecognition speechRecognition = new SimulatedSpeechRecognition();
		Reflection.setPrivateField(UtilSpeechRecognition.getInstance(), "instance", speechRecognition);

		AskBrick testAskBrick = new AskBrick();

		ifAnswerLogicBeginBrick = new IfAnswerLogicBeginBrick(sprite, TEST_ANSWER_TRUE);
		ifLogicElseBrick = new IfLogicElseBrick(sprite, ifAnswerLogicBeginBrick);
		ifLogicEndBrick = new IfLogicEndBrick(sprite, ifLogicElseBrick, ifAnswerLogicBeginBrick);

		project.getUserVariables().deleteUserVariableByName(TEST_USERVARIABLE);
		project.getUserVariables().addProjectUserVariable(TEST_USERVARIABLE);
		UserVariable userVariable = project.getUserVariables().getUserVariable(TEST_USERVARIABLE, null);
		userVariable.setValue(IF_FALSE_VALUE);

		SetVariableBrick setVariableBrick = new SetVariableBrick(sprite, new Formula(IF_TRUE_VALUE), userVariable);

		script.addBrick(testAskBrick);
		script.addBrick(ifAnswerLogicBeginBrick);
		script.addBrick(setVariableBrick);
		script.addBrick(ifLogicElseBrick);
		script.addBrick(ifLogicEndBrick);

		sprite.addScript(script);
		project.addSprite(sprite);
		ProjectManager.getInstance().setProject(project);

		sprite.createStartScriptActionSequence();
		sprite.look.act(100f);

		assertEquals("AskBrick didn't request recognition. isRecognitionRequested", true,
				speechRecognition.isRecognitionRequested());

		ArrayList<String> outsideResults = new ArrayList<String>();
		outsideResults.add("fun");
		outsideResults.add("run");

		speechRecognition.setRecogniserResult(outsideResults);
		speechRecognition.triggerReturnResults();

		sprite.look.act(10f);
		sprite.look.act(10f);

		userVariable = project.getUserVariables().getUserVariable(TEST_USERVARIABLE, null);

		assertEquals("IfAnswerBrick not executed as expected", IF_TRUE_VALUE, userVariable.getValue().intValue());
		Reflection.setPrivateField(UtilSpeechRecognition.class, "instance", null);
	}

	public void testIfElseBrick() throws InterruptedException {
		Project project = new Project(getContext(), "testProject");
		Sprite sprite = new Sprite("testSprite");
		Script script = new StartScript(sprite);

		SimulatedSpeechRecognition speechRecognition = new SimulatedSpeechRecognition();
		Reflection.setPrivateField(UtilSpeechRecognition.getInstance(), "instance", speechRecognition);

		AskBrick testAskBrick = new AskBrick();

		ifAnswerLogicBeginBrick = new IfAnswerLogicBeginBrick(sprite, TEST_ANSWER_TRUE);
		ifLogicElseBrick = new IfLogicElseBrick(sprite, ifAnswerLogicBeginBrick);
		ifLogicEndBrick = new IfLogicEndBrick(sprite, ifLogicElseBrick, ifAnswerLogicBeginBrick);

		project.getUserVariables().deleteUserVariableByName(TEST_USERVARIABLE);
		project.getUserVariables().addProjectUserVariable(TEST_USERVARIABLE);
		UserVariable userVariable = project.getUserVariables().getUserVariable(TEST_USERVARIABLE, null);
		userVariable.setValue(IF_FALSE_VALUE);

		SetVariableBrick setVariableBrick = new SetVariableBrick(sprite, new Formula(IF_TRUE_VALUE), userVariable);

		script.addBrick(testAskBrick);
		script.addBrick(ifAnswerLogicBeginBrick);
		script.addBrick(ifLogicElseBrick);
		script.addBrick(setVariableBrick);
		script.addBrick(ifLogicEndBrick);

		sprite.addScript(script);
		project.addSprite(sprite);
		ProjectManager.getInstance().setProject(project);

		sprite.createStartScriptActionSequence();
		sprite.look.act(100f);

		assertEquals("AskBrick didn't request recognition. isRecognitionRequested", true,
				speechRecognition.isRecognitionRequested());

		ArrayList<String> outsideResults = new ArrayList<String>();
		outsideResults.add("fun");
		outsideResults.add("sun");

		speechRecognition.setRecogniserResult(outsideResults);
		speechRecognition.triggerReturnResults();

		sprite.look.act(10f);
		sprite.look.act(10f);

		userVariable = project.getUserVariables().getUserVariable(TEST_USERVARIABLE, null);

		assertEquals("IfAnswerBrick not executed as expected", IF_TRUE_VALUE, userVariable.getValue().intValue());
		Reflection.setPrivateField(UtilSpeechRecognition.class, "instance", null);

	}

}
