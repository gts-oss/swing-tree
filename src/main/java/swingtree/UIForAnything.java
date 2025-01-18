package swingtree;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sprouts.Action;
import sprouts.*;
import swingtree.api.Peeker;
import swingtree.layout.AddConstraint;
import swingtree.style.ComponentExtension;
import swingtree.style.StyleConf;
import swingtree.threading.EventProcessor;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *  This is the root builder type for all other SwingTree builder subtypes.
 *  It is a generic builder which may wrap any type of Swing component to allow for method chaining
 *  and nesting based building in SwingTree. <br>
 *  Note that a builder is immutable, which means that every method call on a builder
 *  returns a new builder instance with the new state. <br>
 *  The state of the previous builder is disposed of, which means that the wrapped component
 *  is no longer referenced by the previous builder instance and can be garbage collected.<br>
 *  <b>Spent builder nodes may not be reused!</b>
 *
 * @param <I> The type parameter representing the concrete subtype of this abstract class, "I" stands for "Implementation".
 * @param <C> The type parameter representing the concrete component type which this builder is responsible for building.
 * @param <E> The component type parameter which ought to be built in some way.
 */
public abstract class UIForAnything<I, C extends E, E extends Component>
{
    private static final Logger log = LoggerFactory.getLogger(UIForAnything.class);

    private interface Ref<T> {
        static <T> Ref<T> of(Reference<T> ref) {
            return ref::get;
        }
        static <T> Ref<T> of(T value) {
            return ()->value;
        }
        @Nullable T get();
    }

    /**
     *  The type class of the component managed by this builder.
     *  See documentation for method "build" for more information.
     * @return The type class of the component managed by this builder.
     */
    public final Class<C> getType() {
        return _state().componentType();
    }

    /**
     *  The component managed by this builder.
     *
     *  @throws IllegalStateException if this method is called from a thread other than the EDT
     *                                and this UI is configured to be decoupled from the application thread.
     *                                See {@link UI#use(EventProcessor, Supplier)}.
     *  @return The component managed by this builder.
     * @deprecated Use {@link #get(Class)} instead.
     */
    @Deprecated
    public final C getComponent() {
        boolean isCoupled       = _state().eventProcessor() == EventProcessor.COUPLED;
        boolean isCoupledStrict = _state().eventProcessor() == EventProcessor.COUPLED_STRICT;

        if ( !isCoupled && !isCoupledStrict && !UI.thisIsUIThread() )
            throw new IllegalStateException(
                    "This UI is configured to be decoupled from the application thread, " +
                    "which means that it can only be modified from the EDT. " +
                    "Please use 'UI.run(()->...)' method to execute your modifications on the EDT."
                );
        return _state().component();
    }

    /**
     *  The optional component managed by this builder.
     *
     * @return An {@link OptionalUI} wrapping a component or null.
     *         This optional will throw an exception if the
     *         application has an application thread (see {@link UI#use(EventProcessor, Supplier)})
     *         and this method is called from a thread other than the EDT.
     * @deprecated Use {@link #get(Class)} instead.
     */
    @Deprecated
    public final OptionalUI<C> component() {
        return OptionalUI.ofNullable(_state().component());
    }

    /**
     *  Use this if you wish to access the component wrapped by this builder directly.
     *  This is useful for more fine-grained control, like for example calling
     *  methods like "setName", "setTitle", and so on... <br>
     *  This method accepts a lambda to which the component wrapped by this builder will be supplied.
     *  The lambda can then call said methods or perform other tasks which
     *  might be relevant to the component while also not
     *  breaking the benefits of nesting and method chaining provided by this class...
     *  <br>
     *  The below example shows how this method allows for more fine-grained control over the wrapped component:
     *  <pre>{@code
     *      UI.panel()
     *      peek( panel -> panel.setDebugGraphicsOptions(true) );
     *  }</pre>
     *  <br><br>
     *
     * @param action A Consumer lambda which simply returned the wrapped JComponent type for interacting it.
     * @return This very instance, which enables builder-style method chaining.
     */
    public final I peek( Peeker<C> action ) {
        return _with( thisComponent -> {
                    try {
                        action.accept(thisComponent);
                    } catch ( Exception e ) {
                        log.error("Error trying to run 'Peeker' '"+action+"' on component '"+thisComponent+"'.", e);
                    }
                })
                ._this();
    }

