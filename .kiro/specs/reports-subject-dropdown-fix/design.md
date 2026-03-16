# Reports Subject Dropdown Deduplication Bugfix Design

## Overview

The Reports & Analytics page displays duplicate and "Unknown" entries in the Subject dropdown because it directly maps over the allocations array without deduplication or validation. When a faculty member teaches the same subject to multiple classes, each allocation creates a separate dropdown option, resulting in duplicate subject entries. Additionally, allocations with invalid subjectIds (subjects that don't exist in the subjects list) display as "Unknown" in the dropdown.

The fix will filter and deduplicate the allocations before rendering dropdown options, ensuring only unique, valid subjects appear. This is a minimal, targeted fix that preserves all existing functionality while improving the user experience.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when the dropdown renders duplicate subjects or "Unknown" entries due to multiple allocations with the same subjectId or invalid subjectIds
- **Property (P)**: The desired behavior - dropdown displays only unique, valid subjects with no duplicates or "Unknown" entries
- **Preservation**: Existing dropdown functionality, subject name formatting, and report generation that must remain unchanged
- **allocations**: Array of ClassAllocation objects fetched from the backend, each containing a subjectId
- **subjects**: Array of Subject objects fetched from the backend, each with id, code, and name properties
- **getSubjectName**: Helper function that formats subject display as "code - name" or returns "Unknown" for invalid subjectIds
- **selectedSubject**: State variable storing the currently selected subjectId for report generation

## Bug Details

### Fault Condition

The bug manifests when the Subject dropdown is rendered in ReportsView.jsx. The component directly maps over the allocations array to create option elements, creating one option per allocation without checking for duplicate subjectIds or validating that the subjectId exists in the subjects list.

**Formal Specification:**
```
FUNCTION isBugCondition(allocations, subjects)
  INPUT: allocations of type Array<ClassAllocation>, subjects of type Array<Subject>
  OUTPUT: boolean
  
  RETURN (EXISTS allocation1, allocation2 IN allocations WHERE 
           allocation1.id != allocation2.id AND 
           allocation1.subjectId == allocation2.subjectId)
         OR (EXISTS allocation IN allocations WHERE 
             NOT EXISTS subject IN subjects WHERE subject.id == allocation.subjectId)
END FUNCTION
```

### Examples

- **Duplicate Subjects**: Faculty teaches "CS101 - Data Structures" to Class A and Class B. The dropdown shows:
  - CS101 - Data Structures (from allocation 1)
  - CS101 - Data Structures (from allocation 2)
  - Expected: CS101 - Data Structures (single entry)

- **Invalid SubjectId**: Allocation has subjectId "abc123" but no subject with that ID exists. The dropdown shows:
  - Unknown
  - Expected: This allocation should not appear in the dropdown

- **Mixed Valid and Invalid**: Faculty has 3 allocations: CS101 (valid), CS102 (valid), CS101 (duplicate), "xyz789" (invalid). The dropdown shows:
  - CS101 - Data Structures
  - CS102 - Algorithms
  - CS101 - Data Structures (duplicate)
  - Unknown
  - Expected: CS101 - Data Structures, CS102 - Algorithms (only unique, valid subjects)

- **Edge Case - All Invalid**: Faculty has allocations but all subjectIds are invalid. The dropdown shows:
  - Unknown
  - Unknown
  - Expected: Only the "Select Subject" placeholder (no valid options)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- The getSubjectName function must continue to format valid subjects as "code - name" and return "Unknown" for invalid subjectIds
- When a valid subject is selected, the selectedSubject state must continue to store the subjectId correctly
- Report generation must continue to use the selected subjectId without modification
- The allocations and subjects arrays must continue to be fetched and stored in state unchanged
- The dropdown's onChange handler and value binding must continue to work exactly as before
- All other UI elements (report type selector, date range inputs, buttons) must remain completely unaffected

**Scope:**
All functionality that does NOT involve rendering the subject dropdown options should be completely unaffected by this fix. This includes:
- Data fetching (allocations and subjects)
- Report generation logic
- PDF and CSV export functionality
- Chart rendering and analytics
- Date range selection
- Report type selection

## Hypothesized Root Cause

Based on the bug description and code analysis, the root cause is clear:

1. **No Deduplication Logic**: The dropdown rendering code (lines 295-300 in ReportsView.jsx) directly maps over the allocations array without checking for duplicate subjectIds:
   ```jsx
   {allocations.map(allocation => (
     <option key={allocation.id} value={allocation.subjectId}>
       {getSubjectName(allocation.subjectId)}
     </option>
   ))}
   ```

2. **No Validation Filter**: The code does not filter out allocations with invalid subjectIds before rendering. Every allocation creates an option, even if the subjectId doesn't exist in the subjects list.

3. **One-to-One Mapping Assumption**: The code assumes a one-to-one relationship between allocations and subjects, but in reality, multiple allocations can share the same subjectId (same subject taught to different classes).

## Correctness Properties

Property 1: Fault Condition - Unique Valid Subjects Only

_For any_ set of allocations where multiple allocations share the same subjectId or where some allocations have invalid subjectIds, the fixed dropdown rendering SHALL display only unique subjects that exist in the subjects list, with each valid subject appearing exactly once in the dropdown.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Existing Dropdown Functionality

_For any_ user interaction with the dropdown (selecting a subject, using the selected value for reports), the fixed code SHALL produce exactly the same behavior as the original code, preserving subject name formatting, state management, and report generation functionality.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `frontend/src/pages/faculty/ReportsView.jsx`

**Function**: ReportsView component (specifically the dropdown rendering section)

**Specific Changes**:
1. **Add Deduplication Logic**: Before rendering dropdown options, create a Set or use a filter to extract unique subjectIds from the allocations array
   - Use `Array.filter` with `Array.findIndex` to keep only the first occurrence of each subjectId
   - Or use a Set to track seen subjectIds and filter accordingly

2. **Add Validation Filter**: Filter out allocations where the subjectId does not exist in the subjects list
   - Check if `subjects.find(s => s.id === allocation.subjectId)` returns a valid subject
   - Only include allocations with valid subjectIds in the dropdown

3. **Combine Filters**: Apply both deduplication and validation in a single filtering operation before mapping to option elements
   - Filter allocations to keep only those with valid subjectIds
   - Then deduplicate by subjectId
   - Then map to option elements

4. **Preserve Key Attribute**: Ensure the key attribute remains unique (use allocation.id or subjectId)
   - Since we're deduplicating, we may need to use subjectId as the key instead of allocation.id

5. **No Changes to getSubjectName**: The helper function remains unchanged and continues to handle formatting

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm that duplicate and "Unknown" entries appear in the dropdown when the bug condition holds.

**Test Plan**: Create test scenarios with allocations that have duplicate subjectIds and invalid subjectIds. Render the component with unfixed code and inspect the dropdown options to observe duplicates and "Unknown" entries.

**Test Cases**:
1. **Duplicate Subjects Test**: Create allocations with duplicate subjectIds (e.g., two allocations for CS101). Verify dropdown shows duplicate entries (will fail on unfixed code - duplicates will appear)
2. **Invalid SubjectId Test**: Create an allocation with a subjectId that doesn't exist in subjects list. Verify dropdown shows "Unknown" entry (will fail on unfixed code - "Unknown" will appear)
3. **Mixed Scenario Test**: Create allocations with valid, duplicate, and invalid subjectIds. Verify dropdown shows duplicates and "Unknown" entries (will fail on unfixed code)
4. **All Invalid Test**: Create allocations where all subjectIds are invalid. Verify dropdown shows multiple "Unknown" entries (will fail on unfixed code)

**Expected Counterexamples**:
- Dropdown contains multiple options with the same subject name (duplicates)
- Dropdown contains options labeled "Unknown"
- Possible causes: no deduplication logic, no validation filter before rendering

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (duplicate or invalid subjectIds), the fixed dropdown renders only unique, valid subjects.

**Pseudocode:**
```
FOR ALL allocations, subjects WHERE isBugCondition(allocations, subjects) DO
  renderedOptions := renderDropdown_fixed(allocations, subjects)
  ASSERT all options have unique subjectIds
  ASSERT no options display "Unknown"
  ASSERT all options correspond to valid subjects in subjects list
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (all allocations have unique, valid subjectIds), the fixed dropdown produces the same result as the original dropdown.

**Pseudocode:**
```
FOR ALL allocations, subjects WHERE NOT isBugCondition(allocations, subjects) DO
  ASSERT renderDropdown_original(allocations, subjects) = renderDropdown_fixed(allocations, subjects)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across different allocation and subject combinations
- It catches edge cases that manual unit tests might miss (e.g., empty arrays, single allocation, all unique subjects)
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for scenarios with unique, valid subjectIds, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Unique Valid Subjects Preservation**: Create allocations where all subjectIds are unique and valid. Verify dropdown renders the same options before and after fix
2. **Subject Selection Preservation**: Select a subject from the dropdown and verify selectedSubject state is set correctly (same behavior before and after fix)
3. **getSubjectName Preservation**: Verify the helper function continues to format subjects as "code - name" and returns "Unknown" for invalid IDs
4. **Report Generation Preservation**: Select a subject and generate a report. Verify the correct subjectId is passed to the API (same behavior before and after fix)

### Unit Tests

- Test dropdown rendering with duplicate subjectIds (should show only unique subjects after fix)
- Test dropdown rendering with invalid subjectIds (should exclude invalid subjects after fix)
- Test dropdown rendering with mixed valid, duplicate, and invalid subjectIds
- Test edge case: empty allocations array (should show only placeholder)
- Test edge case: empty subjects array (should show only placeholder)
- Test edge case: all allocations have invalid subjectIds (should show only placeholder)

### Property-Based Tests

- Generate random allocations with varying degrees of duplication and verify dropdown shows only unique subjects
- Generate random allocations with some invalid subjectIds and verify dropdown excludes them
- Generate random allocations with all unique, valid subjectIds and verify dropdown behavior is preserved (same as original)
- Test that subject selection and report generation work correctly across many random scenarios

### Integration Tests

- Test full report generation flow: select subject from deduplicated dropdown, generate report, verify correct data
- Test switching between report types with deduplicated dropdown
- Test that PDF and CSV export work correctly with subjects selected from deduplicated dropdown
- Test that analytics charts render correctly with subjects selected from deduplicated dropdown
