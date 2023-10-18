package uz.klimuz.weatherclock;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView dateTextVew;
    private TextView weekDayTextVew;
    private TextView timeTextVew;
    private TextView tempTextView;
    private Button updateButton;
    private ImageView imageView;
    private ImageView imageView2;
    private TextView temp2TextView;
    private Button backwardButton;
    private Button forwardButton;
    private TextView forecastTextView;
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
    private ArrayList<String> weatherInfo = new ArrayList();
    private String currencyInfo = "";

    int warmColor;
    int coldColor;
    int greenColor;
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
        tempTextView = findViewById(R.id.tempTextView);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);
        temp2TextView = findViewById(R.id.temp2TextView);
        backwardButton = findViewById(R.id.backwardButton);
        forwardButton = findViewById(R.id.forwardButton);
        forecastTextView = findViewById(R.id.forecastTextView);
        updateButton = findViewById(R.id.button);
        celsTextView = findViewById(R.id.celsTextView);
        cels2TextView = findViewById(R.id.cels2TextView);
        batteryLevelTextView = findViewById(R.id.batteryLevelTextView);
        chargingImageView = findViewById(R.id.chargingImageView);

        currencyTextView = findViewById(R.id.currencyTextView);

        timeUpdate();
        updateWeather();
        registerBatteryLevelReceiver();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWeather();
            }
        });
        backwardButton.setEnabled(false);
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counterForecast -= 3;
                drawForecast();
                if (counterForecast <= 3){
                    backwardButton.setEnabled(false);
                } else {
                    backwardButton.setEnabled(true);
                }
                forwardButton.setEnabled(true);
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counterForecast += 3;
                drawForecast();
                if (counterForecast >= 24){
                    forwardButton.setEnabled(false);
                } else {
                    forwardButton.setEnabled(true);
                }
                backwardButton.setEnabled(true);
            }
        });
    }

    private void registerBatteryLevelReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(battery_receiver, filter);
    }

    private BroadcastReceiver battery_receiver = new BroadcastReceiver() {
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

                if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB){
                    chargingImageView.setImageResource(R.drawable.battery_charging);
                    batteryLevelTextView.setTextColor(greenColor);
                }else {
                    chargingImageView.setImageResource(R.drawable.battery_alert);
                    batteryLevelTextView.setTextColor(warmColor);
                }
                String batteryLevelText = level + "%";
                batteryLevelTextView.setText(batteryLevelText);
            }

        }
    };

    private void drawForecast(){
        if (weatherInfo.size() > counterForecast + 3) {
            String forecastText = "In " + counterForecast + " hours";
            forecastTextView.setText(forecastText);
            String temp2 = weatherInfo.get(counterForecast * 2);
            String temp2Substring = temp2.substring(1, 3);
            StringBuilder digits = new StringBuilder();
            for (int i = 0; i < temp2Substring.length(); i++){
                char a = temp2Substring.charAt(i);
                if (Character.isDigit(a)){
                    digits.append(a);
                }
            }
            if (digits.toString().length() > 0) {
                if (Integer.parseInt(digits.toString()) > 25 && temp2.contains("+")) {
                    temp2TextView.setTextColor(warmColor);
                    cels2TextView.setTextColor(warmColor);
                } else {
                    temp2TextView.setTextColor(coldColor);
                    cels2TextView.setTextColor(coldColor);
                }
            }
            temp2TextView.setText(temp2);
            imageView2 = mapImage(imageView2, weatherInfo.get(counterForecast * 2 + 1));
        }
    }
    private ImageView mapImage(ImageView imageViewToMap, String imageCode){
        switch (imageCode) {
            case "skc_n":
                imageViewToMap.setImageResource(R.drawable.skc_n);
                break;
            case "skc_d":
                imageViewToMap.setImageResource(R.drawable.skc_d);
                break;
            case "bkn_n":
                imageViewToMap.setImageResource(R.drawable.bkn_n);
                break;
            case "bkn_d":
                imageViewToMap.setImageResource(R.drawable.bkn_d);
                break;
            case "ovc_sn":
                imageViewToMap.setImageResource(R.drawable.ovc_sn);
                break;
            case "ovc_m_sn":
                imageViewToMap.setImageResource(R.drawable.ovc_m_sn);
                break;
            case "ovc_m_ra":
                imageViewToMap.setImageResource(R.drawable.ovc_m_ra);
                break;
            case "ovc":
                imageViewToMap.setImageResource(R.drawable.ovc);
                break;
            case "ovc_ra_sn":
                imageViewToMap.setImageResource(R.drawable.ovc_ra_sn);
                break;
            case "ovc_ra":
                imageViewToMap.setImageResource(R.drawable.ovc_ra);
                break;
            case "bkn_p_ra_d":
                imageViewToMap.setImageResource(R.drawable.bkn_p_ra_d);
                break;
            case "bkn_p_ra_n":
                imageViewToMap.setImageResource(R.drawable.bkn_p_ra_n);
                break;
            default:
                imageViewToMap.setImageResource(R.drawable.unknown);
        }
        return imageViewToMap;
    }

    private void updateWeather() {
        counterTime = 0;

        final Handler weatherHandler = new Handler();
        new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            weatherInfo = WeatherParser.mainMethod();
                            currencyInfo = CurrencyParser.findOutCourse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        weatherHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (weatherInfo.size() > 0) {
                                    tempTextView.setText(weatherInfo.get(0));
                                    StringBuilder digits = new StringBuilder();
                                    for (int i = 0; i < weatherInfo.get(0).length(); i++){
                                        char a = weatherInfo.get(0).charAt(i);
                                        if (Character.isDigit(a)){
                                            digits.append(a);
                                        }
                                    }
                                    if (digits.toString().length() > 0) {
                                        if (Integer.parseInt(digits.toString()) > 25 && weatherInfo.get(0).contains("+")) {
                                            tempTextView.setTextColor(warmColor);
                                            celsTextView.setTextColor(warmColor);
                                        } else {
                                            tempTextView.setTextColor(coldColor);
                                            celsTextView.setTextColor(coldColor);
                                        }
                                    }
                                    imageView = mapImage(imageView, weatherInfo.get(1));
                                    currencyTextView.setText(currencyInfo);
                                    drawForecast();
                                }
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
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                timeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Calendar calendar = Calendar.getInstance();

                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        String dayOfMonthString = "";
                        if (dayOfMonth < 10){
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
                        if (hours < 10){
                            hoursString = 0 + String.valueOf(hours);
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        minutes = calendar.get(Calendar.MINUTE);
                        String minutesString = "";
                        if (minutes < 10){
                            minutesString = 0 + String.valueOf(minutes);
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String timeText = hoursString + " : " + minutesString;
                        timeTextVew.setText(timeText);
                        counterTime++;
                        if (counterTime == 600){
                            updateWeather();
                        }
                        timeUpdate();
                    }
                });
            }
        }).start();
    }
}