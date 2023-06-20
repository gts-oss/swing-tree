package swingtree.animation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *  Runs an {@link Animation} on a {@link Component} according to a {@link LifeTime} and a {@link StopCondition}.
 */
class ComponentAnimator
{
    private final WeakReference<Component> _compRef;
    private final LifeTime      _lifeTime;
    private final StopCondition _condition;
    private final Animation     _animation;


    ComponentAnimator(
        Component     component, // may be null if the animation is not associated with a specific component
        LifeTime lifeTime,
        StopCondition condition,
        Animation     animation
    ) {
        _compRef   = component == null ? null : new WeakReference<>(component);
        _lifeTime = Objects.requireNonNull(lifeTime);
        _condition = Objects.requireNonNull(condition);
        _animation = Objects.requireNonNull(animation);
    }

    public Optional<JComponent> component() {
        if ( _compRef == null ) return Optional.empty();
        Component _component = this._compRef.get();
        return Optional.ofNullable( _component instanceof JComponent ? (JComponent) _component : null );
    }

    private AnimationState _createState( long now, ActionEvent event ) {
        long duration = _lifeTime.getDurationIn(TimeUnit.MILLISECONDS);
        long howLongIsRunning = Math.max(0, now - _lifeTime.getStartTimeIn(TimeUnit.MILLISECONDS));
        long howLongCurrentLoop = howLongIsRunning % duration;
        long howManyLoops       = howLongIsRunning / duration;
        double progress         = howLongCurrentLoop / (double) duration;
        return new AnimationState() {
            @Override public double progress()       { return progress;     }
            @Override public long currentIteration() { return howManyLoops; }
            @Override public LifeTime lifetime()     { return _lifeTime;    }
            @Override public ActionEvent event()     { return event;        }
        };
    }

    boolean run( long now, ActionEvent event )
    {
        if ( now < _lifeTime.getStartTimeIn(TimeUnit.MILLISECONDS) )
            return true;

        AnimationState state = _createState(now, event);
        boolean shouldContinue = false;

        try {
            shouldContinue = _condition.check(state);
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        Component component = _compRef == null ? null : _compRef.get();

        if ( _compRef != null && component == null )
            return false; // There was a component but it has been garbage collected.

        if ( !shouldContinue ) {
            try {
                _animation.finish(state); // An animation may want to do something when it is finished (e.g. reset the component to its original state).
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            return false;
        }

        try {
            _animation.run(state);
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        if ( component != null ) {
            component.revalidate();
            component.repaint();
        }

        return true;
    }
}
