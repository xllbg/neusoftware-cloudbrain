const http = require('http');

function post(url, data) {
  return new Promise((resolve, reject) => {
    const body = JSON.stringify(data);
    const options = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(body)
      }
    };
    const req = http.request(url, options, (res) => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => resolve(JSON.parse(d)));
    });
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

(async () => {
  try {
    console.log('测试AI病历生成...');
    const result = await post(
      'http://localhost:8080/api/medical-record/generate?patientId=1',
      {
        chiefComplaint: '头痛3天',
        presentIllness: '患者3天前无明显诱因出现头痛，呈持续性胀痛，以双侧颞部为主，休息后稍缓解，无恶心呕吐，无肢体活动障碍。',
        pastHistory: '既往体健，无高血压糖尿病病史。'
      }
    );
    console.log('AI病历生成结果:', JSON.stringify(result, null, 2));
  } catch (e) {
    console.error('AI病历生成失败:', e.message);
  }
})();
