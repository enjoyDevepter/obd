package com.mapbar.adas.log;


import com.mapbar.adas.log.Listener.SimpleListener;

import java.util.WeakHashMap;

/**
 * 基础的监听器集合(弱引用),特点:<br>
 * 1. 只会以弱引用持有监听器，避免内存泄露<br>
 * 2. 通知时发现引用不存在就会自动清理<br>
 *
 * @param <E> 具体的监听器类型,必须实现 {@link SimpleListener}
 * @author baimi
 */
public class WeakSimpleListeners<E extends Enum<?>> {

    /**
     * 各监听器引用集合
     */
    private WeakHashMap<SimpleListener<E>, Object> references = new WeakHashMap<SimpleListener<E>, Object>();

    /**
     * 添加监听器
     *
     * @param listener
     */
    public void add(SimpleListener<E> listener) {
        this.references.put(listener, null);
    }

    /**
     * 通知事件到各监听器
     *
     * @param e
     */
    public void conveyEvent(E e) {
        for (SimpleListener<E> listener : this.references.keySet()) {
            listener.onEvent(e);
        }
    }

}
