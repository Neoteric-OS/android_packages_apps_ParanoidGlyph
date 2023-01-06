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

package co.aospa.glyph.Manager;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import co.aospa.glyph.R;
import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Manager.StatusManager;
import co.aospa.glyph.Utils.FileUtils;

public final class AnimationManager {

    private static final String TAG = "GlyphAnimationManager";
    private static final boolean DEBUG = true;

    private Context mContext;
    private ExecutorService mExecutorService;

    public AnimationManager(Context context) {
        mContext = context;
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    private Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    public void play(String name) {
        play(name, false);
    }

    public void play(String name, boolean wait) {
        if (DEBUG) Log.d(TAG, "Playing animation | name: " + name + " | waiting: " + Boolean.toString(wait));

        if (StatusManager.isAllLedActive()) {
            if (DEBUG) Log.d(TAG, "All LEDs are active, exiting animation | name: " + name);
            return;
        }

        if (!wait && StatusManager.isAnimationActive()) {
            if (DEBUG) Log.d(TAG, "There is already an animation playing, exiting as there is no need to wait | name: " + name);
            return;
        }

        playCsv(name, wait);
    }

    private void playCsv(String name, boolean wait) {
        submit(() -> {
            if (wait && StatusManager.isAnimationActive()) {
                if (DEBUG) Log.d(TAG, "There is already an animation playing, wait | name: " + name);
                while (StatusManager.isAnimationActive()) {};
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    mContext.getResources().openRawResource(mContext.getResources().getIdentifier("anim_"+name, "raw", mContext.getPackageName()))))) {
                StatusManager.setAnimationActive(true);
                while (reader.readLine() != null) {
                    final String[] split = reader.readLine().split(";");
                    FileUtils.writeLine(Constants.CAMERARINGLEDPATH, (Float.parseFloat(split[0]) / 100 ) * Constants.BRIGHTNESS);
                    FileUtils.writeLine(Constants.CENTERRINGLEDPATH, (Float.parseFloat(split[1]) / 100 ) * Constants.BRIGHTNESS);
                    FileUtils.writeLine(Constants.EXCLAMATIONBARLEDPATH, (Float.parseFloat(split[2]) / 100 ) * Constants.BRIGHTNESS);
                    FileUtils.writeLine(Constants.EXCLAMATIONDOTLEDPATH, (Float.parseFloat(split[3]) / 100 ) * Constants.BRIGHTNESS);
                    FileUtils.writeLine(Constants.SLANTLEDPATH, (Float.parseFloat(split[4]) / 100 ) * Constants.BRIGHTNESS);
                    Thread.sleep(10);
                }
            } catch (IOException | NumberFormatException e) {
                if (DEBUG) Log.d(TAG, "Exception while playing animation | name: " + name);
            } catch (InterruptedException e) {
                if (DEBUG) Log.d(TAG, "Exception while playing animation, interrupted | name: " + name);
            } finally {
                if (DEBUG) Log.d(TAG, "Done playing animation | name: " + name);
                FileUtils.writeLine(Constants.CAMERARINGLEDPATH, 0);
                FileUtils.writeLine(Constants.CENTERRINGLEDPATH, 0);
                FileUtils.writeLine(Constants.EXCLAMATIONBARLEDPATH, 0);
                FileUtils.writeLine(Constants.EXCLAMATIONDOTLEDPATH, 0);
                FileUtils.writeLine(Constants.SLANTLEDPATH, 0);
                StatusManager.setAnimationActive(false);
            }
        });
    }
}