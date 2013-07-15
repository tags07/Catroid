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
package org.catrobat.catroid.content.bricks;

import java.util.ArrayList;
import java.util.List;

import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;

public abstract class SendBeginBrick extends NestingBrick {
	private static final long serialVersionUID = 1L;

	protected SendEndBrick sendEndBrick;
	private transient long beginSendTime;

	private transient SendBeginBrick copy;

	protected SendBeginBrick() {
	}

	protected void setFirstStartTime() {
		beginSendTime = System.nanoTime();
	}

	public long getBeginSendTime() {
		return beginSendTime;
	}

	public void setBeginSendTime(long beginSendTime) {
		this.beginSendTime = beginSendTime;
	}

	public SendEndBrick getSendEndBrick() {
		return this.sendEndBrick;
	}

	public void setSendEndBrick(SendEndBrick sendEndBrick) {
		this.sendEndBrick = sendEndBrick;
	}

	@Override
	public boolean isDraggableOver(Brick brick) {
		if (brick == sendEndBrick) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isInitialized() {
		if (sendEndBrick == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void initialize() {
		sendEndBrick = new SendEndBrick(sprite, this);
	}

	@Override
	public List<NestingBrick> getAllNestingBrickParts(boolean sorted) {
		List<NestingBrick> nestingBrickList = new ArrayList<NestingBrick>();
		nestingBrickList.add(this);
		nestingBrickList.add(sendEndBrick);

		return nestingBrickList;
	}

	@Override
	public abstract Brick clone();

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		//sendEndBrick will be set in the SendEndBrick's copyBrickForSprite method
		SendBeginBrick copyBrick = (SendBeginBrick) clone();
		copyBrick.sprite = sprite;
		copy = copyBrick;
		return copyBrick;
	}

	public SendBeginBrick getCopy() {
		return copy;
	}

}
