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
import com.tuplejump.stargate.cassandra.CustomColumnFactory;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.Row;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.List;

/**
 * User: satya
 */
public class Values extends Aggregate {

    @JsonCreator
    public Values(@JsonProperty("field") String field, @JsonProperty("name") String name, @JsonProperty("distinct") boolean distinct, @JsonProperty("groupBy") String groupBy) {
        super(field, name, distinct, groupBy);
    }

    public String getFunction() {
        return "values";
    }


    @Override
    public List<Row> process(List<Row> rows, CustomColumnFactory customColumnFactory, ColumnFamilyStore table, RowIndex currentIndex) throws Exception {
        Collection<Object> values = values(rows, table);
        return singleRow("[" + StringUtils.join(values, ',') + "]", customColumnFactory, table, currentIndex);
    }
}
