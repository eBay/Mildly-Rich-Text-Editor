package com.ebay.mildlyrichtexteditor;

import android.content.Context;
import android.graphics.Typeface;
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
 * Adapted from https://github.com/agungsijawir/droid-writer/blob/master/DroidWriter/src/hu/scythe/droidwriter/DroidWriterEditText.java
 */
public class MildlyRichTextEditor extends AppCompatEditText
        implements CompoundButton.OnCheckedChangeListener {
    /**
     * Enumeration of the various supported styles
     */
    public enum Style {
        BOLD("Bold"),
        ITALIC("Italic"),
        UNDERLINE("Underline"),
        FONT_SIZE("Font Size"),
        SERIF("Serif"),
        SANS_SERIF("Sans serif"),
        MONOSPACE("Monospace");
        private final String val;

        Style(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    private ImageButton fontSizeButton;
    @VisibleForTesting
    protected List<ToggleButton> fontSizeButtons;
    @VisibleForTesting
    protected PopupWindow popupWindow;
    @VisibleForTesting
    protected ToggleButton boldButton;
    @VisibleForTesting
    protected ToggleButton italicsButton;
    @VisibleForTesting
    protected ToggleButton underlineButton;
    @VisibleForTesting
    protected Float currentRelativeSize = 1f;
    private static final String DEFAULT_ABSOLUTE_SIZE = "14";
    private static final String ABS_SIZE_10 = "10";
    private static final String ABS_SIZE_14 = "14";
    private static final String ABS_SIZE_16 = "16";
    private static final String ABS_SIZE_18 = "18";
    private static final String ABS_SIZE_24 = "24";
    private static final String ABS_SIZE_32 = "32";
    private static final String ABS_SIZE_48 = "48";
    private boolean isEditorEmpty;
    private boolean isDeletion;

    public MildlyRichTextEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        this.addTextChangedListener(new RichTextEditorTextWatcher());
    }

    /**
     * Sets the bold button and its onClick event listener
     *
     * @param button A ToggleButton for the bold style
     */
    public void setBoldToggleButton(ToggleButton button) {
        boldButton = button;
        boldButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                toggleStyle(BOLD, null);
            }
        });
    }

    /**
     * Sets the italics button and its onClick event listener
     *
     * @param button A ToggleButton for the italic style
     */
    public void setItalicsToggleButton(ToggleButton button) {
        italicsButton = button;
        italicsButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                toggleStyle(ITALIC, null);
            }
        });
    }

    /**
     * Sets the underline button and its onClick event listener
     *
     * @param button A ToggleButton for the underline style
     */
    public void setUnderlineToggleButton(ToggleButton button) {
        underlineButton = button;
        underlineButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                toggleStyle(UNDERLINE, null);
            }
        });
    }

    /**
     * Sets the font size button and its onClick event listener. Initializes the PopupWindow
     * containing the various supported font sizes.
     *
     * @param button  An ImageButton for opening the PopupWindow menu to select the font size
     * @param menu    The Menu that pops up when the ImageButton is clicked
     * @param buttons The list of ToggleButtons representing the various supported font sizes
     */
    public void setFontSizeButton(ImageButton button, final View menu, List<ToggleButton> buttons) {
        fontSizeButton = button;
        fontSizeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(fontSizeButton);
            }
        });
        popupWindow = new PopupWindow(fontSizeButton.getContext());
        popupWindow.setFocusable(true);
        popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(menu);
        fontSizeButtons = buttons;
        for (ToggleButton t : fontSizeButtons) {
            if (DEFAULT_ABSOLUTE_SIZE.equals(t.getText().toString()))
                t.setChecked(true); //default

            t.setOnCheckedChangeListener(this);
        }
    }

    /**
     * Callback for the font size ToggleButtons. Because a font size must always be active, it is
     * impossible to uncheck a font size ToggleButton. Dismisses the popup after a font size is
     * selected.
     *
     * @param buttonView The font size ToggleButton that was toggled.
     * @param isChecked  The state of the font size ToggleButton.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            toggleStyle(FONT_SIZE, currentRelativeSize);
            for (ToggleButton t : fontSizeButtons) {
                if (t.getId() != buttonView.getId()) {
                    t.setOnCheckedChangeListener(null);
                    t.setChecked(false);
                    t.setOnCheckedChangeListener(this);
                }
            }
        } else //disallow unchecking
            buttonView.setChecked(true);

        popupWindow.dismiss();
    }

    /**
     * Converts the current Editable in the MildlyRichTextEditor widget to HTML and returns it.
     *
     * @return A string containing the resulting HTML from conversion of the editor's rich text.
     */
    public String getTextHtml() {
        return RichTextEditorUtil.compatToHtml(getText());
    }

    /**
     * Sets the Editable in the MildlyRichTextEditor widget from the provided HTML input.
     *
     * @param html A string containing HTML used to set the Editable text in the editor widget.
     */
    public void setTextHtml(String html) {
        Spanned htmlDescription = RichTextEditorUtil.compatFromHtml(html);
        String trimmedHtmlDescription = htmlDescription.toString().trim();
        setTextKeepState(htmlDescription.subSequence(0, trimmedHtmlDescription.length()));
    }

    /**
     * Triggered when the user has (or has not) selected text within the editor widget. Also
     * triggered when the user types text, i.e. when the cursor has moved. Responsible for
     * setting the style controls based on the styles present in the widget's Editable.
     *
     * @param selStart The start of the user's selection. Equal to the end if the user has not made
     *                 a selection.
     * @param selEnd   The end of the user's selection. Equal to the start if the user has not made a
     *                 selection.
     */
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (isDeletion) {
            isDeletion = false;
            return;
        }

        if (isEditorEmpty) {
            isEditorEmpty = false;
            return;
        }

        if (boldButton == null || italicsButton == null || underlineButton == null ||
                fontSizeButtons == null)
            return;

        boolean boldExists = false;
        boolean italicsExists = false;
        boolean underlinedExists = false;
        Float sizeProportion = null;

        if (selStart > 0 && selStart == selEnd) { //user moved cursor
            CharacterStyle[] styleSpans =
                    getText().getSpans(selStart - 1, selStart, CharacterStyle.class);
            for (CharacterStyle styleSpan : styleSpans) {
                if (styleSpan instanceof StyleSpan) {
                    if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD)
                        boldExists = true;
                    else if (((StyleSpan) styleSpan).getStyle() == Typeface.ITALIC)
                        italicsExists = true;
                    else if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD_ITALIC)
                        boldExists = italicsExists = true;
                } else if (styleSpan instanceof UnderlineSpan)
                    underlinedExists = true;
                else if (styleSpan instanceof RelativeSizeSpan)
                    sizeProportion = ((RelativeSizeSpan) styleSpan).getSizeChange();
            }
        } else { //user selected multiple characters
            CharacterStyle[] styleSpans =
                    getText().getSpans(selStart, selEnd, CharacterStyle.class);
            for (CharacterStyle styleSpan : styleSpans) {
                if (getText().getSpanStart(styleSpan) <= selStart &&
                        getText().getSpanEnd(styleSpan) >= selEnd) {
                    if (styleSpan instanceof StyleSpan) {
                        if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD)
                            boldExists = true;
                        else if (((StyleSpan) styleSpan).getStyle() == Typeface.ITALIC)
                            italicsExists = true;
                        else if (((StyleSpan) styleSpan).getStyle() == Typeface.BOLD_ITALIC)
                            boldExists = italicsExists = true;
                    } else if (styleSpan instanceof UnderlineSpan)
                        underlinedExists = true;
                    else if (styleSpan instanceof RelativeSizeSpan)
                        sizeProportion = ((RelativeSizeSpan) styleSpan).getSizeChange();
                }
            }
        }

        boldButton.setChecked(boldExists);
        italicsButton.setChecked(italicsExists);
        underlineButton.setChecked(underlinedExists);
        if (sizeProportion != null) {
            switch (sizeProportion.toString()) {
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
     *                       not made a selection (cursor moved due to typing or deleting)
     * @param selectionEnd   The end of the user's selection. Equal to the start if the user has not
     *                       made a selection (cursor moved due to typing or deleting).
     * @return true if the user has made a selection (via long press), or false if the user has
     * not made a selection (cursor moved due to typing or deleting).
     */
    @VisibleForTesting
    protected boolean isTextSelected(int selectionStart, int selectionEnd) {
        if (selectionStart > selectionEnd) {
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
     * @param style        The style to toggle.
     * @param relativeSize The size of the font relative to the default absolute size of 14sp.
     */
    @VisibleForTesting
    protected void toggleStyle(Style style, Float relativeSize) {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        boolean sectionSelected = isTextSelected(selStart, selEnd);
        if (!sectionSelected)
            return;

        Spannable str = getText();
        boolean exists = false;
        StyleSpan[] styleSpans;

        Iterator<StyleSpan> iterator;

        switch (style) {
            case BOLD:
                styleSpans = str.getSpans(selStart, selEnd, StyleSpan.class);
                iterator = Arrays.asList(styleSpans).iterator();
                while (iterator.hasNext()) //unset if set
                {
                    StyleSpan styleSpan = iterator.next();
                    if (styleSpan.getStyle() == Typeface.BOLD) {
                        str.removeSpan(styleSpan);
                        exists = true;
                    }
                }

                if (!exists) //set style since it has not been set
                    str.setSpan(new StyleSpan(Typeface.BOLD), selStart, selEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                break;
            case ITALIC:
                styleSpans = str.getSpans(selStart, selEnd, StyleSpan.class);
                iterator = Arrays.asList(styleSpans).iterator();
                while (iterator.hasNext()) {
                    StyleSpan styleSpan = iterator.next();
                    if (styleSpan.getStyle() == Typeface.ITALIC) {
                        str.removeSpan(styleSpan);
                        exists = true;
                    }
                }

                if (!exists)
                    str.setSpan(new StyleSpan(Typeface.ITALIC), selStart, selEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                break;
            case UNDERLINE:
                UnderlineSpan[] spans = str.getSpans(selStart, selEnd, UnderlineSpan.class);
                Iterator<UnderlineSpan> underlineIterator = Arrays.asList(spans).iterator();
                while (underlineIterator.hasNext()) {
                    UnderlineSpan underlineSpan = underlineIterator.next();
                    str.removeSpan(underlineSpan);
                    exists = true;
                }

                if (!exists)
                    str.setSpan(new UnderlineSpan(), selStart, selEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                break;
            case FONT_SIZE:
                CharacterStyle[] sizeSpans = str.getSpans(selStart, selEnd, CharacterStyle.class);
                Iterator<CharacterStyle> sizeIterator = Arrays.asList(sizeSpans).iterator();
                while (sizeIterator.hasNext()) {
                    CharacterStyle sizeSpan = sizeIterator.next();
                    if (sizeSpan instanceof RelativeSizeSpan) {
                        str.removeSpan(sizeSpan);
                        exists = true;
                    }
                }

                if (!exists)
                    str.setSpan(new RelativeSizeSpan(relativeSize), selStart, selEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                break;
        }

        setSelection(selStart, selEnd);
    }

    /**
     * TextWatcher that handles both addition and deletion of text. Removes and sets spans based
     * on the state of the style controls.
     */
    public class RichTextEditorTextWatcher implements TextWatcher {
        private CharacterStyle[] prevStyles;

        /**
         * Handles the case where the user has entered text.
         *
         * @param e The text from the editor widget which includes the change the user has made.
         */
        @Override
        public void afterTextChanged(Editable e) {
            if (TextUtils.isEmpty(e.toString().trim())) {
                boldButton.setChecked(false);
                italicsButton.setChecked(false);
                underlineButton.setChecked(false);
                for (ToggleButton t : fontSizeButtons) {
                    t.setOnCheckedChangeListener(null);
                    if (DEFAULT_ABSOLUTE_SIZE.equals(t.getText().toString()))
                        t.setChecked(true);
                    else
                        t.setChecked(false);
                    t.setOnCheckedChangeListener(MildlyRichTextEditor.this);
                }

                CharacterStyle[] styles = e.getSpans(0, e.length(), CharacterStyle.class);
                for (CharacterStyle style : styles) {
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
                    e.getSpans(position - 1, position, CharacterStyle.class);
            StyleSpan currentBoldSpan = null;
            StyleSpan currentItalicSpan = null;
            UnderlineSpan currentUnderlineSpan = null;
            RelativeSizeSpan appliedSizeSpan = null;

            for (CharacterStyle appliedStyle : appliedStyles) //identify styles applied to entered text
            {
                if (appliedStyle instanceof StyleSpan) {
                    if (((StyleSpan) appliedStyle).getStyle() == Typeface.BOLD)
                        currentBoldSpan = (StyleSpan) appliedStyle;
                    else if (((StyleSpan) appliedStyle).getStyle() == Typeface.ITALIC)
                        currentItalicSpan = (StyleSpan) appliedStyle;
                } else if (appliedStyle instanceof UnderlineSpan)
                    currentUnderlineSpan = (UnderlineSpan) appliedStyle;
                else if (appliedStyle instanceof RelativeSizeSpan)
                    appliedSizeSpan = (RelativeSizeSpan) appliedStyle;
            }

            if (boldButton != null && boldButton.isChecked() && currentBoldSpan == null) {
                // User switched bold style button on and character does not have bold style applied
                // therefore, apply the bold style inclusively (proceeding characters will inherit this style)
                e.setSpan(new StyleSpan(Typeface.BOLD), position - 1, position,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            } else if (boldButton != null && !boldButton.isChecked() && currentBoldSpan != null) {
                // User switched bold style button off and character already has the bold style applied
                // Therefore, remove the old bold style span and define a new style that ends 1 position to the right
                // before the newly entered character
                int boldStart = e.getSpanStart(currentBoldSpan);
                int boldEnd = e.getSpanEnd(currentBoldSpan);
                e.removeSpan(currentBoldSpan);
                if (boldStart <= (position - 1))
                    e.setSpan(new StyleSpan(Typeface.BOLD), boldStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                // Since the old bold style span ends after the current cursor position, we need to define a second
                // newly created style span that begins after the newly entered character and ends at the old span's
                // ending position. Therefore, we split the span:
                if (boldEnd > position)
                    e.setSpan(new StyleSpan(Typeface.BOLD), position, boldEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }

            // Handle italics in the same fashion as above
            if (italicsButton != null && italicsButton.isChecked() && currentItalicSpan == null)
                e.setSpan(new StyleSpan(Typeface.ITALIC), position - 1, position,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            else if (italicsButton != null && !italicsButton.isChecked() &&
                    currentItalicSpan != null) {
                int italicStart = e.getSpanStart(currentItalicSpan);
                int italicEnd = e.getSpanEnd(currentItalicSpan);
                e.removeSpan(currentItalicSpan);
                if (italicStart <= (position - 1)) {
                    e.setSpan(new StyleSpan(Typeface.ITALIC), italicStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                }
                if (italicEnd > position) //split the span
                    e.setSpan(new StyleSpan(Typeface.ITALIC), position, italicEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }

            // Handle underlines in the same fashion as above
            if (underlineButton != null && underlineButton.isChecked() &&
                    currentUnderlineSpan == null)
                e.setSpan(new UnderlineSpan(), position - 1, position,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            else if (underlineButton != null && !underlineButton.isChecked() &&
                    currentUnderlineSpan != null) {
                int underLineStart = e.getSpanStart(currentUnderlineSpan);
                int underLineEnd = e.getSpanEnd(currentUnderlineSpan);
                e.removeSpan(currentUnderlineSpan);
                if (underLineStart <= (position - 1))
                    e.setSpan(new UnderlineSpan(), underLineStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                if (underLineEnd > position) //split the span
                    e.setSpan(new UnderlineSpan(), position, underLineEnd,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }

            //Float selectedRelativeSize = relativeSizes.get(sizeSpinner.getSelectedItemPosition());
            Float selectedRelativeSize = null;
            for (ToggleButton t : fontSizeButtons) {
                if (t.isChecked())
                    selectedRelativeSize = Float.parseFloat(t.getText().toString()) / 14f;
            }

            if (selectedRelativeSize != null &&
                    Float.compare(currentRelativeSize, selectedRelativeSize) != 0 &&
                    appliedSizeSpan == null) {
                // User changed the size and character is not yet set to the selected size proportion
                // therefore, apply the size inclusively (proceeding characters will inherit this style)
                e.setSpan(new RelativeSizeSpan(selectedRelativeSize),
                        position - 1, position, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                currentRelativeSize = selectedRelativeSize;
            } else if (selectedRelativeSize != null &&
                    Float.compare(currentRelativeSize, selectedRelativeSize) != 0 &&
                    appliedSizeSpan != null) {
                // User changed the size and character already is the selected size.
                // Therefore, remove the old size span and define a new size span that ends 1
                // position to the right before the newly entered character
                int sizeStart = e.getSpanStart(appliedSizeSpan);
                int sizeEnd = e.getSpanEnd(appliedSizeSpan);
                e.removeSpan(appliedSizeSpan);
                if (sizeStart <= (position - 1))
                    e.setSpan(new RelativeSizeSpan(currentRelativeSize),
                            sizeStart, position - 1,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                e.setSpan(new RelativeSizeSpan(selectedRelativeSize),
                        position - 1, sizeEnd,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                currentRelativeSize = selectedRelativeSize;
            }
        }

        /**
         * Handles the case where the user has made a deletion.
         *
         * @param s     The CharSequence containing the text prior to the deletion.
         * @param start The starting position of the user's cursor prior to the change.
         * @param count The length of s prior to the user's change.
         * @param after The length of the newly new text replacing count number of characters
         *              after the start, e.g. 1 when the user has typed a single character. 0 when
         *              the user has deleted a single character.
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (after == 0) { //delete
                isDeletion = true;
                Editable e = MildlyRichTextEditor.this.getText();
                this.prevStyles = e.getSpans(start, start + count, CharacterStyle.class);

                if (this.prevStyles.length > 0) {
                    StyleSpan currentBoldSpan = null;
                    StyleSpan currentItalicSpan = null;
                    UnderlineSpan currentUnderlineSpan = null;
                    RelativeSizeSpan appliedSizeSpan = null;

                    boldButton.setChecked(false);
                    italicsButton.setChecked(false);
                    underlineButton.setChecked(false);

                    for (CharacterStyle appliedStyle : this.prevStyles) {
                        if (appliedStyle instanceof StyleSpan) {
                            if (((StyleSpan) appliedStyle).getStyle() == Typeface.BOLD) {
                                boldButton.setChecked(true);
                                currentBoldSpan = (StyleSpan) appliedStyle;
                            } else if (((StyleSpan) appliedStyle).getStyle() == Typeface.ITALIC) {
                                italicsButton.setChecked(true);
                                currentItalicSpan = (StyleSpan) appliedStyle;
                            }
                        } else if (appliedStyle instanceof UnderlineSpan) {
                            underlineButton.setChecked(true);
                            currentUnderlineSpan = (UnderlineSpan) appliedStyle;
                        } else if (appliedStyle instanceof RelativeSizeSpan) {
                            appliedSizeSpan = (RelativeSizeSpan) appliedStyle;
                            String absoluteSize = Integer.toString(
                                    Math.round(appliedSizeSpan.getSizeChange() * 14f));
                            for (ToggleButton t : fontSizeButtons) {
                                if (absoluteSize.equals(t.getText().toString()))
                                    t.setChecked(true);
                            }
                        }
                    }

                    int spanStart;
                    int newSpanEnd;

                    if (currentBoldSpan != null) {
                        spanStart = e.getSpanStart(currentBoldSpan);
                        newSpanEnd = e.getSpanEnd(currentBoldSpan) - 1;
                        e.removeSpan(currentBoldSpan);
                        if (spanStart <= newSpanEnd) {
                            e.setSpan(new StyleSpan(Typeface.BOLD), spanStart, newSpanEnd,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        } else
                            boldButton.setChecked(false);
                    }

                    if (currentItalicSpan != null) {
                        spanStart = e.getSpanStart(currentItalicSpan);
                        newSpanEnd = e.getSpanEnd(currentItalicSpan) - 1;
                        e.removeSpan(currentItalicSpan);
                        if (spanStart <= newSpanEnd)
                            e.setSpan(new StyleSpan(Typeface.ITALIC), spanStart, newSpanEnd,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        else
                            italicsButton.setChecked(false);
                    }

                    if (currentUnderlineSpan != null) {
                        spanStart = e.getSpanStart(currentUnderlineSpan);
                        newSpanEnd = e.getSpanEnd(currentUnderlineSpan) - 1;
                        e.removeSpan(currentUnderlineSpan);
                        if (spanStart <= newSpanEnd)
                            e.setSpan(new UnderlineSpan(), spanStart, newSpanEnd,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        else
                            underlineButton.setChecked(false);
                    }

                    if (appliedSizeSpan != null) {
                        spanStart = e.getSpanStart(appliedSizeSpan);
                        newSpanEnd = e.getSpanEnd(appliedSizeSpan) - 1;
                        e.removeSpan(appliedSizeSpan);
                        if (spanStart <= newSpanEnd) {
                            e.setSpan(new RelativeSizeSpan(appliedSizeSpan.getSizeChange()),
                                    spanStart, newSpanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                            currentRelativeSize = appliedSizeSpan.getSizeChange();
                        }
                    }
                } else {
                    boldButton.setChecked(false);
                    italicsButton.setChecked(false);
                    underlineButton.setChecked(false);
                }
            } else
                isDeletion = false;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //not implemented
        }
    }
}