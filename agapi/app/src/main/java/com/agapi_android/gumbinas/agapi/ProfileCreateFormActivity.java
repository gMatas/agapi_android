package com.agapi_android.gumbinas.agapi;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;

import com.agapi_android.gumbinas.agapi.api.controllers.AgapiController;
import com.agapi_android.gumbinas.agapi.api.models.HealthIssue;
import com.agapi_android.gumbinas.agapi.api.models.Profile;
import com.agapi_android.gumbinas.agapi.api.enumerators.HealthIssueCategory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ProfileCreateFormActivity extends AppCompatActivity {

    private AgapiController _agapi;
    private Profile _userProfile;
    private List<HealthIssue> _userHealthIssues;
    private List<HealthIssueCategory> _selectedHealthIssueCategories;
    private boolean _provideHealthInformation;

    private LayoutInflater _formContentInflater;

    // Common form views
    private ScrollView _formScrollView;
    private Button _formLeftButton;
    private Button _formRightButton;

    // Profile create form views and view groups
    private TextInputLayout _nameTextInputLayout;
    private TextInputEditText _nameTextInputEditText;
    private TextInputLayout _surnameTextInputLayout;
    private TextInputEditText _surnameTextInputEditText;
    private TextInputLayout _birthdateTextInputLayout;
    private TextInputEditText _birthdateTextInputEditText;
    private TextInputLayout _phoneTextInputLayout;
    private TextInputEditText _phoneTextInputEditText;
    private TextInputLayout _emailTextInputLayout;
    private TextInputEditText _emailTextInputEditText;
    private TextInputLayout _streetAddressTextInputLayout;
    private TextInputEditText _streetAddressTextInputEditText;
    private TextInputLayout _cityTextInputLayout;
    private TextInputEditText _cityTextInputEditText;
    private TextInputLayout _countryTextInputLayout;
    private TextInputEditText _countryTextInputEditText;

    // Profile health create form views and view groups
    private LinearLayoutCompat _healthIssueLayout;

    private DatePickerDialog.OnDateSetListener mDateOnDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy / MM / dd");
                    String dateString = dateFormat.format(calendar.getTime());
                    _birthdateTextInputEditText.setText(dateString);
                }
            };

    private TextInputEditText.OnClickListener mDateEditTextOnClickListener =
            new TextInputEditText.OnClickListener() {

        @Override
        public void onClick(View v) {
            Calendar currentCalendar = Calendar.getInstance();
            int year = currentCalendar.get(Calendar.YEAR);
            int month = currentCalendar.get(Calendar.MONTH);
            int date = currentCalendar.get(Calendar.DATE);
            DatePickerDialogFragment datePicker = new DatePickerDialogFragment(
                    ProfileCreateFormActivity.this,
                    mDateOnDateSetListener,
                    year, month, date);
            datePicker.show();
            if (_birthdateTextInputEditText.getText().length() == 0) {
                _birthdateTextInputLayout.setErrorEnabled(true);
                _birthdateTextInputLayout.setError("Įvesktite savo gimimo datą");
            }
        }
    };

    private Button.OnClickListener mCancelButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private Button.OnClickListener mContinueButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // Validate all inputs
            List<TextInputLayout> editTextLayoutList = new ArrayList<>();
            editTextLayoutList.add(_nameTextInputLayout);
            editTextLayoutList.add(_surnameTextInputLayout);
            editTextLayoutList.add(_birthdateTextInputLayout);
            editTextLayoutList.add(_phoneTextInputLayout);
            editTextLayoutList.add(_streetAddressTextInputLayout);
            editTextLayoutList.add(_cityTextInputLayout);
            editTextLayoutList.add(_countryTextInputLayout);

            boolean isAllValid = true;

            for (TextInputLayout til : editTextLayoutList) {
                TextInputEditText editTextView = (TextInputEditText) til.getEditText();
                assert editTextView != null;
                if (editTextView.getText().toString().trim().isEmpty()) {
                    isAllValid = false;
                    til.setErrorEnabled(true);
                    til.setError("* Būtina užpildyti");
                }
            }
            // TODO: remove isallvalid = true.. it's for debugging!
            if (isAllValid = true) {
                // Save profile data to a user profile object
                _userProfile.firstName = _nameTextInputEditText.getText().toString().trim();
                _userProfile.lastName = _surnameTextInputEditText.getText().toString().trim();
                _userProfile.birthDate = _birthdateTextInputEditText.getText().toString().trim();
                _userProfile.phone = _phoneTextInputEditText.getText().toString().trim();
                _userProfile.email = _emailTextInputEditText.getText().toString().trim();
                _userProfile.streetAddress = _streetAddressTextInputEditText.getText().toString().trim();
                _userProfile.city = _cityTextInputEditText.getText().toString().trim();
                _userProfile.country = _countryTextInputEditText.getText().toString().trim();

                // Inflate user health information form layout in place
                // of profile creation form layout
                _formScrollView.removeAllViews();
                _formContentInflater.inflate(R.layout.form_profile_create_health, _formScrollView);

                // Configure inputs for profile information
                configureProfileHealthCreateForm();

            } else {
                // If input is invalid, show a alert dialog to inform the user about it
                showAlertDialog(
                        getString(R.string.dialog_error_title),
                        "Užpildykite laukelius pažymėtus žvaigždute (*).");
            }
        }
    };

    private Button.OnClickListener mBackButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // Save user health issues to local cache
            saveUserHealthIssuesToLocalCache();

            // Change layout to profile creation layout
            _formScrollView.removeAllViews();
            _formContentInflater.inflate(R.layout.form_profile_create, _formScrollView);
            configureProfileCreateForm();
        }
    };

    private Button.OnClickListener mSaveButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            _agapi.saveUserProfile(_userProfile);

            if (_provideHealthInformation) {
                // Save user health issues to local cache
                saveUserHealthIssuesToLocalCache();
                _agapi.getUserHealthInformation().saveHealthIssues(_userHealthIssues);
            } else {
                _userHealthIssues.clear();
                _agapi.getUserHealthInformation().saveHealthIssues(_userHealthIssues);
            }
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_template);
        setTitle(R.string.form_profile_create_title);

        _userProfile = new Profile();
        _provideHealthInformation = false;
        _userHealthIssues = new LinkedList<>();
        _selectedHealthIssueCategories = new LinkedList<>();

        _agapi = new AgapiController(this);

        if (_agapi.isLoaded()) {
            setTitle(R.string.form_profile_edit_title);
            _userProfile = _agapi.getUserProfile();
        }

        if (!_agapi.getUserHealthInformation().isEmpty()) {
            _provideHealthInformation = true;
            _userHealthIssues = _agapi.getUserHealthInformation().getHealthIssues();
            for (HealthIssue issue : _userHealthIssues) {
                _selectedHealthIssueCategories.add(issue.category);
            }
        }

        // Inflate profile creation layout inside form template content container
        _formScrollView = findViewById(R.id.form_content_layout);
        _formContentInflater = getLayoutInflater();
        _formContentInflater.inflate(R.layout.form_profile_create, _formScrollView);

        // Set up left and right buttons
        _formLeftButton = findViewById(R.id.form_button_left);
        _formRightButton = findViewById(R.id.form_button_right);

        // Configure inputs for profile information
        configureProfileCreateForm();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /*
        * Set up dispatchTouchEvent handler to register a touch,
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

    private void configureProfileCreateForm() {

        // Set up left button
        _formLeftButton.setText(R.string.form_profile_create_button_left);
        _formLeftButton.setOnClickListener(mCancelButtonOnClickListener);

        // Set up right button
        _formRightButton.setText(R.string.form_profile_create_button_right);
        _formRightButton.setOnClickListener(mContinueButtonOnClickListener);

        // Set up name text input
        _nameTextInputLayout = findViewById(
                R.id.form_profile_create_input_text_layout_name);
        _nameTextInputEditText = (TextInputEditText) _nameTextInputLayout.getEditText();

        // Set up surname text input
        _surnameTextInputLayout = findViewById(
                R.id.form_profile_create_input_text_layout_surname);
        _surnameTextInputEditText = (TextInputEditText) _surnameTextInputLayout.getEditText();

        // Set up birth date text input
        _birthdateTextInputLayout = findViewById(
                R.id.form_profile_create_input_text_layout_birthdate);
        _birthdateTextInputEditText = (TextInputEditText) _birthdateTextInputLayout.getEditText();

        // Set up phone text input
        _phoneTextInputLayout = findViewById(R.id.form_profile_create_input_text_layout_phone);
        _phoneTextInputEditText = (TextInputEditText) _phoneTextInputLayout.getEditText();

        // Set up email text input
        _emailTextInputLayout = findViewById(R.id.form_profile_create_input_text_layout_email);
        _emailTextInputEditText = (TextInputEditText) _emailTextInputLayout.getEditText();

        // Set up street address text input
        _streetAddressTextInputLayout = findViewById(R.id.form_profile_create_input_text_layout_street_address);
        _streetAddressTextInputEditText = (TextInputEditText) _streetAddressTextInputLayout.getEditText();

        // Set up city text input
        _cityTextInputLayout = findViewById(R.id.form_profile_create_input_text_layout_city);
        _cityTextInputEditText = (TextInputEditText) _cityTextInputLayout.getEditText();

        // Set up country text input
        _countryTextInputLayout = findViewById(R.id.form_profile_create_input_text_layout_country);
        _countryTextInputEditText = (TextInputEditText) _countryTextInputLayout.getEditText();

        // Fill profile creation form with user profile information
        _nameTextInputEditText.setText(_userProfile.firstName);
        _surnameTextInputEditText.setText(_userProfile.lastName);
        _birthdateTextInputEditText.setText(_userProfile.birthDate);
        _phoneTextInputEditText.setText(_userProfile.phone);
        _emailTextInputEditText.setText(_userProfile.email);
        _streetAddressTextInputEditText.setText(_userProfile.streetAddress);
        _cityTextInputEditText.setText(_userProfile.city);
        _countryTextInputEditText.setText(_userProfile.country);

        // Set up listeners for name text input view
        assert _nameTextInputEditText != null;
        setErrorMessageAndOrCapitaliseFirstLetter(
                _nameTextInputLayout, _nameTextInputEditText,
                true, "Įveskite savo vardą");

        // Set up listeners for surname text input view
        assert _surnameTextInputEditText != null;
        setErrorMessageAndOrCapitaliseFirstLetter(
                _surnameTextInputLayout, _surnameTextInputEditText,
                true, "Įveskite savo pavardę");

        // Set up listeners for birthdate text input view
        assert _birthdateTextInputEditText != null;
        _birthdateTextInputEditText.setOnClickListener(mDateEditTextOnClickListener);
        _birthdateTextInputEditText.addTextChangedListener(getTextCustomWatcher(
                _birthdateTextInputLayout,
                false,
                "Įvesktite savo gimimo datą"));

        // Set up listeners for phone text input view
        assert _phoneTextInputEditText != null;
        setErrorMessageAndOrCapitaliseFirstLetter(
                _phoneTextInputLayout, _phoneTextInputEditText,
                false, "Įveskite šio įrenginio telefono numerį");

        // Set up listeners for street_address text input view
        assert _streetAddressTextInputEditText != null;
        setErrorMessageAndOrCapitaliseFirstLetter(
                _streetAddressTextInputLayout, _streetAddressTextInputEditText,
                true, "Įveskite savo gatvės adresą");

        // Set up listeners for city text input view
        assert _cityTextInputEditText != null;
        setErrorMessageAndOrCapitaliseFirstLetter(
                _cityTextInputLayout, _cityTextInputEditText,
                true, "Įveskite savo gyvenamąjį miestą");

        // Set up listeners for country text input view
        assert _countryTextInputEditText != null;
        setErrorMessageAndOrCapitaliseFirstLetter(
                _countryTextInputLayout, _countryTextInputEditText,
                true, "Įveskite savo gyvenamąją šalį");
    }

    private void configureProfileHealthCreateForm() {
        // Set up left button
        _formLeftButton.setText(R.string.form_profile_health_create_button_left);
        _formLeftButton.setOnClickListener(mBackButtonOnClickListener);

        // Set up right button
        _formRightButton.setText(R.string.form_profile_health_create_button_right);
        _formRightButton.setOnClickListener(mSaveButtonOnClickListener);

        // Set up health information layout and switch for it
        _healthIssueLayout = findViewById(R.id.form_profile_health_create_health_issue_layout);
        Switch healthInfoSwitch = findViewById(R.id.form_profile_health_create_health_info_switch);
        if (!healthInfoSwitch.isChecked()) _healthIssueLayout.setVisibility(View.GONE);
        healthInfoSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    _healthIssueLayout.setVisibility(View.VISIBLE);
                    _provideHealthInformation = true;
                }
                else {
                    _healthIssueLayout.setVisibility(View.GONE);
                    _provideHealthInformation = false;
                }
            }
        });
        healthInfoSwitch.setChecked(_provideHealthInformation);

        // Set up health issue add button
        Button healthIssueAddButton = findViewById(R.id.form_profile_health_create_add_health_issue_button);
        healthIssueAddButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHealthIssueSelectionDialog(_healthIssueLayout);
            }
        });

        // Load already selected user health issues and their categories to the layout
        for (HealthIssueCategory category : _selectedHealthIssueCategories) {
            addHealthIssueCategoryViewToLayout(_healthIssueLayout, category);
        }
        loadUserHealthIssuesFromLocalCache();
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

    private void showHealthIssueSelectionDialog(final ViewGroup parentViewGroup) {

        // Get health issues categories names
        List<String> categoriesNamesList = new ArrayList<>();
        for (HealthIssueCategory category : HealthIssueCategory.values()) {
            categoriesNamesList.add(category.getName());
        }
        String[] categoriesNames = new String[categoriesNamesList.size()];
        categoriesNamesList.toArray(categoriesNames);

        final List<HealthIssueCategory> selectedCategories = new ArrayList<>();
        final List<HealthIssueCategory> unselectedCategories = new ArrayList<>();

        boolean[] checkedItems = new boolean[categoriesNames.length];
        for (HealthIssueCategory category : _selectedHealthIssueCategories) {
            int categoryNameIndex = categoriesNamesList.indexOf(category.getName());
            if (categoryNameIndex >= 0)
                checkedItems[categoryNameIndex] = true;
        }

        // Build and show multiple selections dialog to choose health issues
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.form_profile_health_create_dialog_select_title))
                .setMultiChoiceItems(
                        categoriesNames, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        HealthIssueCategory category = HealthIssueCategory.values()[which];
                        if (isChecked) {
                            selectedCategories.add(category);
                            unselectedCategories.remove(category);
                        }
                        else {
                            unselectedCategories.add(category);
                            selectedCategories.remove(category);
                        }
                    }
                }).setPositiveButton(R.string.dialog_button_confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (HealthIssueCategory category : selectedCategories) {
                            // If previously not selected, now this category must be added to selection
                            if (!_selectedHealthIssueCategories.contains(category)) {
                                _selectedHealthIssueCategories.add(category);

                                addHealthIssueCategoryViewToLayout(parentViewGroup, category);
                            }
                        }
                        for (HealthIssueCategory category : unselectedCategories) {
                            // If previously selected, now this category must be removed
                            if (_selectedHealthIssueCategories.contains(category)) {
                                _selectedHealthIssueCategories.remove(category);
                                removeHealthIssueCategoryViewFromLayout(parentViewGroup, category);
                            }
                        }
                    }
                }).setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        selectedCategories.clear();
                        unselectedCategories.clear();
                    }
                }).show();
    }

    private void addHealthIssueCategoryViewToLayout(ViewGroup parentViewGroup, HealthIssueCategory category) {

        TextInputLayout textInputLayout = new TextInputLayout(this);
        TextInputLayout.LayoutParams textInputLayoutParams = new TextInputLayout.LayoutParams(
                TextInputLayout.LayoutParams.MATCH_PARENT, TextInputLayout.LayoutParams.WRAP_CONTENT);
        textInputLayoutParams.setMarginStart(
                (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        textInputLayoutParams.setMarginEnd(
                (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        textInputLayout.setLayoutParams(textInputLayoutParams);
        textInputLayout.setCounterEnabled(true);
        int maxLength = 60;
        textInputLayout.setCounterMaxLength(maxLength);
        textInputLayout.setTag(category);
        parentViewGroup.addView(textInputLayout);

        TextInputEditText textInputEditText = new TextInputEditText(this);
        textInputEditText.setHint(category.getDescription());
        textInputEditText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        textInputEditText.setSingleLine(false);
        textInputEditText.setVerticalScrollBarEnabled(true);
        textInputEditText.setMovementMethod(ScrollingMovementMethod.getInstance());
        textInputEditText.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        textInputEditText.setMaxLines(4);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(maxLength);
        textInputEditText.setFilters(inputFilters);
        textInputLayout.addView(textInputEditText);
    }

    private void removeHealthIssueCategoryViewFromLayout(ViewGroup parentViewGroup, HealthIssueCategory category) {
        View targetView = parentViewGroup.findViewWithTag(category);
        parentViewGroup.removeView(targetView);
    }

    private void saveUserHealthIssuesToLocalCache() {
        // Clear old user health issues cache
//        _userHealthIssues.clear();

        List<HealthIssue> tempHealthIssues = new ArrayList<>();

        // Save user provided health issue information to cache
        for (HealthIssueCategory category : _selectedHealthIssueCategories) {
            TextInputLayout viewGroup = _healthIssueLayout.findViewWithTag(category);
            TextInputEditText editText = (TextInputEditText) viewGroup.getEditText();
            assert editText != null;
            String healthIssueDescription = editText.getText().toString().trim();
            HealthIssue healthIssue = new HealthIssue(category, healthIssueDescription);
            for (HealthIssue userHealthIssue : _userHealthIssues) {
                if (userHealthIssue.category.name().equals(healthIssue.category.name())) {
                    healthIssue.id = userHealthIssue.id;
                }
            }
            tempHealthIssues.add(healthIssue);
        }
        _userHealthIssues.clear();
        _userHealthIssues.addAll(tempHealthIssues);
    }

    private void loadUserHealthIssuesFromLocalCache() {
        // Load user provided health issue information from cache
        for (HealthIssue healthIssue : _userHealthIssues) {
            TextInputLayout viewGroup = _healthIssueLayout.findViewWithTag(healthIssue.category);
            TextInputEditText editText = (TextInputEditText) viewGroup.getEditText();
            assert editText != null;
            editText.setText(healthIssue.description);
        }
    }

    private class DatePickerDialogFragment extends DatePickerDialog {
        private DatePickerDialogFragment(@NonNull Context context, OnDateSetListener listener,
                                        int year, int month, int dayOfMonth) {
            super(context, listener, year, month, dayOfMonth);
        }
    }

}
