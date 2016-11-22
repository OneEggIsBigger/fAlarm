package de.openfiresource.falarm.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import de.openfiresource.falarm.R;

/**
 * Created by Kevin on 25.10.2016.
 */

public class ApiUtils {
    private static final String URL = "https://falcon.alphard.uberspace.de/falarm/";

    private static RequestQueue queue;
    private static Context context;

    public static void init(Context context) {
        ApiUtils.context = context;
        queue = Volley.newRequestQueue(context);
    }

    public static void confirm(String key, boolean come) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            StringRequest postRequest = new StringRequest(Request.Method.POST, URL + "api/confirm",
                    //@todo: Better error handling here
                    response -> {
                        // response
                        Log.d("Response", response);
                    },
                    error -> {
                        // error
                        Log.e("Error.Response", error.toString());
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", user.getEmail());
                    params.put("name", user.getDisplayName());
                    params.put("operation", key);
                    params.put("come", Boolean.toString(come));

                    return params;
                }
            };
            queue.add(postRequest);
        }
    }

    public static void register(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            StringRequest postRequest = new StringRequest(Request.Method.PUT, URL + "api/user",
                    response -> {
                        // response
                        Log.d("Response", response);
                        if (response.equals("true"))
                            Toast.makeText(context, context.getString(R.string.fcm_key_ok), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, context.getString(R.string.fcm_key_error), Toast.LENGTH_LONG).show();
                    },
                    error -> {
                        // error
                        Log.d("Error.Response", error.toString());
                        Toast.makeText(context, context.getString(R.string.fcm_key_error), Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", user.getEmail());
                    params.put("key", token);

                    return params;
                }
            };
            queue.add(postRequest);
        }
    }

    public static void register() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        register(token);
    }
}
