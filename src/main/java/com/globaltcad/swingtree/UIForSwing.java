package com.globaltcad.swingtree;

import javax.swing.*;

public class UIForSwing<C extends JComponent> extends UIForAbstractSwing<UIForSwing<C>, C>
{
    /**
     * {@link UIForAbstractSwing} types always wrap
     * a single component for which they are responsible.
     *
     * @param component The {@link JComponent} type which will be wrapped by this builder node.
     */
    public UIForSwing(C component) {
        super(component);
    }
}
