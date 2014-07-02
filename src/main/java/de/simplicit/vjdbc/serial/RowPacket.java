// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.util.JavaVersionInfo;

/**
 * A RowPacket contains the data of a part (or a whole) JDBC-ResultSet.
 */
public class RowPacket implements Externalizable {
    static final int ORACLE_ROW_ID = -8;
    private static final int DEFAULT_ARRAY_SIZE = 100;
    static final long serialVersionUID = 6366194574502000718L;

    private static Log _logger = LogFactory.getLog(RowPacket.class);

    private int _rowCount = 0;
    private boolean _forwardOnly = false;
    private boolean _lastPart = false;

    private FlattenedColumnValues[] _flattenedColumnsValues = null;
    
    // Transient attributes
    private transient List<RowPacket> chain = null;
    private transient int[] _columnTypes = null;
    private transient int _offset = 0;
    private transient int _maxrows = 0;

    public RowPacket() {
    }

    public RowPacket(int packetsize, boolean forwardOnly) {
        _maxrows = packetsize;
        _forwardOnly = forwardOnly;
    }

    
    RowPacket(boolean forwardOnly, boolean lastPart, int rowCount, FlattenedColumnValues[] flattenedColumnsValues){
    	this._forwardOnly = forwardOnly;
    	this._lastPart = lastPart;
    	this._rowCount = rowCount;
    	this._flattenedColumnsValues = flattenedColumnsValues;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(_forwardOnly);
        out.writeBoolean(_lastPart);
        out.writeInt(_rowCount);
        if(_rowCount > 0) {
            out.writeObject(_flattenedColumnsValues);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _forwardOnly = in.readBoolean();
        _lastPart = in.readBoolean();
        _rowCount = in.readInt();
        if (_rowCount>0){
        	_flattenedColumnsValues = (FlattenedColumnValues[]) in.readObject();
        }
        
//        if(_rowCount > 0) {
//            FlattenedColumnValues[] flattenedColumns = (FlattenedColumnValues[]) in.readObject();
//            _rows = new ArrayList(_rowCount);
//            for(int i = 0; i < _rowCount; i++) {
//                Object[] row = new Object[flattenedColumns.length];
//                for(int j = 0; j < flattenedColumns.length; j++) {
//                    row[j] = flattenedColumns[j].getValue(i);
//                }
//                _rows.add(row);
//            }
//        }
//        else {
//            _rows = new ArrayList();
//        }
    }

    public Object[] get(int index) throws SQLException {
    	RowPacket rowPacket = this;
        if (chain!=null){
        	rowPacket = chain.get(index/_rowCount);
        }

    	int adjustedIndex = index - rowPacket._offset;

        if(adjustedIndex < 0) {
            throw new SQLException("Index " + index + " is below the possible index");
        } else if(adjustedIndex >= rowPacket._rowCount) {
            throw new SQLException("Index " + index + " is above the possible index");
        } else {
        	// in Augeo each row is read only once, so there is no need to cache 
        	Object[] row = new Object[rowPacket._flattenedColumnsValues.length];
        	for (int k=0; k<row.length; k++){
        		row[k] = rowPacket._flattenedColumnsValues[k].getValue(adjustedIndex);
        	}
        	return row;
        }
    }

    public int size() {
    	RowPacket rowPacket = this;
        if (chain!=null){
        	rowPacket = chain.get(chain.size()-1);
        }
        return rowPacket._offset + rowPacket._rowCount;
    }

    public boolean isLastPart() {
    	RowPacket rowPacket = this;
        if (chain!=null){
        	rowPacket = chain.get(chain.size()-1);
        }    	
    	return rowPacket._lastPart;
    }

    public boolean populate(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        int columnCount = metaData.getColumnCount();
        _rowCount = 0;

        while (rs.next()) {
            if(_rowCount == 0) {
                prepareFlattenedColumns(metaData, columnCount);
            }

            for(int i = 1; i <= columnCount; i++) {
                boolean foundMatch = true;

                int internalIndex = i - 1;

                switch (_columnTypes[internalIndex]) {
                case Types.NULL:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, null);
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getString(i));
                    break;

                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getNString(i));
                    break;

                case Types.NUMERIC:
                case Types.DECIMAL:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getBigDecimal(i));
                    break;

                case Types.BIT:
                    _flattenedColumnsValues[internalIndex].setBoolean(_rowCount, rs.getBoolean(i));
                    break;

                case Types.TINYINT:
                    _flattenedColumnsValues[internalIndex].setByte(_rowCount, rs.getByte(i));
                    break;

                case Types.SMALLINT:
                    _flattenedColumnsValues[internalIndex].setShort(_rowCount, rs.getShort(i));
                    break;

                case Types.INTEGER:
                    _flattenedColumnsValues[internalIndex].setInt(_rowCount, rs.getInt(i));
                    break;

                case Types.BIGINT:
                    _flattenedColumnsValues[internalIndex].setLong(_rowCount, rs.getLong(i));
                    break;

                case Types.REAL:
                    _flattenedColumnsValues[internalIndex].setFloat(_rowCount, rs.getFloat(i));
                    break;

                case Types.FLOAT:
                case Types.DOUBLE:
                    _flattenedColumnsValues[internalIndex].setDouble(_rowCount, rs.getDouble(i));
                    break;

                case Types.DATE:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getDate(i));
                    break;

