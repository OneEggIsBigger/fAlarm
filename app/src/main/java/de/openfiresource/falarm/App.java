package de.openfiresource.falarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.widget.ImageView;

import com.karumi.dexter.Dexter;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.orm.SugarApp;
import com.orm.SugarContext;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import de.openfiresource.falarm.models.Notification;

/**
 * Created by stieglit on 03.08.2016.
 */
public class App extends SugarApp {
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
        LeakCanary.install(this);
        Dexter.initialize(this);

        //Load Default settings
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        new Notification(0, this).loadDefault();

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        SugarContext.terminate();
        super.onTerminate();
    }
}