/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution;

import com.hagoapp.datacova.verification.Configuration;
import com.hagoapp.f2t.ColumnDefinition;
import com.hagoapp.f2t.DataCell;
import com.hagoapp.f2t.DataRow;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Validator implements Closeable {

    protected Configuration config;
    protected final Map<String, Integer> fieldIndexer = new HashMap<>();
    protected FieldLoader fieldLoader = row -> {
        var cells = row.getCells();
        return fieldIndexer.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> cells.get(entry.getValue())
        ));
    };
    private boolean initialized = false;

    public interface FieldLoader {
        Map<String, DataCell> loadField(DataRow row);
    }

    public Validator setConfig(Configuration configuration) {
        this.config = configuration;
        init();
        return this;
    }

    public Configuration getConfig() {
        return config;
    }

    public Validator withColumnDefinition(List<ColumnDefinition> columns) {
        for (int i = 0; i < columns.size(); i++) {
            fieldIndexer.put(columns.get(i).getName(), i);
        }
        init();
        return this;
    }

    protected void init() {
        if (initialized) {
            return;
        }
        if ((config == null) || fieldIndexer.isEmpty()) {
            return;
        }
        var keys = new ArrayList<>(fieldIndexer.keySet());
        if (config.isIgnoreFieldCase()) {
            keys.forEach(field -> {
                if (config.getFields().stream().noneMatch(f -> f.compareToIgnoreCase(field) == 0)) {
                    fieldIndexer.remove(field);
                }
            });
        } else {
            keys.forEach(field -> {
                if (!config.getFields().contains(field)) {
                    fieldIndexer.remove(field);
                }
            });
        }
        prepare();
        initialized = true;
    }

    protected abstract void prepare();

    public abstract int getSupportedVerificationType();

    /**
     * Run validation on a row and return failed fields.
     *
     * @param row data row
     * @return A map contains all fields in this row which failed the verification, field name as key, field value as value
     */
    public abstract Map<String, Object> verify(@NotNull DataRow row);

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
