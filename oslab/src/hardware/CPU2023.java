package hardware;

import thread.InterruptType;
import thread.JobRequestThread;
import manage.Instruction;
import manage.PCB;
import hardware.MMU;
import thread.PageInterruption_thread;
import thread.ProcessScheduling_thread;
import os.OperationSystem;
public class CPU2023 implements InterruptType {
    /**
     * 系统管理器，用以获取系统资源
     */
    private OperationSystem os;
    /**
     * 程序计数器，下一条指令的执行编号
     */
    private int PC;
    /**
     * 指令寄存器，正在执行的指令编号
     */
    private int IR;
    /**
     * 状态寄存器 0用户态  1内核态
     */
    private int PSW;
    public static final int USER_STATE = 0;
    public static final int KERNEL_STATE = 1;
    /**
     * 当前运行态的PCB指针
     */
    private PCB runningPCB;
    /**
     * 允许调度标志，控制调度时机
     * 只有该标志打开后，才可以进行三级调度，否则CPU执行指令和调度操作将会出现混乱
     */
    private volatile boolean canSchedule;
    /**
     * 当前剩余时间片
     */
    private int timeSlice;

    public CPU2023(OperationSystem os) {
        this.os                    = os;
        this.PC                         = 1;
        this.IR                         = 0;
        this.PSW                        = USER_STATE;
        this.runningPCB                 = null;
        //默认不可调度 等待时钟中断处理操作
        this.canSchedule                = false;
        this.timeSlice                  = 0;
    }

    /**
     * 中断处理
     */
    public synchronized void interrupt(int interruptVectorType) {
        switch (interruptVectorType) {
            // 时钟中断处理
            case Clock_Interrupt: {
                // 允许调度
                this.openSchedule();
                break;
            }
            // 实时作业请求中断
            case Add_Job_Interrupt: {
                //切换至内核态
                this.switchToKernelState();
                //作业请求中断处理
                JobRequestThread JRT= new JobRequestThread(this.os,Add_Job_Interrupt);
                JRT.start();
                break;
            }
            case Read_Job_Interrupt:{
                //切换至内核态
                this.switchToKernelState();
                //作业请求中断处理
                JobRequestThread JRT= new JobRequestThread(this.os,Read_Job_Interrupt);
                JRT.start();
                break;
            }
            case Page_Interruption:{
                //切换至内核态
                this.switchToKernelState();
                //缺页中断处理
                PageInterruption_thread PIT = new PageInterruption_thread(this.os);
                PIT.LoadPage();
                break;
            }
            default: {
                break;
            }
        }
        this.switchToUserState();
    }

    /**
     * 执行当前指令
     */
    public synchronized void execute() {
        // 刷新GUI
        this.os.getDashboard().refreshRunningProcess();
        if (this.runningPCB == null) {
            return;
        }
        // 指令指针自增并获取当前指令
        this.IR = this.PC++;
        this.getRunningPCB().setPC(this.PC);
        this.getRunningPCB().setIR(this.IR);
        Instruction currentInstrction = this.runningPCB.getInstruc()[IR-1];
        int r_addr = this.os.getMmu().VirtualAddressToRealAddress(this.runningPCB, currentInstrction.getInstruc_Addr());

        //更新运行时间
        this.getRunningPCB().setRunTimes(this.os.getClock().getCurrentTime()-this.getRunningPCB().getInTimes());
        //输出信息
        this.os.getDashboard().consoleLog(this.os.getClock().getCurrentTime()+"："
                +"[运行进程：" + this.runningPCB.getProID() + ":" + currentInstrction.getInstruc_ID() + "," + currentInstrction.getInstruc_State()
                + "," + currentInstrction.getInstruc_Addr() + "," + r_addr + "]");
        // 时间片 -1
        --this.timeSlice;
    }

    /**
     * 恢复CPU现场
     * @param pcb 即将进入运行态的进程 PCB
     */
    public synchronized void CPU_REC(PCB pcb) {
        this.PC         = pcb.getPC();
        this.IR         = pcb.getIR();
        this.timeSlice  = ProcessScheduling_thread.Times;
        // 进程设置运行态
        this.runningPCB = pcb;
        // 更新GUI
        this.os.getDashboard().refreshRunningProcess();
    }

    /**
     * 保护CPU现场
     */
    public synchronized void CPU_PRO() {
        this.runningPCB.setIR(this.IR);
        this.runningPCB.setPC(this.PC);
        // 进程解除运行态
        this.runningPCB = null;
        // 更新GUI
        this.os.getDashboard().refreshRunningProcess();
    }

    /**
     * 打开调度
     */
    public synchronized void openSchedule() {
        this.canSchedule = true;
    }

    /**
     * 关闭调度
     */
    public synchronized void closeSchedule() {
        this.canSchedule = false;
    }

    /**
     * 切换内核态
     */
    public synchronized void switchToKernelState() {
        if (this.PSW == KERNEL_STATE) {
            return;
        }
        this.PSW = KERNEL_STATE;
     //   this.os.getDashboard().refreshCPU();
    }

    /**
     * 切换用户态
     */
    public synchronized void switchToUserState() {
        if (this.PSW == USER_STATE) {
            return;
        }
        this.PSW = USER_STATE;
     //   this.os.getDashboard().refreshCPU();
    }

    public OperationSystem getManager() {
        return os;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getIR() {
        return IR;
    }

    public void setIR(int IR) {
        this.IR = IR;
    }

    public int getPSW() {
        return PSW;
    }

    public void setPSW(int PSW) {
        this.PSW = PSW;
    }

    public PCB getRunningPCB() {
        return runningPCB;
    }

    public void setRunningPCB(PCB runningPCB) {
        this.runningPCB = runningPCB;
    }

    public boolean isCanSchedule() {
        return canSchedule;
    }

    public void setCanSchedule(boolean canSchedule) {
        this.canSchedule = canSchedule;
    }

    public int getTimeSlice() {
        return timeSlice;
    }

    public void setTimeSlice(int timeSlice) {
        this.timeSlice = timeSlice;
    }
}
