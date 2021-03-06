/*
 * Copyright 2014, Tuplejump Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tuplejump.stargate.lucene.query.function;

import com.tuplejump.stargate.RowIndex;
import com.tuplejump.stargate.Utils;
import com.tuplejump.stargate.cassandra.CustomColumnFactory;
import org.apache.cassandra.cql3.CQL3Type;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.CompositeType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.codehaus.jackson.annotate.JsonProperty;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: satya
 */

public abstract class Aggregate implements Function {

    protected String field;
    protected String alias;
    protected String groupBy;
    protected boolean distinct = false;

    public Aggregate(@JsonProperty("field") String field, @JsonProperty("alias") String alias, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String groupBy) {
        this.field = field;
        this.alias = alias == null ? getFunction() : alias;
        this.distinct = distinct;
        this.groupBy = groupBy;
    }

    public abstract String getFunction();

    public Collection<Object> values(List<Row> rows, ColumnFamilyStore table) throws Exception {
        CompositeType baseComparator = (CompositeType) table.getComparator();
        if (rows.size() > 0) {
            Collection<Object> results;
            if (distinct) {
                results = new TreeSet<>();
            } else {
                results = new ArrayList<>();
            }
            for (Row row : rows) {
                ColumnFamily cf = row.cf;
                Collection<Column> cols = cf.getSortedColumns();
                for (Column column : cols) {
                    String actualColumnName = Utils.getColumnNameStr(baseComparator, column.name());
                    if (field.equalsIgnoreCase(actualColumnName)) {
                        AbstractType<?> valueValidator = table.metadata.getValueValidatorFromColumnName(column.name());

                        results.add(valueValidator.compose(column.value()));
                    }
                }

            }
            return results;
        } else {
            return Collections.emptyList();
        }
    }


    protected boolean isNumber(CQL3Type cqlType) {
        if (cqlType == CQL3Type.Native.INT || cqlType == CQL3Type.Native.VARINT || cqlType == CQL3Type.Native.BIGINT ||
                cqlType == CQL3Type.Native.COUNTER || cqlType == CQL3Type.Native.DECIMAL
                || cqlType == CQL3Type.Native.DOUBLE || cqlType == CQL3Type.Native.FLOAT)
            return true;
        return false;
    }

    protected List<Row> singleRow(String valueStr, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) {
        ByteBuffer value = UTF8Type.instance.decompose("{'" + alias + "':" + valueStr + "}");
        Row row = customColumnFactory.getRowWithMetaColumn(table, currentIndex, value);
        return Collections.singletonList(row);

    }
}
