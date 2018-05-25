package com.mapbar.adas.log;

public interface IEventInfo<E extends Enum<?>> {

    /**
     * @return 事件标识
     */
    E getEvent();

    /**
     * 设置事件标识
     *
     * @param event
     */
    void setEvent(E event);

}
