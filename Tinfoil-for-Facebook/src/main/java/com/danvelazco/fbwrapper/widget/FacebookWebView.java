/*
 * Copyright (C) 2013 Daniel Velazco
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

package com.danvelazco.fbwrapper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

// TODO: We are going to use our own WebView in the re-write
public class FacebookWebView extends WebView {

    public FacebookWebView(Context context) {
        this(context, null);
    }

    public FacebookWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FacebookWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {

        // Enable scrollbar fading
        setScrollbarFadingEnabled(true);

    }

}