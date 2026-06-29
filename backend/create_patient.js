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

// 注册患者
async function registerPatient() {
  const body = JSON.stringify({
    username: 'patient001',
    password: '123456',
    name: '张三',
    phone: '13800138001',
    idCard: '110101199001011234',
    gender: '男',
    age: 30
  });

  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/patient/register',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
  }, body);

  console.log('注册患者:', JSON.stringify(result));
  return result.data?.data;
}

// 患者登录
async function loginPatient() {
  const body = JSON.stringify({
    username: 'patient001',
    password: '123456'
  });

  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/patient/login',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
  }, body);

  console.log('患者登录:', JSON.stringify(result));
  return result.data?.data;
}

// 医生登录
async function loginDoctor() {
  const body = JSON.stringify({
    name: 'DoctorWang',
    phone: '13900000002',
    password: '123456'
  });

  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/doctor/login/phone',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
  }, body);

  console.log('医生登录:', JSON.stringify(result));
  return result.data?.data;
}

(async () => {
  // 先测试患者登录
  console.log('=== 测试患者登录 ===');
  const patientLogin = await loginPatient();
  console.log('患者登录结果:', patientLogin);

  // 如果登录失败，尝试注册
  if (!patientLogin?.token) {
    console.log('\n=== 注册新患者 ===');
    await registerPatient();

    console.log('\n=== 再次测试患者登录 ===');
    await loginPatient();
  }

  // 医生登录
  console.log('\n=== 医生登录 ===');
  const doctorLogin = await loginDoctor();
  console.log('医生登录结果:', doctorLogin);
})();
