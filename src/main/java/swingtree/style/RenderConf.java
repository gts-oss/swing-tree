package swingtree.style;

import swingtree.UI;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Objects;

/**
 *  An immutable snapshot of essential component state needed for rendering
 *  the style of a component.
 *  This is immutable to use it as a basis for caching.
 *  When the snapshot changes compared to the previous one, the image buffer based
 *  render cache is being invalidated and the component is rendered again
 *  (potentially with a new cached image buffer).
 */
final class RenderConf
{
    private static final RenderConf _NONE = new RenderConf(
                                                    StructureConf.none(),
                                                    BaseColorConf.none(),
                                                    StyleLayer.empty(),
                                                    new ComponentAreas()
                                                );

    private final StructureConf _structureConf;
    private final BaseColorConf  _baseColor;
    private final StyleLayer     _layer;
    private final ComponentAreas _areas;

    private boolean _wasAlreadyHashed = false;
    private int     _hashCode         = 0; // cached hash code



    private RenderConf(
        StructureConf structureConf,
        BaseColorConf  base,
        StyleLayer     layers,
        ComponentAreas areas
    ) {
        _structureConf = Objects.requireNonNull(structureConf);
        _baseColor    = Objects.requireNonNull(base);
        _layer        = Objects.requireNonNull(layers);
        _areas        = Objects.requireNonNull(areas);
    }

    static RenderConf of(
        StructureConf structureConf,
        BaseColorConf  base,
        StyleLayer     layers,
        ComponentAreas areas
    ) {
        if (
            structureConf == StructureConf.none() &&
            base      == BaseColorConf.none() &&
            layers    == _NONE._layer &&
            areas     == _NONE._areas
        )
            return _NONE;
        else
            return new RenderConf(structureConf, base, layers, areas);
    }

    static RenderConf of(UI.Layer layer, ComponentConf fullConf) {
        StructureConf structureConf = StructureConf.of(
                                        fullConf.style().border(),
                                        fullConf.baseOutline(),
                                        fullConf.currentBounds().size()
                                    );
        BaseColorConf colorConf = BaseColorConf.of(
                                    fullConf.style().base().foundationColor().filter( c -> layer == UI.Layer.BACKGROUND ).orElse(null),
                                    fullConf.style().base().backgroundColor().filter( c -> layer == UI.Layer.BACKGROUND ).orElse(null),
                                    fullConf.style()
                                            .border()
                                            .color()
                                            .filter(c -> layer == UI.Layer.BORDER )
                                            .orElse(null)
                                );
        return of(
                    structureConf,
                    colorConf,
                    fullConf.style().layer(layer),
                    fullConf.areas()
                );
    }

    StructureConf structure() { return _structureConf; }

    BaseColorConf baseColors() { return _baseColor; }

    StyleLayer layer() { return _layer; }

    ComponentAreas areas() { return _areas; }


    void paintClippedTo(UI.ComponentArea area, Graphics g, Runnable painter ) {
        Shape oldClip = g.getClip();

        Shape newClip = get(area);
        if ( newClip != null && newClip != oldClip ) {
            newClip = StyleUtility.intersect(newClip, oldClip);
            g.setClip(newClip);
        }

        painter.run();

        g.setClip(oldClip);
    }

    public Area get(UI.ComponentArea areaType ) {
        switch ( areaType ) {
            case ALL:
                return null; // No clipping
            case BODY:
                return _areas.bodyArea().getFor(_structureConf, _areas); // all - exterior == interior + border
            case INTERIOR:
                return _areas.interiorArea().getFor(_structureConf, _areas); // all - exterior - border == content - border
            case BORDER:
                return _areas.borderArea().getFor(_structureConf, _areas); // all - exterior - interior
            case EXTERIOR:
                return _areas.exteriorArea().getFor(_structureConf, _areas); // all - border - interior
            default:
                return null;
        }
    }

    @Override
    public int hashCode() {
        if ( _wasAlreadyHashed )
            return _hashCode;

        _hashCode = Objects.hash(_structureConf, _baseColor, _layer);
        _wasAlreadyHashed = true;
        return _hashCode;
    }

    @Override
    public boolean equals( Object o ) {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass() != this.getClass() ) return false;
        RenderConf other = (RenderConf) o;
        return Objects.equals(_structureConf, other._structureConf) &&
               Objects.equals(_baseColor, other._baseColor) &&
               Objects.equals(_layer, other._layer);
    }


}
