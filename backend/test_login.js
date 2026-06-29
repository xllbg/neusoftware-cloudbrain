const http = require('http');

const body = JSON.stringify({
  name: '王明',
  phone: '13800138001',
  password: '123456'
});

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/doctor/login/phone',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(body, 'utf8')
  }
};

const req = http.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    console.log('Status:', res.statusCode);
    console.log('Response:', data);
    const json = JSON.parse(data);
    if (json.code === 200) {
      console.log('\n✅ 登录成功！');
      console.log('Token:', json.data.token);
      console.log('姓名:', json.data.name);
    } else {
      console.log('\n❌ 登录失败:', json.message);
    }
  });
});

req.on('error', (e) => {
  console.error('请求错误:', e.message);
});

req.write(body);
req.end();
