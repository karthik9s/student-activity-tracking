# Bugfix Requirements Document

## Introduction

The faculty performance entry form has a bug in the class selection dropdown where the selected class value is not being properly maintained in the Formik form state. When a faculty member selects a class from the dropdown, the form expands to show additional fields (Student, Assessment Type, Marks, etc.), but the `allocationId` field value in Formik is not being set, causing the class selection to not be properly maintained. This occurs because the `handleAllocationChange` function updates the component state (`selectedAllocation`) but does not update the corresponding Formik field value (`allocationId`).

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a faculty member selects a class from the "Select Class" dropdown THEN the system updates `selectedAllocation` state but does not update the `allocationId` field value in Formik

1.2 WHEN a faculty member changes the class selection after initially selecting a class THEN the system does not properly maintain the new selection in the Formik form state

1.3 WHEN the form is submitted without the `allocationId` field being set THEN the system may fail validation or submit incomplete data

### Expected Behavior (Correct)

2.1 WHEN a faculty member selects a class from the "Select Class" dropdown THEN the system SHALL update both the `selectedAllocation` state AND set the `allocationId` field value in Formik to the selected allocation ID

2.2 WHEN a faculty member changes the class selection after initially selecting a class THEN the system SHALL properly update the `allocationId` field value in Formik to reflect the new selection

2.3 WHEN the form is submitted with a selected class THEN the system SHALL have the correct `allocationId` value available in the Formik form state

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a faculty member selects a class THEN the system SHALL CONTINUE TO expand the form to show Student, Assessment Type, Marks Obtained, Total Marks, and Remarks fields

3.2 WHEN a faculty member selects a class THEN the system SHALL CONTINUE TO fetch and display the list of students for that class

3.3 WHEN a faculty member selects a class THEN the system SHALL CONTINUE TO reset the `studentId` field to empty

3.4 WHEN a faculty member selects a class THEN the system SHALL CONTINUE TO clear the students list and performance records before fetching new data

3.5 WHEN a faculty member submits the performance form THEN the system SHALL CONTINUE TO validate all required fields and submit the performance data correctly

3.6 WHEN a faculty member views the page THEN the system SHALL CONTINUE TO display the list of allocated classes in the dropdown

3.7 WHEN the form is reset after successful submission THEN the system SHALL CONTINUE TO clear all form fields and maintain the current class selection
