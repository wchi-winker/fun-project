// 包含对称密码（symmetric key）算法API（该文件内部包含了linux/crypto.h所以无需再次引入linux/crypto.h）
#include <crypto/skcipher.h> 
// 对称密码API需要使用的struct scatterlist结构（用来保存输入/输出缓冲区）
#include <linux/scatterlist.h>
#include <linux/fs.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/types.h>
#include <linux/kernel.h>
#include <linux/errno.h>
#include <linux/fs.h>
#include <linux/i2c.h>
#include <linux/ioctl.h>
#include <linux/types.h>
#include <linux/stat.h>
#include <linux/fcntl.h>
#include <linux/unistd.h>
#include <asm/ioctl.h>
#include <linux/ioctl.h>
#include <linux/mman.h>
#include <linux/fb.h>
#include <linux/mm.h>

#define BLOCK_SIZE 16
#define AES_IV_SIZE  16

#define DEMO_NAME "file_entryption"

typedef struct mdata{
	int dev_major;
	short data;
} data_t;

struct demo_dev{
    int major;
    int minor;
    int data;
    void* private;
    char* name;
	struct device* dev;
	struct class* cls;
};

struct demo_dev* ddev;

typedef struct _FILE_INFO_
{
    int key_len;
    int option;
    char file_path[50];
    char key[50];
} FILE_INFO;


#define FILE_ENTRY _IOW('d', 1, struct _FILE_INFO_)
#define FILE_DETRY _IOW('d', 2, struct _FILE_INFO_)

// 文件加密
int linux_kernel_crypto_encrypt(void* data_in_out, int data_len, void* key, int key_len, void* iv, int iv_len) {
	struct crypto_skcipher* cipher;
	struct skcipher_request* req;
    struct crypto_wait wait;
	struct scatterlist sg;
	int ret;
 
    // 该函数根据密码算法名称分配密码算法对象，内核支持的密码算法可以在/proc/crypto文件中查看
	cipher = crypto_alloc_skcipher("cbc(aes)", 0, 0);
	if (IS_ERR(cipher)) {
		printk("fail to allocate cipher\n");
		return -1;
	}

    // 分配req对象
	req = skcipher_request_alloc(cipher, GFP_KERNEL);
	if (IS_ERR(req)) {
		printk("fail to allocate req\n");
		return -1;
	}
    // 初始化scatterlist时需要使用kmalloc分配的内存，如果使用vmalloc分配的内存会导致内存页分配错误，目前还不知道具体原因
	sg_init_one(&sg, data_in_out, data_len);

    // 设置异步调用的回调函数，这里wait是一个自定数据结构，其会被传给回调函数。
	skcipher_request_set_callback(req, CRYPTO_TFM_REQ_MAY_BACKLOG, crypto_req_done, &wait);

    // 内核的对称加密API可以“原地”加密，即加解密共用相同的缓冲区，因此这里的src和dst可以设置为同一个
	skcipher_request_set_crypt(req, &sg, &sg, data_len, iv);

    // 设置密钥和密钥长度，密码长度单位为字节
	ret = crypto_skcipher_setkey(cipher, key, key_len);
    if ( 0 != ret) {
        printk("fail to set key, error %d\n", ret);
        return -1;
    }

    // 执行加密操作
	ret = crypto_wait_req(crypto_skcipher_encrypt(req), &wait); 
	// if (0 != ret) {
    //     printk("encryption error %d\n", ret);
    //     return -1;
	// }

	// 释放资源
	crypto_free_skcipher(cipher);
	skcipher_request_free(req);
	printk("encryption finished");

	return 0;
}

