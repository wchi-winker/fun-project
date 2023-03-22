package hardware;
import manage.MCB;
import manage.PCB;
import os.OperationSystem;
import thread.InterruptType;
import thread.PageInterruption_thread;

import java.util.Vector;


public class MMU
{
	private int[][] TLB =new int[kernel.TLB_LENGTH][2];	//TLB快表

	private OperationSystem os;

	private Vector<MCB> mcb;    //物理主存
	public MMU(OperationSystem os)
	{
		this.os                    = os;
		this.mcb = new Vector<>();
		// 初始化页表
		ClearMCB();
	}

	/**
	* 初始化物理页
	 */
	public void ClearMCB()
	{
		// 清空快表
		for(int i=0;i<kernel.MEMORY_SIZE;i++)
		{
			MCB temp = new MCB(-1 ,-1, -1);
			this.mcb.add(temp);
		}
	}

	/**
	 * 获取进程物理页起始地址
	 */
	public int getMCBStartAddr(int pid){
		int start_addr = -1;
		for(int i=0;i<kernel.MEMORY_COUNT;i++){
			MCB temp = this.mcb.get(i);
			if(temp.getPid() == -1){
				start_addr = i;
				for(int j=i;j<i + kernel.PAGE_COUNT;j++) {
					this.mcb.get(j).setPid(pid);
				}
				return start_addr;
			}
		}
		return start_addr;
	}

	public void freeMCBAddr(int pid){
		for(int i=0;i<kernel.MEMORY_COUNT;i++){
			MCB temp = this.mcb.get(i);
			if(temp.getPid() == pid){
				this.mcb.get(i).setPid(-1);
			}
		}
	}

	public int CheckTLBVirtualPageNo(short line)
	{
		//在TLB中检测第line行的虚拟页号
		return this.TLB[line][0];
	}

	
	public int FindRealPageNo(PCB pcb, short virtual_page_no)
	{
		//在页表中查询虚拟页号对应的实际页框号
		if(pcb.getPage_table()[virtual_page_no] != -1)
			return pcb.getPage_table()[virtual_page_no];

		return -1;
	}
	
	public void AddTLB(short virtual_page_no,short real_page_no)
	{
		//添加TLB数据
		short location=0;
		while(this.TLB[location][0]!=-1)
		{
			// 循环遍历直到可以加入
			location=(short) ((location+1)%kernel.TLB_LENGTH);
		}
		this.TLB[location][0]=virtual_page_no;
		this.TLB[location][1]=real_page_no;
	}
	
	public void EditTLBData(short line,short data_1,short data_2)
	{
		//修改TLB快表的值
		this.TLB[line][0]=data_1;
		this.TLB[line][1]=data_2;
	}
	
	public short GetVirtualAddress_VirtualPage(short virtual_address)
	{
		//获取虚拟地址的页号
		return (short) ((virtual_address>>8)&0xFF);	
	}
	
	public short GetVirtualAddress_Offset(short virtual_address)
	{
		//获取虚拟地址的偏移，双字节数据的位移
		return (short) (virtual_address&0xFF);
	}
	
	public int PageToRealAddress(short num)
	{
		//将传入的页/块号num转换成为该页/块的基地址
		//该功能对内存与磁盘地址同样有效
		if(num<kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE)
			return num*kernel.SINGLE_PAGE_SIZE;
		else
		{
			num=(short) (num-(kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE));
			//cylinder磁道、sector扇区、offset偏移
			int cylinder=num/kernel.HARDDISK_SECTOR_NUM;	//计算磁道
			int sector=num%kernel.HARDDISK_SECTOR_NUM;		//计算扇区
			int offset=0;									//计算偏移
			return HardDisk.harddisk.CreateAddress(cylinder, sector, offset);
		}
	}

	public int VirtualAddressToRealAddress(PCB pcb,short virtual_address)
	{
		//将虚拟地址转换为实地址
		int real_page_no = FindRealPageNo(pcb, virtual_address);	// 在页表汇总查找对应的主存块号
		// 页表中不存在该项目
		if(real_page_no==-1)
		{
			pcb.getSchedule().getManager().getCpu().interrupt(InterruptType.Page_Interruption);
			// 重新查询页表
			real_page_no = FindRealPageNo(pcb, virtual_address);
		}
		// 页表中存在该项目，更新时间戳
		else {
			PageInterruption_thread PIT = new PageInterruption_thread(this.os);
			PIT.UpdatePage(real_page_no);
		}

		return real_page_no;
	}

	public Vector<MCB> getMcb() {
		return mcb;
	}

	public void setMcb(Vector<MCB> mcb) {
		this.mcb = mcb;
	}
}
