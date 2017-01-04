package com.ebay.mildlyrichtexteditor;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Arrays;
import java.util.List;

public class DemoAppActivity extends AppCompatActivity {
    private MildlyRichTextEditor editor;
    private TextView htmlTextView;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.rte_toolbar);
        setSupportActionBar(toolbar);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        htmlTextView = (TextView) bottomSheet.findViewById(R.id.html);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        initializeRichTextEditor();
    }

    private void initializeRichTextEditor() {
        ImageButton fontSizeButton = (ImageButton) findViewById(R.id.size_btn);
        ToggleButton boldButton = (ToggleButton) findViewById(R.id.bold_btn);
        ToggleButton italicButton = (ToggleButton) findViewById(R.id.italic_btn);
        ToggleButton underlineButton = (ToggleButton) findViewById(R.id.underline_btn);
        ToggleButton unorderedList = (ToggleButton) findViewById(R.id.unordered_list_btn);
        unorderedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(v.getContext(), getString(R.string.coming_soon));
            }
        });
        View fontSizeMenu = getLayoutInflater().inflate(R.layout.menu_size_popup_layout, null);
        ToggleButton tenButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_ten);
        ToggleButton fourteenButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_fourteen);
        ToggleButton sixteenButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_sixteen);
        ToggleButton eighteenButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_eighteen);
        ToggleButton twentyFourButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_twenty_four);
        ToggleButton thirtyTwoButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_thirty_two);
        ToggleButton fortyEightButton = (ToggleButton) fontSizeMenu.findViewById(R.id.size_forty_eight);
        List<ToggleButton> sizeToggles = Arrays.asList(tenButton, fourteenButton, sixteenButton,
                eighteenButton, twentyFourButton, thirtyTwoButton, fortyEightButton);

        editor = (MildlyRichTextEditor) findViewById(R.id.rte);
        editor.setBoldToggleButton(boldButton);
        editor.setItalicsToggleButton(italicButton);
        editor.setUnderlineToggleButton(underlineButton);
        editor.setFontSizeButton(fontSizeButton, fontSizeMenu, sizeToggles);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_html:
                hideSoftKeyboard();
                showHtmlBottomSheet();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showHtmlBottomSheet() {
        htmlTextView.setText(editor.getTextHtml());
        bottomSheetBehavior.setPeekHeight(300);
    }
}
