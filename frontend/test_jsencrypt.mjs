import http from 'http';
import { JSEncrypt } from 'jsencrypt';

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

// 用 jsencrypt 加密
function encryptWithJsencrypt(plaintext, publicKey) {
  const encryptor = new JSEncrypt();
  encryptor.setPublicKey(publicKey);
  const encrypted = encryptor.encrypt(plaintext);
  if (!encrypted) {
    throw new Error('jsencrypt 加密失败，返回 null');
  }
  return encrypted;
}

// 测试解密
async function test() {
  try {
    console.log('1. 获取公钥...');
    const publicKey = await getPublicKey();
    console.log('   公钥长度:', publicKey.length);
    console.log('   公钥前50字符:', publicKey.substring(0, 50));

    const testData = {
      name: 'DoctorWang',
      phone: '13900000002',
      password: '123456'
    };
    const jsonStr = JSON.stringify(testData);
    console.log('\n2. 原始数据:', jsonStr);

    console.log('\n3. 使用 jsencrypt 加密...');
    const encrypted = encryptWithJsencrypt(jsonStr, publicKey);
    console.log('   加密后长度:', encrypted.length);
    console.log('   加密后前50字符:', encrypted.substring(0, 50));

    console.log('\n4. 发送到后端 test-decrypt 接口...');
    const body = encrypted;

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
              console.log('\n✅ 解密成功！');
              console.log('   解密结果:', json.data.decrypted);
            } else {
              console.log('\n❌ 解密失败:', json.message);
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
    console.error(e.stack);
  }
}

test();