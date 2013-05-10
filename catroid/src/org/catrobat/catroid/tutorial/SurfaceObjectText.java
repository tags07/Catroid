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
package org.catrobat.catroid.tutorial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * @author amore
 * 
 */
public class SurfaceObjectText implements SurfaceObject {

	private Context context;
	private TutorialOverlay tutorialOverlay;
	private Paint paint;

	private String text;
	private int[] position = { 220, 150 };

	private long lastUpdateTime = 0;
	private int updateTime = 150;

	private int currentStep = 0;

	public SurfaceObjectText(TutorialOverlay overlay, String text) {
		context = Tutorial.getInstance(null).getActualContext();

		this.text = addingLineBreaks(text);

		this.tutorialOverlay = overlay;
		tutorialOverlay.addSurfaceObject(this);
	}

	private String addingLineBreaks(String text) {
		int textWidth = 200;

		Paint paint = new Paint();
		paint.setTextSize(25);

		String formatedText = "";
		if (paint.measureText(text) < textWidth) {
			formatedText = text;
		} else {

			String[] words = text.split(" ");
			String currentLine = "";

			for (int i = 0; i < words.length; i++) {
				if (paint.measureText(currentLine + words[i] + " ") < textWidth) {
					currentLine += words[i] + " ";
					formatedText += words[i] + " ";
				} else {
					currentLine = words[i] + " ";
					formatedText += "\n" + words[i] + " ";
				}
			}
		}

		return formatedText;
	}

	@Override
	public void draw(Canvas canvas) {
		paint = new Paint();
		paint.setTextSize(25);
		paint.setARGB(255, 0, 238, 0);

		drawMultilineText(this.text, this.position[0], this.position[1], paint, canvas);

	}

	void drawMultilineText(String str, int x, int y, Paint paint, Canvas canvas) {
		int lineHeight = 0;
		int yoffset = 0;
		String[] lines = str.split("\n");
		Rect bounds = new Rect();

		paint.getTextBounds(str, 0, 2, bounds);
		lineHeight = (int) (bounds.height() * 1.2);

		for (int i = 0; i < lines.length; ++i) {
			canvas.drawText(lines[i], x, y + yoffset, paint);
			yoffset = yoffset + lineHeight;
		}
	}

	@Override
	public void update(long gameTime) {
		if ((lastUpdateTime + updateTime) < gameTime) {
			lastUpdateTime = gameTime;
			currentStep++;
		}

	}

}
