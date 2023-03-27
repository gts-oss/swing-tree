package swingtree;

import javax.swing.*;

/**
 *  A swing tree builder node for {@link UIForTextPane} instances.
 */
public class UIForTextPane<P extends JTextPane> extends UIForAnyEditorPane<UIForTextPane<P>, P>
{
    /**
     * {@link UIForAnySwing} (sub)types always wrap
     * a single component for which they are responsible.
     *
     * @param component The {@link JComponent} type which will be wrapped by this builder node.
     */
    public UIForTextPane(P component) { super(component); }
}
