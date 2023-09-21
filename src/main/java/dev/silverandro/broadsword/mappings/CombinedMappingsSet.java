package dev.silverandro.broadsword.mappings;

public class CombinedMappingsSet extends MappingsSet {
    private boolean supportsFieldTypes = true;

    @Override
    protected boolean supportsFieldTypes() {
        return supportsFieldTypes;
    }

    public void fromMappings(MappingsSet setA, String setAFrom, String setATo, MappingsSet setB, String setBFrom, String setBTo) {
        supportsFieldTypes = setA.supportsFieldTypes();
        if (setAFrom.equals(setBFrom)) {
            setA.classMapping.forEach((s, s2) -> {
                classMapping.put(s2, setB.remapClass(s));
            });
            setA.fieldMapping.forEach((s, s2) -> {
                var decomposed = decomposeEntry(s);
                if (!setA.supportsFieldTypes()) {
                    var simpleKey = decomposed[0] + "!" + decomposed[1];
                    var found = setB.fieldMapping.keySet()
                            .stream()
                            .filter(s1 -> s1.startsWith(simpleKey))
                            .findFirst().orElse(s);
                    var decomposedFound = decomposeEntry(found);
                    fieldMapping.put(
                            setA.remapClass(decomposed[0]) + "!" + s2 + "!" + setA.remapDescriptor(decomposedFound[2]),
                            setB.remapField(decomposed[0], decomposed[1], decomposedFound[2])
                    );
                } else {
                    fieldMapping.put(
                            decomposed[0] + "!" + decomposed[1] + "!" + decomposed[2],
                            setB.remapField(decomposed[0], decomposed[1], decomposed[2])
                    );
                }
            });
            setA.methodMapping.forEach((s, s2) -> {
                var decomposed = decomposeEntry(s);
                methodMapping.put(setA.remapClass(decomposed[0]) + "!" + s2 + "!" + setA.remapDescriptor(decomposed[2]), setB.remapMethod(decomposed[0], decomposed[1], decomposed[2]));
            });
        } else {
            throw new IllegalArgumentException("Dont know how to combine mapping sets of (" + setAFrom + " -> " + setATo + ") and " +
                    "(" + setBFrom + " -> " + setBTo + ")");
        }
    }

    private String[] decomposeEntry(String entry) {
        return entry.split("!");
    }
}
