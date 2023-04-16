#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <string.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

typedef struct _FILE_INFO_
{
    int key_len;
    int option;
    char file_path[50];
    char key[50];
} FILE_INFO;

FILE_INFO file_info;

static pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t  cond = PTHREAD_COND_INITIALIZER;
static int data_ready = 0;
static int data = 0;

#define FILE_ENTRY _IOW('d', 1, struct _FILE_INFO_)
#define FILE_DETRY _IOW('d', 2, struct _FILE_INFO_)

void *file_entryption(void *arg) {
    int fd_drive;

    /* 打开驱动文件 */
    fd_drive = open("/dev/file_entryption", O_RDONLY);
	if (fd_drive < 0) {
		printf("Open device failed\n");
		return ((void *)-1);
	}

    int ret = 0;

    while(1){
        pthread_mutex_lock(&lock);
        while (!data_ready) {
            pthread_cond_wait(&cond, &lock);
        }
        data_ready = 0;
        
        if(file_info.option == 1){
            ret = ioctl(fd_drive, FILE_ENTRY, &file_info);
        } else if(file_info.option == 2){
            ret = ioctl(fd_drive, FILE_DETRY, &file_info);
        }
        if(ret < 0) {
            printf("Failed to encrypt file:%s\n",file_info.file_path);
            // exit(-1);
        }
        if(file_info.option == 1){
            printf("Successfully encrypted file:%s\n",file_info.file_path);
        } else if(file_info.option == 2){
            printf("Successfully decrypted file:%s\n",file_info.file_path);
        }
        pthread_mutex_unlock(&lock);
    }
    close(fd_drive);
    return ((void *)0);
}

int main(void)
{
    int key_len;
    int option;
    char file_path[50];
    char key[50];
	int ret = 0;

    /* 创建与驱动交互的线程 */
    pthread_t file_entryption_thread;
    pthread_create(&file_entryption_thread, NULL, file_entryption, NULL);

	while(1) {
        printf("----------Welcome to use file entryption tool----------\n");
        printf("1. file entryption\n");
        printf("2. file detryption\n");
        printf("Please input your choice,file path,key\n");
        printf("--------------------------------------------------------\n");

        scanf("%d %s %s",&option,file_path,key);

        if(option != 1 && option != 2){
            printf("Please input the correct option!\n");
            continue;
        }

        if(access(file_path,W_OK) == -1){
            printf("Access to file error!\n");
            continue;
        }
        
        key_len = strlen(key);
        if(key_len != 16){
            printf("Please enter a 16-bit key!\n");
            continue;
        }

        pthread_mutex_lock(&lock);
        strcpy(file_info.file_path, file_path);
        strcpy(file_info.key, key);
        file_info.key_len = key_len;
        file_info.option = option;

        data_ready = 1;
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&lock);
	}
	return 0;
}