package hardware;

import thread.PST;
import os.OperationSystem;

import java.util.Timer;
public class Clock_thread extends Timer {
    private OperationSystem os;
    /**
     * 系统时间间隔，单位 ms
     */
    public static final int INTERVAL = 1000;
    /**
     * 当前系统时间
     */
    private volatile int COUNTTIME;
    /**
     * 暂停标志
     */
    private volatile boolean pause;

    public Clock_thread(OperationSystem os) {
        super("Clock");
        this.os = os;
        this.COUNTTIME = -1;
        //默认时钟不走，等待启动按钮
        this.pause = true;
    }

    /**
     * 时间增加
     */
    public void addTime() {
        ++this.COUNTTIME;
    }

    /**
     * 开始执行，并设置时钟中断 1000ms
     *
     */
    public void start() {
        PST PST=  new PST(this.os);
        this.schedule(PST, 0, INTERVAL);
    }

    public int getCurrentTime() {
        return COUNTTIME;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
}
