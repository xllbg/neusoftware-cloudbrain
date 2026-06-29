const http = require('http');
const JSEncrypt = require('../frontend/node_modules/jsencrypt/bin/jsencrypt.min.js');

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
function derToPem(derBase64) {
  let pem = "-----BEGIN PUBLIC KEY-----\n";
  for (let i = 0; i < derBase64.length; i += 64) {
    pem += derBase64.slice(i, i + 64) + "\n";
  }
  pem += "-----END PUBLIC KEY-----\n";
  return pem;
}

// 测试解密
async function test() {
  try {
    console.log('1. 获取公钥...');
    const publicKey = await getPublicKey();
    console.log('   公钥长度:', publicKey.length);

    const testData = {
      name: 'DoctorWang',
      phone: '13900000002',
      password: '123456'
    };
    const jsonStr = JSON.stringify(testData);
    console.log('\n2. 原始数据:', jsonStr);

    // 测试 1: 纯 base64（不带 PEM 标记）
    console.log('\n3. 测试纯 base64 公钥...');
    const encryptor1 = new JSEncrypt();
    const result1 = encryptor1.setPublicKey(publicKey);
    console.log('   setPublicKey 返回:', result1);
    const encrypted1 = encryptor1.encrypt(jsonStr);
    console.log('   加密结果:', encrypted1 ? (encrypted1.substring(0, 50) + '...') : 'null/false');

    // 测试 2: PEM 格式
    console.log('\n4. 测试 PEM 格式公钥...');
    const pemKey = derToPem(publicKey);
    const encryptor2 = new JSEncrypt();
    const result2 = encryptor2.setPublicKey(pemKey);
    console.log('   setPublicKey 返回:', result2);
    const encrypted2 = encryptor2.encrypt(jsonStr);
    console.log('   加密结果:', encrypted2 ? (encrypted2.substring(0, 50) + '...') : 'null/false');

    // 发送 test-decrypt 请求
    if (encrypted2) {
      console.log('\n5. 发送解密测试...');
      const body = encrypted2;
      const options = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/auth/test-decrypt',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Encrypted': 'true',
          'Content-Length': Buffer.byteLength(body, 'utf8')
        }
      };

      await new Promise((resolve) => {
        const req = http.request(options, (res) => {
          let data = '';
          res.on('data', chunk => data += chunk);
          res.on('end', () => {
            console.log('   状态码:', res.statusCode);
            console.log('   响应:', data);
            resolve();
          });
        });
        req.on('error', e => console.error('   错误:', e.message));
        req.write(body);
        req.end();
      });
    }

  } catch (e) {
    console.error('错误:', e.message);
    console.error(e.stack);
  }
}

test();