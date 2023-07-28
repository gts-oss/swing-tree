package swingtree.components;

import swingtree.style.ComponentExtension;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.*;
import javax.swing.plaf.PanelUI;
import java.awt.*;

/**
 * <code>JBox</code> is a generic lightweight container similar to
 * <code>javax.swing.JPanel</code>, but with 2 important differences:
 * <ul>
 *     <li>
 *         The <code>JBox</code> is transparent by default, meaning that it does
 *         not paint its background if it is not explicitly set through the style API.
 *     </li>
 *     <li> It does not have any insets by default. </li>
 * </ul>
 *
 * @author Daniel Nepp
 */
public class JBox extends JComponent implements Accessible
{
    /**
     * @see #getUIClassID
     */
    private static final String uiClassID = "PanelUI";

    /**
     * Creates a new JBox with the specified layout manager and buffering
     * strategy.
     *
     * @param layout  the LayoutManager to use
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free
     *        updates
     */
    public JBox(LayoutManager layout, boolean isDoubleBuffered) {
        setLayout(layout);
        setDoubleBuffered(isDoubleBuffered);
        setOpaque(false);
        updateUI();
    }

    /**
     * Create a new buffered JBox with the specified layout manager
     *
     * @param layout  the LayoutManager to use
     */
    public JBox(LayoutManager layout) {
        this(layout, true);
    }

    /**
     * Creates a new <code>JBox</code> with <code>FlowLayout</code>
     * and the specified buffering strategy.
     * If <code>isDoubleBuffered</code> is true, the <code>JBox</code>
     * will use a double buffer.
     *
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free
     *        updates
     */
    public JBox(boolean isDoubleBuffered) {
        this(new FlowLayout(), isDoubleBuffered);
    }

    /**
     * Creates a new <code>JBox</code> with a double buffer
     * and a flow layout.
     */
    public JBox() {
        this(true);
    }

    /**
     * Resets the UI property with a value from the current look and feel.
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI(ComponentExtension.from(this).createJBoxUI());
    }

    /**
     * Returns the look and feel (L&amp;amp;F) object that renders this component.
     *
     * @return the PanelUI object that renders this component
     */
    public PanelUI getUI() { return (PanelUI) ui; }


    /**
     * Sets the look and feel (L&amp;F) object that renders this component.
     *
     * @param ui  the PanelUI L&amp;F object
     * @see UIDefaults#getUI
     * @beaninfo
     *        bound: true
     *       hidden: true
     *    attribute: visualUpdate true
     *  description: The UI object that implements the Component's LookAndFeel.
     */
    public void setUI(PanelUI ui) {
        super.setUI(ui);
    }

    /**
     * Returns a string that specifies the name of the L&amp;F class
     * that renders this component.
     *
     * @return "PanelUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     * @beaninfo
     *        expert: true
     *   description: A string that specifies the name of the L&amp;F class.
     */
    @Override public String getUIClassID() { return uiClassID; }

    /**
     * Returns a string representation of this JBox. This method
     * is intended to be used only for debugging purposes, and the
     * content and format of the returned string may vary between
     * implementations. The returned string may be empty but may not
     * be <code>null</code>.
     *
     * @return  a string representation of this JBox.
     */
    protected String paramString() { return super.paramString(); }

/////////////////
// Accessibility support
////////////////

    /**
     * Gets the AccessibleContext associated with this JBox.
     * For the JBox, the AccessibleContext takes the form of an
     * AccessibleJBox.
     * A new AccessibleJBox instance is created if necessary.
     *
     * @return an AccessibleJBox that serves as the
     *         AccessibleContext of this JBox
     */
    public AccessibleContext getAccessibleContext() {
        if ( accessibleContext == null )
            accessibleContext = new AccessibleJBox();

        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the
     * <code>JBox</code> class.  It provides an implementation of the
     * Java Accessibility API appropriate to panel user-interface
     * elements.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    protected class AccessibleJBox extends JComponent.AccessibleJComponent {

        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the
         * object
         */
        public AccessibleRole getAccessibleRole() { return AccessibleRole.PANEL; }
    }
}
