package com.ebay.mildlyrichtexteditor;

import android.app.Application;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ToggleButton;

import com.ebay.mildlyrichtexteditorlibrary.BuildConfig;
import org.hamcrest.*;
import org.hamcrest.core.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class MildlyRichTextEditorTest
{
	private MildlyRichTextEditor editor;
	private Application context = RuntimeEnvironment.application;

	@Before
	public void initialize()
	{
		editor = new MildlyRichTextEditor(context, null);
	}

	@Test
	public void testSetBoldToggleButton()
	{
		ToggleButton button = new ToggleButton(context);
		button.setId(android.R.id.button1);
		editor.setBoldToggleButton(button);

		assertThat(editor.boldButton, is(notNullValue()));
		assertThat(editor.boldButton.getId(), is(android.R.id.button1));
	}

	@Test
	public void testSetItalicsToggleButton()
	{
		ToggleButton button = new ToggleButton(context);
		button.setId(android.R.id.button2);
		editor.setItalicsToggleButton(button);

		assertThat(editor.italicsButton, is(notNullValue()));
		assertThat(editor.italicsButton.getId(), is(android.R.id.button2));
	}

	@Test
	public void testSetUnderlineToggleButton()
	{
		ToggleButton button = new ToggleButton(context);
		button.setId(android.R.id.button3);
		editor.setUnderlineToggleButton(button);

		assertThat(editor.underlineButton, is(notNullValue()));
		assertThat(editor.underlineButton.getId(), is(android.R.id.button3));
	}

	@Test
	public void testSetFontSizeButton()
	{
		ImageButton sizeButton = new ImageButton(context);
		View menu = mock(View.class);
		ToggleButton ten = new ToggleButton(context);
		ten.setText("10");
		ToggleButton fourteen = new ToggleButton(context);
		fourteen.setText("14");
		ToggleButton sixteen = new ToggleButton(context);
		sixteen.setText("16");
		List<ToggleButton> sizeToggles = Arrays.asList(ten, fourteen, sixteen);
		editor.setFontSizeButton(sizeButton, menu, sizeToggles);

		assertThat(editor.popupWindow, is(notNullValue()));
		assertThat(editor.popupWindow.isFocusable(), is(true));
		assertThat(editor.popupWindow.getWidth(), is(LinearLayout.LayoutParams.WRAP_CONTENT));
		assertThat(editor.popupWindow.getHeight(), is(WindowManager.LayoutParams.WRAP_CONTENT));
		assertThat(editor.popupWindow.getContentView(), is(menu));
		assertThat(editor.fontSizeButtons.size(), is(3));
		assertThat(editor.fontSizeButtons.get(0).isChecked(), is(false));
		assertThat(editor.fontSizeButtons.get(1).isChecked(), is(true));
		assertThat(editor.fontSizeButtons.get(2).isChecked(), is(false));
	}

	@Test
	public void testOnCheckedChanged()
	{
		ToggleButton button1 = new ToggleButton(context);
		button1.setId(android.R.id.button1);
		button1.setOnCheckedChangeListener(editor);
		ToggleButton button2 = new ToggleButton(context);
		button2.setId(android.R.id.button2);
		button2.setOnCheckedChangeListener(editor);
		ToggleButton button3 = new ToggleButton(context);
		button3.setId(android.R.id.button3);
		button3.setOnCheckedChangeListener(editor);
		editor.popupWindow = new PopupWindow(context);
		editor.fontSizeButtons = new ArrayList<>();
		editor.fontSizeButtons.add(button1);
		editor.fontSizeButtons.add(button2);
		editor.fontSizeButtons.add(button3);

		button1.setChecked(true);
		assertThat(button1.isChecked(), is(true));
		assertThat(button2.isChecked(), is(false));
		assertThat(button3.isChecked(), is(false));

		button1.setChecked(false);
		assertThat(button1.isChecked(), is(true));
		assertThat(button2.isChecked(), is(false));
		assertThat(button3.isChecked(), is(false));

		button2.setChecked(true);
		assertThat(button1.isChecked(), is(false));
		assertThat(button2.isChecked(), is(true));
		assertThat(button3.isChecked(), is(false));

		button3.setChecked(true);
		assertThat(button1.isChecked(), is(false));
		assertThat(button2.isChecked(), is(false));
		assertThat(button3.isChecked(), is(true));
	}

	@Test
	public void testGetTextHtml()
	{
		Spannable spannable = new SpannableString("String with bold, italic and underline spans");
		spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 16, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.ITALIC), 18, 24, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		spannable.setSpan(new MildlyRichTextEditor.CustomUnderlineSpan(), 29, 38, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		editor.setText(spannable);
		String html = editor.getTextHtml();

		assertThat(html, containsString("<b>bold</b>"));
		assertThat(html, containsString("<i>italic</i>"));
		assertThat(html, containsString("<u>underline</u>"));
	}

	@Test
	public void testSetTextHtml()
	{
		editor.setTextHtml("<p dir=\"ltr\">String with <b>bold</b>, <i>italic</i> and " + "<u>underline</u> " +
            "spans</p>");
		Spanned spanned = editor.getText();
		assert spanned != null;
		CharacterStyle[] styles = spanned.getSpans(0, spanned.length(), CharacterStyle.class);

		assertThat(styles.length, is(3));
		assertThat(styles[0], is(instanceOf(StyleSpan.class)));
		assertThat(((StyleSpan)styles[0]).getStyle(), is(Typeface.BOLD));
		assertThat(styles[1], is(instanceOf(StyleSpan.class)));
		assertThat(((StyleSpan)styles[1]).getStyle(), is(Typeface.ITALIC));
		assertThat(styles[2], is(instanceOf(UnderlineSpan.class)));
	}

	@Test
	public void testOnSelectionChanged()
	{
		editor.boldButton = new ToggleButton(context);
		editor.italicsButton = new ToggleButton(context);
		editor.underlineButton = new ToggleButton(context);
		editor.fontSizeButtons = new ArrayList<>();
		editor.setTextHtml("<p dir=\"ltr\">String with <b>bold</b>, <i>italic</i> and " + "<u>underline</u> " +
            "spans</p>");

		editor.onSelectionChanged(0, 11);
		assertThat(editor.boldButton.isChecked(), is(false));
		assertThat(editor.italicsButton.isChecked(), is(false));
		assertThat(editor.underlineButton.isChecked(), is(false));

		editor.onSelectionChanged(12, 16);
		assertThat(editor.boldButton.isChecked(), is(true));
		assertThat(editor.italicsButton.isChecked(), is(false));
		assertThat(editor.underlineButton.isChecked(), is(false));

		editor.onSelectionChanged(18, 24);
		assertThat(editor.boldButton.isChecked(), is(false));
		assertThat(editor.italicsButton.isChecked(), is(true));
		assertThat(editor.underlineButton.isChecked(), is(false));

		editor.onSelectionChanged(29, 38);
		assertThat(editor.boldButton.isChecked(), is(false));
		assertThat(editor.italicsButton.isChecked(), is(false));
		assertThat(editor.underlineButton.isChecked(), is(true));
	}

	@Test
	public void testIsTextSelected()
	{
		assertThat(editor.isTextSelected(0, 0), is(false));
		assertThat(editor.isTextSelected(-5, -5), is(false));
		assertThat(editor.isTextSelected(0, 99), is(true));
		assertThat(editor.isTextSelected(99, 0), is(true));
		assertThat(editor.isTextSelected(-5, 5), is(true));
	}

	@Test
	public void testToggleStyle()
	{
		String text = "some text";
		editor.setTextHtml(text);
		editor.setSelection(1, 1);
		editor.toggleStyle(MildlyRichTextEditor.Style.BOLD, null);
		CharacterStyle[] styles = Objects.requireNonNull(editor.getText()).getSpans(0, 5, CharacterStyle.class);
		assertThat(styles, Matchers.emptyArray());

		text = "Bold me!";
		editor.setTextHtml(text);
		editor.setSelection(0, 5);
		editor.toggleStyle(MildlyRichTextEditor.Style.BOLD, null);
		styles = editor.getText().getSpans(0, 5, CharacterStyle.class);
		assertThat(styles.length, Is.is(1));
		assertThat(styles[0], Matchers.instanceOf(StyleSpan.class));
		assertThat(((StyleSpan)styles[0]).getStyle(), Is.is(Typeface.BOLD));
		editor.setSelection(6, text.length() - 1);
		styles = editor.getText().getSpans(6, 8, CharacterStyle.class);
		assertThat(styles.length, Is.is(0));

		text = "Italicize me!";
		editor.setTextHtml(text);
		editor.setSelection(0, 5);
		editor.toggleStyle(MildlyRichTextEditor.Style.ITALIC, null);
		styles = editor.getText().getSpans(0, 5, CharacterStyle.class);
		assertThat(styles.length, Is.is(1));
		assertThat(styles[0], Matchers.instanceOf(StyleSpan.class));
		assertThat(((StyleSpan)styles[0]).getStyle(), Is.is(Typeface.ITALIC));
		editor.setSelection(6, text.length() - 1);
		styles = editor.getText().getSpans(6, 8, CharacterStyle.class);
		assertThat(styles.length, Is.is(0));

		text = "Underline me!";
		editor.setTextHtml(text);
		editor.setSelection(0, 5);
		editor.toggleStyle(MildlyRichTextEditor.Style.UNDERLINE, null);
		UnderlineSpan[] underlineSpans = editor.getText().getSpans(0, 5, UnderlineSpan.class);
		assertThat(underlineSpans.length, Is.is(1));
		assertThat(underlineSpans[0], Matchers.instanceOf(UnderlineSpan.class));
		editor.setSelection(6, text.length());
		underlineSpans = editor.getText().getSpans(6, 8, UnderlineSpan.class);
		assertThat(underlineSpans.length, Is.is(0));

		text = "Size me!";
		editor.setTextHtml(text);
		editor.setSelection(0, 5);
		editor.toggleStyle(MildlyRichTextEditor.Style.FONT_SIZE, 1.2f);
		RelativeSizeSpan[] sizeSpans = editor.getText().getSpans(0, 5, RelativeSizeSpan.class);
		assertThat(sizeSpans.length, Is.is(1));
		assertThat(sizeSpans[0], Matchers.instanceOf(RelativeSizeSpan.class));
		editor.setSelection(6, text.length());
		sizeSpans = editor.getText().getSpans(6, 8, RelativeSizeSpan.class);
		assertThat(sizeSpans.length, Is.is(0));
	}

	@Test
	public void testRichTextEditorTextWatcher()
	{
		Spannable spannable = new SpannableString("String with bold, italic and underline spans");
		spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 16, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.ITALIC), 18, 24, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		spannable.setSpan(new MildlyRichTextEditor.CustomUnderlineSpan(), 29, 38, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		spannable.setSpan(new RelativeSizeSpan(1.2f), 0, 6, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		editor.setText(spannable);
		MildlyRichTextEditor.RichTextEditorTextWatcher watcher = editor.new RichTextEditorTextWatcher();
		editor.addTextChangedListener(watcher);
		editor.boldButton = new ToggleButton(context);
		editor.boldButton.setChecked(true);
		editor.italicsButton = new ToggleButton(context);
		editor.italicsButton.setChecked(true);
		editor.underlineButton = new ToggleButton(context);
		editor.underlineButton.setChecked(true);
		ToggleButton ten = new ToggleButton(context);
		ten.setText("10");
		ten.setTextOn("10");
		ten.setTextOff("10");
		ToggleButton fourteen = new ToggleButton(context);
		fourteen.setText("14");
		fourteen.setTextOn("14");
		fourteen.setTextOff("14");
		ToggleButton sixteen = new ToggleButton(context);
		sixteen.setText("16");
		sixteen.setTextOn("16");
		sixteen.setTextOff("16");
		editor.fontSizeButtons = Arrays.asList(ten, fourteen, sixteen);
		editor.fontSizeButtons.get(2).setChecked(true);

		watcher.beforeTextChanged(spannable, 0, 38, 1); //addition
		assertThat(editor.boldButton.isChecked(), is(true));
		assertThat(editor.italicsButton.isChecked(), is(true));
		assertThat(editor.underlineButton.isChecked(), is(true));

		editor.setText("abc");
		watcher.beforeTextChanged(spannable, 0, 3, 0); //deletion
		assertThat(editor.boldButton.isChecked(), is(false));
		assertThat(editor.italicsButton.isChecked(), is(false));
		assertThat(editor.underlineButton.isChecked(), is(false));

		editor.setText(spannable);
		Editable e = new SpannableStringBuilder("");
		watcher.afterTextChanged(e);
		assertThat(editor.boldButton.isChecked(), is(false));
		assertThat(editor.italicsButton.isChecked(), is(false));
		assertThat(editor.underlineButton.isChecked(), is(false));

		e = new SpannableStringBuilder(spannable);
		editor.currentRelativeSize = 1.2f;
		editor.setText(spannable);
		Selection.setSelection(spannable, 1, spannable.length());
		editor.setSelection(1, spannable.length());
		watcher.afterTextChanged(e);
		assertThat(editor.currentRelativeSize, is(1.2f));
		RelativeSizeSpan[] spans = e.getSpans(0, spannable.length(), RelativeSizeSpan.class);
		assertThat(spans[0].getSizeChange(), is(1.2f));
	}

	@Test
	public void testSanitizeUnderlineSpan()
	{
		Editable editable = new SpannableStringBuilder();
		editable.append("aaa").setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		editable.append(" ")
				.append("bbb")
				.setSpan(new StyleSpan(Typeface.ITALIC), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		editable.append(" ").append("ccc").setSpan(new UnderlineSpan(), 8, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		editable.append(" ")
				.append("ddd")
				.setSpan(new MildlyRichTextEditor.CustomUnderlineSpan(), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		editable = editor.sanitizeUnderlineSpan(editable);

		assert editable != null;
		CharacterStyle[] styles = editable.getSpans(0, editable.length(), CharacterStyle.class);
		assertThat(styles.length, Is.is(3));
		assertThat(styles[0], Is.is(instanceOf(StyleSpan.class)));
		assertThat(styles[1], Is.is(instanceOf(StyleSpan.class)));
		assertThat(styles[2], Is.is(instanceOf(MildlyRichTextEditor.CustomUnderlineSpan.class)));
	}
}
