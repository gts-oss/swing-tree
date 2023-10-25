package swingtree;

import sprouts.Change;
import sprouts.Vals;
import sprouts.Var;
import swingtree.api.mvvm.EntryViewModel;
import swingtree.api.mvvm.ViewSupplier;
import swingtree.components.JScrollPanels;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 *  A builder node for {@link JScrollPanels}, a custom SwingTree component,
 *  which is similar to a {@link JList} but with the ability to interact with
 *  the individual components in the list.
 *  <p>
 *
 * @param <P> The type of the component which this builder node wraps.
 */
public class UIForScrollPanels<P extends JScrollPanels> extends UIForScrollPane<P>
{
	/**
	 * Extensions of the {@link  UIForAnySwing} always wrap
	 * a single component for which they are responsible.
	 *
	 * @param component The JComponent type which will be wrapped by this builder node.
	 */
	protected UIForScrollPanels( P component ) { super(component); }


	@Override
	protected void _doAddComponent(JComponent newComponent, Object conf, P thisComponent ) {
		Objects.requireNonNull(newComponent);

		EntryViewModel entry = _entryModel();
		if ( conf == null )
			thisComponent.addEntry( entry, m -> UI.of(newComponent) );
		else
			thisComponent.addEntry( conf.toString(), entry, m -> UI.of(newComponent) );
	}

	private EntryViewModel _entryModel() {
		Var<Boolean> selected = Var.of(false);
		Var<Integer> position = Var.of(0);
		return new EntryViewModel() {
			@Override public Var<Boolean> isSelected() { return selected; }
			@Override public Var<Integer> position() { return position; }
		};
	}

	@Override
	protected <M> void _addViewableProps( Vals<M> models, String attr, ViewSupplier<M> viewSupplier, P thisComponent)
	{
		BiFunction<Integer, Vals<M>, M> modelFetcher = (i, vals) -> {
			M v = vals.at(i).get();
			if ( v instanceof EntryViewModel ) ((EntryViewModel) v).position().set(i);
			return v;
		};
		BiFunction<Integer, Vals<M>, M> entryFetcher = (i, vals) -> {
			M v = modelFetcher.apply(i, vals);
			return ( v != null ? (M) v : (M)_entryModel() );
		};

		Consumer<Vals<M>> addAll = vals -> {
			boolean allAreEntries = vals.stream().allMatch( v -> v instanceof EntryViewModel );
			if ( allAreEntries ) {
				List<EntryViewModel> entries = (List) vals.toList();
				thisComponent.addAllEntries(attr, entries, (ViewSupplier<EntryViewModel>) viewSupplier);
			}
			else
				for ( int i = 0; i< vals.size(); i++ ) {
					int finalI = i;
					thisComponent.addEntry(
							_entryModel(),
							m -> viewSupplier.createViewFor(entryFetcher.apply(finalI,vals))
						);
				}
		};

		_onShow( models, delegate -> {
			Vals<M> vals = delegate.vals();
			int delegateIndex = delegate.index();
			Change changeType = delegate.changeType();
			// we simply redo all the components.
			switch ( changeType ) {
				case SET:
				case ADD:
				case REMOVE:
					if ( delegateIndex >= 0 ) {
						if ( changeType == Change.ADD ) {
							M m = entryFetcher.apply(delegateIndex, vals);
							if ( m instanceof EntryViewModel )
								thisComponent.addEntryAt(delegateIndex, null, (EntryViewModel)m, (ViewSupplier<EntryViewModel>) viewSupplier);
							else
								thisComponent.addEntryAt(delegateIndex, null, _entryModel(), em -> viewSupplier.createViewFor(m));
						} else if ( changeType == Change.REMOVE )
							thisComponent.removeEntryAt( delegateIndex );
						else if ( changeType == Change.SET ) {
							M m = entryFetcher.apply(delegateIndex, vals);
							if ( m instanceof EntryViewModel )
								thisComponent.setEntryAt(delegateIndex, null, (EntryViewModel)m, (ViewSupplier<EntryViewModel>) viewSupplier);
							else
								thisComponent.setEntryAt(delegateIndex, null, _entryModel(), em -> viewSupplier.createViewFor(m));
						}
						// Now we need to update the positions of all the entries
						for ( int i = delegateIndex; i < vals.size(); i++ ) {
							M m = entryFetcher.apply(i, vals);
							if ( m instanceof EntryViewModel )
								((EntryViewModel)m).position().set(i);
						}
					} else {
						thisComponent.removeAllEntries();
						addAll.accept(vals);
					}
				break;
				case CLEAR: thisComponent.removeAllEntries(); break;
				case NONE: break;
				default: throw new IllegalStateException("Unknown type: "+delegate.changeType());
			}
		});
		addAll.accept(models);
	}
}
