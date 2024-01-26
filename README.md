# Broadsword

A java class file remapper with a focus on speed by working directly with class bytes rather than multiple abstraction
layers such as with ow2-asm.

This library was primarily created for my own projects, with implicit requirements. As such, there is some data that may
be ignored or malformed, as it is not relevant to my work.
### How does it work
Rather than using ASM to read the class file, and then using a visitor to visit every possible node to remap
it if necessary, broadsword instead works directly on the constant pool. Since everything in a java class file
indexes into the constant pool, this allows it to work on less data, faster.

Two passes are performed for mapping, the first one reads the constant pool and field/method tables, and builds a map of what
entries are referenced by each type. This way, we have a list of which `CONSTANT_Utf8_info` structures
should be changed and which should be left alone.

We then rewrite the constant pool in place before emitting the rest of the class. When loaded, the JVM resolves all 
references into the pool, modifying all usages of that constant.

### Disadvantages of this approach

Since we directly modify the class pool without breaking down the class, we have no context about the use of each
constant pool entry, and cannot separate them. This means if a constant pool entry is reused, broadsword will
remap *both* cases, even if only one is the actual usage that should be remapped. This could cause edgcases
to pop up, especially if combined with other tooling that expects the unmapped form.

For example, if you have the program

```java
public class Test {
    public static void main(String[] args) {
        System.out.println("main");
    }
}
```

The `CONSTANT_Utf8_info` for `main` gets reused for both the code and the method name, so if it was then remapped, both 
cases would be modified. This should really only meaningfully differ when a class is doing reflection on itself. Or has
a method of the same name.

### Example usage
```java
public class RemapClassFile {
    public static void remap() {
        var mapping = new EnigmaMappings();
        mapping.parseFromDirectory(Path.of("mappingsDir"));

        var open = new File("testclass.class");
        var inputStream = new FileInputStream(open);
        var bytes = inputStream.readAllBytes();
        inputStream.close();
        
        var resultingBytes = ClassFileRemapper.remapClassBytes(bytes, mapping,
                className -> {
                    // Look up the class info, possibly using ClassStructExtractor
                    return new ClassMappingStruct(List.of(), Map.of());
                }
        );
        
        var output = new File("remappedClass.class");
        output.getParentFile().mkdirs();
        output.createNewFile();
        var outputStream = new FileOutputStream(output);
        outputStream.write(resultingBytes);
        outputStream.close();
    }
}
```