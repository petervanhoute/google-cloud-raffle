/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.glaforge.jixoo.image;

import io.github.glaforge.jixoo.model.PixooAnimation;

import java.awt.image.BufferedImage;


public class ImageProcessor {

    
    public enum ScaleMode {
        FIT_CENTER,
        FILL_CROP,
        STRETCH
    }

    
    public static PixooImage resizeAndFit(PixooImage input) {
        return resizeAndFit(input, ScaleMode.FIT_CENTER);
    }

    
    public static PixooImage resizeAndFit(PixooImage input, ScaleMode scaleMode) {
        return input.resizeAndFit(64, 64, scaleMode);
    }

    
    public static PixooAnimation processImage(PixooImage input) {
        return processImage(input, ScaleMode.FIT_CENTER);
    }

    
    public static PixooAnimation processImage(PixooImage input, ScaleMode scaleMode) {
        PixooImage processed = resizeAndFit(input, scaleMode);
        return PixooAnimation.singleImage(processed);
    }

    // --- Convenience methods for JVM users using BufferedImage ---

    
    public static BufferedImage resizeAndFit(BufferedImage input) {
        return resizeAndFit(input, ScaleMode.FIT_CENTER);
    }

    
    public static BufferedImage resizeAndFit(BufferedImage input, ScaleMode scaleMode) {
        PixooImage pix = PixooImage.fromBufferedImage(input);
        return pix.resizeAndFit(64, 64, scaleMode).toBufferedImage();
    }

    
    public static PixooAnimation processImage(BufferedImage input) {
        return processImage(PixooImage.fromBufferedImage(input));
    }
}
