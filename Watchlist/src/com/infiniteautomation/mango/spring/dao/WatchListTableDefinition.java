/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.dao;

import java.util.List;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.db.AbstractTableDefinition;

/**
 *
 * @author Terry Packer
 */
@Component
public class WatchListTableDefinition extends AbstractTableDefinition {

    public static final String TABLE_NAME = "watchlists";

    public final Field<Integer> USER_ID = DSL.field(DSL.name("userId"), SQLDataType.INTEGER.nullable(false));
    public final Field<String> TYPE = DSL.field(DSL.name("type"), SQLDataType.VARCHAR(20));
    public final Field<String> DATA = DSL.field(DSL.name("data"), SQLDataType.CLOB);

    public final Name POINTS_TABLE_ALIAS = DSL.name("wlp");
    public final Table<? extends Record> POINTS_TABLE_AS_ALIAS = DSL.table("watchListPoints").as(POINTS_TABLE_ALIAS);
    public final Field<Integer> POINTS_DATA_POINT_ID_ALIAS = DSL.field(POINTS_TABLE_ALIAS.append(DSL.name("dataPointId")), SQLDataType.INTEGER.nullable(false));
    public final Field<Integer> POINTS_DATA_POINT_WATCHLIST_ID_ALIAS = DSL.field(POINTS_TABLE_ALIAS.append(DSL.name("watchListId")), SQLDataType.INTEGER.nullable(false));
    public final Field<Integer> POINTS_DATA_POINT_WATCHLIST_SORT_ORDER = DSL.field(POINTS_TABLE_ALIAS.append(DSL.name("sortOrder")), SQLDataType.INTEGER.nullable(false));

    public WatchListTableDefinition() {
        super(DSL.table(TABLE_NAME), DSL.name("wl"));
    }

    @Override
    protected void addFields(List<Field<?>> fields) {
        super.addFields(fields);
        fields.add(USER_ID);
        fields.add(TYPE);
        fields.add(DATA);
    }
}