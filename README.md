# Broadsword

A java class file remapper with a focus on speed, by dropping frivolous features like "parameter names", "line mappings",
 "usable signatures", and "comments".

This library was primarily created for my projects that require remapping only at runtime or for other non-linking work,
as such anything not critical for the class file to actually *run* in some namespace isn't processed or remapped.

This library definitely has lots of edgecases, but for standard, javac-d code, it shouldnt be an issue.

### How does it work
Rather than using ASM to read the class file, and then using a visitor to visit every possible node to remap
it if necessary, broadsword instead works directly on the constant pool. Since everything in a java class file
indexes into the constant pool, this allows it to work on less data, faster.

Two passes are performed for mapping, the first one reads the constant pool and field/method tables, and builds a map of what
entries are referenced by each type. This way, we have a list of which `CONSTANT_Utf8_info` structures
should be changed and which should be left alone.

### Disadvantages of this approach

Since we directly modify the class pool without breaking down the class, we have no context about the use of each
constant pool entry, and cannot separate them. This means if a constant pool entry is reused, broadsword will
remap *both* cases, even if only one is the actual usage that should be remapped. This could cause edgcases
to pop up, especially if combined with other tooling that expects the unmapped form.

This allows preserving the original intent of the program in more cases, although there still may be edgecases where
this method doesnt work.

### Future work

Eventually I plan to create a method that works directly on a `(ByteArray)InputStream`, so that you can inject the
remapper directly into any file reading pipeline.

Additionally, work can be done to mitigate the disadvantages section. If we allow injecting new entries into the
constant pool (and redirecting old "wrapper" usages), we could remap all *types* of a usage, such as all class references, all strings, or all method names.
This wouldn't completely solve issues with this approach, but it would fix some more of the edgecases.

This split in behavior causes some interesting concerns about code reuse. I would rather not copy paste the remapping
code ~4 times, but adding an abstraction layer could seriously injure the ability of the JIT to compile everything.
I plan to investigate in the future &| talk with some people much more experienced with the JIT.

### Example usage
```java
public class RemapClassFile {
    public static void remap() {
        var mapping = new EnigmaMappings();
        mapping.parseFromDirectory(Path.of("mappingsDir"));
        ClassFileRemapper.registerMappings("intermediary", "named", mapping);

        var open = new File("testclass.class");
        var inputStream = new FileInputStream(open);
        var resultingBytes = ClassFileRemapper.remapClassBytes(inputStream.readAllBytes(), "intermediary", "named");
        inputStream.close();

        var output = new File("remappedClass.class");
        output.getParentFile().mkdirs();
        output.createNewFile();
        var outputStream = new FileOutputStream(output);
        outputStream.write(resultingBytes);
        outputStream.close();
    }
}
```