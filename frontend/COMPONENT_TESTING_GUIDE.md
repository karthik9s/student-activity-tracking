# Component Testing Guide

## Overview
This guide outlines the testing strategy for React components in the Student Activity Tracking System.

## Testing Framework
- **Test Runner**: Vitest
- **Testing Library**: React Testing Library
- **Mocking**: vi (Vitest mocking utilities)

## Setup

### Install Dependencies
```bash
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event
```

### Configure Vitest
Create `vitest.config.js`:
```javascript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
  },
});
```

## Test Categories

### 1. Component Rendering Tests
Test that components render correctly with props:

```javascript
// Example: StudentManagement.test.jsx
import { render, screen } from '@testing-library/react';
import StudentManagement from '../pages/admin/StudentManagement';

describe('StudentManagement', () => {
  it('renders student management page', () => {
    render(<StudentManagement />);
    expect(screen.getByText(/student management/i)).toBeInTheDocument();
  });
});
```

### 2. User Interaction Tests
Test user interactions like clicks, form submissions:

```javascript
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

it('handles form submission', async () => {
  const user = userEvent.setup();
  render(<StudentForm />);
  
  await user.type(screen.getByLabelText(/name/i), 'John Doe');
  await user.click(screen.getByRole('button', { name: /submit/i }));
  
  expect(mockSubmit).toHaveBeenCalled();
});
```

### 3. Protected Route Tests
Test authentication and authorization:

```javascript
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ProtectedRoute from '../routes/ProtectedRoute';

it('redirects unauthenticated users', () => {
  const { container } = render(
    <MemoryRouter>
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    </MemoryRouter>
  );
  
  expect(container).not.toHaveTextContent('Protected Content');
});
```

### 4. Form Validation Tests
Test form validation logic:

```javascript
it('displays validation errors', async () => {
  render(<StudentForm />);
  
  const submitButton = screen.getByRole('button', { name: /submit/i });
  fireEvent.click(submitButton);
  
  expect(await screen.findByText(/name is required/i)).toBeInTheDocument();
});
```

## Component Test Coverage

### Admin Components
- **StudentManagement**: List rendering, pagination, search, CRUD operations
- **FacultyManagement**: List rendering, pagination, CRUD operations
- **CourseManagement**: List rendering, CRUD operations
- **SubjectManagement**: List rendering, course-subject mapping
- **ClassAllocationManagement**: Allocation form, validation
- **DashboardHome**: Statistics display, charts rendering

### Faculty Components
- **AttendanceMarking**: Class selection, student list, bulk marking
- **PerformanceEntry**: Assessment form, grade calculation display
- **ReportsView**: Report generation, filters, export functionality
- **FacultyDashboard**: Statistics display, allocated classes

### Student Components
- **AttendanceView**: Subject-wise attendance, percentage display
- **PerformanceView**: Subject-wise performance, GPA display
- **StudentDashboard**: Overall statistics, charts

### Common Components
- **Navbar**: Navigation links, user menu, logout
- **NotificationPanel**: Notification list, mark as read
- **GlobalSearch**: Search input, results display
- **AdvancedFilterModal**: Filter controls, apply/reset

## Running Tests

### Run all tests:
```bash
npm test
```

### Run with coverage:
```bash
npm test -- --coverage
```

### Run specific test file:
```bash
npm test StudentManagement.test.jsx
```

### Watch mode:
```bash
npm test -- --watch
```

## Mocking Strategies

### Mock API Calls
```javascript
import { vi } from 'vitest';
import * as studentApi from '../api/endpoints/studentApi';

vi.mock('../api/endpoints/studentApi');

beforeEach(() => {
  studentApi.getAll.mockResolvedValue({ data: [] });
});
```

### Mock React Router
```javascript
import { MemoryRouter } from 'react-router-dom';

const renderWithRouter = (component) => {
  return render(
    <MemoryRouter>
      {component}
    </MemoryRouter>
  );
};
```

### Mock Auth Context
```javascript
const mockAuthContext = {
  user: { id: '1', role: 'ADMIN' },
  isAuthenticated: true,
  logout: vi.fn(),
};

<AuthContext.Provider value={mockAuthContext}>
  <Component />
</AuthContext.Provider>
```

## Best Practices

1. **Test User Behavior**: Focus on what users see and do, not implementation details
2. **Use Semantic Queries**: Prefer `getByRole`, `getByLabelText` over `getByTestId`
3. **Async Operations**: Use `waitFor`, `findBy` queries for async updates
4. **Clean Up**: Tests should not affect each other, use `beforeEach`/`afterEach`
5. **Meaningful Assertions**: Test actual behavior, not just presence of elements

## Coverage Goals

- **Critical Paths**: 90%+ (authentication, data submission)
- **UI Components**: 70%+ (rendering, interactions)
- **Utility Functions**: 80%+ (helpers, formatters)
- **Overall Target**: 75%+

## Example Test Suite

```javascript
// StudentManagement.test.jsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import StudentManagement from '../pages/admin/StudentManagement';
import * as studentApi from '../api/endpoints/studentApi';

vi.mock('../api/endpoints/studentApi');

describe('StudentManagement', () => {
  beforeEach(() => {
    studentApi.getAll.mockResolvedValue({
      data: {
        content: [
          { id: '1', name: 'John Doe', rollNumber: 'S001' },
          { id: '2', name: 'Jane Smith', rollNumber: 'S002' },
        ],
        totalElements: 2,
      },
    });
  });

  it('renders student list', async () => {
    render(<StudentManagement />);
    
    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });
  });

  it('handles search', async () => {
    const user = userEvent.setup();
    render(<StudentManagement />);
    
    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'John');
    
    expect(studentApi.getAll).toHaveBeenCalledWith(
      expect.objectContaining({ search: 'John' })
    );
  });

  it('opens create form', async () => {
    const user = userEvent.setup();
    render(<StudentManagement />);
    
    const addButton = screen.getByRole('button', { name: /add student/i });
    await user.click(addButton);
    
    expect(screen.getByText(/create student/i)).toBeInTheDocument();
  });
});
```

## Continuous Integration

Add to `package.json`:
```json
{
  "scripts": {
    "test": "vitest",
    "test:ci": "vitest run --coverage",
    "test:ui": "vitest --ui"
  }
}
```

## Next Steps

1. Set up testing infrastructure
2. Write tests for critical user flows
3. Add tests for new features before implementation
4. Review coverage reports regularly
5. Refactor components for better testability
