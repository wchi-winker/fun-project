package thread;

import manage.PCB;
import os.OperationSystem;

import java.util.TimerTask;


public class InputBlock_thread extends TimerTask {

    ProcessScheduling_thread PST;

    // 需要唤醒的阻塞进程ID
    private int blockQueueId;

    public InputBlock_thread(ProcessScheduling_thread schedue, int blockQueueId) {
        this.PST = schedue;
        this.blockQueueId = blockQueueId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000); // 等待２秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 时钟中断处理操作
        this.WakeUpBlockProcess();
    }

    /**
     * 时钟中断处理函数
     */
    public void WakeUpBlockProcess() {
        // 系统时间递增
        this.os.getClock().addTime();
        // 刷新GUI
        this.os.getDashboard().refreshTime(this.os.getClock().getCurrentTime());
        this.os.getCpu().interrupt(InterruptType.Clock_Interrupt);

        // CPU切换内核态
        this.PST.getManager().getCpu().switchToKernelState();
        // 阻塞态 -> 就绪态
        synchronized (this) {
            this.os.getDashboard().process( this.os.getClock().getCurrentTime()+"："+"[重新进入就绪队列："
                    +this.os.getCpu().getRunningPCB().getProID()+"，"
                    + (this.getManager().getCpu().getRunningPCB().getInstrucNum()-this.os.getCpu().getIR())+"]");
            this.readyQueue.add(this.os.getCpu().getRunningPCB());
        }
        // 保护CPU现场
        this.os.getCpu().getRunningPCB().setPSW(PCB.READY_STATE);
        this.os.getCpu().CPU_PRO();
        // CPU切换用户态
        this.getManager().getCpu().switchToUserState();
    }

}
