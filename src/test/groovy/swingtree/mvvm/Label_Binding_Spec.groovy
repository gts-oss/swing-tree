package swingtree.mvvm

import swingtree.threading.EventProcessor
import swingtree.UI
import sprouts.Val
import sprouts.Var
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Title

import javax.swing.Icon
import javax.swing.SwingConstants
import java.awt.Color
import java.awt.Dimension

@Title("Label Binding")
@Narrative('''
    
    As a developer, I want to bind a label to a property so 
    that the label is updated when the property changes.
    Binding is a powerful feature that allows us to create
    UIs which are not only decoupled from the business logic of
    an application, but also make it easy to create UIs which
    are dynamic and reactive.
    
''')
class Label_Binding_Spec extends Specification
{
    def setupSpec() {
        UI.SETTINGS().setEventProcessor(EventProcessor.COUPLED)
        // This is so that the test thread is also allowed to perform UI operations
    }

    def 'We can bind to the text of a label.'()
    {
        reportInfo """
            Note that for a binding to work, the property must be a `Var` or `Val`
            implementation. All you have to do to then change the state of the UI component
            is changing the state of the property by calling its "set" method.
            Internally it will then call the "show()" method for you 
            which triggers the observers registered by the UI.
        """

        given : 'We create a simple swing-tree property for modelling the text.'
            Val<String> text = Var.of("Hello World")

        when : 'We create and bind to a label UI node...'
            var ui = UI.label("").withText(text)

        then : 'The label should be updated when the property changes.'
            ui.component.text == "Hello World"

        when : 'We change the property value...'
            text.set("Goodbye World")
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.text == "Goodbye World"
    }

    def 'We can bind to the foreground and background color of a UI node.'()
    {
        given : 'We create 2 simple swing-tree properties for modelling colors.'
            Val<Color> color1 = Var.of(Color.RED)
            Val<Color> color2 = Var.of(Color.BLUE)

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .withForeground(color1)
                    .withBackground(color2)

        then : 'The label should have the property colors.'
            ui.component.foreground == Color.RED
            ui.component.background == Color.BLUE

        when : 'We change the color values of both properties...'
            color1.set(Color.GREEN)
            color2.set(Color.YELLOW)
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label colors are updated.'
            ui.component.foreground == Color.GREEN
            ui.component.background == Color.YELLOW
    }

    def 'It is possible to bind to the minimum, maximum and preferred size of a label'()
    {
        reportInfo """
            Note that this works for all kind of UI nodes, not just labels.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<Dimension> minSize = Var.of(new Dimension(100, 100))
            Val<Dimension> maxSize = Var.of(new Dimension(200, 200))
            Val<Dimension> prefSize = Var.of(new Dimension(150, 150))

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .withMinSize(minSize)
                    .withMaxSize(maxSize)
                    .withPrefSize(prefSize)

        then : 'The label should be updated when the property changes.'
            ui.component.minimumSize == new Dimension(100, 100)
            ui.component.maximumSize == new Dimension(200, 200)
            ui.component.preferredSize == new Dimension(150, 150)

        when : 'We change the items of the properties...'
            minSize.set(new Dimension(50, 50))
            maxSize.set(new Dimension(100, 100))
            prefSize.set(new Dimension(75, 75))
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.minimumSize == new Dimension(50, 50)
            ui.component.maximumSize == new Dimension(100, 100)
            ui.component.preferredSize == new Dimension(75, 75)
    }

    def 'You can bind a variable to the "enable" flag of a label.'()
    {
        reportInfo """
            Note that this works for all kind of UI nodes, not just labels.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<Boolean> enabled = Var.of(true)

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .isEnabledIf(enabled)

        then : 'The label should be updated when the property changes.'
            ui.component.enabled == true

        when : 'We change the items of the properties...'
            enabled.set(false)
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.enabled == false
    }

