package dev.silverandro.broadsword.mappings;

import dev.silverandro.broadsword.tools.UTF8Container;

public final class CustomMappingsSet extends MappingsSet {
    public void putClass(UTF8Container originalName, UTF8Container newName) {
        classMapping.put(originalName, newName);
    }

    public void putField(UTF8Container parentClass, UTF8Container originalName, UTF8Container desc, UTF8Container newName) {
        fieldMapping.put(new OwnedType(parentClass, originalName, desc), newName);
    }

    public void putMethod(UTF8Container parentClass, UTF8Container originalName, UTF8Container desc, UTF8Container newName) {
        methodMapping.put(new OwnedType(parentClass, originalName, desc), newName);
    }

    public void putModule(UTF8Container originalName, UTF8Container newName) {
        moduleMapping.put(originalName, newName);
    }

    public void putPackage(UTF8Container originalName, UTF8Container newName) {
        packageMapping.put(originalName, newName);
    }
}
