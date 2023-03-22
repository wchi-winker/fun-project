package os;
import ui.Ui;
import hardware.*;
import thread.ProcessScheduling_thread;

public class OperationSystem {
    /**
     * 系统时钟
     */
    private Clock_thread clock;
    /**
     * CPU 中央处理器
     */
    private CPU2023 cpu;
    /**
     * 内存管理单元
     */
    private MMU mmu;
    /**
     * 进程调度模块
     */
    private ProcessScheduling_thread schedue;
    /**
     * 图像化界面
     */
    private Ui dashboard;

    public OperationSystem() {
        // 启动图形界面
        this.dashboard      = new Ui(this);
        this.clock          = new Clock_thread(this);
        this.mmu            = new MMU(this);
        this.cpu            = new CPU2023(this);
        this.schedue = new ProcessScheduling_thread(this);
    }


    /**
     * 开始运行
     */
    public void start() {
        //激活添加作业按钮
        this.dashboard.AddjobButton(true);
        //若作业数大于5，激活启动按钮，否则提示添加作业
        this.dashboard.StartButton(this.schedue.getJobManage().getAlljobnum()>=5);
        // 时钟线程启动等待启动按钮按下开始计时
        this.clock.start();
        // 调度线程启动(至少等时钟开始后才会开始即按下启动按钮后)
        this.schedue.start();
        this.dashboard.headtitle("进程调度事件：");
    }
    public Clock_thread getClock() {
        return clock;
    }

    public CPU2023 getCpu() {
        return cpu;
    }

    public ProcessScheduling_thread getSchedule() {
        return schedue;
    }

    public Ui getDashboard() {
        return dashboard;
    }

    public MMU getMmu(){return mmu;}

}