    /**
     *  Use this to only build a certain part of the UI if the provided boolean condition is true.
     *  Which is to say, if the condition is false, then the second lambda is ignored, if
     *  on the other hand the condition is true, then the second lambda is executed
     *  with the current builder instance passed to it as a parameter.
     *  Inside the lambda, one can then continue building the UI while also not
     *  breaking the benefits of nesting and method chaining provided by this builder...
     *  <p>
     *  This is in essence a more advanced version of {@link #apply(Consumer)}.
     *  <br>
     *  Here a simple usage example:
     *  <pre>{@code
     *    UI.panel()
     *    .applyIf( userIsLoggedIn, ui -> ui
     *      .add( UI.label("Welcome back!") )
     *      .add( UI.button("Logout")).onClick( () -> logout() )
     *      .add( UI.button("Settings")).onClick( () -> showSettings() )
     *    )
     *    .applyIf( !userIsLoggedIn, ui -> ui
     *      .add( UI.label("Please login to continue.") )
     *      .add( UI.button("Login")).onClick( () -> login() );
     *    );
     *  }</pre>
     *  Here we use theis method to build a panel
     *  with different content depending on whether the user is logged in or not.
     *  <br><br>
     *
     * @param condition The truth value which determines if the second consumer lambda is executed or not.
     * @param building A {@link Consumer} lambda which simply consumes this builder instance.
     * @return This very instance, which enables builder-style method chaining.
     */
    public final I applyIf( boolean condition, Consumer<I> building ) {
        NullUtil.nullArgCheck(building, "building", Consumer.class);

        if ( !condition )
            return _this();

        return _with( thisComponent -> {
                    BuilderState<C> proceduralBuilder =
                        new BuilderState<>(
                                _state().eventProcessor(),
                                BuilderState.Mode.PROCEDURAL_OR_DECLARATIVE,
                                _state().componentType(),
                                ()->thisComponent
                            );
                    building.accept(_newBuilderWithState(proceduralBuilder)._this());
                })
                ._this();
    }


    /**
     *  Allows you to build declarative UI conditionally,
     *  meaning that the UI is only built if the provided {@link Optional} value is present.
     *  If the value is not present, meaning it is null, then the second lambda
     *  (containing UI declarations relevant to the value) is simply ignored.
     *  <p>
     *  Consider the following example:
     *  <pre>{@code
     * // In your view model:
     * public Optional<MySubModel> getM() {
     *   return Optional.ofNullable(this.model);
     * }
     *
     * // In your view:
     * UI.panel()
     * .add(UI.label("Maybe Sub Model:"))
     * .applyIfPresent(vm.getM().map(m->ui->ui
     *   .add(UI.label("Hello Sub Model!"))
     *   .add(UI.label("A:")
     *   .add(UI.textField(m.getA()))
     *   .add(UI.label("B:"))
     *   .add(UI.textField(m.getB()))
     *   // ...
     * ))
     * .add(UI.label("Some other stuff..."));
     * }</pre>
     *
     * The {@code applyIfPresent} method takes an {@code Optional<Consumer<I>>} as parameter,
     * where {@code I} is the type of the UI builder.
     * This allows you to map the optional value to a consumer which is only executed if the value is present.
     * If the optional value is present, the consumer is executed with the
     * current UI builder as a parameter, which allows you to continue building the UI as usual.
     * <br>
     * The {@code m->ui->ui} may look a bit confusing at first, but it is simply a lambda expression
     * which takes the optional value and returns a consumer ({@code ui->ui... }) which takes the UI builder
     * as a parameter.
     * <br>
     * This is in essence a more advanced {@code Optional} centric version of {@link #applyIf(boolean, Consumer)}
     * and {@link #apply(Consumer)}.
     * <br>
     *
     * @param building An optional consumer lambda which simply consumes this builder node.
     * @return This very instance, which enables builder-style method chaining.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final I applyIfPresent( Optional<Consumer<I>> building ) {
        NullUtil.nullArgCheck(building, "building", Optional.class);
        return _with( thisComponent -> {
                    BuilderState<C> proceduralBuilder =
                        new BuilderState<>(
                                _state().eventProcessor(),
                                BuilderState.Mode.PROCEDURAL_OR_DECLARATIVE,
                                _state().componentType(),
                                ()->thisComponent
                            );
                    building.ifPresent( buildingLambda -> {
                        buildingLambda.accept(_newBuilderWithState(proceduralBuilder)._this());
                    });
                })
                ._this();
    }

    /**
     *  Use this to continue building UI inside a provided lambda
     *  if you need to introduce some imperative code in between
     *  the building process. <br>
     *  This is especially useful for when you need to build UI based on loops.
     *  The current builder instance will simply be supplied to the provided {@link Consumer} lambda.
     *  Inside the supplied lambda, you can then continue building the UI while also not
     *  breaking the benefits of nesting and method chaining, effectively preserving
     *  the declarative nature of the builder.
     *  <br><br>
     *  Here is a simple example of how this method can be used to build a panel
     *  with a variable amount of images displayed in a grid:
     *  <pre>{@code
     *      UI.panel("wrap 3")
     *      .apply( ui -> {
     *          for ( String path : imagePaths )
     *              ui.add( UI.label(UI.icon(path)) );
     *      });
     *  }</pre>
     *  <br><br>
     *  Here is another example of how this method can be used to build a panel
     *  with a variable amount of buttons displayed in a grid:
     *  <pre>{@code
     *    UI.panel("wrap 4")
     *    .apply( ui -> {
     *      for ( int i = 0; i < numOfButtons; i++ )
     *          ui.add( UI.button("Button " + i)
     *          .onClick( () -> {...} );
     *    });
     *  }</pre>
     *  <br><br>
     *
     * @param building A Consumer lambda which simply consumes this builder instance.
     * @return This very instance, which enables builder-style method chaining.
     */
    public final I apply( Consumer<I> building ) {
        NullUtil.nullArgCheck(building, "building", Consumer.class);
        return applyIf(true, building);
    }

