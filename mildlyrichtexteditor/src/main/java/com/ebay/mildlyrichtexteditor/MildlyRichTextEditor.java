package com.ebay.mildlyrichtexteditor;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ToggleButton;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.ebay.mildlyrichtexteditor.MildlyRichTextEditor.Style.BOLD;
import static com.ebay.mildlyrichtexteditor.MildlyRichTextEditor.Style.FONT_SIZE;
import static com.ebay.mildlyrichtexteditor.MildlyRichTextEditor.Style.ITALIC;
import static com.ebay.mildlyrichtexteditor.MildlyRichTextEditor.Style.UNDERLINE;

/**
 * Standalone rich text editor widget, currently supporting bold, italic, underline and font size.
 * <p>
 * Adapted from https://github.com/agungsijawir/droid-writer/blob/master/DroidWriter/src/hu/scythe/droidwriter
 * /DroidWriterEditText.java
 */
public class MildlyRichTextEditor extends AppCompatEditText implements CompoundButton.OnCheckedChangeListener
{
	/**
	 * Enumeration of the various supported styles
	 */
	public enum Style
	{
		BOLD("Bold"),
		ITALIC("Italic"),
		UNDERLINE("Underline"),
		FONT_SIZE("Font Size"),
		SERIF("Serif"),
		SANS_SERIF("Sans serif"),
		MONOSPACE("Monospace");
		private final String val;

		Style(String val)
		{
			this.val = val;
		}

		@NonNull
		@Override
		public String toString()
		{
			return val;
		}
	}

	private static final String DEFAULT_ABSOLUTE_SIZE = "14";
	private static final String ABS_SIZE_10 = "10";
	private static final String ABS_SIZE_14 = "14";
	private static final String ABS_SIZE_16 = "16";
	private static final String ABS_SIZE_18 = "18";
	private static final String ABS_SIZE_24 = "24";
	private static final String ABS_SIZE_32 = "32";
	private static final String ABS_SIZE_48 = "48";
	private ImageButton fontSizeButton;
	@VisibleForTesting protected List<ToggleButton> fontSizeButtons;
	@VisibleForTesting protected PopupWindow popupWindow;
	@VisibleForTesting protected ToggleButton boldButton;
	@VisibleForTesting protected ToggleButton italicsButton;
	@VisibleForTesting protected ToggleButton underlineButton;
	@VisibleForTesting protected Float currentRelativeSize = 1f;
	@VisibleForTesting protected boolean isDeletion;
	@VisibleForTesting protected boolean isEditorEmpty;
	private RichTextEditorTextWatcher textWatcher;

