# Performance Entry Class Selection Fix - Bugfix Design

## Overview

The bug occurs in the `handleAllocationChange` function in `PerformanceEntry.jsx` where selecting a class from the dropdown updates the component state (`selectedAllocation`) but fails to update the corresponding Formik field value (`allocationId`). This causes the form to lack the proper allocation ID when submitted, potentially leading to validation failures or incomplete data submission. The fix requires adding a single line to set the Formik field value when the allocation changes, ensuring both the component state and form state remain synchronized.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when a faculty member selects a class from the dropdown, the `allocationId` field in Formik is not updated
- **Property (P)**: The desired behavior when a class is selected - both `selectedAllocation` state and `allocationId` Formik field should be updated
- **Preservation**: Existing form expansion, student fetching, field reset, and submission behaviors that must remain unchanged by the fix
- **handleAllocationChange**: The event handler function in `PerformanceEntry.jsx` (line 91) that processes class selection changes
- **selectedAllocation**: Component state variable that stores the currently selected allocation object
- **allocationId**: Formik field value that should contain the ID of the selected allocation
- **setFieldValue**: Formik helper function used to programmatically update field values

## Bug Details

### Fault Condition

The bug manifests when a faculty member selects a class from the "Select Class" dropdown. The `handleAllocationChange` function updates the `selectedAllocation` component state and resets the `studentId` field, but it does not update the `allocationId` field value in Formik's form state.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type { event: ChangeEvent, setFieldValue: Function }
  OUTPUT: boolean
  
  RETURN input.event.target.value IN allocations.map(a => a.id)
         AND selectedAllocation is updated
         AND allocationId field in Formik is NOT updated
