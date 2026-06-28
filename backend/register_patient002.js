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

// 注册患者：李四
async function registerPatient() {
  const body = JSON.stringify({
    username: 'patient002',
    password: '123456',
    name: '李四',
    phone: '13800000003',
    idCard: '210000199801010032',
    gender: '女',
    age: 28
  });

  const result = await request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/patient/register',
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
  }, body);

  console.log('注册患者:', JSON.stringify(result, null, 2));
  return result.data;
}

(async () => {
  console.log('=== 注册新患者：李四 ===');
  const result = await registerPatient();

  if (result.code === 200) {
    console.log('✅ 患者注册成功！');
    console.log('账号: patient002');
    console.log('密码: 123456');
  } else {
    console.log('❌ 注册失败:', result.message);
  }
})();
