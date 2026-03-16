# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Fault Condition** - Duplicate and Invalid Subject Detection
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Test concrete failing cases - duplicate subjectIds and invalid subjectIds in allocations
  - Test that dropdown renders duplicate entries when multiple allocations share the same subjectId
  - Test that dropdown renders "Unknown" entries when allocations contain invalid subjectIds
  - Test implementation details from Fault Condition in design: `isBugCondition(allocations, subjects)` returns true when duplicates or invalid subjects exist
  - The test assertions should match the Expected Behavior Properties from design: dropdown should display only unique, valid subjects
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found:
    - Duplicate subject entries in dropdown (e.g., "CS101 - Data Structures" appears twice)
    - "Unknown" entries in dropdown for invalid subjectIds
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Existing Dropdown Functionality
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs (unique, valid subjectIds)
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements:
    - Dropdown displays all unique subjects when allocations have different subjectIds
    - Subject selection sets selectedSubject state correctly
    - getSubjectName function formats subjects as "code - name" and returns "Unknown" for invalid IDs
    - Report generation uses the correct subjectId
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 3. Fix for duplicate and "Unknown" subject entries in Reports dropdown

  - [x] 3.1 Implement deduplication and validation logic
    - Add validation filter to exclude allocations with invalid subjectIds (subjectIds not in subjects list)
    - Add deduplication logic to keep only unique subjectIds from filtered allocations
    - Apply combined filter before mapping to dropdown options
    - Ensure key attribute uses subjectId for deduplicated options
    - Preserve getSubjectName function unchanged
    - _Bug_Condition: isBugCondition(allocations, subjects) where multiple allocations share the same subjectId OR allocations contain invalid subjectIds_
    - _Expected_Behavior: Dropdown displays only unique subjects that exist in subjects list, with each valid subject appearing exactly once (from Correctness Properties in design)_
    - _Preservation: Existing dropdown functionality (subject name formatting, state management, report generation) remains unchanged (from Preservation Requirements in design)_
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Unique Valid Subjects Only
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify dropdown shows only unique subjects (no duplicates)
    - Verify dropdown excludes invalid subjects (no "Unknown" entries)
    - _Requirements: Expected Behavior Properties from design (2.1, 2.2, 2.3)_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Existing Dropdown Functionality
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix:
      - Dropdown displays all unique subjects for different subjectIds
      - Subject selection works correctly
      - getSubjectName formatting preserved
      - Report generation uses correct subjectId

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
