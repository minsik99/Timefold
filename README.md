# Timefold

java version : 21

# 요청과 응답에 사용되는 데이터

## requestJson
```
{
  "maxNurses": 30,
  "startDate": "2025-07-01", 
  "endDate": "2025-07-31",
  "nurseList": [
    {
      "nurseId": 1,
      "name": "김간호",
      "experience": 5,
      "isNightKeep": 0,
      "requests": [
        {
          "reqId": 1,
          "reqDate": "2025-07-15", 
          "desiredShift": "Day"
        }
      ]
    }
  ]
}
```

## responseJson
```
[
  {
    "schId": 1,
    "shiftDate": "2025-07-01",
    "nurseId": 1,
    "shiftType": "Day" // Day, Evening, Night, Off
  }
]
```
