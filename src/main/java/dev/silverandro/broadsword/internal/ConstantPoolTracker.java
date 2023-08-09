/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;

/**
 * A class that allows for tracking all the necessary information for remapping a constant pool.
 * The actual data is abstracted over with some getters and setters, but its still pretty close to minimal.
 *
 * @implNote This class is very fragile internally! For speed everything is stored int->int but that means you can put
 * the contextually wrong int into the map very easily, which can cause hard to debug issues.
 */
public final class ConstantPoolTracker {
    private final Int2IntAVLTreeMap indexToRemapType;
    private final Int2IntAVLTreeMap classToUTF8;
    private final Int2IntAVLTreeMap nameToNt;
    private final Int2IntAVLTreeMap ntToOwnerClass;
    private final Int2IntAVLTreeMap nameToDesc;

    public ConstantPoolTracker() {
        indexToRemapType = new Int2IntAVLTreeMap();
        classToUTF8 = new Int2IntAVLTreeMap();
        nameToNt = new Int2IntAVLTreeMap();
        ntToOwnerClass = new Int2IntAVLTreeMap();
        nameToDesc = new Int2IntAVLTreeMap();
    }

    //
    // Put Operations
    //
    public void putClass(int index, int contentIndex) {
        indexToRemapType.put(contentIndex, RemapType.CLASS);
        classToUTF8.put(index, contentIndex);
    }

    public void putSelfFieldName(int index, int descIndex) {
        indexToRemapType.put(index, RemapType.SELF_FIELD_NAME);
        nameToDesc.put(index, descIndex);
    }

    public void putSelfMethodName(int index, int descIndex) {
        indexToRemapType.put(index, RemapType.SELF_METHOD_NAME);
        nameToDesc.put(index, descIndex);
    }

    public void putMethodName(int index, boolean isSelfMethod) {
        var rType = RemapType.METHOD_NAME;
        if (isSelfMethod) rType = RemapType.SELF_METHOD_NAME;
        indexToRemapType.put(index, RemapType.METHOD_NAME);
    }

    public void putDescriptor(int index) {
        indexToRemapType.put(index, RemapType.DESCRIPTOR);
    }

    public void putModule(int index) {
        indexToRemapType.put(index, RemapType.MODULE);
    }

    public void putPackage(int index) {
        indexToRemapType.put(index, RemapType.PACKAGE);
    }

    public void putNtName(int index, int nameIndex) {
        nameToNt.put(nameIndex, index);
    }

    public void putNtClass(int ntIndex, int classIndex) {
        ntToOwnerClass.put(ntIndex, classIndex);
    }

    //
    // Get Operations
    //
    public int getClassContent(int index) {
        return classToUTF8.get(index);
    }

    public int getRemapType(int index) {
        return indexToRemapType.get(index);
    }

    public int getDescIndex(int nameIndex) {
        return nameToDesc.get(nameIndex);
    }

    public int getNameOwner(int nameIndex) {
        return ntToOwnerClass.get(nameToNt.get(nameIndex));
    }
}
