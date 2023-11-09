package uz.klimuz.weatherclock;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView dateTextVew;
    private TextView weekDayTextVew;
    private TextView timeTextVew;
    private TextView weatherTempTextView;
    private Button updateButton;
    private ImageView weatherImageView;
    private ImageView forecastImageView;
    private TextView forecastTempTextView;
    private Button backwardButton;
    private Button forwardButton;
    private TextView forecastTimeTextView;
    private TextView celsTextView;
    private TextView cels2TextView;
    private TextView batteryLevelTextView;
    private ImageView chargingImageView;

    private int month;
    private int weekDay;
    private int hours;
    private int minutes;
    private int counterTime;
    private int counterForecast = 3;

    private TextView currencyTextView;


    /*weatherInfo
         00-temp current;
         01-image current;
         02-temp 1hour;
         03-image 1hour;
         04-temp 2hour;
         05-image 2hour;
         06-temp 3hour;
         07-image 3hour;
         08-etc; */
    private ArrayList<ForecastHour> forecastsList = new ArrayList<>();
    private String currencyInfo = "";
    private final String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=Tashkent&appid=ccfc57acc1e76e2bb422580207a1a1ed&units=metric";
    private final String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?q=Tashkent&appid=ccfc57acc1e76e2bb422580207a1a1ed&units=metric";
    private static String iconCode = "";
    public static String forecastIconCode = "";

    public static ArrayList<String> currentWeather = new ArrayList<>();

    public static int warmColor;
    public static int coldColor;
    public static int greenColor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        warmColor = resources.getColor(R.color.red);
        coldColor = resources.getColor(R.color.purple_700);
        greenColor = resources.getColor(R.color.green);

        dateTextVew = findViewById(R.id.dateTextView);
        weekDayTextVew = findViewById(R.id.weekDayTextView);
        timeTextVew = findViewById(R.id.timeTextView);
        weatherTempTextView = findViewById(R.id.tempTextView);
        weatherImageView = findViewById(R.id.weatherImageView);
        forecastImageView = findViewById(R.id.forecastImageView);
        forecastTempTextView = findViewById(R.id.forecastTempTextView);
        backwardButton = findViewById(R.id.backwardButton);
        forwardButton = findViewById(R.id.forwardButton);
        forecastTimeTextView = findViewById(R.id.forecastTextView);
        updateButton = findViewById(R.id.button);
        celsTextView = findViewById(R.id.celsTextView);
        cels2TextView = findViewById(R.id.cels2TextView);
        batteryLevelTextView = findViewById(R.id.batteryLevelTextView);
        chargingImageView = findViewById(R.id.chargingImageView);
        currencyTextView = findViewById(R.id.currencyTextView);
        timeUpdate();
        try {
            updateWeatherAndCurrency();
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerBatteryLevelReceiver();
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updateWeatherAndCurrency();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        backwardButton.setEnabled(false);
        String forecastTime = "In %d hours";
        forecastTimeTextView.setText(String.format(forecastTime, counterForecast));
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counterForecast -= 3;
                drawForecast();
                backwardButton.setEnabled(counterForecast > 3);
                forwardButton.setEnabled(true);
                forecastTimeTextView.setText(String.format(forecastTime, counterForecast));
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counterForecast += 3;
                drawForecast();
                forwardButton.setEnabled(counterForecast < 24);
                backwardButton.setEnabled(true);
                forecastTimeTextView.setText(String.format(forecastTime, counterForecast));
            }
        });
    }

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(battery_receiver, filter);
    }

    private final BroadcastReceiver battery_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPresent = intent.getBooleanExtra("present", false);
            int plugged = intent.getIntExtra("plugged", -1);
            int scale = intent.getIntExtra("scale", -1);
            int rawlevel = intent.getIntExtra("level", -1);
            int level = 0;
            if (isPresent) {
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                    chargingImageView.setImageResource(R.drawable.battery_charging);
                    batteryLevelTextView.setTextColor(greenColor);
                } else {
                    chargingImageView.setImageResource(R.drawable.battery_alert);
                    batteryLevelTextView.setTextColor(warmColor);
                }
                String batteryLevelText = level + "%";
                batteryLevelTextView.setText(batteryLevelText);
            }

        }
    };
    private void drawForecast() {
        DownloadForecastJSONTask downloadForecastJSONTask = new DownloadForecastJSONTask();
        downloadForecastJSONTask.execute(forecastUrl);

    }
    private ImageView mapImage(ImageView imageViewToMap, String imageCode) {
        switch (imageCode) {
            case "01n":
                imageViewToMap.setImageResource(R.drawable.skc_n);
                break;
            case "01d":
                imageViewToMap.setImageResource(R.drawable.skc_d);
                break;
            case "02n":
            case "04n":
                imageViewToMap.setImageResource(R.drawable.bkn_n);
                break;
            case "02d":
            case "04d":
                imageViewToMap.setImageResource(R.drawable.bkn_d);
                break;
            case "13d":
            case "13n":
                imageViewToMap.setImageResource(R.drawable.ovc_sn);
                break;
            case "10d":
            case "10n":
                imageViewToMap.setImageResource(R.drawable.ovc_m_ra);
                break;
            case "03d":
            case "03n":
                imageViewToMap.setImageResource(R.drawable.ovc);
                break;
            case "50d":
            case "50n":
                imageViewToMap.setImageResource(R.drawable.mist);
                break;
            case "09d":
            case "09n":
                imageViewToMap.setImageResource(R.drawable.ovc_ra);
                break;
            case "11d":
                imageViewToMap.setImageResource(R.drawable.bkn_p_ra_d);
                break;
            case "11n":
                imageViewToMap.setImageResource(R.drawable.bkn_p_ra_n);
                break;
            default:
                imageViewToMap.setImageResource(R.drawable.unknown);
        }
        return imageViewToMap;
    }
    private void updateWeatherAndCurrency() throws IOException {
        counterTime = 0;
        DownloadWeatherJSONTask downloadWeatherJSONTask = new DownloadWeatherJSONTask();
        downloadWeatherJSONTask.execute(weatherUrl);
        drawForecast();
        final Handler currencyHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currencyInfo = CurrencyParser.findOutCourse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currencyHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        currencyTextView.setText(currencyInfo);
                    }
                });
            }
        }).start();
    }
    private void timeUpdate() {
        final Handler timeHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        String dayOfMonthString = "";
                        if (dayOfMonth < 10) {
                            dayOfMonthString = 0 + String.valueOf(dayOfMonth);
                        } else {
                            dayOfMonthString = String.valueOf(dayOfMonth);
                        }

                        month = calendar.get(Calendar.MONTH);
                        String monthString = "";
                        switch (month) {
                            case 0:
                                monthString = "Jan";
                                break;
                            case 1:
                                monthString = "Feb";
                                break;
                            case 2:
                                monthString = "Mar";
                                break;
                            case 3:
                                monthString = "Apr";
                                break;
                            case 4:
                                monthString = "May";
                                break;
                            case 5:
                                monthString = "Jun";
                                break;
                            case 6:
                                monthString = "Jul";
                                break;
                            case 7:
                                monthString = "Aug";
                                break;
                            case 8:
                                monthString = "Sep";
                                break;
                            case 9:
                                monthString = "Oct";
                                break;
                            case 10:
                                monthString = "Nov";
                                break;
                            case 11:
                                monthString = "Dec";
                                break;
                        }
                        String dateText = dayOfMonthString + "-" + monthString
                                + "-" + calendar.get(Calendar.YEAR);
                        dateTextVew.setText(dateText);

                        weekDay = calendar.get(Calendar.DAY_OF_WEEK);
                        String weekDayString = "";
                        switch (weekDay) {
                            case 1:
                                weekDayString = "Sunday";
                                break;
                            case 2:
                                weekDayString = "Monday";
                                break;
                            case 3:
                                weekDayString = "Tuesday";
                                break;
                            case 4:
                                weekDayString = "Wednesday";
                                break;
                            case 5:
                                weekDayString = "Thursday";
                                break;
                            case 6:
                                weekDayString = "Friday";
                                break;
                            case 7:
                                weekDayString = "Saturday";
                                break;
                        }
                        weekDayTextVew.setText(weekDayString);
                        hours = calendar.get(Calendar.HOUR_OF_DAY);
                        String hoursString = "";
                        if (hours < 10) {
                            hoursString = 0 + String.valueOf(hours);
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        minutes = calendar.get(Calendar.MINUTE);
                        String minutesString = "";
                        if (minutes < 10) {
                            minutesString = 0 + String.valueOf(minutes);
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String timeText = hoursString + " : " + minutesString;
                        timeTextVew.setText(timeText);
                        counterTime++;
                        if (counterTime == 1800) {
                            try {
                                updateWeatherAndCurrency();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        timeUpdate();
                    }
                });
            }
        }).start();
    }
    private class DownloadWeatherJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new  URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null){
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray weather = jsonObject.getJSONArray("weather");
                JSONObject list = weather.getJSONObject(0);
                String icon = list.getString("icon");
                JSONObject main = jsonObject.getJSONObject("main");
                int tempRounded = (int) Math.round(Double.valueOf(main.getString("temp")));
                String currentTemperature = "";
                if (tempRounded > 0){
                    currentTemperature = "+%d";
                }
                if (tempRounded > 25){
                    weatherTempTextView.setTextColor(warmColor);
                    celsTextView.setTextColor(warmColor);
                }else {
                    weatherTempTextView.setTextColor(coldColor);
                    celsTextView.setTextColor(coldColor);
                }
                weatherTempTextView.setText(String.format(currentTemperature, tempRounded));
                mapImage(weatherImageView, icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class DownloadForecastJSONTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new  URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line =reader.readLine();
                while (line != null){
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray list = jsonObject.getJSONArray("list");
                for (int a = 0; a <= 16; a++){
                    JSONObject forecastItem = list.getJSONObject(a);
                    JSONArray weather = forecastItem.getJSONArray("weather");
                    JSONObject condition = weather.getJSONObject(0);
                    String iconCode = condition.getString("icon");
                    JSONObject main = forecastItem.getJSONObject("main");
                    int tempRounded = (int) Math.round(Double.parseDouble(main.getString("temp")));
                    String date = forecastItem.getString("dt_txt");
                    int day = Integer.parseInt(date.substring(8, 10));
                    int hour = Integer.parseInt(date.substring(11,13));
                    ForecastHour forecastHour = new ForecastHour(day, hour, tempRounded, iconCode);
                    forecastsList.add(forecastHour);
                }
                Calendar calendar = Calendar.getInstance();
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                hours = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfMonthStamp = 0;
                int timeStamp = 0;
                if (hours + counterForecast < 6 && hours + counterForecast >= 3){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 0;
                }else if (hours + counterForecast < 9 && hours + counterForecast >= 6){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 3;
                }else if (hours + counterForecast < 12 && hours + counterForecast >= 9){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 6;
                }else if (hours + counterForecast < 15 && hours + counterForecast >= 12){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 9;
                }else if (hours + counterForecast < 18 && hours + counterForecast >= 15){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 12;
                }else if (hours + counterForecast < 21 && hours + counterForecast >= 18){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 15;
                }else if (hours + counterForecast < 24 && hours + counterForecast >= 21){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 18;
                }else if (hours + counterForecast < 27 && hours + counterForecast >= 24){
                    dayOfMonthStamp = dayOfMonth;
                    timeStamp = 21;
                }else if (hours + counterForecast < 30 && hours + counterForecast >= 27){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 0;
                }else if (hours + counterForecast < 33 && hours + counterForecast >= 30){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 3;
                }else if (hours + counterForecast < 36 && hours + counterForecast >= 33){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 6;
                }else if (hours + counterForecast < 39 && hours + counterForecast >= 36){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 9;
                }else if (hours + counterForecast < 42 && hours + counterForecast >= 39){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 12;
                }else if (hours + counterForecast < 45 && hours + counterForecast >= 42){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 15;
                }else if (hours + counterForecast < 48 && hours + counterForecast >= 45){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 18;
                }else if (hours + counterForecast >= 48){
                    dayOfMonthStamp = dayOfMonth + 1;
                    timeStamp = 21;
                }
                for (ForecastHour forecastHour : forecastsList){
                    if (forecastHour.getDate() == dayOfMonthStamp && forecastHour.getTime() == timeStamp){
                        mapImage(forecastImageView, forecastHour.getIconCode());
                        forecastTempTextView.setText(String.valueOf(forecastHour.getTemperature()));
//                        String test = "date:%d, time%d , temp:%d, code %s";
//                        Log.i("myValue", String.format(test,forecastHour.getDate(), forecastHour.getTime(),forecastHour.getTemperature(), forecastHour.getIconCode()));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}