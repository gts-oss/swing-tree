package swingtree.style;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;
import org.slf4j.Logger;
import swingtree.UI;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Optional;

/**
 *   A specialized {@link ImageIcon} subclass that allows you to use SVG based icon images in your UI.
 *   This in essence just a wrapper around the JSVG library, which renders SVG images into Java2D graphics API.
 */
public class SVGIcon extends ImageIcon
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SVGIcon.class);

    private final SVGDocument _svgDocument;

    private int _width;
    private int _height;

    private UI.FitComponent _fitComponent;


    public SVGIcon( SVGIcon icon ) {
        super();
        _svgDocument  = icon._svgDocument;
        _width        = icon._width;
        _height       = icon._height;
        _fitComponent = icon._fitComponent;
    }

    public SVGIcon( URL svgUrl, int width, int height, UI.FitComponent fitComponent ) {
        super();
        _width = width;
        _height = height;
        _fitComponent = fitComponent;

        SVGDocument tempSVGDocument = null;
        try {
            SVGLoader loader = new SVGLoader();
            tempSVGDocument = loader.load(svgUrl);
        } catch (Exception e) {
            log.error("Failed to load SVG document from URL: " + svgUrl, e);
        }
        this._svgDocument = tempSVGDocument;
    }

    public SVGIcon( URL svgUrl, int width, int height ) {
        this(svgUrl, width, height, UI.FitComponent.MIN_DIM);
    }

    public SVGIcon( String path, int width, int height ) {
        this(SVGIcon.class.getResource(path), width, height);
    }

    public SVGIcon( String path ) { this(path, -1, -1); }

    public SVGIcon( URL svgUrl ) { this(svgUrl, -1, -1); }

    @Override
    public int getIconWidth() { return _width; }

    public void setIconWidth( int width ) { _width = width; }

    @Override
    public int getIconHeight() { return _height; }

    public void setIconHeight( int height ) { _height = height; }

    public void setFitComponent( UI.FitComponent fit ) { _fitComponent = fit; }

    public UI.FitComponent getFitComponent() { return _fitComponent; }

    @Override
    public Image getImage() {
        // We create a new buffered image, render into it, and then return it.
        BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        if ( _svgDocument != null )
            _svgDocument.render(
                    null,
                    image.createGraphics(),
                    new ViewBox(0, 0, getIconWidth(), getIconHeight())
                );

        return image;
    }

    @Override
    public void setImage( Image image ) {
        // We don't support this.
    }

    @Override
    public void paintIcon( java.awt.Component c, java.awt.Graphics g, int x, int y )
    {
        if ( _svgDocument == null )
            return;

        Insets insets = Optional.ofNullable( c instanceof JComponent ? ((JComponent)c).getBorder() : null )
                                .map( b -> {
                                    try {
                                        return _determineInsetsForBorder(b, c);
                                    } catch (Exception e) {
                                        return new Insets(0,0,0,0);
                                    }
                                })
                                .orElse(new Insets(0,0,0,0));
        x = insets.left;
        y = insets.top;

        int width  = c.getWidth();
        int height = c.getHeight();

        width  = width  - insets.right  - insets.left;
        height = height - insets.bottom - insets.top;

        paintIcon( c, g, x, y, width, height );
    }

    private Insets _determineInsetsForBorder( Border b, Component c )
    {
        if ( b == null )
            return new Insets(0,0,0,0);

        if ( b instanceof StyleAndAnimationBorder )
            return ((StyleAndAnimationBorder<?>)b).getFullPaddingInsets();

        // Compound border
        if ( b instanceof javax.swing.border.CompoundBorder ) {
            javax.swing.border.CompoundBorder cb = (javax.swing.border.CompoundBorder) b;
            return cb.getOutsideBorder().getBorderInsets(c);
        }

        try {
            return b.getBorderInsets(c);
        } catch (Exception e) {
            // Ignore
        }
        return new Insets(0,0,0,0);
    }

    public void paintIcon(
        final java.awt.Component c,
        final java.awt.Graphics g,
        final int x,
        final int y,
        int width,
        int height
    ) {
        width  = ( width  < 0 ? getIconWidth()  : width  );
        height = ( height < 0 ? getIconHeight() : height );

        Graphics2D g2d = (Graphics2D) g.create();

        FloatSize svgSize = _svgDocument.size();
        float svgRefWidth  = ( svgSize.width  > svgSize.height ? 1f : svgSize.width  / svgSize.height );
        float svgRefHeight = ( svgSize.height > svgSize.width  ? 1f : svgSize.height / svgSize.width  );
        float imgRefWidth  = (     width      >=   height      ? 1f :  (float) width /   height       );
        float imgRefHeight = (     height     >=   width       ? 1f : (float) height /   width        );

        float scaleX = imgRefWidth  / svgRefWidth;
        float scaleY = imgRefHeight / svgRefHeight;

        if ( _fitComponent == UI.FitComponent.MIN_DIM || _fitComponent == UI.FitComponent.MAX_DIM ) {
            if ( width < height )
                scaleX = 1f;
            if ( height < width )
                scaleY = 1f;
        }

        if ( _fitComponent == UI.FitComponent.WIDTH )
            scaleX = 1f;

        if ( _fitComponent == UI.FitComponent.HEIGHT )
            scaleY = 1f;

        ViewBox viewBox = new ViewBox(x, y, width, height);
        float boxX      = viewBox.x  / scaleX;
        float boxY      = viewBox.y  / scaleY;
        float boxWidth  = viewBox.width  / scaleX;
        float boxHeight = viewBox.height / scaleY;
        if ( _fitComponent == UI.FitComponent.MAX_DIM ) {
            // We now want to make sure that the
            if ( boxWidth < boxHeight ) {
                // We find the scale factor of the heights between the two rectangles:
                float scaleHeight = ( viewBox.height / svgSize.height );
                // We now want to scale the view box so that both have the same heights:
                float newWidth  = svgSize.width  * scaleHeight;
                float newHeight = svgSize.height * scaleHeight;
                float newX = viewBox.x + (viewBox.width - newWidth) / 2f;
                float newY = viewBox.y;
                viewBox = new ViewBox(newX, newY, newWidth, newHeight);
            } else {
                // We find the scale factor of the widths between the two rectangles:
                float scaleWidth = ( viewBox.width / svgSize.width );
                // We now want to scale the view box so that both have the same widths:
                float newWidth  = svgSize.width  * scaleWidth;
                float newHeight = svgSize.height * scaleWidth;
                float newX = viewBox.x;
                float newY = viewBox.y + (viewBox.height - newHeight) / 2f;
                viewBox = new ViewBox(newX, newY, newWidth, newHeight);
            }
        }
        else
            viewBox = new ViewBox(boxX, boxY, boxWidth, boxHeight);

        if ( _fitComponent == UI.FitComponent.NO ) {
            width = _width >= 0 ? _width : (int) svgSize.width;
            height = _height >= 0 ? _height : (int) svgSize.height;
            viewBox = new ViewBox(x, y, width, height);
        }

        // Let's check if the view box exists:
        if ( viewBox.width <= 0 || viewBox.height <= 0 )
            return;

        // Also let's check if the view box has valid values:
        if ( Float.isNaN(viewBox.x) || Float.isNaN(viewBox.y) || Float.isNaN(viewBox.width) || Float.isNaN(viewBox.height) )
            return;

        // Now onto the actual rendering:

        boolean doAntiAliasing  = UI.scale() < 1.5;
        boolean wasAntiAliasing = g2d.getRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING ) == java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
        if ( doAntiAliasing && !wasAntiAliasing )
            g2d.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON );

        AffineTransform oldTransform = g2d.getTransform();
        AffineTransform newTransform = new AffineTransform(oldTransform);
        newTransform.scale(scaleX, scaleY);

        g2d.setTransform(newTransform);

        try {
            // We also have to scale x and y, this is because the SVGDocument does not
            // account for the scale of the transform with respect to the view box!
            _svgDocument.render((JComponent) c, g2d, viewBox);
        } catch (Exception e) {
            log.warn("Failed to render SVG document: " + _svgDocument, e);
        }

        g2d.setTransform(oldTransform);

        if ( doAntiAliasing && !wasAntiAliasing )
            g2d.setRenderingHint( java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF );

    }

}
