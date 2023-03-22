package manage;

// 存储主存块信息
public class MCB {
    /**
     * 主存块id
     */
    private int MID;
    /**
     * 主存块存储时间，用于LRU选择替换对象
     */
    private int MTime;
    /**
     * 记录当前页被哪个进程占用
     */
    private int Pid;
    public MCB(int MID, int MTime,int Pid){
        this.MID = MID;
        this.MTime = MTime;
        this.Pid = Pid;
    }

    public void setMID(int MID) {
        this.MID = MID;
    }

    public void setMTime(int MTime) {
        this.MTime = MTime;
    }

    public int getMID() {
        return MID;
    }

    public int getMTime() {
        return MTime;
    }

    public int getPid() {
        return Pid;
    }

    public void setPid(int pid) {
        Pid = pid;
    }
}
