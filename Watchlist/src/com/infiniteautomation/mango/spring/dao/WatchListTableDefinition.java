/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.spring.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.db.AbstractTableDefinition;
import com.infiniteautomation.mango.spring.db.UserTableDefinition;

/**
 *
 * @author Terry Packer
 */
@Component
public class WatchListTableDefinition extends AbstractTableDefinition {

    public static final String TABLE_NAME = "watchLists";
    public static final Table<Record> TABLE = DSL.table(TABLE_NAME);
    public static final Field<Integer> ID = DSL.field(TABLE.getQualifiedName().append("id"), SQLDataType.INTEGER.nullable(false));
    public static final Field<Integer> READ_PERMISSION = DSL.field(TABLE.getQualifiedName().append("readPermissionId"), SQLDataType.INTEGER.nullable(false));
    public static final Field<Integer> EDIT_PERMISSION = DSL.field(TABLE.getQualifiedName().append("editPermissionId"), SQLDataType.INTEGER.nullable(false));

    public static final Field<Integer> READ_PERMISSION_ALIAS = DSL.field( DSL.name("wl").append("readPermissionId"), SQLDataType.INTEGER.nullable(false));
    public static final Field<Integer> EDIT_PERMISSION_ALIAS = DSL.field( DSL.name("wl").append("editPermissionId"), SQLDataType.INTEGER.nullable(false));

    public final Field<Integer> USER_ID = DSL.field(DSL.name("userId"), SQLDataType.INTEGER.nullable(false));
    public final Field<String> TYPE = DSL.field(DSL.name("type"), SQLDataType.VARCHAR(20));
    public final Field<String> DATA = DSL.field(DSL.name("data"), SQLDataType.CLOB);

    public final Name POINTS_TABLE_ALIAS = DSL.name("wlp");
    public final Table<? extends Record> POINTS_TABLE_AS_ALIAS = DSL.table("watchListPoints").as(POINTS_TABLE_ALIAS);
    public final Field<Integer> POINTS_DATA_POINT_ID_ALIAS = DSL.field(POINTS_TABLE_ALIAS.append(DSL.name("dataPointId")), SQLDataType.INTEGER.nullable(false));
    public final Field<Integer> POINTS_DATA_POINT_WATCHLIST_ID_ALIAS = DSL.field(POINTS_TABLE_ALIAS.append(DSL.name("watchListId")), SQLDataType.INTEGER.nullable(false));
    public final Field<Integer> POINTS_DATA_POINT_WATCHLIST_SORT_ORDER = DSL.field(POINTS_TABLE_ALIAS.append(DSL.name("sortOrder")), SQLDataType.INTEGER.nullable(false));

    private final UserTableDefinition userTable;

    @Autowired
    public WatchListTableDefinition(UserTableDefinition userTable) {
        super(DSL.table(TABLE_NAME), DSL.name("wl"));
        this.userTable = userTable;
    }

    @Override
    protected void addFields(List<Field<?>> fields) {
        super.addFields(fields);
        fields.add(USER_ID);
        fields.add(TYPE);
        fields.add(DATA);
        fields.add(READ_PERMISSION);
        fields.add(EDIT_PERMISSION);
    }

    @Override
    protected Map<String, Field<?>> getAliasMappings() {
        Map<String, Field<?>> map = new HashMap<>();
        map.put("username", userTable.getXidAlias());
        return map;
    }
}
