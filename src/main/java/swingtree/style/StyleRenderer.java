package swingtree.style;

import com.github.weisj.jsvg.geometry.size.FloatSize;
import org.slf4j.Logger;
import swingtree.UI;
import swingtree.api.Painter;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

/**
 *  A stateless un-instantiable utility class that renders the style of a component
 *  using the immutable {@link ComponentConf} object containing the essential state
 *  needed for rendering, like for example the current {@link Bounds} and {@link Style}
 *  of a particular component.
 */
final class StyleRenderer
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StyleRenderer.class);

    private StyleRenderer() {} // Un-instantiable!


    public static void renderStyleFor( StyleEngine engine, UI.Layer layer, Graphics2D g2d )
    {
        ComponentConf conf = engine.getComponentConf();

        // First up, we render things unique to certain layers:

        if ( layer == UI.Layer.BACKGROUND ) {
            conf.style().base().foundationColor().ifPresent(outerColor -> {
                // Check if the color is transparent
                if ( outerColor.getAlpha() > 0 ) {
                    g2d.setColor(outerColor);
                    g2d.fill(engine.getExteriorArea());
                }
            });
            conf.style().base().backgroundColor().ifPresent(color -> {
                if ( color.getAlpha() == 0 ) return;
                g2d.setColor(color);
                g2d.fill(engine.getInteriorArea());
            });
        }

        if ( layer == UI.Layer.BORDER ) {
            conf.style().border().color().ifPresent(color -> {
                _drawBorder( engine, color, g2d);
            });
        }

        // Now onto things every layer has in common:

        // Every layer has 4 things:
        // 1. A grounding serving as a base background, which is a filled color and/or an image:
        for ( ImageStyle imageStyle : conf.style().images(layer) )
            if ( !imageStyle.equals(ImageStyle.none()) )
                _renderImage( imageStyle, conf.currentBounds(), engine, g2d);

        // 2. Gradients, which are best used to give a component a nice surface lighting effect.
        // They may transition vertically, horizontally or diagonally over various different colors:
        for ( GradientStyle gradient : conf.style().gradients(layer) )
            if ( gradient.colors().length > 0 ) {
                if ( gradient.colors().length == 1 ) {
                    g2d.setColor(gradient.colors()[0]);
                    g2d.fill(engine.getInteriorArea());
                }
                else if ( gradient.transition().isDiagonal() )
                    _renderDiagonalGradient(g2d, conf.currentBounds(), conf.style().margin(), gradient, engine.getInteriorArea());
                else
                    _renderVerticalOrHorizontalGradient(g2d, conf.currentBounds(), conf.style().margin(), gradient, engine.getInteriorArea());
            }

        // 3. Shadows, which are simple gradient based drop shadows that can go inwards or outwards
        for ( ShadowStyle shadow : conf.style().shadows(layer) )
            _renderShadows(shadow, engine, g2d);

        // 4. Painters, which are provided by the user and can be anything
        List<Painter> painters = conf.style().painters(layer);
        if ( !painters.isEmpty() )
            engine.paintWithContentAreaClip( g2d, () -> {
                // We remember the current transform and clip so that we can reset them after each painter:
                AffineTransform currentTransform = new AffineTransform(g2d.getTransform());
                Shape           currentClip      = g2d.getClip();

                painters.forEach( backgroundPainter -> {

                    if ( backgroundPainter == Painter.none() )
                        return;

                    try {
                        backgroundPainter.paint(g2d);
                    } catch (Exception e) {
                        log.warn(
                            "An exception occurred while executing painter '" + backgroundPainter + "' " +
                            "on layer '" + layer + "' for style '" + conf.style() + "' ",
                            e
                        );
                    /*
                        If exceptions happen in user provided painters, we don't want to
                        mess up the rendering of the rest of the component, so we catch them here!

                        We log as warning because exceptions during rendering are not considered
                        as harmful as elsewhere!

                        Hi there! If you are reading this, you are probably a developer using the SwingTree
                        library, thank you for using it! Good luck finding out what went wrong! :)
                    */
                    } finally {
                        // We do not know what the painter did to the graphics object, so we reset it:
                        g2d.setTransform(currentTransform);
                        g2d.setClip(currentClip);
                    }
                });
            });
    }

    private static void _drawBorder( StyleEngine engine, Color color, Graphics2D g2d ) {
        ComponentConf conf = engine.getComponentConf();
        if ( !Outline.none().equals(conf.style().border().widths()) ) {
            try {
                Area borderArea = engine.getBorderArea();
                g2d.setColor(color);
                g2d.fill(borderArea);

                if (!conf.style().border().gradients().isEmpty()) {
                    for ( GradientStyle gradient : conf.style().border().gradients() ) {
                        if ( gradient.colors().length > 0 ) {
                            if ( gradient.transition().isDiagonal() )
                                _renderDiagonalGradient(g2d, conf.currentBounds(), conf.style().margin(), gradient, borderArea);
                            else
                                _renderVerticalOrHorizontalGradient(g2d, conf.currentBounds(), conf.style().margin(), gradient, borderArea);
                        }
                    }
                }
            } catch ( Exception e ) {
                log.warn(
                    "An exception occurred while drawing the border of border style '" + conf.style().border() + "' ",
                    e
                );
                /*
                    If exceptions happen in user provided painters, we don't want to
                    mess up the rendering of the rest of the component, so we catch them here!
                */
            }
        }
    }

    private static void _renderShadows(
        ShadowStyle shadow,
        StyleEngine engine,
        Graphics2D  g2d
    ) {
        if ( !shadow.color().isPresent() )
            return;

        Color shadowColor = shadow.color().orElse(Color.BLACK);
        Style style       = engine.getComponentConf().style();
        Bounds bounds     = engine.getComponentConf().currentBounds();

        // First let's check if we need to render any shadows at all
        // Is the shadow color transparent?
        if ( shadowColor.getAlpha() == 0 )
            return;

        // The background box is calculated from the margins and border radius:
        final int leftBorderWidth   = style.border().widths().left().orElse(0);
        final int topBorderWidth    = style.border().widths().top().orElse(0);
        final int rightBorderWidth  = style.border().widths().right().orElse(0);
        final int bottomBorderWidth = style.border().widths().bottom().orElse(0);
        final int left   = Math.max(style.margin().left().orElse(0),   0) + ( shadow.isInset() ? leftBorderWidth   : 0 );
        final int top    = Math.max(style.margin().top().orElse(0),    0) + ( shadow.isInset() ? topBorderWidth    : 0 );
        final int right  = Math.max(style.margin().right().orElse(0),  0) + ( shadow.isInset() ? rightBorderWidth  : 0 );
        final int bottom = Math.max(style.margin().bottom().orElse(0), 0) + ( shadow.isInset() ? bottomBorderWidth : 0 );
        final int topLeftRadius     = Math.max(style.border().topLeftRadius(), 0);
        final int topRightRadius    = Math.max(style.border().topRightRadius(), 0);
        final int bottomRightRadius = Math.max(style.border().bottomRightRadius(), 0);
        final int bottomLeftRadius  = Math.max(style.border().bottomLeftRadius(), 0);

        final int width     = bounds.width();
        final int height    = bounds.height();

        // Calculate the shadow box bounds based on the padding and border thickness
        final int x = left + shadow.horizontalOffset();
        final int y = top  + shadow.verticalOffset();
        final int w = width  - left - right;
        final int h = height - top  - bottom;

        final int blurRadius   = Math.max(shadow.blurRadius(), 0);
        final int spreadRadius = !shadow.isOutset() ? shadow.spreadRadius() : -shadow.spreadRadius();

        Rectangle outerShadowRect = new Rectangle(
                                        x - blurRadius + spreadRadius,
                                        y - blurRadius + spreadRadius,
                                        w + blurRadius * 2 - spreadRadius * 2,
                                        h + blurRadius * 2 - spreadRadius * 2
                                    );

        Function<Integer, Integer> offsetFunction = (radius) -> (int)((radius * 2) / ( shadow.isInset() ? 4.5 : 3.79) );

        final int averageCornerRadius = ( topLeftRadius + topRightRadius + bottomRightRadius + bottomLeftRadius ) / 4;
        final int averageBorderWidth  = ( leftBorderWidth + topBorderWidth + rightBorderWidth +  bottomBorderWidth ) / 4;
        final int shadowCornerRadius  = Math.max( 0, averageCornerRadius - averageBorderWidth );
        final int gradientStartOffset = 1 + offsetFunction.apply(shadowCornerRadius);

        Rectangle innerShadowRect = new Rectangle(
                                        x + blurRadius + gradientStartOffset + spreadRadius,
                                        y + blurRadius + gradientStartOffset + spreadRadius,
                                        w - blurRadius * 2 - gradientStartOffset * 2 - spreadRadius * 2,
                                        h - blurRadius * 2 - gradientStartOffset * 2 - spreadRadius * 2
                                    );

        final Area baseArea;

        if ( shadow.isOutset() ) {
            int artifactAdjustment = 1;
            baseArea = AreasCache.calculateBaseArea(engine.getComponentConf(), artifactAdjustment, artifactAdjustment, artifactAdjustment, artifactAdjustment);
        }
        else
            baseArea = new Area(engine.getInteriorArea());

        // Apply the clipping to avoid overlapping the shadow and the box
        Area shadowArea = new Area(outerShadowRect);

        if ( shadow.isOutset() )
            shadowArea.subtract(baseArea);
        else
            shadowArea.intersect(baseArea);

        // Draw the corner shadows
        _renderCornerShadow(shadow, UI.Corner.TOP_LEFT,     shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);
        _renderCornerShadow(shadow, UI.Corner.TOP_RIGHT,    shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);
        _renderCornerShadow(shadow, UI.Corner.BOTTOM_LEFT,  shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);
        _renderCornerShadow(shadow, UI.Corner.BOTTOM_RIGHT, shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);

        // Draw the edge shadows
        _renderEdgeShadow(shadow, UI.Edge.TOP,    shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);
        _renderEdgeShadow(shadow, UI.Edge.RIGHT,  shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);
        _renderEdgeShadow(shadow, UI.Edge.BOTTOM, shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);
        _renderEdgeShadow(shadow, UI.Edge.LEFT,   shadowArea, innerShadowRect, outerShadowRect, gradientStartOffset, g2d);

        Area outerMostArea = new Area(outerShadowRect);
        // If the base rectangle and the outer shadow box are not equal, then we need to fill the area of the base rectangle that is not covered by the outer shadow box!
        _renderShadowBody(shadow, baseArea, innerShadowRect, outerMostArea, g2d);

    }

    private static void _renderShadowBody(
        ShadowStyle shadowStyle,
        Area        baseArea,
        Rectangle   innerShadowRect,
        Area        outerShadowBox,
        Graphics2D  g2d
    ) {
        Graphics2D g2d2 = (Graphics2D) g2d.create();
        g2d2.setColor(shadowStyle.color().orElse(Color.BLACK));
        if ( !shadowStyle.isOutset() ) {
            baseArea.subtract(outerShadowBox);
            g2d2.fill(baseArea);
        } else {
            Area innerShadowArea = new Area(innerShadowRect);
            innerShadowArea.subtract(baseArea);
            g2d2.fill(innerShadowArea);
        }
        g2d2.dispose();
    }

    private static void _renderCornerShadow(
        ShadowStyle shadowStyle,
        UI.Corner   corner,
        Area        areaWhereShadowIsAllowed,
        Rectangle   innerShadowRect,
        Rectangle   outerShadowRect,
        int         gradientStartOffset,
        Graphics2D  g2d
    ) {
        // We define a clipping box so that corners don't overlap
        float clipBoxWidth   = outerShadowRect.width / 2f;
        float clipBoxHeight  = outerShadowRect.height / 2f;
        float clipBoxCenterX = outerShadowRect.x + clipBoxWidth;
        float clipBoxCenterY = outerShadowRect.y + clipBoxHeight;
        Rectangle2D.Float cornerClipBox; // outer box!

        // The defining the corner shadow bound (where it starts and ends
        Rectangle2D.Float cornerBox;
        float cx;
        float cy;
        float cr; // depending on the corner, this is either the corner box width or height
        switch (corner) {
            case TOP_LEFT:
                cornerBox = new Rectangle2D.Float(
                                    outerShadowRect.x, outerShadowRect.y,
                                    innerShadowRect.x - outerShadowRect.x,
                                    innerShadowRect.y - outerShadowRect.y
                                );
                cornerClipBox = new Rectangle2D.Float(
                                    clipBoxCenterX - clipBoxWidth, clipBoxCenterY - clipBoxHeight,
                                    clipBoxWidth, clipBoxHeight
                                );

                cx = cornerBox.x + cornerBox.width;
                cy = cornerBox.y + cornerBox.height;
                cr = cornerBox.width;
                break;
            case TOP_RIGHT:
                cornerBox = new Rectangle2D.Float(
                                innerShadowRect.x + innerShadowRect.width, outerShadowRect.y,
                                outerShadowRect.x + outerShadowRect.width - innerShadowRect.x - innerShadowRect.width,
                                innerShadowRect.y - outerShadowRect.y
                            );
                cornerClipBox = new Rectangle2D.Float(
                                    clipBoxCenterX, clipBoxCenterY - clipBoxHeight,
                                    clipBoxWidth, clipBoxHeight
                                );

                cx = cornerBox.x;
                cy = cornerBox.y + cornerBox.height;
                cr = cornerBox.width;
                break;
            case BOTTOM_LEFT:
                cornerBox = new Rectangle2D.Float(
                                outerShadowRect.x,
                                innerShadowRect.y + innerShadowRect.height,
                                innerShadowRect.x - outerShadowRect.x,
                                outerShadowRect.y + outerShadowRect.height - innerShadowRect.y - innerShadowRect.height
                            );
                cornerClipBox = new Rectangle2D.Float(
                                    clipBoxCenterX - clipBoxWidth, clipBoxCenterY,
                                    clipBoxWidth, clipBoxHeight
                                );

                cx = cornerBox.x + cornerBox.width;
                cy = cornerBox.y;
                cr = cornerBox.width;
                break;
            case BOTTOM_RIGHT:
                cornerBox = new Rectangle2D.Float(
                            innerShadowRect.x + innerShadowRect.width, innerShadowRect.y + innerShadowRect.height,
                            outerShadowRect.x + outerShadowRect.width - innerShadowRect.x - innerShadowRect.width,
                            outerShadowRect.y + outerShadowRect.height - innerShadowRect.y - innerShadowRect.height
                            );
                cornerClipBox = new Rectangle2D.Float(
                                    clipBoxCenterX, clipBoxCenterY,
                                    clipBoxWidth, clipBoxHeight
                                );

                cx = cornerBox.x;
                cy = cornerBox.y;
                cr = cornerBox.width;
                break;
            default:
                throw new IllegalArgumentException("Invalid corner: " + corner);
        }

        if (cr <= 0) return;

        Color innerColor;
        Color outerColor;
        Color shadowBackgroundColor = _transparentShadowBackground(shadowStyle);
        if ( shadowStyle.isOutset() ) {
            innerColor = shadowStyle.color().orElse(Color.BLACK);
            outerColor = shadowBackgroundColor;
        } else {
            innerColor = shadowBackgroundColor;
            outerColor = shadowStyle.color().orElse(Color.BLACK);
        }
        float gradientStart = (float) gradientStartOffset / cr;

        // The first thing we can do is to clip the corner box to the area where the shadow is allowed
        Area cornerArea = new Area(cornerBox);
        cornerArea.intersect(areaWhereShadowIsAllowed);

        // In the simplest case we don't need to do any gradient painting:
        if ( gradientStart == 1f || gradientStart == 0f ) {
            // Simple, we just draw a circle and clip it
            Area circle = new Area(new Ellipse2D.Float(cx - cr, cy - cr, cr * 2, cr * 2));
            if ( shadowStyle.isInset() ) {
                g2d.setColor(outerColor);
                cornerArea.subtract(circle);
            } else {
                g2d.setColor(innerColor);
                cornerArea.intersect(circle);
            }
            g2d.fill(cornerArea);
            return;
        }

        RadialGradientPaint cornerPaint;
        if ( gradientStart > 1f || gradientStart < 0f )
            cornerPaint = new RadialGradientPaint(
                             cx, cy, cr,
                             new float[] {0f, 1f},
                             new Color[] {innerColor, outerColor}
                         );
        else
            cornerPaint = new RadialGradientPaint(
                             cx, cy, cr,
                             new float[] {0f, gradientStart, 1f},
                             new Color[] {innerColor, innerColor, outerColor}
                         );

        // We need to clip the corner paint to the corner box
        cornerArea.intersect(new Area(cornerClipBox));

        Graphics2D cornerG2d = (Graphics2D) g2d.create();
        cornerG2d.setPaint(cornerPaint);
        cornerG2d.fill(cornerArea);
        cornerG2d.dispose();
    }

    private static void _renderEdgeShadow(
        ShadowStyle shadowStyle,
        UI.Edge     edge,
        Area        contentArea,
        Rectangle   innerShadowRect,
        Rectangle   outerShadowRect,
        int         gradientStartOffset,
        Graphics2D  g2d
    ) {
        // We define a boundary center point and a clipping box so that edges don't overlap
        float clipBoundaryX = outerShadowRect.x + outerShadowRect.width / 2f;
        float clipBoundaryY = outerShadowRect.y + outerShadowRect.height / 2f;
        Rectangle2D.Float edgeClipBox = null;

        Rectangle2D.Float edgeBox;
        float gradEndX;
        float gradEndY;
        float gradStartX;
        float gradStartY;
        switch (edge) {
            case TOP:
                edgeBox = new Rectangle2D.Float(
                                innerShadowRect.x, outerShadowRect.y,
                                innerShadowRect.width, innerShadowRect.y - outerShadowRect.y
                            );

                if ( (edgeBox.y + edgeBox.height) > clipBoundaryY )
                    edgeClipBox = new Rectangle2D.Float(
                            edgeBox.x, edgeBox.y,
                            edgeBox.width, clipBoundaryY - edgeBox.y
                    );

                gradEndX = edgeBox.x;
                gradEndY = edgeBox.y;
                gradStartX = edgeBox.x;
                gradStartY = edgeBox.y + edgeBox.height;
                break;
            case RIGHT:
                edgeBox = new Rectangle2D.Float(
                                innerShadowRect.x + innerShadowRect.width, innerShadowRect.y,
                                outerShadowRect.x + outerShadowRect.width - innerShadowRect.x - innerShadowRect.width,
                                innerShadowRect.height
                            );
                if ( edgeBox.x < clipBoundaryX )
                    edgeClipBox = new Rectangle2D.Float(
                                        clipBoundaryX, edgeBox.y,
                                        edgeBox.x + edgeBox.width - clipBoundaryX, edgeBox.height
                                    );
                gradEndX = edgeBox.x + edgeBox.width;
                gradEndY = edgeBox.y;
                gradStartX = edgeBox.x;
                gradStartY = edgeBox.y;
                break;
            case BOTTOM:
                edgeBox = new Rectangle2D.Float(
                        innerShadowRect.x, innerShadowRect.y + innerShadowRect.height,
                        innerShadowRect.width, outerShadowRect.y + outerShadowRect.height - innerShadowRect.y - innerShadowRect.height
                    );
                if ( edgeBox.y < clipBoundaryY )
                    edgeClipBox = new Rectangle2D.Float(
                            edgeBox.x,
                            clipBoundaryY,
                            edgeBox.width,
                            edgeBox.y + edgeBox.height - clipBoundaryY
                    );

                gradEndX = edgeBox.x;
                gradEndY = edgeBox.y + edgeBox.height;
                gradStartX = edgeBox.x;
                gradStartY = edgeBox.y;
                break;
            case LEFT:
                edgeBox = new Rectangle2D.Float(
                            outerShadowRect.x,
                            innerShadowRect.y,
                            innerShadowRect.x - outerShadowRect.x,
                            innerShadowRect.height
                            );
                if ( (edgeBox.x + edgeBox.width) > clipBoundaryX )
                    edgeClipBox = new Rectangle2D.Float(
                            edgeBox.x,
                            edgeBox.y,
                            clipBoundaryX - edgeBox.x,
                            edgeBox.height
                    );
                gradEndX = edgeBox.x;
                gradEndY = edgeBox.y;
                gradStartX = edgeBox.x + edgeBox.width;
                gradStartY = edgeBox.y;
                break;
            default:
                throw new IllegalArgumentException("Invalid edge: " + edge);
        }

        if ( gradStartX == gradEndX && gradStartY == gradEndY ) return;

        Color innerColor;
        Color outerColor;
        // Same as shadow color but without alpha:
        Color shadowBackgroundColor = _transparentShadowBackground(shadowStyle);
        if (shadowStyle.isOutset()) {
            innerColor = shadowStyle.color().orElse(Color.BLACK);
            outerColor = shadowBackgroundColor;
        } else {
            innerColor = shadowBackgroundColor;
            outerColor = shadowStyle.color().orElse(Color.BLACK);
        }
        LinearGradientPaint edgePaint;
        // distance between start and end of gradient
        float dist = (float) Math.sqrt(
                                    (gradEndX - gradStartX) * (gradEndX - gradStartX) +
                                    (gradEndY - gradStartY) * (gradEndY - gradStartY)
                                );
        float gradientStart = (float) gradientStartOffset / dist;
        if ( gradientStart > 1f || gradientStart < 0f )
            edgePaint = new LinearGradientPaint(
                               gradStartX, gradStartY,
                               gradEndX, gradEndY,
                               new float[] {0f, 1f},
                               new Color[] {innerColor, outerColor}
                           );
        else {
            if ( gradientStart == 1f || gradientStart == 0f ) {
                // The gradient does not really exist, so we can just fill the whole area and then return
                Area edgeArea = new Area(edgeBox);
                g2d.setColor(innerColor);
                if ( shadowStyle.isOutset() )
                    edgeArea.intersect(contentArea);
                g2d.fill(edgeArea);
                return;
            }
            edgePaint = new LinearGradientPaint(
                             gradStartX, gradStartY,
                             gradEndX, gradEndY,
                             new float[] {0f, gradientStart, 1f},
                             new Color[] {innerColor, innerColor, outerColor}
                         );
        }

        // We need to clip the edge paint to the edge box
        Area edgeArea = new Area(edgeBox);
        edgeArea.intersect(contentArea);
        if ( edgeClipBox != null )
            edgeArea.intersect(new Area(edgeClipBox));

        Graphics2D edgeG2d = (Graphics2D) g2d.create();
        edgeG2d.setPaint(edgePaint);
        edgeG2d.fill(edgeArea);
        edgeG2d.dispose();
    }

    private static Color _transparentShadowBackground(ShadowStyle shadow) {
        return shadow.color()
                    .map(c -> new Color(c.getRed(), c.getGreen(), c.getBlue(), 0))
                    .orElse(new Color(0.5f, 0.5f, 0.5f, 0f));
    }

    /**
     *  Renders a shade from the top left corner to the bottom right corner.
     *
     * @param g2d The graphics object to render to.
     * @param bounds The bounds of the component.
     * @param margin The margin of the component.
     * @param gradient The shade to render.
     */
    private static void _renderDiagonalGradient(
        Graphics2D    g2d,
        Bounds        bounds,
        Outline       margin,
        GradientStyle gradient,
        Area          specificArea
    ) {
        Color[] colors = gradient.colors();
        UI.Transition type = gradient.transition();
        Dimension size = new Dimension(bounds.width(), bounds.height());
        size.width  -= (margin.right().orElse(0) + margin.left().orElse(0));
        size.height -= (margin.bottom().orElse(0) + margin.top().orElse(0));
        int width  = size.width;
        int height = size.height;
        int realX = margin.left().orElse(0);
        int realY = margin.top().orElse(0);

        int corner1X;
        int corner1Y;
        int corner2X;
        int corner2Y;
        int diagonalCorner1X;
        int diagonalCorner1Y;
        int diagonalCorner2X;
        int diagonalCorner2Y;

        boolean revertColors = false;
        if ( type == UI.Transition.TOP_RIGHT_TO_BOTTOM_LEFT ) {
            type = UI.Transition.BOTTOM_LEFT_TO_TOP_RIGHT;
            // We revert the colors
            revertColors = true;
        }
        if ( type == UI.Transition.BOTTOM_RIGHT_TO_TOP_LEFT ) {
            type = UI.Transition.TOP_LEFT_TO_BOTTOM_RIGHT;
            revertColors = true;
        }

        if ( revertColors ) {// We revert the colors
            if ( colors.length == 2 ) {
                Color tmp = colors[0];
                colors[0] = colors[1];
                colors[1] = tmp;
            } else
                // We have more than 2 colors, so we need to revert the array
                for ( int i = 0; i < colors.length / 2; i++ ) {
                    Color tmp = colors[i];
                    colors[i] = colors[colors.length - i - 1];
                    colors[colors.length - i - 1] = tmp;
                }
        }

        if ( type == UI.Transition.TOP_LEFT_TO_BOTTOM_RIGHT ) {
            corner1X = realX;
            corner1Y = realY;
            corner2X = realX + width;
            corner2Y = realY + height;
            diagonalCorner1X = realX;
            diagonalCorner1Y = realY + height;
            diagonalCorner2X = realX + width;
            diagonalCorner2Y = realY;
        } else if ( type == UI.Transition.BOTTOM_LEFT_TO_TOP_RIGHT ) {
            corner1X = realX + width;
            corner1Y = realY;
            corner2X = realX;
            corner2Y = realY + height;
            diagonalCorner1X = realX + width;
            diagonalCorner1Y = realY + height;
            diagonalCorner2X = realX;
            diagonalCorner2Y = realY;
        }
        else
            throw new IllegalArgumentException("Invalid gradient alignment: " + type);

        int diagonalCenterX = (diagonalCorner1X + diagonalCorner2X) / 2;
        int diagonalCenterY = (diagonalCorner1Y + diagonalCorner2Y) / 2;

        float[] fractions = new float[colors.length];
        for ( int i = 0; i < colors.length; i++ )
            fractions[i] = (float) i / (float) (colors.length - 1);

        if ( gradient.type() == UI.GradientType.RADIAL ) {
            float startCornerX, startCornerY;
            if ( type == UI.Transition.TOP_LEFT_TO_BOTTOM_RIGHT ) {
                startCornerX = corner1X;
                startCornerY = corner1Y;
            } else {
                startCornerX = corner2X;
                startCornerY = corner2Y;
            }
            float radius = (float) Math.sqrt(
                                                (diagonalCenterX - startCornerX) * (diagonalCenterX - startCornerX) +
                                                (diagonalCenterY - startCornerY) * (diagonalCenterY - startCornerY)
                                            );
            if ( colors.length == 2 )
                g2d.setPaint(new RadialGradientPaint(
                        new Point2D.Float(startCornerX, startCornerY),
                        radius,
                        fractions,
                        colors
                    ));
            else
                g2d.setPaint(new RadialGradientPaint(
                        new Point2D.Float(startCornerX, startCornerY),
                        radius,
                        fractions,
                        colors,
                        MultipleGradientPaint.CycleMethod.NO_CYCLE
                    ));
        } else if ( gradient.type() == UI.GradientType.LINEAR ) {
            double vector1X = diagonalCorner1X - diagonalCenterX;
            double vector1Y = diagonalCorner1Y - diagonalCenterY;
            double vector2X = diagonalCorner2X - diagonalCenterX;
            double vector2Y = diagonalCorner2Y - diagonalCenterY;

            double vectorLength = Math.sqrt(vector1X * vector1X + vector1Y * vector1Y);
            vector1X = (vector1X / vectorLength);
            vector1Y = (vector1Y / vectorLength);

            vectorLength = Math.sqrt(vector2X * vector2X + vector2Y * vector2Y);
            vector2X = (vector2X / vectorLength);
            vector2Y = (vector2Y / vectorLength);

            double nVector1X = -vector1Y;
            double nVector1Y =  vector1X;
            double nVector2X = -vector2Y;
            double nVector2Y =  vector2X;

            double distance1 = (corner1X - diagonalCenterX) * nVector1X + (corner1Y - diagonalCenterY) * nVector1Y;
            double distance2 = (corner2X - diagonalCenterX) * nVector2X + (corner2Y - diagonalCenterY) * nVector2Y;

            int gradientStartX = (int) (diagonalCenterX + nVector1X * distance1);
            int gradientStartY = (int) (diagonalCenterY + nVector1Y * distance1);
            int gradientEndX = (int) (diagonalCenterX + nVector2X * distance2);
            int gradientEndY = (int) (diagonalCenterY + nVector2Y * distance2);

            if ( colors.length == 2 )
                g2d.setPaint(new GradientPaint(
                                gradientStartX, gradientStartY, colors[0],
                                gradientEndX, gradientEndY, colors[1]
                            ));
            else
                g2d.setPaint(new LinearGradientPaint(
                                gradientStartX, gradientStartY,
                                gradientEndX, gradientEndY,
                                fractions, colors
                            ));
        }
        g2d.fill(specificArea);
    }

    private static void _renderVerticalOrHorizontalGradient(
        Graphics2D    g2d,
        Bounds        bounds,
        Outline       margin,
        GradientStyle gradient,
        Area          specificArea
    ) {
        UI.Transition type = gradient.transition();
        Color[] colors = gradient.colors();
        Dimension size = new Dimension(bounds.width(), bounds.height());
        size.width  -= (margin.right().orElse(0) + margin.left().orElse(0));
        size.height -= (margin.bottom().orElse(0) + margin.top().orElse(0));
        int width  = size.width;
        int height = size.height;
        int realX = margin.left().orElse(0);
        int realY = margin.top().orElse(0);

        int corner1X;
        int corner1Y;
        int corner2X;
        int corner2Y;

        if ( type == UI.Transition.TOP_TO_BOTTOM ) {
            corner1X = realX;
            corner1Y = realY;
            corner2X = realX;
            corner2Y = realY + height;
        } else if ( type == UI.Transition.LEFT_TO_RIGHT ) {
            corner1X = realX;
            corner1Y = realY;
            corner2X = realX + width;
            corner2Y = realY;
        } else if ( type == UI.Transition.BOTTOM_TO_TOP ) {
            corner1X = realX;
            corner1Y = realY + height;
            corner2X = realX;
            corner2Y = realY;
        } else if ( type == UI.Transition.RIGHT_TO_LEFT ) {
            corner1X = realX + width;
            corner1Y = realY;
            corner2X = realX;
            corner2Y = realY;
        }
        else throw new IllegalArgumentException("Unknown gradient alignment: " + type);

        if ( colors.length == 2 )
            g2d.setPaint(
                    new GradientPaint(
                            corner1X, corner1Y, colors[0],
                            corner2X, corner2Y, colors[1]
                        )
                );
        else {
            float[] fractions = new float[colors.length];
            for ( int i = 0; i < colors.length; i++ )
                fractions[i] = (float) i / (float) (colors.length - 1);

            if ( gradient.type() == UI.GradientType.LINEAR )
                g2d.setPaint(
                    new LinearGradientPaint(
                            corner1X, corner1Y,
                            corner2X, corner2Y,
                            fractions, colors
                        )
                );
            else if ( gradient.type() == UI.GradientType.RADIAL ) {
                float radius = (float) Math.sqrt(
                                            (corner2X - corner1X) * (corner2X - corner1X) +
                                            (corner2Y - corner1Y) * (corner2Y - corner1Y)
                                        );
                g2d.setPaint(new RadialGradientPaint(
                                new Point2D.Float(corner1X, corner1Y),
                                radius,
                                fractions,
                                colors
                            ));
            }
            else
                throw new IllegalArgumentException("Invalid gradient type: " + gradient.type());
        }
        g2d.fill(specificArea);
    }

    private static void _renderImage(
        ImageStyle  style,
        Bounds      bounds,
        StyleEngine engine,
        Graphics2D  g2d
    ) {
        if ( style.primer().isPresent() ) {
            g2d.setColor(style.primer().get());
            g2d.fill(_areaFrom(style.clipArea(), engine));
        }

        style.image().ifPresent( imageIcon -> {
            final UI.Placement placement       = style.placement();
            final boolean      repeat          = style.repeat();
            final Outline      padding         = style.padding();
            final int          componentWidth  = bounds.width();
            final int          componentHeight = bounds.height();

            int imgWidth  = style.width().orElse(imageIcon.getIconWidth());
            int imgHeight = style.height().orElse(imageIcon.getIconHeight());

            if ( style.fitMode() != UI.FitComponent.NO ) {
                if ( imageIcon instanceof SvgIcon) {
                    imgWidth  = style.width().orElse(componentWidth);
                    imgHeight = style.height().orElse(componentHeight);
                } else {
                    if ( style.fitMode() == UI.FitComponent.WIDTH_AND_HEIGHT ) {
                        imgWidth  = style.width().orElse(componentWidth);
                        imgHeight = style.height().orElse(componentHeight);
                    }
                    if ( style.fitMode() == UI.FitComponent.WIDTH )
                        imgWidth = style.width().orElse(componentWidth);
                    if ( style.fitMode() == UI.FitComponent.HEIGHT )
                        imgHeight = style.height().orElse(componentHeight);

                    if ( style.fitMode() == UI.FitComponent.MAX_DIM ) {
                        if ( componentWidth > componentHeight ) {
                            imgWidth = style.width().orElse(componentWidth);
                        } else if ( componentWidth < componentHeight ) {
                            imgHeight = style.height().orElse(componentHeight);
                        } else {
                            imgWidth  = style.width().orElse(componentWidth);
                            imgHeight = style.height().orElse(componentHeight);
                        }
                    }
                    if ( style.fitMode() == UI.FitComponent.MIN_DIM ) {
                        if ( componentWidth < componentHeight ) {
                            imgWidth = style.width().orElse(componentWidth);
                        } else if ( componentWidth > componentHeight ) {
                            imgHeight = style.height().orElse(componentHeight);
                        } else {
                            imgWidth  = style.width().orElse(componentWidth);
                            imgHeight = style.height().orElse(componentHeight);
                        }
                    }
                }
                imgWidth  = imgWidth  >= 0 ? imgWidth  : componentWidth;
                imgHeight = imgHeight >= 0 ? imgHeight : componentHeight;
            }
            if ( imageIcon instanceof SvgIcon ) {
                SvgIcon   svgIcon = (SvgIcon) imageIcon;
                FloatSize size    = svgIcon.getSvgSize();
                imgWidth  = imgWidth  >= 0 ? imgWidth  : (int) size.width;
                imgHeight = imgHeight >= 0 ? imgHeight : (int) size.height;
            }
            int x = style.horizontalOffset();
            int y = style.verticalOffset();

            final float opacity = style.opacity();

            switch ( placement ) {
                case TOP:
                    x += (componentWidth - imgWidth) / 2;
                    break;
                case LEFT:
                    y += (componentHeight - imgHeight) / 2;
                    break;
                case BOTTOM:
                    x += (componentWidth - imgWidth) / 2;
                    y += componentHeight - imgHeight;
                    break;
                case RIGHT:
                    x += componentWidth - imgWidth;
                    y += (componentHeight - imgHeight) / 2;
                    break;
                case TOP_LEFT: break;
                case TOP_RIGHT:
                    x += componentWidth - imgWidth;
                    break;
                case BOTTOM_LEFT:
                    y += componentHeight - imgHeight;
                    break;
                case BOTTOM_RIGHT:
                    x += componentWidth - imgWidth;
                    y += componentHeight - imgHeight;
                    break;
                case CENTER:
                case UNDEFINED:
                    x += (componentWidth - imgWidth) / 2;
                    y += (componentHeight - imgHeight) / 2;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown placement: " + placement);
            }
            // We apply the padding:
            x += padding.left().orElse(0);
            y += padding.top().orElse(0);
            imgWidth  -= padding.left().orElse(0) + padding.right().orElse(0);
            imgHeight -= padding.top().orElse(0)  + padding.bottom().orElse(0);
            if ( imageIcon instanceof SvgIcon ) {
                SvgIcon svgIcon = (SvgIcon) imageIcon;
                if ( imgWidth > -1 && svgIcon.getIconWidth() < 0 )
                    svgIcon = svgIcon.withIconWidth(imgWidth);
                if ( imgHeight > -1 && svgIcon.getIconHeight() < 0 )
                    svgIcon = svgIcon.withIconHeight(imgHeight);
                imageIcon = svgIcon;
            }

            final Shape oldClip = g2d.getClip();

            Shape newClip = _areaFrom(style.clipArea(), engine);
            // We merge the new clip with the old one:
            if ( newClip != null && oldClip != null )
                newClip = StyleUtility.intersect( newClip, oldClip );

            g2d.setClip(newClip);

            if ( !repeat && imageIcon instanceof SvgIcon ) {
                SvgIcon svgIcon = ((SvgIcon) imageIcon).withFitComponent(style.fitMode());
                svgIcon.withPreferredPlacement(UI.Placement.CENTER)
                        .paintIcon(null, g2d, x, y, imgWidth, imgHeight);
            }
            else
            {
                Image image;
                if ( imageIcon instanceof SvgIcon) {
                    SvgIcon svgIcon = (SvgIcon) imageIcon;
                    svgIcon = svgIcon.withIconWidth(imgWidth);
                    svgIcon = svgIcon.withIconHeight(imgHeight);
                    image   = svgIcon.getImage(); // This will render the SVGIcon with the new size
                }
                else
                    image = imageIcon.getImage();

                Composite oldComposite = g2d.getComposite();

                try {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    if (repeat) {
                        Paint oldPaint = g2d.getPaint();
                        try {
                            g2d.setPaint(new TexturePaint((BufferedImage) image, new Rectangle(x, y, imgWidth, imgHeight)));
                            g2d.fill(engine.getInteriorArea());
                        } finally {
                            g2d.setPaint(oldPaint);
                        }
                    }
                    else
                        g2d.drawImage(image, x, y, imgWidth, imgHeight, null);

                } finally {
                    g2d.setComposite(oldComposite);
                }
            }
            g2d.setClip(oldClip);
        });
    }

    private static Area _areaFrom( UI.ComponentArea areaType, StyleEngine engine ) {
            switch ( areaType ) {
                case INTERIOR:
                    return engine.getInteriorArea();
                case BORDER:
                    return engine.getBorderArea();
                case EXTERIOR:
                    return engine.getExteriorArea();
                case ALL:
                    return null;
                default:
                    log.warn("Unknown clip area: " + areaType);
                    return null;
            }
    }

}
