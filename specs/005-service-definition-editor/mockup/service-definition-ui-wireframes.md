# Service Definition Editor - UX Specification

This document describes the split-pane tree view interface for creating and editing Service Request definitions in Libre311, featuring Google Forms-style question editing.

## Concept Mapping

| Google Forms | Libre311 | Description |
|--------------|----------|-------------|
| Form | Service Request | A type of issue citizens can report |
| Form Title | Service Name | e.g., "Transportation", "Pothole Repair" |
| Form Description | Service Description | Brief explanation shown to citizens |
| Question | Attribute | A field that collects information |
| Answer Type | Attribute DataType | STRING, TEXT, NUMBER, DATETIME, SINGLEVALUELIST, MULTIVALUELIST |
| Options | Attribute Values | Choices for dropdown/multi-select fields |
| Required toggle | Required flag | Whether the field must be filled |

### Option Indicator Styles

| Type | Indicator | Description |
|------|-----------|-------------|
| Multiple choice (MULTIVALUELIST) | `[ ]` Square checkbox | Allows selecting multiple options |
| Dropdown (SINGLEVALUELIST) | `O` Circle radio | Allows selecting only one option |

---

## Main Layout: Split-Pane Interface

The interface uses a split-pane layout with a tree navigation panel on the left and an editor panel on the right.

```
+-----------------------------------------------------------------------------------+
| <- Admin          Service Definition Configuration                                |
+----------------+------------------------------------------------------------------+
|                |                                                                  |
|  SERVICES      |                      EDITOR PANEL                                |
|  [+ Group]     |                                                                  |
|                |  Shows either:                                                   |
|  v Roads &     |  - Group editor (when group selected)                            |
|    Traffic     |  - Service editor (when service selected)                        |
|    > Pothole   |  - Empty state (when nothing selected)                           |
|    > Street.   |                                                                  |
|    > Traffic   |                                                                  |
|    [+ Add]     |                                                                  |
|                |                                                                  |
|  v Community   |                                                                  |
|    Input       |                                                                  |
|    > Trans.*   |                                                                  |
|    > Access.   |                                                                  |
|    [+ Add]     |                                                                  |
|                |                                                                  |
|  > Sanitation  |                                                                  |
|    (3)         |                                                                  |
|                |                                                                  |
+----------------+------------------------------------------------------------------+
```

---

## Left Panel: Tree View

### Tree Structure

The tree panel displays groups and their service types hierarchically.

```
+------------------+
| SERVICES [+Group]|
+------------------+
|                  |
| v Roads &        |  <- Expand/collapse chevron
|   Traffic     3  |  <- Service count badge (includes all services)
|   +-------------+
|   | :: > Pothole|  <- Drag handle (::) for service reordering
|   | :: > Street.|
|   | :: > Traffic|
|   | [+ Add Svc] |
|   +-------------+
|                  |
| v Community      |
|   Input       2  |
|   +-------------+
|   | :: > Trans. |  <- Selected (highlighted)
|   | :: > Access.|
|   | [+ Add Svc] |
|   +-------------+
|                  |
| > Sanitation  3  |  <- Collapsed group
|                  |
+------------------+
```

**Note**: Service count badge shows the total number of services in the group, including hidden/inactive services.

### Tree Item States

| State | Visual Treatment |
|-------|------------------|
| Default | Normal text, light background |
| Hover | Slightly darker background, drag handle visible |
| Selected | Purple left border, blue-tinted background |
| Dragging | Semi-transparent (50% opacity) |
| Drop target | Purple insertion line with dot indicator |

### Drag and Drop Behaviors

1. **Service Reordering**: Drag services within a group to reorder
2. **Service Moving**: Drag services between groups to move them
3. **Drop Indicator**: Purple line with dot shows insertion point

```
| :: > Pothole    |
|-----------------|  <- Drop indicator line
| :: > Streetlight|
```

---

## Right Panel: Group Editor

When a group is selected, the editor shows group properties.

```
+------------------------------------------------------------------+
|                                                                  |
|  +------------------------------------------------------------+  |
|  |########################################################### |  |  <- Purple top border
|  |                                                            |  |
|  |  GROUP NAME                                                |  |
|  |  +------------------------------------------------------+  |  |
|  |  | Roads & Traffic                                      |  |  |
|  |  +------------------------------------------------------+  |  |
|  |                                                            |  |
|  +------------------------------------------------------------+  |
|  |  [Delete]                                         [Save]   |  |
|  +------------------------------------------------------------+  |
|                                                                  |
+------------------------------------------------------------------+
```

