# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Class Selection Does Not Update Formik Field
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: For deterministic bugs, scope the property to the concrete failing case(s) to ensure reproducibility
  - Test that when a faculty member selects a class from the dropdown, the `allocationId` field in Formik is updated to the selected allocation ID
  - Test implementation details from Fault Condition in design: `isBugCondition(input)` where `input.event.target.value` is a valid allocation ID
  - The test assertions should match the Expected Behavior Properties from design: both `selectedAllocation` state AND `allocationId` Formik field are updated
  - Simulate class selection event with a valid allocation ID (e.g., "allocation-123")
  - Verify that `setFieldValue('allocationId', e.target.value)` is called with the correct allocation ID
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found: `allocationId` field remains empty after class selection
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Non-Allocation Field Behavior
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs (student selection, marks input, form submission, etc.)
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements
  - Property-based testing generates many test cases for stronger guarantees
  - Test that student dropdown selection continues to work exactly as before
  - Test that marks input fields (marksObtained, totalMarks) continue to work
  - Test that form expansion to show additional fields when class is selected continues to work
  - Test that students are fetched and displayed correctly when class is selected
  - Test that `studentId` field is reset when allocation changes
  - Test that students list and performance records are cleared when allocation changes
  - Test that form submission logic continues to work with all validations
  - Test that grade calculation and preview continue to work correctly
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 3. Fix for class selection not updating Formik field

  - [x] 3.1 Implement the fix
    - Add `setFieldValue('allocationId', e.target.value)` to the `handleAllocationChange` function in `PerformanceEntry.jsx`
    - Place the new line immediately after `setSelectedAllocation(allocation)` and before `setFieldValue('studentId', '')`
    - Ensure the value passed is `e.target.value` which contains the selected allocation ID
    - Maintain all existing logic: `setSelectedAllocation`, `setFieldValue('studentId', '')`, `setStudents([])`, `setPerformanceRecords([])`
    - _Bug_Condition: isBugCondition(input) where input.event.target.value is a valid allocation ID AND allocationId field in Formik is NOT updated_
    - _Expected_Behavior: Both selectedAllocation state AND allocationId Formik field are updated to the selected allocation's ID_
    - _Preservation: Form expansion, student fetching, field reset, students list clearing, performance records clearing, form validation, submission logic, and all other form interactions remain unchanged_
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Class Selection Updates Formik Field
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify that `allocationId` field in Formik is now updated when class is selected
    - Verify that `setFieldValue('allocationId', e.target.value)` is called correctly
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-Allocation Field Behavior
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)
    - Verify form expansion, student fetching, field reset, and all other behaviors remain unchanged
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
