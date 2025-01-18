package swingtree;

import org.jspecify.annotations.Nullable;
import sprouts.Var;
import sprouts.Vals;

import java.util.Objects;

class ValsBasedComboModel<E extends @Nullable Object> extends AbstractComboModel<E>
{
    private final Vals<E> _items;

    ValsBasedComboModel( Vals<E> items ) {
        super(Var.ofNullable(_findCommonType(items), null));
        _items         = Objects.requireNonNull(items);
        _selectedIndex = _indexOf(_getSelectedItemSafely());
    }

    ValsBasedComboModel( Var<E> var, Vals<E> items ) {
        super(var);
        _items         = Objects.requireNonNull(items);
        _selectedIndex = _indexOf(_getSelectedItemSafely());
    }

    @Override public int getSize() { return _items.size(); }

    @Override public @Nullable E getElementAt(int index ) { return _items.at(index).orElseNull(); }

    @Override public AbstractComboModel<E> withVar( Var<E> newVar ) {
        return new ValsBasedComboModel<>(newVar, _items);
    }

    @Override
    protected void setAt( int index, @Nullable E element ) {
        /*
            Vals are immutable, so we can't modify them.
            So we'll just ignore it.
        */
    }
}
