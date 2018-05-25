package com.mapbar.adas.log;


/**
 * 监听器(用于归类)
 *
 * @author baimi
 */
public interface Listener {

    /**
     * 简洁的监听器
     */
    interface SuccinctListener extends Listener {

        /**
         * 通知监听器
         */
        void onEvent();
    }

    /**
     * 简单的监听器
     */
    interface SimpleListener<E extends Enum<?>> extends Listener {

        /**
         * 通知监听器
         */
        void onEvent(E event);
    }

    /**
     * 通用的监听器
     */
    interface GenericListener<E extends BaseEventInfo> extends Listener {

        /**
         * 通知监听器
         *
         * @param eventInfo 事件信息(里面可以获得事件类型,可以自行继承或实现{@link BaseEventInfo}以支持更多的信息传递)持
         */
        void onEvent(E eventInfo);
    }
}