                case Types.TIME:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getTime(i));
                    break;

                case Types.TIMESTAMP:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getTimestamp(i));
                    break;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, rs.getBytes(i));
                    break;

                case Types.JAVA_OBJECT:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialJavaObject(rs.getObject(i)));
                    break;

                case Types.CLOB:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialClob(rs.getClob(i)));
                    break;

                case Types.NCLOB:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialNClob(rs.getNClob(i)));
                    break;

                case Types.BLOB:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialBlob(rs.getBlob(i)));
                    break;

                case Types.ARRAY:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialArray(rs.getArray(i)));
                    break;

                case Types.STRUCT:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialStruct((Struct) rs.getObject(i)));
                    break;

                case ORACLE_ROW_ID:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialRowId(rs.getRowId(i), rs.getString(i)));
                    break;

                    // what oracle does instead of SQLXML in their 1.6 driver,
                    // don't ask me why, commented out so we don't need
                    // an oracle driver to compile this class
                    //case 2007:
                    //_flattenedColumnsValues[internalIndex].setObject(_rowCount, new XMLType(((OracleResultSet)rs).getOPAQUE(i)));
                case Types.SQLXML:
                    _flattenedColumnsValues[internalIndex].setObject(_rowCount, new SerialSQLXML(rs.getSQLXML(i)));
                    break;

                default:
                    if(JavaVersionInfo.use14Api) {
                        if(_columnTypes[internalIndex] == Types.BOOLEAN) {
                            _flattenedColumnsValues[internalIndex].setBoolean(_rowCount, rs.getBoolean(i));
                        }
                        else {
                            foundMatch = false;
                        }
                    } else {
                        foundMatch = false;
                    }
                    break;
                }

                if(foundMatch) {
                    if(rs.wasNull()) {
                        _flattenedColumnsValues[internalIndex].setIsNull(_rowCount);
                    }
                } else {
                    throw new SQLException("Unsupported JDBC-Type: " + _columnTypes[internalIndex]);
                }
            }

            _rowCount++;

            if(_maxrows > 0 && _rowCount == _maxrows) {
                break;
            }
        }

        _lastPart = _maxrows == 0 || _rowCount < _maxrows;

        return _lastPart;
    }

    private void prepareFlattenedColumns(ResultSetMetaData metaData, int columnCount) throws SQLException {
        _columnTypes = new int[columnCount];
        _flattenedColumnsValues = new FlattenedColumnValues[columnCount];

        for(int i = 1; i <= columnCount; i++) {
            int columnType = _columnTypes[i - 1] = metaData.getColumnType(i);

            if(_logger.isDebugEnabled()) {
                _logger.debug("Column-Type " + i + ": " + metaData.getColumnType(i));
            }

            Class componentType = null;

            switch (columnType) {
            case Types.NULL:
            	componentType = Object.class;
            	break;
            	
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            	componentType = String.class;
            	break;
            
            case Types.NUMERIC:
            case Types.DECIMAL:
            	componentType = BigDecimal.class;
            	break;
            case Types.BIT:
                componentType = Boolean.TYPE;
                break;

            case Types.TINYINT:
                componentType = Byte.TYPE;
                break;

            case Types.SMALLINT:
                componentType = Short.TYPE;
                break;

            case Types.INTEGER:
                componentType = Integer.TYPE;
                break;

            case Types.BIGINT:
                componentType = Long.TYPE;
                break;

            case Types.REAL:
                componentType = Float.TYPE;
                break;

            case Types.FLOAT:
            case Types.DOUBLE:
                componentType = Double.TYPE;
                break;

            case Types.DATE:
            	componentType = java.sql.Date.class;
            	break;
            case Types.TIME:
            	componentType = java.sql.Time.class;
            	break;
            case Types.TIMESTAMP:
            	componentType = java.sql.Timestamp.class;
            	break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            	componentType = byte[].class;
            	break;
            case Types.JAVA_OBJECT:
            	componentType = SerialJavaObject.class;
            	break;
            case Types.CLOB:
            	componentType = SerialClob.class;
            	break;
            case Types.NCLOB:
            	componentType = SerialNClob.class;
            	break;
            case Types.BLOB:
            	componentType = SerialBlob.class;
            	break;
            case Types.ARRAY:
            	componentType = SerialArray.class;
            	break;
            case Types.STRUCT:
            	componentType = SerialStruct.class;
            	break;
            case RowPacket.ORACLE_ROW_ID:
            	componentType = SerialRowId.class;
            	break;
            case Types.SQLXML:
            	componentType = SerialSQLXML.class;
            	break;
            	

            default:
                if(JavaVersionInfo.use14Api) {
                    if(columnType == Types.BOOLEAN) {
                        componentType = Boolean.TYPE;
                    } else {
                        componentType = Object.class;
                    }
                } else {
                    componentType = Object.class;
                }
                break;
            }

            _flattenedColumnsValues[i - 1] = new FlattenedColumnValues(componentType, _maxrows == 0 ? DEFAULT_ARRAY_SIZE : _maxrows);
        }
    }

    public void merge(RowPacket rsp) throws SQLException {
    	if(_forwardOnly) {
            _offset += _rowCount;
            _rowCount = rsp._rowCount;
            _flattenedColumnsValues = rsp._flattenedColumnsValues;
            _lastPart = rsp._lastPart;
        } else {
        	if (chain==null){
        		chain = new ArrayList<RowPacket>();
        		chain.add(this);
        	}
        	if (_rowCount != rsp._rowCount && !rsp._lastPart){
        		throw new SQLException("attempt to merge row packet with different length");
        	}
        	// avoid copying data, just put the packets in list
        	RowPacket last = chain.get(chain.size()-1);
        	rsp._offset = last._offset + last._rowCount;
        	chain.add(rsp);
        }
    }

	int getRowCount() {
		return _rowCount;
	}

	boolean isForwardOnly() {
		return _forwardOnly;
	}

	FlattenedColumnValues[] getFlattenedColumnsValues() {
		return _flattenedColumnsValues;
	}
	
}
