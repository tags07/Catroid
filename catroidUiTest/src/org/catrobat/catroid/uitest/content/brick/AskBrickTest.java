package org.catrobat.catroid.uitest.content.brick;

import java.util.ArrayList;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.AskBrick;
import org.catrobat.catroid.content.bricks.WaitBrick;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.uitest.util.Reflection;
import org.catrobat.catroid.uitest.util.UiTestUtils;
import org.catrobat.catroid.utils.UtilSpeechRecognition;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognizerIntent;
import android.test.ActivityInstrumentationTestCase2;

public class AskBrickTest extends ActivityInstrumentationTestCase2<StageActivity> {

	private Instrumentation instrument;
	private Project project;
	private Sprite sprite;

	public AskBrickTest() {
		super(StageActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		ScreenValues.SCREEN_HEIGHT = 16;
		ScreenValues.SCREEN_WIDTH = 16;
		UiTestUtils.prepareStageForTest();
		instrument = getInstrumentation();
		createProject();
		UtilSpeechRecognition.getInstance();
	}

	@Override
	public void tearDown() throws Exception {
		getActivity().finish();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
	}

	public void testAskBrickWithResult() {
		Intent mockResultIntent = new Intent();
		ArrayList<String> mockRecognizedWords = new ArrayList<String>();
		mockRecognizedWords.add("fun");
		mockRecognizedWords.add("sun");
		mockRecognizedWords.add("run");
		mockResultIntent.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, mockRecognizedWords);

		ActivityResult mockResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, mockResultIntent);
		IntentFilter recognizeFilter = new IntentFilter(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		ActivityMonitor recognizeMonitor = instrument.addMonitor(recognizeFilter, mockResult, true);

		getActivity();

		instrument.waitForMonitorWithTimeout(recognizeMonitor, 2000);
		assertEquals("Recognize intent wasn't fired as expected, monitor hit", 1, recognizeMonitor.getHits());

		assertEquals("Best AskAnswer was not stored as expected.", mockRecognizedWords.get(0), UtilSpeechRecognition
				.getInstance().getLastBestAnswer());

		String expectedLastAnswer = "";
		for (String answer : mockRecognizedWords) {
			expectedLastAnswer += " " + answer;
		}
		assertEquals("Last full AskAnswer was not stored as expected.", expectedLastAnswer, UtilSpeechRecognition
				.getInstance().getLastAnswer());

		instrument.waitForIdleSync();
		assertTrue("Stage didn't finish after AskAnswer returned.",
				ProjectManager.getInstance().getCurrentSprite().look.getAllActionsAreFinished());
	}

	public void testNoAskAnswer() {
		ActivityResult mockResultCanceled = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null);

		getActivity();

		Reflection.setPrivateField(UtilSpeechRecognition.getInstance(), "lastBestAnswer", "fun");
		ActivityMonitor recognizeMonitor = instrument.addMonitor(new IntentFilter(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
				mockResultCanceled, true);

		instrument.waitForMonitorWithTimeout(recognizeMonitor, 2000);
		assertEquals("Recognize intent wasn't fired as expected, monitor hit", 1, recognizeMonitor.getHits());

		instrument.waitForIdleSync();
		assertEquals("AskAnswer did'nt reset answers.", "", UtilSpeechRecognition.getInstance().getLastBestAnswer());
		assertTrue("Stage didn't finish after AskAnswer returned.",
				ProjectManager.getInstance().getCurrentSprite().look.getAllActionsAreFinished());
	}

	public void testResettingAskAnswers() {
		sprite.getScript(0).addBrick(new WaitBrick(sprite, 2000));
		sprite.getScript(0).addBrick(new AskBrick(sprite, "The second question"));

		Intent mockResultIntent = new Intent();
		ArrayList<String> recognizedWords = new ArrayList<String>();
		recognizedWords.add("fun");
		recognizedWords.add("sun");
		recognizedWords.add("run");
		mockResultIntent.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, recognizedWords);

		ActivityResult mockResultOk = new Instrumentation.ActivityResult(Activity.RESULT_OK, mockResultIntent);
		ActivityResult mockResultCanceled = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null);

		ActivityMonitor recognizeOkMonitor = instrument.addMonitor(
				new IntentFilter(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), mockResultOk, true);
		ActivityMonitor recognizeCancelMonitor = instrument.addMonitor(new IntentFilter(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), mockResultCanceled, true);

		getActivity();

		instrument.waitForMonitorWithTimeout(recognizeOkMonitor, 2000);
		assertEquals("Recognize intent wasn't fired as expected, monitor hit", 1, recognizeOkMonitor.getHits());

		assertEquals("AskAnswer was not stored as expected.", recognizedWords.get(0), UtilSpeechRecognition
				.getInstance().getLastBestAnswer());

		instrument.waitForMonitorWithTimeout(recognizeOkMonitor, 2000);
		assertEquals("Second recognize intent wasn't fired as expected, monitor hit", 1,
				recognizeCancelMonitor.getHits());

		instrument.waitForIdleSync();
		assertEquals("AskAnswer was not resettet after second recognition-intent.", "", UtilSpeechRecognition
				.getInstance().getLastBestAnswer());
		assertTrue("Stage didn't finish after AskAnswer returned.",
				ProjectManager.getInstance().getCurrentSprite().look.getAllActionsAreFinished());
	}

	private void createProject() {
		project = new Project(instrument.getTargetContext(), UiTestUtils.DEFAULT_TEST_PROJECT_NAME);
		sprite = new Sprite("cat");
		Script script = new StartScript(sprite);
		WaitBrick waitBrick = new WaitBrick(sprite, 500);
		AskBrick askBrick = new AskBrick(sprite, "Wanna test?");
		script.addBrick(waitBrick);
		script.addBrick(askBrick);

		sprite.addScript(script);
		project.addSprite(sprite);

		ProjectManager.getInstance().setProject(project);
		ProjectManager.getInstance().setCurrentSprite(sprite);
		ProjectManager.getInstance().setCurrentScript(script);
	}
}
