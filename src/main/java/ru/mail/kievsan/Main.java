package ru.mail.kievsan;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Date;

public class Main {

    static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    static final String ACCESS_HEADER = "X-Yandex-Weather-Key";
    static final String ACCESS_KEY = "your-key";
    static final String ERR = "Error HTTP request";
    static final Gson GSON = new Gson();

    static String mode = "HttpClient";

    public static void main(String[] args) {

        System.out.printf("Yandex weather API. %s%nUsed: %s%n", API_URL, mode);
        String response;
        try {
            response = getYandexWeatherResponse(API_URL, "lat=55.75&lon=37.62&limit=3");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Response Body: " + response);

        Temperature temperature = getTemperature(response);

        System.out.printf(
                "%s, фактическая температура :\t%d град. по Цельсию %n",
                temperature.getDate(), temperature.getFactTemperature());
        System.out.printf(
                "В ближайшие %d дн. ожидается среднесуточная температура:\t%.1f град. по Цельсию %n",
                temperature.getForecastLimit(), temperature.getAvgTemperature());
    }

    public static String getYandexWeatherResponse(String apiUrl, String apiProperties) throws Exception {
        final String GET_REQUEST = "%s?%s".formatted(apiUrl, apiProperties);
        if (mode.equals("HttpClient")) return byHttpClient(URI.create(GET_REQUEST));
        return byHttpURLConnection(new URL(GET_REQUEST));
    }

    public static String byHttpURLConnection(URL weatherUrl) throws Exception {
        try {
            HttpURLConnection connection = (HttpURLConnection) weatherUrl.openConnection();
            connection.setRequestProperty(ACCESS_HEADER, ACCESS_KEY);
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
            return response.toString();

        } catch (Exception e) {
            throw new Exception("%s: %s".formatted(ERR, e.getMessage()));
        }
    }

    public static String byHttpClient(URI weatherUri) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(weatherUri)
                .headers(ACCESS_HEADER, ACCESS_KEY)
                .headers("Content-Type","application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2 ) {
                throw new Exception("%s: response status %d".formatted(ERR, response.statusCode()));
            }
            return response.body();
        } catch (Exception e) {
            throw new Exception("%s: %s".formatted(ERR, e.getMessage()));
        }
    }

    static Temperature getTemperature(String yandexWeatherResponse) {
        return GSON.fromJson(yandexWeatherResponse, Temperature.class);
    }

    static class Temperature {
        private final Date now_dt;
        private final Fact fact;
        private final Forecast[] forecasts;

        Temperature(Date nowDt, Fact fact, Forecast[] forecasts) {
            now_dt = nowDt;
            this.fact = fact;
            this.forecasts = forecasts;
        }

        public Date getDate() {
            return this.now_dt;
        }

        public int getForecastLimit() {
            return this.forecasts.length;
        }

        public int getFactTemperature() {
            return this.fact.temp;
        }

        public double getAvgTemperature() {
            return Arrays.stream(this.forecasts)
                    .map(Forecast::getAvgTemperature)
                    .reduce(Double::sum)
                    .orElse(0.0)
                    / getForecastLimit();
        }

        @Override
        public String toString() {
            return GSON.toJson(this);
        }

        private static class Fact {
            int temp;
        }

        private static class Forecast {
            Date date;
            Hour[] hours;

            double getAvgTemperature() {
                return Arrays.stream(this.hours)
                        .map(hour -> hour.temp)
                        .reduce(Integer::sum)
                        .orElse(0)
                        / (double) this.hours.length;
            }


        }

        private static class Hour {
            String hour;
            int temp;
        }
    }
}
