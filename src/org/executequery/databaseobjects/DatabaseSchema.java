/*
 * DatabaseSchema.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.databaseobjects;

import java.util.List;
import org.underworldlabs.jdbc.DataSourceException;

/**
 * Defines a database catalog object.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public interface DatabaseSchema extends DatabaseSource {
    
    /**
     * Returns the parent catalog object.
     *
     * @return the parent object
     */
    DatabaseCatalog getCatalog();

    /**
     * Returns all available objects within this schema.
     *
     * @return the schema objects
     */
    List<SimpleDatabaseObject> getSchemaObjects() throws DataSourceException;
    
}











