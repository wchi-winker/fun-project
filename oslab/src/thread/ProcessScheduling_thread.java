package thread;


import manage.JCB;
import manage.JobIn_thread;
import manage.PCB;
import os.OperationSystem;

import java.util.Vector;

public class ProcessScheduling_thread extends Thread{
    /**
     * 系统时间片长度 3
     */
    public static final int Times= 3;
    /**
     * 最大并发进程数，等于PCB池容量，设置为 4
     */
    public static final int MAX_CONCURRENT_PROCESS_NUM = 4;
    /**
     * 系统管理器，用以获取系统资源
     */
    private OperationSystem os;
    /**
     * 作业管理模块
     */
    private JobIn_thread jobProcessing;
    /**
     * 全体PCB队列(系统PCB表)
     */
    private Vector<PCB> allPCBQueue;
    /**
     * 完成PCB队列
     */
    private Vector<PCB> endPCBQueue;
    /**
     * 就绪队列
     */
    private Vector<PCB> readyQueue;
    /**
     * 作业后备队列
     */
    private Vector<JCB> reserveQueue;
    public ProcessScheduling_thread(OperationSystem os) {
        super("Process_scheduling_thread");
        this.os= os;
        this.jobProcessing= new JobIn_thread(this);
        this.allPCBQueue            = new Vector<>();
        this.readyQueue             = new Vector<>();
        this.endPCBQueue            = new Vector<>();
        this.reserveQueue           = new Vector<>();
    }


    @Override
    public void run() {
        // 每10秒读取一次新作业请求(至少等时钟开始后才会开始即按下启动按钮后)
        this.os.getCpu().interrupt(InterruptType.Read_Job_Interrupt);
        while (true) {
            // 等待可调度时机
            while (!this.os.getCpu().isCanSchedule()) { }
            // 作业调度,创建进程
            this.highLevelSchdule();
            // 进程调度
            this.ProcessScheduling();
            // 关闭调度
            this.os.getCpu().closeSchedule();
            // 刷新GUI
            this.os.getDashboard().refreshRunningProcess();
            // 判断是否存在运行态进程
            if (this.os.getCpu().getRunningPCB() == null) {
                //若完成全部进程，打印信息
                if(this.jobProcessing.getReadJobNum()==this.jobProcessing.getAlljobnum()){
                    this.os.getDashboard().headtitle("状态统计信息：");
                    for(int i=0;i<this.endPCBQueue.size();i++){
                        PCB temp=this.endPCBQueue.get(i);
                        this.os.getDashboard().process(temp.getEndTimes()
                                +":["+temp.getProID()+"："+(temp.getEndTimes()-temp.getTurnTimes())+"+"+temp.getInTimes()+"+"+temp.getRunTimes()+"]");
                    }
                    this.os.getDashboard().startButton.doClick();
                    return;
                }
                // CPU空闲
                this.os.getDashboard().consoleLog(this.getManager().getClock().getCurrentTime()+"："+"[CPU空闲]");
            } else {
                // 刷新GUI
               // this.os.getDashboard().refreshCPU();
                this.os.getDashboard().refreshRunningProcess();
                // 存在 执行当前指令
                this.os.getCpu().execute();
                // 判断进程是否运行完毕
                if (this.os.getCpu().getPC() > this.os.getCpu().getRunningPCB().getInstrucNum()) {
                    // 进程运行完毕
                    // CPU切换内核态
                    this.getManager().getCpu().switchToKernelState();
                    // 撤销当前进程
                    this.os.getCpu().getRunningPCB().cancel();
                    this.os.getCpu().setRunningPCB(null);
                    this.os.getCpu().setTimeSlice(0);
                    // CPU切换用户态
                    this.getManager().getCpu().switchToUserState();
                } else {
                    // 进程未运行完毕
                    // 如果时间片用完，则当前进程 运行态 -> 就绪态
                    if (this.os.getCpu().getTimeSlice() == 0) {
                        // CPU切换内核态
                        this.getManager().getCpu().switchToKernelState();
                        // 当前PCB 运行态 -> 就绪态
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
            }
            // 刷新GUI
            this.os.getDashboard().refreshQueues();
        }
    }

    /**
     * 作业调度
     */
    public synchronized void highLevelSchdule() {
        // 尝试从后备队列中挑选作业，创建相应进程
        this.getJobManage().tryAddProcess();
    }

    /**
     * 进程调度-时间片轮转法
     */
    public synchronized void ProcessScheduling() {
        // 就绪队列为空，或已有进程处于运行态，则低级调度结束
        if (this.readyQueue.size() == 0 || this.os.getCpu().getRunningPCB() != null) {
            return;
        }

        // 遍历就绪队列中所有进程，获取优先数最小的进程
        int priority = this.readyQueue.get(0).getPriority();
        int m = 0;
        for (int i = 1; i < this.readyQueue.size(); i++) {
            PCB pdb = this.readyQueue.get(i);
            if(priority > pdb.getPriority()){
                priority = pdb.getPriority();
                m = i;
            }
        }

        // CPU切换内核态
        this.getManager().getCpu().switchToKernelState();
        // 优先级最高PCB 就绪态 -> 运行态
        this.readyQueue.get(m).setPSW(PCB.RUNNING_STATE);
        // 恢复CPU现场
        this.getManager().getCpu().CPU_REC(this.readyQueue.get(m));
        synchronized (this){
            this.readyQueue.remove(m);
            // CPU切换用户态
            this.getManager().getCpu().switchToUserState();
        }
    }

    public OperationSystem getManager() {
        return os;
    }

    public void setManager(OperationSystem os) {
        this.os = os;
    }

    public JobIn_thread getJobManage() {
        return jobProcessing;
    }

    public void setJobManage(JobIn_thread jobManage) {
        this.jobProcessing =jobProcessing;
    }

    public Vector<PCB> getAllPCBQueue() {
        return allPCBQueue;
    }

    public void setAllPCBQueue(Vector<PCB> allPCBQueue) {
        this.allPCBQueue = allPCBQueue;
    }

    public Vector<PCB> getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(Vector<PCB> readyQueue) {
        this.readyQueue = readyQueue;
    }

    public Vector<JCB> getReserveQueue() {
        return reserveQueue;
    }

    public void setReserveQueue(Vector<JCB> reserveQueue) {
        this.reserveQueue = reserveQueue;
    }
    public Vector<PCB> getEndPCBQueue() {
        return endPCBQueue;
    }

    public OperationSystem getOs() {
        return os;
    }
}
