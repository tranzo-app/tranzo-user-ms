# API Contract: Media Module

**Base URL:** `http://localhost:8089`  
**Controller:** MediaController  
**Total Endpoints:** 2  
**Response Format:** ResponseDto<T>  
**External Dependency:** AWS S3 (for file uploads)

---

## 1. POST /media/upload

**Purpose:** Upload file to S3 and get URL

### Request

```http
POST /media/upload HTTP/1.1
Host: localhost:8089
Content-Type: multipart/form-data
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Request Body (Multipart):**
```
--boundary
Content-Disposition: form-data; name="file"; filename="trip.jpg"
Content-Type: image/jpeg

[binary image data]
--boundary--
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| file | File | Yes | JPG, PNG, GIF, WebP; Max 10MB | Image file to upload |

**Accepted MIME Types:**
- `image/jpeg` (JPG)
- `image/png` (PNG)
- `image/gif` (GIF)
- `image/webp` (WebP)

### Response

#### ✅ 200 OK (File Uploaded)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "File uploaded successfully",
  "data": {
    "fileKey": "uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg",
    "fileName": "trip.jpg",
    "fileSize": 2048576,
    "fileMimeType": "image/jpeg",
    "url": "https://tranzo-bucket.s3.amazonaws.com/uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg",
    "uploadedAt": "2026-01-24T16:30:00Z",
    "expiresAt": null
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- No file provided
- File exceeds 10MB
- Invalid MIME type
- File is empty

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "File validation failed",
  "data": {
    "fieldErrors": {
      "file": "File size must be less than 10MB"
    }
  }
}
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized",
  "data": null
}
```

#### ❌ 415 Unsupported Media Type
**Scenarios:**
- Content-Type header not multipart/form-data

```json
{
  "statusCode": 415,
  "status": "ERROR",
  "statusMessage": "Unsupported media type. Content-Type must be multipart/form-data",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- S3 not configured
- AWS S3 service down
- Network error connecting to S3

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "File upload service temporarily unavailable. Please try again later.",
  "data": null
}
```

#### ❌ 500 Internal Server Error
**Scenarios:**
- Unexpected server error
- File processing error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to upload file",
  "data": null
}
```

### Example Requests

**cURL:**
```bash
curl -X POST http://localhost:8089/media/upload \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@/path/to/image.jpg"
```

**JavaScript (Fetch API):**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const response = await fetch('/media/upload', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: formData
});

const data = await response.json();
if (response.ok) {
  console.log('Uploaded URL:', data.data.url);
  // Store data.data.fileKey for deletion later if needed
}
```

**JavaScript (With progress tracking):**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const xhr = new XMLHttpRequest();

xhr.upload.addEventListener('progress', (e) => {
  if (e.lengthComputable) {
    const percentComplete = (e.loaded / e.total) * 100;
    console.log('Upload progress:', percentComplete + '%');
  }
});

xhr.addEventListener('load', () => {
  const data = JSON.parse(xhr.responseText);
  if (xhr.status === 200) {
    console.log('File uploaded:', data.data.url);
  }
});

xhr.open('POST', '/media/upload');
xhr.setRequestHeader('Authorization', `Bearer ${token}`);
xhr.send(formData);
```

**Python:**
```python
import requests

url = 'http://localhost:8089/media/upload'
headers = {'Authorization': f'Bearer {token}'}
files = {'file': open('image.jpg', 'rb')}

response = requests.post(url, headers=headers, files=files)
data = response.json()

if response.status_code == 200:
    print('Uploaded URL:', data['data']['url'])
else:
    print('Error:', data['statusMessage'])
```

### File Upload Specifications

**Size Limits:**
- Max file size: 10 MB

**Supported Formats:**
| Format | MIME Type | Max Resolution |
|--------|-----------|----------------|
| JPEG | image/jpeg | 8000x8000 |
| PNG | image/png | 8000x8000 |
| GIF | image/gif | 8000x8000 |
| WebP | image/webp | 8000x8000 |

