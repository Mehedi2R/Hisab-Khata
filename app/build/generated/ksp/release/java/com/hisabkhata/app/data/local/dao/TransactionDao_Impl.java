package com.hisabkhata.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.hisabkhata.app.data.local.entity.TransactionEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity;

  private final EntityDeletionOrUpdateAdapter<TransactionEntity> __updateAdapterOfTransactionEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteTransaction;

  private final SharedSQLiteStatement __preparedStmtOfSoftDeleteTransactionsForCustomer;

  private final SharedSQLiteStatement __preparedStmtOfCleanOrphanedTransactions;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `transactions` (`id`,`customerId`,`type`,`amount`,`note`,`date`,`createdAt`,`updatedAt`,`isDeleted`,`syncStatus`,`currency`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCustomerId());
        statement.bindString(3, entity.getType());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getNote());
        statement.bindLong(6, entity.getDate());
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getUpdatedAt());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindString(10, entity.getSyncStatus());
        statement.bindString(11, entity.getCurrency());
      }
    };
    this.__updateAdapterOfTransactionEntity = new EntityDeletionOrUpdateAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `transactions` SET `id` = ?,`customerId` = ?,`type` = ?,`amount` = ?,`note` = ?,`date` = ?,`createdAt` = ?,`updatedAt` = ?,`isDeleted` = ?,`syncStatus` = ?,`currency` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TransactionEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCustomerId());
        statement.bindString(3, entity.getType());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getNote());
        statement.bindLong(6, entity.getDate());
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getUpdatedAt());
        final int _tmp = entity.isDeleted() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindString(10, entity.getSyncStatus());
        statement.bindString(11, entity.getCurrency());
        statement.bindString(12, entity.getId());
      }
    };
    this.__preparedStmtOfSoftDeleteTransaction = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transactions SET isDeleted = 1, updatedAt = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDeleteTransactionsForCustomer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transactions SET isDeleted = 1, updatedAt = ? WHERE customerId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfCleanOrphanedTransactions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transactions SET isDeleted = 1, updatedAt = ? WHERE customerId IN (SELECT id FROM customers WHERE isDeleted = 1)";
        return _query;
      }
    };
  }

  @Override
  public Object insertTransaction(final TransactionEntity transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTransactionEntity.insert(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<TransactionEntity> transactions,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTransactionEntity.insert(transactions);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTransaction(final TransactionEntity transaction,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTransactionEntity.handle(transaction);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteTransaction(final String id, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteTransaction.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSoftDeleteTransaction.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object softDeleteTransactionsForCustomer(final String customerId, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDeleteTransactionsForCustomer.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 2;
        _stmt.bindString(_argIndex, customerId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSoftDeleteTransactionsForCustomer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object cleanOrphanedTransactions(final long updatedAt,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCleanOrphanedTransactions.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAt);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfCleanOrphanedTransactions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> getTransactionsForCustomerFlow(final String customerId) {
    final String _sql = "SELECT * FROM transactions WHERE customerId = ? AND isDeleted = 0 ORDER BY date DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            _item = new TransactionEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpAmount,_tmpNote,_tmpDate,_tmpCreatedAt,_tmpUpdatedAt,_tmpIsDeleted,_tmpSyncStatus,_tmpCurrency);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTransactionsForCustomer(final String customerId,
      final Continuation<? super List<TransactionEntity>> $completion) {
    final String _sql = "SELECT * FROM transactions WHERE customerId = ? AND isDeleted = 0 ORDER BY date DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            _item = new TransactionEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpAmount,_tmpNote,_tmpDate,_tmpCreatedAt,_tmpUpdatedAt,_tmpIsDeleted,_tmpSyncStatus,_tmpCurrency);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TransactionEntity>> getAllTransactionsFlow() {
    final String _sql = "SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions"}, new Callable<List<TransactionEntity>>() {
      @Override
      @NonNull
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            _item = new TransactionEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpAmount,_tmpNote,_tmpDate,_tmpCreatedAt,_tmpUpdatedAt,_tmpIsDeleted,_tmpSyncStatus,_tmpCurrency);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTransactionById(final String id,
      final Continuation<? super TransactionEntity> $completion) {
    final String _sql = "SELECT * FROM transactions WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TransactionEntity>() {
      @Override
      @Nullable
      public TransactionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfIsDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isDeleted");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfCurrency = CursorUtil.getColumnIndexOrThrow(_cursor, "currency");
          final TransactionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpIsDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDeleted);
            _tmpIsDeleted = _tmp != 0;
            final String _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            _result = new TransactionEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpAmount,_tmpNote,_tmpDate,_tmpCreatedAt,_tmpUpdatedAt,_tmpIsDeleted,_tmpSyncStatus,_tmpCurrency);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CustomerCurrencyBalanceRow>> getAllCustomerCurrencyBalancesFlow() {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            t.customerId,\n"
            + "            COALESCE(t.currency, 'BDT') AS currency,\n"
            + "            COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0.0) AS totalDebit,\n"
            + "            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0.0) AS totalCredit\n"
            + "        FROM transactions t\n"
            + "        INNER JOIN customers c ON t.customerId = c.id\n"
            + "        WHERE t.isDeleted = 0 AND c.isDeleted = 0\n"
            + "        GROUP BY t.customerId, t.currency\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions",
        "customers"}, new Callable<List<CustomerCurrencyBalanceRow>>() {
      @Override
      @NonNull
      public List<CustomerCurrencyBalanceRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCustomerId = 0;
          final int _cursorIndexOfCurrency = 1;
          final int _cursorIndexOfTotalDebit = 2;
          final int _cursorIndexOfTotalCredit = 3;
          final List<CustomerCurrencyBalanceRow> _result = new ArrayList<CustomerCurrencyBalanceRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerCurrencyBalanceRow _item;
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final double _tmpTotalDebit;
            _tmpTotalDebit = _cursor.getDouble(_cursorIndexOfTotalDebit);
            final double _tmpTotalCredit;
            _tmpTotalCredit = _cursor.getDouble(_cursorIndexOfTotalCredit);
            _item = new CustomerCurrencyBalanceRow(_tmpCustomerId,_tmpCurrency,_tmpTotalDebit,_tmpTotalCredit);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CustomerCurrencyBalanceRow>> getCustomerCurrencyBalancesFlow(
      final String customerId) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            t.customerId,\n"
            + "            COALESCE(t.currency, 'BDT') AS currency,\n"
            + "            COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0.0) AS totalDebit,\n"
            + "            COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0.0) AS totalCredit\n"
            + "        FROM transactions t\n"
            + "        INNER JOIN customers c ON t.customerId = c.id\n"
            + "        WHERE t.customerId = ? AND t.isDeleted = 0 AND c.isDeleted = 0\n"
            + "        GROUP BY t.customerId, t.currency\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, customerId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions",
        "customers"}, new Callable<List<CustomerCurrencyBalanceRow>>() {
      @Override
      @NonNull
      public List<CustomerCurrencyBalanceRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCustomerId = 0;
          final int _cursorIndexOfCurrency = 1;
          final int _cursorIndexOfTotalDebit = 2;
          final int _cursorIndexOfTotalCredit = 3;
          final List<CustomerCurrencyBalanceRow> _result = new ArrayList<CustomerCurrencyBalanceRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerCurrencyBalanceRow _item;
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpCurrency;
            _tmpCurrency = _cursor.getString(_cursorIndexOfCurrency);
            final double _tmpTotalDebit;
            _tmpTotalDebit = _cursor.getDouble(_cursorIndexOfTotalDebit);
            final double _tmpTotalCredit;
            _tmpTotalCredit = _cursor.getDouble(_cursorIndexOfTotalCredit);
            _item = new CustomerCurrencyBalanceRow(_tmpCustomerId,_tmpCurrency,_tmpTotalDebit,_tmpTotalCredit);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Double> getTodayAmountByTypeFlow(final String type, final long startOfDay,
      final long endOfDay) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(t.amount), 0.0) \n"
            + "        FROM transactions t\n"
            + "        INNER JOIN customers c ON t.customerId = c.id\n"
            + "        WHERE t.isDeleted = 0 AND c.isDeleted = 0 AND t.type = ? AND t.date >= ? AND t.date <= ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"transactions",
        "customers"}, new Callable<Double>() {
      @Override
      @NonNull
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final double _tmp;
            _tmp = _cursor.getDouble(0);
            _result = _tmp;
          } else {
            _result = 0.0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
