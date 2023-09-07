/*
 * Copyright 2023 SilverAndro. All Rights Reserved
 */

package dev.silverandro.broadsword.internal;

/**
 * A class that allows for tracking all the necessary information for remapping a constant pool.
 * The actual data is abstracted over with some getters and setters, but its still pretty close to minimal.
 *
 * @implNote This class is very fragile internally! For speed everything is stored int->int but that means you can put
 * the contextually wrong int into the map very easily, which can cause hard to debug issues.
 */
public final class ConstantPoolTracker {
    private final byte[] indexToRemapType;
    // Works as both Name->NT and NT->OwnerClass
    private final short[] ntData;
    // Works as both Class->UTF8Content and Name->Desc
    private final short[] contentMappings;

    public ConstantPoolTracker(int upperBound) {
        indexToRemapType = new byte[upperBound];
        ntData = new short[upperBound];
        contentMappings = new short[upperBound];
    }

    //
    // Put Operations
    //
    public void putClass(int index, int contentIndex) {
        indexToRemapType[contentIndex] = RemapType.CLASS;
        contentMappings[index] = (short) contentIndex;
    }

    public void putNameToDesc(int nameIndex, int descIndex) {
        contentMappings[nameIndex] = (short) descIndex;
        putDescriptor(descIndex);
    }

    public void putSelfFieldName(int index, int descIndex) {
        indexToRemapType[index] = RemapType.SELF_FIELD_NAME;
        contentMappings[index] = (short) descIndex;
    }

    public void putSelfMethodName(int index, int descIndex) {
        indexToRemapType[index] = RemapType.SELF_METHOD_NAME;
        contentMappings[index] = (short) descIndex;
    }

    public void putFieldNT(int index) {
        indexToRemapType[index] = RemapType.FIELD_NT;
    }

    public void putMethodNT(int index) {
        indexToRemapType[index] = RemapType.METHOD_NT;
    }

    public void putDescriptor(int index) {
        indexToRemapType[index] = RemapType.DESCRIPTOR;
    }

    public void putModule(int index) {
        indexToRemapType[index] = RemapType.MODULE;
    }

    public void putPackage(int index) {
        indexToRemapType[index] = RemapType.PACKAGE;
    }

    public void putNtName(int index, int nameIndex) {
        indexToRemapType[nameIndex] = RemapType.NAME_NT;
        ntData[nameIndex] = (short) index;
    }

    public void putNtClass(int ntIndex, int classIndex) {
        ntData[ntIndex] = (short) classIndex;
    }

    //
    // Get Operations
    //
    public int getClassContent(int index) {
        return contentMappings[index];
    }

    public int getRemapType(int index) {
        return indexToRemapType[index];
    }

    public int getDescIndex(int nameIndex) {
        return contentMappings[nameIndex];
    }

    public int getNameNT(int nameIndex) {
        return ntData[nameIndex];
    }

    public int getNameOwner(int nameIndex) {
        return ntData[getNameNT(nameIndex)];
    }
}
