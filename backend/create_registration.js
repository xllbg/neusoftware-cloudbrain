const http = require('http');

function request(options, postData) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(data) });
        } catch (e) {
          resolve({ status: res.statusCode, data: data });
        }
      });
    });
    req.on('error', reject);
    if (postData) {
      req.write(postData);
    }
    req.end();
  });
}

// 创建挂号（POST /api/registration/create）
async function createRegistration() {
  const body = JSON.stringify({
    patientId: 2,
    doctorId: 6,
    department: '内科',
    registrationDate: '2026-06-25',
    timeSlot: '上午 9:00-10:00'
  });

  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/registration/create',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(body)
    }
  }, body);

  console.log('创建挂号:', JSON.stringify(result, null, 2));
  return result.data;
}

// 获取患者的挂号列表（GET /api/registration/list?patientId=2）
async function getPatientRegistrations() {
  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/registration/list?patientId=2',
    method: 'GET',
    headers: {}
  });

  console.log('\n患者挂号列表:', JSON.stringify(result, null, 2));
  return result.data;
}

// 获取医生的挂号列表（GET /api/registration/doctor/list?doctorId=6）
async function getDoctorRegistrations() {
  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/registration/doctor/list?doctorId=6&page=1&size=10',
    method: 'GET',
    headers: {}
  });

  console.log('\n医生挂号列表:', JSON.stringify(result, null, 2));
  return result.data;
}

(async () => {
  console.log('=== 创建挂号记录 ===');
  await createRegistration();

  console.log('\n=== 查看患者自己的挂号 ===');
  await getPatientRegistrations();

  console.log('\n=== 查看医生的挂号列表 ===');
  await getDoctorRegistrations();
})();
