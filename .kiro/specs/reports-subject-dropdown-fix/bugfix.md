# Bugfix Requirements Document

## Introduction

The Reports & Analytics page (ReportsView.jsx) has a bug in the Subject dropdown where it displays duplicate entries and "Unknown" entries. The dropdown is populated by directly mapping over the allocations array without deduplication or validation. When a faculty member teaches the same subject to multiple classes, the subject appears multiple times in the dropdown. Additionally, when an allocation contains a subjectId that doesn't exist in the subjects list, it displays as "Unknown". This creates a poor user experience and makes it difficult to select the correct subject for report generation.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a faculty member has multiple allocations with the same subjectId (e.g., teaching the same subject to different classes) THEN the system displays duplicate subject entries in the dropdown

1.2 WHEN an allocation contains a subjectId that does not exist in the subjects list THEN the system displays "Unknown" as the subject name in the dropdown

1.3 WHEN the dropdown is rendered THEN the system creates one option element for each allocation without checking for duplicate subjectIds

### Expected Behavior (Correct)

2.1 WHEN a faculty member has multiple allocations with the same subjectId THEN the system SHALL display only one entry for that subject in the dropdown

2.2 WHEN an allocation contains a subjectId that does not exist in the subjects list THEN the system SHALL exclude that allocation from the dropdown options

2.3 WHEN the dropdown is rendered THEN the system SHALL display unique subjects only, formatted as "code - name" with no duplicates or "Unknown" entries

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a faculty member has allocations with different subjectIds THEN the system SHALL CONTINUE TO display all unique subjects in the dropdown

3.2 WHEN a valid subject is selected from the dropdown THEN the system SHALL CONTINUE TO use the correct subjectId for report generation

3.3 WHEN the getSubjectName function is called with a valid subjectId THEN the system SHALL CONTINUE TO return the formatted subject name as "code - name"

3.4 WHEN the getSubjectName function is called with an invalid subjectId THEN the system SHALL CONTINUE TO return "Unknown" (this behavior is preserved for the function itself, but invalid subjects should not appear in the dropdown)

3.5 WHEN the allocations or subjects data is fetched THEN the system SHALL CONTINUE TO store them in state without modification
