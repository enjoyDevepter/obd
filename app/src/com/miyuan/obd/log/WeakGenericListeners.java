package com.miyuan.obd.log;


import java.util.WeakHashMap;

/**
 * 基础的监听器集合(弱引用), 特点:<br>
 * 1.只会以弱引用持有监听器,避免内存泄露<br>
 * 2.通知时发现引用不存在就会自动清理<br>
 */
public class WeakGenericListeners<E extends BaseEventInfo> {

    /**
     * 各监听器引用集合
     */
    private WeakHashMap<Listener.GenericListener<E>, Object> references = new WeakHashMap<Listener.GenericListener<E>, Object>();

    /**
     * 添加监听器
     *
     * @param listener
     */
    public void add(Listener.GenericListener<E> listener) {
        references.put(listener, null);
    }

    public void remove(Listener.GenericListener<E> listener) {
        references.remove(listener);
    }

    /**
     * 通知事件到各监听器
     *
     * @param e
     */
    public void conveyEvent(E e) {
        for (Listener.GenericListener<E> listener : this.references.keySet()) {
            listener.onEvent(e);
        }
    }


}
