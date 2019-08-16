package com.miyuan.obd.log;


import java.util.WeakHashMap;

/**
 * 基础的监听器集合（弱引用）, 特点：<br>
 * 1、只会以弱引用持有监听器，避免内存泄露<br>
 * 2、通知时发现引用不存在就会自动清理<br>
 *
 * @author baimi
 */
public class WeakSuccinctListeners {

    /**
     * 各监听器引用集合
     */
    private WeakHashMap<Listener.SuccinctListener, Object> references = new WeakHashMap<Listener.SuccinctListener, Object>();

    /**
     * 添加监听器
     *
     * @param listener
     */
    public void add(Listener.SuccinctListener listener) {
        references.put(listener, null);
    }

    /**
     * 通知事件到各个监听器
     */
    public void conveyEvent() {
        for (Listener.SuccinctListener listener : references.keySet()) {
            listener.onEvent();
        }
    }

}
