/*
 * DefaultDatabaseExecutable.java
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

package org.executequery.databaseobjects.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import biz.redsoft.IDatabaseMetadata;
import org.executequery.databaseobjects.DatabaseExecutable;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author takisd
 */
public class DefaultDatabaseExecutable extends AbstractDatabaseObject 
                                       implements DatabaseExecutable {
    
    /** the meta tag parent object */
    private DatabaseMetaTag metaTagParent;

    /** proc parameters */
    private List<ProcedureParameter> parameters;

    /** the proc type */
    private short executableType;

    private String procedureSourceCode;
    
    public DefaultDatabaseExecutable() {}
    
    public DefaultDatabaseExecutable(DatabaseMetaTag metaTagParent, String name) {
        this.metaTagParent = metaTagParent;
        setName(name);
        
        if (metaTagParent.getCatalog() != null) {
            setCatalogName(metaTagParent.getCatalog().getName());
        }

        if (metaTagParent.getSchema() != null) {
            setSchemaName(metaTagParent.getSchema().getName());
        }

    }
    
    /**
     * Indicates whether this executable object has any parameters.
     *
     * @return true | false
     */
    public boolean hasParameters() {
        List<ProcedureParameter> _parameters = getParameters();
        return _parameters != null && !_parameters.isEmpty();
    }
   
    /**
     * Adds the specified values as a single parameter to this object.
     */
    public ProcedureParameter addParameter(String name, int type, int dataType,
                             String sqlType, int size) {
        if (parameters == null) {
        
            parameters = new ArrayList<ProcedureParameter>();
        }

        ProcedureParameter parameter = new ProcedureParameter(name, type, dataType, sqlType, size);
        parameters.add(parameter);
        
        return parameter;
    }
    
    /**
     * Returns this object's parameters as an array.
     */
    public ProcedureParameter[] getParametersArray() throws DataSourceException {
        if (parameters == null) {
            getParameters();
        }
        return (ProcedureParameter[])parameters.toArray(new
                                       ProcedureParameter[parameters.size()]);
    }

    public boolean supportCatalogOrSchemaInFunctionOrProcedureCalls() throws DataSourceException {

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            return dmd.supportsCatalogsInProcedureCalls() || dmd.supportsSchemasInProcedureCalls();

        }
        catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    public boolean supportCatalogInFunctionOrProcedureCalls() throws DataSourceException {
        
        try {
            
            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            return dmd.supportsCatalogsInProcedureCalls();
            
        }
        catch (SQLException e) {
            
            throw new DataSourceException(e);
        }
    }
    
    public boolean supportSchemaInFunctionOrProcedureCalls() throws DataSourceException {
        
        try {
            
            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            return dmd.supportsSchemasInProcedureCalls();
            
        }
        catch (SQLException e) {
            
            throw new DataSourceException(e);
        }
    }
    
    /**
     * Returns this object's parameters.
     */
    public List<ProcedureParameter> getParameters() throws DataSourceException {

        if (!isMarkedForReload() && parameters != null) {
        
            return parameters;
        }
        
        ResultSet rs = null;
        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            parameters = new ArrayList<ProcedureParameter>();
            
            String _catalog = getCatalogName();
            String _schema = getSchemaName();

            int type = getType();
            if (type == SYSTEM_FUNCTION ||
                    type == SYSTEM_STRING_FUNCTIONS || 
                    type == SYSTEM_NUMERIC_FUNCTIONS ||
                    type == SYSTEM_DATE_TIME_FUNCTIONS) {
                
                _catalog = null;
                _schema = null;
            
            } else {
              
                // check that the db supports catalog and 
                // schema names for this call
                if (!dmd.supportsCatalogsInProcedureCalls()) {
                    _catalog = null;
                }

                if (!dmd.supportsSchemasInProcedureCalls()) {
                    _schema = null;
                }

            }

            rs = dmd.getProcedureColumns(_catalog, _schema, getName(), null);
            while (rs.next()) {
            
                parameters.add(new ProcedureParameter(rs.getString(4),
                                                      rs.getInt(5),
                                                      rs.getInt(6),
                                                      rs.getString(7),
                                                      rs.getInt(8)));
            }
        
            return parameters;
        
        } catch (SQLException e) {
          
            throw new DataSourceException(e);

        } finally {
          
            releaseResources(rs);
            setMarkedForReload(false);
        }
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return PROCEDURE;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[PROCEDURE];
    }

    /**
     * Returns the parent meta tag object.
     *
     * @return the parent meta tag
     */
    public DatabaseMetaTag getMetaTagParent() {
        return metaTagParent;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object - the meta tag
     */
    public NamedObject getParent() {
        return getMetaTagParent();
    }

    /**
     * The executable (procedure) type:<br>
     * <ul>
     * <li> procedureResultUnknown - May return a result
     * <li> procedureNoResult - Does not return a result
     * <li> procedureReturnsResult - Returns a result
     * </ul>
     *
     * @return the proc type
     */
    public short getExecutableType() {
        return executableType;
    }

    public void setExecutableType(short executableType) {
        this.executableType = executableType;
    }

    public String getProcedureSourceCode() {
        if (!isMarkedForReload() && procedureSourceCode != null) {

            return procedureSourceCode;
        }

        procedureSourceCode = "";

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            if (this.getHost().getDatabaseConnection().getJDBCDriver().getClassName().contains("FBDriver")) {

                URL[] urls = new URL[0];
                Class clazzdb = null;
                Object odb = null;
                try {
                    urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar");
                    ClassLoader cl = new URLClassLoader(urls, dmd.getClass().getClassLoader());
                    clazzdb = cl.loadClass("biz.redsoft.DatabaseMetadata");
                    odb = clazzdb.newInstance();
                    IDatabaseMetadata db = (IDatabaseMetadata)odb;

                    procedureSourceCode = db.getProcedureSourceCode(dmd, getName());

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return procedureSourceCode;

        } finally {

            setMarkedForReload(false);
        }
    }

}





