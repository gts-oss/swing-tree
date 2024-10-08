package swingtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import swingtree.style.ComponentExtension;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.*;

/**
 *  A custom event dispatcher for mouse enter and exit events based on the mouse
 *  cursor location exiting or entering a component's bounds even if the
 *  mouse cursor is on a child component.<br>
 *  This is in essence a fix for the default Swing behavior which also dispatches enter/exit
 *  events when the mouse cursor enters or exits the bounds of a child component
 *  which also has a listener for these events. So using this we try to
 *  make the behavior more predictable, reliable and intuitive.
 */
final class EnterExitComponentBoundsEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EnterExitComponentBoundsEventDispatcher.class);
    private static final EnterExitComponentBoundsEventDispatcher eventDispatcher = new EnterExitComponentBoundsEventDispatcher();
    private static final MouseListener dispatcherListener = new MouseAdapter() { };

    static void addMouseEnterListener(UI.ComponentArea area, Component component, MouseListener listener) {
        Map<Component, ComponentEnterExitListeners> allListeners = eventDispatcher.listeners[area.ordinal()];
        ComponentEnterExitListeners listeners = allListeners.computeIfAbsent(component, k -> {
            // ensures that mouse events are enabled
            k.addMouseListener(dispatcherListener);
            return new ComponentEnterExitListeners(area, component);
        });
        listeners.addEnterListener(listener);
    }

    static void addMouseExitListener(UI.ComponentArea area, Component component, MouseListener listener) {
        Map<Component, ComponentEnterExitListeners> allListeners = eventDispatcher.listeners[area.ordinal()];
        ComponentEnterExitListeners listeners = allListeners.computeIfAbsent(component, k -> {
            // ensures that mouse events are enabled
            k.addMouseListener(dispatcherListener);
            return new ComponentEnterExitListeners(area, component);
        });
        listeners.addExitListener(listener);
    }


    private final Map<Component, ComponentEnterExitListeners>[] listeners;

    private EnterExitComponentBoundsEventDispatcher() {
        listeners = new Map[UI.ComponentArea.values().length];
        for (int i = 0; i < listeners.length; i++)
            listeners[i] = new WeakHashMap<>();
        Toolkit.getDefaultToolkit().addAWTEventListener(this::onMouseEvent, AWTEvent.MOUSE_EVENT_MASK);
        Toolkit.getDefaultToolkit().addAWTEventListener(this::onMouseEvent, AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public void onMouseEvent(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;

            Component c = mouseEvent.getComponent();
            while (c != null) {
                for ( Map<Component, ComponentEnterExitListeners> listeners : this.listeners ) {
                    ComponentEnterExitListeners componentListenerInfo = listeners.get(c);

                    if (componentListenerInfo != null)
                        componentListenerInfo.dispatch(c, mouseEvent);
                }
                c = c.getParent();
            }
        }
    }

    enum Location {
        INSIDE, OUTSIDE
    }

    /**
     *  Contains the enter and exit listeners for a component as well as
     *  a flag indicating whether the mouse cursor is currently within the
     *  bounds of the component or not.
     */
    private static class ComponentEnterExitListeners {
        private final UI.ComponentArea area;
        private Location location;
        private final List<MouseListener> enterListeners;
        private final List<MouseListener> exitListeners;

        public ComponentEnterExitListeners( UI.ComponentArea area, Component component ) {
            this.area = area;
            this.location = Location.OUTSIDE;
            this.enterListeners = new ArrayList<>();
            this.exitListeners  = new ArrayList<>();
        }

        public void addEnterListener(MouseListener listener) {
            enterListeners.add(listener);
        }

        public void addExitListener(MouseListener listener) {
            exitListeners.add(listener);
        }

        public void dispatch(Component component, MouseEvent event) {
            assert isRelated(component, event.getComponent());

            switch (event.getID()) {
                case MouseEvent.MOUSE_ENTERED:
                    if (location == Location.INSIDE)
                        return;

                    location = onMouseEnter(component, event);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    if (location == Location.OUTSIDE)
                        return;

                    if (containsScreenLocation(component, event.getLocationOnScreen()))
                        return;

                    location = onMouseExit(component, event);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    if ( this.area != UI.ComponentArea.ALL ) {
                        if ( location == Location.INSIDE ) {
                            location = onMouseExit(component, event);
                        } else if ( location == Location.OUTSIDE ) {
                            location = onMouseEnter(component, event);
                        }
                    }
                break;
            }
        }

        private Location onMouseEnter(Component component, MouseEvent mouseEvent) {
            if (enterListeners.isEmpty())
                return Location.INSIDE;

            mouseEvent = withNewSource(mouseEvent, component);

            if ( this.area == UI.ComponentArea.ALL ) {
                dispatchEnterToAllListeners(mouseEvent);
            } else {
                Optional<Shape> componentArea = ComponentExtension.from((JComponent) component).getComponentArea(area);
                if ( !componentArea.isPresent() ) {
                    if ( this.area == UI.ComponentArea.BORDER )
                        return Location.INSIDE; // no border means nothing to enter or exit on
                    else
                        dispatchEnterToAllListeners(mouseEvent);
                } else if ( componentArea.get().contains(mouseEvent.getPoint()) )
                    dispatchEnterToAllListeners(mouseEvent);
                else
                    return Location.OUTSIDE;
            }
            return Location.INSIDE;
        }

        private void dispatchEnterToAllListeners(MouseEvent mouseEvent) {
            for (MouseListener listener : enterListeners) {
                try {
                    listener.mouseEntered(mouseEvent);
                } catch (Exception e) {
                    log.error("Failed to process mouseEntered event {}. Error: {}", mouseEvent, e.getMessage(), e);
                }
            }
        }

        private Location onMouseExit(Component component, MouseEvent mouseEvent) {
            if (exitListeners.isEmpty())
                return Location.OUTSIDE;

            mouseEvent = withNewSource(mouseEvent, component);

            if ( this.area == UI.ComponentArea.ALL ) {
                dispatchExitToAllListeners(mouseEvent);
            } else {
                Optional<Shape> componentArea = ComponentExtension.from((JComponent) component).getComponentArea(area);
                if ( !componentArea.isPresent() ) {
                    if ( this.area == UI.ComponentArea.BORDER )
                        return Location.OUTSIDE; // no border means nothing to enter or exit on
                    else
                        dispatchExitToAllListeners(mouseEvent);
                } else if ( !componentArea.get().contains(mouseEvent.getPoint()) )
                    dispatchExitToAllListeners(mouseEvent);
                else
                    return Location.INSIDE;
            }
            return Location.OUTSIDE;
        }

        private void dispatchExitToAllListeners(MouseEvent mouseEvent) {
            for (MouseListener listener : exitListeners) {
                try {
                    listener.mouseExited(mouseEvent);
                } catch (Exception e) {
                    log.error("Failed to process mouseExited event {}. Error: {}", mouseEvent, e.getMessage(), e);
                }
            }
        }
    }

    private static boolean isRelated(Component parent, Component other) {
        Component o = other;
        while (o != null) {
            if (o == parent)
                return true;
            o = o.getParent();
        }
        return false;
    }

    private static boolean containsScreenLocation(Component component, Point screenLocation) {
        Point compLocation = component.getLocationOnScreen();
        Dimension compSize = component.getSize();
        int relativeX = screenLocation.x - compLocation.x;
        int relativeY = screenLocation.y - compLocation.y;
        return (relativeX >= 0 && relativeX < compSize.width && relativeY >= 0 && relativeY < compSize.height);
    }

    private static MouseEvent withNewSource(MouseEvent event, Component newSource) {
        if ( event.getSource() == newSource )
            return event;

        // We need to convert the mouse position to the new source's coordinate system
        Point mousePositionRelativeToNewComponent = event.getPoint();
        SwingUtilities.convertPointToScreen(mousePositionRelativeToNewComponent, (Component) event.getSource());
        SwingUtilities.convertPointFromScreen(mousePositionRelativeToNewComponent, newSource);
        return new MouseEvent(
            newSource,
            event.getID(),
            event.getWhen(),
            event.getModifiersEx(),
            mousePositionRelativeToNewComponent.x,
            mousePositionRelativeToNewComponent.y,
            event.getClickCount(),
            event.isPopupTrigger(),
            event.getButton()
        );
    }

}