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
package io.github.glaforge.jixoo.example;

import io.github.glaforge.jixoo.image.GifEncoder;
import io.github.glaforge.jixoo.image.ImageProcessor;
import io.github.glaforge.jixoo.image.PixooImage;
import io.github.glaforge.jixoo.model.PixooAnimation;
import io.github.glaforge.jixoo.model.PixooFrame;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class RaffleVisualGeneratorApp {

    private static final int SIZE = 64;
    private static final int FRAME_COUNT = 30;
    private static final int DELAY_MS = 80;
    private static final int VISUAL_COUNT = 3;
    private static final String[] SUPPORTED_EXTENSIONS = {"png", "jpg", "jpeg", "gif", "bmp"};

    private static final Path INPUT_DIR = Path.of("raffle-input");
    private static final Path OUTPUT_DIR = Path.of("raffle-output");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(INPUT_DIR);

        if (ensurePlaceholderInputs()) {
            System.out.println("No input images found in '" + INPUT_DIR + "/' - created " + VISUAL_COUNT
                    + " starter placeholders there.");
            System.out.println("Replace 'visual-1', 'visual-2' and 'visual-3' in " + INPUT_DIR
                    + "/ with your own square artwork (png/jpg/gif/bmp), then re-run this app.");
            System.out.println();
        }

        int generated = 0;
        for (int i = 1; i <= VISUAL_COUNT; i++) {
            Optional<Path> input = findInput(i);
            if (input.isEmpty()) {
                System.out.println("Skipping visual-" + i + ": no file named 'visual-" + i
                        + ".<ext>' found in " + INPUT_DIR + "/");
                continue;
            }
            generateFromInput(input.get(), i);
            generated++;
        }

        System.out.println();
        if (generated == 0) {
            System.out.println("No visuals were generated. Add at least one 'visual-N' file to " + INPUT_DIR + "/.");
            return;
        }
        System.out.println("Done! Generated " + generated + " visual(s) under " + OUTPUT_DIR.toAbsolutePath());
        System.out.println("Pick up to 3 of the 'raffle-visual.gif' files and upload them to the raffle's");
        System.out.println("\"LED Board Visuals\" field, and paste this repository's URL into");
        System.out.println("\"Code Repository URL\" since it's the code used to generate them.");
    }

    
    private static Optional<Path> findInput(int index) throws IOException {
        for (String ext : SUPPORTED_EXTENSIONS) {
            Path candidate = INPUT_DIR.resolve("visual-" + index + "." + ext);
            if (Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    
    private enum SceneStyle {
        MATRIX_PORTAL,
        JAVA_ROBOT_STAGE,
        CODER_HYPERDRIVE
    }

    
    private static void generateFromInput(Path inputFile, int index) throws IOException {
        System.out.println("Generating visual-" + index + " from " + inputFile + " ...");

        BufferedImage source = ImageIO.read(inputFile.toFile());
        if (source == null) {
            throw new IOException("Could not decode image file: " + inputFile
                    + " (unsupported format - use PNG, JPG, GIF, or BMP)");
        }

        PixooImage fitted = toPixooImage(source).resizeAndFit(SIZE, SIZE, ImageProcessor.ScaleMode.FILL_CROP);

        SceneStyle scene = SceneStyle.values()[(index - 1) % SceneStyle.values().length];
        System.out.println("  scene: " + scene);

        List<PixooFrame> pixooFrames = new ArrayList<>(FRAME_COUNT);
        for (int f = 0; f < FRAME_COUNT; f++) {
            pixooFrames.add(renderScene(fitted, scene, f).toFrame(DELAY_MS));
        }
        PixooAnimation animation = new PixooAnimation(pixooFrames);

        Path outDir = OUTPUT_DIR.resolve("visual-" + index);
        Files.createDirectories(outDir);

        Path gifPath = outDir.resolve("raffle-visual.gif");
        GifEncoder.encode(animation, gifPath);
        System.out.println("  -> " + gifPath + " (" + Files.size(gifPath) + " bytes, 64x64, square)");
    }

    
    private static PixooImage renderScene(PixooImage base, SceneStyle scene, int frame) {
        BufferedImage canvas = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        double phase = (double) frame / FRAME_COUNT;
        double angle = phase * Math.PI * 2;
        BufferedImage artwork = toBufferedImage(base);
        switch (scene) {
            case MATRIX_PORTAL -> renderMatrixPortal(g, artwork, phase, angle, frame);
            case JAVA_ROBOT_STAGE -> renderJavaRobotStage(g, artwork, phase, angle, frame);
            case CODER_HYPERDRIVE -> renderCoderHyperdrive(g, artwork, phase, angle, frame);
        }
        g.dispose();
        return toPixooImage(canvas);
    }

    private static void renderMatrixPortal(Graphics2D g, BufferedImage artwork, double phase, double angle, int frame) {
        g.drawImage(artwork, 0, 0, null);

        // Layer 2: Structured Matrix rain in background (masked so nothing crosses face/body/laptop)
        g.setClip(0, 0, SIZE, SIZE);
        // Exclude face/torso/laptop mask: x=18..44, y=12..55 and x=33..59, y=38..57
        // We can draw rain columns outside protected bounds or use clip
        int[] columns = {3, 7, 16, 20, 33, 38, 45, 55, 61};
        int[] columnPhases = {0, 7, 13, 3, 18, 9, 22, 5, 15};
        g.setFont(new Font("Monospaced", Font.BOLD, 6));
        for (int i = 0; i < columns.length; i++) {
            int cx = columns[i];
            int p = columnPhases[i];
            int headY = (int) ((frame + p) % SIZE);
            for (int trail = 0; trail < 3; trail++) {
                int y = Math.floorMod(headY - trail * 5, SIZE);
                // Check if inside protected mask
                boolean inFaceTorso = (cx >= 18 && cx <= 44 && y >= 12 && y <= 55);
                boolean inLaptop = (cx >= 33 && cx <= 59 && y >= 38 && y <= 57);
                if (!inFaceTorso && !inLaptop) {
                    g.setColor(trail == 0 ? new Color(57, 255, 69, 220) : new Color(22, 184, 45, 140));
                    g.drawString("0", cx, y);
                }
            }
        }
        g.setClip(null);

        // Layer 3: Symbol data flicker on symbols
        // Upper-left <> (frames 0-5), upper-right {} (7-12), right <> (14-19), lower-right {} (21-26)
        g.setColor(new Color(160, 255, 121, 190));
        if (frame >= 0 && frame <= 5) {
            g.fillRect(10, 10, 3, 1);
        } else if (frame >= 7 && frame <= 12) {
            g.fillRect(42, 8, 1, 3);
        } else if (frame >= 14 && frame <= 19) {
            g.fillRect(52, 18, 2, 2);
        } else if (frame >= 21 && frame <= 26) {
            g.fillRect(52, 32, 2, 2);
        }

        // Layer 4: Laptop execution sweep along top edge of laptop (x=37..56, y=40)
        if (frame <= 14) {
            double progress = 0.5 - 0.5 * Math.cos(Math.PI * frame / 14.0);
            int sweepX = 37 + (int) Math.round(19 * progress);
            g.setColor(new Color(181, 255, 138, 240));
            g.fillRect(sweepX, 40, 2, 1);
        }

        // Layer 5: Single keystroke on typing hand patch (x=28..31, y=49..52) during frames 11-13
        if (frame >= 11 && frame <= 13) {
            g.drawImage(artwork, 28, 49 + 1, 32, 53 + 1, 28, 49, 32, 53, null);
        }

        // Layer 6: Desk reflection sweep at y=57..60 traveling x=5..55 during frames 6-23
        if (frame >= 6 && frame <= 23) {
            double t = (double) (frame - 6) / 17.0;
            int sweepX = 5 + (int) Math.round(50 * (0.5 - 0.5 * Math.cos(Math.PI * t)));
            g.setColor(new Color(33, 201, 59, (int) Math.round(Math.sin(Math.PI * t) * 76)));
            g.fillRect(sweepX, 58, 6, 1);
        }
    }

    private static void renderJavaRobotStage(Graphics2D g, BufferedImage artwork, double phase, double angle, int frame) {
        g.drawImage(artwork, 0, 0, null);

        // Layer A: Moving cinema spotlights behind robot
        g.setComposite(AlphaComposite.SrcOver.derive(0.22f));
        double sinVal = Math.sin(angle);
        int leftOffset = (int) Math.round(3 * sinVal);
        int rightOffset = (int) Math.round(3 * Math.sin(angle + Math.PI));
        int centerOffset = (int) Math.round(1.5 * Math.sin(angle * 2));

        g.setColor(new Color(255, 110, 15));
        g.fillPolygon(new int[]{13, 17 + leftOffset, 29 + leftOffset}, new int[]{4, 36, 36}, 3);
        g.fillPolygon(new int[]{50, 38 + rightOffset, 51 + rightOffset}, new int[]{4, 36, 36}, 3);
        g.setColor(new Color(125, 55, 255));
        g.fillPolygon(new int[]{31, 26 + centerOffset, 37 + centerOffset}, new int[]{3, 36, 36}, 3);
        g.setComposite(AlphaComposite.SrcOver);

        // Layer B: Robot presenter arm motion (x=27..32, y=27..34) offset sequence: 0 -> 1 -> 2 -> 1 -> 0
        int armDy = 0;
        if (frame >= 6 && frame <= 10) armDy = -1;
        else if (frame >= 11 && frame <= 15) armDy = -2;
        else if (frame >= 16 && frame <= 20) armDy = -1;
        if (armDy != 0) {
            // Draw original background patch over arm or draw shifted arm overlay
            g.drawImage(artwork, 27, 27 + armDy, 33, 35 + armDy, 27, 27, 33, 35, null);
        }

        // Layer C: Antenna bulb motion at approx (21,18) - horizontal shift ±1 px and brightness pulse
        int antennaDx = (frame >= 8 && frame <= 14) ? 1 : ((frame >= 23 && frame <= 29) ? -1 : 0);
        g.setColor(((frame / 2) % 2 == 0) ? new Color(255, 176, 32) : new Color(255, 122, 0));
        g.fillRect(21 + antennaDx, 18, 2, 2);

        // Layer D: Java steam animation (x=37..47, y=17..27) three rising strands
        for (int strand = 0; strand < 3; strand++) {
            int strandOffset = strand * 5;
            int rise = (frame + strandOffset) % 15 / 5;
            if (rise > 0) {
                int sx = 39 + strand * 3;
                int sy = 25 - rise * 2;
                g.setColor(new Color(255, 160, 32));
                g.fillRect(sx, sy, 2, 2);
            }
        }

        // Layer E: Stage edge lights travelling highlight (y=38..40, x=13..52)
        int stageX = 14 + ((frame * 2) % 36);
        g.setColor(new Color(255, 176, 32));
        g.fillRect(stageX, 39, 4, 1);

        // Layer F: Cinema aisle lights (y=43..63, x=52..63) sequential progress
        int[] stairY = {44, 48, 52, 56, 60};
        for (int s = 0; s < stairY.length; s++) {
            boolean bright = ((frame / 3 + s) % 5) == 0;
            g.setColor(bright ? new Color(255, 176, 32) : new Color(200, 90, 0));
            g.fillRect(58 + (s % 2), stairY[s], 2, 1);
        }
    }

    private static void renderCoderHyperdrive(Graphics2D g, BufferedImage artwork, double phase, double angle, int frame) {
        g.drawImage(artwork, 0, 0, null);

        // 1. IDE Code animation inside main IDE panel (x=3..30, y=6..26)
        int codeDx = (frame >= 0 && frame <= 7) ? (int) Math.round(Math.sin(frame / 8.0 * Math.PI) * 2)
                : ((frame >= 26 && frame <= 29) ? 0 : 0);
        if (codeDx > 0) {
            g.setColor(new Color(255, 154, 34, 180));
            g.fillRect(13, 11, codeDx, 1);
            g.fillRect(16, 15, codeDx, 1);
        }

        // 2. Laptop cursor at x=45, y=49, size 1x3 px
        boolean cursorVisible = switch (frame / 4) {
            case 0, 2, 4 -> true;
            default -> (frame >= 16);
        };
        if (cursorVisible) {
            g.setColor(new Color(255, 181, 46, frame == 29 ? 100 : 220));
            g.fillRect(45, 49, 1, 3);
        }

        // 3. Typing hand patch (x=23..33, y=44..52) tap -> release -> tap -> release
        int typeDy = 0;
        if ((frame >= 4 && frame <= 6) || (frame >= 10 && frame <= 12)) {
            typeDy = 1;
        }
        if (typeDy > 0) {
            g.drawImage(artwork, 23, 44 + typeDy, 34, 53 + typeDy, 23, 44, 34, 53, null);
        }

        // 4. Raised hand "execute" motion patch (x=42..55, y=26..42)
        int handDy = 0;
        if (frame >= 8 && frame <= 10) handDy = -1;
        else if (frame >= 11 && frame <= 13) handDy = -2;
        else if (frame >= 14 && frame <= 16) handDy = -1;
        if (handDy != 0) {
            g.drawImage(artwork, 42, 26 + handDy, 56, 43 + handDy, 42, 26, 56, 43, null);
        }

        // 5. Execution energy projectile (traveling from fingertips (50,28) to Java logo (51,23) during frames 13-19)
        if (frame >= 13 && frame <= 19) {
            int px = 50 + (frame >= 15 ? 1 : 0);
            int py = 28 - (frame - 13);
            g.setColor(new Color(255, 242, 160));
            g.fillRect(px, py, 1, 1);
            g.setColor(new Color(255, 155, 35, 130));
            g.fillRect(px + 1, py, 1, 1);
        }

        // 6. Java logo reaction highlight (frames 18-21)
        if (frame >= 18 && frame <= 21) {
            int hy = switch (frame) {
                case 18 -> 23;
                case 19 -> 20;
                case 20 -> 15;
                default -> 10;
            };
            g.setColor(new Color(255, 244, 176, switch (frame) { case 18 -> 120; case 19 -> 210; case 20 -> 160; default -> 60; }));
            g.fillRect(49, hy, 5, 2);
        }

        // 7. Steam movement (frames 16-24)
        int steamDy = 0;
        if (frame >= 16 && frame <= 18) steamDy = -1;
        else if (frame >= 19 && frame <= 21) steamDy = -2;
        else if (frame >= 22 && frame <= 24) steamDy = -1;
        if (steamDy != 0) {
            g.drawImage(artwork, 45, 5 + steamDy, 59, 15 + steamDy, 45, 5, 59, 15, null);
        }

        // 8. Success sweep across code row in main IDE (frames 20-24)
        if (frame >= 20 && frame <= 24) {
            int sweepX = 12 + (frame - 20) * 3;
            g.setColor(new Color(255, 208, 90, 180));
            g.fillRect(sweepX, 16, 3, 1);
        }
    }

    private static void drawZoomed(Graphics2D g, BufferedImage image, double scale, int offsetX, int offsetY) {
        int drawSize = (int) Math.ceil(SIZE * scale);
        int position = (SIZE - drawSize) / 2;
        g.drawImage(image, position + offsetX, position + offsetY,
                position + offsetX + drawSize, position + offsetY + drawSize,
                0, 0, image.getWidth(), image.getHeight(), null);
    }

    private static void overlay(Graphics2D g, Color color) {
        g.setColor(color);
        g.fillRect(0, 0, SIZE, SIZE);
    }

    private static void drawPixelLabel(Graphics2D g, String text, int x, int y, Color foreground, Color background) {
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(background);
        g.fillRect(x - 2, y, metrics.stringWidth(text) + 4, 10);
        g.setColor(foreground);
        g.drawString(text, x, y + 8);
    }

    
    private static boolean ensurePlaceholderInputs() throws IOException {
        boolean anyExisting = false;
        for (int i = 1; i <= VISUAL_COUNT; i++) {
            if (findInput(i).isPresent()) {
                anyExisting = true;
                break;
            }
        }
        if (anyExisting) {
            return false;
        }

        String[] labels = {"ONE", "TWO", "THREE"};
        Color[] colors = {new Color(0x1565C0), new Color(0x2E7D32), new Color(0xAD1457)};
        for (int i = 1; i <= VISUAL_COUNT; i++) {
            BufferedImage placeholder = renderPlaceholder(labels[i - 1], colors[i - 1]);
            Path path = INPUT_DIR.resolve("visual-" + i + ".png");
            ImageIO.write(placeholder, "png", path.toFile());
        }
        return true;
    }

    private static BufferedImage renderPlaceholder(String label, Color background) {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(background);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setColor(background.brighter());
        g.setStroke(new java.awt.BasicStroke(3f));
        g.drawRect(2, 2, SIZE - 5, SIZE - 5);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int tx = (SIZE - fm.stringWidth(label)) / 2;
        int ty = ((SIZE - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(label, tx, ty);

        g.dispose();
        return img;
    }

    
    private static PixooImage toPixooImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
        return new PixooImage(w, h, pixels);
    }

    
    private static BufferedImage toBufferedImage(PixooImage image) {
        BufferedImage buffered = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
        buffered.setRGB(0, 0, image.width(), image.height(), image.argbPixels(), 0, image.width());
        return buffered;
    }

}
