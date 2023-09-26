package swingtree.style;

import swingtree.UI;
import swingtree.api.IconDeclaration;

import javax.swing.ImageIcon;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 *  This class represents the style of an image which can be drawn onto the inner
 *  area of a component.
 *  <b>Note that the inner component area is the area enclosed by the border, which
 *  is itself not part of said area!</b>
 *  <p>
 *  The following properties with their respective purpose are available:
 *  <br>
 *  <ol>
 *      <li><h3>Layer:</h3>
 *          The layer onto which the image will be drawn.
 *          Layers exist to determine the order in which something is drawn onto the component.
 *          Here a list of available layers:
 *          <ul>
 *              <li>{@link swingtree.UI.Layer#BACKGROUND}</li>
 *              <li>{@link swingtree.UI.Layer#CONTENT}   </li>
 *              <li>{@link swingtree.UI.Layer#BORDER}    </li>
 *              <li>{@link swingtree.UI.Layer#FOREGROUND}</li>
 *          </ul>
 *      </li>
 *      <li><h3>Primer:</h3>
 *          The primer color of the image style which will
 *          be used as a filler color for the image background.
 *          The background is the inner component area of the component.
 *      </li>
 *      <li><h3>Image:</h3>
 *          The image which will be drawn onto the component,
 *          which may be specified as an instance of {@link Image}, {@link ImageIcon}
 *          or path to an image file (see {@link swingtree.UI#findIcon(String)}).
 *      </li>
 *      <li><h3>Placement:</h3>
 *          The placement type determines where the image will be drawn onto the component.
 *          The following placement options are available:
 *          <ul>
 *              <li> {@link swingtree.UI.Placement#CENTER} </li>
 *              <li> {@link swingtree.UI.Placement#TOP_LEFT} </li>
 *              <li> {@link swingtree.UI.Placement#TOP_RIGHT} </li>
 *              <li> {@link swingtree.UI.Placement#BOTTOM_LEFT} </li>
 *              <li> {@link swingtree.UI.Placement#BOTTOM_RIGHT} </li>
 *              <li> {@link swingtree.UI.Placement#TOP} </li>
 *              <li> {@link swingtree.UI.Placement#BOTTOM} </li>
 *              <li> {@link swingtree.UI.Placement#LEFT} </li>
 *              <li> {@link swingtree.UI.Placement#RIGHT} </li>
 *          </ul>
 *      </li>
 *      <li><h3>Repeat:</h3>
 *          If this flag is set to {@code true}, then the image may be painted
 *          multiple times so that it fills up the entire inner component area.
 *          There will not be a noticeable effect of this flag if the
 *          image already fills out the inner component area (see {@link #autoFit(boolean)}, {@link #size(int, int)}).
 *      </li>
 *      <li><h3>Auto-fit:</h3>
 *          If this flag is set to {@code true}, then the image will be stretched or shrunk
 *          to fill the inner component area dependent on the specified width and height,
 *          meaning that if the width was not specified explicitly through {@link #width(Integer)}
 *          then the image will be scaled to fit the inner component width,
 *          and if a height was not specified through {@link #height(Integer)} then
 *          the image will be scaled to fit the inner component height. <br>
 *          <b>Note that the inner component area is the area enclosed by the border, which
 *          is itself not part of said area!</b>
 *      </li>
 *      <li><h3>Width and Height:</h3>
 *          These properties allow you to specify the width and height of the image.
 *          If the width or height is not specified, then the image will be drawn
 *          with its original width or height or it will be scaled to fit the inner component area
 *          if {@link #autoFit(boolean)} is set to {@code true}.
 *      </li>
 *      <li><h3>Opacity:</h3>
 *          This property allows you to specify the opacity of the image.
 *          The opacity must be between 0.0f and 1.0f, where 0.0f means that the image is completely transparent
 *          and 1.0f means that the image is completely opaque.
 *      </li>
 *      <li><h3>Padding:</h3>
 *          This property allows you to specify the padding of the image.
 *          The padding is the space between the image and the inner component area.
 *          The padding can be specified for each side of the image individually
 *          or for all sides at once.
 *      </li>
 *  </ol>
 *  <p>
 *  <b>Take a look at the following example:</b>
 *  <pre>{@code
 *      of(component).withStyle( it -> it
 *          .image( image -> image
 *              .layer(Layer.BACKGROUND)
 *              .image(image)
 *              .placement(Placement.CENTER)
 *              .autoFit(false)
 *              .repeat(true)
 *              .primer(Color.CYAN)
 *              .padding(12)
 *          )
 *      );
 *  }</pre>
 *  <p>
 *      This will draw the specified image onto the background layer of the component.
 *      The image will be drawn at the center of the inner component area with a padding of 12,
 *      without being scaled to fit the inner component area, instead the size of the image
 *      will be used. <br>
 *      If it does not fill the entire inner component area based on its size, then
 *      it will be repeated across said area, and the primer color
 *      will be used as a filler color for the parts of the image which
 *      are transparent.
 *  </p>
 **/
public final class ImageStyle
{
    private static final ImageStyle _NONE = new ImageStyle(
                                                UI.Layer.BACKGROUND,
                                                null,
                                                null,
                                                UI.Placement.CENTER,
                                                false,
                                                false,
                                                null,
                                                null,
                                                1.0f,
                                                Outline.none()
                                            );

    static ImageStyle none() { return _NONE; }


    private final UI.Layer     _layer;
    private final Color        _primer;

    private final ImageIcon    _image;
    private final UI.Placement _placement;
    private final boolean      _repeat;
    private final boolean      _autoFit;
    private final Integer      _width;
    private final Integer      _height;
    private final float        _opacity;
    private final Outline      _padding;


    private ImageStyle(
        UI.Layer     layer,
        Color        primer,
        ImageIcon    image,
        UI.Placement placement,
        boolean      repeat,
        boolean      autoFit,
        Integer      width,
        Integer      height,
        float        opacity,
        Outline      padding
    ) {
        _layer     = Objects.requireNonNull(layer);
        _primer    = primer;
        _image     = image;
        _placement = Objects.requireNonNull(placement);
        _repeat    = repeat;
        _autoFit   = autoFit;
        _width     = width;
        _height    = height;
        _opacity   = opacity;
        _padding   = Objects.requireNonNull(padding);
        if ( _opacity < 0.0f || _opacity > 1.0f )
            throw new IllegalArgumentException("transparency must be between 0.0f and 1.0f");
    }

    public UI.Layer layer() { return _layer; }

    public Optional<Color> primer() { return Optional.ofNullable(_primer); }

    public Optional<ImageIcon> image() { return Optional.ofNullable(_image); }

    public UI.Placement placement() { return _placement; }

    public boolean repeat() { return _repeat; }

    public boolean autoFit() { return _autoFit; }

    public Optional<Integer> width() { return Optional.ofNullable(_width); }

    public Optional<Integer> height() { return Optional.ofNullable(_height); }

    public float opacity() { return _opacity; }

    public Outline padding() { return _padding; }

    /**
     *  This method allows you to specify the layer onto which the image will be drawn.
     *  The default layer is the background layer. <br>
     *  Here a list of available layers:
     *  <ul>
     *      <li>{@link swingtree.UI.Layer#BACKGROUND}</li>
     *      <li>{@link swingtree.UI.Layer#CONTENT}</li>
     *      <li>{@link swingtree.UI.Layer#BORDER}</li>
     *      <li>{@link swingtree.UI.Layer#FOREGROUND}</li>
     *  </ul>
     *
     * @param layer The layer onto which the image will be drawn.
     * @return A new {@link ImageStyle} instance with the specified layer.
     */
    public ImageStyle layer( UI.Layer layer ) { return new ImageStyle(layer, _primer, _image, _placement, _repeat, _autoFit, _width, _height, _opacity, _padding); }

    /**
     *  Here you can specify the <b>primer color of the image style</b> which will be used
     *  as a filler color for the image background. <br>
     *  Note that the primer color will not be visible if the image is opaque and it fills the entire component.
     *
     * @param color The primer color of the image style.
     * @return A new {@link ImageStyle} instance with the specified primer color.
     */
    public ImageStyle primer( Color color ) { return new ImageStyle(_layer, color, _image, _placement, _repeat, _autoFit, _width, _height, _opacity, _padding); }

    /**
     *  Here you can specify the <b>image</b> which will be drawn onto the component.
     *  The supplied object must be an instance of {@link Image} implementation.
     *
     * @param image The image which will be drawn onto the component.
     * @return A new {@link ImageStyle} instance with the specified image.
     */
    public ImageStyle image( Image image ) { return new ImageStyle(_layer, _primer, image == null ? null : new ImageIcon(image), _placement, _repeat, _autoFit, _width, _height, _opacity, _padding); }

    /**
     *  Here you can specify the <b>image icon</b> which will be drawn onto the component.
     *  The supplied object must be an instance of {@link ImageIcon} implementation.
     *
     * @param image The image icon which will be drawn onto the component.
     * @return A new {@link ImageStyle} instance with the specified image.
     */
    public ImageStyle image( ImageIcon image ) { return new ImageStyle(_layer, _primer, image, _placement, _repeat, _autoFit, _width, _height, _opacity, _padding); }

    /**
     *  Here you can specify the <b>path to the image in the form of an {@link IconDeclaration}</b>
     *  for which the icon will be loaded and drawn onto the component.
     *  If the icon could not be found, then the image will not be drawn.
     *  The path is relative to the classpath or may be an absolute path.
     *
     * @param image The path to the (icon) image in the form of an {@link IconDeclaration}.
     * @return A new {@link ImageStyle} instance with the specified image.
     */
    public ImageStyle image( IconDeclaration image ) { return image.find().map(this::image).orElse(this); }

    /**
     *  Here you can specify the <b>placement</b> of the image onto the component.
     *  The default placement is {@link swingtree.UI.Placement#CENTER}. <br>
     *  Here a list of available options and their effect:
     *  <ul>
     *      <li>{@link swingtree.UI.Placement#CENTER} -
     *          The image will be drawn at the center of the component.
     *          So the center of the image will be at the center of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#TOP_LEFT} -
     *          The image will be drawn beginning at the top left corner of the inner component area.
     *          So the top left corner of the image will be in the top left corner of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#TOP_RIGHT} -
     *          The image will be placed in the top right corner of the inner component area.
     *          So the top right corner of the image will be in the top right corner of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#BOTTOM_LEFT} -
     *          The image will be drawn in the bottom left corner of the inner component area.
     *          So the bottom left corner of the image will be in the bottom left corner of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#BOTTOM_RIGHT} -
     *          The image will be drawn in the bottom right corner of the inner component area.
     *          So the bottom right corner of the image will be in the bottom right corner of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#TOP} -
     *          The image will be drawn in the top center of the inner component area.
     *          So the top center of the image will be in the top center of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#BOTTOM} -
     *          The image will be drawn in the bottom center of the inner component area.
     *          So the bottom center of the image will be in the bottom center of the inner component area.
     *      </li>
     *      <li>{@link swingtree.UI.Placement#LEFT} -
     *          The image will be drawn in the left center of the inner component area.
     *          So the left center of the image will be in the left center of the inner component area.
     *      </li>
     *  </ul>
     *
     * @param placement The placement of the image onto the component.
     * @return A new {@link ImageStyle} instance with the specified placement.
     */
    public ImageStyle placement( UI.Placement placement ) { return new ImageStyle(_layer, _primer, _image, placement, _repeat, _autoFit, _width, _height, _opacity, _padding); }

    /**
     *  If this flag is set to {@code true}, then the image may be painted
     *  multiple times so that it fills up the entire inner component area.
     *  There will not be a noticeable effect of this flag if the
     *  image already fills out the inner component area (see {@link #autoFit(boolean)}, {@link #size(int, int)}).
     *
     * @param repeat Weather the image should be painted repeatedly across the inner component area.
     * @return A new {@link ImageStyle} instance with the specified {@code repeat} flag value.
     */
    public ImageStyle repeat( boolean repeat ) { return new ImageStyle(_layer, _primer, _image, _placement, repeat, _autoFit, _width, _height, _opacity, _padding); }

    /**
     *  If this flag is set to {@code true}, then the image will be stretched or shrunk
     *  to fill the inner component area dependent on the specified width and height,
     *  meaning that if the width was not specified explicitly through {@link #width(Integer)}
     *  then the image will be scaled to fit the inner component width,
     *  and if a height was not specified through {@link #height(Integer)} then
     *  the image will be scaled to fit the inner component height. <br>
     *  <b>Note that the inner component area is the area enclosed by the border, which
     *  is itself not part of said area!</b>
     *
     * @param autoFit If true the image will be scaled to fit the inner component area for every
     *                dimension which was not specified,
     *                otherwise the image will not be scaled to fit the inner component area.
     * @return A new {@link ImageStyle} instance with the specified {@code autoFit} flag value.
     */
    public ImageStyle autoFit( boolean autoFit ) { return new ImageStyle(_layer, _primer, _image, _placement, _repeat, autoFit, _width, _height, _opacity, _padding); }

    /**
     *  Ensures that the image has the specified width.
     *
     * @param width The width of the image.
     * @return A new {@link ImageStyle} instance with the specified {@code width}.
     */
    public ImageStyle width( Integer width ) { return new ImageStyle(_layer, _primer, _image, _placement, _repeat, _autoFit, width, _height, _opacity, _padding); }

    /**
     *  Ensures that the image has the specified height.
     *
     * @param height The height of the image.
     * @return A new {@link ImageStyle} instance with the specified {@code heiht}.
     */
    public ImageStyle height( Integer height ) { return new ImageStyle(_layer, _primer, _image, _placement, _repeat, _autoFit, _width, height, _opacity, _padding); }

    /**
     *  Ensures that the image has the specified width and height.
     *
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A new {@link ImageStyle} instance with the specified {@code width} and {@code height}.
     */
    public ImageStyle size( int width, int height ) { return new ImageStyle(_layer, _primer, _image, _placement, _repeat, _autoFit, width, height, _opacity, _padding); }

    /**
     *  This method allows you to specify the opacity of the image.
     *  The opacity must be between 0.0f and 1.0f, where 0.0f means that the image is completely transparent
     *  and 1.0f means that the image is completely opaque.
     *
     * @param opacity The opacity of the image.
     * @return A new {@link ImageStyle} instance with the specified opacity.
     */
    public ImageStyle opacity( float opacity ) { return new ImageStyle(_layer, _primer, _image, _placement, _repeat, _autoFit, _width, _height, opacity, _padding); }

    /**
     *  This method allows you to specify the padding of the image.
     *  The padding is the space between the image and the inner component area.
     *
     * @param padding The padding of the image.
     * @return A new {@link ImageStyle} instance with the specified padding.
     */
    ImageStyle padding( Outline padding ) { return new ImageStyle(_layer, _primer, _image, _placement, _repeat, _autoFit, _width, _height, _opacity, padding); }

    /**
     *  This method allows you to specify the padding of the image.
     *  The padding is the space between the image and the inner component area.
     *
     * @param top The top padding of the image.
     * @param right The right padding of the image.
     * @param bottom The bottom padding of the image.
     * @param left The left padding of the image.
     * @return A new {@link ImageStyle} instance with the specified padding.
     */
    public ImageStyle padding( int top, int right, int bottom, int left ) { return padding(Outline.of(top, right, bottom, left)); }

    /**
     *  This method allows you to specify the padding of the image.
     *  The padding is the space between the image and the inner component area.
     *
     * @param topBottom The top and bottom padding of the image.
     * @param leftRight The left and right padding of the image.
     * @return A new {@link ImageStyle} instance with the specified padding.
     */
    public ImageStyle padding( int topBottom, int leftRight ) { return padding(Outline.of(topBottom, leftRight, topBottom, leftRight)); }

    /**
     *  This method allows you to specify the padding for all sides of the image.
     *  The padding is the space between the image and the inner component area.
     *
     * @param padding The padding of the image.
     * @return A new {@link ImageStyle} instance with the specified padding.
     */
    public ImageStyle padding( int padding ) { return padding(Outline.of(padding, padding, padding, padding)); }

    ImageStyle _scale( double scaleFactor ) {
        if ( _width == null && _height == null ) return this;
        Integer width  = _width  == null ? null : (int) Math.round(_width  * scaleFactor);
        Integer height = _height == null ? null : (int) Math.round(_height * scaleFactor);
        return new ImageStyle(_layer, _primer, _image, _placement, _repeat, _autoFit, width, height, _opacity, _padding.scale(scaleFactor));
    }

    @Override
    public int hashCode() { return Objects.hash(_layer, _primer, _image, _placement, _repeat, _autoFit, _width, _height, _opacity, _padding); }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null ) return false;
        if ( obj == this ) return true;
        if ( obj.getClass() != getClass() ) return false;
        ImageStyle rhs = (ImageStyle) obj;
        return Objects.equals(_layer,     rhs._layer)     &&
               Objects.equals(_primer,    rhs._primer)    &&
               Objects.equals(_image,     rhs._image)     &&
               Objects.equals(_placement, rhs._placement) &&
               Objects.equals(_repeat,    rhs._repeat)    &&
               Objects.equals(_autoFit,   rhs._autoFit)   &&
               Objects.equals(_width,     rhs._width)     &&
               Objects.equals(_height,    rhs._height)    &&
               Objects.equals(_opacity,   rhs._opacity)   &&
               Objects.equals(_padding,   rhs._padding);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" +
                    "layer="      + _layer                                         + ", " +
                    "primer="     + StyleUtility.toString(_primer)                 + ", " +
                    "image="      + ( _image  == null ? "?" : _image.toString() )  + ", " +
                    "placement="  + _placement                                     + ", " +
                    "repeat="     + _repeat                                        + ", " +
                    "autoFit="    + _autoFit                                       + ", " +
                    "width="      + ( _width  == null ? "?" : _width.toString()  ) + ", " +
                    "height="     + ( _height == null ? "?" : _height.toString() ) + ", " +
                    "opacity="    + _opacity                                       + ", " +
                    "padding="    + _padding                                       +
                "]";
    }
}
