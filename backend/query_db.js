const mysql = require('mysql2');

const conn = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: 'root123',
  database: 'cloudbrain',
  charset: 'utf8mb4'
});

conn.connect();

// 查询患者
conn.query('SELECT id, username, name, phone, status FROM patient', (err, patients) => {
  if (err) throw err;
  console.log('=== 患者表 ===');
  console.log(JSON.stringify(patients, null, 2));
});

// 查询医生
conn.query('SELECT id, username, name, phone, status FROM doctor', (err, doctors) => {
  if (err) throw err;
  console.log('\n=== 医生表 ===');
  console.log(JSON.stringify(doctors, null, 2));
});

// 查询挂号
conn.query('SELECT id, patient_id, doctor_id, department, status, registration_date FROM registration', (err, regs) => {
  if (err) throw err;
  console.log('\n=== 挂号表 ===');
  console.log(JSON.stringify(regs, null, 2));
});

conn.end();
