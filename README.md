Các bước migrate
1. Tải xuống project Katalon:
   https://github.com/dr-joy/katalon-testing-tool.git
2. Tải xuống và nghiên cứu qua về cách hoạt động của Testscript tính lương trong file này:
   https://docs.google.com/spreadsheets/d/1nt0gWY_XV4V5LCvNC-ohnR8Z3vNc6snPzkzP8jLwcnw/edit?gid=1510629262#gid=1510629262
3. Các hàm cần migrate code:
   Tất cả hàm có trong AttendancePayrollKeywords
   *Note: Có thể sử dụng chat GPT để tăng tốc độ code

Environment variable
env=jackfruit;
browser=chrome;
headless=false;

default: localhost:8080
POST: /api/selenium/process_attendance_steps
{
"phaseStart": 1,
"phaseEnd":1,
"removeAllCheckingLog" : false,
"addAllWorkingTimeType" : false,
"addAllPreset" : false,
"addWorkSchedule" : true,
"addAllCheckingLogs" : false,
"approveAllRequest" : false,
"rejectAllRequest" : false,
"removeAllDownloadTemplate" : false,
"createNewDownloadTemplate" : false,
"downloadTemplate" : false
}
