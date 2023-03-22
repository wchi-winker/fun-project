package thread;

import os.OperationSystem;

import java.util.TimerTask;


public class PST extends TimerTask {
    /**
     * 系统管理器，用以获取系统资源
     */
    private OperationSystem os;

    public PST(OperationSystem os) {
        this.os = os;
    }

    @Override
    public void run() {
        // 当前暂停标志为真，则不进行相关操作，调度也无法进行
        if (this.os.getClock().isPause()) {
            return;
        }
        // 时钟中断处理操作
        this.handleClockInterrupt();
    }

    /**
     * 时钟中断处理函数
     */
    public void handleClockInterrupt() {
        // 系统时间递增
        this.os.getClock().addTime();
        // 刷新GUI
        this.os.getDashboard().refreshTime(this.os.getClock().getCurrentTime());
        this.os.getCpu().interrupt(InterruptType.Clock_Interrupt);
    }

}
