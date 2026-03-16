const { MongoClient } = require('mongodb');
const client = new MongoClient('mongodb+srv://karthik:karthik@cluster0.wzxks5z.mongodb.net/student_tracker');
client.connect().then(async () => {
  const db = client.db('student_tracker');
  const info = await db.command({ listCollections: 1, filter: { name: 'performance' } });
  const col = info.cursor.firstBatch[0];
  const schema = col && col.options && col.options.validator;
  if (schema) {
    const examEnum = schema['$jsonSchema'] && schema['$jsonSchema'].properties && schema['$jsonSchema'].properties.examType && schema['$jsonSchema'].properties.examType.enum;
    console.log('Current examType enum:', JSON.stringify(examEnum));
  } else {
    console.log('No validator found');
  }
  await client.close();
}).catch(e => console.error(e.message));
