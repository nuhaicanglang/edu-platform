package com.eduplatform.system.util;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * One-off utility: generate a student submission docx for "对象与类作业"
 * Run: java -cp ... com.eduplatform.system.util.GenerateStudentDocx
 */
public class GenerateStudentDocx {

    public static void main(String[] args) throws Exception {
        Path outDir = Paths.get("C:/Users/ASUS1/Desktop/1/可嵌入式跨课程AI Agent通用架构平台/项目参考文件/作业、实验报告批改案例/面向对象程序设计（Java）（作业）");
        Path outFile = outDir.resolve("对象与类作业.docx");

        try (XWPFDocument doc = new XWPFDocument()) {

            // ===== 标题 =====
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("面向对象程序设计（Java）课程作业");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setFontFamily("宋体");

            XWPFParagraph sub = doc.createParagraph();
            sub.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subRun = sub.createRun();
            subRun.setText("第三章 对象与类");
            subRun.setBold(true);
            subRun.setFontSize(14);
            subRun.setFontFamily("宋体");

            // 学生信息
            addParagraph(doc, "姓名：张三    学号：2024010001    班级：计科2401", true, 12);
            addParagraph(doc, "", false, 10);

            // ===== 一、简答题 =====
            addParagraph(doc, "一、简答题", true, 13);
            addParagraph(doc, "", false, 10);

            // 题目1
            addParagraph(doc, "1. 请简述Java中类和对象的关系。（10分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：类是对象的模板，对象是类的实例。类定义了对象的属性和方法，" +
                    "通过new关键字可以创建类的对象。一个类可以创建多个对象，每个对象都有自己的属性值。" +
                    "类是抽象的概念，对象是具体的实体。", false, 11);
            addParagraph(doc, "", false, 10);

            // 题目2
            addParagraph(doc, "2. 什么是构造方法？构造方法有哪些特点？（10分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：构造方法是一种特殊的方法，用于创建对象时初始化对象的状态。构造方法的特点包括：\n" +
                    "（1）构造方法的名称必须与类名相同；\n" +
                    "（2）构造方法没有返回值类型，连void也不能有；\n" +
                    "（3）一个类可以有多个构造方法（构造方法重载）；\n" +
                    "（4）如果没有定义构造方法，系统会自动提供一个默认的无参构造方法。\n" +
                    "（5）构造方法在创建对象时自动调用。", false, 11);
            addParagraph(doc, "", false, 10);

            // 题目3 - 故意答错一部分
            addParagraph(doc, "3. 请解释Java中的封装性，以及如何实现封装。（10分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：封装就是把数据和方法放在一个类里面。实现封装的方法就是把所有的变量都声明为public，" +
                    "这样其他类就可以方便地访问。也可以用private修饰，但是这样太麻烦了，一般用public就行。" +
                    "封装的好处是代码看起来比较整齐。", false, 11);
            addParagraph(doc, "", false, 10);

            // 题目4
            addParagraph(doc, "4. 请说明this关键字的用法。（10分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：this关键字代表当前对象的引用。this的用法有：\n" +
                    "（1）this.属性名：访问当前对象的成员变量，用于区分成员变量和局部变量；\n" +
                    "（2）this.方法名()：调用当前对象的方法；\n" +
                    "（3）this()：在构造方法中调用本类的其他构造方法，但必须放在构造方法的第一行。", false, 11);
            addParagraph(doc, "", false, 10);

            // ===== 二、编程题 =====
            addParagraph(doc, "二、编程题", true, 13);
            addParagraph(doc, "", false, 10);

            // 编程题1
            addParagraph(doc, "1. 设计一个Student类，包含学号(id)、姓名(name)、年龄(age)属性，" +
                    "提供构造方法、getter/setter方法和toString方法。（20分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：", false, 11);

            // 代码 - 故意有一些小问题
            String code1 = "public class Student {\n" +
                    "    private String id;\n" +
                    "    private String name;\n" +
                    "    private int age;\n" +
                    "\n" +
                    "    // 无参构造方法\n" +
                    "    public Student() {\n" +
                    "    }\n" +
                    "\n" +
                    "    // 有参构造方法\n" +
                    "    public Student(String id, String name, int age) {\n" +
                    "        this.id = id;\n" +
                    "        this.name = name;\n" +
                    "        this.age = age;\n" +
                    "    }\n" +
                    "\n" +
                    "    public String getId() { return id; }\n" +
                    "    public void setId(String id) { this.id = id; }\n" +
                    "\n" +
                    "    public String getName() { return name; }\n" +
                    "    public void setName(String name) { this.name = name; }\n" +
                    "\n" +
                    "    public int getAge() { return age; }\n" +
                    "    public void setAge(int age) { this.age = age; }\n" +
                    "\n" +
                    "    // 缺少age的合法性校验\n" +
                    "    public String toString() {\n" +
                    "        return \"Student{id=\" + id + \", name=\" + name + \", age=\" + age + \"}\";\n" +
                    "    }\n" +
                    "}";
            addCodeBlock(doc, code1);
            addParagraph(doc, "", false, 10);

            // 编程题2 - 故意有错误
            addParagraph(doc, "2. 设计一个BankAccount类，包含账户名(owner)和余额(balance)属性，" +
                    "提供存款(deposit)和取款(withdraw)方法，取款时余额不足应给出提示。（20分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：", false, 11);

            String code2 = "public class BankAccount {\n" +
                    "    String owner;  // 没有使用private修饰\n" +
                    "    double balance;\n" +
                    "\n" +
                    "    public BankAccount(String owner) {\n" +
                    "        this.owner = owner;\n" +
                    "        this.balance = 0;\n" +
                    "    }\n" +
                    "\n" +
                    "    public void deposit(double amount) {\n" +
                    "        balance = balance + amount;\n" +
                    "        // 缺少对amount为负数的检查\n" +
                    "    }\n" +
                    "\n" +
                    "    public void withdraw(double amount) {\n" +
                    "        if (balance >= amount) {\n" +
                    "            balance = balance - amount;\n" +
                    "        } else {\n" +
                    "            System.out.println(\"余额不足\");\n" +
                    "        }\n" +
                    "        // 没有对amount为负数的检查\n" +
                    "    }\n" +
                    "\n" +
                    "    public double getBalance() {\n" +
                    "        return balance;\n" +
                    "    }\n" +
                    "}";
            addCodeBlock(doc, code2);
            addParagraph(doc, "", false, 10);

            // 编程题3
            addParagraph(doc, "3. 编写一个测试类，创建上述Student和BankAccount对象并测试其功能。（20分）", true, 11);
            addParagraph(doc, "", false, 10);
            addParagraph(doc, "答：", false, 11);

            String code3 = "public class Test {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        // 测试Student类\n" +
                    "        Student s1 = new Student(\"2024001\", \"李四\", 20);\n" +
                    "        System.out.println(s1.toString());\n" +
                    "        System.out.println(\"姓名：\" + s1.getName());\n" +
                    "\n" +
                    "        // 测试BankAccount类\n" +
                    "        BankAccount account = new BankAccount(\"王五\");\n" +
                    "        account.deposit(1000);\n" +
                    "        System.out.println(\"存款后余额：\" + account.getBalance());\n" +
                    "        account.withdraw(500);\n" +
                    "        System.out.println(\"取款后余额：\" + account.getBalance());\n" +
                    "        account.withdraw(600);  // 测试余额不足\n" +
                    "    }\n" +
                    "}";
            addCodeBlock(doc, code3);

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(outFile.toFile())) {
                doc.write(fos);
            }
            System.out.println("Generated: " + outFile);
        }
    }

    private static void addParagraph(XWPFDocument doc, String text, boolean bold, int fontSize) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
        run.setFontFamily("宋体");
    }

    private static void addCodeBlock(XWPFDocument doc, String code) {
        for (String line : code.split("\n")) {
            XWPFParagraph p = doc.createParagraph();
            p.setIndentationLeft(720); // 0.5 inch indent
            XWPFRun run = p.createRun();
            run.setText(line);
            run.setFontSize(10);
            run.setFontFamily("Consolas");
        }
    }
}
