package swingtree;

import javax.swing.*;
import java.awt.*;

public class UIForJDialog<D extends JDialog> extends UIForAnyWindow<UIForJDialog<D>, D>
{
	/**
	 * Instances of the {@link AbstractBuilder} as well as its subtypes always wrap
	 * a single component for which they are responsible.
	 *
	 * @param component The component type which will be wrapped by this builder node.
	 */
	public UIForJDialog(D component) { super(component); }

	@Override
	protected void _add(Component component, Object conf) {
		getComponent().add(conf == null ? null : conf.toString(), component);
	}

	@Override
	public void show() {
		JDialog dialog = getComponent();
		Component[] components = dialog.getComponents();
		dialog.setLocationRelativeTo(null); // Initial centering!v
		dialog.pack(); // Otherwise some components resize strangely or are not shown at all...
		// First let's check if the dialog has an owner:
		Window owner = dialog.getOwner();
		// If there is no owner, we make sure that the window is centered on the screen again but with the component:
		if ( owner == null )
			dialog.setLocationRelativeTo(null);
		else // Otherwise we center the dialog on the owner:
			dialog.setLocationRelativeTo(owner);

		// We set the size to fit the component:
		if ( components.length > 0 ) {
			Dimension size = dialog.getSize();
			if ( size == null ) // The dialog has no size! It is best to set the size to the preferred size of the component:
				size = components[0].getPreferredSize();

			if ( size == null ) // The component has no preferred size! It is best to set the size to the minimum size of the component:
				size = components[0].getMinimumSize();

			if ( size == null ) // The component has no minimum size! Let's just look up the size of the component:
				size = components[0].getSize();

			dialog.setSize(size);
		}

		dialog.setVisible(true);
	}

	@Override
	protected JRootPane _getRootPane() {
		return getComponent().getRootPane();
	}

	@Override
	protected void _setTitle(String title) {
		getComponent().setTitle(title);
	}
}