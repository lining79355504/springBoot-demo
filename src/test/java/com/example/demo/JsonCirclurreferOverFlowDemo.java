package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import java.util.Arrays;

/**
 * @author mort
 * @Description
 * @date 2022/6/22
 **/
public class JsonCirclurreferOverFlowDemo {
    public static void main(String[] args) {

        A a = new A();
        B b = new B();
        a.setB(b);
        b.setA(a);
        String s = new Gson().toJson(a);
        System.out.println(new Gson().toJson(a));
        System.out.println(JSON.toJSON(a));
    }

    public static class A {

        private static Integer id = 6 ;

        private B b;

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }


    }

    public static class B {

        private A a;

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }
    }
}