    /**
     *  This method completes the building process for the wrapped
     *  {@link javax.swing.JComponent} type by returning it.
     *  However, it also expects the user to pass the class of the {@link javax.swing.JComponent}
     *  wrapped by this builder! This is not out of necessity but for better
     *  readability when using the builder in more extensive ways where
     *  the beginning and end of the method chaining and nesting of the builder does
     *  not fit on one screen. <br>
     *  In such cases the expression "{@code .get(MyJComponent.class)}" helps
     *  to identify which type of {@link javax.swing.JComponent} is currently being built on a given
     *  nesting layer... <br><br>
     *  Here is a simple example that demonstrates this technique using
     *  a {@link javax.swing.JPanel} and a {@link javax.swing.JMenuBar}:
     *  <pre>{@code
     *      UI.panel()
     *      .add(
     *          UI.menuBar()
     *          .add( UI.menu("File") )
     *          .add( UI.menuItem("Open") )
     *          .add( UI.menuItem("Save") )
     *          // ...
     *          .add( UI.menuItem("Exit") )
     *          .get(JMenuBar.class)
     *      )
     *      .add( UI.button("Click me!") )
     *      .get(JPanel.class);
     *  }</pre>
     *  As you can see, the expression "{@code .get(JMenuBar.class)}" as well as the expression
     *  "{@code .get(JPanel.class)}" at the end of the builder chain help to identify
     *  which type of {@link javax.swing.JComponent} is currently being built and returned.
     *
     * @param type The type class of the component which this builder wraps.
     * @return The result of the building process, namely: a type of JComponent.
     */
    public final C get( Class<C> type ) {
        if ( type != _state().componentType() && !type.isAssignableFrom(_state().componentType()) )
            throw new IllegalArgumentException(
                    "The type of the component wrapped by this builder is '" + _state().componentType() + "', " +
                    "but the provided type is '" + type + "' which is not assignable from '" + _state().componentType() + "'."
                );

        if ( !UI.thisIsUIThread() ) {
            boolean isCoupled       = _state().eventProcessor() == EventProcessor.COUPLED;
            boolean isCoupledStrict = _state().eventProcessor() == EventProcessor.COUPLED_STRICT;

            if ( !isCoupled && !isCoupledStrict )
                throw new IllegalStateException(
                        "This UI is configured to be decoupled from the application thread, " +
                        "which means that it can only be modified from the EDT. " +
                        "Please use 'UI.run(()->...)' method to execute your modifications on the EDT."
                    );

            String currentThreadName = Optional.ofNullable(Thread.currentThread().getName()).orElse("");

            if ( !currentThreadName.trim().equalsIgnoreCase("test worker") )
                log.warn(
                    "This UI is being built on thread '" + currentThreadName + "', " +
                    "which is not the EDT (GUI thread). This may lead to unexpected behavior! " +
                    "Please make sure to build your UI on the EDT.\n" +
                    "Consider taking a look at 'UI.run(()->...)', 'UI.runAndGet(()->...)', 'UI.runLater(()->...)', " +
                    "among other methods to ensure that your UI is built on the EDT.\n" +
                    "Running 'UI.runAndGet(()->...)' for you now...",
                    new Throwable()
                );

            return UI.runAndGet(()->_state().component());
        }
        return _state().component();
    }

