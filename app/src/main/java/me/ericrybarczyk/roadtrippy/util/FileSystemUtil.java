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

package me.ericrybarczyk.roadtrippy.util;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import me.ericrybarczyk.roadtrippy.maps.MapSettings;
import timber.log.Timber;

public class FileSystemUtil {

    public static void saveMapSnapshotImage(Context context, Bitmap bitmap, String tripId) {

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
            Timber.e(e.getMessage());
        } finally {
            try {
                Objects.requireNonNull(fos).close();
            } catch (IOException e) {
                Timber.e(e.getMessage());
            }
        }
    }

    public static File getPrimaryTripImageFile(Context context, String tripId) {
        File imageDir = context.getDir(MapSettings.DESTINATION_MAP_IMAGE_DIRECTORY, Context.MODE_PRIVATE);
        String tripImageFilename = MapSettings.DESTINATION_MAP_MAIN_PREFIX + tripId + MapSettings.DESTINATION_MAP_IMAGE_EXTENSION;
        return new File(imageDir, tripImageFilename);
    }
}
