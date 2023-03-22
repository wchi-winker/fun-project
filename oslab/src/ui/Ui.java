
package ui;

import manage.PCB;
import os.OperationSystem;
import thread.InterruptType;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class Ui extends JFrame {
    private OperationSystem os;

    public StyledDocument doc;
    public JTable readyTable;
    public DefaultTableModel readyTableInfo;

    private SimpleAttributeSet logStyle;
    private SimpleAttributeSet process;
    private SimpleAttributeSet workinfo;
    private SimpleAttributeSet headtitle;

    public Ui(OperationSystem os) {
        this.os = os;
        // 初始化界面元素
        this.initComponents();
        // 初始化界面数据格式
        this.initDataStructure();
        // 显示界面
        this.setVisible(true);
        //+time+
        File ProcessResults = new File("./output1/ProcessResults-140-LZ.txt");
        try {
            //如果路径不存在则新建路径
            if (!ProcessResults.getParentFile().exists())
            {
                ProcessResults.getParentFile().mkdir();
            }
            // 如果文件不存在，则新建
            if (!ProcessResults.exists()) {
                ProcessResults.createNewFile();
            }
            // 改变系统输出流
            System.setOut(new PrintStream(new FileOutputStream(ProcessResults)));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 控制台运行日志输出
     * @param content 输出内容
     */
    public synchronized void consoleLog(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", logStyle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制台进程调度日志输出
     * @param content 输出内容
     */
    public synchronized void process(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", process);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将控制台内容重定向到指定文件


    /**
     * 控制台作业调度日志输出
     * @param content 输出内容
     */
    public synchronized void workinfo(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", workinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制台标题信息日志输出
     * @param content 输出内容
     */
    public synchronized void headtitle(String content) {
        System.out.println(content);
        try {
            this.doc.insertString(this.doc.getLength(), content + "\n", headtitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 激活按钮
     */
    public synchronized void StartButton(boolean t) {
        this.startButton.setEnabled(t);
    }
    public synchronized void AddjobButton(boolean t){this.addJobButton.setEnabled(t);}

    /**
     * 刷新时间
     * @param time 时间
     */
    public synchronized void refreshTime(int time) {
        this.time.setText("" + time);
    }

    /**
     * 刷新CPU信息
     */


    /**
     * 刷新运行进程信息
     */
    public synchronized void refreshRunningProcess() {
        PCB running = this.os.getCpu().getRunningPCB();
    }


    /**
     * 刷新队列信息
     */
    public synchronized void refreshQueues() {
        // 刷新就绪队列
        while (this.readyTableInfo.getRowCount() > 0) {
            this.readyTableInfo.removeRow(0);
        }
        for (int i = 0; i < this.os.getSchedule().getReadyQueue().size(); ++i) {
            PCB temp = this.os.getSchedule().getReadyQueue().get(i);
            this.readyTableInfo.addRow(new String[]{
                    Integer.toString(temp.getProID()),
                    Integer.toString(temp.getPriority()),
                    Integer.toString(temp.getInstrucNum()-temp.getIR()),
                    Integer.toString(temp.getPC()),
                    Integer.toString(temp.getIR())});
        }
    }
    /**
     * 初始化控制台
     */
    public void initConsole() {
        // 绘制控制台富文本
        doc = this.console.getStyledDocument();
        // 保持滚动条在底端
        DefaultCaret caret = (DefaultCaret) this.console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        // 设置控制台输出样式
        this.logStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.logStyle, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.logStyle, 14);
        StyleConstants.setForeground(this.logStyle, Color.gray);

        this.process = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.process, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.process, 14);
        StyleConstants.setForeground(this.process, Color.darkGray);

        this.headtitle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.headtitle, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.headtitle, 20);
        StyleConstants.setForeground(this.headtitle, Color.black);

        this.workinfo = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.workinfo, "Microsoft YaHei UI");
        StyleConstants.setFontSize(this.workinfo, 14);
        StyleConstants.setForeground(this.workinfo, Color.BLUE);

    }

    /**
     * 初始化所有表格
     */
    public void initTable() {
        // 就绪队列
        String[] readyTableHeader = new String[]{"进程ID", "优先级", "剩余指令数", "PC", "IR"};
        this.readyTableInfo = new DefaultTableModel(new String[][]{}, readyTableHeader) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 关闭单元格编辑
                return false;
            }
        };
        this.readyTable = new JTable(this.readyTableInfo);
        this.readyTable.getTableHeader().setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        this.readyTable.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        this.readyQueueScrollPane.setViewportView(this.readyTable);

    }
    /**
     * 添加按钮点击事件
     */
    public void addButtonHandler() {
        // 启动/暂停按钮
        this.startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                os.getClock().setPause(!os.getClock().isPause());
                if (os.getClock().isPause()) {
                 //   clockState.setText("暂停");
                    startButton.setText("启动");
                } else {
                  //  clockState.setText("运行中");
                    startButton.setText("暂停");
                }
            }
        });
        // 添加作业按钮
        this.addJobButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //作业请求中断
                os.getCpu().interrupt(InterruptType.Add_Job_Interrupt);
            }
        });
    }

    /**
     * 初始化页面数据结构
     */
    public void initDataStructure() {
        // 初始化控制台
        this.initConsole();
        // 设置表格格式
        this.initTable();
        // 添加按钮绑定函数
        this.addButtonHandler();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - John
        panel = new JPanel();
        separator1 = new JSeparator();
        startButton = new JButton();
        addJobButton = new JButton();
        consoleScrollPane = new JScrollPane();
        console = new JTextPane();
        consoleLabel = new JLabel();
        readyQueueLabel = new JLabel();
        timeLabel = new JLabel();
        time = new JLabel();
        readyQueueScrollPane = new JScrollPane();

        //======== this ========
        setTitle("OSLAB_LZ");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel ========
        {
            panel.setBackground(new Color(0xccccff));
            panel.setMaximumSize(null);
            panel.setBorder ( new javax . swing. border .CompoundBorder ( new javax . swing. border .TitledBorder ( new javax . swing. border .EmptyBorder (
            0, 0 ,0 , 0) ,  "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e" , javax. swing .border . TitledBorder. CENTER ,javax . swing. border .TitledBorder
            . BOTTOM, new java. awt .Font ( "Dialo\u0067", java .awt . Font. BOLD ,12 ) ,java . awt. Color .
            red ) ,panel. getBorder () ) ); panel. addPropertyChangeListener( new java. beans .PropertyChangeListener ( ){ @Override public void propertyChange (java .
            beans. PropertyChangeEvent e) { if( "borde\u0072" .equals ( e. getPropertyName () ) )throw new RuntimeException( ) ;} } );
            panel.setLayout(null);
            panel.add(separator1);
            separator1.setBounds(new Rectangle(new Point(0, 363), separator1.getPreferredSize()));

            //---- startButton ----
            startButton.setText("\u542f\u52a8");
            startButton.setFont(new Font("\u6977\u4f53", Font.PLAIN, 16));
            startButton.setEnabled(false);
            startButton.setBackground(UIManager.getColor("Button.startBorderColor"));
            startButton.setContentAreaFilled(false);
            panel.add(startButton);
            startButton.setBounds(305, 35, 115, startButton.getPreferredSize().height);

            //---- addJobButton ----
            addJobButton.setText("\u6dfb\u52a0\u4f5c\u4e1a");
            addJobButton.setFont(new Font("\u6977\u4f53", Font.PLAIN, 16));
            addJobButton.setEnabled(false);
            addJobButton.setBackground(UIManager.getColor("Button.startBorderColor"));
            addJobButton.setContentAreaFilled(false);
            panel.add(addJobButton);
            addJobButton.setBounds(100, 35, 115, addJobButton.getPreferredSize().height);

            //======== consoleScrollPane ========
            {
                consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                //---- console ----
                console.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                console.setEditable(false);
                console.setBackground(new Color(0x33ccff));
                consoleScrollPane.setViewportView(console);
            }
            panel.add(consoleScrollPane);
            consoleScrollPane.setBounds(0, 260, 680, 265);

            //---- consoleLabel ----
            consoleLabel.setText("\u663e\u793aCPU\u8fd0\u884c\u72b6\u6001\uff1a");
            consoleLabel.setFont(new Font("\u6977\u4f53", Font.PLAIN, 16));
            consoleLabel.setBackground(new Color(0xffffcc));
            consoleLabel.setForeground(Color.black);
            consoleLabel.setHorizontalAlignment(SwingConstants.LEFT);
            panel.add(consoleLabel);
            consoleLabel.setBounds(0, 230, 200, 30);

            //---- readyQueueLabel ----
            readyQueueLabel.setText("\u5c31\u7eea\u961f\u5217:");
            readyQueueLabel.setFont(new Font("\u6977\u4f53", Font.PLAIN, 16));
            readyQueueLabel.setForeground(Color.black);
            panel.add(readyQueueLabel);
            readyQueueLabel.setBounds(5, 60, 75, 30);

            //---- timeLabel ----
            timeLabel.setText("\u5f53\u524d\u65f6\u95f4\uff1a");
            timeLabel.setFont(new Font("\u6977\u4f53", Font.PLAIN, 16));
            timeLabel.setForeground(Color.black);
            panel.add(timeLabel);
            timeLabel.setBounds(0, 5, 100, 35);

            //---- time ----
            time.setText("0");
            time.setFont(new Font("\u6977\u4f53", Font.PLAIN, 18));
            time.setForeground(Color.black);
            panel.add(time);
            time.setBounds(90, 5, 35, 35);

            //======== readyQueueScrollPane ========
            {
                readyQueueScrollPane.setBackground(new Color(0x009999));
                readyQueueScrollPane.setAutoscrolls(true);
                readyQueueScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                readyQueueScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            }
            panel.add(readyQueueScrollPane);
            readyQueueScrollPane.setBounds(0, 85, 805, 144);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel.getComponentCount(); i++) {
                    Rectangle bounds = panel.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel.setMinimumSize(preferredSize);
                panel.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(panel, BorderLayout.CENTER);
        setSize(1140, 555);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - John
    public JPanel panel;
    private JSeparator separator1;
    public JButton startButton;
    public JButton addJobButton;
    public JScrollPane consoleScrollPane;
    public JTextPane console;
    public JLabel consoleLabel;
    public JLabel readyQueueLabel;
    public JLabel timeLabel;
    public JLabel time;
    public JScrollPane readyQueueScrollPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    public void setManager(OperationSystem os) {
        this.os = os;
    }

    //int time= os.getClock().getCurrentTime();


}
