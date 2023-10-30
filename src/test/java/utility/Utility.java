package utility;

import com.formdev.flatlaf.FlatLightLaf;
import examples.mvvm.SomeSettingsView;
import examples.mvvm.SomeSettingsViewModel;
import swingtree.UI;
import swingtree.UIForAnySwing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Utility {

    public enum LaF {
        DEFAULT, NIMBUS, FLAT_BRIGHT
    }

    public static void setLaF(LaF lookAndFeel) {
        switch ( lookAndFeel ) {
            case DEFAULT: break;
            case FLAT_BRIGHT:
                FlatLightLaf.setup();
            default:
                try {
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if (lookAndFeel.name().equalsIgnoreCase(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
        }

    }

    public static BufferedImage loadImage(String location) {
        try {
            InputStream stream = Utility.class.getClassLoader().getResourceAsStream(location);
            assert stream != null;
            return javax.imageio.ImageIO.read(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Query
    {
        private final Component _current;
        private final Map<String, java.util.List<Component>> _tree = new LinkedHashMap<>();

        public Query(Component current) { _current = current; }

        public Query(UIForAnySwing ui) {
            this(ui.getComponent());
        }

        public <C extends Component> Optional<C> find(Class<C> type, String id) {
            List<C> found = findAll(type, id);
            if ( found.isEmpty() )
                return Optional.empty();
            else
                return Optional.of(found.get(0));
        }

        public <C extends Component> List<C> findAll(Class<C> type, String id) {
            if ( !_tree.containsKey(id) ) {
                _tree.clear();
                Component root = _traverseUpwards(_current);
                _traverseDownwards(root);
            }
            return _tree.getOrDefault(id, new ArrayList<>())
                    .stream()
                    .filter( c -> type.isAssignableFrom(c.getClass()) )
                    .map( c -> (C) c )
                    .collect(Collectors.toList());
        }

        public List<JComponent> findAll(String id) {
            return findAll(JComponent.class, id);
        }

        private Component _traverseUpwards(Component component) {
            Container parent = component.getParent();
            if ( parent != null )
                return _traverseUpwards(parent);
            else
                return component;
        }

        private void _traverseDownwards(Component cmp) {
            // Add this component
            if ( cmp != null ) {
                List<Component> found = _tree.computeIfAbsent(cmp.getName(), k -> new ArrayList<>());
                if ( !found.contains(cmp) )
                    found.add(cmp);
            }
            Container container = (Container) cmp;
            if( container == null ) return; // Not a container, return
            // Go visit and add all children
            for ( Component subComponent : container.getComponents() )
                _traverseDownwards(subComponent);
        }
    }

    public static BufferedImage offscreenRender(Component component) {
        JWindow f = new JWindow();
        f.add(component);
        f.pack();
        try {
            UI.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        component.paint(g2d);
        g2d.dispose();
        return image;
    }

    public static BufferedImage renderSingleComponent(Component component) {
        int w = Math.max(component.getWidth(), component.getPreferredSize().width);
        int h = Math.max(component.getHeight(), component.getPreferredSize().height);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        component.paint(g2d);
        g2d.dispose();
        return image;
    }

    /**
     *  This is used to make UI snapshots for testing purposes.
     */
    public static void main(String[] args) {
        SomeSettingsView ui = new SomeSettingsView(new SomeSettingsViewModel());
        JWindow f = new JWindow();
        f.add(ui);
        f.pack();
        safeUIAsImage(ui, "src/test/resources/snapshots/views/vertical-settings-UI.png");
        //BufferedImage image = offscreenRender(ui);
        //JFrame frame = new JFrame();
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        //frame.pack();
        //frame.setVisible(true);
        System.exit(0);
    }

    public static void safeUIAsImage(JComponent ui, String path) {
        BufferedImage image = offscreenRender(ui);
        safeUIImage( image, path );
    }

    public static void safeUIImage(BufferedImage image, String path) {
        try {
            if ( !new File(path).exists() ) {
                File parent = new File(path).getParentFile();
                if ( !parent.exists() )
                    parent.mkdirs();
            }
            javax.imageio.ImageIO.write(image, "png", new java.io.File(path));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static double similarityBetween(JComponent ui, String imageFile) {
        return similarityBetween(ui, imageFile, 0);
    }


    public static double similarityBetween(JComponent ui, String imageFile, double expectedSimilarity) {
        BufferedImage image = offscreenRender(ui);
        return similarityBetween(image, imageFile, expectedSimilarity);
    }

    public static double similarityBetween(BufferedImage image, String imageFile, double expectedSimilarity) {
        imageFile = "/snapshots/" + imageFile;
        BufferedImage originalImage = image;
        BufferedImage imageFromFile = null;
        try {
            // We load from the test resource folder:
            InputStream resource = Utility.class.getResourceAsStream(imageFile);
            imageFromFile = javax.imageio.ImageIO.read(resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int width1 = imageFromFile.getWidth();
        int width0 = image.getWidth();
        int height1 = imageFromFile.getHeight();
        int height0 = image.getHeight();
        if ((width1 != width0) || (height1 != height0)) {
            // Let's resize the image to the same size
            image = _stretchFirstToSecondSize(image, imageFromFile);
        }
        double similarity = similarityBetween(image, imageFromFile);
        if ( similarity < expectedSimilarity ) {
            try {
                String newPath = "build/resources/test" + imageFile.replace(".png", "-FAILURE.png");
                safeUIImage(originalImage, newPath);
                BufferedImage finalImageFromFile = imageFromFile;
                SwingUtilities.invokeLater(()-> {
                    JLabel wrongImage = new JLabel(new ImageIcon(originalImage));
                    JLabel expectedImage = new JLabel(new ImageIcon(finalImageFromFile));
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.getContentPane().add(
                        UI.box()
                        .add(wrongImage)
                        .add(UI.label("=|=").withFontSize(26))
                        .add(expectedImage)
                        .getComponent()
                    );
                    frame.pack();
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                e.printStackTrace();// Probably a headless exception
            }
        }
        return similarity;
    }

    /**
     *  This method will merge the array if images into a single collage like image
     *  which will then be compared to the provided imageFile.
     * @param images The images to merge
     * @param imageFile The image to compare to
     * @param expectedSimilarity The expected similarity
     * @return The similarity
     */
    public static double similarityBetween(BufferedImage[] images, String imageFile, double expectedSimilarity) {
        int a = (int) Math.sqrt(images.length);
        int b = images.length / a;
        if ( a * b < images.length )
            b++;

        int width = images[0].getWidth();
        int height = images[0].getHeight();
        BufferedImage collage = new BufferedImage(width * a, height * b, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = collage.createGraphics();
        int i = 0;
        for ( int y = 0; y < b; y++ ) {
            for ( int x = 0; x < a; x++ ) {
                if ( i >= images.length )
                    break;
                g2d.drawImage(images[i], x * width, y * height, null);
                i++;
            }
        }
        g2d.dispose();
        return similarityBetween(collage, imageFile, expectedSimilarity);
    }

    /**
     *  Makes sure the directory of the provided file exists.
     *
     * @param filePath The file for which the path is to be established.
     */
    private static void establishLocation(String filePath) {
        String[] parts = filePath.split("/");
        String filename = parts[parts.length - 1];
        String path = filePath.replace(filename, "");
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static double similarityBetween(BufferedImage image0, BufferedImage image1) {
        int width1 = image1.getWidth();
        int height1 = image1.getHeight();
        long diff = 0;
        for (int y = 0; y < height1; y++) {
            for (int x = 0; x < width1; x++) {
                diff += pixelDiff(image1.getRGB(x, y), image0.getRGB(x, y));
            }
        }
        long maxDiff = (long) Math.pow((Math.pow(255, 2) * 3), 0.5) * width1 * height1;
        double similarity = 100.0 * ( 1 - (double) diff / maxDiff );
        return similarity;
    }

    private static double pixelDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >>  8) & 0xff;
        int b1 =  rgb1        & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >>  8) & 0xff;
        int b2 =  rgb2        & 0xff;
        return Math.pow(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2), 0.5);
    }

    /**
     *  Stretches the first image to the size of the second image.
     * @param image0 The image to stretch
     * @param image1 The image with the target size
     * @return The new stretched image
     */
    private static BufferedImage _stretchFirstToSecondSize( BufferedImage image0, BufferedImage image1 )
    {
        int width1 = image1.getWidth();
        int width0 = image0.getWidth();
        double widthRatio = (double) width1 / width0;
        int height1 = image1.getHeight();
        int height0 = image0.getHeight();
        double heightRatio = (double) height1 / height0;
        BufferedImage image = new BufferedImage(width1, height1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(widthRatio, heightRatio);
        g2d.drawRenderedImage(image0, at);
        g2d.dispose();
        return image;
    }

    public static String link(String linkText, Class<?> exampleTypeClass) {
        String pathBase = "https://github.com/globaltcad/swing-tree/blob/main/src/test/java/";
        String filename = exampleTypeClass.getName().replace(".", "/") + ".java";
        String base = "<a href=\"" + pathBase + filename + "\" target=\"_blank\">";
        return  base + linkText + "</a>";
    }

    public static String linkSnapshot(String filename) {
        String pathBase = "https://raw.githubusercontent.com/globaltcad/swing-tree/main/src/test/resources/snapshots/";
        //return "![" + filename + "](" + pathBase + filename + ")"; // old way
        return "<img src=\"" + pathBase + filename + "\" alt=\"" + filename + "\" style=\"max-width: 50%;\" />";
    }
}
