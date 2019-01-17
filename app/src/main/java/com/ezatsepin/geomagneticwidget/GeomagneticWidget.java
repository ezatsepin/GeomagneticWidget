package com.ezatsepin.geomagneticwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.webkit.WebView;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link GeomagneticWidgetConfigureActivity GeomagneticWidgetConfigureActivity}
 */
public class GeomagneticWidget extends AppWidgetProvider {

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.geomagnetic_widget);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static final String GM_WIDGET_CLICKED = "GM_WIDGET_CLICKED";
    private static final String ACTION_APPWIDGET_UPDATE = AppWidgetManager.ACTION_APPWIDGET_UPDATE;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.geomagnetic_widget);
        ComponentName watchWidget = new ComponentName(context, GeomagneticWidget.class);

        remoteViews.setOnClickPendingIntent(R.id.widget_layout, getPendingSelfIntent(context, GM_WIDGET_CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            GeomagneticWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final String action = intent.getAction();

        if (GM_WIDGET_CLICKED.equals(action)  || ACTION_APPWIDGET_UPDATE.equals(action)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.geomagnetic_widget);
            ComponentName watchWidget = new ComponentName(context, GeomagneticWidget.class);

            //Spanned html = Html.fromHtml("<h2 style='color: red;'><font color='#145A14'>text</font></h2><br><table><tr><td>1</td><td>2</td></tr><tr><td>3</td><td>4</td></tr></table>");

            Resources res = context.getResources();


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String str = "http://tyretrader.ua/utils/test2.php";
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(str);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                //Log.d("stringBuffer", stringBuffer.toString());

                try {
                    JSONObject jsonObj = new JSONObject(stringBuffer.toString());
                    Iterator<String> temp = jsonObj.keys();
                    while (temp.hasNext()) {
                        String key = temp.next();
                        String value = jsonObj.get(key).toString();

                        //Log.d("keyVal", key + " - " + value);

                        int idKey = res.getIdentifier(key, "id", context.getPackageName());

                        if (value.isEmpty()) {
                            if (!key.equals("d1") && !key.equals("d2") && !key.equals("d3") ) {
                                remoteViews.setInt(idKey, "setBackgroundColor", ContextCompat.getColor(context, R.color.gm0));
                            }
                            remoteViews.setTextViewText(idKey, "");
                            continue;
                        }

                        remoteViews.setTextViewText(idKey, value);
                        if (!key.equals("d1") && !key.equals("d2") && !key.equals("d3") ) {
                            String colorClass = "gm" + value.toString();
                            int idColor = res.getIdentifier(colorClass, "color", context.getPackageName());
                            remoteViews.setInt(idKey, "setBackgroundColor", ContextCompat.getColor(context, idColor));
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);


            Toast.makeText(context, R.string.data_updated, Toast.LENGTH_SHORT).show();

        } else {

        }
    }


}

