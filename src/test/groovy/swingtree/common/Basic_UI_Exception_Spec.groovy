package swingtree.common

import swingtree.SwingTree
import swingtree.api.IconDeclaration
import swingtree.components.JSplitButton
import swingtree.threading.EventProcessor
import swingtree.UI
import swingtree.api.MenuBuilder
import swingtree.api.SwingBuilder
import sprouts.Val
import sprouts.Var
import swingtree.layout.LayoutConstraint
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import javax.swing.*
import javax.swing.text.JTextComponent
import java.awt.Color
import java.awt.Component

@Title("How Not To Use")
@Narrative('''

    This specification shows you how NOT to use the Swing-Tree API. 

''')
class Basic_UI_Exception_Spec extends Specification
{
    def setupSpec() {
        SwingTree.get().setEventProcessor(EventProcessor.COUPLED_STRICT)
        // In this specification we are using the strict event processor
        // which will throw exceptions if we try to perform UI operations in the test thread.
    }

    def 'The given factory methods do not accept null arguments.'(
            Runnable illegalAction
    ) {
        when : 'We execute the error prone code...'
            illegalAction()
        then : 'An illegal argument exception will be thrown!'
            thrown(IllegalArgumentException)
        where :
            illegalAction << [
                    {UI.of((JMenu)null)},
                    {UI.of((JSeparator)null)},
                    {UI.of((JMenuItem)null)},
                    {UI.of((JPanel)null)},
                    {UI.label((String)null)},
                    {UI.label((Val<String>)null)},
                    {UI.label(300,200,null)},
                    {UI.label((Icon)null)},
                    {UI.label((IconDeclaration)null)},
                    {UI.html((String)null)},
                    {UI.html((Val<String>)null)},
                    {UI.labelWithIcon((Val<Icon>)null)},
                    {UI.button((Icon)null,(Icon)null,(Icon)null)},
                    {UI.button((IconDeclaration)null,(IconDeclaration)null,(IconDeclaration)null)},
                    {UI.button().onClick(null)},
                    {UI.button().onChange(null)},
                    {UI.button((Icon)null)},
                    {UI.button((IconDeclaration)null)},
                    {UI.button((Val<String>)null)},
                    {UI.buttonWithIcon((Val<Icon>)null)},
                    {UI.button((Icon)null,(Icon)null)},
                    {UI.button((IconDeclaration)null,(IconDeclaration)null)},
                    {UI.button(0,0, (ImageIcon)null, (ImageIcon)null)},
                    {UI.button("").isSelectedIf((Var)null)},
                    {UI.toggleButton((String)null)},
                    {UI.toggleButton((Val<String>)null)},
                    {UI.toggleButton((Var<Boolean>)null)},
                    {UI.toggleButton((String)null, (Var<Boolean>)null)},
                    {UI.toggleButton((Val<String>)null, (Var<Boolean>)null)},
                    {UI.toggleButton((Icon)null)},
                    {UI.toggleButton((IconDeclaration)null)},
                    {UI.toggleButtonWithIcon((Val<Icon>)null)},
                    {UI.toggleButtonWithIcon((Val<Icon>)null, (Var<Boolean>)null)},
                    {UI.of((SwingBuilder)null)},
                    {UI.of((MenuBuilder)null)},
                    {UI.of((JCheckBox)null)},
                    {UI.of((JRadioButton)null)},
                    {UI.of((JTextComponent)null)},
                    {UI.of((JComponent)null)},
                    {UI.of((JSplitButton)null)},
                    {UI.of((JPopupMenu)null)},
                    {UI.of((JSeparator)null)},
                    {UI.separator((UI.Align)null)},
                    {UI.of((JTextArea)null)},
                    {UI.of((JLabel)null)},
                    {UI.of((Component)null)},
                    {UI.of((JTabbedPane)null)},
                    {UI.splitItem(null)},
                    {UI.splitButton(null)},
                    {UI.checkBox(null)},
                    {UI.radioButton(null)},
                    {UI.radioButton(null, (Var)null)},
                    {UI.menuItem(null)},
                    {UI.splitItem(null)},
                    {UI.splitRadioItem(null)},
                    {UI.textField((Val)null)},
                    {UI.textField((Var)null)},
                    {UI.textField((String)null)},
                    {UI.textArea((Var)null)},
                    {UI.textArea((Val)null)},
                    {UI.textArea((String)null)},
                    {UI.panel().onMouseClick(null)},
                    {UI.panel().onMouseRelease(null)},
                    {UI.panel().onMousePress(null)},
                    {UI.panel().onMouseDrag(null)},
                    {UI.panel().onMouseEnter(null)},
                    {UI.panel().onMouseExit(null)},
                    {UI.panel().onMouseWheelMove(null)},
                    {UI.panel().onMouseWheelUp(null)},
                    {UI.panel().onMouseWheelDown(null)},
                    {UI.of(new JComboBox<>()).onMouseClick(null)},
                    {UI.of(new JComboBox<>()).onSelection(null)},
                    {UI.of(new JSlider()).onChange(null)},
                    {UI.tabbedPane((UI.Side)null)},
                    {UI.tabbedPane((UI.OverflowPolicy)null)},
                    {UI.tabbedPane((UI.Side)null,(UI.OverflowPolicy)null)},
                    {UI.slider((UI.Align)null)},
                    {UI.slider((UI.Align)null, 1, 10, 3)},
                    {UI.slider(UI.Align.HORIZONTAL).onChange(null)},
                    {UI.comboBox((List)null)},
                    {UI.comboBox((Object[])null)},
                    {UI.comboBox().onSelection(null)},
                    {UI.panel((String)null)},
                    {UI.panel((String)null, (String)null)},
                    {UI.panel((String)null, (String)null, (String)null)},
                    {UI.panel((LayoutConstraint)null)},
                    {UI.panel((Val<LayoutConstraint>)null)},
                    {UI.spinner((Var<?>)null)},
                    {UI.spinner((SpinnerModel)null)},
                    {UI.toolBar((UI.Align)null)},
                    {UI.toolBar((Val)null)},
                    {UI.toolBar().isVisibleIf((Val)null)},
                    {UI.toolBar().isVisibleIf((Var)null)},
            ]
    }

