package swingtree.style;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.util.*;
import java.util.List;

/**
 *  An immutable, wither-like cloner method based settings class for font styles
 *  that is part of the full {@link Style} configuration object.
 */
public final class FontStyle
{
    private static final FontStyle _NONE = new FontStyle("", 0, 0, 0, null, null, null, false, null, null, null);

    public static FontStyle none() { return _NONE; }

    private final String _name;
    private final int _size;
    private final float _posture;
    private final float _weight;
    private final Color _color;
    private final Color _backgroundColor;
    private final Color _selectionColor;
    private final Boolean _isUnderlined;
    private final Boolean _isStrike;
    private final AffineTransform _transform;
    private final Paint _paint;

    FontStyle(
        String name,
        int fontSize,
        float posture,
        float weight,
        Color color,
        Color backgroundColor,
        Color selectionColor,
        Boolean isUnderline,
        Boolean isStrike,
        AffineTransform transform,
        Paint paint
    ) {
        _name    = Objects.requireNonNull(name);
        _size    = fontSize;
        _posture = posture;
        _weight  = weight;
        _color   = color;
        _backgroundColor = backgroundColor;
        _selectionColor  = selectionColor;
        _isUnderlined    = isUnderline;
        _isStrike        = isStrike;
        _transform       = transform;
        _paint           = paint;
    }

    public String name() { return _name; }

    public int size() { return _size; }

    public float posture() { return _posture; }

    public float weight() { return _weight; }
    
    public Optional<Color> color() { return Optional.ofNullable(_color); }

    public Optional<Color> backgroundColor() { return Optional.ofNullable(_backgroundColor); }

    public Optional<Color> selectionColor() { return Optional.ofNullable(_selectionColor); }
    
    public boolean isUnderlined() { return _isUnderlined; }
    
    public Optional<AffineTransform> transform() { return Optional.ofNullable(_transform); }
    
    public Optional<Paint> paint() { return Optional.ofNullable(_paint); }

    FontStyle name( String fontFamily ) { return new FontStyle(fontFamily, _size, _posture, _weight, _color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, _paint); }

    FontStyle size( int fontSize ) { return new FontStyle(_name, fontSize, _posture, _weight, _color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, _paint); }

    FontStyle posture( float posture ) { return new FontStyle(_name, _size, posture, _weight, _color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, _paint); }

    FontStyle weight( float fontWeight ) { return new FontStyle(_name, _size, _posture, fontWeight, _color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, _paint); }

    FontStyle color( Color color ) { return new FontStyle(_name, _size, _posture, _weight, color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, _paint); }

    FontStyle backgroundColor( Color backgroundColor ) { return new FontStyle(_name, _size, _posture, _weight, _color, backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, _paint); }

    FontStyle selectionColor( Color selectionColor ) { return new FontStyle(_name, _size, _posture, _weight, _color, _backgroundColor, selectionColor, _isUnderlined, _isStrike, _transform, _paint); }

    FontStyle isUnderlined( boolean underlined ) { return new FontStyle(_name, _size, _posture, _weight, _color, _backgroundColor, _selectionColor, underlined, _isStrike, _transform, _paint); }

    FontStyle isStrike( boolean strike ) { return new FontStyle(_name, _size, _posture, _weight, _color, _backgroundColor, _selectionColor, _isUnderlined, strike, _transform, _paint); }

    FontStyle transform( AffineTransform transform ) { return new FontStyle(_name, _size, _posture, _weight, _color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike, transform, _paint); }

    FontStyle paint( Paint paint ) { return new FontStyle(_name, _size, _posture, _weight, _color, _backgroundColor, _selectionColor, _isUnderlined, _isStrike,  _transform, paint); }

