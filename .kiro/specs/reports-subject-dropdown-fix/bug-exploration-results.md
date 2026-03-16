# Bug Condition Exploration Test Results

## Test Execution Date
Task 1 completed - Bug exploration tests written and executed on unfixed code.

## Test Results Summary

### EXPECTED OUTCOME: Tests FAILED ✓
The tests correctly FAILED on unfixed code, confirming the bug exists.

## Counterexamples Documented

### 1. Duplicate Subject Entries (Test 1)
**Bug Condition**: Multiple allocations share the same subjectId

**Test Setup**:
- 3 allocations: 2 with subjectId 'subject-cs101', 1 with 'subject-cs102'
- 2 subjects in database: CS101 and CS102

**Expected Behavior**: 2 unique dropdown options
**Actual Behavior on Unfixed Code**: 3 dropdown options

**Counterexample**:
```
Dropdown contains:
- CS101 - Data Structures (from allocation-1)
- CS101 - Data Structures (from allocation-2) ← DUPLICATE
- CS102 - Algorithms (from allocation-3)
```

**Test Failure Message**:
```
AssertionError: expected [ <option …(1)></option>, …(2) ] to have a length of 2 but got 3
```

### 2. "Unknown" Entries for Invalid SubjectIds (Test 2)
**Bug Condition**: Allocations contain subjectIds that don't exist in subjects list

**Test Setup**:
- 3 allocations: 2 with valid subjectIds, 1 with 'invalid-subject-id'
- 2 subjects in database: CS101 and CS102 (invalid-subject-id NOT in list)

**Expected Behavior**: 2 valid dropdown options only
**Actual Behavior on Unfixed Code**: 3 dropdown options including "Unknown"

**Counterexample**:
```
Dropdown contains:
- CS101 - Data Structures
- Unknown ← INVALID ENTRY
- CS102 - Algorithms
```

**Test Failure Message**:
```
AssertionError: expected [ 'CS101 - Data Structures', …(2) ] to not include 'Unknown'
```

### 3. Mixed Scenario - Duplicates AND Invalid SubjectIds (Test 3)
**Bug Condition**: Complex scenario with both duplicate and invalid subjectIds

**Test Setup**:
- 6 allocations:
  - 3 with subjectId 'subject-cs101' (duplicates)
  - 1 with subjectId 'subject-cs102'
  - 2 with invalid subjectIds
- 2 subjects in database: CS101 and CS102

**Expected Behavior**: 2 unique valid dropdown options
**Actual Behavior on Unfixed Code**: 6 dropdown options

**Counterexample**:
```
Dropdown contains:
- CS101 - Data Structures (from allocation-1)
- CS101 - Data Structures (from allocation-2) ← DUPLICATE
- Unknown (from allocation-3) ← INVALID
- CS102 - Algorithms (from allocation-4)
- CS101 - Data Structures (from allocation-5) ← DUPLICATE
- Unknown (from allocation-6) ← INVALID
```

**Test Failure Message**:
```
AssertionError: expected [ <option …(1)></option>, …(5) ] to have a length of 2 but got 6
```

### 4. Edge Case - All Invalid SubjectIds (Test 4)
**Bug Condition**: All allocations have invalid subjectIds

**Test Setup**:
- 3 allocations: all with invalid subjectIds
- 1 subject in database: CS101 (none of the allocation subjectIds match)

**Expected Behavior**: 0 dropdown options (only placeholder)
**Actual Behavior on Unfixed Code**: 0 dropdown options (only placeholder)

**Result**: ✓ PASSED - This edge case already works correctly

## Root Cause Confirmed

The bug is confirmed to exist in `frontend/src/pages/faculty/ReportsView.jsx` lines 295-300:

```jsx
{allocations.map(allocation => (
  <option key={allocation.id} value={allocation.subjectId}>
    {getSubjectName(allocation.subjectId)}
  </option>
))}
```

**Issues**:
1. **No Deduplication**: The code maps over ALL allocations without checking for duplicate subjectIds
2. **No Validation**: The code does not filter out allocations with invalid subjectIds before rendering
3. **One-to-One Mapping Assumption**: Each allocation creates one dropdown option, but multiple allocations can share the same subject

## Next Steps

The bug exploration tests are complete and have successfully surfaced counterexamples demonstrating:
- Duplicate subject entries when multiple allocations share the same subjectId
- "Unknown" entries when allocations contain invalid subjectIds
- Complex scenarios combining both issues

These tests will be used to validate the fix in subsequent tasks.
