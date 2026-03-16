# Bug Condition Exploration Test Results

## Test Execution Date
Executed on unfixed code

## Test Results Summary
All 3 tests FAILED as expected, confirming the bug exists.

## Counterexamples Found

### Test 1: Initial Class Selection
**Test**: `should update allocationId field in Formik when class is selected`
**Expected Behavior**: `allocationIdField.value` should be `'allocation-123'` after selecting a class
**Actual Behavior**: `allocationIdField.value` remains `''` (empty string)
**Counterexample**: When selecting allocation-123 from the dropdown, the field value is not updated

### Test 2: Class Change Scenario
**Test**: `should update allocationId field when changing class selection`
**Expected Behavior**: `allocationIdField.value` should be `'allocation-123'` after first selection
**Actual Behavior**: `allocationIdField.value` remains `''` (empty string)
**Counterexample**: When changing from one class to another, the field value is never updated

### Test 3: Form Submission Readiness
**Test**: `should have allocationId available in form values after class selection`
**Expected Behavior**: `allocationIdField.value` should be `'allocation-123'` and not empty
**Actual Behavior**: `allocationIdField.value` remains `''` (empty string)
**Counterexample**: After selecting a class, the allocationId is not available in the form for submission

## Root Cause Confirmation

The test failures confirm the root cause identified in the design document:

**Missing setFieldValue Call**: The `handleAllocationChange` function in `PerformanceEntry.jsx` (line 91) does NOT call `setFieldValue('allocationId', e.target.value)` to update the Formik field value.

Current implementation:
```javascript
const handleAllocationChange = (e, setFieldValue) => {
  const allocation = allocations.find(a => a.id === e.target.value);
  setSelectedAllocation(allocation);
  setFieldValue('studentId', '');  // Only resets studentId
  setStudents([]);
  setPerformanceRecords([]);
  // MISSING: setFieldValue('allocationId', e.target.value);
};
```

## Impact

The bug causes:
1. The `allocationId` field in Formik form state remains empty after class selection
2. Form submission may fail validation or submit incomplete data
3. Desynchronization between component state (`selectedAllocation`) and form state (`allocationId`)

## Next Steps

These tests will be re-run after implementing the fix to verify that:
1. The `allocationId` field is properly updated when a class is selected
2. The field value changes correctly when switching between classes
3. The form has the correct `allocationId` value available for submission
