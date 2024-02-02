package swingtree.style;

import java.util.Objects;

/**
 *  A wrapper for a value that is produced from a {@link RenderConf} through
 *  a {@link CacheProducerAndValidator#produce(RenderConf, ComponentAreas)} implementation and validated against
 *  a previous {@link RenderConf} through a
 *  {@link CacheProducerAndValidator#leadsToSameValue(RenderConf, RenderConf, ComponentAreas)}
 *  implementation.
 *  <p>
 *  This design is possible due to the fact that the {@link RenderConf} is deeply immutable
 *  and can be used as a key data structure for caching.
 *
 * @param <T> The type of the cached value.
 */
final class Cached<T> implements CacheProducerAndValidator<T>
{
    private final CacheProducerAndValidator<T> _producerAndValidator;
    private T _value;


    Cached( CacheProducerAndValidator<T> producerAndValidator) {
        _producerAndValidator = Objects.requireNonNull(producerAndValidator);
    }

    final Cached<T> validate( RenderConf oldState, RenderConf newState, ComponentAreas context ) {
        if ( _value != null && !_producerAndValidator.leadsToSameValue(oldState, newState, context) ) {
            return new Cached<>(_producerAndValidator);
        }
        return this;
    }

    final T getFor( RenderConf currentState, ComponentAreas context ) {
        if ( _value == null )
            _value = _producerAndValidator.produce(currentState, context);
        return _value;
    }

    final boolean exists() {
        return _value != null;
    }

    @Override
    public T produce(RenderConf currentState, ComponentAreas context) {
        return _producerAndValidator.produce(currentState, context);
    }

    @Override
    public boolean leadsToSameValue(RenderConf oldState, RenderConf newState, ComponentAreas context) {
        return _producerAndValidator.leadsToSameValue(oldState, newState, context);
    }

}
