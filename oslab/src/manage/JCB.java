package manage;

// 存储进程信息
public class JCB {
    /**
     * 作业id
     */
    private int JobId;
    /**
     * 作业进入时间
     */
    private int InTimes;
    /**
     * 作业（进程）包含的指令数
     */
    private int InstruNum;
    /**
     * 作业（进程）优先级
     */
    private int Priority;
    /**
     * 作业指令集
     */
    private Instruction[] instructions;

    public JCB(int id, int priority, int InTimes, int instructionNum) {
        this.JobId = id;
        this.Priority = priority;
        this.InTimes = InTimes;
        this.InstruNum = instructionNum;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public int getPriority() {
        return Priority;
    }

    public int getJobId() {
        return JobId;
    }

    public void setJobId(int jobId) {
        this.JobId = jobId;
    }

    public int getInTimes() {
        return InTimes;
    }

    public void setInTimes(int inTimes) {
        this.InTimes = inTimes;
    }

    public int getInstruNum() {
        return InstruNum;
    }

    public void setInstruNum(int instruNum) {
        this.InstruNum = instruNum;
    }

    public Instruction[] getInstructions() {
        return instructions;
    }

    public void setInstructions(Instruction[] instructions) {
        this.instructions = instructions;
    }
}
