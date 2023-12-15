package swingtree.style;

import org.slf4j.Logger;
import swingtree.UI;
import swingtree.animation.LifeTime;
import swingtree.api.Painter;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.*;

/**
 *  Orchestrates the rendering of a component's style and animations.
 */
final class StyleEngine
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StyleEngine.class);

    public static StyleEngine none() {
        LayerCache[] layerCaches = new LayerCache[UI.Layer.values().length];
        return new StyleEngine(StyleRenderState.none(), new Expirable[0], new AreasCache(), layerCaches);
    }

    static boolean IS_ANTIALIASING_ENABLED(){
        return UI.scale() < 1.5;
    }


    private final StyleRenderState       _state;
    private final Expirable<Painter>[]   _animationPainters;
    private final AreasCache             _areasCache;
    private final LayerCache[]           _layerCaches;


    private StyleEngine(
        StyleRenderState      state,
        Expirable<Painter>[]  animationPainters,
        AreasCache            areasCache,
        LayerCache[]          layerCaches
    ) {
        _state             = Objects.requireNonNull(state);
        _animationPainters = Objects.requireNonNull(animationPainters);
        _areasCache        = Objects.requireNonNull(areasCache);
        _layerCaches       = Objects.requireNonNull(layerCaches);
        for (int i = 0; i < _layerCaches.length && _layerCaches[i] == null; i++ )
            _layerCaches[i] = new LayerCache(UI.Layer.values()[i]);
    }

    StyleRenderState getState() { return _state; }

    StyleEngine withNewStyleAndComponent( Style style, JComponent component ) {
        StyleRenderState newState = _state.with(style, component);
        _areasCache.validate(_state, newState);
        for ( LayerCache layerCache : _layerCaches )
            layerCache.validate(_state, newState);
        return new StyleEngine( newState, _animationPainters, _areasCache, _layerCaches);
    }

    StyleEngine withAnimationPainter(LifeTime lifeTime, Painter animationPainter ) {
        java.util.List<Expirable<Painter>> animationPainters = new ArrayList<>(Arrays.asList(_animationPainters));
        animationPainters.add(new Expirable<>(lifeTime, animationPainter));
        return new StyleEngine( _state, animationPainters.toArray(new Expirable[0]), _areasCache, _layerCaches);
    }

    StyleEngine withoutAnimationPainters() {
        return new StyleEngine( _state, new Expirable[0], _areasCache, _layerCaches);
    }

    StyleEngine withoutExpiredAnimationPainters() {
        List<Expirable<Painter>> animationPainters = new ArrayList<>(Arrays.asList(_animationPainters));
        animationPainters.removeIf(Expirable::isExpired);
        return new StyleEngine( _state, animationPainters.toArray(new Expirable[0]), _areasCache, _layerCaches);
    }

    Style getStyle() { return _state.style(); }

    Optional<Shape> interiorArea() {
        Shape contentClip = null;
        if ( _areasCache.interiorComponentArea().exists() || getStyle().margin().isPositive() )
            contentClip = getInteriorArea();

        return Optional.ofNullable(contentClip);
    }

    Area getInteriorArea()
    {
        return _areasCache.interiorComponentArea().getFor(_state);
    }

    Area getExteriorArea()
    {
        return _areasCache.exteriorComponentArea().getFor(_state);
    }

    Area getBorderArea()
    {
        return _areasCache.borderArea().getFor(_state);
    }

    void paintWithContentAreaClip( Graphics g, Runnable painter ) {
        Shape oldClip = g.getClip();

        Shape newClip = getInteriorArea();
        if ( newClip != null && newClip != oldClip ) {
            newClip = StyleUtility.intersect(newClip, oldClip);
            g.setClip(newClip);
        }

        painter.run();

        g.setClip(oldClip);
    }

    void renderBackgroundStyle( Graphics2D g2d )
    {
        // We remember if antialiasing was enabled before we render:
        boolean antialiasingWasEnabled = g2d.getRenderingHint( RenderingHints.KEY_ANTIALIASING ) == RenderingHints.VALUE_ANTIALIAS_ON;

        // We enable antialiasing:
        if ( IS_ANTIALIASING_ENABLED() )
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        _render(UI.Layer.BACKGROUND, g2d);

        // Reset antialiasing to its previous state:
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasingWasEnabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF );
    }

    boolean hasNoPainters() {
        return _animationPainters.length == 0;
    }

    void paintAnimations( Graphics2D g2d )
    {
        // We remember if antialiasing was enabled before we render:
        boolean antialiasingWasEnabled = g2d.getRenderingHint( RenderingHints.KEY_ANTIALIASING ) == RenderingHints.VALUE_ANTIALIAS_ON;

        // We enable antialiasing:
        if ( StyleEngine.IS_ANTIALIASING_ENABLED() )
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        // Animations are last: they are rendered on top of everything else:
        for ( Expirable<Painter> expirablePainter : _animationPainters )
            if ( !expirablePainter.isExpired() ) {
                try {
                    expirablePainter.get().paint(g2d);
                } catch ( Exception e ) {
                    e.printStackTrace();
                    log.warn(
                        "Exception while painting animation '" + expirablePainter.get() + "' " +
                        "with lifetime " + expirablePainter.getLifeTime()+ ".",
                        e
                    );
                    // An exception inside a painter should not prevent everything else from being painted!
                    // Note that we log as warning because exceptions during rendering are not considered
                    // as harmful as elsewhere!
                }
            }

        // Reset antialiasing to its previous state:
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasingWasEnabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF );
    }

    void paintBorder( Graphics2D g2d )
    {
        // We remember if antialiasing was enabled before we render:
        boolean antialiasingWasEnabled = g2d.getRenderingHint( RenderingHints.KEY_ANTIALIASING ) == RenderingHints.VALUE_ANTIALIAS_ON;

        // We enable antialiasing:
        if ( IS_ANTIALIASING_ENABLED() )
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        _render(UI.Layer.CONTENT, g2d);
        _render(UI.Layer.BORDER, g2d);

        // Reset antialiasing to its previous state:
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasingWasEnabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF );
    }

    public void paintForeground( Graphics2D g2d )
    {
        // We remember if antialiasing was enabled before we render:
        boolean antialiasingWasEnabled = g2d.getRenderingHint( RenderingHints.KEY_ANTIALIASING ) == RenderingHints.VALUE_ANTIALIAS_ON;

        // We enable antialiasing:
        if ( IS_ANTIALIASING_ENABLED() )
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        _render(UI.Layer.FOREGROUND, g2d);

        // Reset antialiasing to its previous state:
        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasingWasEnabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF );
    }

    private void _render( UI.Layer layer, Graphics2D g2d ) {
        _layerCaches[layer.ordinal()].paint(this, g2d, graphics -> {
            StyleRenderer.renderStyleFor(this, layer, graphics);
        });
    }

}
