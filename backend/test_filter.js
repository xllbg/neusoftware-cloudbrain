const http = require('http');

function get(url) {
  return new Promise((resolve) => {
    http.get(url, (res) => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve(JSON.parse(d).data.total));
    });
  });
}

(async () => {
  console.log('科室筛选(内科):', await get('http://localhost:8080/api/registration/doctor/list?doctorId=6&department=内科&page=1&size=10'));
  console.log('状态筛选(completed):', await get('http://localhost:8080/api/registration/doctor/list?doctorId=6&status=completed&page=1&size=10'));
  console.log('状态筛选(pending):', await get('http://localhost:8080/api/registration/doctor/list?doctorId=6&status=pending&page=1&size=10'));
  console.log('状态筛选(in_progress):', await get('http://localhost:8080/api/registration/doctor/list?doctorId=6&status=in_progress&page=1&size=10'));
  console.log('日期筛选(2026-06-25):', await get('http://localhost:8080/api/registration/doctor/list?doctorId=6&date=2026-06-25&page=1&size=10'));
  console.log('组合筛选(内科+completed):', await get('http://localhost:8080/api/registration/doctor/list?doctorId=6&department=内科&status=completed&page=1&size=10'));
})();
