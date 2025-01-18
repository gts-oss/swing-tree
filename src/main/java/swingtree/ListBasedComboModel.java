package swingtree;

import org.jspecify.annotations.Nullable;
import sprouts.Var;

import java.util.List;
import java.util.Objects;

final class ListBasedComboModel<E extends @Nullable Object> extends AbstractComboModel<E>
{
	private final List<E> _items;

	ListBasedComboModel( List<E> items ) {
		super(Var.ofNullable(_findCommonType( (E[]) items.toArray() ), null));
		_items         = Objects.requireNonNull(items);
		_selectedIndex = _indexOf(_getSelectedItemSafely());
	}

	ListBasedComboModel( Var<E> var, List<E> items ) {
		super(var);
		_items         = Objects.requireNonNull(items);
		_selectedIndex = _indexOf(_getSelectedItemSafely());
	}

	@Override public int getSize() { return _items.size(); }
	@Override public E getElementAt( int index ) { return _items.get(index); }

	@Override
	public AbstractComboModel<E> withVar(Var<E> newVar) {
		return new ListBasedComboModel<>(newVar, _items);
	}

	@Override protected void setAt(int index, @Nullable E element) {
		/*
			So the UI component tells us a combo option should be changed...
			But does the user of this library want that?
			Well there is a way to find out:
			Is the list we are using here intended to be modified?
			If so, then we should modify it, otherwise we should not.
			The problem is, we don't know if the list is unmodifiable or not.
			We could check if it is an instance of java.util.Collections.UnmodifiableList,
			but that is a internal class, so we can't rely on it.
			So we'll just try to modify it, and if it fails, we'll just ignore it.
		 */
		try {
			_items.set(index, element);
		} catch (UnsupportedOperationException ignored) {
			// ignore, the user of this library doesn't want us to modify the list
		}
	}

}