	public MildlyRichTextEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize();
	}

	private void initialize()
	{
		textWatcher = new RichTextEditorTextWatcher();
		addTextChangedListener(textWatcher);
	}

	/**
	 * Sets the bold button and its onClick event listener
	 *
	 * @param button A ToggleButton for the bold style
	 */
	public void setBoldToggleButton(ToggleButton button)
	{
		boldButton = button;
		boldButton.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				toggleStyle(BOLD, null);
			}
		});
	}

	/**
	 * Sets the italics button and its onClick event listener
	 *
	 * @param button A ToggleButton for the italic style
	 */
	public void setItalicsToggleButton(ToggleButton button)
	{
		italicsButton = button;
		italicsButton.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				toggleStyle(ITALIC, null);
			}
		});
	}

	/**
	 * Sets the underline button and its onClick event listener
	 *
	 * @param button A ToggleButton for the underline style
	 */
	public void setUnderlineToggleButton(ToggleButton button)
	{
		underlineButton = button;
		underlineButton.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				toggleStyle(UNDERLINE, null);
			}
		});
	}

	/**
	 * Sets the font size button and its onClick event listener. Initializes the PopupWindow
	 * containing the various supported font sizes.
	 *
	 * @param button An ImageButton for opening the PopupWindow menu to select the font size
	 * @param menu The Menu that pops up when the ImageButton is clicked
	 * @param buttons The list of ToggleButtons representing the various supported font sizes
	 */
	public void setFontSizeButton(ImageButton button, final View menu, List<ToggleButton> buttons)
	{
		fontSizeButton = button;
		fontSizeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupWindow.showAsDropDown(fontSizeButton);
			}
		});
		popupWindow = new PopupWindow(fontSizeButton.getContext());
		popupWindow.setFocusable(true);
		popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(menu);
		fontSizeButtons = buttons;
		for (ToggleButton t : fontSizeButtons)
		{
			if (DEFAULT_ABSOLUTE_SIZE.equals(t.getText().toString()))
				t.setChecked(true); //default

			t.setOnCheckedChangeListener(this);
		}
	}

	@Override
	public Parcelable onSaveInstanceState()
	{
		EditorState state = new EditorState(super.onSaveInstanceState());
		if (boldButton != null)
			state.isBoldButtonOn = boldButton.isChecked();

		if (italicsButton != null)
			state.isItalicButtonOn = italicsButton.isChecked();

		if (underlineButton != null)
			state.isUnderlineButtonOn = underlineButton.isChecked();

		return state;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state)
	{
		EditorState editorState = (EditorState)state;
		super.onRestoreInstanceState(editorState.getSuperState());

		if (boldButton != null)
			boldButton.setChecked(editorState.isBoldButtonOn);

		if (italicsButton != null)
			italicsButton.setChecked(editorState.isItalicButtonOn);

		if (underlineButton != null)
			underlineButton.setChecked(editorState.isUnderlineButtonOn);

		addTextChangedListener(new RichTextEditorTextWatcher());
	}

	/**
	 * Callback for the font size ToggleButtons. Because a font size must always be active, it is
	 * impossible to uncheck a font size ToggleButton. Dismisses the popup after a font size is
	 * selected.
	 *
	 * @param buttonView The font size ToggleButton that was toggled.
	 * @param isChecked The state of the font size ToggleButton.
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (isChecked)
		{
			toggleStyle(FONT_SIZE, currentRelativeSize);
			for (ToggleButton t : fontSizeButtons)
			{
				if (t.getId() != buttonView.getId())
				{
					t.setOnCheckedChangeListener(null);
					t.setChecked(false);
					t.setOnCheckedChangeListener(this);
				}
			}
		}
		else //disallow unchecking
			buttonView.setChecked(true);

		popupWindow.dismiss();
	}

	/**
	 * Converts the current Editable in the MildlyRichTextEditor widget to HTML and returns it.
	 *
	 * @return A string containing the resulting HTML from conversion of the editor's rich text.
	 */
	public String getTextHtml()
	{
		return RichTextEditorUtil.compatToHtml(sanitizeUnderlineSpan(getText()));
	}

	/**
	 * Because underline span is added by default to the word that currently has a cursor in it
	 * (Example:" wo|rd ", " |word ", " word| "), we need to sanitize the spanned text before we
	 * convert it into HTML to make sure that only Spans that have been added by the user are saved.
	 */
	@VisibleForTesting
	@Nullable
	Editable sanitizeUnderlineSpan(@Nullable Editable editable)
	{
		if (editable != null && editable.length() > 0)
		{
			CharacterStyle[] styles = editable.getSpans(0, editable.length(), CharacterStyle.class);
			for (CharacterStyle style : styles)
			{
				if (style instanceof UnderlineSpan && !(style instanceof CustomUnderlineSpan))
					editable.removeSpan(style);
			}
		}
		return editable;
	}

	/**
	 * Sets the Editable in the MildlyRichTextEditor widget from the provided HTML input.
	 *
	 * @param html A string containing HTML used to set the Editable text in the editor widget.
	 */
	public void setTextHtml(@Nullable String html)
	{
		if (!TextUtils.isEmpty(html))
		{
			if (textWatcher != null)
				removeTextChangedListener(textWatcher);
			Spanned htmlDescription = RichTextEditorUtil.compatFromHtml(html);
			// We need to substitute all UnderlineSpan spans on the text by spans of type
			// CustomUnderlineSpan, since our editor works with this type of span.
			Editable editableDescription = Editable.Factory.getInstance().newEditable(htmlDescription);
			CharacterStyle[] styles = editableDescription.getSpans(0, htmlDescription.length(), CharacterStyle.class);
			for (CharacterStyle style : styles)
			{
				if (style instanceof UnderlineSpan)
				{
					int spanStart = editableDescription.getSpanStart(style);
					int spanEnd = editableDescription.getSpanEnd(style);
					editableDescription.removeSpan(style);
					editableDescription.setSpan(new CustomUnderlineSpan(), spanStart, spanEnd,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			String trimmedHtmlDescription = editableDescription.toString().trim();
			setTextKeepState(editableDescription.subSequence(0, trimmedHtmlDescription.length()));

			if (textWatcher != null)
				addTextChangedListener(textWatcher);
			else
				addTextChangedListener(new RichTextEditorTextWatcher());
		}
	}

	/**
	 * Triggered when the user has (or has not) selected text within the editor widget. Also
	 * triggered when the user types text, i.e. when the cursor has moved. Responsible for
	 * setting the style controls based on the styles present in the widget's Editable.
	 *
	 * @param selStart The start of the user's selection. Equal to the end if the user has not made
	 * 	a selection.
	 * @param selEnd The end of the user's selection. Equal to the start if the user has not made
	 * 	a selection.
	 */
	@Override
	protected void onSelectionChanged(int selStart, int selEnd)
	{
		if (isDeletion)
		{
			isDeletion = false;
			return;
		}

		if (isEditorEmpty)
		{
			isEditorEmpty = false;
			return;
		}

		if (boldButton == null || italicsButton == null || underlineButton == null || fontSizeButtons == null)
			return;

		boolean boldExists = false;
		boolean italicsExists = false;
		boolean underlinedExists = false;
		Float sizeProportion = null;

		Editable text = getText();
		if (text == null)
			return;

		if (selStart > 0 && selStart == selEnd)
		{ //user moved cursor
			CharacterStyle[] styleSpans = text.getSpans(selStart - 1, selStart, CharacterStyle.class);
			for (CharacterStyle styleSpan : styleSpans)
			{
				if (styleSpan instanceof StyleSpan)
				{
					if (((StyleSpan)styleSpan).getStyle() == Typeface.BOLD)
						boldExists = true;
					else if (((StyleSpan)styleSpan).getStyle() == Typeface.ITALIC)
						italicsExists = true;
					else if (((StyleSpan)styleSpan).getStyle() == Typeface.BOLD_ITALIC)
						boldExists = italicsExists = true;
				}
				else if (styleSpan instanceof CustomUnderlineSpan)
					underlinedExists = true;
				else if (styleSpan instanceof RelativeSizeSpan)
					sizeProportion = ((RelativeSizeSpan)styleSpan).getSizeChange();
			}
		}
		else
		{ //user selected multiple characters
			CharacterStyle[] styleSpans = text.getSpans(selStart, selEnd, CharacterStyle.class);
			for (CharacterStyle styleSpan : styleSpans)
			{
				if (text.getSpanStart(styleSpan) <= selStart && text.getSpanEnd(styleSpan) >= selEnd)
				{
					if (styleSpan instanceof StyleSpan)
					{
						if (((StyleSpan)styleSpan).getStyle() == Typeface.BOLD)
							boldExists = true;
						else if (((StyleSpan)styleSpan).getStyle() == Typeface.ITALIC)
							italicsExists = true;
						else if (((StyleSpan)styleSpan).getStyle() == Typeface.BOLD_ITALIC)
							boldExists = italicsExists = true;
					}
					else if (styleSpan instanceof CustomUnderlineSpan)
						underlinedExists = true;
					else if (styleSpan instanceof RelativeSizeSpan)
						sizeProportion = ((RelativeSizeSpan)styleSpan).getSizeChange();
				}
			}
		}

		boldButton.setChecked(boldExists);
		italicsButton.setChecked(italicsExists);
		underlineButton.setChecked(underlinedExists);
		if (sizeProportion != null)
		{
			switch (sizeProportion.toString())
			{
			case ABS_SIZE_10:
				fontSizeButtons.get(0).setChecked(true);
				break;
			case ABS_SIZE_14:
				fontSizeButtons.get(1).setChecked(true);
				break;
			case ABS_SIZE_16:
				fontSizeButtons.get(2).setChecked(true);
				break;
			case ABS_SIZE_18:
				fontSizeButtons.get(3).setChecked(true);
				break;
			case ABS_SIZE_24:
				fontSizeButtons.get(4).setChecked(true);
				break;
			case ABS_SIZE_32:
				fontSizeButtons.get(5).setChecked(true);
				break;
			case ABS_SIZE_48:
				fontSizeButtons.get(6).setChecked(true);
				break;
			}
		}

		super.onSelectionChanged(selStart, selEnd);
	}

	/**
	 * Determines whether or not the user has made a selection based on the start and end of the
	 * selection.
	 *
	 * @param selectionStart The start of the user's selection. Equal to the end if the user has
	 * 	not made a selection (cursor moved due to typing or deleting)
	 * @param selectionEnd The end of the user's selection. Equal to the start if the user has not
	 * 	made a selection (cursor moved due to typing or deleting).
	 * @return true if the user has made a selection (via long press), or false if the user has
	 * 	not made a selection (cursor moved due to typing or deleting).
	 */
	@VisibleForTesting
	protected boolean isTextSelected(int selectionStart, int selectionEnd)
	{
		if (selectionStart > selectionEnd)
		{
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}
		return selectionEnd > selectionStart;
	}

	/**
	 * Handles setting the style controls based on the styles in the widget's Editable text based
	 * on the user's selection.
	 *
	 * @param style The style to toggle.
	 * @param relativeSize The size of the font relative to the default absolute size of 14sp.
	 */
	@VisibleForTesting
	protected void toggleStyle(Style style, Float relativeSize)
	{
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();
		boolean sectionSelected = isTextSelected(selStart, selEnd);
		if (!sectionSelected)
			return;

		Spannable str = getText();
		if (str == null)
			return;

		boolean exists = false;

		switch (style)
		{
		case BOLD:
			toggleBoldOrItalicStyle(selStart, selEnd, str, Typeface.BOLD);

			break;
		case ITALIC:
			toggleBoldOrItalicStyle(selStart, selEnd, str, Typeface.ITALIC);

			break;
		case UNDERLINE:
			toggleUnderlineStyle(selStart, selEnd, str);

			break;
		case FONT_SIZE:
			toggleFontSizeStyle(relativeSize, selStart, selEnd, str, exists);

			break;
		}

		setSelection(selStart, selEnd);
	}

	private void toggleFontSizeStyle(Float relativeSize, int selStart, int selEnd, Spannable str, boolean exists)
	{
		CharacterStyle[] sizeSpans = str.getSpans(selStart, selEnd, CharacterStyle.class);
		for (CharacterStyle sizeSpan : sizeSpans)
		{
			if (sizeSpan instanceof RelativeSizeSpan)
			{
				str.removeSpan(sizeSpan);
				exists = true;
			}
		}

		if (!exists)
			str.setSpan(new RelativeSizeSpan(relativeSize), selStart, selEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
	}

	private void toggleUnderlineStyle(int selectionStart, int selectionEnd, Spannable str)
	{
		boolean exists = false;
		CustomUnderlineSpan[] underlineSpans = str.getSpans(selectionStart, selectionEnd, CustomUnderlineSpan.class);

		//if underline set, unset it
		for (CustomUnderlineSpan underlineSpan : underlineSpans)
		{
			str.removeSpan(underlineSpan);
			exists = true;
		}

		if (!exists)  //set underline style since it has not been set
			str.setSpan(new CustomUnderlineSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private void toggleBoldOrItalicStyle(int selectionStart, int selectionEnd, Spannable str, int typeface)
	{
		boolean exists = false;
		final StyleSpan[] styleSpans;
		final Iterator<StyleSpan> iterator;
		styleSpans = str.getSpans(selectionStart, selectionEnd, StyleSpan.class);

		//if typeface already set, unset it
		iterator = Arrays.asList(styleSpans).iterator();
		while (iterator.hasNext())
		{
			StyleSpan styleSpan = iterator.next();
			if (styleSpan.getStyle() == typeface)
			{
				str.removeSpan(styleSpan);
				exists = true;
			}
		}

		if (!exists) //set bold style since it has not been set
			str.setSpan(new StyleSpan(typeface), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	/**
	 * TextWatcher that handles both addition and deletion of text. Removes and sets spans based
	 * on the state of the style controls.
	 */
	@VisibleForTesting
	class RichTextEditorTextWatcher implements TextWatcher
	{
		private CharacterStyle[] prevStyles;
		private int positionOffset = 0;

		/**
		 * Minimizing the risk of index out of bound exception.
		 *
		 * @param position Current cursor position.
		 * @return position offset.
		 */
		private int getPositionOffsetSafe(int position)
		{
			return positionOffset <= position ? positionOffset : 0;
		}

		/**
		 * Handles the case where the user has entered text.
		 *
		 * @param e The text from the editor widget which includes the change the user has made.
		 */
		@Override
		public void afterTextChanged(Editable e)
		{
			if (TextUtils.isEmpty(e.toString().trim()))
			{
				boldButton.setChecked(false);
				italicsButton.setChecked(false);
				underlineButton.setChecked(false);
				for (ToggleButton t : fontSizeButtons)
				{
					t.setOnCheckedChangeListener(null);
					if (DEFAULT_ABSOLUTE_SIZE.equals(t.getText().toString()))
						t.setChecked(true);
					else
						t.setChecked(false);
					t.setOnCheckedChangeListener(MildlyRichTextEditor.this);
				}

				CharacterStyle[] styles = e.getSpans(0, e.length(), CharacterStyle.class);
				for (CharacterStyle style : styles)
				{
					e.removeSpan(style);
				}

				isEditorEmpty = true;
				return;
			}

			if (isDeletion)
				return;

			int position = Selection.getSelectionStart(MildlyRichTextEditor.this.getText());

			if (position <= 0)
				return;

			CharacterStyle[] appliedStyles =
				e.getSpans(position - getPositionOffsetSafe(position), position, CharacterStyle.class);
			StyleSpan currentBoldSpan = null;
			StyleSpan currentItalicSpan = null;
			CustomUnderlineSpan currentUnderlineSpan = null;
			RelativeSizeSpan appliedSizeSpan = null;

			for (CharacterStyle appliedStyle : appliedStyles) //identify styles applied to entered text
			{
				if (appliedStyle instanceof StyleSpan)
				{
					if (((StyleSpan)appliedStyle).getStyle() == Typeface.BOLD)
						currentBoldSpan = (StyleSpan)appliedStyle;
					else if (((StyleSpan)appliedStyle).getStyle() == Typeface.ITALIC)
						currentItalicSpan = (StyleSpan)appliedStyle;
				}
				else if (appliedStyle instanceof CustomUnderlineSpan)
					currentUnderlineSpan = (CustomUnderlineSpan)appliedStyle;
				else if (appliedStyle instanceof RelativeSizeSpan)
					appliedSizeSpan = (RelativeSizeSpan)appliedStyle;
			}

			if (boldButton != null && boldButton.isChecked())
			{
				// User switched bold style button on and character does not have bold style applied
				// therefore, apply the bold style inclusively (proceeding characters will inherit
				// this style)
				if (currentBoldSpan == null)
					// User switched bold style button on and character does not have bold style
					// applied therefore, apply the bold style inclusively (proceeding characters
					// will inherit this style)
					e.setSpan(new StyleSpan(Typeface.BOLD), position - getPositionOffsetSafe(position), position,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				else
				{
					e.removeSpan(currentBoldSpan);
					e.setSpan(new StyleSpan(Typeface.BOLD), position - getPositionOffsetSafe(position), position,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else if (boldButton != null && !boldButton.isChecked() && currentBoldSpan != null)
			{
				// User switched bold style button off and character already has the bold style applied
				// Therefore, remove the old bold style span and define a new style that ends 1 position to the right
				// before the newly entered character
				int boldStart = e.getSpanStart(currentBoldSpan);
				int boldEnd = e.getSpanEnd(currentBoldSpan);
				e.removeSpan(currentBoldSpan);
				if (boldStart <= (position - getPositionOffsetSafe(position)))
					e.setSpan(new StyleSpan(Typeface.BOLD), boldStart, position - getPositionOffsetSafe(position),
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				// Since the old bold style span ends after the current cursor position, we need to define a second
				// newly created style span that begins after the newly entered character and ends at the old span's
				// ending position. Therefore, we split the span:
				if (boldEnd > position)
					e.setSpan(new StyleSpan(Typeface.BOLD), position, boldEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}

			// Handle italics in the same fashion as above
			if (italicsButton != null && italicsButton.isChecked())
			{
				if (currentItalicSpan == null)
					e.setSpan(new StyleSpan(Typeface.ITALIC), position - getPositionOffsetSafe(position), position,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				else
				{
					e.removeSpan(currentItalicSpan);
					e.setSpan(new StyleSpan(Typeface.ITALIC), position - getPositionOffsetSafe(position), position,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else if (italicsButton != null && !italicsButton.isChecked() && currentItalicSpan != null)
			{
				int italicStart = e.getSpanStart(currentItalicSpan);
				int italicEnd = e.getSpanEnd(currentItalicSpan);
				e.removeSpan(currentItalicSpan);
				if (italicStart <= (position - getPositionOffsetSafe(position)))
				{
					e.setSpan(new StyleSpan(Typeface.ITALIC), italicStart, position - getPositionOffsetSafe(position),
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				}
				if (italicEnd > position) //split the span
					e.setSpan(new StyleSpan(Typeface.ITALIC), position, italicEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}

			// Handle underlines in the same fashion as above
			if (underlineButton != null && underlineButton.isChecked())
			{
				if (currentUnderlineSpan == null)
					e.setSpan(new CustomUnderlineSpan(), position - getPositionOffsetSafe(position), position,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				else
				{
					e.removeSpan(currentUnderlineSpan);
					e.setSpan(new CustomUnderlineSpan(), position - getPositionOffsetSafe(position), position,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else if (underlineButton != null && !underlineButton.isChecked() && currentUnderlineSpan != null)
			{
				int underLineStart = e.getSpanStart(currentUnderlineSpan);
				int underLineEnd = e.getSpanEnd(currentUnderlineSpan);
				e.removeSpan(currentUnderlineSpan);
				if (underLineStart <= (position - positionOffset))
					e.setSpan(new CustomUnderlineSpan(), underLineStart, position - getPositionOffsetSafe(position),
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

				if (underLineEnd > position) //split the span
					e.setSpan(new CustomUnderlineSpan(), position, underLineEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}

			//Float selectedRelativeSize = relativeSizes.get(sizeSpinner.getSelectedItemPosition());
			Float selectedRelativeSize = null;
			for (ToggleButton t : fontSizeButtons)
			{
				if (t.isChecked())
					selectedRelativeSize = Float.parseFloat(t.getText().toString()) / 14f;
			}

			if (selectedRelativeSize != null && Float.compare(currentRelativeSize, selectedRelativeSize) != 0)
			{
				if (appliedSizeSpan == null)
					// User changed the size and character is not yet set to the selected size proportion
					// therefore, apply the size inclusively (proceeding characters will inherit this style)
					e.setSpan(new RelativeSizeSpan(selectedRelativeSize), position - getPositionOffsetSafe(position),
						position, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				else
				{
					e.removeSpan(currentBoldSpan);
					e.setSpan(new RelativeSizeSpan(selectedRelativeSize), position - getPositionOffsetSafe(position),
						position, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else if (selectedRelativeSize != null && Float.compare(currentRelativeSize,
				selectedRelativeSize) != 0 && appliedSizeSpan != null)
			{
				// User changed the size and character already is the selected size.
				// Therefore, remove the old size span and define a new size span that ends 1
				// position to the right before the newly entered character
				int sizeStart = e.getSpanStart(appliedSizeSpan);
				int sizeEnd = e.getSpanEnd(appliedSizeSpan);
				e.removeSpan(appliedSizeSpan);
				if (sizeStart <= (position - positionOffset))
					e.setSpan(new RelativeSizeSpan(currentRelativeSize), sizeStart,
						position - getPositionOffsetSafe(position), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

				e.setSpan(new RelativeSizeSpan(selectedRelativeSize), position - getPositionOffsetSafe(position),
					sizeEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				currentRelativeSize = selectedRelativeSize;
			}
		}

		/**
		 * Handles the case where the user has made a deletion.
		 *
		 * @param s The CharSequence containing the text prior to the deletion.
		 * @param start The starting position of the user's cursor prior to the change.
		 * @param count The length of s prior to the user's change.
		 * @param after The length of the newly new text replacing count number of characters
		 * 	after the start, e.g. 1 when the user has typed a single character. 0 when
		 * 	the user has deleted a single character.
		 */
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{
			if (after < count)
			{ //delete
				isDeletion = true;
				Editable e = MildlyRichTextEditor.this.getText();
				if (e == null)
					return;

				this.prevStyles = e.getSpans(start, start + count, CharacterStyle.class);

				if (this.prevStyles.length > 0)
				{
					boldButton.setChecked(false);
					italicsButton.setChecked(false);
					underlineButton.setChecked(false);

					for (CharacterStyle appliedStyle : this.prevStyles)
					{
						if (appliedStyle instanceof StyleSpan)
						{
							if (((StyleSpan)appliedStyle).getStyle() == Typeface.BOLD)
								boldButton.setChecked(true);
							else if (((StyleSpan)appliedStyle).getStyle() == Typeface.ITALIC)
								italicsButton.setChecked(true);
						}
						else if (appliedStyle instanceof CustomUnderlineSpan)
							underlineButton.setChecked(true);
						else if (appliedStyle instanceof RelativeSizeSpan)
						{
							RelativeSizeSpan appliedSizeSpan = (RelativeSizeSpan)appliedStyle;
							String absoluteSize = Integer.toString(Math.round(appliedSizeSpan.getSizeChange() * 14f));
							for (ToggleButton t : fontSizeButtons)
							{
								if (absoluteSize.equals(t.getText().toString()))
									t.setChecked(true);
							}
						}
					}
				}
				else
				{
					boldButton.setChecked(false);
					italicsButton.setChecked(false);
					underlineButton.setChecked(false);
				}
			}
			else
			{
				positionOffset = after;
				isDeletion = false;
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			//not implemented
		}
	}

	/**
	 * For saving and restoring the RichTextEditor state
	 */
	protected static class EditorState extends BaseSavedState
	{
		boolean isBoldButtonOn;
		boolean isItalicButtonOn;
		boolean isUnderlineButtonOn;

		EditorState(Parcelable superState)
		{
			super(superState);
		}

		EditorState(Parcel in)
		{
			super(in);
			isBoldButtonOn = (in.readInt() == 1);
			isItalicButtonOn = (in.readInt() == 1);
			isUnderlineButtonOn = (in.readInt() == 1);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			super.writeToParcel(dest, flags);
			dest.writeInt(isBoldButtonOn ? 1 : 0);
			dest.writeInt(isItalicButtonOn ? 1 : 0);
			dest.writeInt(isUnderlineButtonOn ? 1 : 0);
		}

		public static final Parcelable.Creator<EditorState> CREATOR = new Parcelable.Creator<EditorState>()
		{
			@Override
			public EditorState createFromParcel(Parcel in)
			{
				return new EditorState(in);
			}

			@Override
			public EditorState[] newArray(int size)
			{
				return new EditorState[size];
			}
		};
	}

	/**
	 * The behavior of EditText differs a little between versions of Android and different devices. Some versions of
	 * EditText add UnderlineSpan by default to the text, surrounded by spaces, when the cursor is inside this text
	 * (that basically means that EditText is temporarily adding UnderlineSpan to word when the cursor is placed
	 * somewhere inside the word, or at the edge of the word, right before the very first character, or right after the
	 * very last character. Example: " wo|rd ", " |word ", " word| "). This behavior causes the following problem with
	 * our RTE: when such underlined by default text is selected, the RTE does not have a way to determine if
	 * the UnderlineSpan on the text was set by the user or by the editor. This class is intended to solve the problem
	 * by just introducing a new class with the same functionality of UnderlineSpan, but with the different name. This
	 * way, when we get spans that are currently applied to the text, we can distinguish between UnderlineSpan that is
	 * applied by the EditText because of its behavior, and the CustomUnderlineSpan that was actually applied by the
	 * user.
	 */
	@VisibleForTesting
	static final class CustomUnderlineSpan extends UnderlineSpan
	{
		// no additional implementation needed
	}
}