package com.globaltcad.swingtree;

import javax.swing.*;

/**
 *  A swing tree builder for {@link UIForTextPane} instances.
 */
public class UIForTextPane extends UIForAbstractEditorPane<UIForTextPane, JTextPane>
{
    /**
     * {@link UIForAbstractSwing} types always wrap
     * a single component for which they are responsible.
     *
     * @param component The {@link JComponent} type which will be wrapped by this builder node.
     */
    public UIForTextPane(JTextPane component) {
        super(component);
    }
}
