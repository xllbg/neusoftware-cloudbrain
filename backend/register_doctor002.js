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

// 注册医生
async function registerDoctor() {
  const body = JSON.stringify({
    username: 'doctor002',
    password: '123456',
    name: '王医生',
    phone: '13900000005',
    gender: '男',
    age: 35,
    department: '心内科',
    title: '副主任医师',
    hospital: '智慧云脑医院'
  });

  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/doctor/register',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
  }, body);

  console.log('注册医生:', JSON.stringify(result, null, 2));
  return result.data;
}

(async () => {
  console.log('=== 注册新医生 ===');
  const result = await registerDoctor();

  if (result.code === 200) {
    console.log('✅ 医生注册成功！');
    console.log('账号: doctor002');
    console.log('密码: 123456');
    console.log('姓名: 王医生');
    console.log('科室: 心内科');
    console.log('\n注意：医生账号需要管理员审核通过后才能登录！');
  } else {
    console.log('❌ 注册失败:', result.message);
  }
})();
