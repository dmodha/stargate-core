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

package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.util.CQLUnitD;
import junit.framework.Assert;
import org.junit.Test;


/**
 * User: satya
 */
public class AggregatesTest extends IndexTestBase {
    String keyspace = "dummyksAggr";

    public AggregatesTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        try {
            createKS(keyspace);
            createTableAndIndexForRow();
            countResults("TAG2", "", false, true);
            Assert.assertEquals(12, countResults("TAG2", "magic = '" + q("tags", "tags:hello* AND state:CA") + "'", true));
            Assert.assertEquals(12, countResults("TAG2", "magic = '" + q("tags", "tags:hello* AND state:CA") + "'", true));
            countResults("TAG2", "magic = '" + funWithFilter(fun("state", "state-values", "values", true), "tags", "tags:hello*") + "'", true);
            countResults("TAG2", "magic = '" + funWithFilter(fun("segment", "segment-values", "values", true), "tags", "tags:hello*") + "'", true);
            countResults("TAG2", "magic = '" + funWithFilter(fun("segment", "distinct-segment", "count", true), "tags", "tags:hello* AND state:CA") + "'", true);
            countResults("TAG2", "magic = '" + funWithFilter(fun("segment", "sum-segment", "sum", false), "tags", "tags:hello* AND state:CA") + "'", true);
            countResults("TAG2", "magic = '" + funWithFilter(fun("segment", "min-segment", "min", false), "tags", "tags:hello* AND state:CA") + "'", true);
            countResults("TAG2", "magic = '" + funWithFilter(fun("segment", "max-segment", "max", false), "tags", "tags:hello* AND state:CA") + "'", true);

        } finally {
            dropTable(keyspace, "TAG2");
            dropKS(keyspace);
        }
    }

    private void createTableAndIndexForRow() {
        String options = "{\n" +
                "\t\"numShards\":1024,\n" +
                "\t\"metaColumn\":true,\n" +
                "\t\"fields\":{\n" +
                "\t\t\"tags\":{\"type\":\"text\"},\n" +
                "\t\t\"state\":{}\n" +
                "\t}\n" +
                "}\n";
        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE TAG2(key int, tags varchar, state varchar, segment int, magic text, PRIMARY KEY(key))");
        int i = 0;
        while (i < 40) {
            if (i == 20) {
                getSession().execute("CREATE CUSTOM INDEX tagsandstate ON TAG2(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
            }
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 1) + ",'hello1 tag1 lol1', 'CA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 2) + ",'hello1 tag1 lol2', 'LA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 3) + ",'hello1 tag2 lol1', 'NY'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 4) + ",'hello1 tag2 lol2', 'TX'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 5) + ",'hllo3 tag3 lol3',  'TX'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 6) + ",'hello2 tag1 lol1', 'CA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 7) + ",'hello2 tag1 lol2', 'NY'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 8) + ",'hello2 tag2 lol1', 'CA'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 9) + ",'hello2 tag2 lol2', 'TX'," + i + ")");
            getSession().execute("insert into " + keyspace + ".TAG2 (key,tags,state,segment) values (" + (i + 10) + ",'hllo3 tag3 lol3', 'TX'," + i + ")");
            i = i + 10;
        }
    }
}
