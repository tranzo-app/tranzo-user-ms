# API Contract: Create Draft Trip with Images

## Endpoint
```
POST /trips/
```

## HTTP Method
`POST`

## Content-Type
`multipart/form-data`

## Authentication
- **Required**: Yes
- **Type**: Bearer Token (JWT)
- **Header**: `Authorization: Bearer <token>`

## Request

### Multipart Parts

#### Part 1: trip (Required)
- **Name**: `trip`
- **Type**: JSON (application/json)
- **Validation**: `@Validated(DraftChecks.class)`
- **Description**: Trip data object containing draft trip details

**TripDto Structure**:
```json
{
  "tripTitle": "string (required)",
  "tripDescription": "string",
  "tripDestination": "string (required)",
  "tripStartDate": "ISO date (required, format: yyyy-MM-dd)",
  "tripEndDate": "ISO date (required, format: yyyy-MM-dd)",
  "estimatedBudget": "number (optional)",
  "maxParticipants": "integer (optional)",
  "joinPolicy": "OPEN | REQUEST_TO_JOIN | INVITE_ONLY (optional)",
  "visibilityStatus": "PUBLIC | PRIVATE (optional)",
  "tripTags": [
    {
      "tagName": "string"
    }
  ],
  "tripItineraries": []
}
```

#### Part 2: files (Optional)
- **Name**: `files`
- **Type**: Array of files (multipart/form-data)
- **Required**: No
- **Description**: List of image files to attach to the trip
- **Max Size**: Per file size limit as configured in application properties
- **Accepted Formats**: jpg, jpeg, png, gif, webp (as configured)

### Example Request (cURL)
```bash
curl -X POST https://api.example.com/trips/ \
  -H "Authorization: Bearer <jwt-token>" \
  -F "trip={\"tripTitle\":\"Paris Trip\",\"tripDestination\":\"Paris\",\"tripStartDate\":\"2026-06-01\",\"tripEndDate\":\"2026-06-10\"};type=application/json" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg"
```

### Example Request (JavaScript/FormData)
```javascript
const formData = new FormData();
const tripData = {
  tripTitle: "Paris Trip",
  tripDestination: "Paris",
  tripStartDate: "2026-06-01",
  tripEndDate: "2026-06-10"
};

formData.append('trip', JSON.stringify(tripData));
formData.append('files', file1);
formData.append('files', file2);

fetch('/trips/', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});
```

## Response

### Success Response (201 Created)
```json
{
  "statusCode": 201,
  "message": "Draft trip has been created successfully",
  "data": {
    "tripId": "uuid",
    "tripStatus": "DRAFT",
    "tripTitle": "string",
    "tripDestination": "string",
    "tripStartDate": "ISO date",
    "tripEndDate": "ISO date",
    "estimatedBudget": "number",
    "maxParticipants": "integer",
    "joinPolicy": "OPEN | REQUEST_TO_JOIN | INVITE_ONLY",
    "visibilityStatus": "PUBLIC | PRIVATE",
    "tripTags": [...],
    "tripItineraries": [...],
    "createdAt": "ISO datetime",
    "updatedAt": "ISO datetime"
  }
}
```

### Error Responses

#### 400 Bad Request
```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "tripTitle",
      "message": "Trip title is required"
    }
  ]
}
```

#### 401 Unauthorized
```json
{
  "statusCode": 401,
  "message": "Authentication failed"
}
```

#### 500 Internal Server Error
```json
{
  "statusCode": 500,
  "message": "Internal server error"
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 201 | Draft trip created successfully |
| 400 | Validation error in request data |
| 401 | Authentication failed or missing token |
| 500 | Internal server error |

## Validation Rules (DraftChecks)

### Required Fields
- `tripTitle`: Must not be blank
- `tripDestination`: Must not be blank
- `tripStartDate`: Must be a valid date, not null
- `tripEndDate`: Must be a valid date, not null, must be after or equal to tripStartDate

### Optional Fields
- `tripDescription`: Free text
- `estimatedBudget`: Positive number
- `maxParticipants`: Positive integer
- `joinPolicy`: Must be one of OPEN, REQUEST_TO_JOIN, INVITE_ONLY
- `visibilityStatus`: Must be one of PUBLIC, PRIVATE
- `tripTags`: Array of tag objects
- `tripItineraries`: Array of itinerary objects

## Notes

- The trip is created in `DRAFT` status
- Images uploaded are stored in S3 (if configured)
- If no files are provided, the trip is created without images
- The user ID is extracted from the JWT token
- Profile picture URLs in response are presigned S3 URLs (if using S3)
- All dates must be in ISO format (yyyy-MM-dd)
- The endpoint accepts both with and without files

## Related Endpoints

- `PUT /trips/{tripId}` - Update draft trip
- `POST /trips/{tripId}/publish` - Publish draft trip
- `DELETE /trips/{tripId}` - Cancel/delete trip
- `GET /trips/{tripId}` - Fetch trip details
