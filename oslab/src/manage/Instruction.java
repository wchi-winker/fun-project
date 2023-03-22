package manage;

public class Instruction {
    /**
     * 指令编号
     */
    private int Instruc_ID;
    /**
     * 指令状态，用于区分类型
     */
    private int Instruc_State;

    private short Instruc_Addr;
    //初始化
    public Instruction() {
        this.Instruc_ID = 0;
        this.Instruc_State = 0;
        this.Instruc_Addr = 0;
    }
    //构造函数
    public Instruction(int id, int state, short addr) {
        this.Instruc_ID = id;
        this.Instruc_State = state;
        this.Instruc_Addr = addr;
    }
    public int getInstruc_ID() {
        return Instruc_ID;
    }

    public void setInstruc_ID(int instruc_ID) {
        this.Instruc_ID = instruc_ID;
    }

    public int getInstruc_State() {
        return Instruc_State;
    }
    public void setInstruc_State(int instruc_State) {
        this.Instruc_State = instruc_State;
    }

    public short getInstruc_Addr() { return Instruc_Addr; }

    public void setInstruc_Addr(short instruc_Addr) { Instruc_Addr = instruc_Addr; }

    @Override
    public String toString() {
        return this.Instruc_ID + "," + this.Instruc_State + "," + this.Instruc_Addr;
    }

}
