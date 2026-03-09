# 🔐 Update MongoDB Credentials

## Current Issue
Your application.properties has placeholder credentials:
```
spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.wzxks5z.mongodb.net/student_tracker
```

## ✅ How to Fix

### Step 1: Get Your MongoDB Credentials

1. Go to [MongoDB Atlas](https://cloud.mongodb.com/)
2. Login to your account
3. Click on "Database Access" in the left sidebar
4. You should see your database users listed

### Step 2: Find or Create Database User

**Option A: Use Existing User**
- Note down the username
- If you forgot the password, click "Edit" → "Edit Password" → Set new password

**Option B: Create New User**
1. Click "Add New Database User"
2. Choose "Password" authentication
3. Set username (e.g., `admin`, `dbuser`, etc.)
4. Set password (e.g., `SecurePass123!`)
5. Set privileges to "Atlas admin" or "Read and write to any database"
6. Click "Add User"

### Step 3: Update Connection String

Replace `username` and `password` in your connection string:

**Example:**
If your username is `admin` and password is `SecurePass123!`:
```
mongodb+srv://admin:SecurePass123!@cluster0.wzxks5z.mongodb.net/student_tracker
```

**Important:** If your password contains special characters, you need to URL-encode them:
- `@` → `%40`
- `:` → `%3A`
- `/` → `%2F`
- `?` → `%3F`
- `#` → `%23`
- `[` → `%5B`
- `]` → `%5D`
- `%` → `%25`

### Step 4: Update application.properties

Edit `backend/src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@cluster0.wzxks5z.mongodb.net/student_tracker
```

### Step 5: Restart Backend

After updating, restart your backend server.

## 🧪 Test Connection

Once you have the correct credentials, run:
```bash
node check-database-data.js
```

This will:
- ✅ Test connection
- ✅ List all collections
- ✅ Count documents in each collection
- ✅ Show sample data
- ✅ Verify required collections exist

## 📊 Expected Database State

After seeding, you should have:
- **users**: 29 documents (1 admin, 8 faculty, 20 students)
- **students**: 20 documents
- **faculty**: 8 documents
- **courses**: 3 documents
- **subjects**: 106 documents
- **classAllocations**: ~18 documents
- **attendance**: ~5,400 documents
- **performance**: Variable

## 🔄 If Database is Empty

If the database exists but has no data, run the seeding scripts:

```bash
# Update MongoDB URI in seed scripts first
# Then run:
npm run seed:subjects
npm run seed:demo
```

## ⚠️ Security Note

- Never commit real credentials to Git
- Use environment variables in production
- Rotate passwords regularly
- Use strong passwords with special characters

## 📝 Next Steps

1. Get your real MongoDB credentials
2. Update application.properties
3. Update check-database-data.js (line 3)
4. Run: `node check-database-data.js`
5. If empty, run seeding scripts
6. Restart backend
7. Test login with demo credentials

---

**Need the actual credentials? Check your MongoDB Atlas account!**
