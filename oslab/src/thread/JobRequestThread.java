package thread;

import os.OperationSystem;

public class JobRequestThread extends Thread {
    /**
     * 系统管理器，用以获取系统资源
     */
    private OperationSystem os;
    /**
     * 判断作业请求的类型
     */
    private int  index;
    public JobRequestThread(OperationSystem os, int index) {
        super("JobRequest");
        this.os=os;
        this.index=index;
    }

    @Override
    public void run() {
        if(this.index==InterruptType.Read_Job_Interrupt)
        {
            while (true)
            {
                // 每10秒读取一次新作业请求
                if (this.os.getClock().getCurrentTime() % 10 == 0) {
                    this.os.getSchedule().getJobManage().CheckJob();
                }
            }
        }
        if(this.index==InterruptType.Add_Job_Interrupt)
        {
            // 调用作业管理 -> 添加作业
            this.os.getSchedule().getJobManage().addJob();
        }
    }
}