### Group Editor Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| Group Name | Text input | Yes | Display name for the group |

---

## Right Panel: Service Type Editor

When a service is selected, the editor shows the full Google Forms-style interface.

### Layout Structure

```
+------------------------------------------------------------------+
|                                                                  |
|  +------------------------------------------------------------+  |
|  |########################################################### |  |  <- Purple top border
|  |                                                            |  |
|  |  Transportation Survey                                     |  |  <- Service name
|  |  ---------------------------------------------------------  |  |
|  |  Survey to understand community transportation needs       |  |  <- Description
|  |                                                            |  |
|  +------------------------------------------------------------+  |
|                                                                  |
|  +------------------------------------------------------------+  |
|  |  Describe getting around without a car... *                |  |  <- Collapsed card
|  |  Long answer text                                          |  |
|  +------------------------------------------------------------+  |
|                                                                  |
|  +------------------------------------------------------------+  |
|  |                          ::                                |  |  <- Active card
|  |  +-------------------------------------------+  +--------+ |  |     (drag handle)
|  |  | What mode of transportation do you use... |  |Multiple| |  |
|  |  +-------------------------------------------+  | choice | |  |
|  |                                                 +--------+ |  |
|  |  :: [ ] Walking                                        x   |  |  <- Checkboxes for
|  |  :: [ ] Biking                                         x   |  |     multiple choice
|  |  :: [ ] Bus / public transit                           x   |  |
|  |  :: [ ] Personal vehicle                               x   |  |
|  |                                                            |  |
|  |  [ ] Add option                                            |  |
|  |  --------------------------------------------------------- |  |
|  |        [Cancel] [Save] | [Copy] [Delete] | Required O-- | :|  |
|  +------------------------------------------------------------+  |
|                                                                  |
|  +------------------------------------------------------------+  |
|  |  Race                                                      |  |  <- Collapsed card
|  |  O White  O Black or African American  O American...       |  |  <- Circles for dropdown
|  +------------------------------------------------------------+  |
|                                                                  |
|  + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+  |
|  |                         (+)                                |  |  <- Add question
|  |                    Add question                            |  |
|  + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+  |
|                                                                  |
|  +------------------------------------------------------------+  |
|  |  [Delete Service]                                          |  |  <- Footer with delete action
|  +------------------------------------------------------------+  |
|                                                                  |
+------------------------------------------------------------------+
```

---

## Component Details

### 1. Header Card (Service Info)

The top card with a colored banner identifies the Service being edited.

```
+------------------------------------------------------------+
|############################################################|  <- Purple banner (10px)
|                                                            |
|  +------------------------------------------------------+  |
|  | Transportation Survey                                |  |  <- Service name input
|  +------------------------------------------------------+  |
|                                                            |
|  +------------------------------------------------------+  |
|  | Survey to understand community transportation needs  |  |  <- Description textarea
|  +------------------------------------------------------+  |
|                                                            |
|  --------------------------------------------------------- |
|                                      [Cancel]  [Save]      |  <- Save/Cancel for header
+------------------------------------------------------------+

Fields:
- Service Name -> Service.serviceName
- Description -> Service.description
```

### 2. Attribute Card - Collapsed State

When an attribute is not being edited, it shows a compact preview.

```
+------------------------------------------------------------+
|                                                            |
|  Describe getting around without a car in your community * |  <- Question text
|  Long answer text                                          |  <- Field type hint
|                                                            |
+------------------------------------------------------------+

+------------------------------------------------------------+
|                                                            |
|  What mode of transportation do you use? *                 |  <- Question text
|  [ ] Walking  [ ] Biking  [ ] Bus...                       |  <- Checkboxes for MULTIVALUELIST
|                                                            |
+------------------------------------------------------------+

+------------------------------------------------------------+
|                                                            |
|  Race                                                      |  <- Question text
|  O White  O Black or African American  O American...       |  <- Circles for SINGLEVALUELIST
|                                                            |
+------------------------------------------------------------+

- Shows the question text (description)
- Shows placeholder hint OR option preview based on field type:
  - STRING: "Short answer text"
  - TEXT: "Long answer text"
  - NUMBER: "Number"
  - DATETIME: "Date"
  - MULTIVALUELIST: Shows `[ ]` checkboxes with option names
  - SINGLEVALUELIST: Shows `O` circles with option names
- Asterisk (*) indicates required
- Click anywhere to expand and edit
```

