ifneq ($(KERNELRELEASE),)
	obj-m :=fileEncryption.o
else
	KDIR :=/lib/modules/$(shell uname -r)/build
	PWD  := $(shell pwd)
all:
	make -C $(KDIR) M=$(PWD) modules
clean:
	rm -f *.ko *.o *.mod.o *.mod.c *.symvers *.order
endif