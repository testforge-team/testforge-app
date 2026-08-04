// ============================================================
// ProtectedRoute.jsx — the security guard for pages.
//
// NEW IDEA: PROPS.
// Props are values a parent passes INTO a component, like function
// parameters. In App.jsx we will write:
//   <ProtectedRoute adminOnly> <SomeAdminPage/> </ProtectedRoute>
// Here, "children" is whatever was wrapped inside (SomeAdminPage),
// and "adminOnly" is a true/false prop.
//
// HOW IT WORKS: before showing the wrapped page, we check the
// session. Not logged in? Redirect to /login. Student trying an
// admin page? Redirect to /student. Otherwise show the page.
//
// HONEST NOTE (say this in the viva too): this only improves the
// user experience. REAL security is the backend's JWT filter and
// @PreAuthorize — even if someone bypasses this guard, the backend
// still rejects them with 403.
// ============================================================
import { Navigate } from 'react-router-dom'
import { isLoggedIn, isAdmin } from './authService'

export default function ProtectedRoute({ children, adminOnly = false }) {
  if (!isLoggedIn()) return <Navigate to="/login" replace />
  if (adminOnly && !isAdmin()) return <Navigate to="/student" replace />
  return children
}
