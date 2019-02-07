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
import android.graphics.Typeface;

/**
 * This class and the techniques used to incorporate FontAwesome are from the tutorial at
 * https://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
 * written by Gianluca Segato, https://tutsplus.com/authors/gianluca-segato
 */

public class FontManager {
    private static final String ROOT = "fonts/";
    public static final String FONTAWESOME_SOLID = ROOT + "fa-solid-900.ttf";
    public static final String FONTAWESOME_REGULAR = ROOT + "fa-regular-400.ttf";

    public static Typeface getTypeface(Context context, String fontName) {
        return Typeface.createFromAsset(context.getAssets(), fontName);
    }
}
