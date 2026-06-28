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

// 登录 - 使用英文名 DoctorWang
async function login() {
  try {
    console.log('1. 获取公钥...');
    const publicKey = await getPublicKey();
    console.log('   公钥长度:', publicKey.length);

    // 使用 DoctorWang 测试（之前验证密码正确）
    const loginData = {
      name: 'DoctorWang',
      phone: '13900000002',
      password: '123456'
    };

    console.log('2. 加密登录数据...');
    const jsonStr = JSON.stringify(loginData);
    console.log('   原始数据:', jsonStr);

    const encrypted = rsaEncrypt(jsonStr, publicKey);
    console.log('   加密后长度:', encrypted.length);

    console.log('3. 发送加密登录请求...');
    const body = encrypted;

    const options = {
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

    return new Promise((resolve, reject) => {
      const req = http.request(options, (res) => {
        let data = '';
        res.on('data', chunk => data += chunk);
        res.on('end', () => {
          console.log('   状态码:', res.statusCode);
          console.log('   响应:', data);
          try {
            const json = JSON.parse(data);
            if (json.code === 200) {
              console.log('\n✅ 登录成功！');
              console.log('   姓名:', json.data.name);
              console.log('   Token:', json.data.token?.substring(0, 50) + '...');
            } else {
              console.log('\n❌ 登录失败:', json.message);
            }
          } catch (e) {
            console.log('   解析失败:', e.message);
          }
          resolve();
        });
      });
      req.on('error', reject);
      req.write(body);
      req.end();
    });
  } catch (e) {
    console.error('错误:', e.message);
  }
}

login();