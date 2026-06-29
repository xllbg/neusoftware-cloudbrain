const http = require('http');
const JSEncrypt = require('../frontend/node_modules/jsencrypt/bin/jsencrypt.min.js');

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

function derToPem(derBase64) {
  let pem = "-----BEGIN PUBLIC KEY-----\n";
  for (let i = 0; i < derBase64.length; i += 64) {
    pem += derBase64.slice(i, i + 64) + "\n";
  }
  pem += "-----END PUBLIC KEY-----\n";
  return pem;
}

async function test() {
  try {
    const publicKey = await getPublicKey();
    const pemKey = derToPem(publicKey);

    const loginData = {
      name: 'DoctorWang',
      phone: '13900000002',
      password: '123456'
    };
    const jsonStr = JSON.stringify(loginData);

    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(pemKey);
    const encrypted = encryptor.encrypt(jsonStr);

    console.log('加密成功，长度:', encrypted.length);

    // 测试完整登录流程（走 DecryptFilter）
    const body = encrypted;
    const options = {
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

    const result = await new Promise((resolve, reject) => {
      const req = http.request(options, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          resolve({ status: res.statusCode, body: data });
        });
      });
      req.on('error', reject);
      req.write(body);
      req.end();
    });

    console.log('\n完整登录测试结果:');
    console.log('  状态码:', result.status);
    console.log('  响应:', result.body);

  } catch (e) {
    console.error('错误:', e.message);
    console.error(e.stack);
  }
}

test();