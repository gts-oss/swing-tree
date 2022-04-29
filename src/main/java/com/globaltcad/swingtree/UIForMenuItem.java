package com.globaltcad.swingtree;


import javax.swing.*;

/**
 *  A swing tree builder for {@link JMenuItem} instances.
 */
public class UIForMenuItem extends UIForAbstractButton<UIForMenuItem, JMenuItem>
{
    protected UIForMenuItem(JMenuItem component) { super(component); }

    public UIForMenuItem withKeyStroke(KeyStroke keyStroke) {
        this.component.setAccelerator(keyStroke);
        return this;
    }
}
