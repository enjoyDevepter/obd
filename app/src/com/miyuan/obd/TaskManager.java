package com.miyuan.obd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 任务队列管理器
 */
public class TaskManager {

    /**
     * 任务队列
     */
    Map<Class, BaseTask> taskMaps;
    /**
     * 任务队列
     */
    private List<BaseTask> taskList;
    /**
     * 当前执行到的位置
     */
    private int currentLocation = 0;
    //private List<BaseTask> useList;
    /**
     * 线程池，用于处理并发异步任务
     */
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private CompletionService poolManager = new ExecutorCompletionService(threadPool);

    /**
     * 禁止构造
     */
    private TaskManager() {
        taskList = new LinkedList<>();
        taskMaps = new HashMap<>();
    }

    /**
     * 获得单例
     *
     * @return
     */
    public static TaskManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 添加一个任务
     *
     * @param task
     */
    public TaskManager addTask(BaseTask task) {
        taskList.add(task);
        addTaskToIndexMap(task);
        return this;
    }

    /**
     * 将Task任务添加到索引map集合中,方便查找
     *
     * @param task
     */
    private void addTaskToIndexMap(BaseTask task) {
        taskMaps.put(task.getClass(), task);
        for (BaseTask baseTask : task.getChildTask()) {
            addTaskToIndexMap(baseTask);
        }
    }

    /**
     * 根据类别获取Task对象
     *
     * @param clazz
     * @return
     */
    public BaseTask getTask(Class clazz) {
        BaseTask selectTask = taskMaps.get(clazz);
        if (null == selectTask) {
            throw new RuntimeException(clazz.getSimpleName() + "  is not contain in taskMaps.please check class is instance of BaseTask or clazz isn't exist");
        }
        return selectTask;
    }

    /**
     * 向线程池中添加用于并发执行的Task
     */
    public void addAsyTask(BaseTask task) {
        poolManager.submit(task);
    }

    /**
     * 执行下一个任务
     */
    public void next() {
        if ((taskList.size() - 1) >= currentLocation) {//如果还存在下一个任务
            taskList.get(currentLocation++).startExcute();
        }
    }


    /**
     * 任务状态
     */
    enum TaskState {
        /**
         * 未开始
         */
        UNSTART,
        /**
         * 运行中
         */
        RUNNABLE,
        /**
         * 已完成
         */
        FINISHED
    }

    /**
     * 单例持有器
     */
    private static final class InstanceHolder {
        private static final TaskManager INSTANCE = new TaskManager();
    }
}
