package com.ebay.mildlyrichtexteditor;

import android.content.Context;
import android.graphics.Typeface;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class MildlyRichTextEditorInstrumentationTest {

    private Context context;
    private MildlyRichTextEditor editor;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
        editor = new MildlyRichTextEditor(context, null);
    }

    @Test
    public void testConstructor() {
        assertThat(editor, is(notNullValue()));
        assertThat(editor.getSelectionStart(), is(0));
        assertThat(editor.getSelectionEnd(), is(0));
        assertThat(editor.getText().toString(), isEmptyString());
        assertThat(editor.getTextHtml(), isEmptyString());
    }

    @Test
    public void testSelectionSpannable() {
        Spannable spannable = new SpannableString("This string has no spans");
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        editor.setText(spannable);
        editor.setSelection(0, 4);
        assertThat(editor.getSelectionStart(), is(0));
        assertThat(editor.getSelectionEnd(), is(4));
    }
}
