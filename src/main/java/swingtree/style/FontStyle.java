package swingtree.style;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;

public class FontStyle
{
    private final String _name;
    private final int _size;
    private final int _style;
    private final int _weight;
    private final List<TextAttribute> _attributes;
    private final Color _color;
    private final Color _backgroundColor;

    public FontStyle(
        String name,
        int fontSize,
        int fontStyle,
        int fontWeight,
        List<TextAttribute> attributes,
        Color color,
        Color backgroundColor
    ) {
        _name = Objects.requireNonNull(name);
        _size = fontSize;
        _style = fontStyle;
        _weight = fontWeight;
        _attributes = Collections.unmodifiableList(Objects.requireNonNull(attributes));
        _color = color;
        _backgroundColor = backgroundColor;
    }

    public String name() { return _name; }

    public int size() { return _size; }

    public int style() { return _style; }

    public int weight() { return _weight; }
    
    public List<TextAttribute> attributes() { return _attributes; }

    public Optional<Color> color() { return Optional.ofNullable(_color); }

    public Optional<Color> backgroundColor() { return Optional.ofNullable(_backgroundColor); }

    FontStyle withName( String fontFamily ) { return new FontStyle(fontFamily, _size, _style, _weight, _attributes, _color, _backgroundColor); }

    FontStyle withSize( int fontSize ) { return new FontStyle(_name, fontSize, _style, _weight, _attributes, _color, _backgroundColor); }

    FontStyle withStyle( int fontStyle ) { return new FontStyle(_name, _size, fontStyle, _weight, _attributes, _color, _backgroundColor); }

    FontStyle withWeight( int fontWeight ) { return new FontStyle(_name, _size, _style, fontWeight, _attributes, _color, _backgroundColor); }

    FontStyle withAttributes( TextAttribute... attributes ) {
        Objects.requireNonNull(attributes);
        return new FontStyle(_name, _size, _style, _weight, Arrays.asList(attributes), _color, _backgroundColor);
    }

    FontStyle withAttributes( List<TextAttribute> attributes ) {
        Objects.requireNonNull(attributes);
        return new FontStyle(_name, _size, _style, _weight, attributes, _color, _backgroundColor);
    }

    FontStyle withColor( Color color ) { return new FontStyle(_name, _size, _style, _weight, _attributes, color, _backgroundColor); }

    FontStyle withBackgroundColor( Color backgroundColor ) { return new FontStyle(_name, _size, _style, _weight, _attributes, _color, backgroundColor); }

    public Font createDerivedFrom(Font existingFont )
    {
        if ( existingFont == null )
            existingFont = new JLabel().getFont();

        Map<TextAttribute, Object> attributes = new HashMap<>();
        if ( _size > 0 )
            attributes.put(TextAttribute.SIZE, _size);
        if ( _style > 0 )
            attributes.put(TextAttribute.POSTURE, _style);
        if ( _weight > 0 )
            attributes.put(TextAttribute.WEIGHT, _weight);
        if ( _attributes != null )
            _attributes.forEach( attr -> attributes.put(attr, attr));
        if ( !_name.isEmpty() )
            attributes.put(TextAttribute.FAMILY, _name);
        if ( _color != null )
            attributes.put(TextAttribute.FOREGROUND, _color);
        if ( _backgroundColor != null )
            attributes.put(TextAttribute.BACKGROUND, _backgroundColor);

        return existingFont.deriveFont(attributes);
    }
}