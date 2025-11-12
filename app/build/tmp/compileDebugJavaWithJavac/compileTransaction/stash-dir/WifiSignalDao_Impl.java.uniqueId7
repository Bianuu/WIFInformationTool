package com.example.wifiinformationtool;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WifiSignalDao_Impl implements WifiSignalDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WifiSignalEntity> __insertionAdapterOfWifiSignalEntity;

  public WifiSignalDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWifiSignalEntity = new EntityInsertionAdapter<WifiSignalEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `wifi_signal_table` (`id`,`ssid`,`signalStrength`,`timestamp`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final WifiSignalEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.ssid == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.ssid);
        }
        statement.bindLong(3, entity.signalStrength);
        statement.bindLong(4, entity.timestamp);
      }
    };
  }

  @Override
  public void insert(final WifiSignalEntity signal) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWifiSignalEntity.insert(signal);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<WifiSignalEntity> getSignalHistory(final String ssid) {
    final String _sql = "SELECT * FROM wifi_signal_table WHERE ssid = ? ORDER BY timestamp DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ssid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ssid);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSsid = CursorUtil.getColumnIndexOrThrow(_cursor, "ssid");
      final int _cursorIndexOfSignalStrength = CursorUtil.getColumnIndexOrThrow(_cursor, "signalStrength");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<WifiSignalEntity> _result = new ArrayList<WifiSignalEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WifiSignalEntity _item;
        final String _tmpSsid;
        if (_cursor.isNull(_cursorIndexOfSsid)) {
          _tmpSsid = null;
        } else {
          _tmpSsid = _cursor.getString(_cursorIndexOfSsid);
        }
        final int _tmpSignalStrength;
        _tmpSignalStrength = _cursor.getInt(_cursorIndexOfSignalStrength);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item = new WifiSignalEntity(_tmpSsid,_tmpSignalStrength,_tmpTimestamp);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
