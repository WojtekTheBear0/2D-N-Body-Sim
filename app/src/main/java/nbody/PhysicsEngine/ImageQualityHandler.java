package nbody.PhysicsEngine;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.*;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ImageQualityHandler {
    private static final int CACHE_SIZE = 1024;
    private final ConcurrentHashMap<Point2D, Color> colorCache;
    private WritableImage processedImage;
    private double brightness = 0;
    private double contrast = 1;
    private double saturation = 1;
    private double blurRadius = 0;
    
    public ImageQualityHandler() {
        this.colorCache = new ConcurrentHashMap<>(CACHE_SIZE);
    }

    public void loadAndProcessImage(Image sourceImage, int targetWidth, int targetHeight) {
        if (sourceImage == null) return;
        
        processedImage = new WritableImage(targetWidth, targetHeight);
        PixelWriter pixelWriter = processedImage.getPixelWriter();
        PixelReader sourceReader = sourceImage.getPixelReader();

        // Bicubic interpolation for better quality scaling
        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                double srcX = x * (sourceImage.getWidth() / targetWidth);
                double srcY = y * (sourceImage.getHeight() / targetHeight);
                
                Color interpolatedColor = bicubicInterpolate(sourceReader, srcX, srcY, 
                    sourceImage.getWidth(), sourceImage.getHeight());
                
                // Apply color adjustments
                interpolatedColor = adjustColor(interpolatedColor);
                pixelWriter.setColor(x, y, interpolatedColor);
            }
        }

        if (blurRadius > 0) {
            applyGaussianBlur();
        }
    }

    private Color bicubicInterpolate(PixelReader reader, double x, double y, 
                                   double maxWidth, double maxHeight) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        double dx = x - x0;
        double dy = y - y0;

        Color[][] colors = new Color[4][4];
        for (int i = -1; i <= 2; i++) {
            for (int j = -1; j <= 2; j++) {
                int xi = Math.min(Math.max(x0 + i, 0), (int)maxWidth - 1);
                int yj = Math.min(Math.max(y0 + j, 0), (int)maxHeight - 1);
                colors[i+1][j+1] = reader.getColor(xi, yj);
            }
        }

        // Bicubic interpolation for each color channel
        double red = bicubicPolynomial(colors, dx, dy, c -> c.getRed());
        double green = bicubicPolynomial(colors, dx, dy, c -> c.getGreen());
        double blue = bicubicPolynomial(colors, dx, dy, c -> c.getBlue());
        double alpha = bicubicPolynomial(colors, dx, dy, c -> c.getOpacity());

        return new Color(
            clamp(red, 0, 1),
            clamp(green, 0, 1),
            clamp(blue, 0, 1),
            clamp(alpha, 0, 1)
        );
    }

    private double bicubicPolynomial(Color[][] colors, double dx, double dy,
                                   Function<Color, Double> channelExtractor) {
        double[] tmp = new double[4];
        for (int i = 0; i < 4; i++) {
            tmp[i] = cubicHermite(
                channelExtractor.apply(colors[0][i]),
                channelExtractor.apply(colors[1][i]),
                channelExtractor.apply(colors[2][i]),
                channelExtractor.apply(colors[3][i]),
                dx
            );
        }
        return cubicHermite(tmp[0], tmp[1], tmp[2], tmp[3], dy);
    }

    private double cubicHermite(double p0, double p1, double p2, double p3, double t) {
        double a = -0.5 * p0 + 1.5 * p1 - 1.5 * p2 + 0.5 * p3;
        double b = p0 - 2.5 * p1 + 2 * p2 - 0.5 * p3;
        double c = -0.5 * p0 + 0.5 * p2;
        double d = p1;
        
        return a * t * t * t + b * t * t + c * t + d;
    }

    private Color adjustColor(Color color) {
        double r = clamp(color.getRed() * contrast + brightness, 0, 1);
        double g = clamp(color.getGreen() * contrast + brightness, 0, 1);
        double b = clamp(color.getBlue() * contrast + brightness, 0, 1);
        
        // Saturation adjustment
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        r = clamp(luminance + (r - luminance) * saturation, 0, 1);
        g = clamp(luminance + (g - luminance) * saturation, 0, 1);
        b = clamp(luminance + (b - luminance) * saturation, 0, 1);
        
        return new Color(r, g, b, color.getOpacity());
    }

    private void applyGaussianBlur() {
        if (processedImage == null) return;
        
        int width = (int) processedImage.getWidth();
        int height = (int) processedImage.getHeight();
        PixelReader reader = processedImage.getPixelReader();
        WritableImage blurredImage = new WritableImage(width, height);
        PixelWriter writer = blurredImage.getPixelWriter();
        
        // Generate Gaussian kernel
        int kernelSize = (int) (blurRadius * 2 + 1);
        double[][] kernel = createGaussianKernel(kernelSize, blurRadius / 2);
        
        // Apply convolution
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double red = 0, green = 0, blue = 0, alpha = 0;
                double kernelSum = 0;
                
                // Apply kernel
                for (int ky = -kernelSize/2; ky <= kernelSize/2; ky++) {
                    for (int kx = -kernelSize/2; kx <= kernelSize/2; kx++) {
                        int sampleX = Math.min(Math.max(x + kx, 0), width - 1);
                        int sampleY = Math.min(Math.max(y + ky, 0), height - 1);
                        
                        Color color = reader.getColor(sampleX, sampleY);
                        double kernelValue = kernel[ky + kernelSize/2][kx + kernelSize/2];
                        
                        red += color.getRed() * kernelValue;
                        green += color.getGreen() * kernelValue;
                        blue += color.getBlue() * kernelValue;
                        alpha += color.getOpacity() * kernelValue;
                        kernelSum += kernelValue;
                    }
                }
                
                // Normalize and write pixel
                writer.setColor(x, y, new Color(
                    clamp(red / kernelSum, 0, 1),
                    clamp(green / kernelSum, 0, 1),
                    clamp(blue / kernelSum, 0, 1),
                    clamp(alpha / kernelSum, 0, 1)
                ));
            }
        }
        
        processedImage = blurredImage;
    }

    private double[][] createGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double sum = 0;
        int center = size / 2;
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - center;
                double dy = y - center;
                kernel[y][x] = Math.exp(-(dx*dx + dy*dy) / (2 * sigma * sigma));
                sum += kernel[y][x];
            }
        }
        
        // Normalize kernel
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                kernel[y][x] /= sum;
            }
        }
        
        return kernel;
    }

    public Color getColorForPosition(Point2D position, int width, int height) {
        if (processedImage == null) {
            return Color.SKYBLUE; // Default fallback color
        }

        // Check cache first
        Color cachedColor = colorCache.get(position);
        if (cachedColor != null) {
            return cachedColor;
        }

        int x = (int) clamp(position.getX(), 0, width - 1);
        int y = (int) clamp(position.getY(), 0, height - 1);
        
        Color color = processedImage.getPixelReader().getColor(x, y);
        
        // Cache the color if cache isn't full
        if (colorCache.size() < CACHE_SIZE) {
            colorCache.put(position, color);
        }
        
        return color;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Setters for image adjustments
    public void setBrightness(double brightness) {
        this.brightness = clamp(brightness, -1, 1);
    }

    public void setContrast(double contrast) {
        this.contrast = clamp(contrast, 0, 2);
    }

    public void setSaturation(double saturation) {
        this.saturation = clamp(saturation, 0, 2);
    }

    public void setBlurRadius(double radius) {
        this.blurRadius = clamp(radius, 0, 10);
    }

    public void clearCache() {
        colorCache.clear();
    }
}