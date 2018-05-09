package com.agapi_android.gumbinas.agapi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.agapi_android.gumbinas.agapi.api.controllers.AgapiController;
import com.agapi_android.gumbinas.agapi.api.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactCreateFormActivity extends AppCompatActivity {

    private AgapiController _agapi;
    private Contact _contact;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_template);
        setTitle(R.string.form_contact_create_title);

        _agapi = new AgapiController(this);
        _contact = new Contact();

        Intent intent = getIntent();
        if (intent.getAction() != null) {
            setTitle(R.string.form_contact_edit_title);
            long contactId = Long.valueOf(intent.getAction());
            for (Contact contact : _agapi.getUserAddressBook().getContacts()) {
                if (contact.id == contactId) {
                    _contact = contact;
                    break;
                }
            }
        }

        ViewGroup formContentLayout = findViewById(R.id.form_content_layout);
        LayoutInflater layoutInflater = getLayoutInflater();
        layoutInflater.inflate(R.layout.form_contact_create, formContentLayout);

        final CheckBox inQueueCheckbox = findViewById(R.id.form_contact_create_checkbox_in_queue);
        inQueueCheckbox.setChecked(_contact.inQueue);

        final TextInputLayout nameTextInputLayout = findViewById(R.id.form_contact_create_input_text_layout_name);
        final TextInputEditText nameEditText = (TextInputEditText) nameTextInputLayout.getEditText();
        assert nameEditText != null;
        nameEditText.setText(_contact.name);
        setErrorMessageAndOrCapitaliseFirstLetter(nameTextInputLayout, nameEditText,
                true, "Įveskite kontakto pavadinimą");

        final TextInputLayout phoneTextInputLayout = findViewById(R.id.form_contact_create_input_text_layout_phone);
        final TextInputEditText phoneEditText = (TextInputEditText) phoneTextInputLayout.getEditText();
        assert phoneEditText != null;
        phoneEditText.setText(_contact.phone);

        TextInputLayout emailTextInputLayout = findViewById(R.id.form_contact_create_input_text_layout_email);
        final TextInputEditText emailEditText = (TextInputEditText) emailTextInputLayout.getEditText();
        assert emailEditText != null;
        emailEditText.setText(_contact.email);

        TextInputLayout personalMessageTextInputLayout = findViewById(R.id.form_contact_create_input_text_layout_personal_message);
        final TextInputEditText personalMessageEditText = (TextInputEditText) personalMessageTextInputLayout.getEditText();
        assert personalMessageEditText != null;
        personalMessageEditText.setText(_contact.personalMessage);

        Button formLeftButton = findViewById(R.id.form_button_left);
        formLeftButton.setText(R.string.form_contact_create_button_left);
        formLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button formRightButton = findViewById(R.id.form_button_right);
        formRightButton.setText(R.string.form_contact_create_button_right);
        formRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate inputs
                List<TextInputLayout> textInputLayoutList = new ArrayList<>();
                textInputLayoutList.add(nameTextInputLayout);
                textInputLayoutList.add(phoneTextInputLayout);

                boolean isAllValid = true;
                for (TextInputLayout til : textInputLayoutList) {
                    TextInputEditText editTextView = (TextInputEditText) til.getEditText();
                    assert editTextView != null;
                    if (editTextView.getText().toString().trim().isEmpty()) {
                        isAllValid = false;
                        til.setErrorEnabled(true);
                        til.setError("* Būtina užpildyti");
                    }
                }
                if (isAllValid) {
                    // Save contact information to local cache
                    _contact.name = nameEditText.getText().toString().trim();
                    _contact.phone = phoneEditText.getText().toString().trim();
                    _contact.email = emailEditText.getText().toString().trim();
                    _contact.personalMessage = personalMessageEditText.getText().toString().trim();
                    _contact.inQueue = inQueueCheckbox.isChecked();

                    // Save contact from local cache to database and close contact creation activity
                    _agapi.getUserAddressBook().saveContact(_contact);
                    finish();
                } else {
                    showAlertDialog(
                            getString(R.string.dialog_error_title),
                            "Užpildykite laukelius pažymėtus žvaigždute (*).");
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /* Set up dispatchTouchEvent handler to register a touch,
         * which will be used to lose focus of the edit text, and to close input i.e. keyboard. */
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = getCurrentFocus();
            if (focusedView instanceof TextInputEditText) {
                Rect outRect = new Rect();
                focusedView.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    focusedView.clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /***
     * Implement TextWatcher interface
     * @param textInputLayout TextInputLayout instance;
     * @param capitalizeFirstChar capitalise first letter in the text;
     * @param errorMessage error message to diplay as an error within TextInputLayout;
     * @return instance of TextWatcher; */
    private TextWatcher getTextCustomWatcher(final TextInputLayout textInputLayout,
                                             final boolean capitalizeFirstChar,
                                             final CharSequence errorMessage) {

        final EditText v = textInputLayout.getEditText();

        return new TextWatcher() {

            private boolean _moveCursorToEnd = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(errorMessage);
                } else {
                    textInputLayout.setErrorEnabled(false);

                    if (capitalizeFirstChar && before == 0 && s.length() == count) {
                        assert v != null;
                        v.removeTextChangedListener(this);
                        String firstChar = String.valueOf(s.charAt(0)).toUpperCase();

                        if (count > 1) {
                            String fullText = firstChar + s.subSequence(1, count).toString();
                            v.setText(fullText);
                        } else {
                            v.setText(firstChar);
                        }
                        _moveCursorToEnd = true;
                        v.addTextChangedListener(this);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (capitalizeFirstChar && _moveCursorToEnd) {
                    try {
                        assert v != null;
                        v.setSelection(s.length());
                        _moveCursorToEnd = false;
                    } catch (IndexOutOfBoundsException iobe) {
                        Log.e("AGAPI_DEBUG", iobe.toString());
                    }
                }
            }
        };
    }

    private TextInputEditText.OnFocusChangeListener getCustomOnFocusChangeListener(
            final TextInputLayout textInputLayout,
            final CharSequence errorMessage) {

        final EditText editTextView = textInputLayout.getEditText();
        assert editTextView != null;

        return new TextInputEditText.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (editTextView.getText().length() == 0) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(errorMessage);
                } else {
                    textInputLayout.setErrorEnabled(false);
                }
            }
        };
    }

    private void setErrorMessageAndOrCapitaliseFirstLetter(TextInputLayout textInputLayout,
                                                           TextInputEditText textInputEditText,
                                                           boolean capitaliseFirstLetter,
                                                           CharSequence errorMessage) {
        textInputEditText.setOnFocusChangeListener(getCustomOnFocusChangeListener(
                textInputLayout,
                errorMessage));
        textInputEditText.addTextChangedListener(getTextCustomWatcher(
                textInputLayout,
                capitaliseFirstLetter,
                errorMessage));
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
