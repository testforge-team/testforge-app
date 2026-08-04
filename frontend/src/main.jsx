// ============================================================
// main.jsx — the starting point of the React app.
//
// FIRST, WHAT IS A .jsx FILE?
// JSX is JavaScript that is allowed to contain HTML-like tags.
// Vite converts it into normal JavaScript for the browser.
//
// WHAT IS "import"?
// In HTML you load files with <script src="...">. In React, every
// file says at the top which other files/libraries it needs, using
// import. Names in { curly braces } are specific tools from a
// library; a name without braces is the main thing the file exports.
// ============================================================
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import './styles.css'   // importing a css file simply applies it

// This line finds <div id="root"> in index.html and tells React:
// "draw our app inside this div".
// <BrowserRouter> wraps the app so we can have multiple "pages"
// (like /login, /register) without real page reloads.
ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>
)
