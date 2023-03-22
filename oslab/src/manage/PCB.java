package manage;

import hardware.kernel;
import thread.ProcessScheduling_thread;

import java.util.Random;


public class PCB {
    // 进程状态常量
    public static final short READY_STATE = 0;
    public static final short RUNNING_STATE = 1;
    public static final short FINISH_STATE = 2;
    /**
     * 页表
     */
    private short []page_table=new short[(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE];

    /**
     * 调度模块
     */
    private ProcessScheduling_thread PST;
    /**
     * 进程编号
     */
    private int ProID;
    /**
     * 进程优先级
     * 取值范围1-5，数字越小，优先级越大
     */
    private int Priority;
    /**
     * 指令数量
     */
    private int InstrucNum;
    /**
     * 作业指令集
     */
    private Instruction[] Instruc;
    /**
     * 程序计数器，下一条指令的执行编号
     */
    private int PC;
    /**
     * 指令寄存器，正在执行的指令编号
     */
    private int IR;
    /**
     * 进程状态
     */
    private short PSW;
    /**
     * 进程创建时间
     */
    private int InTimes;
    /**
     * 进程结束时间
     */
    private int EndTimes;
    /**
     * 进程周转时间
     */
    private int TurnTimes;
    /**
     * 进程运行时间
     */
    private int RunTimes;
    /**
     * PCB内存块始地址
     */
    private int McbAddr;

    public PCB(ProcessScheduling_thread PST) {
        this.PST = PST;
        this.PC                 = 1;
        this.IR                 = 0;
        this.PSW = READY_STATE;
    }

    /**
     * 进程创建原语
     * @param jcb JCB作业控制块
     */
    public void create(JCB jcb) {
        //初始化信息
        this.ProID = jcb.getJobId();
        this.Priority = jcb.getPriority();
        this.InstrucNum = jcb.getInstruNum();
        this.InTimes = this.PST.getManager().getClock().getCurrentTime();
        this.TurnTimes = this.InTimes-jcb.getInTimes();
        this.RunTimes = 0;
        this.Instruc = jcb.getInstructions();
        this.PageTableCreate(this.page_table); // 初始化页表
     synchronized (this.PST) {
         // 切换至核心态
         this.PST.getManager().getCpu().switchToKernelState();
         this.McbAddr = this.PST.getManager().getMmu().getMCBStartAddr(this.ProID); // 分配内存块并获取起始地址
         this.PST.getAllPCBQueue().add(this);
         // 创建后即加入就绪队列
         this.PST.getReadyQueue().add(this);
         // 切换回用户态
         this.PST.getManager().getCpu().switchToUserState();
         this.PST.getManager().getDashboard().process(this.getSchedule().getManager().getClock().getCurrentTime()
                 +"：[创建进程：" + this.ProID +"," + this.McbAddr + ",顺序分配]");
        }
    }

    /**
     * 进程撤销原语
     */
    public void cancel() {
        synchronized (this.PST) {
            //切换至核心态
            this.PST.getManager().getCpu().switchToKernelState();
            // 设置收尾信息
            this.PSW = FINISH_STATE;
            this.EndTimes = this.PST.getManager().getClock().getCurrentTime();
            this.RunTimes = this.EndTimes - this.InTimes;
            this.TurnTimes=this.TurnTimes+this.RunTimes;
            this.PST.getEndPCBQueue().add(this);
            this.PST.getAllPCBQueue().remove(this);
            this.PST.getOs().getMmu().freeMCBAddr(this.ProID);
            //切换回用户态
            this.PST.getManager().getCpu().switchToUserState();
            this.PST.getManager().getDashboard().process( this.PST.getManager().getClock().getCurrentTime()+"：[终止进程：" + this.ProID +"]");
        }
    }

    /**
     * 初始化页表
     */
    public void PageTableCreate(short []page_table)
    {
        for(int i=0;i<kernel.PAGE_TABLE_LENGTH;i++)
        {
            page_table[i]=-1;
        }
    }

    public short CheckPageTable(short line_no)
    {
        //查询第line_no项表的值
        return this.page_table[line_no];
    }
    public ProcessScheduling_thread getSchedule() {
        return PST;
    }

    public void setSchedule(ProcessScheduling_thread PST) {
        this.PST = PST;
    }

    public int getProID() {
        return ProID;
    }

    public void setProID(int proID) {
        this.ProID = proID;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        this.Priority = priority;
    }

    public int getInstrucNum() {
        return InstrucNum;
    }

    public void setInstrucNum(int instrucNum) {
        this.InstrucNum = instrucNum;
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

    public short getPSW() {
        return PSW;
    }

    public void setPSW(short PSW) {
        this.PSW = PSW;
    }

    public int getInTimes() {
        return InTimes;
    }

    public void setInTimes(int inTimes) {
        this.InTimes = inTimes;
    }

    public int getEndTimes() {
        return EndTimes;
    }

    public void setEndTimes(int endTimes) {
        this.EndTimes = endTimes;
    }

    public int getTurnTimes() {
        return TurnTimes;
    }

    public void setTurnTimes(int turnTimes) {
        this.TurnTimes = turnTimes;
    }

    public int getRunTimes() {
        return RunTimes;
    }

    public void setRunTimes(int runTimes) {
        this.RunTimes = runTimes;
    }

    public Instruction[] getInstruc() {
        return Instruc;
    }

    public void setInstruc(Instruction[] instruc) {
        this.Instruc = instruc;
    }

    public int getMcbAddr() {
        return McbAddr;
    }

    public short[] getPage_table() {
        return page_table;
    }

    public void setPage_table(short[] page_table) {
        this.page_table = page_table;
    }
}