    /**
     *  This builder class expects its implementations to be builder types
     *  for anything which can be built in a nested tree-like structure.
     *  Implementations of this abstract method ought to enable support for nested building.
     *  <br><br>
     *
     * @param components An array of component instances which ought to be added to the wrapped component type.
     * @return This very instance, which enables builder-style method chaining.
     */
    @SafeVarargs
    public final I add( E... components ) {
        NullUtil.nullArgCheck(components, "components", Object[].class);
        return _with( thisComponent -> {
                    _addComponentsTo( thisComponent, components );
               })
               ._this();
    }

    /**
     *  Uses the supplied builder to build its component and then add it to the component
     *  that is being built by this builder instance. This directly allows you to nest your
     *  builder based UI declarations in an HTML-like fashion.
     *
     * @param builder A builder for another {@link JComponent} instance which ought to be added to the wrapped component type.
     * @param <T> The type of the {@link JComponent} which is wrapped by the provided builder.
     * @return This very instance, which enables builder-style method chaining.
     */
    public final <T extends JComponent> I add( UIForAnySwing<?, T> builder ) {
        return (I) this.add( new UIForAnything[]{builder} );
    }

    /**
     *  This method provides the same functionality as the other "add" methods.
     *  However, it bypasses the necessity to call the "get" method by
     *  calling it internally for you. <br>
     *  This helps to improve readability, especially when the degree of nesting is very low.
     *
     * @param builders An array of builder instances whose JComponents ought to be added to the one wrapped by this builder.
     * @param <B> The type of the builder instances which are used to configure the components that will be added to the component wrapped by this builder.
     * @return This very instance, which enables builder-style method chaining.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public final <B extends UIForAnything<?, ?, JComponent>> I add( B... builders )
    {
        if ( builders == null )
            throw new IllegalArgumentException("Swing tree builders may not be null!");

        return _with( thisComponent -> {
                    _addBuildersTo( thisComponent, builders );
                })
                ._this();
    }

    /**
     *  This builder class expects its implementations to be builder types
     *  for anything which can be built in a nested tree-like structure.
     *  Implementations of this abstract method ought to enable support for nested building.
     *  <br><br>
     *
     * @param components A list of component instances which ought to be added to the wrapped component type.
     * @return This very instance, which enables builder-style method chaining.
     */
    public final I add( List<E> components ) {
        return _with( thisComponent -> {
                    for ( E component : components )
                        _addBuilderTo(thisComponent, UI.of((JComponent) component), null);
                })
                ._this();
    }

    @SafeVarargs
    protected final <B extends UIForAnything<?, ?, JComponent>> void _addBuildersTo(
        C thisComponent, B... builders
    ) {
        for ( UIForAnything<?, ?, ?> b : builders )
            _addBuilderTo(thisComponent, b, null);
    }

    @SafeVarargs
    protected final void _addComponentsTo( C thisComponent, E... componentsToBeAdded ) {
        for ( E other : componentsToBeAdded )
            _addBuilderTo(thisComponent, UI.of((JComponent) other), null);
    }

