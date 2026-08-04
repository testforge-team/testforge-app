// ============================================================
// Navbar.jsx — the black bar on top of every page.
//
// NEW IDEA: CONDITIONAL RENDERING.
// A component can decide WHAT to show using normal if/ternary:
//  - logged out -> show nothing (return null)
//  - admin      -> show admin links
//  - student    -> show student links
// The links for pages that do not exist yet (Person B/C/D's pages)
// are already here — clicking them just shows the placeholder page
// until those teammates add their screens.
// ============================================================
import { Link, useNavigate } from 'react-router-dom'
import { getSession, logout, isAdmin, isLoggedIn } from '../auth/authService'

export default function Navbar() {
  const navigate = useNavigate()

  // No navbar on the login/register screens.
  if (!isLoggedIn()) return null

  const { name } = getSession()

  function handleLogout() {
    logout()             // clear localStorage
    navigate('/login')   // go back to the login page
  }

  return (
    <nav className="navbar navbar-expand navbar-dark bg-dark px-3 mb-4">
      <span className="navbar-brand fw-bold">TestForge</span>

      <div className="navbar-nav">
        {isAdmin() ? (
          <>  {/* <> is a "fragment": lets us group tags without an extra div */}
            <Link className="nav-link" to="/admin">Dashboard</Link>
            <Link className="nav-link" to="/admin/topics">Topics</Link>
            <Link className="nav-link" to="/admin/questions">Questions</Link>
            <Link className="nav-link" to="/admin/exams">Exams</Link>
            <Link className="nav-link" to="/admin/reminders">Reminders</Link>
          </>
        ) : (
          <>
            <Link className="nav-link" to="/student">Dashboard</Link>
            <Link className="nav-link" to="/student/history">My Results</Link>
            <Link className="nav-link" to="/student/performance">Performance</Link>
          </>
        )}
      </div>

      <div className="ms-auto d-flex align-items-center">
        <span className="text-light me-3">{name}</span>
        <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  )
}
