package com.ebay.mildlyrichtexteditor;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;

import com.ebay.mildlyrichtexteditorlibrary.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18)
public class MildlyRichTextEditorUtilTest {
    @Test
    public void testCompatFromHtml() {
        Spanned s = RichTextEditorUtil.compatFromHtml("" +
                "<b>bold input</b> " +
                "<i>italic input</i> ");
        CharacterStyle[] styles = s.getSpans(0, s.length(), CharacterStyle.class);

        assertThat(styles.length, is(2));
        assertThat(styles[0], is(instanceOf(StyleSpan.class)));
        assertThat(((StyleSpan) styles[0]).getStyle(), is(Typeface.BOLD));
        assertThat(styles[1], is(instanceOf(StyleSpan.class)));
        assertThat(((StyleSpan) styles[1]).getStyle(), is(Typeface.ITALIC));
    }

    @Test
    public void testCompatToHtml() {
        Editable e = new SpannableStringBuilder("This is my input");
        StyleSpan bold = new StyleSpan(Typeface.BOLD);
        e.setSpan(bold, 0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        assertThat(e.getSpanStart(bold), is(0));
        assertThat(e.getSpanEnd(bold), is(4));
    }
}
