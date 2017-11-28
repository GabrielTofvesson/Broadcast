package net.tofvesson.broadcast.support;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Immutable map that references mutable map. Used to dissuade modification of a map
 * @param <K> Key type
 * @param <V> Value type
 */
public class ImmutableReferenceMap<K, V> implements Map<K, V> {

    protected final Map<K, V> reference;

    public ImmutableReferenceMap(Map<K, V> reference){
        this.reference = reference;
    }

    @Override
    public int size() {
        return reference.size();
    }

    @Override
    public boolean isEmpty() {
        return reference.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return reference.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return reference.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return reference.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V remove(Object key) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public Set<K> keySet() {
        return reference.keySet();
    }

    @Override
    public Collection<V> values() {
        return reference.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return reference.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return reference.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        reference.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V replace(K key, V value) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new IllegalStateException("Unsupported action");
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new IllegalStateException("Unsupported action");
    }
}
