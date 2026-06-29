const http = require('http');
const crypto = require('crypto');

// 生成 RSA 密钥对
const { publicKey, privateKey } = crypto.generateKeyPairSync('rsa', {
  modulusLength: 2048,
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

// 模拟公钥加密（实际前端用的是后端的公钥）
// 这里我们直接用明文测试，因为前端会用后端的公钥加密

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
    'X-Encrypted': 'false',  // 明文传输，不用加密
    'Content-Length': Buffer.byteLength(body, 'utf8')
  }
};

console.log('测试明文登录...');
const req = http.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    console.log('Status:', res.statusCode);
    const json = JSON.parse(data);
    if (json.code === 200) {
      console.log('✅ 明文登录成功！');
      console.log('Token:', json.data.token.substring(0, 50) + '...');
    } else {
      console.log('❌ 明文登录失败:', json.message);
    }
  });
});

req.on('error', (e) => {
  console.error('请求错误:', e.message);
});

req.write(body);
req.end();
