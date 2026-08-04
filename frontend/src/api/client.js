// ============================================================
// client.js — our one and only "phone line" to the backend.
//
// axios is a library that sends HTTP requests from JavaScript,
// the same kind of requests Postman sends. Instead of creating
// a new connection in every file, we create ONE here and every
// other file imports it. That way the JWT token logic below is
// written once and works everywhere.
// ============================================================
import axios from 'axios'

// baseURL means: every request starts with this address.
// So client.get('/topics') really calls
// http://localhost:8080/api/topics — our Spring Boot backend.
const client = axios.create({
  baseURL: 'http://localhost:8080/api',
})

// ---------- REQUEST INTERCEPTOR ----------
// An interceptor is a function that runs automatically BEFORE
// every request leaves the browser. Here we pick up the JWT token
// (saved in localStorage at login) and attach it as the
// Authorization header — exactly what we did manually in Postman
// with "Bearer <token>". Because of these 6 lines, NO other file
// in the project ever has to think about tokens.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ---------- RESPONSE INTERCEPTOR ----------
// This one runs automatically AFTER every response comes back.
// If the backend answers 401 (token missing/expired/invalid),
// we clear the saved session and send the user to the login page.
// One global rule instead of checking in every page.
client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response && err.response.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)   // let the page also see the error
  }
)

export default client   // "export default" = other files can import this