    def 'You can bind a variable to the "visible" flag of a label.'()
    {
        reportInfo """
            Note that this works for all kind of UI nodes, not just labels.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<Boolean> visible = Var.of(true)

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .isVisibleIf(visible)

        then : 'The label should be updated when the property changes.'
            ui.component.visible == true

        when : 'We change the items of the properties...'
            visible.set(false)
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.visible == false
    }

    def 'A property can define the horizontal and vertical alignment of a label.'()
    {
        reportInfo """
            Not only should a view model contain state relevant for modelling the
            text displayed on a label,
            but it should also model state defining how the UI component should
            behave and look like depending on your business logic.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<UI.HorizontalAlignment> horizontal = Var.of(UI.HorizontalAlignment.LEFT)
            Val<UI.VerticalAlignment> vertical = Var.of(UI.VerticalAlignment.TOP)

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .withHorizontalAlignment(horizontal)
                    .withVerticalAlignment(vertical)

        then : 'The label should be updated when the property changes.'
            ui.component.horizontalAlignment == SwingConstants.LEFT
            ui.component.verticalAlignment == SwingConstants.TOP

        when : 'We change the items of the properties...'
            horizontal.set(UI.HorizontalAlignment.CENTER)
            vertical.set(UI.VerticalAlignment.BOTTOM)
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.horizontalAlignment == SwingConstants.CENTER
            ui.component.verticalAlignment == SwingConstants.BOTTOM
    }

    def 'A property can define the image relative horizontal and vertical alignment of a label.'()
    {
        reportInfo """
            Not only should a view model contain state relevant for modelling the
            text displayed on a label,
            but it should also model state defining how the UI component should
            behave and look like depending on your business logic.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<UI.HorizontalAlignment> horizontal = Var.of(UI.HorizontalAlignment.LEFT)
            Val<UI.VerticalAlignment> vertical = Var.of(UI.VerticalAlignment.TOP)

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .withImageRelativeHorizontalAlignment(horizontal)
                    .withImageRelativeVerticalAlignment(vertical)

        then : 'The label should be updated when the property changes.'
            ui.component.horizontalTextPosition == SwingConstants.LEFT
            ui.component.verticalTextPosition == SwingConstants.TOP

        when : 'We change the items of the properties...'
            horizontal.set(UI.HorizontalAlignment.CENTER)
            vertical.set(UI.VerticalAlignment.BOTTOM)
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.horizontalTextPosition == SwingConstants.CENTER
            ui.component.verticalTextPosition == SwingConstants.BOTTOM
    }

    def 'You can dynamically model the font size of your labels using an integer based property.'()
    {
        reportInfo """
            Not only should a view model contain state relevant for modelling the
            text displayed on a label,
            but it should also model state defining how the UI component should
            behave and look like depending on your business logic.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<Integer> fontSize = Var.of(12)

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .withFontSize(fontSize)

        then : 'The label should be updated when the property changes.'
            ui.component.font.size == 12

        when : 'We change the items of the properties...'
            fontSize.set(24)
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be updated.'
            ui.component.font.size == 24
    }

    def 'We can store icons inside properties and then bind them to labels.'()
    {
        reportInfo """
            Not only should a view model contain state relevant for modelling the
            text displayed on a label,
            but it should also model state defining how the UI component should
            behave and look like depending on your business logic.
        """
        given : 'We create a simple swing-tree property for modelling the size.'
            Val<Icon> icon = Var.of(UI.icon("img/seed.png"))
            var originalIcon = icon.orElseThrow()

        when : 'We create and bind to a label UI node...'
            var ui =
                    UI.label("")
                    .withIcon(icon)

        then : 'The label should be updated when the property changes.'
            ui.component.icon != null
            ui.component.icon === originalIcon
            ui.component.icon.iconHeight == 512
            ui.component.icon.iconWidth == 512

        when : 'We change the items of the properties...'
            icon.set(UI.icon("img/swing.png"))
        and : 'Then we wait for the EDT to complete the UI modifications...'
            UI.sync()
        then : 'The label should be a different one.'
            ui.component.icon != null
            ui.component.icon !== originalIcon
            ui.component.icon.iconHeight == 512
            ui.component.icon.iconWidth == 512
    }

}