    FontStyle font( Font font ) {
        Map<TextAttribute, ?> attributeMap = font.getAttributes();
        Color   color       = null;
        Color   background  = null;
        boolean isUnderline = false;
        boolean isStriked   = false;
        AffineTransform transform = null;
        Paint paint = null;
        try {
            isUnderline = Objects.equals(attributeMap.get(TextAttribute.UNDERLINE), TextAttribute.UNDERLINE_ON);
            isStriked = Objects.equals(attributeMap.get(TextAttribute.STRIKETHROUGH), TextAttribute.STRIKETHROUGH_ON);
            paint = (Paint) attributeMap.get(TextAttribute.FOREGROUND);
            transform = (AffineTransform) attributeMap.get(TextAttribute.TRANSFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(font);
        return new FontStyle(
                    font.getFamily(),
                    font.getSize(),
                    font.isItalic() ? 0.2f : 0f,
                    font.isBold() ? 2f : 0f,
                    _color,
                    _backgroundColor,
                    _selectionColor,
                    isUnderline,
                    isStriked,
                    transform,
                    paint
                );
    }

    public Optional<Font> createDerivedFrom( Font existingFont )
    {
        if ( this.equals(_NONE) )
            return Optional.empty();

        boolean isChange = false;

        if ( existingFont == null )
            existingFont = new JLabel().getFont();

        Map<TextAttribute, Object> currentAttributes = (Map<TextAttribute, Object>) existingFont.getAttributes();
        Map<TextAttribute, Object> attributes = new HashMap<>();

        if ( _size > 0 ) {
            isChange = isChange || !Integer.valueOf(_size).equals(currentAttributes.get(TextAttribute.SIZE));
            attributes.put(TextAttribute.SIZE, _size);
        }
        if ( _posture > 0 ) {
            isChange = isChange || !Float.valueOf(_posture).equals(currentAttributes.get(TextAttribute.POSTURE));
            attributes.put(TextAttribute.POSTURE, _posture);
        }
        if ( _weight > 0 ) {
            isChange = isChange || !Float.valueOf(_weight).equals(currentAttributes.get(TextAttribute.WEIGHT));
            attributes.put(TextAttribute.WEIGHT, _weight);
        }
        if ( _isUnderlined != null ) {
            isChange = isChange || !Objects.equals(_isUnderlined, currentAttributes.get(TextAttribute.UNDERLINE));
            attributes.put(TextAttribute.UNDERLINE, _isUnderlined);
        }
        if ( _isStrike != null ) {
            isChange = isChange || !Objects.equals(_isStrike, currentAttributes.get(TextAttribute.STRIKETHROUGH));
            attributes.put(TextAttribute.STRIKETHROUGH, _isStrike);
        }
        if ( _transform != null ) {
            isChange = isChange || !Objects.equals(_transform, currentAttributes.get(TextAttribute.TRANSFORM));
            attributes.put(TextAttribute.TRANSFORM, _transform);
        }
        if ( _paint != null ) {
            isChange = isChange || !Objects.equals(_paint, currentAttributes.get(TextAttribute.FOREGROUND));
            attributes.put(TextAttribute.FOREGROUND, _paint);
        }
        if ( !_name.isEmpty() ) {
            isChange = isChange || !Objects.equals(_name, currentAttributes.get(TextAttribute.FAMILY));
            attributes.put(TextAttribute.FAMILY, _name);
        }
        if ( _color != null ) {
            isChange = isChange || !Objects.equals(_color, currentAttributes.get(TextAttribute.FOREGROUND));
            attributes.put(TextAttribute.FOREGROUND, _color);
        }
        if ( _backgroundColor != null ) {
            isChange = isChange || !Objects.equals(_backgroundColor, currentAttributes.get(TextAttribute.BACKGROUND));
            attributes.put(TextAttribute.BACKGROUND, _backgroundColor);
        }

        if ( isChange )
            return Optional.of(existingFont.deriveFont(attributes));
        else
            return Optional.empty();
    }

    FontStyle _scale( double scale ) {
        return new FontStyle(
                    _name,
                    (int) Math.round(_size * scale),
                    _posture,
                    _weight,
                    _color,
                    _backgroundColor,
                    _selectionColor, 
                    _isUnderlined,
                    _isStrike,
                    _transform,
                    _paint
                );
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(_name);
        hash = 97 * hash + _size;
        hash = 97 * hash + Float.hashCode(_posture);
        hash = 97 * hash + Float.hashCode(_weight);
        hash = 97 * hash + Objects.hashCode(_color);
        hash = 97 * hash + Objects.hashCode(_backgroundColor);
        hash = 97 * hash + Objects.hashCode(_selectionColor);
        hash = 97 * hash + Objects.hashCode(_isUnderlined);
        hash = 97 * hash + Objects.hashCode(_transform);
        hash = 97 * hash + Objects.hashCode(_paint);
        return hash;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        final FontStyle other = (FontStyle)obj;
        if ( !Objects.equals(_name, other._name) )
            return false;
        if ( _size != other._size )
            return false;
        if ( _posture != other._posture)
            return false;
        if ( _weight != other._weight )
            return false;
        if ( !Objects.equals(_color, other._color) )
            return false;
        if ( !Objects.equals(_backgroundColor, other._backgroundColor) )
            return false;
        if ( !Objects.equals(_selectionColor, other._selectionColor) )
            return false;
        if ( !Objects.equals(_isUnderlined, other._isUnderlined) )
            return false;
        if ( !Objects.equals(_transform, other._transform) )
            return false;
        if ( !Objects.equals(_paint, other._paint) )
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "FontStyle[" +
                    "name="            + _name + ", " +
                    "size="            + _size + ", " +
                    "posture="         + _posture + ", " +
                    "weight="          + _weight + ", " +
                    "underlined="      + (_isUnderlined == null ? "?" : _isUnderlined) + ", " +
                    "strikeThrough="   + (_isStrike == null ? "?" : _isStrike) + ", " +
                    "color="           + StyleUtility.toString(_color) + ", " +
                    "backgroundColor=" + StyleUtility.toString(_backgroundColor) + ", " +
                    "selectionColor="  + StyleUtility.toString(_selectionColor) + ", " +
                    "transform="       + ( _transform == null ? "?" : _transform.toString() ) + ", " +
                    "paint="           + ( _paint == null ? "?" : _paint.toString() ) +
                "]";
    }
}