    protected final void _addBuilderTo( C thisComponent, UIForAnything<?, ?, ?> builder, @Nullable AddConstraint conf )
    {
        NullUtil.nullArgCheck(builder, "builder", UIForAnything.class);

        boolean isCoupled       = _state().eventProcessor() == EventProcessor.COUPLED;
        boolean isCoupledStrict = _state().eventProcessor() == EventProcessor.COUPLED_STRICT;

        if ( !isCoupled && !isCoupledStrict && !UI.thisIsUIThread() )
            throw new RuntimeException(
                    "This UI is configured to be decoupled from the application thread, " +
                    "which means that it can only be modified from the EDT. " +
                    "Please use 'UI.run(()->...)' method to execute your modifications on the EDT."
                );

        E childComponent = (E) builder.getComponent();

        if ( childComponent.getParent() != null )
            log.warn(
                "Trying to add component '{}' to this container " +
                "despite it already being part of another container.\n" +
                "Adding it to this '{}' will implicitly remove it from its current container. " +
                "This side effect may not be intended!\n" +
                "If it is, please make this intention explicit by removing " +
                "the component from its current container first.",
                childComponent.getClass(), _state().componentType(),
                new Throwable()
            );

        if ( childComponent instanceof JComponent ) {
            JComponent child = (JComponent) childComponent;

            StyleConf styleConf = ( conf != null ? null : ComponentExtension.from(child).gatherStyle() );
            if ( styleConf != null )
                conf = styleConf.layoutConstraint().map(it-> (AddConstraint) () -> it).orElse(null);

            _addComponentTo(thisComponent, childComponent, conf);

            if ( styleConf != null )
                ComponentExtension.from(child).applyAndInstallStyle(styleConf, true);
            else
                ComponentExtension.from(child).gatherApplyAndInstallStyle(true);
        }
        else
            _addComponentTo(thisComponent, childComponent, conf);

        builder._disposeState(); // The builder may now no longer be used.
    }

    /**
     * This builder class expects its implementations to be builder types
     * for anything which can be built in a nested tree-like structure.
     * Implementations of this abstract method ought to enable support for nested building.
     * <br><br>
     *
     * @param thisComponent  The component which is wrapped by this builder.
     * @param addedComponent A component instance which ought to be added to the wrapped component type.
     * @param constraints    The layout constraint which ought to be used to add the component to the wrapped component type.
     */
    protected abstract void _addComponentTo( C thisComponent, E addedComponent, @Nullable AddConstraint constraints );

    /**
     *  Returns the state of the builder, which is a container for the wrapped component
     *  as well as it's type and current {@link EventProcessor}.
     * @return The state of the builder.
     */
    protected abstract BuilderState<C> _state();

    /**
     *  An internal wither method which creates a new builder instance with the provided
     *  {@link BuilderState} stored inside it.
     *
     * @param newState The new state which should be stored inside the new builder instance.
     * @return A new builder instance with the provided state stored inside it.
     */
    protected abstract UIForAnything<I,C, E> _newBuilderWithState( BuilderState<C> newState );

    /**
     *  Creates a new builder with the provided component mutation applied to the wrapped component. <br>
     *  Note that the SwingTree builders are immutable, which means that this method
     *  does not mutate the current builder instance, but instead creates a new builder instance
     *  with a new {@link BuilderState} which contains the provided component mutation (see {@link BuilderState#withMutator(Consumer)}).
     *  Also see {@link #_newBuilderWithState(BuilderState)}.
     *
     * @param componentMutator A consumer lambda which receives the wrapped component and
     *                         is then used to apply some builder action to it.
     * @return A new builder instance with the provided component mutation applied to the wrapped component.
     */
    protected final UIForAnything<I,C,E> _with( Consumer<C> componentMutator ) {
        BuilderState<C> newState = _state().withMutator(componentMutator);
        return _newBuilderWithState(newState);
    }

    /**
     *  A convenient shortcut to the {@link EventProcessor#registerUIEvent(Runnable)} method
     *  to the current {@link EventProcessor} attached to the current {@link BuilderState}.
     *  In practice, this method will ultimately just delegate tasks to the AWT Event Dispatch Thread (EDT).
     *
     * @param action An action which should be executed by the UI thread,
     *               which is determined by implementations of the {@link EventProcessor},
     *               also see {@link UI#use(EventProcessor, Supplier)}. <br>
     *               Usually the UI thread is AWT's Event Dispatch Thread (EDT).
     */
    protected final void _runInUI( Runnable action ) {
        _state().eventProcessor().registerUIEvent( action );
    }

