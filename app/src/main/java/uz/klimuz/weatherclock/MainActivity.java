package uz.klimuz.weatherclock;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
    private Button button;
    private ImageView imageView;
    private ImageView imageView2;
    private TextView temp2TextView;
    private Button backwardButton;
    private Button forwardButton;
    private TextView forecastTextView;

    private int month;
    private int weekDay;
    private int hours;
    private int minutes;
    private int counterTime;
    private int counterForecast = 3;

    public static ImageView powerImageView;
    public static TextView batteryPercentageTextView;


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



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

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
        button = findViewById(R.id.button);

        powerImageView = findViewById(R.id.powerImageView);
        batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView);

        timeUpdate();
        updateWeather();

        button.setOnClickListener(new View.OnClickListener() {
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

    private void drawForecast(){
        if (weatherInfo.size() > counterForecast + 3) {
            forecastTextView.setText("In " + counterForecast + " hours");
            temp2TextView.setText(weatherInfo.get(counterForecast * 2));
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
                            Parser parser = new Parser();
                            weatherInfo = parser.mainMethod();

                            int index = 0;
                            for (String string : weatherInfo){
                                Log.i("arrayMain:", index + " " + string);
                                index ++;

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        tempTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (weatherInfo.size() > 0) {
                                    tempTextView.setText(weatherInfo.get(0));
                                    imageView = mapImage(imageView, weatherInfo.get(1));
                                    drawForecast();

                                }else return;
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
                       // PowerConnectionReceiver powerConnectionReceiver = new  PowerConnectionReceiver();

                     //   powerConnectionReceiver.onReceive(Context, new Intent());
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
                        dateTextVew.setText(dayOfMonthString + "-" + monthString
                                + "-" + calendar.get(Calendar.YEAR));

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
                        timeTextVew.setText(hoursString + " : " + minutesString);
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