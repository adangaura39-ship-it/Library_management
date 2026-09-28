package com.library.model;

/**
 * Abstract base class for all persisted domain objects.
 * Provides the common identity field and forces subclasses to define
 * how they summarize themselves for console/report output (polymorphism).
 */
public abstract class Entity {

    protected int id;

    protected Entity() {
    }

    protected Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns a short, human-readable one-line summary of this entity.
     * Each subclass overrides this differently -> polymorphism.
     */
    public abstract String getSummary();

    @Override
    public String toString() {
        return getSummary();
    }
}
