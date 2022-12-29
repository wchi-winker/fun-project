#include "my_tools.c"

void show_tasks_info(struct task_struct *task){
    struct task_struct *task_tmp = NULL;
    struct task_action *action_temp = NULL;
    printf("name\tstart_time\tpriority\tsize\n");
    list_for_each_entry(task_tmp, &task->tasks, tasks){
        printf("%s\t%d\t\t%d\t\t%.2fk\n", task_tmp->jobName, task_tmp->arriveTime, task_tmp->priority, task_tmp->size);
    }
    return NULL;
}

void createTasks(struct task_struct *task)
{
    readProcess(task);
    readProgram(task);
    readAction(task);
}

/* 遍历任务队列，符合到达时间的任务加入就绪队列 */
void get_begin_task(struct task_struct *task, struct task_struct *task_ready)
{
    struct task_struct *task_tmp, *next;
    list_for_each_entry_safe(task_tmp, next, &task->tasks, tasks){
        if(task_tmp->arriveTime == currentTime){
            strcpy(task_tmp->processStatus, READY); // 变更任务状态
            list_move_tail(&task_tmp->tasks, &task_ready->tasks); // 加入就绪队列中
        }
    }

}

/* 
** 遍历等待队列:
** 1.将阻塞时间-1
** 2.唤醒等待时间为0的任务，将其加入就绪队列
*/
void try_to_wake_up(struct task_struct *task_wait, struct task_struct *task_ready)
{
    int ret = 0;
    struct task_struct *task_tmp, *next;
    list_for_each_entry_safe(task_tmp, next, &task_wait->tasks, tasks){
        printf("%s\n",task_tmp->jobName);
        if(task_tmp->waitTime == 0){
            strcpy(task_tmp->processStatus, READY); // 变更任务状态
            list_move_tail(&task_tmp->tasks, &task_ready->tasks); // 加入就绪队列中
        }
    }

}

/* 先来先服务调度算法 */
struct task_struct * firstComeFirstServed(struct task_struct *task_ready, int priority)
{
    struct task_struct *task_tmp, *task_pick = NULL;

    /* 遍历绪队列，判断是否有高优先级任务抢占 */
    list_for_each_entry(task_tmp, &task_ready->tasks, tasks){
        if(task_tmp->priority > priority){  // 优先级相同则选择先进入就绪队列的任务
            priority = task_tmp->priority;
            task_pick = task_tmp;
        }
    }
    return task_pick;
}


/* 调度器，从就绪队列中选择一个任务加入运行队列 */
void schedule(struct task_struct *task_ready, struct task_struct **task_run)
{
    int priority = -1;
    struct task_struct *task_pick = NULL;
    
    /* 若运行队列进程状态不是运行态，则一定会发生调度 */
    if(*task_run != NULL)
        if(!strcmp((*task_run)->processStatus, RUN))
            priority = (*task_run)->priority;
    /* 找到需要抢占的进程 */
    task_pick = firstComeFirstServed(task_ready, priority);
    /* 无需抢占，继续执行之前的进程 */
    if(task_pick == NULL)
        return;
    
    /* 将当前运行的进程更改为就绪态 */
    if(*task_run != NULL)
        if(!strcmp((*task_run)->processStatus, RUN)){
            strcpy((*task_run)->processStatus, READY);
            list_add_tail(&(*task_run)->tasks, &task_ready->tasks);
        }
        

    /* 修改抢占进程的状态，并加入运行队列 */
    list_del(&task_pick->tasks);
    if(task_pick->startTime == -1)
        task_pick->startTime = currentTime;
    strcpy(task_pick->processStatus, RUN);
    (*task_run) = task_pick;
}

/* 任务执行，消耗时间,返回是否请求调度 */
void run(struct task_struct **Mtask, struct task_struct *task_run, struct task_struct *task_wait)
{
    int i;   
    struct task_action *action_temp = NULL; 
    
    for (i = 0; i < JOBNUMBER; i++)
    {
        if(!strcmp(Mtask[i]->processStatus, RUN))
            Mtask[i]->runTime += 1;
        if(!strcmp(Mtask[i]->processStatus, WAIT))
            Mtask[i]->waitTime -= 1;
    }
    
    if(task_run == NULL)
        return;
    
    // 遍历运行态任务的动作
    list_for_each_entry(action_temp, &task_run->action->actions, actions){  
        /* 判断当前时间是否有动作执行 */
        if(action_temp->time == task_run->runTime){ 
            if( action_temp->aid == 2){ // 触发IO请求，进入阻塞态
                strcpy(task_run->processStatus, WAIT);
                task_run->waitTime = action_temp->data; // 记录阻塞时间
                list_add_tail(&task_run->tasks, &task_wait->tasks); // 加入到阻塞队列中
            } else if(action_temp->aid == 3){ // 任务完成
                strcpy(task_run->processStatus, FINISH);
                task_run->endTime = currentTime;
                task_run->turnoverTime = task_run->endTime - task_run->arriveTime;
                task_run->useWeightTurnoverTime = task_run->turnoverTime * 1.0 / task_run->runTime;
            }
        }
    }
    
}

void indexTask(struct task_struct *task, struct task_struct **Mtask)
{
    int i = 0;
    struct task_struct *task_tmp;
    list_for_each_entry(task_tmp, &task->tasks, tasks){
        Mtask[i] = task_tmp;
        i++;
    }
}

void showReady(struct task_struct *task)
{
    printf("就绪队列：");
    struct task_struct *task_tmp=NULL;
    list_for_each_entry(task_tmp, &task->tasks, tasks){
        printf("%s ",task_tmp->jobName);
    }
    printf("\n");
}

void showWait(struct task_struct *task)
{
    printf("阻塞队列：");
    struct task_struct *task_tmp=NULL;
    list_for_each_entry(task_tmp, &task->tasks, tasks){
        printf("%s ",task_tmp->jobName);
    }
    printf("\n");
} 
int main(){
    int ret = 0;
    /* 任务队列、运行队列、就绪队列、等待队列 */
    struct task_struct *task = (struct task_struct *)malloc(sizeof(struct task_struct));
    struct task_struct *task_ready = (struct task_struct *)malloc(sizeof(struct task_struct));
    struct task_struct *task_wait = (struct task_struct *)malloc(sizeof(struct task_struct));
    struct task_struct *task_run = NULL;
    
    struct task_struct *Mtask[JOBNUMBER]; // 管理所有进程的指针数组

    /* 初始化链表 */
    INIT_LIST_HEAD(&task->tasks);
    INIT_LIST_HEAD(&task_ready->tasks);
    INIT_LIST_HEAD(&task_wait->tasks);
    
    createTasks(task);
    show_tasks_info(task);
    indexTask(task, Mtask);

    /* 模拟时钟 */
    while(1) {
/*         if(currentTime == 226){
            struct task_struct *task_tmp = NULL;
            task_tmp = list_entry(task_wait->tasks, struct task_struct, tasks);
            printf("%s\n",task_tmp->jobName);
            break;
        } */
        get_begin_task(task, task_ready); // 将新到达的任务加入就绪队列
        
        try_to_wake_up(task_wait, task_ready); // 唤醒等待时间为0的任务
        
        run(Mtask, task_run, task_wait);  // 任务执行，消耗时间, 可能发生任务阻塞以及内存调度
        schedule(task_ready, &task_run); // 轮询调度
        printJob(Mtask);
        showReady(task_ready);
        showWait(task_wait);
        printf("---------------------------------------------\n");
        currentTime++;
    }
    return 0;
}