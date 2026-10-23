package com.hisabkhata.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.hisabkhata.app.data.local.dao.CashBookDao;
import com.hisabkhata.app.data.local.dao.CashBookDao_Impl;
import com.hisabkhata.app.data.local.dao.CustomerDao;
import com.hisabkhata.app.data.local.dao.CustomerDao_Impl;
import com.hisabkhata.app.data.local.dao.FamilyExpenseDao;
import com.hisabkhata.app.data.local.dao.FamilyExpenseDao_Impl;
import com.hisabkhata.app.data.local.dao.TransactionDao;
import com.hisabkhata.app.data.local.dao.TransactionDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CustomerDao _customerDao;

  private volatile TransactionDao _transactionDao;

  private volatile FamilyExpenseDao _familyExpenseDao;

  private volatile CashBookDao _cashBookDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `address` TEXT NOT NULL, `type` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `customerId` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `note` TEXT NOT NULL, `date` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `syncStatus` TEXT NOT NULL, `currency` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_customerId` ON `transactions` (`customerId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_syncStatus` ON `transactions` (`syncStatus`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `family_expenses` (`id` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` REAL NOT NULL, `note` TEXT NOT NULL, `date` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cash_book_entries` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `paymentMethod` TEXT NOT NULL, `note` TEXT NOT NULL, `date` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `currency` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9341297729af91244505224f410d99e1')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `customers`");
        db.execSQL("DROP TABLE IF EXISTS `transactions`");
        db.execSQL("DROP TABLE IF EXISTS `family_expenses`");
        db.execSQL("DROP TABLE IF EXISTS `cash_book_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCustomers = new HashMap<String, TableInfo.Column>(9);
        _columnsCustomers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCustomers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCustomers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCustomers = new TableInfo("customers", _columnsCustomers, _foreignKeysCustomers, _indicesCustomers);
        final TableInfo _existingCustomers = TableInfo.read(db, "customers");
        if (!_infoCustomers.equals(_existingCustomers)) {
          return new RoomOpenHelper.ValidationResult(false, "customers(com.hisabkhata.app.data.local.entity.CustomerEntity).\n"
                  + " Expected:\n" + _infoCustomers + "\n"
                  + " Found:\n" + _existingCustomers);
        }
        final HashMap<String, TableInfo.Column> _columnsTransactions = new HashMap<String, TableInfo.Column>(11);
        _columnsTransactions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("customerId", new TableInfo.Column("customerId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("syncStatus", new TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("currency", new TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTransactions = new HashSet<TableInfo.Index>(3);
        _indicesTransactions.add(new TableInfo.Index("index_transactions_customerId", false, Arrays.asList("customerId"), Arrays.asList("ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        _indicesTransactions.add(new TableInfo.Index("index_transactions_syncStatus", false, Arrays.asList("syncStatus"), Arrays.asList("ASC")));
        final TableInfo _infoTransactions = new TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions);
        final TableInfo _existingTransactions = TableInfo.read(db, "transactions");
        if (!_infoTransactions.equals(_existingTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "transactions(com.hisabkhata.app.data.local.entity.TransactionEntity).\n"
                  + " Expected:\n" + _infoTransactions + "\n"
                  + " Found:\n" + _existingTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsFamilyExpenses = new HashMap<String, TableInfo.Column>(6);
        _columnsFamilyExpenses.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyExpenses.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyExpenses.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyExpenses.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFamilyExpenses.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFamilyExpenses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFamilyExpenses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFamilyExpenses = new TableInfo("family_expenses", _columnsFamilyExpenses, _foreignKeysFamilyExpenses, _indicesFamilyExpenses);
        final TableInfo _existingFamilyExpenses = TableInfo.read(db, "family_expenses");
        if (!_infoFamilyExpenses.equals(_existingFamilyExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "family_expenses(com.hisabkhata.app.data.local.entity.FamilyExpenseEntity).\n"
                  + " Expected:\n" + _infoFamilyExpenses + "\n"
                  + " Found:\n" + _existingFamilyExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsCashBookEntries = new HashMap<String, TableInfo.Column>(9);
        _columnsCashBookEntries.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("paymentMethod", new TableInfo.Column("paymentMethod", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashBookEntries.put("currency", new TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCashBookEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCashBookEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCashBookEntries = new TableInfo("cash_book_entries", _columnsCashBookEntries, _foreignKeysCashBookEntries, _indicesCashBookEntries);
        final TableInfo _existingCashBookEntries = TableInfo.read(db, "cash_book_entries");
        if (!_infoCashBookEntries.equals(_existingCashBookEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "cash_book_entries(com.hisabkhata.app.data.local.entity.CashBookEntity).\n"
                  + " Expected:\n" + _infoCashBookEntries + "\n"
                  + " Found:\n" + _existingCashBookEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9341297729af91244505224f410d99e1", "da0373d3bcb5841c1d6652e974dbc652");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "customers","transactions","family_expenses","cash_book_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `customers`");
      _db.execSQL("DELETE FROM `transactions`");
      _db.execSQL("DELETE FROM `family_expenses`");
      _db.execSQL("DELETE FROM `cash_book_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CustomerDao.class, CustomerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FamilyExpenseDao.class, FamilyExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CashBookDao.class, CashBookDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CustomerDao customerDao() {
    if (_customerDao != null) {
      return _customerDao;
    } else {
      synchronized(this) {
        if(_customerDao == null) {
          _customerDao = new CustomerDao_Impl(this);
        }
        return _customerDao;
      }
    }
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public FamilyExpenseDao familyExpenseDao() {
    if (_familyExpenseDao != null) {
      return _familyExpenseDao;
    } else {
      synchronized(this) {
        if(_familyExpenseDao == null) {
          _familyExpenseDao = new FamilyExpenseDao_Impl(this);
        }
        return _familyExpenseDao;
      }
    }
  }

  @Override
  public CashBookDao cashBookDao() {
    if (_cashBookDao != null) {
      return _cashBookDao;
    } else {
      synchronized(this) {
        if(_cashBookDao == null) {
          _cashBookDao = new CashBookDao_Impl(this);
        }
        return _cashBookDao;
      }
    }
  }
}
