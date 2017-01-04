package com.ebay.mildlyrichtexteditor;

import android.text.Html;
import android.text.Spanned;

import static android.text.Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;

public class RichTextEditorUtil {

    //TODO add/replace with custom HTML parser extending android.text.Html to handle font font size

    public static Spanned compatFromHtml(final String input) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY);
        else
            //noinspection deprecation
            return Html.fromHtml(input);
    }

    public static String compatToHtml(final Spanned input) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return Html.toHtml(input, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        else
            //noinspection deprecation
            return Html.toHtml(input);
    }
}
