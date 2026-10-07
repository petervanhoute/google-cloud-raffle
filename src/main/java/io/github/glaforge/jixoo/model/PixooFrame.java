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
package io.github.glaforge.jixoo.model;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;


public record PixooFrame(byte[] rgbData, int delayMs) {

    
    public PixooFrame {
        Objects.requireNonNull(rgbData, "rgbData cannot be null");
        if (rgbData.length != RawRgbBuffer.TOTAL_BYTES) {
            throw new IllegalArgumentException(
                    "RGB buffer must be exactly " + RawRgbBuffer.TOTAL_BYTES + " bytes, but was " + rgbData.length
            );
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("Frame delay cannot be negative");
        }
        rgbData = rgbData.clone();
    }

    @Override
    public byte[] rgbData() {
        return rgbData.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PixooFrame that)) return false;
        return delayMs == that.delayMs && Arrays.equals(rgbData, that.rgbData);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(rgbData) + Integer.hashCode(delayMs);
    }

    @Override
    public String toString() {
        return "PixooFrame[delayMs=" + delayMs + ", rgbBytes=" + rgbData.length + "]";
    }

    
    public static PixooFrame fromPixooImage(io.github.glaforge.jixoo.image.PixooImage image, int delayMs) {
        return new PixooFrame(image.toRawRgb(), delayMs);
    }

    
    public static PixooFrame fromPixooImage(io.github.glaforge.jixoo.image.PixooImage image) {
        return fromPixooImage(image, 100);
    }

    
    public static PixooFrame fromImage(BufferedImage image, int delayMs) {
        return new PixooFrame(RawRgbBuffer.fromImage(image), delayMs);
    }

    
    public static PixooFrame fromImage(BufferedImage image) {
        return fromImage(image, 100);
    }

    
    public String toBase64() {
        return Base64.getEncoder().encodeToString(rgbData);
    }
}