**S3 Storage Structure:**
```
s3://tranzo-bucket/
├── uploads/
│   ├── 2026/
│   │   ├── 01/
│   │   │   ├── 24/
│   │   │   │   ├── trip_[userid].jpg
│   │   │   │   ├── profile_[userid].png
│   │   │   │   └── ...
```

**File Naming Convention:**
- `{resourceType}_{userId}_{timestamp}.{extension}`
- Example: `trip_550e8400-e29b-41d4-a716-446655440000_1706112600.jpg`

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | File uploaded successfully |
| 400 | File validation error |
| 401 | Unauthorized |
| 415 | Invalid Content-Type |
| 503 | S3 service unavailable |
| 500 | Server error |

---

## 2. GET /media/url

**Purpose:** Get presigned URL for file download/viewing (with optional expiry)

### Request

```http
GET /media/url?key=uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg&expiryMinutes=60 HTTP/1.1
Host: localhost:8089
Authorization: Bearer <access_token>
```

**Authentication:** Required

**Query Parameters:**
| Parameter | Type | Required | Default | Validation | Description |
|-----------|------|----------|---------|------------|-------------|
| key | String | Yes | - | S3 object key | S3 file key/path |
| expiryMinutes | Integer | No | 1440 (24 hours) | 1-525600 | URL expiry time in minutes |

### Response

#### ✅ 200 OK (Presigned URL Generated)
```json
{
  "statusCode": 200,
  "status": "SUCCESS",
  "statusMessage": "Presigned URL generated successfully",
  "data": {
    "fileKey": "uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg",
    "fileName": "trip.jpg",
    "url": "https://tranzo-bucket.s3.amazonaws.com/uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
    "expiresIn": 3600,
    "expiresAt": "2026-01-24T17:30:00Z",
    "contentType": "image/jpeg"
  }
}
```

#### ❌ 400 Bad Request
**Scenarios:**
- Missing key parameter
- Invalid expiryMinutes (< 1 or > 525600)
- Invalid file key format

```json
{
  "statusCode": 400,
  "status": "ERROR",
  "statusMessage": "Validation failed",
  "data": {
    "fieldErrors": {
      "key": "File key is required",
      "expiryMinutes": "Expiry time must be between 1 and 525600 minutes"
    }
  }
}
```

#### ❌ 401 Unauthorized

```json
{
  "statusCode": 401,
  "status": "ERROR",
  "statusMessage": "Unauthorized",
  "data": null
}
```

#### ❌ 403 Forbidden
**Scenarios:**
- User trying to access file from another user's upload
- File belongs to different resource/user

```json
{
  "statusCode": 403,
  "status": "ERROR",
  "statusMessage": "You don't have permission to access this file",
  "data": null
}
```

#### ❌ 404 Not Found
**Scenarios:**
- File doesn't exist in S3
- Invalid key

```json
{
  "statusCode": 404,
  "status": "ERROR",
  "statusMessage": "File not found in storage",
  "data": null
}
```

#### ❌ 503 Service Unavailable
**Scenarios:**
- S3 not configured
- AWS S3 service down

```json
{
  "statusCode": 503,
  "status": "ERROR",
  "statusMessage": "File storage service temporarily unavailable",
  "data": null
}
```

#### ❌ 500 Internal Server Error

```json
{
  "statusCode": 500,
  "status": "ERROR",
  "statusMessage": "Failed to generate file URL",
  "data": null
}
```

### Example Requests

**cURL (Default 24-hour expiry):**
```bash
curl -X GET "http://localhost:8089/media/url?key=uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg" \
  -H "Authorization: Bearer <access_token>"
```

**cURL (Custom expiry - 1 hour):**
```bash
curl -X GET "http://localhost:8089/media/url?key=uploads/2026/01/24/trip_550e8400-e29b-41d4-a716-446655440000.jpg&expiryMinutes=60" \
  -H "Authorization: Bearer <access_token>"
```

**JavaScript:**
```javascript
async function getPresignedUrl(fileKey, expiryMinutes = 1440) {
  const params = new URLSearchParams({
    key: fileKey,
    expiryMinutes: expiryMinutes
  });

  const response = await fetch(`/media/url?${params}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const data = await response.json();
  
  if (response.ok) {
    console.log('Presigned URL:', data.data.url);
    console.log('Expires at:', data.data.expiresAt);
    return data.data.url;
  } else {
    console.error('Error:', data.statusMessage);
  }
}

