package me.ericrybarczyk.roadtrippy;

import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.view.WindowManager;

public class FullScreenDialogFragment extends DialogFragment {

    @Override
    public void onResume() {
        // Technique for full-size dialog display is from https://guides.codepath.com/android/Using-DialogFragment#sizing-dialogs
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }
}
