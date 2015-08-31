package com.takumalee.ormcomparison.fragment;

import android.animation.Animator;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.j256.ormlite.dao.Dao;
import com.takumalee.ormcomparison.R;
import com.takumalee.ormcomparison.adapter.TabFragmentAdapter;
import com.takumalee.ormcomparison.database.greendao.DBHelper;
import com.takumalee.ormcomparison.database.greendao.dao.CharacterDao;
import com.takumalee.ormcomparison.database.ormlite.dao.DAOFactory;
import com.takumalee.ormcomparison.database.ormlite.model.Character;
import com.takumalee.ormcomparison.database.realm.model.RealmCharacter;
import com.takumalee.ormcomparison.fragment.base.ActivityBaseFragment;

import java.sql.SQLException;

import io.realm.Realm;

public class HomeFragment extends ActivityBaseFragment {
    private static final String TAG = HomeFragment.class.getSimpleName();


    final static String CAREER = "Career";
    final static String ATTRIBUTE = "Attr";

    private View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabFragmentAdapter adapter;

    private EditText fieldInsertCount;

    private Button ormliteInsertBtn;
    private TextView textViewOrmlite;
    private Button ormliteClearBtn;

    private Button buttonGreenDao;
    private TextView textViewGreenDao;
    private Button greenClearBtn;

    private Button buttonRealm;
    private TextView textViewRealm;
    private Button realmClearBtn;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker.setScreenName("首頁");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        Log.v(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()");
        view = inflater.inflate(R.layout.fragment_home, container, false);

        fieldInsertCount = (EditText) view.findViewById(R.id.field_insert_count);

        ormliteInsertBtn = (Button) view.findViewById(R.id.button_ormlite_insert);
        textViewOrmlite = (TextView) view.findViewById(R.id.textView_ormlite_time);
        ormliteClearBtn = (Button) view.findViewById(R.id.button_ormlite_clear);

        buttonGreenDao = (Button) view.findViewById(R.id.button_greendao_insert);
        textViewGreenDao = (TextView) view.findViewById(R.id.textView_greendao_time);
        greenClearBtn = (Button) view.findViewById(R.id.button_greendao_clear);

        buttonRealm = (Button) view.findViewById(R.id.button_realm_insert);
        textViewRealm = (TextView) view.findViewById(R.id.textView_realm_time);
        realmClearBtn = (Button) view.findViewById(R.id.button_realm_clear);

        ormliteInsertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldInsertCount.getText().toString().isEmpty()) {
                    return;
                }

                truncateORMLite();

                Dao<Character, Integer> dao = null;
                long ormliteTime = System.currentTimeMillis();
                try {
                    dao = DAOFactory.getInstance().getDbHelper().getDao(Character.class);
                    final int amount = getTestAmount();
                    DAOFactory.getInstance().beginTransaction();
                    for (int i = 0; i < amount; i++) {
                        Character characterOrmlite = new Character();
                        characterOrmlite.setCareers(CAREER);
                        characterOrmlite.setAttributes(ATTRIBUTE);
//                        dao.createIfNotExists(characterOrmlite);
                        dao.create(characterOrmlite);
                    }
                    DAOFactory.getInstance().commitTransaction();
                    long ormliteElapsedTime = System.currentTimeMillis() - ormliteTime;
                    textViewOrmlite.setText(String.valueOf(ormliteElapsedTime) + "ms");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        ormliteClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                truncateORMLite();
            }
        });

        buttonGreenDao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldInsertCount.getText().toString().isEmpty()) {
                    return;
                }

                truncateGreenDAO();

                final int amount = getTestAmount();

                long startTime = System.currentTimeMillis();
                CharacterDao characterDao = DBHelper.getInstance(activity).getCharacterDao();
                SQLiteDatabase db = characterDao.getDatabase();
                db.beginTransaction();
                for (int i = 0; i < amount; i++) {
                    com.takumalee.ormcomparison.database.greendao.dao.Character greenCharacter = new com.takumalee.ormcomparison.database.greendao.dao.Character(null, CAREER, ATTRIBUTE);
                    characterDao.insert(greenCharacter);
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                long greenElapsedTime = System.currentTimeMillis() - startTime;
                textViewGreenDao.setText(String.valueOf(greenElapsedTime) + "ms");
            }
        });
        greenClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                truncateGreenDAO();
            }
        });

        buttonRealm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldInsertCount.getText().toString().isEmpty()) {
                    return;
                }

                truncateRealm();

                final int amount = getTestAmount();
                long startTime = System.currentTimeMillis();

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                for (int i = 0; i < amount; i++) {
                    RealmCharacter realmCharacter = new RealmCharacter();
//                    RealmCharacter realmCharacter = realm.createObject(RealmCharacter.class);
                    realmCharacter.setId(i);
                    realmCharacter.setCareers(CAREER);
                    realmCharacter.setAttributes(ATTRIBUTE);
//                    realm.copyToRealmOrUpdate(realmCharacter);
                    realm.copyToRealm(realmCharacter);
                }
                realm.commitTransaction();
                long realmElapsedTime = System.currentTimeMillis() - startTime;
                textViewRealm.setText(String.valueOf(realmElapsedTime) + "ms");

            }
        });
        realmClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                truncateRealm();
            }
        });

        return view;
    }

    int getTestAmount() {
        String amount = fieldInsertCount.getText().toString();
        return amount.trim().isEmpty() ? 0 : Integer.parseInt(amount);
    }

    void truncateORMLite() {
        DAOFactory.getInstance().getDbHelper().eraseAllData();
    }

    void truncateGreenDAO() {
        CharacterDao characterDao = DBHelper.getInstance(activity).getCharacterDao();

        SQLiteDatabase db = characterDao.getDatabase();
        db.beginTransaction();
        characterDao.deleteAll();
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    void truncateRealm() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(RealmCharacter.class);
        realm.commitTransaction();
    }


    @Override
    public boolean onBackPressed() {
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new TabFragmentAdapter(getChildFragmentManager());
        adapter.addFragment(new Fragment(), "出境");
        adapter.addFragment(new Fragment(), "入境");
        viewPager.setAdapter(adapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
//        ((ORMHomeActivity)activity).addCustomView(tabLayout);
//        historyFragment.setActionBarFunctionListener(actionBarFunctionListener);
//        historyDiagramFragment.setActionBarFunctionListener(actionBarFunctionListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
    }

    @Override
    protected void initActionBar() {

    }

    @Override
    public void onAnimationStart() {

    }

    @Override
    public void onAnimationEnd(Animator animation) {

    }

}
