package swingtree.style;

import java.util.Objects;
import java.util.Optional;

public final class DimensionalityStyle
{
    private static final DimensionalityStyle _NONE = new DimensionalityStyle(
                                                    null, null,
                                                    null, null,
                                                    null, null,
                                                    null, null
                                                    );

    public static DimensionalityStyle none() { return _NONE; }

    private final Integer _minWidth;
    private final Integer _minHeight;

    private final Integer _maxWidth;
    private final Integer _maxHeight;

    private final Integer _preferredWidth;
    private final Integer _preferredHeight;

    private final Integer _width;
    private final Integer _height;


    public DimensionalityStyle(
        Integer minWidth,
        Integer minHeight,
        Integer maxWidth,
        Integer maxHeight,
        Integer preferredWidth,
        Integer preferredHeight,
        Integer width,
        Integer height
    ) {
        _minWidth        = minWidth;
        _minHeight       = minHeight;
        _maxWidth        = maxWidth;
        _maxHeight       = maxHeight;
        _preferredWidth  = preferredWidth;
        _preferredHeight = preferredHeight;
        _width           = width;
        _height          = height;
    }

    DimensionalityStyle _withMinWidth( Integer minWidth ) {
        return new DimensionalityStyle(minWidth, _minHeight, _maxWidth, _maxHeight, _preferredWidth, _preferredHeight, _width, _height);
    }

    DimensionalityStyle _withMinHeight( Integer minHeight ) {
        return new DimensionalityStyle(_minWidth, minHeight, _maxWidth, _maxHeight, _preferredWidth, _preferredHeight, _width, _height);
    }

    DimensionalityStyle _withMaxWidth( Integer maxWidth ) {
        return new DimensionalityStyle(_minWidth, _minHeight, maxWidth, _maxHeight, _preferredWidth, _preferredHeight, _width, _height);
    }

    DimensionalityStyle _withMaxHeight( Integer maxHeight ) {
        return new DimensionalityStyle(_minWidth, _minHeight, _maxWidth, maxHeight, _preferredWidth, _preferredHeight, _width, _height);
    }

    DimensionalityStyle _withPreferredWidth( Integer preferredWidth ) {
        return new DimensionalityStyle(_minWidth, _minHeight, _maxWidth, _maxHeight, preferredWidth, _preferredHeight, _width, _height);
    }

    DimensionalityStyle _withPreferredHeight( Integer preferredHeight ) {
        return new DimensionalityStyle(_minWidth, _minHeight, _maxWidth, _maxHeight, _preferredWidth, preferredHeight, _width, _height);
    }

    DimensionalityStyle _withWidth( Integer width ) {
        return new DimensionalityStyle(_minWidth, _minHeight, _maxWidth, _maxHeight, _preferredWidth, _preferredHeight, width, _height);
    }

    DimensionalityStyle _withHeight( Integer height ) {
        return new DimensionalityStyle(_minWidth, _minHeight, _maxWidth, _maxHeight, _preferredWidth, _preferredHeight, _width, height);
    }

    public Optional<Integer> minWidth() { return Optional.ofNullable(_minWidth); }

    public Optional<Integer> minHeight() { return Optional.ofNullable(_minHeight); }

    public Optional<Integer> maxWidth() { return Optional.ofNullable(_maxWidth); }

    public Optional<Integer> maxHeight() { return Optional.ofNullable(_maxHeight); }

    public Optional<Integer> preferredWidth() { return Optional.ofNullable(_preferredWidth); }

    public Optional<Integer> preferredHeight() { return Optional.ofNullable(_preferredHeight); }

    public Optional<Integer> width() { return Optional.ofNullable(_width); }

    public Optional<Integer> height() { return Optional.ofNullable(_height); }

    DimensionalityStyle _scale( double scale ) {
        return new DimensionalityStyle(
                    _minWidth        == null ? null : (int) Math.round( _minWidth        * scale ),
                    _minHeight       == null ? null : (int) Math.round( _minHeight       * scale ),
                    _maxWidth        == null ? null : (int) Math.round( _maxWidth        * scale ),
                    _maxHeight       == null ? null : (int) Math.round( _maxHeight       * scale ),
                    _preferredWidth  == null ? null : (int) Math.round( _preferredWidth  * scale ),
                    _preferredHeight == null ? null : (int) Math.round( _preferredHeight * scale ),
                    _width           == null ? null : (int) Math.round( _width           * scale ),
                    _height          == null ? null : (int) Math.round( _height          * scale )
                );
    }

    @Override
    public String toString() {
        return
            "DimensionalityStyle[" +
                    "minWidth="        + (_minWidth        == null ? "?" : _minWidth        ) + ", " +
                    "minHeight="       + (_minHeight       == null ? "?" : _minHeight       ) + ", " +
                    "maxWidth="        + (_maxWidth        == null ? "?" : _maxWidth        ) + ", " +
                    "maxHeight="       + (_maxHeight       == null ? "?" : _maxHeight       ) + ", " +
                    "preferredWidth="  + (_preferredWidth  == null ? "?" : _preferredWidth  ) + ", " +
                    "preferredHeight=" + (_preferredHeight == null ? "?" : _preferredHeight ) + ", " +
                    "width="           + (_width           == null ? "?" : _width           ) + ", " +
                    "height="          + (_height          == null ? "?" : _height          ) +
                "]";
    }

    @Override
    public final boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !(o instanceof DimensionalityStyle) ) return false;
        DimensionalityStyle that = (DimensionalityStyle) o;
        return Objects.equals(_minWidth,        that._minWidth       ) &&
               Objects.equals(_minHeight,       that._minHeight      ) &&
               Objects.equals(_maxWidth,        that._maxWidth       ) &&
               Objects.equals(_maxHeight,       that._maxHeight      ) &&
               Objects.equals(_preferredWidth,  that._preferredWidth ) &&
               Objects.equals(_preferredHeight, that._preferredHeight);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(_minWidth, _minHeight, _maxWidth, _maxHeight, _preferredWidth, _preferredHeight);
    }

}
