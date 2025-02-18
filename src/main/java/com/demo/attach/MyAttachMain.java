package com.demo.attach;

import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

/**
 * 运行时attach pid 进程  加载agent 实现类似arthas 的功能
 */
public class MyAttachMain {
    public static void main(String[] args) {
        VirtualMachine vm = null;  // tools.jar里的类  需手工引用
        try {
            vm = VirtualMachine.attach("3188");//MyBizMain进程ID
            vm.loadAgent("");//java agent jar包路径
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
