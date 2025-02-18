package com.demo.attach;

public class MyBizMain {
    public String foo() {
        return "------我是MyBizMain-----";
    }

    public static void main(String[] args) throws InterruptedException {
        MyBizMain myBizMain = new MyBizMain();
        while (true) {
            System.out.println(myBizMain.foo());
            Thread.sleep(1000);
        }
    }
}

