const http = require('http');

// 先获取公钥
function getPublicKey() {
  return new Promise((resolve, reject) => {
    http.get('http://localhost:8080/api/auth/public-key', (res) => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => {
        const json = JSON.parse(d);
        resolve(json.data.publicKey);
      });
    }).on('error', reject);
  });
}

// 使用 Node.js 内置 RSA 加密
const crypto = require('crypto');

function encrypt(publicKey, data) {
  const buffer = Buffer.from(data, 'utf8');
  const encrypted = crypto.publicEncrypt(
    {
      key: publicKey,
      padding: crypto.constants.RSA_PKCS1_PADDING,
    },
    buffer
  );
  return encrypted.toString('base64');
}

// 发送加密登录请求
async function testEncryptedLogin() {
  console.log('1. 获取公钥...');
  const publicKey = await getPublicKey();
  console.log('   公钥获取成功:', publicKey.substring(0, 50) + '...');

  console.log('2. 加密登录数据...');
  const loginData = {
    name: '王明',
    phone: '13800138001',
    password: '123456'
  };
  const jsonData = JSON.stringify(loginData);
  const encrypted = encrypt(publicKey, jsonData);
  console.log('   加密成功，长度:', encrypted.length);

  console.log('3. 发送加密请求...');
  const options = {
    hostname: 'localhost',
    port: 8080,
    path: '/api/doctor/login/phone',
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain',
      'X-Encrypted': 'true',
      'Content-Length': Buffer.byteLength(encrypted, 'utf8')
    }
  };

  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        console.log('   Status:', res.statusCode);
        const json = JSON.parse(data);
        if (json.code === 200) {
          console.log('✅ 加密登录成功！');
          console.log('Token:', json.data.token.substring(0, 50) + '...');
          resolve(true);
        } else {
          console.log('❌ 加密登录失败:', json.message);
          resolve(false);
        }
      });
    });
    req.on('error', (e) => {
      console.error('请求错误:', e.message);
      reject(e);
    });
    req.write(encrypted);
    req.end();
  });
}

testEncryptedLogin().then(success => {
  console.log('\n测试结果:', success ? '成功' : '失败');
});
