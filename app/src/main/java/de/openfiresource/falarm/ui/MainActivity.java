package de.openfiresource.falarm.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.action.WelcomeButtonAction;
import com.dexafree.materialList.listeners.RecyclerItemClickListener;
import com.dexafree.materialList.view.MaterialListView;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.openfiresource.falarm.BuildConfig;
import de.openfiresource.falarm.R;
import de.openfiresource.falarm.dialogs.MainMultiplePermissionsListener;
import de.openfiresource.falarm.models.OperationMessage;
import de.openfiresource.falarm.utils.PlayServiceUtils;
import de.openfiresource.falarm.utils.ApiUtils;

public class MainActivity extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener,
        Drawer.OnDrawerItemClickListener {

    public static final String INTENT_RECEIVED_MESSAGE = "de.openfiresource.falarm.ui.receivedMessage";
    public static final String SHOW_WELCOME_CARD_VERSION = "showWelcomeCardVersion";
    private static final int RC_SIGN_IN = 9001;

    private static final int NAV_RULES = 1;
    private static final int NAV_SETTINGS = 2;
    private static final int NAV_LOGOUT = 3;
    private static final int NAV_ABOUT = 4;
    private static final int NAV_LEGAL_NOTICE = 5;

    private SharedPreferences mSharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Drawer mDrawer;

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.material_listview)
    MaterialListView mListView;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotifications();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser == null) {
            startActivityForResult(showLogin(), RC_SIGN_IN);
        } else {
            //Toolbar
            setUpNavidationDrawer();

            updateNotifications();
            mListView.addOnItemTouchListener(this);
            
            //Load permissions
            CompositeMultiplePermissionsListener compositeMultiplePermissionsListener
                    = new CompositeMultiplePermissionsListener(new MainMultiplePermissionsListener(this),
                    SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(rootView,
                            R.string.permission_rationale_message)
                            .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                            .build());
            Dexter.checkPermissions(compositeMultiplePermissionsListener,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }
    }

    private Intent showLogin() {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.AppTheme)
                .setLogo(R.drawable.falarm_logo)
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setProviders(
                        AuthUI.EMAIL_PROVIDER,
                        AuthUI.GOOGLE_PROVIDER)
                .build();
    }

    private void setUpNavidationDrawer() {
        setSupportActionBar(mToolbar);

        ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem().withName(mUser.getDisplayName())
                .withEmail(mUser.getEmail());
        if (mUser.getPhotoUrl() != null)
            profileDrawerItem.withIcon(mUser.getPhotoUrl());


        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.flame)
                .addProfiles(profileDrawerItem)
                .withOnAccountHeaderListener((view, profile, currentProfile) -> false)
                .build();

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.action_rules))
                                .withIdentifier(NAV_RULES).withIcon(GoogleMaterial.Icon.gmd_group),
                        new PrimaryDrawerItem().withName(getString(R.string.action_settings))
                                .withIdentifier(NAV_SETTINGS).withIcon(GoogleMaterial.Icon.gmd_settings),
                        new SectionDrawerItem().withName(getString(R.string.action_sub_user)),
                        new PrimaryDrawerItem().withName(getString(R.string.action_logout))
                                .withIdentifier(NAV_LOGOUT).withIcon(GoogleMaterial.Icon.gmd_clear)
                )
                .addStickyDrawerItems(new PrimaryDrawerItem().withName(getString(R.string.action_about))
                        .withIdentifier(NAV_ABOUT).withIcon(GoogleMaterial.Icon.gmd_info))
                .addStickyDrawerItems(new PrimaryDrawerItem().withName(getString(R.string.action_legal_notice))
                        .withIdentifier(NAV_LEGAL_NOTICE).withIcon(GoogleMaterial.Icon.gmd_mood))
                .withOnDrawerItemClickListener(this)
                .build();
        mDrawer.setSelection(-1);

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (drawerItem != null) {
            if (drawerItem instanceof Nameable) {
                Intent intent;
                switch ((int) drawerItem.getIdentifier()) {
                    case NAV_RULES:
                        intent = new Intent(this, RuleListActivity.class);
                        startActivity(intent);
                        break;
                    case NAV_SETTINGS:
                        intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case NAV_LEGAL_NOTICE:
                        intent = new Intent(this, LegalNoticeActivity.class);
                        startActivity(intent);
                        break;
                    case NAV_LOGOUT:
                        AuthUI.getInstance()
                                .signOut(this)
                                .addOnCompleteListener(task -> {
                                    // user is now signed out. Restart this activity to get the login screen.
                                    Intent newIntent = getIntent();
                                    finish();
                                    startActivity(newIntent);
                                });
                        finish();
                        break;
                    case NAV_ABOUT:
                        new LibsBuilder()
                                .withFields(R.string.class.getFields())
                                .withActivityTitle(getString(R.string.action_about))
                                .withActivityTheme(R.style.AboutLibrariesTheme_Light)
                                .start(this);
                        break;
                }
            }
        }
        return false;

    }

    private int getVersionCode() {
        PackageManager pm = getBaseContext().getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getBaseContext().getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
        }
        return 0;
    }

    @Override
    public void onResume() {
        PlayServiceUtils.checkPlayServices(this);
        super.onResume();
    }

    @Override
    protected void onStart() {
        //Broadcast receiver
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(INTENT_RECEIVED_MESSAGE);
        registerReceiver(receiver, filterSend);

        super.onStart();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                finish();
                startActivity(new Intent(this, MainActivity.class));
                ApiUtils.register();
            }
        } else {
            // user is not signed in. Maybe just wait for the user to press
            // "sign in" again, or show a message
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(this.receiver);
    }

    private void updateNotifications() {
        mListView.getAdapter().clearAll();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int lastVersion = mSharedPreferences.getInt(SHOW_WELCOME_CARD_VERSION, 0);
        if (lastVersion < getVersionCode()) {
            createWelcomeCard();
        }

        List<OperationMessage> notifications = OperationMessage.find(OperationMessage.class, null, null, null, "id desc", "15");

        for (OperationMessage operationMessage : notifications) {
            DateFormat df = new DateFormat();
            String message = "Alarmiert am " + df.format("dd.MM.yyyy HH:mm:ss", operationMessage.getTimestamp());
            createCard(operationMessage.getTitle(), message, operationMessage.getId());
        }
    }

    private void createWelcomeCard() {
        String text = null;
        int lastVersion = mSharedPreferences.getInt(SHOW_WELCOME_CARD_VERSION, 0);
        if (lastVersion != 0) {
            switch (getVersionCode()) {
                case 3:
                    text = getString(R.string.welcome_card_desc_v3);
                    break;
            }
        }

        if (text == null)
            text = getString(R.string.welcome_card_desc);

        Card card = new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_welcome_card_layout)
                .setTitle(getString(R.string.welcome_card_title))
                .setTitleColor(Color.WHITE)
                .setDescription(text)
                .setDescriptionColor(Color.WHITE)
                .setSubtitle(getString(R.string.welcome_card_subtitle))
                .setSubtitleColor(Color.WHITE)
                .setBackgroundColor(Color.RED)
                .addAction(R.id.ok_button, new WelcomeButtonAction(this)
                        .setText("Okay!")
                        .setTextColor(Color.WHITE)
                        .setListener((view, card1) -> {
                            mListView.getAdapter().remove(card1, true);
                            mSharedPreferences.edit()
                                    .putInt(SHOW_WELCOME_CARD_VERSION, getVersionCode())
                                    .commit();
                        }))
                .endConfig()
                .build();
        card.setDismissible(true);
        mListView.getAdapter().add(card);
    }

    private void createCard(String title, String desc, long id) {
        Card card = new Card.Builder(this)
                .setTag(id)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_basic_buttons_card)
                .setTitle(title)
                .setDescription(desc)
                .endConfig()
                .build();

        mListView.getAdapter().add(mListView.getAdapter().getItemCount(), card, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(@NonNull Card card, int position) {
        if (card.getTag() != null) {
            long id = (long) card.getTag();

            Intent intent = new Intent(this, OperationActivity.class);
            intent.putExtra(OperationActivity.EXTRA_ID, id);
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(@NonNull Card card, int position) {
        if (card.getTag() != null) {
            long id = (long) card.getTag();

            final CharSequence[] items = {getString(R.string.delete)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.main_long_click_header);
            builder.setItems(items, (dialog, item) -> {
                switch (item) {
                    case 0:
                        OperationMessage operationMessage = OperationMessage.findById(OperationMessage.class, id);
                        operationMessage.delete();
                        updateNotifications();
                        break;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