    /**
     *  A convenient delegate to the {@link EventProcessor#registerAppEvent(Runnable)} method,
     *  which allows you to execute an action on the current application thread.
     *  To configure the current {@link EventProcessor} see {@link UI#use(EventProcessor, Supplier)}
     *  or the underlying {@link SwingTree#setEventProcessor(EventProcessor)} method.
     *
     * @param action An action which should be executed by the application thread,
     *               which is determined by implementations of the current {@link EventProcessor},
     *               also see {@link UI#use(EventProcessor, Supplier)}.
     */
    protected final void _runInApp( Runnable action ) {
        _state().eventProcessor().registerAppEvent(action);
    }

    /**
     *  A convenient delegate to the {@link EventProcessor#registerAppEvent(Runnable)} method,
     *  which allows you to execute an action on the current application thread.
     *  Which thread executes these tasks is determined by the current {@link EventProcessor}.
     *  Usually this is the {@link EventProcessor#COUPLED} or {@link EventProcessor#COUPLED_STRICT} event processor.
     *
     * @param value A value which should be captured and then passed to the provided action
     *              on the current application thread (see {@link EventProcessor} and {@link UI#use(EventProcessor, Supplier)}).
     * @param action A consumer lambda which is executed by the application thread
     *               and receives the provided value.
     * @param <T> The type of the value.
     */
    protected final <T> void _runInApp( T value, Consumer<T> action ) {
        _runInApp(()->action.accept(value));
    }

    /**
     *  Use this to register a state change listener for the provided property
     *  which will be executed by the UI thread (see {@link EventProcessor}).
     *
     * @param val A property whose state changes should be listened to on the UI thread.
     * @param thisComponent The component which is wrapped by this builder.
     * @param displayAction A consumer lambda receiving the provided value and
     *                      is then executed by the UI thread.
     * @param <T> The type of the item wrapped by the provided property.
     */
    protected final <T> void _onShow( Val<T> val, C thisComponent, BiConsumer<C, T> displayAction )
    {
        Objects.requireNonNull(val);
        Objects.requireNonNull(displayAction);
        _onShowDelegated( val, thisComponent, (component, delegate) -> displayAction.accept(component, delegate.currentValue().orElseNull()));
    }

    protected final <T> void _onShowDelegated( Val<T> val, C thisComponent, BiConsumer<C, ValDelegate<T>> displayAction )
    {
        Objects.requireNonNull(val);
        Objects.requireNonNull(displayAction);
        Ref<Val<T>> valRef;
        if ( val.isLens() || val.isView() )
            valRef = Ref.of(val);
            /*
                We don't want lenses or views to be garbage collected, so we keep a strong reference to them.
                The reason for this is that these types of properties
                are necessarily part of the application state,
                and they also are only weakly referenced by their
                parent properties (lenses and views observer their parent properties).
             */
        else
            valRef = Ref.of(new WeakReference<>(val));

        _onShowDelegated( valRef, new WeakReference<>(thisComponent), displayAction );
    }

    protected final <T> UIForAnything<I,C,E> _withOnShow( Val<T> val, BiConsumer<C, T> displayAction )
    {
        Objects.requireNonNull(val);
        Objects.requireNonNull(displayAction);
        return _with( thisComponent -> _onShow( val, thisComponent, displayAction) );
    }

    private <T> void _onShow(
        Ref<Val<T>>       propertyRef,
        WeakReference<C>  weakComponent,
        BiConsumer<C, T>  displayAction
    ) {
        Objects.requireNonNull(propertyRef);
        Objects.requireNonNull(weakComponent);
        Objects.requireNonNull(displayAction);
        _onShowDelegated(propertyRef, weakComponent, (component, delegate) -> displayAction.accept(component, delegate.currentValue().orElseNull()));
    }