END FUNCTION
```

### Examples

- **Example 1**: Faculty selects "CSE - Data Structures - Year 2 - Section A" from dropdown
  - Expected: `selectedAllocation` state = allocation object, `allocationId` Formik field = allocation.id
  - Actual: `selectedAllocation` state = allocation object, `allocationId` Formik field = '' (empty)

- **Example 2**: Faculty changes selection from "CSE - Data Structures" to "CSE - Algorithms"
  - Expected: `selectedAllocation` state = new allocation object, `allocationId` Formik field = new allocation.id
  - Actual: `selectedAllocation` state = new allocation object, `allocationId` Formik field = '' (empty)

- **Example 3**: Faculty selects a class and then submits the form
  - Expected: Form submission includes correct `allocationId` value
  - Actual: Form submission has empty `allocationId`, potentially causing validation errors

- **Edge Case**: Faculty selects a class, form expands, then selects a different class
  - Expected: Both state and Formik field update to new allocation ID
  - Actual: Only state updates, Formik field remains empty

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Form expansion to show Student, Assessment Type, Marks Obtained, Total Marks, and Remarks fields when a class is selected
- Fetching and displaying the list of students for the selected class
- Resetting the `studentId` field to empty when allocation changes
- Clearing students list and performance records before fetching new data
- Form validation and submission logic for all fields
- Display of allocated classes in the dropdown
- Form reset behavior after successful submission

**Scope:**
All inputs and interactions that do NOT involve the initial class selection or class change should be completely unaffected by this fix. This includes:
- Student dropdown selection
- Assessment type selection
- Marks input fields
- Remarks textarea input
- Form submission button click
- Form validation triggers
- Performance records display
- Grade calculation and preview

## Hypothesized Root Cause

Based on the bug description and code analysis, the root cause is clear:

1. **Missing setFieldValue Call**: The `handleAllocationChange` function (line 91) calls `setFieldValue('studentId', '')` to reset the student field, but it does not call `setFieldValue('allocationId', e.target.value)` to update the allocation ID field.

2. **State-Form Desynchronization**: The function updates the component state (`setSelectedAllocation`) but not the Formik form state for the `allocationId` field, causing a desynchronization between what the component "knows" (the selected allocation) and what the form "knows" (no allocation ID).

3. **Incomplete Event Handler**: The event handler was implemented to handle UI state changes (expanding the form, fetching students) but was not fully integrated with Formik's form state management.

## Correctness Properties

Property 1: Fault Condition - Class Selection Updates Formik Field

_For any_ class selection event where a faculty member selects a valid allocation from the dropdown, the fixed handleAllocationChange function SHALL update both the selectedAllocation component state AND set the allocationId field value in Formik to the selected allocation's ID.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Non-Allocation Field Behavior

_For any_ form interaction that does NOT involve changing the class selection (student selection, marks input, assessment type selection, form submission, etc.), the fixed code SHALL produce exactly the same behavior as the original code, preserving all existing form functionality.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

The root cause analysis indicates a straightforward fix is needed.

**File**: `frontend/src/pages/faculty/PerformanceEntry.jsx`

**Function**: `handleAllocationChange` (line 91)

**Specific Changes**:
1. **Add setFieldValue for allocationId**: Add a call to `setFieldValue('allocationId', e.target.value)` immediately after finding the allocation object
   - This ensures the Formik field is updated when the dropdown value changes
   - The value should be set to `e.target.value` which contains the selected allocation ID

2. **Maintain Existing Logic**: Keep all existing lines unchanged:
   - `setSelectedAllocation(allocation)` - updates component state
   - `setFieldValue('studentId', '')` - resets student selection
   - `setStudents([])` - clears student list
   - `setPerformanceRecords([])` - clears performance records

**Modified Function**:
```javascript
const handleAllocationChange = (e, setFieldValue) => {
  const allocation = allocations.find(a => a.id === e.target.value);
  setSelectedAllocation(allocation);
  setFieldValue('allocationId', e.target.value);  // ADD THIS LINE
  setFieldValue('studentId', '');
  setStudents([]);
  setPerformanceRecords([]);
};
```

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code by verifying the `allocationId` field is not set, then verify the fix works correctly and preserves existing behavior.

### Exploratory Fault Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm that the `allocationId` field in Formik is not being updated when a class is selected.

**Test Plan**: Write tests that simulate selecting a class from the dropdown and assert that the `allocationId` field value in Formik is updated. Run these tests on the UNFIXED code to observe failures and confirm the root cause.

**Test Cases**:
1. **Initial Class Selection Test**: Select a class from dropdown, verify `allocationId` field is empty (will fail on unfixed code)
2. **Class Change Test**: Select one class, then select a different class, verify `allocationId` field is empty (will fail on unfixed code)
3. **Form State Inspection Test**: After selecting a class, inspect Formik values object to confirm `allocationId` is missing (will fail on unfixed code)
4. **Submission Readiness Test**: Select a class and attempt to access `values.allocationId` in submit handler, verify it's undefined (will fail on unfixed code)

**Expected Counterexamples**:
- `allocationId` field value remains empty string after class selection
- Formik values object shows `allocationId: ''` even when `selectedAllocation` state is populated
- Form submission may fail validation or submit incomplete data due to missing `allocationId`

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds (class selection events), the fixed function produces the expected behavior (both state and Formik field are updated).

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := handleAllocationChange_fixed(input.event, input.setFieldValue)
  ASSERT selectedAllocation is updated
  ASSERT allocationId field in Formik equals input.event.target.value
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold (all other form interactions), the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT originalBehavior(input) = fixedBehavior(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for student selection, marks input, form submission, etc., then write property-based tests capturing that behavior.

**Test Cases**:
1. **Student Selection Preservation**: Verify selecting a student from dropdown continues to work exactly as before
2. **Marks Input Preservation**: Verify entering marks in marksObtained and totalMarks fields continues to work
3. **Form Expansion Preservation**: Verify form still expands to show additional fields when class is selected
4. **Student Fetch Preservation**: Verify students are still fetched and displayed correctly
5. **Form Submission Preservation**: Verify form submission logic continues to work with all validations
6. **Grade Calculation Preservation**: Verify grade preview calculation continues to work correctly
7. **Performance Records Display Preservation**: Verify performance records table continues to display correctly

### Unit Tests

- Test that selecting a class updates both `selectedAllocation` state and `allocationId` Formik field
- Test that changing class selection updates `allocationId` to the new value
- Test that `studentId` field is still reset when allocation changes
- Test that students list and performance records are still cleared when allocation changes
- Test edge case where allocation is not found (should handle gracefully)

### Property-Based Tests

- Generate random allocation selections and verify `allocationId` field is always updated correctly
- Generate random sequences of allocation changes and verify field stays synchronized
- Generate random form interactions (student selection, marks input) and verify preservation of existing behavior
- Test that form submission always has access to correct `allocationId` value

### Integration Tests

- Test full flow: select class → form expands → students load → select student → enter marks → submit
- Test changing class mid-form: select class → enter data → change class → verify form resets correctly
- Test that submitted performance data includes correct allocation information
- Test visual feedback: verify dropdown value matches Formik field value at all times
