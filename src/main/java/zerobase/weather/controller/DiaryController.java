package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        //controller가 생성될 때 이 생성자에다가 service를 가져오도록 만듬
        this.diaryService = diaryService;
    }
    @ApiOperation(value="일기 텍스트와 날씨를 이용해서 DB에 저장",notes = "일기를 저장합니다.")
    @PostMapping("/create/diary")
    void createDiary(
            //?date=2023-07-24
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate date,
            @RequestBody String text){

        diaryService.createDiary(date,text);


    }

    @GetMapping("/read/diary")
    public List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        return diaryService.readDiary(date);
    }
    @GetMapping("/read/diaries")
    public List<Diary> readDiaries(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 기간의 첫 번째 날", example = "2023-07-28") LocalDate startDate,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "조회할 기간의 마지막 날", example = "2023-07-28") LocalDate endDate
        ){
        return diaryService.readDiaries(startDate,endDate);
    }
    @PutMapping("/update/diary")
    void updateDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date,
                     @RequestBody String text){
        diaryService.updateDiary(date,text);
    }

    @DeleteMapping("/delete/diary")
    void deleteDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        diaryService.deleteDiary(date);
    }
}