    def 'Certain Swing-Tree UI builder, do not allow you to use nullable properties.'(
            Runnable illegalAction
    ) {
        when : 'We execute the error prone code...'
            illegalAction()
        then : 'An illegal argument exception will be thrown!'
            thrown(IllegalArgumentException)
        where :
            illegalAction << [
                    { UI.toggleButton(Var.ofNullable(Boolean, false)) },
                    { UI.toggleButton("Toggle Me!", Var.ofNullable(Boolean, true)) },
                    { UI.toggleButton(Var.ofNullable(String, null), Var.of(true)) },
                    { UI.toggleButtonWithIcon(Var.ofNullable(Icon, null), Var.of(true)) },
                    { UI.toggleButtonWithIcon(null, null) },
                    { UI.textArea(Var.ofNullable(String, "Ooops")) },
                    { UI.textArea(Val.ofNullable(String, null)) },
                    { UI.textField(UI.HorizontalAlignment.RIGHT, Val.ofNullable(String, "")) },
                    { UI.textField(UI.HorizontalAlignment.LEFT, Var.ofNullable(String, "")) },
                    { UI.panel(Val.ofNullable(LayoutConstraint,UI.FILL)) },
                    { UI.spinner(Var.ofNullable(Byte,null)) },
                    { UI.radioButton(Val.ofNullable(String,"")) },
                    { UI.radioButton("Hi!",Var.ofNullable(Boolean,true)) },
                    { UI.radioButton(Val.ofNullable(String,""),Var.ofNullable(Boolean,true)) },
                    { UI.radioButton((Enum)null, Var.ofNullable(Enum.class, (Enum)null)) },
                    { UI.radioButton("Hi").isSelectedIf((Enum)null, Var.ofNullable(Enum.class, (Enum)null)) },
                    { UI.checkBox(Val.ofNullable(String,"")) },
                    { UI.checkBox("Done!", Var.ofNullable(Boolean,true)) },
                    { UI.checkBox(Val.ofNullable(String,""), Var.ofNullable(Boolean,true)) },
                    { UI.panel().isEnabledIf(Val.ofNullable(Boolean, false)) },
                    { UI.button().isSelectedIf(Val.ofNullable(Boolean, true)) },
                    { UI.button().isSelectedIfNot(Val.ofNullable(Boolean, true)) },
                    { UI.button().isSelectedIf(Var.ofNullable(Boolean, true)) },
                    { UI.button().isSelectedIfNot(Var.ofNullable(Boolean, true)) },
                    { UI.popupMenu().isVisibleIf(Val.ofNullable(Boolean,true)) },
                    { UI.popupMenu().isVisibleIf(Var.ofNullable(Boolean,true)) },
                    { UI.button(Val.ofNullable(String,"Click Me!")) },
                    { UI.label(Val.ofNullable(String, "")) },
                    { UI.spinner().withValue(Val.ofNullable(Number, 4)) },
                    { UI.spinner().withValue(Var.ofNullable(Number, 4)) },
                    { UI.panel().withForeground(Val.ofNullable(Color,null)) },
                    { UI.passwordField(Val.ofNullable(String, "")) },
                    { UI.passwordField(Var.ofNullable(String, "")) }
                ]
    }

}
