package com.globaltcad.swingtree.api.mvvm;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractVariable<T> implements Var<T>
{
	private final List<Consumer<Val<T>>> viewActions = new ArrayList<>();

	private T value;
	private final Class<T> type;
	private final String name;
	private final Consumer<Val<T>> action;


	protected AbstractVariable( Class<T> type, T iniValue, String name, Consumer<Val<T>> action ) {
		Objects.requireNonNull(name);
		this.value = iniValue;
		this.type = ( iniValue == null ? type : (Class<T>) iniValue.getClass());
		this.name = name;
		this.action = ( action == null ? v -> {} : action );
		if ( this.value != null ) {
			// We check if the type is correct
			if ( !type.isAssignableFrom(this.value.getClass()) )
				throw new IllegalArgumentException(
						"The provided type of the initial value is not compatible with the actual type of the variable"
					);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Class<T> type() { return type; }

	/**
	 * {@inheritDoc}
	 */
	@Override public String id() { return name; }

	/**
	 * {@inheritDoc}
	 */
	@Override public Var<T> withID( String id ) {
		AbstractVariable<T> newVar = new AbstractVariable<T>(type, value, id, null){};
		newVar.viewActions.addAll(viewActions);
		return newVar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Var<T> withAction(Consumer<Val<T>> action ) {
		Objects.requireNonNull(action);
		AbstractVariable<T> newVar = new AbstractVariable<T>(type, value, name, action){};
		newVar.viewActions.addAll(viewActions);
		return newVar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Var<T> act() {
		action.accept(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		// This class is similar to optional, so if the value is null, we throw an exception!
		if ( value == null )
			throw new NoSuchElementException("No value present");

		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPresent() { return value != null; }

	/**
	 * {@inheritDoc}
	 */
	public T orElseNullable(T other) {
		return value != null ? value : other;
	}

	/**
	 * {@inheritDoc}
	 */
	public T orElseThrow() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Var<T> set(T newValue) { value = newValue; return this; }

	/**
	 * {@inheritDoc}
	 */
	@Override public Val<T> onShowThis( Consumer<Val<T>> displayAction ) {
		this.viewActions.add(displayAction);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Val<T> show() {
		for ( Consumer<Val<T>> action : this.viewActions )
			try {
				action.accept(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return this;
	}

	@Override
	public String toString() {
		String asString = ( this.orElseNull() == null ? "null" : this.orElseNull().toString() );
		if ( id() == null ) return asString;
		else return
				asString +
						" ( " +
						"type='"+( type() == null ? "?" : type().getSimpleName() )+"', " +
						"name='"+ id()+"' " +
						")";
	}

	@Override
	public boolean equals( Object obj ) {
		if ( obj == null ) return false;
		if ( obj == this ) return true;
		if ( obj instanceof Val ) {
			Val<?> other = (Val<?>) obj;
			if ( other.type() != this.type ) return false;
			if ( other.orElseNull() == null ) return this.value == null;
			return Val.equals( other.get(), this.value ); // Arrays are compared with Arrays.equals
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + ( this.value == null ? 0 : Val.hashCode(this.value) );
		hash = 31 * hash + ( this.type  == null ? 0 : this.type.hashCode() );
		hash = 31 * hash + ( this.name  == null ? 0 : this.name.hashCode() );
		return hash;
	}
}
