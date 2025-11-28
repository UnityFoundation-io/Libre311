# Libre311 Users Guide

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting-started)
- [Public User Guide](#public-user-guide)
  - [Browsing Service Requests](#browsing-service-requests)
  - [Creating a Service Request](#creating-a-service-request)
  - [Searching and Filtering](#searching-and-filtering)
- [Administrator Guide](#administrator-guide)
  - [Logging In](#logging-in)
  - [Managing Service Requests](#managing-service-requests)
  - [Configuring Service Definitions](#configuring-service-definitions)
- [Mobile Experience](#mobile-experience)
- [Accessibility](#accessibility)
- [Troubleshooting](#troubleshooting)

---

## Introduction

Libre311 is a mobile-friendly web application that implements the Open311 standard for civic service requests. It enables communities to report and track non-emergency issues such as potholes, graffiti, streetlight outages, and other municipal services.

### What is Open311?

Open311 is a standardized protocol for civic issue tracking. Libre311 implements the Open311 GeoReport v2 API, making service request data accessible and interoperable with other civic technology tools.

### Key Features

- **No account required** for public users to browse and submit service requests
- **Mobile-friendly** interface optimized for smartphones and tablets
- **Map-based** visualization of service requests
- **Multi-format views** including list, map, and table layouts
- **Image uploads** to document issues
- **Administrator tools** for managing and responding to requests

---

## Getting Started

### Accessing Libre311

Open your web browser and navigate to your Libre311 instance URL. The application works on:
- Desktop computers
- Tablets
- Smartphones

No installation or app download is required.

### User Roles

**Public Users**
- Anyone can browse and create service requests without creating an account
- No login required for basic functionality

**Administrators**
- Municipal staff or authorized personnel who manage service requests
- Require login credentials to access administrative features

---

## Public User Guide

### Browsing Service Requests

#### Map View

The map view is the default landing page and provides a geographic overview of service requests.

![Libre311 Map View showing service requests as color-coded markers](images/map-view-public.png)

*Figure 1: Map view displaying service requests as color-coded markers. The map includes zoom controls, a toggle between Map/List views, and a "New Request" button.*

**How to use the map:**

1. **Pan the map**: Click and drag to move around
2. **Zoom**: Use the +/- buttons or pinch-to-zoom on mobile
3. **View markers**: Each marker represents a service request
4. **Click a marker**: Opens a preview of the service request
5. **View details**: Click on the preview to see the full request information

**Map features:**
- Color-coded markers by request status
- Clustered markers for areas with many requests
- Location-based filtering

#### List View

The list view shows service requests in a scrollable format.

![Libre311 List View showing service requests with details and photos](images/list-view-public.png)

*Figure 2: List view displaying service requests with ID, status, title, location, date, description, and associated photos. Includes pagination controls and toggle between Map/List views.*

**Accessing list view:**
- Click "List" in the navigation menu
- Requests are displayed with key information:
  - Service request ID
  - Request type (e.g., "Pothole", "Graffiti")
  - Status (Open, Closed, In Progress)
  - Location address
  - Date submitted
  - Description preview

**List features:**
- Pagination controls to navigate through requests
- Click any request to view full details
- Sorted by most recent first

#### Viewing Request Details

When you click on a service request, you'll see:

![Libre311 Service Request Details View](images/request-details-public.png)

*Figure 3: Service request details showing request #355 with ID, status, date, photo, service type, location, and jurisdiction-defined custom questions.*

- **Request ID**: Unique tracking number (e.g., #355)
- **Status**: Current state of the request (displayed as a badge, e.g., "Open")
- **Date Submitted**: When the request was created
- **Photo**: Images uploaded by the reporter (if any)
- **Service Type**: Category of the issue (e.g., "Trail Maintenance")
- **Location**: Complete address information
- **Custom Questions**: Additional details specific to the service type, as defined by jurisdiction administrators
  - These questions vary by service type
  - Examples: "What is the issue?", "Does this issue impact walkability?"
  - Answers provided by the person who submitted the request

### Creating a Service Request

Public users can submit service requests without logging in. The creation process uses a multi-step wizard to gather all necessary information.

#### Step 1: Select a Request Category

1. Click "Create Request" or "New Service Request" button
2. Browse available service categories (e.g., Roads, Parks, Public Safety)
3. Select the specific service type that matches your issue
   - Examples: Pothole, Graffiti, Broken Streetlight, Tree Trimming

#### Step 2: Select Location

**Option A: Use Your Current Location**
- Click "Use My Location" to automatically detect your position
- Requires location permission in your browser

**Option B: Search for an Address**
- Enter an address in the search box
- Select from the autocomplete suggestions
- The map will center on the selected location

**Option C: Click on the Map**
- Manually click on the map to place a marker
- Adjust the marker position by dragging it

**Tips:**
- Be as precise as possible with the location
- You can zoom in to pinpoint the exact spot
- The marker shows where the issue will be recorded

#### Step 3: Describe the Issue

Fill in the service request details:

**Required fields** (marked with asterisk):
- **Description**: Explain the issue clearly and concisely
- **Additional details**: Provide context that helps responders understand the situation

**Service-specific questions:**
- Different service types may have custom fields
- Examples: "Size of pothole", "Color of graffiti", "Number of affected streetlights"
- Answer all applicable questions

**Best practices:**
- Be specific and descriptive
- Include landmarks or cross streets for easier identification
- Mention safety hazards if applicable

#### Step 4: Upload Photos (Optional)

Photos help responders understand and prioritize the issue.

**How to upload:**
1. Click "Add Photo" or "Upload Image"
2. Choose a photo from your device
3. You can upload multiple photos
4. Review photos before submitting

**Photo guidelines:**
- Clear, well-lit photos work best
- Show the issue from multiple angles if helpful
- Include context (surroundings) in at least one photo
- Avoid including people's faces or vehicle license plates

**Note:** Image uploads are subject to content moderation for safety and appropriateness.

#### Step 5: Provide Contact Information (Optional)

Contact information helps staff follow up with you about your request.

**Fields:**
- **Name**: Your full name
- **Email**: Contact email address
- **Phone**: Contact phone number

**Privacy:**
- Contact information is optional
- Used only for follow-up communication
- Not displayed publicly with the request

#### Step 6: Review and Submit

1. Review all information you've entered
2. Make changes by clicking "Back" or editing specific sections
3. Complete any required captcha verification
4. Click "Submit" to create the request

**After submission:**
- You'll receive a confirmation with your request ID
- Write down or screenshot your request ID for future reference
- You can search for your request using the ID or location

### Searching and Filtering

#### Search by Request ID

If you have a service request ID:
1. Enter the ID in the search box
2. Press Enter or click Search
3. The matching request will be displayed

#### Filter by Service Type

1. Use the service type dropdown or filter menu
2. Select one or more service categories
3. The map and list will update to show only matching requests

#### Filter by Status

View requests by their current status:
- **Open**: Recently submitted, not yet addressed
- **In Progress**: Being worked on by staff
- **Closed**: Completed or resolved

#### Filter by Date Range

1. Select a start date
2. Select an end date
3. View only requests submitted within that timeframe

#### Geographic Filtering

On the map view:
- Pan to your area of interest
- Requests visible on the current map view are automatically filtered
- Use "Search this area" to apply the geographic filter

---

## Administrator Guide

Administrators have additional capabilities for managing service requests and configuring the system.

### Logging In

1. Click "Login" in the navigation menu
2. Enter your credentials:
   - Username or email
   - Password
3. Click "Sign In"
4. You'll be redirected to the authenticated view

**Authentication:**
- Libre311 uses UnityAuth for secure authentication
- First-time users must be registered by a system administrator
- Contact your system administrator for account setup

### Managing Service Requests

#### Table View

Administrators have access to a table view with enhanced functionality.

**Accessing table view:**
- After logging in, click "Table" in the navigation
- Available only to authenticated users

**Table features:**
- Comprehensive list of all service requests
- Sortable columns
- Pagination controls
- Bulk actions (when available)
- Export capabilities

**Columns displayed:**
- Request ID
- Service Type
- Status
- Location
- Submitted Date
- Priority (if configured)
- Assigned To (if configured)

#### Viewing Request Details

Click any request in the table to view full details, including:
- All public information
- Internal notes (admin-only)
- Assignment information
- Status history
- Related requests

#### Updating Service Requests

**Changing status:**
1. Open a service request
2. Click the status dropdown
3. Select new status:
   - Open
   - In Progress
   - Closed
4. Optionally add a note about the status change
5. Save changes

**Adding internal notes:**
- Internal notes are visible only to administrators
- Use notes to communicate with other staff
- Document actions taken or additional context

**Updating request information:**
- Edit description or details if needed
- Correct location if misplaced
- Update service type if miscategorized
- Add priority levels
- Assign to specific staff members or departments

**Best practices:**
- Update status promptly to keep the public informed
- Add notes when making changes for accountability
- Use descriptive status updates
- Close requests when fully resolved

#### Bulk Operations

For managing multiple requests efficiently:
1. Select multiple requests using checkboxes
2. Choose a bulk action from the menu
3. Common bulk actions:
   - Change status
   - Assign to department
   - Export selected requests

### Configuring Service Definitions

Administrators can customize the types of service requests available.

#### Managing Service Groups

Service groups organize related service types into categories.

**Accessing groups:**
- Navigate to "Groups" in the admin menu
- View all existing service groups

**Creating a new group:**
1. Click "Add Group" or "New Group"
2. Enter group name (e.g., "Transportation", "Parks & Recreation")
3. Optionally add a description
4. Save the group

**Editing a group:**
1. Click on an existing group
2. Update name or description
3. Save changes

**Deleting a group:**
- Groups can only be deleted if no services are assigned
- Remove all services first, then delete the group

#### Managing Service Types

Service types are the specific issues that users can report.

**Creating a new service type:**
1. Navigate to a service group
2. Click "Add Service" or "New Service"
3. Configure service details:
   - **Service name**: (e.g., "Pothole", "Graffiti Removal")
   - **Description**: What this service covers
   - **Keywords**: For search and filtering
4. Save the service

**Configuring service attributes:**

Service attributes are custom fields specific to a service type.

1. Open a service type
2. Click "Add Attribute"
3. Configure the attribute:
   - **Field name**: Label for the field
   - **Field type**:
     - Text input
     - Text area
     - Dropdown/Select
     - Checkbox
     - Number
     - Date
   - **Required**: Whether users must complete this field
   - **Options**: For dropdown fields, define available choices
4. Reorder attributes by dragging
5. Save changes

**Example service configuration:**

Service: "Pothole Report"
- Attribute 1: "Approximate size" (Dropdown: Small/Medium/Large)
- Attribute 2: "Traffic impact" (Dropdown: None/Minor/Major)
- Attribute 3: "Additional details" (Text area, optional)

**Editing service types:**
- Click on a service to edit its configuration
- Update name, description, or attributes
- Changes apply to new requests only

**Deactivating services:**
- Services can be deactivated instead of deleted
- Deactivated services are hidden from public view
- Existing requests remain accessible

#### Service Definition Best Practices

- Use clear, citizen-friendly service names
- Group related services logically
- Keep required fields to a minimum
- Provide dropdown options when possible to standardize responses
- Include helpful descriptions and examples
- Test new service types before making them public

---

## Mobile Experience

Libre311 is optimized for mobile devices with responsive design.

### Mobile-Specific Features

**Touch-optimized interface:**
- Large, tappable buttons and controls
- Swipe gestures for navigation
- Pinch-to-zoom on maps

**Location services:**
- One-tap location detection using GPS
- Faster request creation with automatic location

**Camera integration:**
- Take photos directly from the app
- Instant upload without saving to device

**Offline considerations:**
- Internet connection required for all features
- Draft requests are not saved if you navigate away
- Take photos first if in low-connectivity areas

### Mobile Navigation

- Hamburger menu (≡) for main navigation
- Bottom navigation bar for primary actions
- Swipe back gestures where supported
- Collapsible filters and search

---

## Accessibility

Libre311 is designed to be accessible to all users.

### Keyboard Navigation

- All interactive elements are keyboard-accessible
- Tab through controls in logical order
- Enter or Space to activate buttons
- Escape to close dialogs and menus

### Screen Reader Support

- Semantic HTML structure
- ARIA labels and descriptions
- Meaningful link text
- Status announcements for dynamic content

### Visual Accessibility

- Sufficient color contrast
- Not relying on color alone to convey information
- Resizable text (browser zoom supported)
- Focus indicators on interactive elements

### Alternative Text

- All images have descriptive alt text
- Map markers include text descriptions
- Icons accompanied by text labels where appropriate

---

## Troubleshooting

### Common Issues

#### "Cannot detect my location"

**Solutions:**
- Grant location permission when prompted
- Check browser location settings
- Try searching for your address instead
- Ensure location services are enabled on your device

#### "Photo upload failed"

**Solutions:**
- Check your internet connection
- Ensure photo is under size limit (typically 10MB)
- Try a different photo format (JPEG, PNG)
- Make sure photo content is appropriate

#### "Cannot submit request"

**Solutions:**
- Check all required fields are completed
- Verify location is selected
- Complete captcha verification if present
- Try refreshing the page
- Check internet connection

#### "Request ID not found"

**Solutions:**
- Double-check the request ID for typos
- Ensure request was successfully submitted
- Try searching by location instead
- Contact support if request should exist

#### "Page not loading"

**Solutions:**
- Refresh the browser
- Clear browser cache and cookies
- Try a different browser
- Check internet connection
- Verify the Libre311 service is operational

### Getting Help

**For public users:**
- Contact information should be available on the homepage
- Check your jurisdiction's website for support resources
- Email or call the number provided by your municipality

**For administrators:**
- Refer to technical documentation
- Contact system administrator
- Check application logs for errors
- Review Open311 API documentation at [open311.org](https://www.open311.org)

### Browser Compatibility

Libre311 works best on modern browsers:
- Chrome/Chromium (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

**Mobile browsers:**
- Safari on iOS
- Chrome on Android
- Samsung Internet

**Note:** Internet Explorer is not supported.

---

## Additional Resources

### Open311 Standard

Learn more about Open311 at [www.open311.org](https://www.open311.org)

### API Access

Libre311 provides a RESTful API compatible with Open311 GeoReport v2. API documentation is available at `/api/docs` on your Libre311 instance.

### Privacy Policy

Refer to your jurisdiction's privacy policy regarding:
- Data collection and storage
- Contact information usage
- Image content moderation
- Request data retention

---

## Glossary

**Service Request**: A report of a non-emergency civic issue submitted by a resident

**Open311**: An open standard for civic issue tracking and management

**Service Type**: A category of issue that can be reported (e.g., Pothole, Graffiti)

**Service Group**: A collection of related service types (e.g., Transportation, Parks)

**Service Definition**: The configuration of a service type including custom fields

**Attribute**: A custom field specific to a service type

**Request ID**: A unique identifier assigned to each service request

**Status**: The current state of a service request (Open, In Progress, Closed)

**GeoReport**: The Open311 specification for location-based service requests

---

*This users guide is for Libre311, an open-source Open311 implementation. For technical documentation and source code, visit the project repository.*