### 3. Attribute Card - Expanded State (Active Editing)

When clicked, the card expands to show all editing controls.

```
+------------------------------------------------------------+
|                            ::                              |  <- Drag handle
|  --------------------------------------------------------- |
|                                                            |
|  +-------------------------------------+     +-----------+ |
|  | What mode of transportation do you  |     | Multiple  | |  <- Question input
|  | use on a weekly basis?              |     | choice  v | |  <- Type dropdown
|  +-------------------------------------+     +-----------+ |
|                                                            |
|  :: [ ] Walking                                        x   |  <- Checkbox for multi-select
|  :: [ ] Mobility device (e.g. wheelchair, walker)      x   |     with drag handle and
|  :: [ ] Biking                                         x   |     delete button
|  :: [ ] Motorcycle or moped                            x   |
|  :: [ ] Personal vehicle (e.g. car, truck)             x   |
|  :: [ ] Bus / public transit                           x   |
|                                                            |
|  [ ] Add option                                            |  <- Add new option
|                                                            |
|  --------------------------------------------------------- |
|                                                            |
|  [Cancel] [Save] | [Copy] [Delete] | Required [==O]  :     |  <- Footer controls
|                                                            |
+------------------------------------------------------------+
```

**Note:** For Dropdown (SINGLEVALUELIST) types, circles `O` are shown instead of checkboxes `[ ]`.

**Element Breakdown:**

| Element | Description | Data Mapping |
|---------|-------------|--------------|
| :: (drag handle) | Reorder attributes | `attributeOrder` |
| Question text field | The prompt shown to users | `description` |
| Type dropdown | Select field type | `datatype` |
| Option rows | Available choices | `AttributeValue.valueName` |
| [ ] or O indicator | Checkbox for multi-select, circle for single-select | - |
| :: on options | Drag handle for reordering options | Option order |
| x buttons | Delete individual options | - |
| Add option | Create new choice | - |
| [Cancel] | Revert changes to this attribute | - |
| [Save] | Save changes to this attribute (disabled until changes made) | - |
| [Copy] | Duplicate this attribute | - |
| [Delete] | Remove attribute | - |
| Required toggle | Mark as mandatory | `required` |
| : More options | Additional settings | `datatypeDescription`, etc. |

### 4. Answer Type Dropdown

Maps to `AttributeDataType` enum:

```
+-------------------------------------+
| Multiple choice                   v |
+-------------------------------------+
|  O Short answer                     |  -> STRING
|  O Paragraph                        |  -> TEXT
|  * Multiple choice                  |  -> MULTIVALUELIST
|  O Dropdown                         |  -> SINGLEVALUELIST
|  O Number                           |  -> NUMBER
|  O Date                             |  -> DATETIME
+-------------------------------------+
```

---

## Interaction Behaviors

### Selection and Navigation

1. **Clicking a group**: Shows group editor, highlights group in tree
2. **Clicking a service**: Shows service editor, highlights service in tree
3. **Unsaved changes warning**: Modal appears when switching with pending changes

### Unsaved Changes Modal

```
+------------------------------------------+
|                                          |
|  Unsaved Changes                         |
|                                          |
|  You have unsaved changes. Do you want   |
|  to save them before leaving?            |
|                                          |
|                    [Discard]    [Save]   |
|                                          |
+------------------------------------------+
```

### Save/Cancel Behavior

**Attribute-level (each question card):**
- **Save button**: Disabled until changes are made to that specific attribute, saves only that attribute
- **Cancel button**: Reverts changes to that specific attribute
- Each question is saved independently - no service-level save button

**Header card (service name/description):**
- **Save button**: Saves changes to the service name and/or description
- **Cancel button**: Reverts changes to the service name and description
- Changes are independent of attribute changes

### Clicking a Collapsed Card

- Card expands to show full editing interface
- Previously expanded card collapses
- Only one card expanded at a time

### Drag to Reorder Questions

1. Grab the :: handle at top of expanded card
2. Drag to new position
3. Insertion point shown visually
4. Updates `attributeOrder` on drop
5. Marks changes as unsaved

### Drag to Reorder Options

1. Grab the :: handle next to option
2. Drag to new position within list
3. Updates option order on drop
4. Marks changes as unsaved

### Adding a Question

