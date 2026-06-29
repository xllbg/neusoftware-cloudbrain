const http = require('http');

const body = JSON.stringify({
  name: '王明',
  phone: '13900000002',
  password: '123456'
});

const options = {
  hostname: 'localhost',
  port: 5173,
  path: '/api/doctor/login/phone',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
    'Content-Length': Buffer.byteLength(body, 'utf8')
  }
};

const req = http.request(options, (res) => {
  console.log('HTTP 状态码:', res.statusCode);
  console.log('Content-Type:', res.headers['content-type']);
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => {
    console.log('响应体:', data);
    try {
      const json = JSON.parse(data);
      console.log('\n解析后:');
      console.log('  code:', json.code);
      console.log('  message:', json.message);
    } catch (e) {
      console.log('JSON 解析失败');
    }
  });
});

req.on('error', e => console.error('错误:', e.message));
req.write(body);
req.end();