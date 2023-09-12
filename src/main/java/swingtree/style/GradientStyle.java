package swingtree.style;

import swingtree.UI;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 *  An immutable, wither-like cloner method based settings API
 *  for specifying a gradient style as a sub-style of various other styles,
 *  like for example {@link BaseStyle} or {@link BorderStyle} accessed through the
 *  {@link ComponentStyleDelegate#gradient(String, Function)} or
 *  {@link ComponentStyleDelegate#borderGradient(String, Function)}
 *  methods.
 *  <p>
 *  Note that you can use the {@link #none()} method to specify that no gradient should be used,
 *  as the instance returned by that method is a gradient without any colors, effectively
 *  making it a representation of the absence of a gradient.
 *  <p>
 *  The following properties with their respective purpose are available:
 *  <br>
 *  <ol>
 *      <li><h3>Transition</h3>
 *          <p>
 *              The transition defines the direction of the gradient.
 *              <br>
 *              The following transitions are available:
 *          </p>
 *          <ul>
 *              <li>{@link UI.Transition#TOP_LEFT_TO_BOTTOM_RIGHT}</li>
 *              <li>{@link UI.Transition#BOTTOM_LEFT_TO_TOP_RIGHT}</li>
 *              <li>{@link UI.Transition#TOP_RIGHT_TO_BOTTOM_LEFT}</li>
 *              <li>{@link UI.Transition#BOTTOM_RIGHT_TO_TOP_LEFT}</li>
 *              <li>{@link UI.Transition#TOP_TO_BOTTOM}</li>
 *              <li>{@link UI.Transition#LEFT_TO_RIGHT}</li>
 *              <li>{@link UI.Transition#BOTTOM_TO_TOP}</li>
 *              <li>{@link UI.Transition#RIGHT_TO_LEFT}</li>
 *          </ul>
 *      </li>
 *      <li><h3>Type</h3>
 *          <p>
 *              The type defines the shape of the gradient
 *              which can be either linear or radial. <br>
 *              So the following types are available:
 *          </p>
 *          <ul>
 *              <li>{@link UI.GradientType#LINEAR}</li>
 *              <li>{@link UI.GradientType#RADIAL}</li>
 *          </ul>
 *      </li>
 *      <li><h3>Colors</h3>
 *          <p>
 *              An array of colors that will be used
 *              as a basis for the gradient transition.
 *          </p>
 *      </li>
 *      <li><h3>Layer</h3>
 *          <p>
 *              The layer defines if the gradient should be applied
 *              to the background or the border of a component.
 *          </p>
 *      </li>
 *  <ul>
 */
public final class GradientStyle
{
    private static final GradientStyle _NONE = new GradientStyle(
                                                        UI.Transition.TOP_TO_BOTTOM,
                                                        UI.GradientType.LINEAR,
                                                        new Color[0],
                                                        UI.Layer.BACKGROUND
                                                    );

    /**
     *  Use the returned instance as a representation of the absence of a gradient.
     *
     *  @return A gradient without any colors, effectively
     *          representing the absence of a gradient.
     */
    public static GradientStyle none() { return _NONE; }


    private final UI.Transition   _transition;
    private final UI.GradientType _type;
    private final Color[]         _colors;
    private final UI.Layer        _layer;


    private GradientStyle( UI.Transition transition, UI.GradientType type, Color[] colors, UI.Layer layer )
    {
        _transition = Objects.requireNonNull(transition);
        _type       = Objects.requireNonNull(type);
        _colors     = Objects.requireNonNull(colors);
        _layer      = Objects.requireNonNull(layer);
    }

    UI.Transition transition() { return _transition; }

    UI.GradientType type() { return _type; }

    Color[] colors() { return _colors; }

    UI.Layer layer() { return _layer; }

    /**
     *  Define a list of colors which will, as part of the gradient, transition from one
     *  to the next in the order they are specified.
     *  <p>
     *  Note that you need to specify at least two colors for a gradient to be visible.
     *
     * @param colors The colors in the gradient.
     * @return A new gradient style with the specified colors.
     * @throws NullPointerException if any of the colors is {@code null}.
     */
    public GradientStyle colors( Color... colors ) {
        Objects.requireNonNull(colors);
        for ( Color color : colors )
            Objects.requireNonNull(color);
        return new GradientStyle(_transition, _type, colors, _layer);
    }

    /**
     *  Define a list of {@link String} based colors which will, as part of the gradient, transition from one
     *  to the next in the order they are specified.
     *  <p>
     *  Note that you need to specify at least two colors for a gradient to be visible.
     *
     * @param colors The colors in the gradient in {@link String} format.
     * @return A new gradient style with the specified colors.
     * @throws NullPointerException if any of the colors is {@code null}.
     */
    public GradientStyle colors( String... colors ) {
        Objects.requireNonNull(colors);
        Color[] actualColors = new Color[colors.length];
        for ( int i = 0; i < colors.length; i++ )
            actualColors[i] = StyleUtility.toColor(colors[i]);
        return new GradientStyle(_transition, _type, actualColors, _layer);
    }

    /**
     *  Define the alignment of the gradient which is one of the following:
     *  <ul>
     *     <li>{@link UI.Transition#TOP_LEFT_TO_BOTTOM_RIGHT}</li>
     *     <li>{@link UI.Transition#BOTTOM_LEFT_TO_TOP_RIGHT}</li>
     *     <li>{@link UI.Transition#TOP_RIGHT_TO_BOTTOM_LEFT}</li>
     *     <li>{@link UI.Transition#BOTTOM_RIGHT_TO_TOP_LEFT}</li>
     *     <li>{@link UI.Transition#TOP_TO_BOTTOM}</li>
     *     <li>{@link UI.Transition#LEFT_TO_RIGHT}</li>
     *     <li>{@link UI.Transition#BOTTOM_TO_TOP}</li>
     *     <li>{@link UI.Transition#RIGHT_TO_LEFT}</li>
     *  </ul>
     *
     * @param transition The alignment of the gradient.
     * @return A new gradient style with the specified alignment.
     * @throws NullPointerException if the alignment is {@code null}.
     */
    public GradientStyle transition( UI.Transition transition ) {
        Objects.requireNonNull(transition);
        return new GradientStyle(transition, _type, _colors, _layer);
    }

    /**
     *  Define the type of the gradient which is one of the following:
     *  <ul>
     *     <li>{@link UI.GradientType#LINEAR}</li>
     *     <li>{@link UI.GradientType#RADIAL}</li>
     *  </ul>
     *
     * @param type The type of the gradient.
     * @return A new gradient style with the specified type.
     * @throws NullPointerException if the type is {@code null}.
     */
    public GradientStyle type( UI.GradientType type ) {
        Objects.requireNonNull(type);
        return new GradientStyle(_transition, type, _colors, _layer);
    }

    /**
     *  Define the layer of the gradient which is one of the following:
     *  <ul>
     *     <li>{@link UI.Layer#BACKGROUND}</li>
     *     <li>{@link UI.Layer#BORDER}</li>
     *  </ul>
     *
     * @param layer The layer of the gradient.
     * @return A new gradient style with the specified layer.
     * @throws NullPointerException if the layer is {@code null}.
     */
    public GradientStyle layer( UI.Layer layer ) {
        Objects.requireNonNull(layer);
        return new GradientStyle(_transition, _type, _colors, layer);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                    "transition=" + _transition + ", " +
                    "type="       + _type + ", " +
                    "colors="     + Arrays.toString(_colors) + ", " +
                    "layer="      + _layer                   +
                ']';
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !(o instanceof GradientStyle) ) return false;
        GradientStyle that = (GradientStyle) o;
        return _transition == that._transition       &&
               _type       == that._type             &&
               Arrays.equals(_colors, that._colors)  &&
               _layer      == that._layer;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(_transition);
        result = 31 * result + Objects.hash(_type);
        result = 31 * result + Arrays.hashCode(_colors);
        result = 31 * result + Objects.hash(_layer);
        return result;
    }

}
