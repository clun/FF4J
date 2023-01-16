package org.ff4j.persistence.inmemory;

import org.ff4j.feature.Flag;
import org.ff4j.property.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * Hold objects for a namespace.
 */
public class NamespaceInMemory {

    /** namespace identifier. */
    private final String name;

    /** features. */
    private final Map<String, Flag> features = new HashMap<>();

    /** properties. */
    private final Map<String, Property<?>> properties = new HashMap<>();

    /**
     * Constructor.
     *
     * @param name
     *      namespace identifier.
     */
    public NamespaceInMemory(String name) {
        this.name = name;
    }

    /**
     * Gets name
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets features
     *
     * @return value of features
     */
    public Map<String, Flag> getFeatures() {
        return features;
    }

    /**
     * Gets properties
     *
     * @return value of properties
     */
    public Map<String, Property<?>> getProperties() {
        return properties;
    }
}