// Usage
getPresignedUrl('uploads/2026/01/24/trip_550e8400.jpg', 60)
  .then(url => {
    // Use URL for direct image link, download, etc.
    const img = document.createElement('img');
    img.src = url;
    document.body.appendChild(img);
  });
```

**Python:**
```python
import requests
from urllib.parse import urlencode

token = 'your_access_token'
file_key = 'uploads/2026/01/24/trip_550e8400.jpg'
expiry_minutes = 60

params = {
    'key': file_key,
    'expiryMinutes': expiry_minutes
}

headers = {'Authorization': f'Bearer {token}'}
response = requests.get(
    'http://localhost:8089/media/url',
    params=params,
    headers=headers
)

data = response.json()

if response.status_code == 200:
    print('Presigned URL:', data['data']['url'])
    print('Expires in:', data['data']['expiresIn'], 'seconds')
else:
    print('Error:', data['statusMessage'])
```

### Presigned URL Details

**Purpose:**
- Temporary public access to private S3 files
- Time-limited (default 24 hours)
- Querystring authentication
- Cannot be revoked once generated

**Use Cases:**
1. Image viewing in browser
2. Direct downloads
3. Temporary sharing with external parties
4. Multipart form displays

**Example presigned URL:**
```
https://tranzo-bucket.s3.amazonaws.com/uploads/2026/01/24/trip_550e8400.jpg?
  X-Amz-Algorithm=AWS4-HMAC-SHA256
  &X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20260124%2Fus-east-1%2Fs3%2Faws4_request
  &X-Amz-Date=20260124T163000Z
  &X-Amz-Expires=3600
  &X-Amz-SignedHeaders=host
  &X-Amz-Signature=abc123def456...
```

### Status Code Summary
| Code | Reason |
|------|--------|
| 200 | Presigned URL generated |
| 400 | Invalid parameters |
| 401 | Unauthorized |
| 403 | No permission |
| 404 | File not found |
| 503 | S3 service unavailable |
| 500 | Server error |

---

## AWS S3 Configuration

### Environment Variables Required

```env
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=us-east-1
AWS_S3_BUCKET_NAME=tranzo-bucket
AWS_S3_ENDPOINT=https://tranzo-bucket.s3.amazonaws.com
```

### S3 Bucket Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowPublicRead",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::tranzo-bucket/*"
    },
    {
      "Sid": "AllowAppUpload",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::ACCOUNT_ID:root"
      },
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::tranzo-bucket/*"
    }
  ]
}
```

---

## Best Practices

### Frontend

1. **Before Upload:** Validate file locally
```javascript
function validateFile(file) {
  const maxSize = 10 * 1024 * 1024; // 10MB
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  
  if (file.size > maxSize) {
    console.error('File too large');
    return false;
  }
  
  if (!validTypes.includes(file.type)) {
    console.error('Invalid file type');
    return false;
  }
  
  return true;
}
```

2. **Handle Upload Errors Gracefully**
```javascript
async function uploadWithRetry(file, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await uploadFile(file);
      if (response.ok) return response.data;
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await sleep(1000 * (i + 1)); // Exponential backoff
    }
  }
}
```

3. **Use Presigned URLs for Display**
```javascript
// Don't display uploaded URL directly if private
// Instead, always fetch fresh presigned URL when needed
async function displayImage(fileKey) {
  const presignedUrl = await getPresignedUrl(fileKey);
  const img = document.createElement('img');
  img.src = presignedUrl;
  return img;
}
```

---

## Summary Table

| # | Endpoint | Method | Auth | Response | Status Codes |
|---|----------|--------|------|----------|--------------|
| 1 | /media/upload | POST | Yes | ResponseDto<UploadResponseDto> | 200, 400, 401, 415, 503, 500 |
| 2 | /media/url | GET | Yes | ResponseDto<PresignedUrlResponseDto> | 200, 400, 401, 403, 404, 503, 500 |

---

**Last Updated:** July 14, 2026