1. Click the "+ Add question" card at the bottom of the questions list
2. New question card is created and automatically expanded (active)
3. Defaults to "Short answer" type
4. Question input shows greyed placeholder text: "Question"
5. Below the question input, greyed placeholder text shows: "Short answer text"
6. Focus automatically moves to the question input field
7. All other question cards collapse when new card is added

```
New Question Card (Expanded):
+------------------------------------------------------------+
|                            ::                              |  <- Drag handle
|  --------------------------------------------------------- |
|                                                            |
|  +-------------------------------------+     +-----------+ |
|  | Question                            |     | Short   v | |  <- Placeholder text
|  |        (greyed placeholder)         |     | answer    | |     (greyed)
|  +-------------------------------------+     +-----------+ |
|                                                            |
|  Short answer text                                         |  <- Type hint
|        (greyed placeholder)                                |     (greyed)
|                                                            |
|  --------------------------------------------------------- |
|                                                            |
|  [Cancel] [Save] | [Copy] [Delete] | Required O--  :       |
|                                                            |
+------------------------------------------------------------+
```

### Adding an Option (for list types)

1. Click "Add option" row
2. New empty option appears
3. Focus moves to new option's text field
4. Type value and press Enter or click away

### Deleting an Option

- Click x next to any option
- Option removed immediately (no confirmation)
- Cannot delete if only one option remains

### Required Toggle

- Toggle switch in card footer
- Marks changes as unsaved (not auto-saved)
- Asterisk (*) appears/disappears in collapsed view on save

### Duplicate Attribute

- Click [Copy] icon
- Creates copy of attribute with all options
- New attribute appears below original
- Name appended with "(copy)"

### Delete Attribute

- Click [Delete] icon
- Confirmation dialog appears
- On confirm, attribute removed with animation

---

## Complete Example: Transportation Survey

```
+----------------+------------------------------------------------------------------+
|                |                                                                  |
| SERVICES       | +------------------------------------------------------------+  |
| [+ Group]      | |############################################################ |  |
|                | |                                                            |  |
| v Roads &      | |  Transportation Survey                                     |  |
|   Traffic    3 | |  ---------------------------------------------------------  |  |
|   > Pothole    | |  Survey to understand community transportation needs       |  |
|   > Street.    | |                                              [Cancel][Save] |  |
|   > Traffic    | +------------------------------------------------------------+  |
|   [+ Add]      |                                                                  |
|                | +------------------------------------------------------------+  |
| v Community    | |  Describe getting around without a car... *                |  |
|   Input      2 | |  Long answer text                                          |  |
|   > Trans. *   | +------------------------------------------------------------+  |
|   > Access.    |                                                                  |
|   [+ Add]      | +------------------------------------------------------------+  |
|                | |                          ::                                |  |
| > Sanitation 3 | |  +-------------------------------------------+ +--------+  |  |
|                | |  | What mode of transportation do you use... | |Multiple|  |  |
|                | |  +-------------------------------------------+ | choice |  |  |
|                | |                                                +--------+  |  |
|                | |  :: [ ] Walking                                        x   |  |
|                | |  :: [ ] Mobility device                                x   |  |
|                | |  :: [ ] Biking                                         x   |  |
|                | |  :: [ ] Personal vehicle                               x   |  |
|                | |  :: [ ] Bus / public transit                           x   |  |
|                | |                                                            |  |
|                | |  [ ] Add option                                            |  |
|                | |  ---------------------------------------------------------  |  |
|                | |      [Cancel] [Save] | [Copy] [Delete] | Required O-- | :  |  |
|                | +------------------------------------------------------------+  |
|                |                                                                  |
|                | +------------------------------------------------------------+  |
|                | |  Race                                                      |  |
|                | |  O White  O Black or African American  O American...       |  |
|                | +------------------------------------------------------------+  |
|                |                                                                  |
|                | + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+  |
|                | |                    (+) Add question                        |  |
|                | + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+  |
|                |                                                                  |
|                | +------------------------------------------------------------+  |
|                | |  [Delete Service]                                          |  |
|                | +------------------------------------------------------------+  |
|                |                                                                  |
+----------------+------------------------------------------------------------------+
```

---

## Data Model Mapping

### Group -> ServiceGroup Entity

```
Group Editor Fields:
+------------------------------------------------------------+
| Group Name          ->  ServiceGroup.name                   |
+------------------------------------------------------------+
```

### Service -> Service Entity

