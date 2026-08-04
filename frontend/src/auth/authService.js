// ============================================================
// authService.js — small helper functions for login/register
// and for remembering who is logged in.
//
// WHAT IS localStorage?
// A tiny storage box inside the browser. Whatever we put in it
// stays there even if the page is refreshed. We use it to keep
// the JWT token and the user's name/role/id after login.
//
// WHAT IS async/await?
// Talking to the backend takes time (it goes over the network).
// "await" means "pause this function until the answer arrives".
// A function that uses await must be marked "async".
// ============================================================
import client from '../api/client'

// Called by the Login page. Sends email+password to the backend.
// If they are correct, the backend replies with
// { token, role, name, userId } and we save it.
export async function login(email, password) {
  const res = await client.post('/auth/login', { email, password })
  saveSession(res.data)          // res.data = the JSON the backend sent
  return res.data
}

// Called by the Register page. Same idea; the backend creates the
// account AND logs the user in (it returns a token immediately).
export async function register(name, email, password) {
  const res = await client.post('/auth/register', { name, email, password })
  saveSession(res.data)
  return res.data
}

// Store the four values we need everywhere.
function saveSession(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('role', data.role)
  localStorage.setItem('name', data.name)
  localStorage.setItem('userId', data.userId)
}

// Forget everything — used by the Logout button.
export function logout() {
  localStorage.clear()
}

// Read the saved values back (any page can call this).
export function getSession() {
  return {
    token: localStorage.getItem('token'),
    role: localStorage.getItem('role'),
    name: localStorage.getItem('name'),
    userId: localStorage.getItem('userId'),
  }
}

// Two tiny yes/no questions used by the route guards and navbar.
// "!!" turns a value into true/false: token exists -> true.
export function isLoggedIn() { return !!localStorage.getItem('token') }
export function isAdmin() { return localStorage.getItem('role') === 'ADMIN' }
