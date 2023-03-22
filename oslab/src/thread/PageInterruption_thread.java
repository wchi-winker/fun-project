package thread;

import hardware.kernel;
import manage.MCB;
import manage.PCB;
import os.OperationSystem;

import java.util.Vector;

public class PageInterruption_thread {
    private OperationSystem os;
    public PageInterruption_thread(OperationSystem os) {
        this.os = os;
    }

    public void LoadPage(){
        int out_page = PageOut();
        PageIn(out_page);
    }

    public void UpdatePage(int real_page_no){
        int c_time = this.os.getClock().getCurrentTime();
        os.getMmu().getMcb().get(real_page_no).setMTime(c_time);
    }

    // 页装入操作函数
    public void PageIn(int out_page){
        PCB RunningPCB = this.os.getCpu().getRunningPCB();
        short []page_table = RunningPCB.getPage_table();
        int pid = this.os.getCpu().getRunningPCB().getProID();
        int c_time = this.os.getClock().getCurrentTime();
        int IR = RunningPCB.getIR();
        int mid = RunningPCB.getInstruc()[IR-1].getInstruc_Addr();

        this.os.getDashboard().workinfo(  Math.max(c_time,0)+"："
                +"[缺页中断："+ pid + "," + os.getMmu().getMcb().get(out_page).getMID() + "," + mid +"]");
        // 在页表中去除被替换的块
        for(int i = 0; i < 16; i++){
            if(page_table[i] == out_page)
                page_table[i] = -1;
        }
        // 修改物理块号记录信息，存储时间，逻辑块号等
        os.getMmu().getMcb().get(out_page).setMTime(c_time);
        os.getMmu().getMcb().get(out_page).setMID(mid);
        // 将物理地址记录到页表中
        page_table[mid] = (short)out_page;

        RunningPCB.setPage_table(page_table);
    }

    // LRU 选出需要替换的页
    public int PageOut(){
        Vector<MCB> mcb =  os.getMmu().getMcb(); // 获取内存表
        PCB RunningPCB = this.os.getCpu().getRunningPCB(); // 获取正在运行的进程的PCB信息
        int OutAddr = RunningPCB.getMcbAddr(); // 获取正在运行进程的内存起始地址

        int time = mcb.get(OutAddr).getMTime(); // 获取对应页面的载入时间
        for(int i = OutAddr; i< kernel.PAGE_COUNT; i++){
            if(mcb.get(i).getMTime() < time){ // 把时间戳最小的页面作为需要替换的页，即最近最久未使用
                OutAddr = i;
            }
        }
        return OutAddr;
    }
}