// 文件解密
int linux_kernel_crypto_decrypt(void* data_in_out, int data_len, void* key, int key_len, void* iv, int iv_len) {
	struct crypto_skcipher* cipher;
	struct skcipher_request* req;
    struct crypto_wait wait;
	struct scatterlist sg;
    size_t block_size;
	int ret;
 
    // 该函数根据密码算法名称分配密码算法对象，内核支持的密码算法可以在/proc/crypto文件中查看
	cipher = crypto_alloc_skcipher("cbc(aes)", 0, 0);
	if (IS_ERR(cipher)) {
		printk("fail to allocate cipher\n");
		return -1;
	}
    // 分配req对象
	req = skcipher_request_alloc(cipher, GFP_KERNEL);
	if (IS_ERR(req)) {
		printk("fail to allocate req\n");
		return -1;
	}
    // 初始化scatterlist时需要使用kmalloc分配的内存，如果使用vmalloc分配的内存会导致内存页分配错误，目前还不知道具体原因
	sg_init_one(&sg, data_in_out, data_len);

    // 设置异步调用的回调函数，这里wait是一个自定数据结构，其会被传给回调函数。
	skcipher_request_set_callback(req, CRYPTO_TFM_REQ_MAY_BACKLOG, crypto_req_done, &wait);

    // 内核的对称加密API可以“原地”加密，即加解密共用相同的缓冲区，因此这里的src和dst可以设置为同一个
	skcipher_request_set_crypt(req, &sg, &sg, data_len, iv);

    // 设置密钥和密钥长度，密码长度单位为字节
	ret = crypto_skcipher_setkey(cipher, key, key_len);
    if ( 0 != ret) {
        printk("fail to set key, error %d\n", ret);
        return -1;
    }

    // 执行解密操作
	ret = crypto_wait_req(crypto_skcipher_decrypt(req), &wait); 
	// if (0 != ret) {
    //     printk("decryption error %d\n", ret);
    //     return -1;
	// }

	// 释放资源
	crypto_free_skcipher(cipher);
	skcipher_request_free(req);
	printk("encryption finished");
	return 0;
}


static long int encrypt_ioctl(struct file* file, unsigned int cmd, unsigned long int arg) {
    
    struct file *filp = NULL;
    u8 iv[AES_IV_SIZE];
    int ret = 0, buflen = 0;
    char *buf = NULL;
    loff_t pos = 0;
    loff_t pos2 = 0;
    FILE_INFO my_file;

    copy_from_user(&my_file, (FILE_INFO *)arg, sizeof(FILE_INFO));

    /* Open the file */
    printk(KERN_ALERT "file path:%s\n",my_file.file_path);
    filp = filp_open(my_file.file_path,  O_RDWR, 0644);
    if (IS_ERR(filp)) {
        printk(KERN_ERR "Failed to open file %s\n", my_file.file_path);
        return PTR_ERR(filp);
    }

    buflen = i_size_read(filp->f_inode);
    printk(KERN_ALERT "buflen after:%d\n",buflen);

    buf = kmalloc(buflen, GFP_KERNEL);
    if (!buf) {
        printk(KERN_ERR "Failed to allocate buffer for file encryption\n");
        return -ENOMEM;
    }

    kernel_read(filp, buf, buflen, &pos);

    switch(cmd) {
        case FILE_ENTRY:
            ret = linux_kernel_crypto_encrypt(buf, buflen, my_file.key, my_file.key_len, iv, AES_IV_SIZE);
            break;
        case FILE_DETRY:
            ret = linux_kernel_crypto_decrypt(buf, buflen, my_file.key, my_file.key_len, iv, AES_IV_SIZE);
            break;
        default:
            break;
    }

    kernel_write(filp, buf, buflen, &pos2);
out:
    filp_close(filp, NULL);
    return ret;
}

struct file_operations  ddev_fops  = {
	.unlocked_ioctl = encrypt_ioctl
};

static int __init file_entryption_init(void) {
    printk("<-KERNEL-> file entryption module init! \n");

    ddev = kzalloc(sizeof(*ddev), GFP_KERNEL);
    if(!ddev)
        return -ENOMEM;

    ddev->name = DEMO_NAME;
    ddev->major = register_chrdev(0, ddev->name, &ddev_fops);

	ddev->cls = class_create(THIS_MODULE, DEMO_NAME);
	if(IS_ERR(&ddev->cls)){
		pr_err("file_entryption: device class create failed\n");
		goto err_class_create;
	}

	ddev->dev = device_create(ddev->cls, NULL, MKDEV(ddev->major, 0), NULL, DEMO_NAME);
	if(IS_ERR(&ddev->dev)){
		pr_err("file_entryption: create device failed@@\n");
		goto err_device_create;
	}

    return 0;

err_device_create:
	class_destroy(ddev->cls);
err_class_create:
	unregister_chrdev(ddev->major, DEMO_NAME);
	kfree(ddev);
	return -EFAULT;
}

static void __exit file_entryption_exit(void) {
    printk("<-KERNEL-> file entryption module exit! \n");
	
    device_destroy(ddev->cls, MKDEV(ddev->major, 0));
	class_destroy(ddev->cls);
	unregister_chrdev(ddev->major, DEMO_NAME);
	kfree(ddev);
}

module_init(file_entryption_init);
module_exit(file_entryption_exit);
MODULE_LICENSE("GPL");
MODULE_AUTHOR("Your");