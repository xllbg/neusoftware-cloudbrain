const http = require('https');

const apiKey = 'sk-2d29fcb7df3d45898789df62e33a6eca';

const data = JSON.stringify({
  model: 'deepseek-chat',
  messages: [{ role: 'user', content: '你好，请用一句话介绍自己' }],
  max_tokens: 50,
  temperature: 0.1
});

const options = {
  hostname: 'api.deepseek.com',
  port: 443,
  path: '/chat/completions',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + apiKey,
    'Content-Length': Buffer.byteLength(data)
  }
};

const req = http.request(options, (res) => {
  let body = '';
  res.on('data', (chunk) => body += chunk);
  res.on('end', () => {
    console.log('Status:', res.statusCode);
    console.log('Response:', body);
  });
});

req.on('error', (e) => {
  console.error('Error:', e.message);
});

req.write(data);
req.end();
