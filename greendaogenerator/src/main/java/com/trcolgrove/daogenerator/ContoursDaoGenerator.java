package com.trcolgrove.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class ContoursDaoGenerator {

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.trcolgrove.daoentries");
        addStoredSet(schema);
        new DaoGenerator().generateAll(schema, "../app/src-gen");
    }

    private static void addStoredSet(Schema schema) {
        Entity storedSet = schema.addEntity("StoredSet");
        storedSet.addIdProperty();
        storedSet.addBooleanProperty("uploaded").notNull();
        storedSet.addStringProperty("scoreSetJson").notNull();
    }
}