package thread;

public interface InterruptType {
    // 中断类型
    //时钟中断
    int Clock_Interrupt = 0;
    //添加中断
    int Add_Job_Interrupt= 1;
    //读取中断
    int Read_Job_Interrupt =2;
    // 缺页中断
    int Page_Interruption = 3;
}
