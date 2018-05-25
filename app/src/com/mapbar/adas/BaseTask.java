package com.mapbar.adas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * 任务栈基类<br/>
 */
public abstract class BaseTask implements Callable {


    /**
     * 子任务集合,
     */
    private List<BaseTask> childTask = new ArrayList<BaseTask>();

    /**
     * 当前执行到的位置
     */
    private int currentLocation = 0;

    /**
     * 子任务是否并发<br/>
     * 默认值：false  非并发
     */
    private boolean childTaskIsConcurrency = false;


    /**
     * 获取父节点的任务项
     */
    private BaseTask parentTask;

    /**
     * 当前任务执行状态  默认值{@link com.mapbar.adas.TaskManager.TaskState#UNSTART}
     */
    private TaskManager.TaskState taskState = TaskManager.TaskState.UNSTART;

    /**
     * 获取当前任务执行状态{@link #taskState}
     *
     * @return
     */
    public TaskManager.TaskState getTaskState() {
        return taskState;
    }

    /**
     * 获取父节点的任务项
     */
    public BaseTask getParentTask() {
        return parentTask;
    }

    /**
     * 设置当前Task的根节点
     *
     * @param parentTask
     */
    private void setParentTask(BaseTask parentTask) {
        this.parentTask = parentTask;
    }

    /**
     * 获取根节点的任务项
     *
     * @return
     */
    public BaseTask getRootTask() {
        BaseTask rootTask;
        if (null == parentTask) {
            rootTask = this;
        } else if (null == parentTask.getRootTask()) {
            rootTask = parentTask;
        } else {
            rootTask = parentTask.getRootTask();
        }
        return rootTask;
    }

    /**
     * 添加子任务
     *
     * @param task
     * @return 返回当前对象
     */
    public BaseTask addChildTask(BaseTask task) {
        task.setParentTask(this);//将当前任务作为父节点设置给子任务
        childTask.add(task);
        return this;
    }

    public List<BaseTask> getChildTask() {
        return childTask;
    }

    /**
     * 获取当前任务的子任务是否为并发执行 {@link #childTaskIsConcurrency}
     *
     * @return
     */
    public boolean isChildTaskIsConcurrency() {
        return childTaskIsConcurrency;
    }

    /**
     * 设置当前任务的子任务会否为并发执行 {@link #childTaskIsConcurrency}
     *
     * @param childTaskIsConcurrency
     */
    public void setChildTaskIsConcurrency(boolean childTaskIsConcurrency) {
        this.childTaskIsConcurrency = childTaskIsConcurrency;
    }

    /**
     * 当前任务完成后需要回调的方法,需要手动回调当前方法<br/>
     */
    public synchronized final void complate() {
        if (TaskManager.TaskState.FINISHED != taskState) {//TODO 并发执行的情况下，有可能当前方法被执行多次

            taskState = TaskManager.TaskState.FINISHED;
            if (null != parentTask) {
                parentTask.onChildComplate(this);//通知父类，当前子任务已经完成了
            } else {
                TaskManager.getInstance().next();//如果当前任务不存在父节点,说明是root节点下面的任务
            }
        }
    }


    /**
     * 当前方法用户可以选择手动覆盖<br/>
     * 如果有特殊逻辑可以先覆盖当前方法，然后通过super.complate来执行下一个任务切换的方法，或者通过super.onChildComplate继续按原有的逻辑执行下去
     */
    protected synchronized void onChildComplate(BaseTask baseTask) {
        /**当前子任务是串行，并且还有下一个任务没有执行完*/
        if (!childTaskIsConcurrency && excuteNextChildTask()) {
            return;
        }
        complate();

    }

    /**
     * 执行子类的所有任务<br/>
     * 子任务执行的时机由调用者自己决定。默认是在当前任务完成之后进行调用
     */
    public void excuteChildTask() {
        if (childTask.size() >= 1) {//如果当前任务存在子任务。执行子任务
            if (childTaskIsConcurrency) {//如果当前任务的子任务是并发执行的。则一次性将所有的任务添加到线程池中去
                for (BaseTask baseTask : childTask) {
                    if (TaskManager.TaskState.UNSTART == baseTask.getTaskState()) {
                        TaskManager.getInstance().addAsyTask(baseTask);
                    }
                }
            } else {//如果当前任务的子任务是串行的。则依次执行所有的子任务
                excuteNextChildTask();
            }

        }
    }

    /**
     * 执行下一个子任务
     *
     * @return 如果下一个任务不存在了，则直接false，反之返回true
     */
    private boolean excuteNextChildTask() {
        if (!childTaskIsConcurrency && (childTask.size() - 1) >= currentLocation) {//当前子任务非并发，并且还存在下一个任务
            childTask.get(currentLocation++).startExcute();
            return true;
        }
        return false;
    }


    /**
     * 把当前任务扔到ui线程去执行。而不是一条直线，一直进行下去
     */
    public final void startExcute() {
        if (GlobalUtil.isNotUIThread()) {
            GlobalUtil.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    excuteMethod();
                }
            });
        } else {
            excuteMethod();
        }
    }

    /**
     * 运行于子线程
     *
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        excuteMethod();
        return null;
    }

    protected void excuteMethod() {

        taskState = TaskManager.TaskState.RUNNABLE;//修改执行状态

        excute();

    }

    /**
     * 任务体<br/>
     * 如果当前任务是串行的，则运行于主线程<br/>
     * 如果当前任务是并发执行的，则运行于子线程<br/>
     */
    public abstract void excute();


    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
