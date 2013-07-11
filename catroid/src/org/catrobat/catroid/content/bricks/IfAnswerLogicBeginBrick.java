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

import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.BrickValues;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ExtendedActions;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.ScriptActivity;
import org.catrobat.catroid.ui.dialogs.BrickTextDialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

public class IfAnswerLogicBeginBrick extends IfLogicBeginBrick {
	private static final long serialVersionUID = 1L;
	private String answerPrediction;

	public IfAnswerLogicBeginBrick(Sprite sprite, String suggestion) {
		super(sprite, new Formula(1));
		this.sprite = sprite;
		answerPrediction = suggestion;
	}

	@Override
	public int getRequiredResources() {
		return SPEECH_RECOGNITION;
	}

	@Override
	public Brick clone() {
		return new IfAnswerLogicBeginBrick(sprite, answerPrediction);
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}
		if (view == null) {
			alphaValue = 255;
		}

		view = View.inflate(context, R.layout.brick_if_answer, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(R.id.brick_if_answer_begin_checkbox);
		final Brick brickInstance = this;

		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		TextView prototypeTextView = (TextView) view.findViewById(R.id.brick_if_answer_begin_prototype_text_view);
		EditText ifBeginEditText = (EditText) view.findViewById(R.id.brick_if_answer_begin_edit_text);
		ifBeginEditText.setText(answerPrediction);

		prototypeTextView.setVisibility(View.GONE);
		ifBeginEditText.setVisibility(View.VISIBLE);

		ifBeginEditText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (checkbox.getVisibility() == View.VISIBLE) {
					return;
				}
				ScriptActivity activity = (ScriptActivity) view.getContext();

				BrickTextDialog editDialog = new BrickTextDialog() {
					@Override
					protected void initialize() {
						input.setText(answerPrediction);
						input.setSelectAllOnFocus(true);
					}

					@Override
					protected boolean getPositiveButtonEnabled() {
						return true;
					}

					@Override
					protected TextWatcher getInputTextChangedListener(Button buttonPositive) {
						return new TextWatcher() {
							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {
							}

							@Override
							public void beforeTextChanged(CharSequence s, int start, int count, int after) {
							}

							@Override
							public void afterTextChanged(Editable s) {
							}
						};
					}

					@Override
					protected boolean handleOkButton() {
						answerPrediction = (input.getText().toString()).trim();
						return true;
					}
				};

				editDialog.show(activity.getSupportFragmentManager(), "dialog_ask_brick");
			}
		});

		return view;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.brick_if_answer_begin_layout);
		Drawable background = layout.getBackground();
		background.setAlpha(alphaValue);

		TextView ifLabel = (TextView) view.findViewById(R.id.if_answer_label);
		TextView ifLabelEnd = (TextView) view.findViewById(R.id.if_answer_label_second_part);
		EditText editX = (EditText) view.findViewById(R.id.brick_if_answer_begin_edit_text);
		ifLabel.setTextColor(ifLabel.getTextColors().withAlpha(alphaValue));
		ifLabelEnd.setTextColor(ifLabelEnd.getTextColors().withAlpha(alphaValue));
		editX.setTextColor(editX.getTextColors().withAlpha(alphaValue));
		editX.getBackground().setAlpha(alphaValue);

		this.alphaValue = (alphaValue);
		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		View prototypeView = View.inflate(context, R.layout.brick_if_answer, null);
		TextView textIfBegin = (TextView) prototypeView.findViewById(R.id.brick_if_answer_begin_prototype_text_view);
		textIfBegin.setText(String.valueOf(BrickValues.IF_CONDITION));
		return prototypeView;
	}

	@Override
	public void onClick(View view) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		SequenceAction ifAction = ExtendedActions.sequence();
		SequenceAction elseAction = ExtendedActions.sequence();
		Action action = ExtendedActions.ifAnswerLogic(sprite, answerPrediction, ifAction, elseAction); //TODO finish!!!
		sequence.addAction(action);

		LinkedList<SequenceAction> returnActionList = new LinkedList<SequenceAction>();
		returnActionList.add(elseAction);
		returnActionList.add(ifAction);

		return returnActionList;
	}

}
