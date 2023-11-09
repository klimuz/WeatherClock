package uz.klimuz.weatherclock;

public class ForecastHour {
    private final int date;
    private final int time;
    private final int temperature;
    private final String iconCode;

    public int getDate() {
        return date;
    }

    public int getTime() {
        return time;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getIconCode() {
        return iconCode;
    }

    public ForecastHour(int date, int time, int temperature, String iconCode){
        this.date = date;
        this.time = time;
        this.temperature = temperature;
        this.iconCode = iconCode;


    }
}