    private <T> void _onShowDelegated(
        Ref<Val<T>>       propertyRef,
        WeakReference<C>  weakComponent,
        BiConsumer<C, ValDelegate<T>>  displayAction
    ) {
        Objects.requireNonNull(propertyRef);
        Objects.requireNonNull(weakComponent);
        Objects.requireNonNull(displayAction);
        Action<ValDelegate<T>> action = Action.ofWeak(Objects.requireNonNull(weakComponent.get()), (localComponent, delegate)->{
            _runInUI(() ->
                UI.run( () -> {
                    try {
                        displayAction.accept(localComponent, delegate); // Here the captured value is used. This is extremely important!
                        /*
                             Since this is happening in another thread we are using the captured property item/value.
                             The property might have changed in the meantime, but we don't care about that,
                             we want things to happen in the order they were triggered.
                         */
                    } catch ( Exception e ) {
                        throw new RuntimeException(
                            "Failed to apply state of property '" + propertyRef.get() + "' to " +
                            "component '" + localComponent + "'.",
                            e
                        );
                    }
                })
            );
        });
        Optional.ofNullable(propertyRef.get()).ifPresent(
            property -> Viewable.cast(property).onChange(From.ALL, action)
        );
    }

    /**
     *  Use this to register a state change listener for the provided property list
     *  which will be executed by the UI thread (see {@link EventProcessor}).
     *
     * @param vals A property list whose state changes should be listened to on the UI thread.
     * @param c The component which is wrapped by this builder.
     * @param displayAction A consumer lambda receiving the action delegate and
     *                      is then executed by the UI thread.
     * @param <T> The type of the items wrapped by the provided property list.
     */
    protected final <T> void _onShow(
        Vals<T> vals, C c, BiConsumer<C, ValsDelegate<T>> displayAction
    ) {
        Objects.requireNonNull(vals);
        Objects.requireNonNull(displayAction);
        _onShow( vals, new WeakReference<>(c), displayAction );
    }

    protected final <T> UIForAnything<I,C,E> _withOnShow(
        Vals<T> vals, BiConsumer<C, ValsDelegate<T>> displayAction
    ) {
        Objects.requireNonNull(vals);
        Objects.requireNonNull(displayAction);
        return _with( thisComponent -> _onShow( vals, thisComponent, displayAction ) );
    }

    private <T> void _onShow(
        Vals<T> properties, WeakReference<C> weakComponent, BiConsumer<C, ValsDelegate<T>> displayAction
    ) {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(displayAction);
        Action<ValsDelegate<T>> action = Action.ofWeak(Objects.requireNonNull(weakComponent.get()), (localComponent, delegate)->{
            _runInUI(() ->{
                displayAction.accept(localComponent, delegate);
                /*
                    We make sure that the action is only executed if the component
                    is not disposed. This is important because the action may
                    access the component, and we don't want to get a NPE.
                */
            });
        });
        Viewables.cast(properties).onChange(action);
    }

    /**
     *  Exposes the this-pointer of the builder instance cast to the {@code I} type
     *  parameter of the builder class. <br>
     *  This is done to reduce the amount of type casting
     *  and warnings in the codebase. <br>
     * @return The builder instance itself based on the type parameter {@code <I>}.
     */
    protected final I _this() { return (I) this; }

    /**
     *  This method is used to dispose of the state of the builder,
     *  which means that the builder state disposes of its reference to either
     *  the wrapped component or the wrapped component or the composite of component
     *  factories which are used to build the wrapped component eagerly each time
     *  the wrapped component is accessed. <br>
     *  This is important to avoid memory leaks, as a component is typically
     *  part of a tree of components, and if one component is not garbage collected,
     *  then the whole tree is not garbage collected.
     */
    protected final void _disposeState() { _state().dispose(); }

    @Override
    public final int hashCode() {
        return _state().hashCode();
    }

    @Override
    @SuppressWarnings("EqualsGetClass")
    public final boolean equals( Object obj ) {
        if ( obj == null ) return false;
        if ( obj == this ) return true;
        if ( obj.getClass() != getClass() ) return false;
        UIForAnything<?,?,?> other = (UIForAnything<?,?,?>) obj;
        return _state().equals(other._state());
    }

    @Override
    public final String toString() {
        String componentTypeName = _state().componentType().getName();
        String builderTypeName   = getClass().getSimpleName();
        String asString          = builderTypeName + "[" + componentTypeName + "]";

        if ( _state().isDisposed() ) {
            // We make the whole string strikethrough if the builder is disposed.
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < asString.length(); i++ )
                sb.append(asString.charAt(i)).append("\u0336");
            asString = sb.toString();
        }

        return asString;
    }

}
