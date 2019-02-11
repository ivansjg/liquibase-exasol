/**
 * Copyright 2010 Open Pricer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package liquibase.ext.exasol.sqlgenerator;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.DateTimeType;
import liquibase.datatype.core.DateType;
import liquibase.datatype.core.TimeType;
import liquibase.ext.exasol.database.ExasolDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AddDefaultValueGenerator;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;


/**
 * Exasol syntax for adding default value
 *
 */
public class AddDefaultValueGeneratorExasol extends AddDefaultValueGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof ExasolDatabase;
    }

	/**
	 * @see liquibase.sqlgenerator.core.AddDefaultValueGenerator#generateSql(liquibase.statement.core.AddDefaultValueStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
	 */
	@Override
	public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		Object defaultValue = statement.getDefaultValue();
        LiquibaseDataType defaultValueType = DataTypeFactory.getInstance().fromObject(defaultValue, database);
		return new Sql[]{
				new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						+ " ALTER COLUMN  "
                        + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
                        + " SET DEFAULT "
						+ (defaultValueType instanceof DateTimeType ?" TIMESTAMP "+ defaultValueType.objectToSql(defaultValue, database)
							:(defaultValueType instanceof DateType ?" DATE "+ defaultValueType.objectToSql(defaultValue, database)
								//:(defaultValueType instanceof TimeType ?" TO_TIMESTAMP("+ "'"+defaultValueType.objectToSql(defaultValue, database)+"','HH24:MI:SS.FF9')"
								:(defaultValueType instanceof TimeType ? defaultValueType.objectToSql(defaultValue, database)
									: defaultValueType.objectToSql(defaultValue, database)
									)
							)
						  )
						//+ "'"+defaultValueType.objectToSql(defaultValue, database)+"'"
						,new Column().setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())).setName(statement.getColumnName())))
		};
	}
}
