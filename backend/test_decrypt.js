const http = require('http');
const crypto = require('crypto');

// 1. 获取公钥
function getPublicKey() {
  return new Promise((resolve, reject) => {
    http.get('http://localhost:8080/api/auth/public-key', (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        const json = JSON.parse(data);
        resolve(json.data.publicKey);
      });
    }).on('error', reject);
  });
}

// 将 DER 公钥转换为 PEM 格式
function derToPem(derBuffer, type = 'PUBLIC KEY') {
  const base64 = derBuffer.toString('base64');
  let pem = `-----BEGIN ${type}-----\n`;
  for (let i = 0; i < base64.length; i += 64) {
    pem += base64.slice(i, i + 64) + '\n';
  }
  pem += `-----END ${type}-----\n`;
  return pem;
}

function rsaEncrypt(plaintext, publicKeyDerBase64) {
  const derBuffer = Buffer.from(publicKeyDerBase64, 'base64');
  const pem = derToPem(derBuffer);

  const encrypted = crypto.publicEncrypt(
    {
      key: pem,
      padding: crypto.constants.RSA_PKCS1_PADDING
    },
    Buffer.from(plaintext, 'utf8')
  );
  return encrypted.toString('base64');
}

// 登录测试 - 检查加密登录 vs test-decrypt 的区别
async function testLogin() {
  try {
    console.log('1. 获取公钥...');
    const publicKey = await getPublicKey();

    const loginData = {
      name: 'DoctorWang',
      phone: '13900000002',
      password: '123456'
    };

    console.log('2. 加密登录数据...');
    const jsonStr = JSON.stringify(loginData);
    const encrypted = rsaEncrypt(jsonStr, publicKey);

    // 测试不同的 Content-Type
    const body = encrypted;

    console.log('3. 测试 /api/auth/test-decrypt（text/plain）...');
    const options1 = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/auth/test-decrypt',
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain',
        'X-Encrypted': 'true',  // 加上这个头
        'Content-Length': Buffer.byteLength(body, 'utf8')
      }
    };

    await new Promise((resolve) => {
      const req = http.request(options1, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          console.log('   状态码:', res.statusCode);
          console.log('   响应:', data.substring(0, 200));
          resolve();
        });
      });
      req.on('error', e => console.error('   错误:', e.message));
      req.write(body);
      req.end();
    });

    console.log('\n4. 测试 /api/doctor/login/phone（text/plain + X-Encrypted）...');
    const options2 = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/doctor/login/phone',
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain',
        'X-Encrypted': 'true',
        'Content-Length': Buffer.byteLength(body, 'utf8')
      }
    };

    await new Promise((resolve) => {
      const req = http.request(options2, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          console.log('   状态码:', res.statusCode);
          console.log('   响应:', data.substring(0, 200));
          resolve();
        });
      });
      req.on('error', e => console.error('   错误:', e.message));
      req.write(body);
      req.end();
    });

    console.log('\n5. 测试 /api/doctor/login/phone（application/json + X-Encrypted）...');
    const options3 = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/doctor/login/phone',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Encrypted': 'true',
        'Content-Length': Buffer.byteLength(body, 'utf8')
      }
    };

    await new Promise((resolve) => {
      const req = http.request(options3, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          console.log('   状态码:', res.statusCode);
          console.log('   响应:', data.substring(0, 200));
          resolve();
        });
      });
      req.on('error', e => console.error('   错误:', e.message));
      req.write(body);
      req.end();
    });

  } catch (e) {
    console.error('错误:', e.message);
  }
}

testLogin();