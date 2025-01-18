package swingtree;

import org.jspecify.annotations.Nullable;
import sprouts.From;
import sprouts.Val;
import sprouts.Var;

final class ArrayPropertyComboModel<E extends @Nullable Object> extends AbstractComboModel<E>
{
	private final Val<E[]> _items;
	private final boolean _mutable;

	ArrayPropertyComboModel( Var<E> selection, Val<E[]> items ) {
		super(selection);
		_items = items;
		_selectedIndex = _indexOf(_getSelectedItemSafely());
		_mutable = false;
	}

	ArrayPropertyComboModel( Var<E> selection, Var<E[]> items ) {
		super(selection);
		_items = items;
		_selectedIndex = _indexOf(_getSelectedItemSafely());
		_mutable = true;
	}

	@Override
	public AbstractComboModel<E> withVar( Var<E> newVar ) {
		return new ArrayPropertyComboModel<>(newVar, _items);
	}

	@Override protected void setAt(int index, @Nullable E element) {
		if ( _mutable )
			_items.ifPresent(i -> {
				i[index] = element;
				if ( _items instanceof Var ) ((Var<E>) _items).fireChange(From.VIEW);
			});
	}
	@Override public int getSize() { return _items.mapTo(Integer.class, i -> i.length ).orElse(0); }
	@Override public @Nullable E getElementAt( int index ) {
		return (E) _items.mapTo(Object.class, i -> i[index] ).orElseNull();
	}

}