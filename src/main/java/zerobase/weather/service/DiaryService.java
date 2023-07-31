package zerobase.weather.service;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class DiaryService {
    @Value("${openWeatherMap.key}")
    private String apiKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherApplication.class);
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }
    @Transactional
    @Scheduled(cron = " 0 0 1  * * *")
    public void createDateWeather(){
        dateWeatherRepository.save(getDateWeather());
    }
    public DateWeather getDateWeatherApi(){
        /* openApi를 이용해 날씨 정보 가져오기 */

        String weatherInfo = getWeatherString();

        HashMap<String,Object> map = JsonParseString(weatherInfo);

        DateWeather dateWeather = new DateWeather();
        dateWeather.setWeather(map.get("main").toString());
        dateWeather.setDate(LocalDate.now());
        dateWeather.setIcon(map.get("icon").toString());
        dateWeather.setTemperature((Double)map.get("temp"));

        return dateWeather;
    }
    public void createDiary(LocalDate date, String text){
        LOGGER.info("start to create diary>"+date);
        DateWeather dateWeather = getDateWeather();

        Diary newDiary = new Diary();
        newDiary.setDateInfo(dateWeather);
        newDiary.setText(text);

        diaryRepository.save(newDiary);
        LOGGER.info("end to create diary");
    }

    private DateWeather getDateWeather(){
        DateWeather dateWeather = new DateWeather();

        List<DateWeather> dateWeatherList = dateWeatherRepository.findAllByDate(LocalDate.now());
        if(dateWeatherList.size() == 0){
            return getDateWeatherApi();
        }else{
            return dateWeatherList.get(0);
        }
    }
    private String getWeatherString(){
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid="+apiKey;
        System.out.println(apiUrl);

        try {
            //url 클래스의 openConnection 메소드를 통해 HttpURLConnection 인스턴스를 반환
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            BufferedReader br;

            if(responseCode == 200){
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String str;
            StringBuilder sb = new StringBuilder();
            while((str = br.readLine()) != null){
                sb.append(str);
            }
            br.close();
            return sb.toString();

        } catch (Exception e) {
            return "fail getWeatherString";
        }

    }

    private HashMap<String,Object> JsonParseString(String weatherInfo){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try{
            jsonObject = (JSONObject) jsonParser.parse(weatherInfo);
        }catch(ParseException e){
            throw new RuntimeException(e);
        }
        JSONObject main = (JSONObject) jsonObject.get("main");
        JSONArray weatherArr = (JSONArray) jsonObject.get("weather");
        JSONObject weather = (JSONObject) weatherArr.get(0);

        HashMap<String,Object> map = new HashMap<>();
        map.put("main",weather.get("main"));
        map.put("icon",weather.get("icon"));
        map.put("temp",main.get("temp"));

        return map;
    }

    public List<Diary> readDiary(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate,endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary updateDiary = diaryRepository.getFirstByDate(date);
        updateDiary.setText(text);
        diaryRepository.save(updateDiary);
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
