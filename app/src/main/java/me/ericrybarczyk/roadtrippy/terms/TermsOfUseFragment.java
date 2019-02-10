/*
 * Copyright (C) 2018 Eric Rybarczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.ericrybarczyk.roadtrippy.terms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ericrybarczyk.roadtrippy.R;
import me.ericrybarczyk.roadtrippy.triplist.TripListActivity;
import me.ericrybarczyk.roadtrippy.util.ArgumentKeys;

import static com.google.common.base.Preconditions.checkNotNull;

public class TermsOfUseFragment extends Fragment {

    @BindView(R.id.terms_open_statement_text) protected TextView termsIntroText;
    @BindView(R.id.accept_terms_button) protected Button acceptTermsButton;
    @BindView(R.id.cancel_terms_button) protected Button cancelButton;
    @BindView(R.id.terms_full_content_webview) protected WebView termsFullContent;

    private boolean userHasCurrentAcceptance;

    public static TermsOfUseFragment newInstance() {
        return new TermsOfUseFragment();
    }

    public TermsOfUseFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userHasCurrentAcceptance = getUserCurrentAcceptance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_terms_of_use, container, false);
        ButterKnife.bind(this, rootView);

        if (userHasCurrentAcceptance) { // set display to Revoke wording
            termsIntroText.setText(getString(R.string.terms_revoke_text));
            acceptTermsButton.setText(getString(R.string.terms_continue_button_label));
            cancelButton.setText(getString(R.string.terms_revoke_button_label));
        }

        try {
            termsFullContent.loadData(readHtmlFileAsString(),"text/html", "UTF-8");
        } catch (IOException e) {
            // can't continue if this content fails to display
            e.printStackTrace();
        }

        acceptTermsButton.setOnClickListener(v -> {
            Context context = checkNotNull(getContext());
            saveTermsConditionsResult(context, true);
            // go back to using the app
            startActivity(new Intent(context, TripListActivity.class));
        });

        cancelButton.setOnClickListener(v -> {
            Context context = checkNotNull(getContext());
            saveTermsConditionsResult(context, false);
            // effectively close the app when they decline or revoke
            Intent systemHome = new Intent(Intent.ACTION_MAIN);
            systemHome.addCategory(Intent.CATEGORY_HOME);
            systemHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(systemHome);
            getActivity().finish();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        userHasCurrentAcceptance = getUserCurrentAcceptance();
    }

    private boolean getUserCurrentAcceptance() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(ArgumentKeys.KEY_USER_ACCEPTED_TERMS_CONDITIONS, false);
    }

    private void saveTermsConditionsResult(Context context, boolean userDidAcceptTerms) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ArgumentKeys.KEY_USER_ACCEPTED_TERMS_CONDITIONS, userDidAcceptTerms);
        editor.apply();
    }

    private String readHtmlFileAsString() throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.legal_language);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String htmlContent;
        int c;

        c = inputStream.read();
        while (c != -1) {
            outputStream.write(c);
            c = inputStream.read();
        }
        inputStream.close();
        htmlContent = outputStream.toString();

        return htmlContent;
    }
}
