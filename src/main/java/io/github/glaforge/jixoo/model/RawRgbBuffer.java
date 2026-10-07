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

import java.awt.Color;
import java.awt.image.BufferedImage;


public final class RawRgbBuffer {
    
    public static final int WIDTH = 64;
    
    public static final int HEIGHT = 64;
    
    public static final int BYTES_PER_PIXEL = 3;
    
    public static final int TOTAL_BYTES = WIDTH * HEIGHT * BYTES_PER_PIXEL; // 12,288 bytes

    private RawRgbBuffer() {}

    
    public static byte[] fromRgb(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException(
                    "RGB components must be between 0 and 255. Given: (" + r + ", " + g + ", " + b + ")"
            );
        }
        byte rb = (byte) r;
        byte gb = (byte) g;
        byte bb = (byte) b;
        byte[] rawRgb = new byte[TOTAL_BYTES];
        for (int i = 0; i < TOTAL_BYTES; i += 3) {
            rawRgb[i] = rb;
            rawRgb[i + 1] = gb;
            rawRgb[i + 2] = bb;
        }
        return rawRgb;
    }

    
    public static byte[] fromColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        return fromRgb(color.getRed(), color.getGreen(), color.getBlue());
    }

    
    public static byte[] fromImage(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (image.getWidth() != WIDTH || image.getHeight() != HEIGHT) {
            throw new IllegalArgumentException(
                    "Image dimensions must be exactly 64x64 pixels. Given: " + image.getWidth() + "x" + image.getHeight()
            );
        }

        int[] argbPixels = new int[WIDTH * HEIGHT];
        image.getRGB(0, 0, WIDTH, HEIGHT, argbPixels, 0, WIDTH);

        byte[] rawRgb = new byte[TOTAL_BYTES];
        int index = 0;

        for (int argb : argbPixels) {
            rawRgb[index++] = (byte) ((argb >> 16) & 0xFF); // Red
            rawRgb[index++] = (byte) ((argb >> 8) & 0xFF);  // Green
            rawRgb[index++] = (byte) (argb & 0xFF);         // Blue
        }

        return rawRgb;
    }
}
