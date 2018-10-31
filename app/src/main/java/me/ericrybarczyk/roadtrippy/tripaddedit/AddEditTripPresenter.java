package me.ericrybarczyk.roadtrippy.tripaddedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import me.ericrybarczyk.roadtrippy.persistence.TripDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTripPresenter implements AddEditTripContract.Presenter {

    private TripDataSource tripDataSource;
    private FirebaseUser firebaseUser;
    private AddEditTripContract.View addEditTripView;
    private static final String TAG = AddEditTripPresenter.class.getSimpleName();

    public AddEditTripPresenter(@NonNull TripDataSource dataSource, @NonNull FirebaseUser firebaseUser, @NonNull AddEditTripContract.View view) {
        this.tripDataSource = checkNotNull(dataSource);
        this.firebaseUser = checkNotNull(firebaseUser);
        this.addEditTripView = checkNotNull(view);

        this.addEditTripView.setPresenter(this);
    }

    public void saveMapSnapshotImage(Context context, Bitmap bitmap, String tripId) {

        // calculations to slice a part of the image for use in trip list view
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();
        // original is slightly portrait. Remove 20% horizontal 60% vertical, keep centered remainder. Looks good in List View, Picasso centers to fit.
        int startX = Math.round(currentWidth * 0.1f);
        int startY = Math.round(currentHeight * 0.3f);
        int resizedWidth = currentWidth - (startX * 2);
        int resizedHeight = currentHeight - (startY * 2);

        FileOutputStream fos = null;
        try {
            File imageDir = context.getDir(MapSettings.DESTINATION_MAP_IMAGE_DIRECTORY, Context.MODE_PRIVATE);

            // main image is used on trip detail screen
            File mainFile = new File(imageDir, MapSettings.DESTINATION_MAP_MAIN_PREFIX + tripId+ MapSettings.DESTINATION_MAP_IMAGE_EXTENSION);
            fos = new FileOutputStream(mainFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, MapSettings.MAP_IMAGE_SAVE_QUALITY, fos);

            // resized is a narrow image for use in main trip list screen
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, startX, startY, resizedWidth, resizedHeight);
            File resizedFile = new File(imageDir, MapSettings.DESTINATION_MAP_SLICED_PREFIX + tripId + MapSettings.DESTINATION_MAP_IMAGE_EXTENSION);
            fos = new FileOutputStream(resizedFile);
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, MapSettings.MAP_IMAGE_SAVE_QUALITY, fos);

        } catch (FileNotFoundException e) {
            // in case of file errors, placeholder images will be acceptable
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void start() {

    }
}
