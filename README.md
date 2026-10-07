# Devoxx Raffle — Pixoo 64 LED Visuals

Repository submitted for the [Devoxx Raffle](https://devoxx-raffle.cloud.run/#entry-form-panel) "LED Board Visuals" & "Code Repository URL" entry.

## About the Submission

This repository contains the Java-based visual generator using the zero-dependency Jixoo/Pixoo64 library (`RaffleVisualGeneratorApp.java`), designed to produce custom, animated 64x64 pixel art visuals optimized for Divoom Pixoo 64 LED matrix displays.

### Raffle Entries
1. **LED Board Visuals (2x Chances)**: Custom 64x64 animated GIFs generated for the Pixoo 64 matrix display.
2. **Code Repository URL (3x Chances)**: This public repository (`https://github.com/petervanhoute/google-cloud-raffle`), providing the Java source code used to generate the LED board visuals.

## Project Structure

- `src/main/java/io/github/glaforge/jixoo/example/RaffleVisualGeneratorApp.java` — Main generator application rendering custom multi-frame animated LED scenes using Java2D.
- `src/main/java/io/github/glaforge/jixoo/image/` — Core image processing, letterbox fitting, and zero-dependency GIF89a encoding (`GifEncoder`, `PixooImage`, `ImageProcessor`).
- `src/main/java/io/github/glaforge/jixoo/model/` — Lightweight animation model classes (`PixooAnimation`, `PixooFrame`, `RawRgbBuffer`).

## License

Apache License 2.0 — see [LICENSE](LICENSE).
