package swingtree;

import org.jspecify.annotations.Nullable;
import sprouts.Var;
import sprouts.Vars;
import sprouts.Viewables;

import java.util.Objects;

class VarsBasedComboModel<E extends @Nullable Object> extends AbstractComboModel<E>
{
    private final Vars<E> _items;

    VarsBasedComboModel( Vars<E> items ) {
        super(Var.ofNullable(_findCommonType(items), null));
        _items         = Objects.requireNonNull(items);
        _selectedIndex = _indexOf(_getSelectedItemSafely());
        Viewables.cast(_items).onChange(it -> _itemListChanged() );
    }

    VarsBasedComboModel( Var<E> var, Vars<E> items ) {
        super(var);
        _items         = Objects.requireNonNull(items);
        _selectedIndex = _indexOf(_getSelectedItemSafely());
        Viewables.cast(_items).onChange( it -> _itemListChanged() );
    }

    private void _itemListChanged() {
        UI.run(()-> {
            int newSelection = _indexOf(_getSelectedItemSafely());
            if ( newSelection != _selectedIndex )
                this.setSelectedItem(_items.at(newSelection).orElseNull());
                // ^ This will fire the listeners for us already
            else
                fireListeners();
        });
    }

    @Override public int getSize() { return _items.size(); }

    @Override public @Nullable E getElementAt(int index ) { return _items.at(index).orElseNull(); }

    @Override public AbstractComboModel<E> withVar(Var<E> newVar) {
        return new VarsBasedComboModel<>(newVar, _items);
    }

    @Override
    protected void setAt(int index, @Nullable E element) {
        /*
            So the UI component tells us a combo option should be changed...
            But does the user of this library want that?
            Well there is a way to find out:
            Is the list we are using here intended to be modified?
            If so, then we should modify it, otherwise we should not.
            The problem is, we don't know if the list is unmodifiable or not.
            We could check if it is an instance of java.util.Collections.UnmodifiableList,
            but that is an internal class, so we can't rely on it.
            So we'll just try to modify it, and if it fails, we'll just ignore it.
         */
        try {
            _items.at(index).set(NullUtil.fakeNonNull(element));
        } catch ( UnsupportedOperationException ignored ) {
            // ignore, the user of this library doesn't want us to modify the list
        }
    }
}
