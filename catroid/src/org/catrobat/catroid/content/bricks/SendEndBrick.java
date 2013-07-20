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

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ExtendedActions;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class SendEndBrick extends NestingBrick implements AllowedAfterDeadEndBrick {
	static final int FOREVER = -1;
	private static final long serialVersionUID = 1L;
	private static final String TAG = SendEndBrick.class.getSimpleName();
	private SendBeginBrick sendBeginBrick;

	public SendEndBrick(Sprite sprite, SendBeginBrick sendStartingBrick) {
		this.sprite = sprite;
		this.sendBeginBrick = sendStartingBrick;
		sendStartingBrick.setSendEndBrick(this);
	}

	public SendEndBrick() {

	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		SendEndBrick copyBrick = (SendEndBrick) clone();
		sendBeginBrick.setSendEndBrick(this);

		copyBrick.sprite = sprite;
		return copyBrick;
	}

	public SendBeginBrick getSendBeginBrick() {
		return sendBeginBrick;
	}

	public void setSendBeginBrick(SendBeginBrick sendBeginBrick) {
		this.sendBeginBrick = sendBeginBrick;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.brick_send_end, null);
		view = getViewWithAlpha(alphaValue);
		checkbox = (CheckBox) view.findViewById(R.id.brick_send_end_checkbox);
		final Brick brickInstance = this;

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});
		return view;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_send_end_layout);
		if (layout == null) {
			layout = (LinearLayout) view.findViewById(R.id.brick_send_end_layout);
			TextView sendLabel = (TextView) view.findViewById(R.id.brick_send_end_label);
			sendLabel.setTextColor(sendLabel.getTextColors().withAlpha(alphaValue));
		} else {
			TextView sendLabel = (TextView) view.findViewById(R.id.brick_send_end_label);
			sendLabel.setTextColor(sendLabel.getTextColors().withAlpha(alphaValue));
		}
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);

		this.alphaValue = (alphaValue);
		return view;
	}

	@Override
	public Brick clone() {
		return new SendEndBrick(getSprite(), getSendBeginBrick());
	}

	@Override
	public View getPrototypeView(Context context) {
		return View.inflate(context, R.layout.brick_send_end, null);
	}

	@Override
	public boolean isDraggableOver(Brick brick) {
		if (brick == sendBeginBrick) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isInitialized() {
		if (sendBeginBrick == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void initialize() {
		sendBeginBrick = new SendToPcBrick(sprite);
		Log.w(TAG, "Not supposed to create the SendBeginBrick!");
	}

	@Override
	public List<NestingBrick> getAllNestingBrickParts(boolean sorted) {
		List<NestingBrick> nestingBrickList = new ArrayList<NestingBrick>();
		if (sorted) {
			nestingBrickList.add(sendBeginBrick);
			nestingBrickList.add(this);
		} else {
			nestingBrickList.add(this);
			nestingBrickList.add(sendBeginBrick);
		}
		return nestingBrickList;
	}

	@Override
	public View getNoPuzzleView(Context context, int brickId, BaseAdapter adapter) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.brick_send_end, null);
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(ExtendedActions.sendEnd(sprite));
		return null;
	}
}
