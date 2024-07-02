package dev.silverandro.broadsword.bench;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class Verify {
    public static void main(String[] args) throws IOException {
        var f = new File("run/testoutputs/Dummy.class");

        var inputStream = new FileInputStream(f);

        new ClassReader(inputStream).accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    }
}
