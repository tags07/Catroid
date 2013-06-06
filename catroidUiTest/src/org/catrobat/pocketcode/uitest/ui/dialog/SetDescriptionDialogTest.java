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
package org.catrobat.pocketcode.uitest.ui.dialog;

import org.catrobat.pocketcode.ProjectManager;
import org.catrobat.pocketcode.R;
import org.catrobat.pocketcode.uitest.util.UiTestUtils;
import org.catrobat.pocketcode.content.Project;
import org.catrobat.pocketcode.io.StorageHandler;
import org.catrobat.pocketcode.ui.MainMenuActivity;
import org.catrobat.pocketcode.ui.MyProjectsActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

public class SetDescriptionDialogTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Solo solo;
	private String testProject = UiTestUtils.PROJECTNAME1;

	public SetDescriptionDialogTest() {
		super(MainMenuActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		UiTestUtils.clearAllUtilTestProjects();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	@Override
	protected void tearDown() throws Exception {
		UiTestUtils.goBackToHome(getInstrumentation());
		solo.finishOpenedActivities();
		ProjectManager.getInstance().deleteCurrentProject();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
		solo = null;
	}

	public void testMultiLineProjectDescription() {
		StorageHandler storageHandler = StorageHandler.getInstance();
		Project uploadProject = new Project(getActivity(), testProject);
		storageHandler.saveProject(uploadProject);

		solo.sleep(300);
		solo.clickOnButton(solo.getString(R.string.main_menu_programs));
		solo.waitForActivity(MyProjectsActivity.class.getSimpleName());
		solo.waitForFragmentById(R.id.fragment_projects_list);
		solo.clickLongOnText(testProject);
		solo.clickOnText(solo.getString(R.string.set_description));
		EditText description = (EditText) solo.getView(R.id.dialog_text_EditMultiLineText);
		solo.sleep(2000);
		int descriptionInputType = description.getInputType();
		int typeToCheck = android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_CLASS_TEXT
				| android.text.InputType.TYPE_TEXT_VARIATION_NORMAL;
		assertEquals("Description field is not multiline!", descriptionInputType, typeToCheck);

		int projectDescriptionNumberOfLines = (description.getHeight() - description.getCompoundPaddingTop() - description
				.getCompoundPaddingBottom()) / description.getLineHeight();
		assertEquals("Project description field is not multiline", 3, projectDescriptionNumberOfLines);
	}
}