```
Header Card Fields:
+------------------------------------------------------------+
| Service Name        ->  Service.serviceName                 |
| Description         ->  Service.description                 |
| (Group assignment)  ->  Service.serviceGroup                |
| (Display order)     ->  Service.orderPosition               |
+------------------------------------------------------------+
```

### Attribute Card -> ServiceDefinitionAttribute Entity

```
Attribute Card Fields:
+------------------------------------------------------------+
| Question text       ->  ServiceDefinitionAttribute.description        |
| Answer type dropdown->  ServiceDefinitionAttribute.datatype           |
| Required toggle     ->  ServiceDefinitionAttribute.required           |
| Card position       ->  ServiceDefinitionAttribute.attributeOrder     |
| Help text (in : menu)->  ServiceDefinitionAttribute.datatypeDescription|
| variable (always true)->  ServiceDefinitionAttribute.variable         |
+------------------------------------------------------------+
```

### Option Rows -> AttributeValue Entities

```
For SINGLEVALUELIST / MULTIVALUELIST types:
+------------------------------------------------------------+
| O Walking          ->  AttributeValue.valueName = "Walking" |
| O Biking           ->  AttributeValue.valueName = "Biking"  |
+------------------------------------------------------------+
```

---

## State Management

### Unsaved Changes Tracking

The editor tracks unsaved changes at the card level (header card or attribute card):

1. Any edit to a card's fields marks that card as "dirty"
2. Save button on the card becomes enabled when dirty
3. Navigation triggers save/discard prompt when any card has unsaved changes

### Change Types That Trigger Dirty State

- Editing service/group name or description
- Adding/removing/editing attributes
- Reordering attributes (drag and drop)
- Adding/removing/editing options
- Reordering options (drag and drop)
- Toggling required flag
- Changing attribute type

### Save Behavior

- Each card (header or attribute) has its own Save button
- Save is explicit - click the Save button to persist changes
- Each card saves independently (no batch save for entire service)
- Success shows confirmation toast
- Tree view updates to reflect changes (e.g., name change)

---

## Implementation Notes

### Technology Stack

- **Framework**: SvelteKit (existing)
- **UI Components**: STWUI (existing)
- **Drag & Drop**: HTML5 Drag and Drop API or existing component
- **State Management**: Svelte stores / context

### Key Components to Build

1. **ServiceDefinitionEditor.svelte** - Main split-pane layout
2. **TreePanel.svelte** - Left panel with tree navigation
3. **TreeGroup.svelte** - Group item in tree
4. **TreeService.svelte** - Service item in tree
5. **GroupEditor.svelte** - Right panel for group editing
6. **ServiceEditor.svelte** - Right panel for service editing
7. **HeaderCard.svelte** - Service name/description card
8. **AttributeCard.svelte** - Individual field editor
9. **AttributeTypeSelector.svelte** - Dropdown for field types
10. **OptionsList.svelte** - Manage list options with drag/drop
11. **AttributeFooter.svelte** - Duplicate/delete/required controls
12. **UnsavedChangesModal.svelte** - Confirmation dialog

### API Integration

Uses existing endpoints from `JurisdictionAdminController`:

**Group Operations:**
- `GET /api/jurisdiction-admin/groups` - List groups
- `POST /api/jurisdiction-admin/groups` - Create group
- `PATCH /api/jurisdiction-admin/groups/{groupId}` - Update group
- `DELETE /api/jurisdiction-admin/groups/{groupId}` - Delete group

**Service Operations:**
- `POST /api/jurisdiction-admin/services` - Create service
- `PATCH /api/jurisdiction-admin/services/{serviceCode}` - Update service
- `DELETE /api/jurisdiction-admin/services/{serviceCode}` - Delete service
- `PATCH /api/jurisdiction-admin/groups/{groupId}/services-order` - Reorder services

**Attribute Operations:**
- `POST /api/jurisdiction-admin/services/{serviceCode}/attributes` - Add attribute
- `PATCH /api/jurisdiction-admin/services/{serviceCode}/attributes/{attributeCode}` - Update
- `DELETE /api/jurisdiction-admin/services/{serviceCode}/attributes/{attributeCode}` - Delete
- `PATCH /api/jurisdiction-admin/services/{serviceCode}/attributes-order` - Reorder

### Explicit Save Behavior

- Each card (header or attribute) tracks its own changes
- Changes are collected locally until the card's Save button is clicked
- No automatic saving on blur/change
- Each card's Save button sends only that card's changes to API
- Cancel on a card discards only that card's local changes
- Unsaved changes prompt on navigation if any card has pending changes
