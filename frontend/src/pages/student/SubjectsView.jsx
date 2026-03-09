import { useState, useEffect } from 'react';
import { getMySubjects } from '../../api/endpoints/studentApi';
import { toast } from 'react-toastify';
import './SubjectsView.css';

const SubjectsView = () => {
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSubjects();
  }, []);

  const fetchSubjects = async () => {
    try {
      setLoading(true);
      const response = await getMySubjects();
      setSubjects(response.data);
    } catch (error) {
      toast.error('Failed to fetch subjects');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading subjects...</div>;
  }

  return (
    <div className="subjects-view">
      <div className="page-header">
        <h1>My Subjects</h1>
        <p>View all subjects you are enrolled in</p>
      </div>

      {subjects.length === 0 ? (
        <div className="no-data">
          <p>No subjects found. Please contact your administrator.</p>
        </div>
      ) : (
        <div className="subjects-grid">
          {subjects.map(subject => (
            <div key={subject.id} className="subject-card">
              <div className="subject-header">
                <h3>{subject.name}</h3>
                <span className="subject-code">{subject.code}</span>
              </div>
              <div className="subject-details">
                <div className="detail-row">
                  <span className="label">Credits:</span>
                  <span className="value">{subject.credits}</span>
                </div>
                {subject.facultyName && (
                  <div className="detail-row">
                    <span className="label">Faculty:</span>
                    <span className="value">{subject.facultyName}</span>
                  </div>
                )}
                {subject.semester && (
                  <div className="detail-row">
                    <span className="label">Semester:</span>
                    <span className="value">{subject.semester}</span>
                  </div>
                )}
                {subject.description && (
                  <div className="subject-description">
                    <p>{subject.description}</p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default SubjectsView;
