# Deploy to Render.com

## Prerequisites
1. GitHub account (free)
2. Render.com account (free)

## Step-by-Step Deployment

### 1. Push to GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/employee-attendance-analyzer.git
git push -u origin main
```

### 2. Create Render Account
- Go to https://render.com
- Sign up with GitHub (or create account)

### 3. Connect Repository
- Log in to Render.com
- Click "New +" → "Web Service"
- Select "Build and deploy from a Git repository"
- Click "Connect GitHub"
- Authorize Render to access your GitHub repos
- Select `employee-attendance-analyzer` repo

### 4. Configure Deployment
Render should auto-detect from `render.yaml`, but manually set if needed:
- **Name:** `attendance-analyzer` (or your choice)
- **Environment:** Docker
- **Build Command:** (leave empty - using Dockerfile)
- **Start Command:** (leave empty - using Dockerfile)
- **Plan:** Free tier

### 5. Deploy
- Click "Create Web Service"
- Render will build and deploy automatically (takes 5-10 minutes)
- Once deployed, you'll get a public URL like: `https://attendance-analyzer.onrender.com`

### 6. Access Your App
- Open the URL in your browser
- Your app will be at: `https://attendance-analyzer.onrender.com/attendance/`

## Notes
- Free tier may auto-spin down after 15 minutes of inactivity
- Each request will spin it back up (may take 30 seconds)
- For production, upgrade to paid tier

## Troubleshooting
If deployment fails:
1. Check "Logs" tab in Render dashboard
2. Common issues:
   - Missing `pom.xml` - update build.sh if needed
   - Port not exposed - check Dockerfile has `EXPOSE 8080`
   - Build timeout - free tier has 30-minute build limit

---

**Need help?** Contact Render support or check logs in the dashboard.
