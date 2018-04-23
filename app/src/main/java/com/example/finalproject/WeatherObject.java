package com.example.finalproject;

public class WeatherObject
{
    private String weatherType;

    private int tempLow;
    private int tempHigh;

    private String timeWeatherWasPulled;

    private int windMax;
    private int windAvg;

    private int humidityAvg;

    private String forecast;

    public WeatherObject()
    { }

    public WeatherObject(String weatherType, int tempLow, int tempHigh, java.lang.String timeWeatherWasPulled, int windMax, int windAvg, int humidityAvg)
    {
        this.weatherType = weatherType;
        this.tempLow = tempLow;
        this.tempHigh = tempHigh;
        this.timeWeatherWasPulled = timeWeatherWasPulled;
        this.windMax = windMax;
        this.windAvg = windAvg;
        this.humidityAvg = humidityAvg;
    }

    public String getForecast()
    {
        return forecast;
    }

    public void setForecast(String forecast)
    {
        this.forecast = forecast;
    }

    public String getWeatherType()
    {
        return weatherType;
    }

    public void setWeatherType(String weatherType)
    {
        this.weatherType = weatherType;
    }

    public int getTempLow()
    {
        return tempLow;
    }

    public void setTempLow(int tempLow)
    {
        this.tempLow = tempLow;
    }

    public int getTempHigh()
    {
        return tempHigh;
    }

    public void setTempHigh(int tempHigh)
    {
        this.tempHigh = tempHigh;
    }

    public java.lang.String getTimeWeatherWasPulled()
    {
        return timeWeatherWasPulled;
    }

    public void setTimeWeatherWasPulled(java.lang.String timeWeatherWasPulled)
    {
        this.timeWeatherWasPulled = timeWeatherWasPulled;
    }

    public int getWindMax()
    {
        return windMax;
    }

    public void setWindMax(int windMax)
    {
        this.windMax = windMax;
    }

    public int getWindAvg()
    {
        return windAvg;
    }

    public void setWindAvg(int windAvg)
    {
        this.windAvg = windAvg;
    }

    public int getHumidityAvg()
    {
        return humidityAvg;
    }

    public void setHumidityAvg(int humidityAvg)
    {
        this.humidityAvg = humidityAvg;
    }
}
