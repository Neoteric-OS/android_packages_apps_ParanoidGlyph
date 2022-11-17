/*
 * Copyright (C) 2022 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.aospa.glyph;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.PreferenceManager;

public final class GlyphSettingsManager {

    private static final String TAG = "GlyphSettingsManager";
    private static final boolean DEBUG = true;

    public static boolean enableGlyph(Context context, boolean enable) {
        return Settings.Secure.putInt(context.getContentResolver(),
                GlyphConstants.GLYPH_ENABLE, enable ? 1 : 0);
    }

    public static boolean isGlyphEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                GlyphConstants.GLYPH_ENABLE, 1) != 0;
    }

    public static int getGlyphBrightness(Context context) {
        int brightness = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(GlyphConstants.GLYPH_BRIGHTNESS, 3);
        switch (brightness) {
            case 1:
                return 102; // 4095/40
            case 2:
                return 682; // 4095/6
            case 3:
                return 1365; // 4095/6
            default:
                return 4095;
        }
    }

    public static boolean isGlyphChargingEnabled(Context context) {
        return isGlyphChargingDotEnabled(context) || isGlyphChargingLevelEnabled(context);
    }

    public static boolean isGlyphChargingDotEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(GlyphConstants.GLYPH_CHARGING_DOT_ENABLE, false);
    }

    public static boolean isGlyphChargingLevelEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(GlyphConstants.GLYPH_CHARGING_LEVEL_ENABLE, false);
    }
}