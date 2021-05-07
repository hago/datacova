/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution;

import com.hagoapp.datacova.entity.action.verification.Configuration;
import com.hagoapp.f2t.ColumnDefinition;
import com.hagoapp.f2t.DataCell;
import com.hagoapp.f2t.DataRow;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Validator {

    protected Configuration config;
    protected final Map<String, Integer> fieldIndexer = new HashMap<>();
    protected FieldLoader fieldLoader = row -> {
        var cells = row.getCells();
        return fieldIndexer.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> cells.get(entry.getValue())
        ));
    };

    public interface FieldLoader {
        Map<String, DataCell> loadField(DataRow row);
    }

    public Validator withConfig(Configuration configuration) {
        this.config = configuration;
        init();
        return this;
    }

    public Validator withColumnDefinition(List<ColumnDefinition> columns) {
        for (int i = 0; i < columns.size(); i++) {
            fieldIndexer.put(columns.get(i).getName(), i);
        }
        init();
        return this;
    }

    protected void init() {
        if ((config == null) || fieldIndexer.isEmpty()) {
            return;
        }
        if (config.isIgnoreFieldCase()) {
            fieldIndexer.keySet().forEach(field -> {
                if (config.getFields().stream().noneMatch(f -> f.compareToIgnoreCase(field) == 0)) {
                    fieldIndexer.remove(field);
                }
            });
        } else {
            fieldIndexer.keySet().forEach(field -> {
                if (!config.getFields().contains(field)) {
                    fieldIndexer.remove(field);
                }
            });
        }
    }

    public abstract int getSupportedVerificationType();

    /**
     * Run validation on a row and return failed fields.
     *
     * @param row data row
     * @return The list of fields which failed.
     */
    public abstract List<String> verify(@NotNull DataRow row);

    public abstract String getAbstract();
}
